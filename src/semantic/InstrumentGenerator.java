package semantic;

import midcode.BasicBlock;
import midcode.instrument.Instrument;

import java.util.List;

public abstract class InstrumentGenerator extends Generator {

    public InstrumentGenerator(List<Instrument> instruments) {
        this.instruments = instruments;
    }
    public InstrumentGenerator(BasicBlock basicBlock) {
        this.instruments = basicBlock.getInstruments();
    }

    protected List<Instrument> instruments;
    public List<Instrument> getInstruments() {
        return instruments;
    }
    protected void addInstrument(Instrument code){
        instruments.add(code);
    }
    protected void addInstrument(List<Instrument> code){
        instruments.addAll(code);
    }
}
