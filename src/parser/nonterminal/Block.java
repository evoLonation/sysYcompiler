package parser.nonterminal;

import parser.nonterminal.stmt.Stmt;

import java.util.List;

public class Block implements Stmt {
    List<BlockItem> blockItems;

    public Block(List<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }
}
