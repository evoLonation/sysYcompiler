package parser.nonterminal.stmt;

public class Continue implements Stmt {
    private final int line;

    public Continue(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
