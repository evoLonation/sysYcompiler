package midcode;

import midcode.instrument.Instrument;
import midcode.instrument.Jump;

import java.util.List;


/**
 * offset : sp ~ sp + offset 是该基本块的活动记录。（包括了涵盖他的所有BLock直到FuncBlock）
 */
public class BasicBlock {
    private int offset;

    private List<Instrument> instruments;
    private Jump lastInstrument;

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLastInstrument(Jump lastInstrument) {
        this.lastInstrument = lastInstrument;
    }
}
