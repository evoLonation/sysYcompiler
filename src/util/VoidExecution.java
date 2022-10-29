package util;

import common.SemanticException;

import java.util.HashMap;
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

    public final void exec(T param){
        Class<?> nowClass = param.getClass();
        while(!map.containsKey(nowClass)){
            if(nowClass.getSuperclass() == Object.class){
                nowClass = nowClass.getInterfaces().length == 1 ? nowClass.getInterfaces()[0] : null;
            }else{
                nowClass = nowClass.getSuperclass();
            }
            if(nowClass == null){
                ((Executor<T>)defaultExec).innerExec(param);
                return;
            }
        }

        ((Executor<T>)map.get(nowClass)).innerExec(param);
    }


    protected interface Executor<TT>{
        abstract void innerExec(TT param);
    }
}
