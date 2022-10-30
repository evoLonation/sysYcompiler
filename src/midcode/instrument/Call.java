package midcode.instrument;


import midcode.Function;
import midcode.value.Temp;
import midcode.value.Value;

import java.util.List;
import java.util.Optional;

public class Call implements Instrument{
    private Function function;
    private Temp ret;
    private List<Value> params;

    public Call(Function function, List<Value> params, Temp ret) {
        this.function = function;
        this.ret = ret;
        this.params = params;
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

    public List<Value> getParams() {
        return params;
    }

    @Override
    public String print() {
        StringBuilder paramsStr = new StringBuilder();
        params.forEach(p -> paramsStr.append(p.print()).append(", "));
        if(ret == null){
            return "call " + function.getEntry().getName() + " params " + paramsStr;
        }else {
            return ret.print() +  " = call " + function.getEntry().getName() + " params " + paramsStr;
        }
    }
}
