package midcode;

import midcode.instruction.Instruction;
import midcode.instruction.Jump;
import midcode.instruction.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * offset : sp ~ sp + offset 是该基本块的活动记录。（包括了涵盖他的所有BLock直到FuncBlock）
 * 注意，sp只有进入一个function时才会修改。
 */
public class BasicBlock implements MidCode{
    private final String name;

    private final List<Sequence> instructions = new ArrayList<>();
    Jump lastJump;


    public List<Sequence> getSequenceList() {
        return instructions;
    }
    //保证除了最后一个是jump，前面都是sequence
    public List<Instruction> getInstructions() {
        return Stream.concat(instructions.stream(), Stream.of(lastJump)).collect(Collectors.toList());
    }


    BasicBlock(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String print() {
        StringBuilder ret = new StringBuilder("  " + name + ":").append("\n");
        for(Instruction instruction : instructions){
            ret.append("    ").append(instruction.print()).append("\n");
        }
        ret.append("    ").append(lastJump.print()).append("\n");
        return ret.toString();
    }

    public Jump getJump() {
        return lastJump;
    }
}
