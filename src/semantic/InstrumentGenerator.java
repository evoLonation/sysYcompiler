package semantic;

import midcode.instrument.Instrument;

import java.util.List;

public abstract class InstrumentGenerator {

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
