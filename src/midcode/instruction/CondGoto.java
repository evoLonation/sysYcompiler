package midcode.instruction;

import midcode.BasicBlock;
import midcode.value.LValue;
import midcode.value.RValue;

public class CondGoto implements Jump{
    private BasicBlock trueBasicBlock;
    private BasicBlock falseBasicBlock;
    private RValue cond;

    public CondGoto(LValue cond) {
        this.cond = cond;
    }

    public void setTrueBasicBlock(BasicBlock trueBasicBlock) {
        this.trueBasicBlock = trueBasicBlock;
    }

    public void setFalseBasicBlock(BasicBlock falseBasicBlock) {
        this.falseBasicBlock = falseBasicBlock;
    }

    public BasicBlock getTrueBasicBlock() {
        return trueBasicBlock;
    }

    public BasicBlock getFalseBasicBlock() {
        return falseBasicBlock;
    }

    public RValue getCond() {
        return cond;
    }

    public void setCond(RValue cond) {
        this.cond = cond;
    }

    @Override
    public String print() {
        return "if " + cond.print() + " goto " + trueBasicBlock.getName() + " else " + falseBasicBlock.getName();
    }

}
