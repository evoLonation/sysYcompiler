package semantic;

import midcode.instrument.Goto;
import midcode.instrument.IfGoto;
import midcode.instrument.Label;
import midcode.instrument.LabelFactory;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;

public class CondGenerator extends VoidExecution<Exp> {
    private final LabelFactory labelFactory = LabelFactory.getInstance();
    private Exp exp;

    private Label trueLabel;
    private  Label falseLabel;

    public CondGenerator(Exp exp) {
        this.exp = exp;
        trueLabel = labelFactory.newLabel();
        falseLabel = labelFactory.newLabel();
    }

    @Override
    protected void inject() {
        inject(BinaryExp.class, exp -> {
            if(exp.getOp() == BinaryOp.OR){
                CondGenerator condGenerator1 = new CondGenerator(exp.getExp1());
                CondGenerator condGenerator2 = new CondGenerator(exp.getExp2());
                condGenerator1.generate();
                condGenerator2.generate();
                midCodes.addAll(condGenerator1.getMidCodes());
                midCodes.add(condGenerator1.getFalseLabel());
                midCodes.addAll(condGenerator2.getMidCodes());
                midCodes.add(condGenerator2.getTrueLabel());
                midCodes.add(condGenerator1.getTrueLabel());
                midCodes.add(new Goto(trueLabel));
                midCodes.add(condGenerator2.getFalseLabel());
                midCodes.add(new Goto(falseLabel));
            }else if(exp.getOp() == BinaryOp.AND){
                CondGenerator condGenerator1 = new CondGenerator(exp.getExp1());
                CondGenerator condGenerator2 = new CondGenerator(exp.getExp2());
                condGenerator1.generate();
                condGenerator2.generate();
                midCodes.addAll(condGenerator1.getMidCodes());
                midCodes.add(condGenerator1.getTrueLabel());
                midCodes.addAll(condGenerator2.getMidCodes());
                midCodes.add(condGenerator2.getTrueLabel());
                midCodes.add(new Goto(trueLabel));
                midCodes.add(condGenerator1.getFalseLabel());
                midCodes.add(condGenerator2.getFalseLabel());
                midCodes.add(new Goto(falseLabel));
            }else{
                NormalGenerate(exp);
            }
        });

        inject(this::NormalGenerate);
    }

    private void NormalGenerate(Exp exp){
        AddExpGenerator addExpGenerator = new AddExpGenerator(exp);
        midCodes.addAll(addExpGenerator.getMidCodes());
        midCodes.add(new IfGoto(trueLabel, addExpGenerator.getResult()));
        midCodes.add(new Goto(falseLabel));
    };

    public CondGenerator generate(){
        exec(exp);
        return this;
    }

    public Label getTrueLabel() {
        return trueLabel;
    }

    public Label getFalseLabel() {
        return falseLabel;
    }
}
