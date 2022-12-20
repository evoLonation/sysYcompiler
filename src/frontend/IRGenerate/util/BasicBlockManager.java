package frontend.IRGenerate.util;

import midcode.BasicBlock;
import midcode.Function;
import midcode.instruction.*;
import midcode.util.BasicBlockFactory;

public class BasicBlockManager {

    private Function nowFunction;

    private BasicBlock nowBasicBlock;

    public Function newFunction(String symbol){
        assert nowFunction == null;
        assert nowBasicBlock == null;
        Function ret = new Function();
        ret.setEntry(BasicBlockFactory.newEntryBasicBlock(symbol));
        nowFunction = ret;
        nowBasicBlock = nowFunction.getEntry();
        return ret;
    }

    public Function newMainFunction() {
        return newFunction("main");
    }

    public void outFunction(int offset){
        assert nowFunction != null;
        assert nowBasicBlock == null;
        nowFunction.setOffset(offset);
        nowFunction = null;
    }

    public BasicBlock newBasicBlock(){
        BasicBlock newBasicBlock = BasicBlockFactory.newBasicBlock();
        nowFunction.getOtherBasicBlocks().add(newBasicBlock);
        nowBasicBlock = newBasicBlock;
        return newBasicBlock;
    }

    public void addSequence(Sequence instruction) {
        assert nowBasicBlock != null;
        nowBasicBlock.getSequenceList().add(instruction);
    }

    public BackFill outBasicBlock(Goto outCode){
        nowBasicBlock.setLastJump(outCode);
        BackFill backFill = new BackFill();
        backFill.add(outCode);
        nowBasicBlock = null;
        return backFill;
    }

    public static class CondGotoBackFill{
        public BackFill trueBackFill;
        public BackFill falseBackFill;

        public CondGotoBackFill(BackFill trueBackFill, BackFill falseBackFill) {
            this.trueBackFill = trueBackFill;
            this.falseBackFill = falseBackFill;
        }
    }

    public CondGotoBackFill outBasicBlock(CondGoto outCode){
        nowBasicBlock.setLastJump(outCode);
        BackFill trueBackFill = new BackFill();
        BackFill falseBackFill = new BackFill();
        trueBackFill.add(outCode, true);
        falseBackFill.add(outCode, false);
        nowBasicBlock = null;
        return new CondGotoBackFill(trueBackFill, falseBackFill);
    }

    public void outBasicBlock(Return outCode){
        nowBasicBlock.setLastJump(outCode);
        nowBasicBlock = null;
    }

    private BasicBlockManager() {}
    static private final BasicBlockManager instance = new BasicBlockManager();
    static public BasicBlockManager getInstance(){
        return instance;
    }

}
