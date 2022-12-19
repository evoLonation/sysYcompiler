package vm;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.Function;
import midcode.Module;
import midcode.instruction.*;
import midcode.value.*;
import util.VoidExecution;

import java.util.*;

//todo stack的默认值是null，是个问题
public class VirtualMachine {
    private String stdout = "";
    private final Scanner scanner;
//    private final IntValue[] staticData;
    private final StaticData staticData;
//    private final ValueValue[] stack = new ValueValue[102400];
    // 16M
    private final MemActiveStack memActiveStack = new MemActiveStack(1 << 24);
    private final Stack<Map<Temp, IntValue>> tempStack = new Stack<>();

    private final Stack<ValueValue> paramStack = new Stack<>();
    private final Stack<Function> functionStack = new Stack<>();

    static class StaticData {
        // data数据的前部分是默认值为0的，后部分是手动初始化的
        private final int[] data;

        /**
         * @param data 初始化了的数据
         * @param zeroNumber 未初始化（自动零值）的数据的长度
         */
        StaticData(int[] data, int zeroNumber) {
            //todo test
            this.data = new int[zeroNumber + data.length];
            System.arraycopy(data, 0, this.data, zeroNumber, data.length + zeroNumber);
        }

        IntValue get(int i) {
            return new IntValue(data[i]);
        }

        void set(IntValue intValue, int i){
            data[i] = intValue.value;
        }
    }

    static class MemActiveStack {
        private final int[] valueStack;
        private final boolean[] isGlobalStack;
        private final boolean[] isPointerStack;
        private final int size;
        private int sp;

        public MemActiveStack(int size) {
            // 数组初始化都是默认值
            valueStack = new int[size];
            isGlobalStack = new boolean[size];
            isPointerStack = new boolean[size];
            this.size = size;
            sp = 1;
        }

        void push(int offset){
            sp += offset;
        }

        void pop(int offset){
            sp -= offset;
        }

        int getSp() {
            return sp;
        }

        private ValueValue getAbsolute(int address){
            check( address < size);
            if(isPointerStack[address]){
                return new Address(valueStack[address], isGlobalStack[address]);
            }else{
                return new IntValue(valueStack[address]);
            }
        }

        private ValueValue get(int i){
            return getAbsolute(sp + i);
        }
        public void set(ValueValue valueValue, int i){
            setAbsolute(valueValue, sp + i);
        }

        public void setAbsolute(ValueValue valueValue, int address) {
            check(address < size);
            if(valueValue instanceof Address){
                isPointerStack[address] = true;
                valueStack[address] = ((Address) valueValue).address;
                isGlobalStack[address] = ((Address) valueValue).isGlobal;
            }else if(valueValue instanceof IntValue){
                isPointerStack[address] = false;
                valueStack[address] = ((IntValue) valueValue).value;
            }else{
                throw new SemanticException();
            }
        }
    }

    static private void check(boolean cond) {
        if(!cond){
            throw new SemanticException();
        }
    }


    private final Module module;

    public VirtualMachine(Module module, String stdin) {
        //todo zero number
        staticData = new StaticData(module.getStaticData(), 0);
        this.module = module;
        instructionVoidExecution.inject();
        if(stdin == null){
            this.scanner = new Scanner(System.in);
        }else {
            this.scanner = new Scanner(stdin);
        }
    }

    public VirtualMachine(Module module) {
        this(module, null);
    }

    public void run(){
        run(module.getMainFunc());
    }

    private void run(Function function) {
        if(!functionStack.isEmpty()){
            // 多一个用来存储返回值
            memActiveStack.push(functionStack.peek().getOffset() + 1);
        }
        functionStack.push(function);
        tempStack.push(new HashMap<>());
        while(!paramStack.isEmpty()){
            memActiveStack.set(paramStack.pop(), paramStack.size());
        }
        run(function.getEntry());
        tempStack.pop();
        functionStack.pop();
        if(!functionStack.isEmpty()){
            memActiveStack.pop(functionStack.peek().getOffset() + 1);
        }
    }

