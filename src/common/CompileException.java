package common;

public class CompileException extends RuntimeException {
    protected char errorId;
    protected String information;
    protected int lineno;
    public CompileException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other error";
    }

    public CompileException() {
    }

    @Override
    public String toString() {
        return Integer.toString(lineno) + " " + errorId;
    }
}
