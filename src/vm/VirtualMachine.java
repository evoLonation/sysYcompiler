package vm;

import common.SemanticException;
import frontend.lexer.FormatString;
import midcode.BasicBlock;
import midcode.Function;
import midcode.Module;
import midcode.instrument.*;
import midcode.value.*;
import frontend.parser.nonterminal.exp.BinaryOp;
import frontend.parser.nonterminal.exp.UnaryOp;
import util.VoidExecution;

import java.util.*;

//todo stack的默认值是null，是个问题
public class VirtualMachine {
    private String stdout = "";
    private final Scanner scanner;
    private final IntValue[] staticData;
//    private final ValueValue[] stack = new ValueValue[102400];
    private final FuncStack funcStack = new FuncStack(102400);
    private int sp;
    private final Stack<Map<Temp, IntValue>> tempStack = new Stack<>();

    private final Stack<ValueValue> paramStack = new Stack<>();
    private final Stack<Function> functionStack = new Stack<>();


    static class FuncStack{
        private final int[] valueStack;
        private final boolean[] isGlobalStack;
        private final boolean[] isAddressStack;
        private final int size;

        public FuncStack(int size) {
            valueStack = new int[size];
            isGlobalStack = new boolean[size];
            isAddressStack = new boolean[size];
            this.size = size;
        }

        public ValueValue get(int i){
            assert i < size;
            if(isAddressStack[i]){
                return new Address(valueStack[i], isGlobalStack[i]);
            }else{
                return new IntValue(valueStack[i]);
            }
        }

        public void set(ValueValue valueValue, int i){
            assert i < size;
            if(valueValue instanceof Address){
                isAddressStack[i] = true;
                valueStack[i] = ((Address) valueValue).address;
                isGlobalStack[i] = ((Address) valueValue).isGlobal;
            }else if(valueValue instanceof IntValue){
                isAddressStack[i] = false;
                valueStack[i] = ((IntValue) valueValue).value;
            }else{
                throw new SemanticException();
            }
        }
    }


    private final Module module;

    public VirtualMachine(Module module, String stdin) {
        staticData = new IntValue[module.getStaticData().length];
        for(int i = 0; i < staticData.length; i++){
            staticData[i] = new IntValue(module.getStaticData()[i]);
        }
        this.module = module;
        instrumentExecution.inject();
        this.scanner = new Scanner(stdin);
    }

    public VirtualMachine(Module module) {
        staticData = new IntValue[module.getStaticData().length];
        for(int i = 0; i < staticData.length; i++){
            staticData[i] = new IntValue(module.getStaticData()[i]);
        }
        this.module = module;
        instrumentExecution.inject();
        this.scanner = new Scanner(System.in);
    }

    public void run(){
        run(module.getMainFunc(), 0);
    }

    private void run(Function function, int paramNumber) {
        if(!functionStack.isEmpty()){
            // 多一个用来存储返回值
            sp += functionStack.peek().getOffset() + 1;
        }
        functionStack.push(function);
        tempStack.push(new HashMap<>());
        for(int i = paramNumber - 1; i >= 0; i--){
            funcStack.set(paramStack.pop(), sp + i);
        }
        run(function.getEntry());
        tempStack.pop();
        functionStack.pop();
        if(!functionStack.isEmpty()){
            sp = sp - (functionStack.peek().getOffset() + 1);
        }
    }

    private void run(BasicBlock basicBlock) {
        for(Instrument instrument : basicBlock.getInstruments()){
            run(instrument);
        }
        Jump last = basicBlock.getLastInstrument();
        if(last instanceof Goto){
            run(((Goto) last).getBasicBlock());
        }else if(last instanceof CondGoto){
            if(getIntValue(((CondGoto) last).getCond()).value != 0) {
                run(((CondGoto) last).getTrueBasicBlock());
            }else{
                run(((CondGoto) last).getFalseBasicBlock());
            }
        }else if(last instanceof Return){
            // main函数时sp为0
            if(sp > 0) {
                funcStack.set(((Return) last).getReturnValue().map(this::getIntValue).orElse(new IntValue(0)), sp - 1);
            }
        }else{
            throw new SemanticException();
        }
    }

    private void run(Instrument instrument){
        instrumentExecution.exec(instrument);
    }