    private void run(BasicBlock basicBlock) {
        for(Instruction instruction : basicBlock.getSequenceList()){
            run(instruction);
        }
        Jump last = basicBlock.getJump();
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
            memActiveStack.set(((Return) last).getReturnValue().map(this::getIntValue).orElse(new IntValue(0)), -1);
        }else{
            throw new SemanticException();
        }
    }

    private void run(Instruction instruction){
        instructionVoidExecution.exec(instruction);
    }


    private final VoidExecution<Instruction> instructionVoidExecution = new VoidExecution<Instruction>() {
        @Override
        public void inject() {
            inject(ImplicitDef.class, param -> {});

            inject(Assignment.class, assign -> saveValueToLValue(assign.getLeft(), getIntValue(assign.getRight())));

            inject(BinaryOperation.class, operation -> saveValueToLValue(operation.getResult(), new IntValue(compute(getIntValue(operation.getLeft()).value, operation.getOp(), getIntValue(operation.getRight()).value))));

            inject(Call.class, call -> {
                run(call.getFunction());
                if(call.getRet().isPresent()){
                    ValueValue ret = memActiveStack.get(functionStack.peek().getOffset());
                    check(ret instanceof IntValue);
                    saveValueToLValue(call.getRet().get(), (IntValue) ret);
                }
            });

            inject(GetInt.class, getint -> saveValueToLValue(getint.getLeft(), new IntValue(scanner.nextInt())));

            inject(Load.class, load -> saveValueToLValue(load.getLeft(), (IntValue) getValue(getAddress(load.getRight()))));

            inject(Param.class, param ->{
                Value paramValue = param.getValue();
                if(paramValue instanceof RValue){
                    paramStack.push(getIntValue((RValue) paramValue));
                }else if(paramValue instanceof AddressValue){
                    paramStack.push(getAddress((AddressValue) paramValue));
                }else{
                    throw new SemanticException();
                }
            });

            inject(PrintInt.class, printf -> stdout += getIntValue(printf.getRValue()).value);
            inject(PrintString.class, printf -> stdout += printf.getString().replace("\\n", "\n"));

            inject(Store.class, store -> saveValueToAddress(getAddress(store.getLeft()), getIntValue(store.getRight())));

            inject(UnaryOperation.class, ope -> saveValueToLValue(ope.getResult(), new IntValue(compute(getIntValue(ope.getValue()).value, ope.getOp()))));


        }
    };


    private void saveValueToLValue(LValue lValue, IntValue value){
        if(lValue instanceof Variable){
            Variable variable = (Variable) lValue;
            if(variable.isGlobal()){
                staticData.set(value, variable.getOffset());
            }else{
                memActiveStack.set(value, variable.getOffset());
            }
        }else if(lValue instanceof Temp) {
            tempStack.peek().put((Temp) lValue, value);
        }
    }



    private void saveValueToAddress(Address address, IntValue value){
        if(address.isGlobal){
            staticData.set(value, address.address);
        }else{
            memActiveStack.setAbsolute(value, address.address);
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
                return staticData.get(((Variable) value).getOffset());
            }else{
                ValueValue ret = memActiveStack.get(((Variable) value).getOffset());
                check(ret instanceof IntValue);
                return (IntValue) ret;
            }
        }else{
            throw new SemanticException();
        }
    }

    private Address getAddress(AddressValue addressValue){
        int offset = getIntValue(addressValue.getOffset()).value;
        int base;
        boolean isGlobal;
        if(addressValue instanceof PointerValue){
            ValueValue ret = memActiveStack.get(addressValue.getStaticOffset());
            check(ret instanceof Address);
            base = ((Address) ret).address;
            isGlobal = ((Address) ret).isGlobal;
        }else{
            ArrayValue arrayValue = (ArrayValue) addressValue;
            if(arrayValue.isGlobal()){
                isGlobal = true;
                base = arrayValue.getStaticOffset();
            }else{
                isGlobal = false;
                base = memActiveStack.getSp() + arrayValue.getStaticOffset();
            }
        }
        int address;
        if(isGlobal){
            address = base + offset;
        }else{
            address = base - offset;
        }
        return new Address(address, isGlobal);

    }

    private ValueValue getValue(Address address){
        if(address.isGlobal){
            return staticData.get(address.address);
        }else{
            return memActiveStack.getAbsolute(address.address);
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
