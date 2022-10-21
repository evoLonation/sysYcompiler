package midcode.instrument;

import midcode.BasicBlock;
import midcode.value.RValue;

public class CondGoto implements Jump{
    private BasicBlock trueBasicBlock;
    private BasicBlock falseBasicBlock;
    private final RValue cond;

    public CondGoto(RValue cond) {
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

    public RValue getCond() {
        return cond;
    }
}
