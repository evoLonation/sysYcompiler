package semantic;

import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Stmt;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicBlockGenerator extends Generator{

    protected Block getBlock(Stmt stmt) {
        Block block;
        if(stmt instanceof Block){
            block = (Block) stmt;
        }else{
            List<BlockItem> blockItems = new ArrayList<>();
            blockItems.add(stmt);
            block = new Block(blockItems, 0);
        }
        return block;
    }

}
