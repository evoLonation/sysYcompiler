package semantic;

import midcode.BasicBlock;
import parser.nonterminal.stmt.While;

public class WhileGenerator extends BasicBlockGenerator{
    private final While aWhile;

    public WhileGenerator(BasicBlock basicBlock, While aWhile) {
        super(basicBlock);
        this.aWhile = aWhile;
    }

    private final BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {

    }
}
