package frontend.semantic;

import frontend.lexer.FormatString;
import midcode.instrument.*;
import midcode.value.Constant;
import midcode.value.RValue;
import midcode.value.Temp;
import frontend.parser.nonterminal.BlockItem;
import frontend.parser.nonterminal.decl.Decl;
import frontend.parser.nonterminal.exp.Exp;
import frontend.parser.nonterminal.exp.LVal;
import frontend.parser.nonterminal.stmt.*;
import util.VoidExecution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 只接受assign，getint，printf，exp，decl
 */
public class SingleItemGenerator extends InstrumentGenerator{
    private final BlockItem blockItem;

    SingleItemGenerator(BlockItem blockItem) {
        this.blockItem = blockItem;
    }

    public void generate(){
        execution.inject();
        execution.exec(blockItem);
    }


    private final VoidExecution<BlockItem> execution = new VoidExecution<BlockItem>() {
        @Override
        public void inject() {
            inject(stmt -> {
                assert stmt instanceof Exp;
                new ExpGenerator((Exp) stmt).generate();
            });

            inject(Assign.class, assign -> {
                LValGenerator.Result lValResult = new LValGenerator(assign.getLVal()).generate();
                if(!(lValResult instanceof LValGenerator.LValueResult || lValResult instanceof LValGenerator.IntPointerResult)){
                    errorRecorder.changeConst(assign.getLVal().getIdent().line(), assign.getLVal().getIdent().getValue());
                    return;
                }
                RValue expResult = new ExpGenerator(assign.getExp()).generate().getRValueResult();
                if(lValResult instanceof LValGenerator.IntPointerResult){
                    addInstrument(new Store(((LValGenerator.IntPointerResult) lValResult).addressValue, expResult));
                }else{
                    addInstrument(new Assignment(((LValGenerator.LValueResult) lValResult).lVal, expResult));
                }
            });

            inject(Decl.class, decl -> new DeclGenerator(decl).generate());

            inject(GetIntNode.class, getint->{
                LVal lVal = getint.getLVal();
                LValGenerator.Result result = new LValGenerator(lVal).generate();
                if(!(result instanceof LValGenerator.LValueResult || result instanceof LValGenerator.IntPointerResult)){
                    errorRecorder.changeConst(lVal.getIdent().line(), lVal.getIdent().getValue());
                    return;
                }
                if(result instanceof LValGenerator.IntPointerResult){
                    Temp temp = valueFactory.newTemp();
                    addInstrument(new GetInt(temp));
                    addInstrument(new Store(((LValGenerator.IntPointerResult) result).addressValue, temp));
                }else{
                    addInstrument(new GetInt(((LValGenerator.LValueResult) result).lVal));
                }
            });

            inject(PrintfNode.class, printf->{
                List<Exp> exps = printf.getExps();
                FormatString formatString = printf.getFormatString();
                List<RValue> rValues = new ArrayList<>();
                for(Exp exp : exps) {
                    rValues.add(new ExpGenerator(exp).generate().getRValueResult());
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
                Iterator<RValue> iterator = rValues.iterator();
                StringBuilder str = null;
                for(FormatString.Char c : formatString.getCharList()){
                    if(c instanceof FormatString.NormalChar){
                        if(str != null){
                            str.append(c);
                        }else{
                            str = new StringBuilder(c.toString());
                        }
                    }else{
                        if(str != null){
                            addInstrument(new PrintString(str.toString()));
                            str = null;
                        }
                        addInstrument(new PrintInt(iterator.next()));
                    }
                }
                if(str != null){
                    addInstrument(new PrintString(str.toString()));
                }
//                addInstrument(new Printf(formatString, rValues));
            });



        }
    };
}
