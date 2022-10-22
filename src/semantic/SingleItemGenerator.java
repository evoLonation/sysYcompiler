package semantic;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.Assignment;
import midcode.instrument.Instrument;
import midcode.instrument.Store;
import midcode.value.LValue;
import parser.nonterminal.BlockItem;
import parser.nonterminal.decl.Decl;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.FuncCall;
import parser.nonterminal.stmt.*;
import type.*;

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
                }
                ExpGenerator.Result expResult = new ExpGenerator(instruments, assign.getExp()).getResult();
                assert expResult instanceof ExpGenerator.RValueResult;
                if(lValResult instanceof LValGenerator.IntPointerResult){
                    addInstrument(new Store(((LValGenerator.IntPointerResult) lValResult).pointerValue, ((ExpGenerator.RValueResult) expResult).rValue));
                }else{
                    addInstrument(new Assignment(((LValGenerator.LValueResult) lValResult).lVal, ((ExpGenerator.RValueResult) expResult).rValue));
                }
            });

            inject(Decl.class, decl -> new DeclGenerator(instruments, decl));

            inject(FuncCall.class, funcCall -> new FuncCallGenerator(instruments, funcCall));

            inject(GetInt.class, getint->{
                // todo
            });

            inject(Printf.class, printf->{
                //todo
            });



        }
    };
}
