package frontend.semantic;

import midcode.instrument.Instrument;

public abstract class InstrumentGenerator extends Generator {

    protected void addInstrument(Instrument code){
        basicBlockFactory.addInstrument(code);
    }
}
