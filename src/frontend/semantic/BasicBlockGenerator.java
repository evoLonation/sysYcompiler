package frontend.semantic;

import frontend.parser.nonterminal.Block;
import frontend.parser.nonterminal.BlockItem;
import frontend.parser.nonterminal.stmt.Stmt;

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
