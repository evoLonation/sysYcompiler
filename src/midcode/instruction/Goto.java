package midcode.instruction;

import midcode.BasicBlock;

public class Goto implements Jump{
    BasicBlock basicBlock;

    public Goto(){}
    public Goto(BasicBlock basicBlock){
        this.basicBlock = basicBlock;
    }
    @Override
    public String print() {
        return "goto " + basicBlock.getName();
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }
}
