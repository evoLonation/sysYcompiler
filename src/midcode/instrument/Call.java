package midcode.instrument;


import midcode.Function;
import midcode.value.Temp;

import java.util.Optional;

public class Call implements Instrument{
    private Function function;
    private int paramNumber;
    private Temp ret;


    public Function getFunction() {
        return function;
    }

    public int getParamNumber() {
        return paramNumber;
    }

    public Optional<Temp> getRet() {
        return Optional.ofNullable(ret);
    }
}
