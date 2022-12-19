package midcode;

import java.util.*;

public class Function implements MidCode{
    private int offset;
    // 除了entry之外的其他basicBlock
    Set<BasicBlock> basicBlocks = new LinkedHashSet<>();
    private BasicBlock entry;

    public Set<BasicBlock> getOtherBasicBlocks() {
        return basicBlocks;
    }

    public BasicBlock getEntry() {
        return entry;
    }

    public Function() {}

    @Override
    public String print() {
        StringBuilder ret = new StringBuilder("def ").append(entry.getName()).append(":\n");
        ret.append("offset : ").append(offset).append("\n\n");
        ret.append(entry.print()).append("\n");
        for(BasicBlock basicBlock : basicBlocks){
            ret.append(basicBlock.print()).append("\n");
        }
        ret.append("end ").append(entry.getName()).append("\n");
        return ret.toString();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setEntry(BasicBlock entry) {
        this.entry = entry;
    }
}
