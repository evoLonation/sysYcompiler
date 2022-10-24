package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.If;
import parser.nonterminal.stmt.Stmt;

import java.util.ArrayList;
import java.util.List;

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
            BasicBlock ifBasicBlock = basicBlockFactory.newBasicBlock();
            condBackFill.trueBackFill.fill(ifBasicBlock);
            symbolTable.newBlock();
            new NormalBlockGenerator(getBlock(ifStmt)).generate().deliverTo(backFill);
            symbolTable.outBlock();
        }else {
            condBackFill.trueBackFill.deliverTo(backFill);
        }
        if(ifNode.getElseStmt().isPresent()) {
            Stmt elseStmt = ifNode.getElseStmt().get();
            BasicBlock elseBasicBlock = basicBlockFactory.newBasicBlock();
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
