package midcode.instrument;

import common.SemanticException;
import midcode.value.RValue;
import midcode.value.Temp;
import frontend.parser.nonterminal.exp.UnaryOp;

/**
 * 考虑到实际机器码的计算是将结果放在寄存器，因此result限制为Temp
 */
public class UnaryOperation implements Instrument{
    private Temp result;
    private RValue value;
    private UnaryOp op;

    public UnaryOperation(RValue value, UnaryOp op, Temp result) {
        this.result = result;
        this.value = value;
        this.op = op;
    }

    @Override
    public String print() {
        String opStr;
        switch (op){
            case PLUS: opStr = "+"; break;
            case MINU: opStr = "-"; break;
            case NOT: opStr = "!"; break;
            default: throw new SemanticException();
        }
        return result.print() + " = " + opStr + " " + value.print();
    }

    public Temp getResult() {
        return result;
    }

    public RValue getValue() {
        return value;
    }

    public UnaryOp getOp() {
        return op;
    }
}
