package semantic;

import parser.nonterminal.AST;

import java.util.HashMap;
import java.util.Map;

public abstract class VoidExecution<T extends AST> extends Generator {
    protected VoidExecution(){
        inject();
    }

    protected abstract void inject();
    private final Map<Class<? extends T>, Executor<? extends T>> map = new HashMap<>();
    private Executor<? extends T> defaultExec;

    protected <TT extends T> void inject(Class<TT> clazz, Executor<TT> executor){
        map.put(clazz, executor);
    }

    protected  void inject(Executor<? extends T> executor){
        defaultExec = executor;
    }

    protected void exec(T ast){
        if(!map.containsKey(ast.getClass())){
            ((Executor<T>)defaultExec).innerExec(ast);
        }
        ((Executor<T>)map.get(ast.getClass())).innerExec(ast);
    }

    protected interface Executor<TT>{
        abstract void innerExec(TT ast);
    }
}
