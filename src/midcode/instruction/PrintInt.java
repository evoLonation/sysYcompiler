package midcode.instruction;

import midcode.value.RValue;

public class PrintInt implements Sequence {
    private final RValue rValue;

    public PrintInt(RValue rValue) {
        this.rValue = rValue;
    }

    public RValue getRValue() {
        return rValue;
    }

    @Override
    public String print() {
        return "printInt " + rValue.print();
    }
}
