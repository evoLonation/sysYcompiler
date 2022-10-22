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

    //todo 最后一行return 的检查
    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()) {
            if(dealBlockItem(item)){
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

}
