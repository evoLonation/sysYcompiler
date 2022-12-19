package midcode.instruction;

import common.SemanticException;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Temp;

/**
 * 考虑到实际机器码的计算是将结果放在寄存器，因此result限制为Temp
 */
public class UnaryOperation implements Sequence {
    private LValue result;
    private RValue value;
    private UnaryOp op;

    public enum UnaryOp {
        PLUS,
        MINU,
        NOT,
    }

    public UnaryOperation(RValue value, UnaryOp op, LValue result) {
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

    public LValue getResult() {
        return result;
    }

    public RValue getValue() {
        return value;
    }

    public UnaryOp getOp() {
        return op;
    }

    public void setResult(LValue result) {
        this.result = result;
    }

    public void setValue(RValue value) {
        this.value = value;
    }

    public void setOp(UnaryOp op) {
        this.op = op;
    }
}
