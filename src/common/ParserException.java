package common;

public class ParserException extends CompileException{
    public ParserException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other frontend.error when parser";
    }
    public ParserException() {
        this(0);
    }
}
