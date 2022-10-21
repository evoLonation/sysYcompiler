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
            inject(stmt -> {throw new SemanticException();});

            inject(Exp.class, stmt -> new ExpGenerator(instruments, stmt));

            inject(Assign.class, assign -> {
                LValGenerator.Result lValResult = new LValGenerator(instruments, assign.getLVal()).getResult();
                assert lValResult instanceof LValGenerator.LValueResult || lValResult instanceof LValGenerator.IntPointerResult;
                ExpGenerator.Result expResult = new ExpGenerator(instruments, assign.getExp()).getResult();
                assert expResult instanceof ExpGenerator.RValueResult;
                if(lValResult instanceof LValGenerator.IntPointerResult){
                    addInstrument(new Store(((LValGenerator.IntPointerResult) lValResult).pointerValue, ((ExpGenerator.RValueResult) expResult).rValue));
                }else{
                    addInstrument(new Assignment(((LValGenerator.LValueResult) lValResult).lVal, ((ExpGenerator.RValueResult) expResult).rValue));
                }
            });

            inject(Decl.class, decl -> new DeclGenerator(instruments, decl));

            inject(GetInt.class, getint->{
                // todo
            });

            inject(Printf.class, printf->{
                //todo
            });



        }
    };
}
