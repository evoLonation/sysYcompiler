package midcode.instrument;


import midcode.Function;
import midcode.value.Temp;
import midcode.value.Value;

import java.util.List;
import java.util.Optional;

public class Call implements Sequence{
    private Function function;
    private Temp ret;

    public Call(Function function, Temp ret) {
        this.function = function;
        this.ret = ret;
    }

    public Call(Function function, List<Value> params) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }


    public Optional<Temp> getRet() {
        return Optional.ofNullable(ret);
    }


    @Override
    public String print() {
        if(ret == null){
            return "call " + function.getEntry().getName() ;
        }else {
            return ret.print() +  " = call " + function.getEntry().getName() ;
        }
    }
}
