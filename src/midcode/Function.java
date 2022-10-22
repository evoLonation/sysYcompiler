package midcode;

import java.util.ArrayList;
import java.util.List;

public class Function implements MidCode{
    int offset;
    // 除了entry之外的其他basicBlock
    List<BasicBlock> basicBlocks = new ArrayList<>();
    BasicBlock entry;

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public BasicBlock getEntry() {
        return entry;
    }

    Function() {}

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
}
