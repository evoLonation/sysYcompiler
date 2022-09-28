class Terminal extends Word {
    public static final String IDENFR = "IDENFR";
    public static final String INTCON = "INTCON";
    public static final String STRCON = "STRCON";
    public static final String MAINTK = "MAINTK";
    public static final String CONSTTK = "CONSTTK";
    public static final String INTTK = "INTTK";
    public static final String BREAKTK = "BREAKTK";
    public static final String CONTINUETK = "CONTINUETK";
    public static final String IFTK = "IFTK";
    public static final String ELSETK = "ELSETK";
    public static final String NOT = "NOT";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String WHILETK = "WHILETK";
    public static final String GETINTTK = "GETINTTK";
    public static final String PRINTFTK = "PRINTFTK";
    public static final String RETURNTK = "RETURNTK";
    public static final String PLUS = "PLUS";
    public static final String MINU = "MINU";
    public static final String VOIDTK = "VOIDTK";
    public static final String MULT = "MULT";
    public static final String DIV = "DIV";
    public static final String MOD = "MOD";
    public static final String LSS = "LSS";
    public static final String LEQ = "LEQ";
    public static final String GRE = "GRE";
    public static final String GEQ = "GEQ";
    public static final String EQL = "EQL";
    public static final String NEQ = "NEQ";
    public static final String ASSIGN = "ASSIGN";
    public static final String SEMICN = "SEMICN";
    public static final String COMMA = "COMMA";
    public static final String LPARENT = "LPARENT";
    public static final String RPARENT = "RPARENT";
    public static final String LBRACK = "LBRACK";
    public static final String RBRACK = "RBRACK";
    public static final String LBRACE = "LBRACE";
    public static final String RBRACE = "RBRACE";

    protected String value;
    protected int digitValue;
    protected int line;
    public String getValue() {
        return value;
    }

    public int getDigitValue() {
        return digitValue;
    }

    public int getLine() {
        return line;
    }

    Terminal(String type, String value, int lineno) {
        this.type = type;
        this.value = value;
        this.line = lineno;
    }
    Terminal(int value, int lineno) {
        this.type = INTCON;
        this.value = Integer.toString(value);
        this.digitValue = value;
        this.line = lineno;
    }

    @Override
    public String toString() {
        return getType() + " " + getValue();
//        return getLine() + " : " + getType() + " " + getValue();
    }

}
