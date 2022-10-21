package semantic;

import midcode.BackFill;
import midcode.BasicBlock;
import midcode.instrument.Goto;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Break;
import parser.nonterminal.stmt.Continue;
import parser.nonterminal.stmt.ReturnNode;

// 直属于while语句或者间接属于while语句的block，需要处理break和continue
public class WhileBlockGenerator extends BlockGenerator{

    public WhileBlockGenerator(BasicBlock basicBlock, Block block) {
        super(basicBlock, block);
        generate();
    }

    private BackFill backFill ;
    private BackFill continueBackFill ;
    private BackFill breakBackFill ;

    public BackFill getBackFill() {
        return backFill;
    }

    public BackFill getContinueBackFill() {
        return continueBackFill;
    }

    public BackFill getBreakBackFill() {
        return breakBackFill;
    }

    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()){
            dealBlockItem(item);
            if(item instanceof ReturnNode || item instanceof Continue || item instanceof Break){
                return;
            }
        }
        backFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
    }

    @Override
    protected BackFill blockGenerator(BasicBlock basicBlock, Block block, boolean isReturn) {
        return new WhileBlockGenerator(basicBlock, block).getBackFill();
    }

    @Override
    protected void dealBreak(Break breakItem) {
        breakBackFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
        breakBackFill.fill(currentBasicBlock);
    }

    @Override
    protected void dealContinue(Continue continueItem) {
        continueBackFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
        continueBackFill.fill(currentBasicBlock);
    }
}
