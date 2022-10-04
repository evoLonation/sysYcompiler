package parser.nonterminal;


public class MainFuncDef extends ASDDefault implements ASD {
    private final Block block;

    public MainFuncDef(Block block) {
        this.block = block;
        addSon(block);
    }
}
