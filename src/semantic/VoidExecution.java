package semantic;

import common.SemanticException;
import midcode.instrument.Instrument;
import parser.nonterminal.AST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class VoidExecution<T>  {

    public abstract void inject();
    private final Map<Class<? extends T>, Executor<? extends T>> map = new HashMap<>();
    private Executor<? extends T> defaultExec = ast -> {throw new SemanticException();};

    protected <TT extends T> void inject(Class<TT> clazz, Executor<TT> executor){
        map.put(clazz, executor);
    }

    protected  void inject(Executor<? extends T> executor){
        defaultExec = executor;
    }

    public final void exec(T ast){
        Class<?> nowClass = ast.getClass();
        while(!map.containsKey(nowClass)){
            if(nowClass.getSuperclass() == Object.class){
                nowClass = nowClass.getInterfaces().length == 1 ? nowClass.getInterfaces()[0] : null;
            }else{
                nowClass = nowClass.getSuperclass();
            }
            if(nowClass == null){
                ((Executor<T>)defaultExec).innerExec(ast);
                return;
            }
        }

        ((Executor<T>)map.get(nowClass)).innerExec(ast);
    }


    protected interface Executor<TT>{
        abstract void innerExec(TT ast);
    }
}
