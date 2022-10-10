package common;

public class SemanticException extends CompileException{
    public SemanticException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other error when semantic";
    }
    public SemanticException() {
        this(0);
    }
}
