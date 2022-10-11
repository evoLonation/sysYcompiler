package lexer;

public enum TerminalType {
    IDENFR("IDENFR"),
    INTCON("INTCON"),
    STRCON("STRCON"),
    MAINTK("MAINTK"),
    CONSTTK("CONSTTK"),
    INTTK("INTTK"),
    BREAKTK("BREAKTK"),
    CONTINUETK("CONTINUETK"),
    IFTK("IFTK"),
    ELSETK("ELSETK"),
    NOT("NOT"),
    AND("AND"),
    OR("OR"),
    WHILETK("WHILETK"),
    GETINTTK("GETINTTK"),
    PRINTFTK("PRINTFTK"),
    RETURNTK("RETURNTK"),
    PLUS("PLUS"),
    MINU("MINU"),
    VOIDTK("VOIDTK"),
    MULT("MULT"),
    DIV("DIV"),
    MOD("MOD"),
    LSS("LSS"),
    LEQ("LEQ"),
    GRE("GRE"),
    GEQ("GEQ"),
    EQL("EQL"),
    NEQ("NEQ"),
    ASSIGN("ASSIGN"),
    SEMICN("SEMICN"),
    COMMA("COMMA"),
    LPARENT("LPARENT"),
    RPARENT("RPARENT"),
    LBRACK("LBRACK"),
    RBRACK("RBRACK"),
    LBRACE("LBRACE"),
    RBRACE("RBRACE");
    private final String typeName;
    TerminalType(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return typeName;
    }

}
