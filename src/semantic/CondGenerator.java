package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.instrument.CondGoto;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
// todo 添加对于or和and的编译期常量计算
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

    private final BackFill trueBackFill = new BackFill();
    private final BackFill falseBackFill = new BackFill();

    @Override
    protected void generate() {
        if(exp instanceof BinaryExp){
            BinaryExp binaryExp = (BinaryExp) exp;
            if(binaryExp.getOp() == BinaryOp.OR || binaryExp.getOp() == BinaryOp.AND){
                CondGenerator condGenerator1 = new CondGenerator(basicBlock, binaryExp.getExp1());
                BasicBlock rightBasicBlock = basicBlockFactory.newBasicBlock();
                CondGenerator condGenerator2 = new CondGenerator(rightBasicBlock, binaryExp.getExp2());
                if(binaryExp.getOp() == BinaryOp.OR) {
                    condGenerator1.getFalseBackFill().fill(rightBasicBlock);
                    condGenerator1.getTrueBackFill().deliverTo(trueBackFill);
                }else{
                    condGenerator1.getFalseBackFill().deliverTo(falseBackFill);
                    condGenerator1.getTrueBackFill().fill(rightBasicBlock);
                }
                condGenerator2.getTrueBackFill().deliverTo(trueBackFill);
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
        result.trueBackFill.deliverTo(trueBackFill);
        result.falseBackFill.deliverTo(falseBackFill);
    }

}
