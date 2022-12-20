package frontend.IRGenerate;

import frontend.IRGenerate.util.BackFill;
import midcode.BasicBlock;
import frontend.parser.nonterminal.stmt.If;
import frontend.parser.nonterminal.stmt.Stmt;

public class IfGenerator extends BasicBlockGenerator {
    private final If ifNode;

    IfGenerator(If ifNode) {
        this.ifNode = ifNode;
    }


    BackFill generate() {
        BackFill backFill = new BackFill();
        CondGenerator.CondBackFill condBackFill = new CondGenerator(ifNode.getCond()).generate();
        if(ifNode.getIfStmt().isPresent()) {
            Stmt ifStmt = ifNode.getIfStmt().get();
            BasicBlock ifBasicBlock = basicBlockManager.newBasicBlock();
            condBackFill.trueBackFill.fill(ifBasicBlock);
            symbolTable.newBlock();
            new NormalBlockGenerator(getBlock(ifStmt)).generate().deliverTo(backFill);
            symbolTable.outBlock();
        }else {
            condBackFill.trueBackFill.deliverTo(backFill);
        }
        if(ifNode.getElseStmt().isPresent()) {
            Stmt elseStmt = ifNode.getElseStmt().get();
            BasicBlock elseBasicBlock = basicBlockManager.newBasicBlock();
            condBackFill.falseBackFill.fill(elseBasicBlock);
            symbolTable.newBlock();
            new NormalBlockGenerator(getBlock(elseStmt)).generate().deliverTo(backFill);
            symbolTable.outBlock();
        }else {
            condBackFill.falseBackFill.deliverTo(backFill);
        }
        return backFill;
    }

}
