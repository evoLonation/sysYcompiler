package midcode.instruction;


import midcode.Function;
import midcode.value.LValue;
import midcode.value.Temp;
import midcode.value.Value;

import java.util.List;
import java.util.Optional;

public class Call implements Sequence{
    private Function function;
    private LValue ret;

    public Call(Function function, LValue ret) {
        this.function = function;
        this.ret = ret;
    }

    public Call(Function function, List<Value> params) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }


    public Optional<LValue> getRet() {
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

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setRet(LValue ret) {
        this.ret = ret;
    }

}
