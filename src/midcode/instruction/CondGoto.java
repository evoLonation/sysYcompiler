package midcode.instruction;

import midcode.BasicBlock;
import midcode.value.LValue;

public class CondGoto implements Jump{
    BasicBlock trueBasicBlock;
    BasicBlock falseBasicBlock;
    private final LValue cond;

    public CondGoto(LValue cond) {
        this.cond = cond;
    }

    void setTrueBasicBlock(BasicBlock trueBasicBlock) {
        this.trueBasicBlock = trueBasicBlock;
    }

    void setFalseBasicBlock(BasicBlock falseBasicBlock) {
        this.falseBasicBlock = falseBasicBlock;
    }

    public BasicBlock getTrueBasicBlock() {
        return trueBasicBlock;
    }

    public BasicBlock getFalseBasicBlock() {
        return falseBasicBlock;
    }

    public LValue getCond() {
        return cond;
    }

    @Override
    public String print() {
        return "if " + cond.print() + " goto " + trueBasicBlock.getName() + " else " + falseBasicBlock.getName();
    }

}
