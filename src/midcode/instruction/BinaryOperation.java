package midcode.instruction;

import common.SemanticException;
import midcode.value.LValue;
import midcode.value.RValue;


public class BinaryOperation implements Sequence{
    private RValue left;
    private RValue right;
    private BinaryOp op;
    private LValue result;

    public enum BinaryOp {
        PLUS,
        MINU,
        MULT,
        DIV,
        MOD,
        BITAND,
        LSS,
        LEQ,
        GRE,
        GEQ,
        EQL,
        NEQ,
    }

    public BinaryOperation(RValue left, RValue right, BinaryOp op, LValue result) {
        this.left = left;
        this.right = right;
        this.op = op;
        this.result = result;
    }

    @Override
    public String print() {
        String opStr;
        switch (op){
            case MINU: opStr = "-"; break;
            case PLUS: opStr = "+"; break;
            case LEQ: opStr = "<="; break;
            case GEQ: opStr = ">="; break;
            case GRE: opStr = ">"; break;
            case EQL: opStr = "=="; break;
            case DIV: opStr = "/"; break;
            case LSS: opStr = "<"; break;
            case MOD: opStr = "%"; break;
            case NEQ: opStr = "!="; break;
            case MULT: opStr = "*"; break;
            case BITAND: opStr = "&"; break;
            default: throw new SemanticException();
        }
        return result.print() + " = " + left.print() + " " + opStr + " " + right.print();
    }

    public RValue getLeft() {
        return left;
    }

    public RValue getRight() {
        return right;
    }

    public BinaryOp getOp() {
        return op;
    }

    public LValue getResult() {
        return result;
    }

    public void setLeft(RValue left) {
        this.left = left;
    }

    public void setRight(RValue right) {
        this.right = right;
    }

    public void setOp(BinaryOp op) {
        this.op = op;
    }

    public void setResult(LValue result) {
        this.result = result;
    }
}
