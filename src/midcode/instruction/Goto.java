package midcode.instruction;

import midcode.BasicBlock;

public class Goto implements Jump{
    BasicBlock basicBlock;

    @Override
    public String print() {
        return "goto " + basicBlock.getName();
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }
}
