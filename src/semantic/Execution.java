package semantic;

import common.SemanticException;
import midcode.instrument.Instrument;
import parser.nonterminal.AST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Execution<T extends AST, RT> {

    public abstract void inject();
    protected final Map<Class<? extends T>, Executor<? extends T, RT>> map = new HashMap<>();
    private Executor<? extends T, RT> defaultExec = ast -> {throw new SemanticException();};


    protected <TT extends T> void inject(Class<TT> clazz, Executor<TT, RT> executor) {
        map.put(clazz, executor);
    }

    protected  void inject(Executor<? extends T, RT> executor){
        defaultExec = executor;
    }

    public final RT exec(T ast){
        Class<?> nowClass = ast.getClass();
        while(!map.containsKey(nowClass)){
            if(nowClass.getSuperclass() == Object.class){
                nowClass = nowClass.getInterfaces().length == 1 ? nowClass.getInterfaces()[0] : null;
            }else{
                nowClass = nowClass.getSuperclass();
            }
            if(nowClass == null){
                return ((Executor<T, RT>)defaultExec).innerExec(ast);
            }
        }
        return ((Executor<T, RT>)map.get(nowClass)).innerExec(ast);
    }

    protected interface Executor<TT, RTT>{
        RTT innerExec(TT ast);
    }


}
