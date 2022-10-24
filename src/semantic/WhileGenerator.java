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
    }

    BackFill generate() {
        BackFill backFill = new BackFill();
        BackFill preBackFill = basicBlockFactory.outBasicBlock(new Goto());
        BasicBlock condBasicBlock = basicBlockFactory.newBasicBlock();
        preBackFill.fill(condBasicBlock);
        CondGenerator.CondBackFill condBackFill = new CondGenerator(whileNode.getCond()).generate();
        condBackFill.falseBackFill.deliverTo(backFill);
        if(whileNode.getStmt().isPresent()) {
            whileStmtDealer.inWhile();
            Stmt whileStmt = whileNode.getStmt().get();
            BasicBlock whileBasicBlock = basicBlockFactory.newBasicBlock();
            condBackFill.trueBackFill.fill(whileBasicBlock);
            symbolTable.newBlock();
            BackFill whileBlockBackFill = new NormalBlockGenerator(getBlock(whileStmt)).generate();
            symbolTable.outBlock();
            whileBlockBackFill.fill(condBasicBlock);
            whileStmtDealer.getContinueBackFill().fill(condBasicBlock);
            whileStmtDealer.getBreakBackFill().deliverTo(backFill);
            whileStmtDealer.outWhile();
        }else {
            condBackFill.trueBackFill.deliverTo(backFill);
        }
        return backFill;
    }

}
