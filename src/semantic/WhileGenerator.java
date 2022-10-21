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

    private BackFill backFill;

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        BackFill preBackFill = basicBlockFactory.outBasicBlock(basicBlock, new Goto());
        BasicBlock condBasicBlock = basicBlockFactory.newBasicBlock();
        preBackFill.fill(condBasicBlock);
        CondGenerator condGenerator = new CondGenerator(condBasicBlock, whileNode.getCond());
        backFill = condGenerator.getFalseBackFill();
        if(whileNode.getStmt().isPresent()) {
            Stmt whileStmt = whileNode.getStmt().get();
            BasicBlock whileBasicBlock = basicBlockFactory.newBasicBlock();
            WhileBlockGenerator whileBlockGenerator = new WhileBlockGenerator(whileBasicBlock, getBlock(whileStmt));
            condGenerator.getTrueBackFill().fill(whileBasicBlock);
            whileBlockGenerator.getBackFill().fill(condBasicBlock);
            whileBlockGenerator.getContinueBackFill().fill(condBasicBlock);
            whileBlockGenerator.getBreakBackFill().deliverTo(backFill);
        }else {
            backFill = condGenerator.getTrueBackFill();
        }
    }

    private Block getBlock(Stmt elseStmt) {
        Block block;
        if(elseStmt instanceof Block){
            block = (Block) elseStmt;
        }else{
            List<BlockItem> blockItems = new ArrayList<>();
            blockItems.add(elseStmt);
            block = new Block(blockItems, 0);
        }
        return block;
    }
}
