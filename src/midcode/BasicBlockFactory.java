package midcode;

import lexer.Ident;
import midcode.instrument.CondGoto;
import midcode.instrument.Goto;
import midcode.instrument.Return;
import type.SymbolTable;

public class BasicBlockFactory {
    private final SymbolTable symbolTable = SymbolTable.getInstance();

    /**
     * @return a function that already has entry basic block
     */
    public Function newFunction(Ident ident, boolean isReturn){
        Function ret = new Function();
        ret.entry = new BasicBlock();
        symbolTable.addFunc(ret, ident, isReturn);
        return ret;
    }
    public Function newMainFunction(){
        Function ret = new Function();
        ret.entry = new BasicBlock();
        symbolTable.newBlock();
        return ret;
    }

    /**
     * @return return a basic block and entry a new domain
     */
    public BasicBlock newBasicBlock(){
        BasicBlock ret = new BasicBlock();
        symbolTable.newBlock();
        return ret;
    }

    public BackFill outBasicBlock(BasicBlock basicBlock, Goto outCode){
        basicBlock.lastInstrument = outCode;
        basicBlock.offset = symbolTable.outBlock();
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
        basicBlock.offset = symbolTable.outBlock();
        BackFill trueBackFill = new BackFill();
        BackFill falseBackFill = new BackFill();
        trueBackFill.add(outCode, true);
        falseBackFill.add(outCode, false);
        return new CondGotoBackFill(trueBackFill, falseBackFill);
    }

    public void outBasicBlock(BasicBlock basicBlock, Return outCode){
        basicBlock.lastInstrument = outCode;
        basicBlock.offset = symbolTable.outBlock();
    }

    private BasicBlockFactory() {}
    static private final BasicBlockFactory instance = new BasicBlockFactory();
    static public BasicBlockFactory getInstance(){
        return instance;
    }

}
