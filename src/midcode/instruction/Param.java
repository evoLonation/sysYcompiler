package midcode.instruction;

import midcode.value.Value;

public class Param implements Sequence {
    // 可以传指针，可以传int
    private Value value;

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

    public void setValue(Value value) {
        this.value = value;
    }
}
