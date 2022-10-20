package midcode.instrument;

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
}
