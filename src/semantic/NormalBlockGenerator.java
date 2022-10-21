package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.instrument.Goto;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Break;
import parser.nonterminal.stmt.Continue;
import parser.nonterminal.stmt.ReturnNode;

// 普通的block，包括单独的一个block或者if、else里面的block，不接受continue和break
public class NormalBlockGenerator extends BlockGenerator{
    public NormalBlockGenerator(BasicBlock basicBlock, Block block) {
        super(basicBlock, block);
        generate();
    }

    private BackFill backFill;

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()){
            dealBlockItem(item);
            if(item instanceof ReturnNode){
                return;
            }
        }
        backFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
    }

    @Override
    protected BackFill blockGenerator(BasicBlock basicBlock, Block block, boolean isReturn) {
        return new NormalBlockGenerator(basicBlock, block).getBackFill();
    }


    @Override
    protected void dealBreak(Break breakItem) {
        errorRecorder.wrongBreak(breakItem.line());
    }

    @Override
    protected void dealContinue(Continue continueItem) {
        errorRecorder.wrongContinue(continueItem.line());
    }
}
