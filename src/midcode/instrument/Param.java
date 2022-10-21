package midcode.instrument;

import midcode.value.RValue;
import midcode.value.Value;

public class Param implements Instrument{
    // 可以传指针，可以传int
    private Value value;

    public Param(Value value) {
        this.value = value;
    }
}
