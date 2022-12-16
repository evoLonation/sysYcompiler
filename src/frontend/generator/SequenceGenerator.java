package frontend.generator;

import midcode.instruction.Sequence;

public abstract class SequenceGenerator extends Generator {

    protected void addSequence(Sequence code){
        basicBlockFactory.addSequence(code);
    }
}
