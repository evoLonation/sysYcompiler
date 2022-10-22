package semantic;

import lexer.FormatString;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.Constant;
import midcode.value.RValue;
import midcode.value.Temp;
import parser.nonterminal.BlockItem;
import parser.nonterminal.decl.Decl;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.FuncCall;
import parser.nonterminal.exp.LVal;
import parser.nonterminal.stmt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 只接受assign，getint，printf，exp，decl
 */
public class SingleItemGenerator extends InstrumentGenerator{
    private final BlockItem blockItem;

    public SingleItemGenerator(List<Instrument> instruments, BlockItem blockItem) {
        super(instruments);
        this.blockItem = blockItem;
        generate();
    }

    public SingleItemGenerator(BasicBlock basicBlock, BlockItem blockItem) {
        super(basicBlock);
        this.blockItem = blockItem;
        generate();
    }

    public void generate(){
        execution.inject();
        execution.exec(blockItem);
    }


    private final VoidExecution<BlockItem> execution = new VoidExecution<BlockItem>() {
        @Override
        protected void inject() {
            inject(stmt -> {
                assert stmt instanceof Exp;
                new ExpGenerator(instruments, (Exp) stmt);
            });

            inject(Assign.class, assign -> {
                LValGenerator.Result lValResult = new LValGenerator(instruments, assign.getLVal()).getResult();
                if(!(lValResult instanceof LValGenerator.LValueResult || lValResult instanceof LValGenerator.IntPointerResult)){
                    errorRecorder.changeConst(assign.getLVal().getIdent().line(), assign.getLVal().getIdent().getValue());
                    return;
                }
                RValue expResult = new ExpGenerator(instruments, assign.getExp()).getRValueResult();
                if(lValResult instanceof LValGenerator.IntPointerResult){
                    addInstrument(new Store(((LValGenerator.IntPointerResult) lValResult).pointerValue, expResult));
                }else{
                    addInstrument(new Assignment(((LValGenerator.LValueResult) lValResult).lVal, expResult));
                }
            });

            inject(Decl.class, decl -> new DeclGenerator(instruments, decl));

            inject(GetIntNode.class, getint->{
                LVal lVal = getint.getLVal();
                LValGenerator.Result result = new LValGenerator(instruments, lVal).getResult();
                if(!(result instanceof LValGenerator.LValueResult || result instanceof LValGenerator.IntPointerResult)){
                    errorRecorder.changeConst(lVal.getIdent().line(), lVal.getIdent().getValue());
                    return;
                }
                if(result instanceof LValGenerator.IntPointerResult){
                    Temp temp = valueFactory.newTemp();
                    addInstrument(new GetInt(temp));
                    addInstrument(new Store(((LValGenerator.IntPointerResult) result).pointerValue, temp));
                }else{
                    addInstrument(new GetInt(((LValGenerator.LValueResult) result).lVal));
                }
            });

            inject(PrintfNode.class, printf->{
                List<Exp> exps = printf.getExps();
                FormatString formatString = printf.getFormatString();
                List<RValue> rValues = new ArrayList<>();
                for(Exp exp : exps){
                    rValues.add(new ExpGenerator(instruments, exp).getRValueResult());
                }
                int real = rValues.size();
                int need = formatString.getFormatCharNumber();
                if(real != need){
                    errorRecorder.printfParamNotMatch(printf.getLine(), need, real);
                    if(real > need){
                        rValues = rValues.subList(0, need);
                    }else{
                        while(real != need){
                            rValues.add(new Constant(0));
                            real++;
                        }
                    }
                }
                addInstrument(new Printf(formatString, rValues));
            });



        }
    };
}
