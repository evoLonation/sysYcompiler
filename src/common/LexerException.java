package common;

public class LexerException extends CompileException{
    public LexerException(int lineno) {
        this.lineno = lineno;
        this.errorId = 'o';
        this.information = "other frontend.error when frontend.lexer";
    }
    public LexerException() {
        this(0);
    }
}
