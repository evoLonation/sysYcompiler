package midcode;

import lexer.Ident;
import midcode.instrument.BackFill;
import midcode.instrument.CondGoto;
import midcode.instrument.Goto;
import midcode.instrument.Return;
import type.SymbolTable;

public class BasicBlockFactory {

    private int notFuncBasicBlockNumber = 0;
    private Function nowFunction;

    public Function newFunction(Ident ident, boolean isReturn){
        assert nowFunction == null;
        Function ret = new Function();
        ret.entry = new BasicBlock("function$" + ident.getValue());
        nowFunction = ret;
        return ret;
    }

    public Function newMainFunction(){
        assert nowFunction == null;
        Function ret = new Function();
        ret.entry = new BasicBlock("function$main");
        nowFunction = ret;
        return ret;
    }
    public void outFunction(Function function){
        function.offset = SymbolTable.getInstance().getMaxOffset();
        nowFunction = null;
    }

    public BasicBlock newBasicBlock(){
        BasicBlock newBasicBlock = new BasicBlock("basicBlock$" + ++notFuncBasicBlockNumber);
        nowFunction.basicBlocks.add(newBasicBlock);
        return newBasicBlock;
    }

    public BackFill outBasicBlock(BasicBlock basicBlock, Goto outCode){
        basicBlock.lastInstrument = outCode;
        BackFill backFill = new BackFill();
        backFill.add(outCode);
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

    public CondGotoBackFill outBasicBlock(BasicBlock basicBlock, CondGoto outCode){
        basicBlock.lastInstrument = outCode;
        BackFill trueBackFill = new BackFill();
        BackFill falseBackFill = new BackFill();
        trueBackFill.add(outCode, true);
        falseBackFill.add(outCode, false);
        return new CondGotoBackFill(trueBackFill, falseBackFill);
    }

    public void outBasicBlock(BasicBlock basicBlock, Return outCode){
        basicBlock.lastInstrument = outCode;
    }

    private BasicBlockFactory() {}
    static private final BasicBlockFactory instance = new BasicBlockFactory();
    static public BasicBlockFactory getInstance(){
        return instance;
    }

}
