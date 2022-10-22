package midcode.instrument;


import midcode.Function;
import midcode.value.Temp;

import java.util.Optional;

public class Call implements Instrument{
    private Function function;
    private int paramNumber;
    private Temp ret;

    public Call(Function function, int paramNumber, Temp ret) {
        this.function = function;
        this.paramNumber = paramNumber;
        this.ret = ret;
    }

    public Call(Function function, int paramNumber) {
        this.function = function;
        this.paramNumber = paramNumber;
    }

    public Function getFunction() {
        return function;
    }

    public int getParamNumber() {
        return paramNumber;
    }

    public Optional<Temp> getRet() {
        return Optional.ofNullable(ret);
    }

    @Override
    public String print() {
        return "call " + function.getEntry().getName() + " params " + getParamNumber();
    }
}