    private final VoidExecution<Instrument> instrumentExecution = new VoidExecution<Instrument>() {
        @Override
        public void inject() {
            inject(Assignment.class, assign -> saveValueToLValue(assign.getLeft(), getIntValue(assign.getRight())));

            inject(BinaryOperation.class, operation -> saveValueToLValue(operation.getResult(), new IntValue(compute(getIntValue(operation.getLeft()).value, operation.getOp(), getIntValue(operation.getRight()).value))));

            inject(Call.class, call -> {
                run(call.getFunction(), call.getParamNumber());
                if(call.getRet().isPresent()){
                    ValueValue ret = funcStack.get(sp + functionStack.peek().getOffset());
                    assert ret instanceof IntValue;
                    saveValueToLValue(call.getRet().get(), (IntValue) ret);
                }
            });

            inject(GetInt.class, getint -> saveValueToLValue(getint.getlValue(), new IntValue(scanner.nextInt())));

            inject(Load.class, load -> saveValueToLValue(load.getLeft(), (IntValue) getValue(getAddress(load.getRight()))));

            inject(Param.class, param ->{
                Value paramValue = param.getValue();
                if(paramValue instanceof RValue){
                    paramStack.push(getIntValue((RValue) paramValue));
                }else if(paramValue instanceof PointerValue){
                    paramStack.push(getAddress((PointerValue) paramValue));
                }else{
                    throw new SemanticException();
                }
            });

            inject(Printf.class, printf ->{
                List<RValue> rValues = printf.getRValues();
                int i = 0;
                for(FormatString.Char chr : printf.getFormatString().getCharList()){
                    if(chr instanceof FormatString.NormalChar){
                        stdout += ((FormatString.NormalChar) chr).getValue();
                    }else if(chr instanceof FormatString.FormatChar) {
                        stdout += getIntValue(rValues.get(i++)).value;
                    }else{
                        throw new SemanticException();
                    }
                }
            });

            inject(Store.class, store -> saveValueToAddress(getAddress(store.getLeft()), getIntValue(store.getRight())));

            inject(UnaryOperation.class, ope -> saveValueToLValue(ope.getResult(), new IntValue(compute(getIntValue(ope.getValue()).value, ope.getOp()))));


        }
    };


    private void saveValueToLValue(LValue lValue, IntValue value){
        if(lValue instanceof Variable){
            Variable variable = (Variable) lValue;
            if(variable.isGlobal()){
                staticData[variable.getOffset()] = value;
            }else{
                funcStack.set(value, sp + variable.getOffset());
            }
        }else if(lValue instanceof Temp) {
            tempStack.peek().put((Temp) lValue, value);
        }
    }



    private void saveValueToAddress(Address address, IntValue value){
        if(address.isGlobal){
            staticData[address.address] = value;
        }else{
            funcStack.set(value, address.address);
        }
    }


    private IntValue getIntValue(RValue value){
        if(value instanceof Constant){
            return new IntValue(((Constant) value).getNumber());
        }else if(value instanceof Temp){
            return tempStack.peek().getOrDefault(value, new IntValue(0));
        }else if(value instanceof Variable){
            Variable variable = (Variable) value;
            if(variable.isGlobal()){
                return staticData[((Variable) value).getOffset()];
            }else{
                ValueValue ret = funcStack.get(sp + ((Variable) value).getOffset());
                if(ret == null){
                    ret = new IntValue(0);
                }
                assert ret instanceof IntValue;
                return (IntValue) ret;
            }
        }else{
            throw new SemanticException();
        }
    }

    private Address getAddress(PointerValue pointerValue){
        int offset = getIntValue(pointerValue.getOffset()).value;
        int base;
        boolean isGlobal;
        if(pointerValue.isGlobal()){
            isGlobal = true;
            assert pointerValue.getType() == PointerValue.Type.array;
            base = pointerValue.getStaticOffset();
        }else{
            switch (pointerValue.getType()){
                case array:
                    isGlobal = false;
                    base = sp + pointerValue.getStaticOffset();
                    break;
                case pointer:
                    ValueValue ret = funcStack.get(sp + pointerValue.getStaticOffset());
                    assert ret instanceof Address;
                    base = ((Address) ret).address;
                    isGlobal = ((Address) ret).isGlobal;
                    break;
                default: throw new SemanticException();
            }
        }
        return new Address(base + offset, isGlobal);
    }

    private ValueValue getValue(Address address){
        if(address.isGlobal){
            return staticData[address.address];
        }else{
            return funcStack.get(address.address);
        }
    }


    // value的值
    static class ValueValue {

    }
    static class IntValue extends ValueValue{
        int value;
        IntValue(int value) {
            this.value = value;
        }
    }
    static class Address extends ValueValue{
        // 这里的offset是绝对地址（对于stack或者staticData的绝对）
        int address;
        boolean isGlobal;
        Address(int address, boolean isGlobal) {
            this.address = address;
            this.isGlobal = isGlobal;
        }
    }


    public String getStdout(){
        return stdout;
    }

    private int compute(int a, BinaryOperation.BinaryOp op, int b){
        switch (op){
            case PLUS: return a + b;
            case MINU: return a - b;
            case MULT: return a * b;
            case DIV: return  a / b;
            case MOD: return a % b;
            case LEQ: return a <= b ? 1 : 0;
            case GRE: return a > b ? 1 : 0;
            case GEQ: return a >= b ? 1 : 0;
            case LSS: return a < b ? 1 : 0;
            case NEQ: return a != b ? 1 : 0;
            case EQL: return a == b ? 1 : 0;
            default: throw new SemanticException();
        }
    }
    private int compute(int a, UnaryOperation.UnaryOp op){
        switch (op){
            case MINU: a = - a; break;
            case NOT: a = a != 0 ? 0 : 1; break;
            case PLUS: break;
        }
        return a;
    }



}
