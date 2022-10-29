package frontend.parser.nonterminal.stmt;

public class Break implements Stmt {
    private final int line;

    public Break(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }
}
