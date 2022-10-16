package parser.nonterminal;


public class MainFuncDef extends ASTDefault implements AST {
    private final Block block;

    public MainFuncDef(Block block) {
        this.block = block;
        addSon(block);
    }

    public Block getBlock() {
        return block;
    }
}
