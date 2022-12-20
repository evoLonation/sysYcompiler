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
        return Stream.concat(getLValueUseValues(instruction).stream(),
                getDefValue(instruction).map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    /**
     * 这里面包括了lvalue和addressValue，没有包括constant（因为一般constant不需要存到寄存器里）
      */
    public List<LValue> getLValueUseValues(Instruction instruction){
//        return new ArrayList<>(useValueExecution.exec(instruction));
        return getUseGetterSetter(instruction).stream()
                .map(GetterSetter::get)
                .filter(rValue -> rValue instanceof LValue).map(rValue -> (LValue)rValue)
                .collect(Collectors.toList());
//        return useValueExecution.exec(instruction).stream()
//                .map(value -> value instanceof AddressValue ? ((AddressValue) value).getOffset() : value)
//                .filter(value -> value instanceof LValue).map(value -> (LValue) value)
//                .collect(Collectors.toList());
    }
    public List<RValue> getUseValues(Instruction instruction){
        return getUseGetterSetter(instruction).stream().map(GetterSetter::get).collect(Collectors.toList());
    }

    public Optional<LValue> getDefValue(Instruction instruction){
        return getDefGetterSetter(instruction).map(GetterSetter::get);
//        return defValueExecution.exec(instruction);
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
            inject(PrintInt.class, param -> Collections.singletonList(param.getValue()));
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

    private final Execution<Instruction, GetterSetter<LValue>> getDefGetterSetterExecution = new Execution<Instruction, GetterSetter<LValue>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class, param -> new GetterSetter<>(param::getResult, param::setResult));
            inject(UnaryOperation.class, param -> new GetterSetter<>(param::getResult, param::setResult));
            inject(Assignment.class, param -> new GetterSetter<>(param::getLeft, param::setLeft));
            inject(Call.class, param -> {
                if(param.getRet().isPresent()){
                    return new GetterSetter<>(() -> param.getRet().get(), param::setRet);
                }else{
                    return null;
                }
            });
            inject(GetInt.class, param -> new GetterSetter<>(param::getLeft, param::setLeft));
            inject(Load.class, param -> new GetterSetter<>(param::getLeft, param::setLeft));
            inject(ImplicitDef.class, param -> new GetterSetter<>(param::getVariable, value -> param.setVariable((Variable) value)));
            inject(Goto.class, param -> null);
            inject(PrintString.class, param -> null);
            inject(CondGoto.class, param -> null);
            inject(Param.class, param -> null);
            inject(PrintInt.class, param -> null);
            inject(Return.class, param -> null);
            inject(Store.class, param -> null);
        }
    };


    private GetterSetter<RValue> getRValueGSetter(GetterSetter<Value> getterSetter){
        Value value = getterSetter.get();
        if(value instanceof AddressValue){
            AddressValue addressValue = (AddressValue) value;
            return new GetterSetter<>(addressValue::getOffset, addressValue::setOffset);
        }else if(value instanceof RValue){
            return new GetterSetter<>(() -> (RValue) getterSetter.get(), getterSetter::set);
        }else{
            throw new SemanticException();
        }
    }


    private final Execution<Instruction, List<GetterSetter<RValue>>> getUseGetterSetterExecution = new Execution<Instruction, List<GetterSetter<RValue>>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class, param -> Arrays.asList(new GetterSetter<>(param::getLeft, param::setLeft), new GetterSetter<>(param::getRight, param::setRight)));
            inject(UnaryOperation.class, param -> Collections.singletonList(new GetterSetter<>(param::getValue, param::setValue)));
            inject(Assignment.class, param -> Collections.singletonList(new GetterSetter<>(param::getRight, param::setRight)));
            inject(Return.class, param -> param.getReturnValue().map(r -> Collections.singletonList(
                    new GetterSetter<>(() -> param.getReturnValue().get(), param::setReturnValue))).orElse(Collections.emptyList()));
            inject(CondGoto.class, param -> Collections.singletonList(new GetterSetter<>(param::getCond, param::setCond)));
            inject(Param.class, param -> Collections.singletonList(getRValueGSetter(new GetterSetter<>(param::getValue, param::setValue))));
            inject(PrintInt.class, param -> Collections.singletonList(new GetterSetter<>(param::getValue, param::setValue)));
            inject(Store.class, param -> Arrays.asList(
                    new GetterSetter<>(() -> param.getLeft().getOffset(), value -> param.getLeft().setOffset(value)),
                    new GetterSetter<>(param::getRight, param::setRight)));
            inject(Load.class, param -> Collections.singletonList(new GetterSetter<>(() -> param.getRight().getOffset(), value -> param.getRight().setOffset(value))));
            inject(Call.class, param -> Collections.emptyList());
            inject(GetInt.class, param -> Collections.emptyList());
            inject(Goto.class, param -> Collections.emptyList());
            inject(ImplicitDef.class, param -> Collections.emptyList());
            inject(PrintString.class, param -> Collections.emptyList());

        }
    };

    public <TT extends Instruction> void addUseDefGSetter(Class<TT> clazz, Execution.Executor<TT, GetterSetter<LValue>> def, Execution.Executor<TT, List<GetterSetter<RValue>>> use){
        getUseGetterSetterExecution.inject(clazz, use);
        getDefGetterSetterExecution.inject(clazz, def);
    }

    private interface Setter<T>{
        void set(T value);
    }
    private interface Getter<T>{
        T get();
    }
    public static class GetterSetter<T>{
        private final Setter<T> setter;
        private final Getter<T> getter;
        void set(T value){
            setter.set(value);
        }
        T get(){
            return getter.get();
        }

        public GetterSetter(Getter<T> getter, Setter<T> setter) {
            this.setter = setter;
            this.getter = getter;
        }
    }

    public Optional<GetterSetter<LValue>> getDefGetterSetter(Instruction instruction){
        return Optional.ofNullable(getDefGetterSetterExecution.exec(instruction));
    }
    public List<GetterSetter<RValue>> getUseGetterSetter(Instruction instruction){
        return getUseGetterSetterExecution.exec(instruction);
    }



    private ValueGetter() {
        useValueExecution.inject();
        defValueExecution.inject();
        jumpSetExecution.inject();
        setDefValueExecution.inject();
        getDefGetterSetterExecution.inject();
        getUseGetterSetterExecution.inject();
    }
    static private final ValueGetter instance = new ValueGetter();
    static public ValueGetter getInstance(){
        return instance;
    }
}
