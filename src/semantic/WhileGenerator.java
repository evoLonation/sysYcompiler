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

    WhileGenerator(While whileNode) {
        this.whileNode = whileNode;
        generate();
    }

    private final BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        BackFill preBackFill = basicBlockFactory.outBasicBlock(new Goto());
        BasicBlock condBasicBlock = basicBlockFactory.newBasicBlock();
        preBackFill.fill(condBasicBlock);
        CondGenerator condGenerator = new CondGenerator(whileNode.getCond());
        condGenerator.getFalseBackFill().deliverTo(backFill);
        if(whileNode.getStmt().isPresent()) {
            whileStmtDealer.inWhile();
            Stmt whileStmt = whileNode.getStmt().get();
            BasicBlock whileBasicBlock = basicBlockFactory.newBasicBlock();
            symbolTable.newBlock();
            NormalBlockGenerator whileBlockGenerator = new NormalBlockGenerator(getBlock(whileStmt));
            symbolTable.outBlock();
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
