package frontend.IRGenerate;

import midcode.instruction.Sequence;

public abstract class SequenceGenerator extends Generator {

    protected void addSequence(Sequence code){
        basicBlockManager.addSequence(code);
    }
}
