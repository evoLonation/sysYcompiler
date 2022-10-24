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

    public IfGenerator(If ifNode) {
        this.ifNode = ifNode;
        generate();
    }

    private final BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        CondGenerator condGenerator = new CondGenerator(ifNode.getCond());
        if(ifNode.getIfStmt().isPresent()) {
            Stmt ifStmt = ifNode.getIfStmt().get();
            BasicBlock ifBasicBlock = basicBlockFactory.newBasicBlock();
            condGenerator.getTrueBackFill().fill(ifBasicBlock);
            symbolTable.newBlock();
            new NormalBlockGenerator(getBlock(ifStmt)).getBackFill().deliverTo(backFill);
            symbolTable.outBlock();
        }else {
            condGenerator.getTrueBackFill().deliverTo(backFill);
        }
        if(ifNode.getElseStmt().isPresent()) {
            Stmt elseStmt = ifNode.getElseStmt().get();
            BasicBlock elseBasicBlock = basicBlockFactory.newBasicBlock();
            condGenerator.getFalseBackFill().fill(elseBasicBlock);
            symbolTable.newBlock();
            new NormalBlockGenerator(getBlock(elseStmt)).getBackFill().deliverTo(backFill);
            symbolTable.outBlock();
        }else {
            condGenerator.getFalseBackFill().deliverTo(backFill);
        }
    }

}
