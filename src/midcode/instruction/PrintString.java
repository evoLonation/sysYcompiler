package midcode.instruction;

public class PrintString implements Sequence {
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
