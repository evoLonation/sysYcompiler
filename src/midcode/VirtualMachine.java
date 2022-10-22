package midcode;

import common.SemanticException;
import lexer.FormatString;
import midcode.instrument.*;
import midcode.value.*;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.UnaryOp;
import semantic.Execution;
import semantic.VoidExecution;

import java.util.*;

public class VirtualMachine {
    private String stdout = "";
    private final String stdin;
    private final Scanner scanner;
    private final int[] staticData;
    private final int[] stack = new int[102400];
    private int sp;
    private final Stack<Map<Temp, Integer>> tempStack = new Stack<>();

    private final Stack<Integer> paramStack = new Stack<>();
    private final Stack<Function> functionStack = new Stack<>();



    private final Module module;

    public VirtualMachine(Module module, String stdin) {
        staticData = module.staticData;
        this.module = module;
        this.stdin = stdin;
        instrumentExecution.inject();
        this.scanner = new Scanner(stdin);
    }

    public void run(){
        run(module.mainFunc, 0);
    }

    private void run(Function function, int paramNumber){
        if(!functionStack.isEmpty()){
            // 多一个用来存储返回值
            sp += functionStack.peek().offset + 1;
        }
        functionStack.push(function);
        tempStack.push(new HashMap<>());
        for(int i = paramNumber - 1; i >= 0; i--){
            stack[sp + i] = paramStack.pop();
        }
        run(function.getEntry());
        tempStack.pop();
        functionStack.pop();
        if(!functionStack.isEmpty()){
            sp = sp - (functionStack.peek().offset + 1);
        }
    }

    private void run(BasicBlock basicBlock) {
        for(Instrument instrument : basicBlock.getInstruments()){
            run(instrument);
        }
        Jump last = basicBlock.lastInstrument;
        if(last instanceof Goto){
            run(((Goto) last).getBasicBlock());
        }else if(last instanceof CondGoto){
            if(getValue(((CondGoto) last).getCond()) != 0){
                run(((CondGoto) last).getTrueBasicBlock());
            }else{
                run(((CondGoto) last).getFalseBasicBlock());
            }
        }else if(last instanceof Return){
            // main函数时sp为0
            if(sp > 1) {
                stack[sp - 1] = ((Return) last).getReturnValue().map(this::getValue).orElse(0);
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
            inject(Assignment.class, assign -> saveValue(assign.getLeft(), getValue(assign.getRight())));

            inject(BinaryOperation.class, operation -> saveValue(operation.getResult(), compute(getValue(operation.getLeft()), operation.getOp(), getValue(operation.getRight()))));

            inject(Call.class, call -> {
                run(call.getFunction(), call.getParamNumber());
                if(call.getRet().isPresent()){
                    saveValue(call.getRet().get(), stack[sp + functionStack.peek().offset]);
                }
            });

            inject(GetInt.class, getint -> saveValue(getint.getlValue(), scanner.nextInt()));

            inject(Load.class, load -> saveValue(load.getLeft(), getValue(load.getRight())));

            inject(Param.class, param ->{
                Value paramValue = param.getValue();
                if(paramValue instanceof RValue){
                    paramStack.push(getValue(paramValue));
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
                        stdout += getValue(rValues.get(i++));
                    }else{
                        throw new SemanticException();
                    }
                }
            });

            inject(Store.class, store -> saveValue(store.getLeft(), getValue(store.getRight())));

            inject(UnaryOperation.class, ope -> saveValue(ope.getResult(), compute(getValue(ope.getValue()), ope.getOp())));


        }
    };

    private void saveValue(LValue lValue, int value){
        if(lValue instanceof Variable){
            Variable variable = (Variable) lValue;
            if(variable.isGlobal()){
                staticData[variable.getOffset()] = value;
            }else{
                stack[sp + variable.getOffset()] = value;
            }
        }else if(lValue instanceof Temp){
            tempStack.peek().put((Temp) lValue, value);
        }
    }
    private void saveValue(PointerValue pointerValue, int value){
        int address = getAddress(pointerValue);
        if(pointerValue.isGlobal()){
            staticData[address] = value;
        }else{
            stack[address] = value;
        }
    }

    private int getValue(Value value){
        if(value instanceof Constant){
            return ((Constant) value).getNumber();
        }else if(value instanceof Temp){
            return tempStack.peek().getOrDefault(value, 0);
        }else if(value instanceof Variable){
            Variable variable = (Variable) value;
            if(variable.isGlobal()){
                return staticData[((Variable) value).getOffset()];
            }else{
                return stack[sp + ((Variable) value).getOffset()];
            }
        }else if(value instanceof PointerValue){
            PointerValue pointerValue = (PointerValue) value;
            int address = getAddress(pointerValue);
            if(pointerValue.isGlobal()){
                return staticData[address];
            }else{
                return stack[address];
            }
        }else{
            throw new SemanticException();
        }
    }

    private int getAddress(PointerValue pointerValue) {
        if(pointerValue.isGlobal()){
            assert pointerValue.getType() == PointerValue.Type.array;
            return pointerValue.getMemOffset();
        }else{
            switch (pointerValue.getType()){
                case array: return sp + pointerValue.getMemOffset() + getValue(pointerValue.getOffset());
                case pointer: return stack[sp + pointerValue.getMemOffset()] + getValue(pointerValue.getOffset());
                default: throw new SemanticException();
            }
        }
    }

    public String getStdout(){
        return stdout;
    }

    private int compute(int a, BinaryOp op, int b){
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
            case AND: return a != 0 && b != 0 ? 1 : 0;
            case OR: return a != 0 || b != 0 ? 1 : 0;
            case EQL: return a == b ? 1 : 0;
            default: throw new SemanticException();
        }
    }
    private int compute(int a, UnaryOp op){
        switch (op){
            case MINU: a = - a; break;
            case NOT: a = a != 0 ? 0 : 1; break;
            case PLUS: break;
        }
        return a;
    }

}
