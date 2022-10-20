package midcode.instrument;

import midcode.value.RValue;

public class Param implements Instrument{
    private RValue value;

    public Param(RValue value) {
        this.value = value;
    }
}
