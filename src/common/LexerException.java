package common;

public class LexerException extends CompileException{
    public LexerException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other error when lexer";
    }
    public LexerException() {
        this(0);
    }
}
