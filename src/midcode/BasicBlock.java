package midcode;

import midcode.instrument.Instrument;
import midcode.instrument.Jump;
import type.SymbolTable;

import java.util.ArrayList;
import java.util.List;


/**
 * offset : sp ~ sp + offset 是该基本块的活动记录。（包括了涵盖他的所有BLock直到FuncBlock）
 * 注意，sp只有进入一个function时才会修改。
 */
public class BasicBlock {
    int offset;

    private final List<Instrument> instruments = new ArrayList<>();
    Jump lastInstrument;


    public void addInstrument(Instrument instrument){
        instruments.add(instrument);
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }

    BasicBlock() {}
}
