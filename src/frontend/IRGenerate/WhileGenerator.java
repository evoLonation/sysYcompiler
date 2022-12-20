package frontend.IRGenerate;

import frontend.IRGenerate.util.BackFill;
import midcode.BasicBlock;
import midcode.instruction.Goto;
import frontend.parser.nonterminal.stmt.Stmt;
import frontend.parser.nonterminal.stmt.While;

public class WhileGenerator extends BasicBlockGenerator{
    private final While whileNode;

    WhileGenerator(While whileNode) {
        this.whileNode = whileNode;
    }

    BackFill generate() {
        BackFill backFill = new BackFill();
        BackFill preBackFill = basicBlockManager.outBasicBlock(new Goto());
        BasicBlock condBasicBlock = basicBlockManager.newBasicBlock();
        preBackFill.fill(condBasicBlock);
        CondGenerator.CondBackFill condBackFill = new CondGenerator(whileNode.getCond()).generate();
        condBackFill.falseBackFill.deliverTo(backFill);
        if(whileNode.getStmt().isPresent()) {
            whileStmtDealer.inWhile();
            Stmt whileStmt = whileNode.getStmt().get();
            BasicBlock whileBasicBlock = basicBlockManager.newBasicBlock();
            condBackFill.trueBackFill.fill(whileBasicBlock);
            symbolTable.newBlock();
            BackFill whileBlockBackFill = new NormalBlockGenerator(getBlock(whileStmt)).generate();
            symbolTable.outBlock();
            whileBlockBackFill.fill(condBasicBlock);
            WhileStmtDealer.WhileLayer layer = whileStmtDealer.outWhile();
            layer.getContinueBackFill().fill(condBasicBlock);
            layer.getBreakBackFill().deliverTo(backFill);
        }else {
            condBackFill.trueBackFill.deliverTo(backFill);
        }
        return backFill;
    }

}
