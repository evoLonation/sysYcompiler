package frontend.IRGenerate;

import common.SemanticException;
import frontend.lexer.Ident;
import midcode.instruction.BinaryOperation;
import midcode.instruction.Load;
import midcode.instruction.UnaryOperation;
import midcode.value.*;
import frontend.parser.nonterminal.exp.*;
import frontend.parser.nonterminal.exp.Number;
import frontend.type.PointerType;
import util.Execution;

import java.util.Optional;

/**
 * 没有||和&&的exp
 * 保证返回的rvalue取值操作得到的是exp的值。
 * 1、要作为调用函数的参数。这时LVal不能是指向常量数组的，类型为指针的PointerType。
 * 2、用于赋值等等。这时得到的Type必须是IntType。
 */
public class ExpGenerator extends SequenceGenerator {

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
        public AddressValue value;
        public PointerType type;

        public PointerResult(AddressValue value, PointerType type) {
            this.value = value;
            this.type = type;
        }
    }

    private static class TempResult extends Result{
        public PointerType pointerType;
        public Ident ident;
        public Exp offsetExp;

        public TempResult(PointerType pointerType, Ident ident, Exp offsetExp) {
            this.pointerType = pointerType;
            this.ident = ident;
            this.offsetExp = offsetExp;
        }
    }

    private static class VoidResult extends Result{

    }


    Result generate() {
        Result result = execution.exec(exp);
        if(result instanceof TempResult){
            AddressValue addressValue = valueFactory.newPointer(((TempResult) result).ident, new ExpGenerator(((TempResult) result).offsetExp).generate().getRValueResult());
            result = new PointerResult(addressValue, ((TempResult) result).pointerType);
        }
        return result;
    }


    private final Execution<Exp, Result> execution = new Execution<Exp, Result>() {
        @Override
        public void inject() {
            inject(BinaryExp.class,  exp -> {
                Result result1 = exec(exp.getExp1());
                if(result1 instanceof RValueResult){
                    Result result2 = exec(exp.getExp2());
                    BinaryOperation.BinaryOp op = mapOp(exp.getOp());
                    assert result2 instanceof RValueResult;
                    RValue value2 = ((RValueResult) result2).rValue;
                    RValue value1 = ((RValueResult) result1).rValue;
                    if(value1 instanceof Constant && value2 instanceof Constant){
                        return new RValueResult(new Constant(compute(((Constant) value1).getNumber(), op, ((Constant) value2).getNumber())));
                    }else {
                        Temp result = valueFactory.newTemp();
                        addSequence(new BinaryOperation(value1, value2, op, result));
                        return new RValueResult(result);
                    }
                }else if(result1 instanceof TempResult) {
                    BinaryOp op = exp.getOp();
                    assert op == BinaryOp.PLUS || op == BinaryOp.MINU;
                    PointerType lvalType = ((TempResult) result1).pointerType;
                    Exp offsetExp = ((TempResult) result1).offsetExp;
                    Exp newOffsetExp;
                    if(lvalType.getSecondLen().isPresent()){
                        newOffsetExp = new BinaryExp(offsetExp, op, new BinaryExp(new Number(lvalType.getSecondLen().get()), BinaryOp.MULT, exp.getExp2()));
                    }else{
                        newOffsetExp = new BinaryExp(offsetExp, op, exp.getExp2());
                    }
                    return new TempResult(lvalType, ((TempResult) result1).ident, newOffsetExp);
                }else{
                    throw new SemanticException();
                }
            });


            inject(UnaryExp.class,  exp -> {
                Result result = exec(exp.getExp());
                UnaryOperation.UnaryOp op = mapOp(exp.getOp());
                assert result instanceof RValueResult;
                RValue value = ((RValueResult) result).rValue;
                if(value instanceof Constant){
                    return new RValueResult(new Constant(compute(((Constant) value).getNumber(), op)));
                }else{
                    Temp ret = valueFactory.newTemp();
                    addSequence(new UnaryOperation(value, op, ret));
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
                    addSequence(new Load(ret, ((LValGenerator.IntPointerResult) lValResult).addressValue) );
                    return new RValueResult(ret);
                }else if(lValResult instanceof LValGenerator.ArrayPointerResult){
                    return new TempResult(((LValGenerator.ArrayPointerResult) lValResult).pointerType, ((LValGenerator.ArrayPointerResult) lValResult).ident, ((LValGenerator.ArrayPointerResult) lValResult).offsetExp);
                }else{
                    throw new SemanticException();
                }
            });
        }
    };

    private UnaryOperation.UnaryOp mapOp(UnaryOp unaryOp){
        switch (unaryOp){
            case NOT: return UnaryOperation.UnaryOp.NOT;
            case MINU: return UnaryOperation.UnaryOp.MINU;
            case PLUS: return UnaryOperation.UnaryOp.PLUS;
        }
        throw new SemanticException();
    }
    private BinaryOperation.BinaryOp mapOp(BinaryOp binaryOp){
        switch (binaryOp){
            case PLUS: return BinaryOperation.BinaryOp.PLUS;
            case MINU: return BinaryOperation.BinaryOp.MINU;
            case EQL: return BinaryOperation.BinaryOp.EQL;
            case NEQ: return BinaryOperation.BinaryOp.NEQ;
            case MULT: return BinaryOperation.BinaryOp.MULT;
            case MOD: return BinaryOperation.BinaryOp.MOD;
            case LSS: return BinaryOperation.BinaryOp.LSS;
            case DIV: return BinaryOperation.BinaryOp.DIV;
            case GRE: return BinaryOperation.BinaryOp.GRE;
            case GEQ: return BinaryOperation.BinaryOp.GEQ;
            case LEQ: return BinaryOperation.BinaryOp.LEQ;
        }
        throw new SemanticException();
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

    private boolean toBool(int a){
        return a != 0;
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
