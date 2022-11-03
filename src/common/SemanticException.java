package common;

public class SemanticException extends CompileException{
    public SemanticException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other frontend.error when frontend.semantic";
    }
    public SemanticException() {
        this(0);
    }
    public SemanticException(String info) {
        this(0);
        information = info;
    }
}
