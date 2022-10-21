package semantic;

import midcode.BasicBlock;
import midcode.BasicBlockFactory;

public abstract class BasicBlockGenerator extends Generator{


    protected BasicBlock basicBlock;
    protected BasicBlockFactory basicBlockFactory = BasicBlockFactory.getInstance();

    public BasicBlockGenerator(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

}
