package midcode.instrument;

import midcode.BasicBlock;

public class Goto implements Jump{
    BasicBlock basicBlock;

    @Override
    public String print() {
        return "goto " + basicBlock.getName();
    }
}
