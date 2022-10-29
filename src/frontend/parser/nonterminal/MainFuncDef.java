package frontend.parser.nonterminal;


public class MainFuncDef implements AST {
    private final Block block;

    public MainFuncDef(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
