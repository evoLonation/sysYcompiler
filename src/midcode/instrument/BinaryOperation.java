package midcode.instrument;

import common.SemanticException;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Temp;
import parser.nonterminal.exp.BinaryOp;

/**
 * 考虑到实际机器码的计算是将结果放在寄存器，因此result限制为Temp
 */
public class BinaryOperation implements Instrument{
    private RValue left;
    private RValue right;
    private BinaryOp op;
    private Temp result;

    public BinaryOperation(RValue left, RValue right, BinaryOp op, Temp result) {
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
            case OR: opStr = "||"; break;
            case AND: opStr = "&&"; break;
            case DIV: opStr = "/"; break;
            case LSS: opStr = "<"; break;
            case MOD: opStr = "%"; break;
            case NEQ: opStr = "!="; break;
            case MULT: opStr = "*"; break;
            default: throw new SemanticException();
        }
        return result.print() + " = " + left.print() + " " + opStr + " " + right.print();
    }
}
