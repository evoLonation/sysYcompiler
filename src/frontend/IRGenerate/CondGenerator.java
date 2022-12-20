package frontend.IRGenerate;

import frontend.IRGenerate.util.BackFill;
import frontend.IRGenerate.util.BasicBlockManager;
import midcode.BasicBlock;
import midcode.instruction.CondGoto;
import midcode.instruction.Goto;
import midcode.value.Constant;
import midcode.value.LValue;
import midcode.value.RValue;
import frontend.parser.nonterminal.exp.BinaryExp;
import frontend.parser.nonterminal.exp.BinaryOp;
import frontend.parser.nonterminal.exp.Exp;

public class CondGenerator extends BasicBlockGenerator {
    private final Exp exp;

    CondGenerator(Exp exp) {
        this.exp = exp;
    }

    private final BackFill trueBackFill = new BackFill();
    private final BackFill falseBackFill = new BackFill();

    public static class CondBackFill {
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
                BasicBlock rightBasicBlock = basicBlockManager.newBasicBlock();
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
        if(expResult instanceof Constant){
            BackFill result = basicBlockManager.outBasicBlock(new Goto());
            if(((Constant) expResult).getNumber() == 0){
                result.deliverTo(falseBackFill);
            }else{
                result.deliverTo(trueBackFill);
            }
        }else{
            assert expResult instanceof LValue;
            CondGoto jump = new CondGoto((LValue) expResult);
            BasicBlockManager.CondGotoBackFill result = basicBlockManager.outBasicBlock(jump);
            result.falseBackFill.deliverTo(falseBackFill);
            result.trueBackFill.deliverTo(trueBackFill);
        }
    }

}
