package midcode.instrument;

import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Temp;
import parser.nonterminal.exp.UnaryOp;

/**
 * 考虑到实际机器码的计算是将结果放在寄存器，因此result限制为Temp
 */
public class UnaryOperation implements Instrument{
    private LValue result;
    private RValue value;
    private UnaryOp op;

    public UnaryOperation(RValue value, UnaryOp op, Temp result) {
        this.result = result;
        this.value = value;
        this.op = op;
    }
}
