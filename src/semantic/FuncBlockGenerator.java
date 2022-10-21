package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Break;
import parser.nonterminal.stmt.Continue;
import parser.nonterminal.stmt.ReturnNode;

// 直属于函数的block，需要检查最后一个语句是否有return
public class FuncBlockGenerator extends BlockGenerator{

    public FuncBlockGenerator(BasicBlock basicBlock, Block block) {
        super(basicBlock, block);
        generate();
    }

    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()) {
            dealBlockItem(item);
            if(item instanceof ReturnNode){
                return;
            }
        }
        // 在这里说明最外层的block没有return语句
        if(isReturn){
            errorRecorder.returnLack(block.endLine());
        }else{
            dealBlockItem(new ReturnNode(0));
        }
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
