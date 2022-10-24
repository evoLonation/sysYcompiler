package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.instrument.CondGoto;
import midcode.value.RValue;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
// todo 添加对于or和and的编译期常量计算
public class CondGenerator extends BasicBlockGenerator {
    private final Exp exp;

    CondGenerator(Exp exp) {
        this.exp = exp;
    }

    private final BackFill trueBackFill = new BackFill();
    private final BackFill falseBackFill = new BackFill();

    public static class CondBackFill{
        public BackFill trueBackFill;
        public BackFill falseBackFill;

        public CondBackFill(BackFill trueBackFill, BackFill falseBackFill) {
            this.trueBackFill = trueBackFill;
            this.falseBackFill = falseBackFill;
        }
    }

    CondBackFill generate() {
        if(exp instanceof BinaryExp){
            BinaryExp binaryExp = (BinaryExp) exp;
            if(binaryExp.getOp() == BinaryOp.OR || binaryExp.getOp() == BinaryOp.AND){
                CondBackFill cond1BackFill = new CondGenerator(binaryExp.getExp1()).generate();
                BasicBlock rightBasicBlock = basicBlockFactory.newBasicBlock();
                CondBackFill cond2BackFill = new CondGenerator(binaryExp.getExp2()).generate();
                if(binaryExp.getOp() == BinaryOp.OR) {
                    cond1BackFill.falseBackFill.fill(rightBasicBlock);
                    cond1BackFill.trueBackFill.deliverTo(trueBackFill);
                }else{
                    cond1BackFill.falseBackFill.deliverTo(falseBackFill);
                    cond1BackFill.trueBackFill.fill(rightBasicBlock);
                }
                cond2BackFill.trueBackFill.deliverTo(trueBackFill);
                cond2BackFill.falseBackFill.deliverTo(falseBackFill);
            }else{
                normalGenerate(exp);
            }
        }else{
            normalGenerate(exp);
        }
        return new CondBackFill(trueBackFill, falseBackFill);
    }

    private void normalGenerate(Exp exp){
        RValue expResult = new ExpGenerator(exp).generate().getRValueResult();
        CondGoto jump = new CondGoto(expResult);
        BasicBlockFactory.CondGotoBackFill result = basicBlockFactory.outBasicBlock(jump);
        result.trueBackFill.deliverTo(trueBackFill);
        result.falseBackFill.deliverTo(falseBackFill);
    }

}
