package midcode.instrument;

import midcode.value.Value;

public class Param implements Instrument{
    // 可以传指针，可以传int
    private final Value value;

    public Param(Value value) {
        this.value = value;
    }

    @Override
    public String print() {
        return "param " + value.print();
    }

    public Value getValue() {
        return value;
    }
}
