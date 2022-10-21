package semantic;

import midcode.BasicBlock;
import midcode.instrument.CondGoto;
import midcode.instrument.Goto;

public class BackFill {
    public void add(Goto instrument){

    }

    public void add(CondGoto instrument, boolean isFirst){

    }

    public void fill(BasicBlock basicBlock){

    }

    public void deliverTo(BackFill backFill){
        throw new UnsupportedOperationException();
    }
}
