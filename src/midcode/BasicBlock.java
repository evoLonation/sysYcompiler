package midcode;

import midcode.instrument.Instruction;
import midcode.instrument.Jump;

import java.util.ArrayList;
import java.util.List;


/**
 * offset : sp ~ sp + offset 是该基本块的活动记录。（包括了涵盖他的所有BLock直到FuncBlock）
 * 注意，sp只有进入一个function时才会修改。
 */
public class BasicBlock implements MidCode{
    private final String name;

    private final List<Instruction> instructions = new ArrayList<>();
    Jump lastInstrument;


    public List<Instruction> getInstruments() {
        return instructions;
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
        ret.append("    ").append(lastInstrument.print()).append("\n");
        return ret.toString();
    }

    public Jump getLastInstrument() {
        return lastInstrument;
    }
}
