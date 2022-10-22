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

    public IfGenerator(BasicBlock basicBlock, If ifNode) {
        super(basicBlock);
        this.ifNode = ifNode;
        generate();
    }

    private BackFill backFill;

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        CondGenerator condGenerator = new CondGenerator(basicBlock, ifNode.getCond());
        if(ifNode.getIfStmt().isPresent()) {
            Stmt ifStmt = ifNode.getIfStmt().get();
            BasicBlock ifBasicBlock = basicBlockFactory.newBasicBlock();
            condGenerator.getTrueBackFill().fill(ifBasicBlock);
            backFill = new NormalBlockGenerator(ifBasicBlock, getBlock(ifStmt)).getBackFill();
        }else {
            backFill = condGenerator.getTrueBackFill();
        }
        if(ifNode.getElseStmt().isPresent()) {
            Stmt elseStmt = ifNode.getElseStmt().get();
            BasicBlock elseBasicBlock = basicBlockFactory.newBasicBlock();
            condGenerator.getFalseBackFill().fill(elseBasicBlock);
            if (backFill != null){
                new NormalBlockGenerator(elseBasicBlock, getBlock(elseStmt)).getBackFill().deliverTo(backFill);
            }else{
                backFill = new NormalBlockGenerator(elseBasicBlock, getBlock(elseStmt)).getBackFill();
            }
        }else {
            if (backFill != null){
                condGenerator.getFalseBackFill().deliverTo(backFill);
            }else{
                backFill = condGenerator.getFalseBackFill();
            }
        }
    }



}
