package semantic;

import common.SemanticException;
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

/**
 * 保证返回的rvalue取值操作得到的是exp的值。
 */
public class ExpGenerator extends InstrumentGenerator{

    private final Exp exp;

    public ExpGenerator(List<Instrument> instruments, Exp exp) {
        super(instruments);
        this.exp = exp;
    }

    public static class Result {
        public VarType type;
        public RValue value;

        public Result(RValue result, VarType type) {
            this.type = type;
            this.value = result;
        }
    }

    private Result result;

    public Result getResult() {
        return result;
    }

    public void generate() {
        execution.exec(exp);
    }


    private final Execution<Exp, Result> execution = new Execution<Exp, Result>() {
        @Override
        protected void inject() {
            inject(BinaryExp.class,  exp -> {

                Result result1 = exec(exp.getExp1());
                Result result2 = exec(exp.getExp2());
                if(result1.value instanceof Constant && result2.value instanceof Constant){
                    return new Result(new Constant(compute(((Constant) result1.value).getNumber(), exp.getOp(), ((Constant) result2.value).getNumber())), new IntType());
                }else if(
                        result1.type instanceof IntType && result2.type instanceof IntType ||
                                result1.type instanceof PointerType && result2.type instanceof IntType && exp.getOp() == BinaryOp.PLUS
                ){
                    Temp ret = valueFactory.newTemp();
                    addInstrument(new BinaryOperation(result1.value, result2.value, exp.getOp(), ret));
                    return new Result(ret, result1.type);
                }else{
                    throw new SemanticException();
                }
            });


            inject(UnaryExp.class,  exp -> {
                Result result = exec(exp.getExp());
                if(result.value instanceof Constant){
                    return new Result(new Constant(compute(((Constant) result.value).getNumber(), exp.getOp())), new IntType());
                }else if(result.type instanceof IntType){
                    Temp ret = valueFactory.newTemp();
                    addInstrument(new UnaryOperation(result.value, exp.getOp(), ret));
                    return new Result(ret, new IntType());
                }else{
                    throw new SemanticException();
                }
            });

            inject(Number.class, exp -> new Result(new Constant(exp.getNumber()), new IntType()));


            inject(FuncCall.class, exp -> {
                FuncCallGenerator funcCallGenerator = new FuncCallGenerator(instruments, exp);
                if(!funcCallGenerator.getResult().isPresent()){
                    throw new SemanticException();
                }
                return new Result(funcCallGenerator.getResult().get(), new IntType());
            });

            inject(LVal.class, exp -> {
                LValGenerator.Result lValResult = new LValGenerator(instruments, exp, false).getResult();
                if(lValResult.identType == LValGenerator.IdentType.Pointer && lValResult.type instanceof IntType){
                    Temp resultValue = valueFactory.newTemp();
                    addInstrument(new Load(resultValue, lValResult.value));
                    return new Result(resultValue, lValResult.type);
                }else{
                    return new Result(lValResult.value, lValResult.type);
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
