package semantic;

import midcode.BackFill;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.instrument.CondGoto;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;

public class CondGenerator extends BasicBlockGenerator {
    private final Exp exp;

    public CondGenerator(BasicBlock basicBlock, Exp exp) {
        super(basicBlock);
        this.exp = exp;
        generate();
    }

    public BackFill getTrueBackFill() {
        return trueBackFill;
    }

    public BackFill getFalseBackFill() {
        return falseBackFill;
    }

    private BackFill trueBackFill;
    private BackFill falseBackFill;

    @Override
    protected void generate() {
        if(exp instanceof BinaryExp){
            BinaryExp binaryExp = (BinaryExp) exp;
            CondGenerator condGenerator1 = new CondGenerator(basicBlock, binaryExp.getExp1());
            CondGenerator condGenerator2 = new CondGenerator(basicBlockFactory.newBasicBlock(), binaryExp.getExp2());
            if(binaryExp.getOp() == BinaryOp.OR) {
                condGenerator1.getFalseBackFill().fill(condGenerator2.getBasicBlock());
                trueBackFill = condGenerator1.getTrueBackFill();
                condGenerator2.getTrueBackFill().deliverTo(trueBackFill);
                falseBackFill = condGenerator2.getFalseBackFill();
            }else if(binaryExp.getOp() == BinaryOp.AND){
                condGenerator1.getTrueBackFill().fill(condGenerator2.getBasicBlock());
                falseBackFill = condGenerator1.getFalseBackFill();
                trueBackFill = condGenerator2.getTrueBackFill();
                condGenerator2.getFalseBackFill().deliverTo(falseBackFill);
            }else{
                normalGenerate(exp);
            }
        }else{
            normalGenerate(exp);
        }
    }

    private void normalGenerate(Exp exp){
        ExpGenerator.Result expResult = new ExpGenerator(basicBlock.getInstruments(), exp).getResult();
        assert expResult instanceof ExpGenerator.RValueResult;
        CondGoto jump = new CondGoto(((ExpGenerator.RValueResult) expResult).rValue);
        BasicBlockFactory.CondGotoBackFill result = basicBlockFactory.outBasicBlock(basicBlock, jump);
        trueBackFill = result.trueBackFill;
        falseBackFill = result.falseBackFill;
    }

}
