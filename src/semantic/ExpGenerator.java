package semantic;

import common.SemanticException;
import lexer.Ident;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.instrument.Load;
import midcode.instrument.UnaryOperation;
import midcode.value.*;
import parser.nonterminal.exp.*;
import parser.nonterminal.exp.Number;
import type.IntType;
import type.PointerType;
import type.VarType;

import java.util.List;
import java.util.Optional;

/**
 * 保证返回的rvalue取值操作得到的是exp的值。
 * 1、要作为调用函数的参数。这时LVal不能是指向常量数组的，类型为指针的PointerType。
 * 2、用于赋值等等。这时得到的Type必须是IntType。
 */
public class ExpGenerator extends InstrumentGenerator{

    private final Exp exp;

    ExpGenerator(Exp exp) {
        this.exp = exp;
        execution.inject();
    }

    public static abstract class Result{
        public RValue getRValueResult(){
            throw new SemanticException();
        }
    }

    public static class RValueResult extends Result{
        public RValue rValue;

        public RValueResult(RValue rValue) {
            this.rValue = rValue;
        }
        @Override
        public RValue getRValueResult(){
            return rValue;
        }
    }

    // 该类不会在中间产生，只会在generate方法中进行转化
    public static class PointerResult extends Result{
        public PointerValue value;
        public PointerType type;

        public PointerResult(PointerValue value, PointerType type) {
            this.value = value;
            this.type = type;
        }
    }

    private static class TempResult extends Result{
        public PointerType pointerType;
        public Ident ident;
        public RValue offset;

        public TempResult(PointerType pointerType, Ident ident, RValue offset) {
            this.pointerType = pointerType;
            this.ident = ident;
            this.offset = offset;
        }
    }

    private static class VoidResult extends Result{

    }


    Result generate() {
        Result result = execution.exec(exp);
        if(result instanceof TempResult){
            PointerValue pointerValue = valueFactory.newPointer(((TempResult) result).ident, ((TempResult) result).offset);
            result = new PointerResult(pointerValue, ((TempResult) result).pointerType);
        }
        return result;
    }


    private final Execution<Exp, Result> execution = new Execution<Exp, Result>() {
        @Override
        public void inject() {
            inject(BinaryExp.class,  exp -> {

                Result result1 = exec(exp.getExp1());
                Result result2 = exec(exp.getExp2());
                BinaryOp op = exp.getOp();

                assert result2 instanceof RValueResult;
                RValue value2 = ((RValueResult) result2).rValue;
                if(result1 instanceof RValueResult){
                    RValue value1 = ((RValueResult) result1).rValue;
                    if(value1 instanceof Constant && value2 instanceof Constant){
                        return new RValueResult(new Constant(compute(((Constant) value1).getNumber(), op, ((Constant) value2).getNumber())));
                    }else {
                        Temp result = valueFactory.newTemp();
                        addInstrument(new BinaryOperation(value1, value2, op, result));
                        return new RValueResult(result);
                    }
                }else if(result1 instanceof TempResult) {
                    //todo 指针减去int？
                    assert op == BinaryOp.PLUS || op == BinaryOp.MINU;
                    PointerType lvalType = ((TempResult) result1).pointerType;
                    RValue offset = ((TempResult) result1).offset;
                    Temp newOffset = valueFactory.newTemp();
                    if(lvalType.getSecondLen().isPresent()){
                        Temp temp1 = valueFactory.newTemp();
                        addInstrument(new BinaryOperation(value2, new Constant(lvalType.getSecondLen().get()), BinaryOp.MULT, temp1));
                        addInstrument(new BinaryOperation(offset, temp1, BinaryOp.PLUS, newOffset));
                    }else{
                        addInstrument(new BinaryOperation(offset, value2, BinaryOp.PLUS, newOffset));
                    }
                    return new TempResult(lvalType, ((TempResult) result1).ident, newOffset);
                }else{
                    throw new SemanticException();
                }
            });


            inject(UnaryExp.class,  exp -> {
                Result result = exec(exp.getExp());
                UnaryOp op = exp.getOp();
                assert result instanceof RValueResult;
                RValue value = ((RValueResult) result).rValue;
                if(value instanceof Constant){
                    return new RValueResult(new Constant(compute(((Constant) value).getNumber(), op)));
                }else{
                    Temp ret = valueFactory.newTemp();
                    addInstrument(new UnaryOperation(value, op, ret));
                    return new RValueResult(ret);
                }
            });

            inject(Number.class, exp -> new RValueResult(new Constant(exp.getNumber())));

            inject(FuncCall.class, exp -> {
                Optional<LValue> returnValue = new FuncCallGenerator(exp).generate();
                if(!returnValue.isPresent()){
                    return new VoidResult();
                }else{
                    return new RValueResult(returnValue.get());
                }
            });

            inject(LVal.class, exp -> {
                LValGenerator.Result lValResult = new LValGenerator(exp).generate();
                if(lValResult instanceof LValGenerator.ConstantResult){
                    return new RValueResult(((LValGenerator.ConstantResult) lValResult).constant);
                }else if(lValResult instanceof LValGenerator.LValueResult){
                    return new RValueResult(((LValGenerator.LValueResult) lValResult).lVal);
                }else if(lValResult instanceof LValGenerator.IntPointerResult){
                    Temp ret = valueFactory.newTemp();
                    addInstrument(new Load(ret, ((LValGenerator.IntPointerResult) lValResult).pointerValue) );
                    return new RValueResult(ret);
                }else if(lValResult instanceof LValGenerator.ArrayPointerResult){
                    return new TempResult(((LValGenerator.ArrayPointerResult) lValResult).pointerType, ((LValGenerator.ArrayPointerResult) lValResult).ident, ((LValGenerator.ArrayPointerResult) lValResult).offset);
                }else{
                    throw new SemanticException();
                }
            });
        }
    };

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
    private boolean toBool(int a ){
        return a != 0;
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
