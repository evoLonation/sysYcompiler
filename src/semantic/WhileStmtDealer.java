package semantic;

import error.ErrorRecorder;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.instrument.BackFill;
import midcode.instrument.Goto;
import parser.nonterminal.stmt.Break;

public class WhileStmtDealer {
    private int whileLayer = 0;

    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
    private final BasicBlockFactory basicBlockFactory = BasicBlockFactory.getInstance();

    public void inWhile(){
        whileLayer++;
    }
    public void outWhile(){
        whileLayer--;
    }

    private final BackFill breakBackFill = new BackFill();
    private final BackFill continueBackFill = new BackFill();

    /**
     * @return 该basicBlock是否封底了
     */
    public boolean newBreak(BasicBlock basicBlock, int line){
        if(whileLayer > 0){
            basicBlockFactory.outBasicBlock(new Goto()).deliverTo(breakBackFill);
            return true;
        }else{
            errorRecorder.wrongBreak(line);
            return false;
        }
    }
    /**
     * @return 该basicBlock是否封底了
     */
    public boolean newContinue(BasicBlock basicBlock, int line){
        if(whileLayer > 0){
            basicBlockFactory.outBasicBlock(new Goto()).deliverTo(continueBackFill);
            return true;
        }else {
            errorRecorder.wrongContinue(line);
            return false;
        }
    }

    public BackFill getBreakBackFill(){
        return breakBackFill;
    }
    public BackFill getContinueBackFill(){
        return continueBackFill;
    }

    private WhileStmtDealer(){}
    static private final WhileStmtDealer instance = new WhileStmtDealer();
    static public WhileStmtDealer getInstance(){
        return instance;
    }

}
