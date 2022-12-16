package frontend.generator;

import midcode.instruction.BackFill;
import midcode.instruction.Goto;
import frontend.parser.nonterminal.Block;
import frontend.parser.nonterminal.BlockItem;

// 普通的block，包括单独的一个block或者if、else里面的block，不接受continue和break
public class NormalBlockGenerator extends BlockGenerator{
    NormalBlockGenerator(Block block) {
        super(block);
    }

    BackFill generate() {
        BackFill backFill = new BackFill();
        for(BlockItem item : block.getBlockItems()){
            if(dealBlockItem(item)){
                return backFill;
            }
        }
        // 正常退出，中途没有return或者continue、break
        basicBlockFactory.outBasicBlock(new Goto()).deliverTo(backFill);
        return backFill;
    }

}
