package semantic;

import midcode.BasicBlock;
import midcode.instrument.Instrument;

import java.util.List;

public abstract class InstrumentGenerator extends Generator {

    protected void addInstrument(Instrument code){
        basicBlockFactory.addInstrument(code);
    }
}
