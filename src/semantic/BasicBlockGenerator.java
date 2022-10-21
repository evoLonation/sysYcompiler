package semantic;

import midcode.BasicBlock;

public abstract class BasicBlockGenerator extends Generator{
    protected BasicBlock basicBlock;

    public BasicBlockGenerator(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }


}
