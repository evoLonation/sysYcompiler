package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.instrument.Goto;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.Stmt;
import parser.nonterminal.stmt.While;

import java.util.ArrayList;
import java.util.List;

public class WhileGenerator extends BasicBlockGenerator{
    private final While whileNode;

    public WhileGenerator(BasicBlock basicBlock, While whileNode) {
        super(basicBlock);
        this.whileNode = whileNode;
        generate();
    }

    private final BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        BackFill preBackFill = basicBlockFactory.outBasicBlock(basicBlock, new Goto());
        BasicBlock condBasicBlock = basicBlockFactory.newBasicBlock();
        preBackFill.fill(condBasicBlock);
        CondGenerator condGenerator = new CondGenerator(condBasicBlock, whileNode.getCond());
        condGenerator.getFalseBackFill().deliverTo(backFill);
        if(whileNode.getStmt().isPresent()) {
            whileStmtDealer.inWhile();
            Stmt whileStmt = whileNode.getStmt().get();
            BasicBlock whileBasicBlock = basicBlockFactory.newBasicBlock();
            NormalBlockGenerator whileBlockGenerator = new NormalBlockGenerator(whileBasicBlock, getBlock(whileStmt));
            condGenerator.getTrueBackFill().fill(whileBasicBlock);
            whileBlockGenerator.getBackFill().fill(condBasicBlock);
            whileStmtDealer.getContinueBackFill().fill(condBasicBlock);
            whileStmtDealer.getBreakBackFill().deliverTo(backFill);
            whileStmtDealer.outWhile();
        }else {
            condGenerator.getTrueBackFill().deliverTo(backFill);
        }
    }

}
