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

    private BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()){
            if(dealBlockItem(item)){
                return;
            }
        }
        // 正常退出，中途没有return或者continue、break
        basicBlockFactory.outBasicBlock(new Goto()).deliverTo(backFill);
    }

}
