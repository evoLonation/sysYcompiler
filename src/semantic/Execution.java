package semantic;

import common.SemanticException;
import midcode.instrument.Instrument;
import parser.nonterminal.AST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Execution<T extends AST, RT> {

    protected abstract void inject();
    protected final Map<Class<? extends T>, Executor<? extends T, RT>> map = new HashMap<>();
    private Executor<? extends T, RT> defaultExec = ast -> {throw new SemanticException();};


    protected <TT extends T> void inject(Class<TT> clazz, Executor<TT, RT> executor) {
        map.put(clazz, executor);
    }

    protected  void inject(Executor<? extends T, RT> executor){
        defaultExec = executor;
    }

    protected RT exec(T ast){
        if(!map.containsKey(ast.getClass())){
            return ((Executor<T, RT>)defaultExec).innerExec(ast);
        }
        return ((Executor<T, RT>)map.get(ast.getClass())).innerExec(ast);
    }

    protected interface Executor<TT, RTT>{
        RTT innerExec(TT ast);
    }


}
