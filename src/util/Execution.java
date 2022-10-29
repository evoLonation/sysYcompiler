package util;

import common.SemanticException;

import java.util.HashMap;
import java.util.Map;

public abstract class Execution<T, RT> {

    public abstract void inject();
    protected final Map<Class<? extends T>, Executor<? extends T, RT>> map = new HashMap<>();
    private Executor<? extends T, RT> defaultExec = ast -> {throw new SemanticException();};


    protected <TT extends T> void inject(Class<TT> clazz, Executor<TT, RT> executor) {
        map.put(clazz, executor);
    }

    protected  void inject(Executor<? extends T, RT> executor){
        defaultExec = executor;
    }

    public final RT exec(T param){
        Class<?> nowClass = param.getClass();
        while(!map.containsKey(nowClass)){
            if(nowClass.getSuperclass() == Object.class){
                nowClass = nowClass.getInterfaces().length == 1 ? nowClass.getInterfaces()[0] : null;
            }else{
                nowClass = nowClass.getSuperclass();
            }
            if(nowClass == null){
                return ((Executor<T, RT>)defaultExec).innerExec(param);
            }
        }
        return ((Executor<T, RT>)map.get(nowClass)).innerExec(param);
    }

    protected interface Executor<TT, RTT>{
        RTT innerExec(TT param);
    }


}
