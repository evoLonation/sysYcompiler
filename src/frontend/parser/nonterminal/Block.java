package frontend.parser.nonterminal;

import frontend.parser.nonterminal.stmt.Stmt;

import java.util.List;

public class Block implements Stmt {
    private final List<BlockItem> blockItems;
    private final int endLine;

    public List<BlockItem> getBlockItems() {
        return blockItems;
    }

    public Block(List<BlockItem> blockItems, int endLine) {
        this.blockItems = blockItems;
        this.endLine = endLine;
    }

    public int endLine() {
        return endLine;
    }
}
