package semantic;

import midcode.BasicBlock;
import parser.nonterminal.stmt.If;

public class IfGenerator extends BasicBlockGenerator {
    private final If ifNode;

    public IfGenerator(BasicBlock basicBlock, If ifNode) {
        super(basicBlock);
        this.ifNode = ifNode;
    }

    private final BackFill backFill = new BackFill();

    public BackFill getBackFill() {
        return backFill;
    }

    @Override
    protected void generate() {
        CondGenerator condGenerator = new CondGenerator(basicBlock, ifNode.getCond());
        if(ifNode.getIfStmt().isPresent()) {
            SingleItemGenerator ifStmtGenerator = new SingleItemGenerator(new BasicBlock(), ifNode.getIfStmt().get(), true);
            condGenerator.getTrueBackFill().fill(ifStmtGenerator.getBasicBlock());
            assert ifStmtGenerator.getBackFill().isPresent();
            ifStmtGenerator.getBackFill().get().deliverTo(backFill);
        }else {
            condGenerator.getTrueBackFill().deliverTo(backFill);
        }
        if(ifNode.getElseStmt().isPresent()){
            SingleItemGenerator elseStmtGenerator = new SingleItemGenerator(new BasicBlock(), ifNode.getElseStmt().get(), true);
            condGenerator.getFalseBackFill().fill(elseStmtGenerator.getBasicBlock());
            assert elseStmtGenerator.getBackFill().isPresent();
            elseStmtGenerator.getBackFill().get().deliverTo(backFill);
        }else{
            condGenerator.getFalseBackFill().deliverTo(backFill);
        }
    }

}
