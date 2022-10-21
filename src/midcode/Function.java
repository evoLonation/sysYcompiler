package midcode;

import java.util.List;

public class Function {
    int offset;
    List<BasicBlock> basicBlock;
    BasicBlock entry;

    public List<BasicBlock> getBasicBlock() {
        return basicBlock;
    }

    public BasicBlock getEntry() {
        return entry;
    }

    Function() {}
}
