package semantic;

import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Stmt;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicBlockGenerator extends Generator{

    protected BasicBlock basicBlock;

    public BasicBlockGenerator(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    protected Block getBlock(Stmt elseStmt) {
        Block block;
        if(elseStmt instanceof Block){
            block = (Block) elseStmt;
        }else{
            List<BlockItem> blockItems = new ArrayList<>();
            blockItems.add(elseStmt);
            block = new Block(blockItems, 0);
        }
        return block;
    }
}
