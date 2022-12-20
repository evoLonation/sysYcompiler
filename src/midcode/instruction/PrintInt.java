package midcode.instruction;

import midcode.value.RValue;

public class PrintInt implements Sequence {
    private RValue value;

    public PrintInt(RValue value) {
        this.value = value;
    }

    public RValue getValue() {
        return value;
    }

    public void setValue(RValue value) {
        this.value = value;
    }

    @Override
    public String print() {
        return "printInt " + value.print();
    }
}
