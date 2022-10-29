package frontend.lexer;

public class IntConst extends Terminal{
    protected int digitValue;
    public int getDigitValue() {
        return digitValue;
    }
    IntConst(int value, int lineno) {
        super(TerminalType.INTCON, Integer.toString(value), lineno);
        this.digitValue = value;
    }
}
