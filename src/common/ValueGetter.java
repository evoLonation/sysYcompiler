package common;

import midcode.BasicBlock;
import midcode.instruction.*;
import midcode.value.*;
import util.Execution;
import util.VoidExecution;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueGetter {
    // repeatable
    public List<LValue> getAllValues(Instruction instruction){
        return Stream.concat(getUseValues(instruction).stream(),
                getDefValue(instruction).map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    /**
     * 这里面包括了lvalue和addressValue，没有包括constant（因为一般constant不需要存到寄存器里）
      */
    public List<LValue> getUseValues(Instruction instruction){
//        return new ArrayList<>(useValueExecution.exec(instruction));
        return useValueExecution.exec(instruction).stream()
                .map(value -> value instanceof AddressValue ? ((AddressValue) value).getOffset() : value)
                .filter(value -> value instanceof LValue).map(value -> (LValue) value)
                .collect(Collectors.toList());
    }

    public Optional<LValue> getDefValue(Instruction instruction){
        return defValueExecution.exec(instruction);
    }

    private LValue setDefValue;
    public void setDefValue(Instruction instruction, LValue lValue){
        //todo
        setDefValue = lValue;
        setDefValueExecution.exec(instruction);
    }



    public Set<BasicBlock> getJumpBasicBlock(Jump jump){
        return jumpSetExecution.exec(jump);
    }

    private final Execution<Jump, Set<BasicBlock>> jumpSetExecution = new Execution<Jump, Set<BasicBlock>>() {
        @Override
        public void inject() {
            inject(param -> Collections.emptySet());
            inject(Goto.class, param -> Collections.singleton(param.getBasicBlock()));
            inject(CondGoto.class, param -> Stream.concat(Stream.of(param.getFalseBasicBlock()), Stream.of(param.getTrueBasicBlock())).collect(Collectors.toSet()));
        }
    };

    //todo只返回lvalue的use
    private final Execution<Instruction, List<Value>> useValueExecution = new Execution<Instruction, List<Value>>() {
        @Override
        public void inject() {
            inject(param -> new ArrayList<>());
            inject(BinaryOperation.class , param -> Arrays.asList(param.getLeft(), param.getRight()));
            inject(UnaryOperation.class, param -> Collections.singletonList(param.getValue()));
            inject(Assignment.class, param -> Collections.singletonList(param.getRight()));
            inject(Return.class, param -> param.getReturnValue().map(r -> Collections.singletonList((Value)r)).orElse(Collections.emptyList()));
            inject(CondGoto.class, param -> Collections.singletonList(param.getCond()));
            inject(Param.class, param -> Collections.singletonList(param.getValue()));
            inject(PrintInt.class, param -> Collections.singletonList(param.getRValue()));
            inject(Store.class, param -> Arrays.asList(param.getRight(), param.getLeft()));
            inject(Load.class, param -> Collections.singletonList(param.getRight()));
        }
    };

    private final Execution<Instruction, Optional<LValue>> defValueExecution = new Execution<Instruction, Optional<LValue>>() {
        @Override
        public void inject() {
            inject(param -> Optional.empty());
            inject(BinaryOperation.class , param -> Optional.of(param.getResult()));
            inject(UnaryOperation.class, param -> Optional.of(param.getResult()));
            inject(Assignment.class, param -> Optional.of(param.getLeft()));
            inject(Call.class, param -> param.getRet().map(value -> value));
            inject(GetInt.class, param -> Optional.of(param.getLeft()));
            inject(Load.class, param -> Optional.of(param.getLeft()));
            inject(ImplicitDef.class, param -> Optional.of(param.getVariable()));
        }
    };

    private final VoidExecution<Instruction> setDefValueExecution = new VoidExecution<Instruction>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class , param -> param.setResult(setDefValue));
            inject(UnaryOperation.class, param -> param.setResult(setDefValue));
            inject(Assignment.class, param -> param.setLeft(setDefValue));
            inject(Call.class, param -> param.setRet(setDefValue));
            inject(GetInt.class, param -> param.setLeft(setDefValue));
            inject(Load.class, param -> param.setLeft(setDefValue));
            inject(ImplicitDef.class, param -> param.setVariable((Variable) setDefValue));
        }
    };


    private LValue originLValue;
    private LValue targetLValue;
    private interface Setter{
        void set(LValue lValue);
    }
    private interface Getter<T extends RValue>{
        RValue get();
    }
    private class SetterChain{
        private final List<Setter> setters = new ArrayList<>();
        private final List<Getter<? extends RValue>> getters = new ArrayList<>();
        SetterChain add(Getter<? extends RValue> getter, Setter setter){
            setters.add(setter);
            getters.add(getter);
            return this;
        }
        void exec(){
            for(int i = 0; i < setters.size(); i++){
                if(getters.get(i).get().equals(originLValue)){
                    setters.get(i).set(targetLValue);
                }
            }
        }
    }
    private final SetterChain setterChain = new SetterChain();

    private final VoidExecution<Instruction> setUseValueExecution = new VoidExecution<Instruction>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class, param -> setterChain.add(param::getLeft, param::setLeft).add(param::getRight, param::setRight).exec());
        }
    };


    private ValueGetter() {
        useValueExecution.inject();
        defValueExecution.inject();
        jumpSetExecution.inject();
        setDefValueExecution.inject();
    }
    static private final ValueGetter instance = new ValueGetter();
    static public ValueGetter getInstance(){
        return instance;
    }
}
