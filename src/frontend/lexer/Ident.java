package frontend.lexer;

public class Ident extends Terminal{
    public Ident(String value, int lineno) {
        super(TerminalType.IDENFR, value, lineno);
    }
}
