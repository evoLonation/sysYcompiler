package frontend.lexer;

public class Terminal{
    protected String value;
    protected int line;
    protected TerminalType type;
    public String getValue() {
        return value;
    }

    public TerminalType getTerminalType() {
        return type;
    }

    public int line() {
        return line;
    }

    public Terminal(TerminalType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return getTerminalType().getName() + " " + getValue();
//        return getLine() + " : " + getType() + " " + getValue();
    }

}
