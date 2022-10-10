package common;

public abstract class CompileException extends RuntimeException {
    protected char errorId;
    protected String information;
    protected int lineno;


    protected CompileException() {
    }

    @Override
    public String toString() {
        return Integer.toString(lineno) + " " + errorId + " : " + information;
    }
}
