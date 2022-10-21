package semantic;

import midcode.BasicBlock;
import midcode.instrument.CondGoto;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
import type.IntType;

public class CondGenerator extends BasicBlockGenerator {
    private final Exp exp;

    public CondGenerator(BasicBlock basicBlock, Exp exp) {
        super(basicBlock);
        this.exp = exp;
    }

    public BackFill getTrueBackFill() {
        return trueBackFill;
    }

    public BackFill getFalseBackFill() {
        return falseBackFill;
    }

    private final BackFill trueBackFill = new BackFill();
    private final BackFill falseBackFill = new BackFill();

    @Override
    protected void generate() {
        if(exp instanceof BinaryExp){
            BinaryExp binaryExp = (BinaryExp) exp;
            CondGenerator condGenerator1 = new CondGenerator(basicBlock, binaryExp.getExp1());
            CondGenerator condGenerator2 = new CondGenerator(new BasicBlock(), binaryExp.getExp2());
            if(binaryExp.getOp() == BinaryOp.OR) {
                condGenerator1.getFalseBackFill().fill(condGenerator2.getBasicBlock());
                trueBackFill.merge(condGenerator1.getTrueBackFill());
                trueBackFill.merge(condGenerator2.getTrueBackFill());
                falseBackFill.merge(condGenerator2.getFalseBackFill());
            }else if(binaryExp.getOp() == BinaryOp.AND){
                condGenerator1.getTrueBackFill().fill(condGenerator2.getBasicBlock());
                trueBackFill.merge(condGenerator2.getTrueBackFill());
                falseBackFill.merge(condGenerator1.getFalseBackFill());
                falseBackFill.merge(condGenerator2.getFalseBackFill());
            }else{
                normalGenerate(exp);
            }
        }else{
            normalGenerate(exp);
        }
    }

    private void normalGenerate(Exp exp){
        ExpGenerator expGenerator = new ExpGenerator(basicBlock.getInstruments(), exp);
        assert expGenerator.getResult().type instanceof IntType;
        CondGoto jump = new CondGoto(expGenerator.getResult().value);
        trueBackFill.add(jump, true);
        falseBackFill.add(jump, false);
    }

}
