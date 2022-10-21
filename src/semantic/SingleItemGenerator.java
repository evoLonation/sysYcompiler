package semantic;

import common.SemanticException;
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
    }

    public void generate(){
        execution.exec(blockItem);
    }


    private final VoidExecution<BlockItem> execution = new VoidExecution<BlockItem>() {
        @Override
        protected void inject() {
            inject(stmt -> {throw new SemanticException();});

            inject(Exp.class, stmt -> new ExpGenerator(instruments, stmt));

            inject(Assign.class, assign -> {
                LValGenerator.Result lValResult = new LValGenerator(instruments, assign.getLVal(), true).getResult();
                ExpGenerator.Result expResult = new ExpGenerator(instruments, assign.getExp()).getResult();
                assert lValResult.type instanceof IntType;
                assert expResult.type instanceof IntType;
                if(lValResult.identType == LValGenerator.IdentType.Pointer){
                    addInstrument(new Store(lValResult.value, expResult.value));
                }else{
                    assert lValResult.value instanceof LValue;
                    addInstrument(new Assignment((LValue) lValResult.value, expResult.value));
                }
            });

            inject(Decl.class, decl -> new DeclGenerator(instruments, decl));

            inject(GetInt.class, getint->{throw new UnsupportedOperationException();});

            inject(Printf.class, printf->{throw new UnsupportedOperationException();});


        }
    };
}
