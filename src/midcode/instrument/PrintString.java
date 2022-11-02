package midcode.instrument;

import midcode.value.RValue;

public class PrintString implements Instrument{
    private final String string;

    public PrintString(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String print() {
        return "printString \"" + string + "\"";
    }
}
