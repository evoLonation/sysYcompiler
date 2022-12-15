package frontend.semantic;

import midcode.instrument.Instruction;

public abstract class InstrumentGenerator extends Generator {

    protected void addInstrument(Instruction code){
        basicBlockFactory.addInstrument(code);
    }
}
