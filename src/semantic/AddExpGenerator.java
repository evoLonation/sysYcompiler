package semantic;

import common.SemanticException;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Load;
import midcode.instrument.UnaryOperation;
import midcode.value.*;
import parser.nonterminal.exp.*;
import parser.nonterminal.exp.Number;

/**
 * 该类假定exp的结果一定是int类型的，否则会进行相应错误处理
 */
public class AddExpGenerator extends Execution<Exp, RValue>{

    private final Exp exp;

    public AddExpGenerator(Exp exp) {
        this.exp = exp;
    }

    private RValue result;

    public RValue getResult() {
        return result;
    }

    public AddExpGenerator generate() {
        this.result = exec(exp);
        return this;
    }

    @Override
    public void inject(){

        inject(BinaryExp.class,  exp -> {
            RValue result1 = exec(exp.getExp1());
            RValue result2 = exec(exp.getExp2());
            if(result1 instanceof Constant && result2 instanceof Constant){
                return new Constant(compute(((Constant) result1).getNumber(), exp.getOp(), ((Constant) result2).getNumber()));
            }else{
                Temp ret = valueFactory.newTemp();
                midCodes.add(new BinaryOperation(result1, result2, exp.getOp(), ret));
                return ret;
            }
        });

        inject(UnaryExp.class,  exp -> {
            RValue result = exec(exp.getExp());
            if(result instanceof Constant){
                return new Constant(compute(((Constant) result).getNumber(), exp.getOp()));
            }else{
                Temp ret = valueFactory.newTemp();
                midCodes.add(new UnaryOperation(result, exp.getOp(), ret));
                return ret;
            }
        });

        inject(Number.class, exp -> new Constant(exp.getNumber()));

        inject(FuncCall.class, exp -> {
            FuncCallGenerator funcCallGenerator = new FuncCallGenerator(exp);
            addMidCode(funcCallGenerator.getMidCodes());
            if(!funcCallGenerator.getResult().isPresent()){
                throw new SemanticException();
            }
            return funcCallGenerator.getResult().get();
        });

        inject(LVal.class, exp -> {
            LValGenerator lValGenerator = new LValGenerator(exp).generate();
            switch (lValGenerator.getResultType()){
                case Constant: return lValGenerator.getConstant();
                case IntVariable: return lValGenerator.getIntVariable();
                case Array0: {
                    Temp temp = valueFactory.newTemp();
                    LValGenerator.ArrayResult result = lValGenerator.getArray01();
                    addMidCode(new Load(temp, result.arrayVariable, result.offset));
                    return temp;
                }
                case Array1:
                case Array2:
                default:
                    throw new SemanticException();
            }
        });

    }

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
