package midcode;

import midcode.instrument.Instrument;
import midcode.instrument.Jump;

import java.util.ArrayList;
import java.util.List;


/**
 * offset : sp ~ sp + offset 是该基本块的活动记录。（包括了涵盖他的所有BLock直到FuncBlock）
 * 注意，sp只有进入一个function时才会修改。
 */
public class BasicBlock implements MidCode{
    private final String name;

    private final List<Instrument> instruments = new ArrayList<>();
    Jump lastInstrument;


    public List<Instrument> getInstruments() {
        return instruments;
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
        for(Instrument instrument : instruments){
            ret.append("    ").append(instrument.print()).append("\n");
        }
        ret.append("    ").append(lastInstrument.print()).append("\n");
        return ret.toString();
    }

    public Jump getLastInstrument() {
        return lastInstrument;
    }
}
