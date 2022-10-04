package parser.nonterminal;

import parser.nonterminal.stmt.Stmt;

import java.util.List;

public class Block extends ASDDefault implements Stmt {
    private final List<BlockItem> blockItems;

    public List<BlockItem> getBlockItems() {
        return blockItems;
    }

    public Block(List<BlockItem> blockItems) {
        this.blockItems = blockItems;
        addSon(blockItems);
    }
}
