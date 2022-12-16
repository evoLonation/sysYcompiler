package frontend.generator;

import frontend.error.ErrorRecorder;
import midcode.BasicBlockFactory;
import midcode.instruction.BackFill;
import midcode.instruction.Goto;

import java.util.Stack;

public class WhileStmtDealer {
//    private int whileLayer = 0;

    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
    private final BasicBlockFactory basicBlockFactory = BasicBlockFactory.getInstance();


    static class WhileLayer {
        private final BackFill breakBackFill;
        private final BackFill continueBackFill;
        public BackFill getBreakBackFill() {
            return breakBackFill;
        }
        public BackFill getContinueBackFill() {
            return continueBackFill;
        }
        WhileLayer(BackFill breakBackFill, BackFill continueBackFill) {
            this.breakBackFill = breakBackFill;
            this.continueBackFill = continueBackFill;
        }
    }

    private final Stack<WhileLayer> whileLayerStack = new Stack<>();

    public void inWhile(){
        whileLayerStack.push(new WhileLayer(new BackFill(), new BackFill()));
//        whileLayer++;
    }
    public WhileLayer outWhile(){
        return whileLayerStack.pop();
//        whileLayer--;
    }

    /**
     * @return 该basicBlock是否封底了
     */
    public boolean newBreak(int line){
        if(!whileLayerStack.empty()){
            basicBlockFactory.outBasicBlock(new Goto()).deliverTo(whileLayerStack.peek().breakBackFill);
            return true;
        }else{
            errorRecorder.wrongBreak(line);
            return false;
        }
    }
    /**
     * @return 该basicBlock是否封底了
     */
    public boolean newContinue(int line){
        if(!whileLayerStack.empty()){
            basicBlockFactory.outBasicBlock(new Goto()).deliverTo(whileLayerStack.peek().continueBackFill);
            return true;
        }else {
            errorRecorder.wrongContinue(line);
            return false;
        }
    }

    private WhileStmtDealer(){}
    static private final WhileStmtDealer instance = new WhileStmtDealer();
    static public WhileStmtDealer getInstance(){
        return instance;
    }

}
