import java.util.*;

class Lexer {
    MyIterator<Character> iterator;
    public Lexer(Iterator<Character> iterator){
        this.iterator = new MyIterator<>(iterator);
    }

    public boolean isIdentifierNoDigit(char c){
        return Character.isLetter(c) || c == '_';
    }

    public List<Terminal> analysis(){
        List<Terminal> wordList = new ArrayList<>();
        while(true){
            Terminal terminal = analysisOne();
            if (terminal != null){
                wordList.add(terminal);
            }else{
                return wordList;
            }
        }
    }
    // if done, return null
    public Terminal analysisOne() {
        // 先获得字符判断接下来是数字还是标识符还是其他符号
        // 一共两种特殊符号，一种是全英文，一种是符号
        nextChar();
        if(iterator.now() != null){
            Character c = iterator.now();
            if (isIdentifierNoDigit(c)) {
                StringBuilder lexeme = new StringBuilder(c.toString());
                while (iterator.now() != null) {
                    iterator.next();
                    c = iterator.now();
                    if (isIdentifierNoDigit(c) || Character.isDigit(c)) {
                        lexeme.append(c);
                    }else{
                        break;
                    }
                }
                return getByLexeme(lexeme.toString());
            } else if (Character.isDigit(c)) {
                StringBuilder digits = new StringBuilder(c.toString());
                while (iterator.now() != null) {
                    iterator.next();
                    c = iterator.now();
                    if (Character.isDigit(c)) {
                        digits.append(c);
                    }else{
                        break;
                    }
                }
                return new Terminal(Integer.parseInt(digits.toString()));
            } else if(c == '"') {
                return new Terminal(Terminal.STRCON, getSTRCON());
            } else{
                return getOperator();
            }
        }
        return null;
    }

    // 如果iterator.now是字符，则不动;否则找到第一个字符
    private void nextChar() {
        while (iterator.now() != null) {
            char c = iterator.now();
            if(c == '/'){
                char nextC = iterator.pre(1);
                if(nextC == '/'){
                    iterator.next();
                    iterator.next();
                    Character commentChar = iterator.now();
                    while(commentChar != '\n' && commentChar != '\r' && commentChar != null){
                        iterator.next();
                        commentChar = iterator.now();
                    }
                    iterator.next();
                    nextChar();
                    return;
                }else if(nextC == '*') {
                    iterator.next();
                    iterator.next();
                    char commentChar1 = iterator.now();
                    iterator.next();
                    char commentChar2 = iterator.now();
                    while(commentChar1 != '*' || commentChar2 != '/'){
                        commentChar1 = commentChar2;
                        iterator.next();
                        commentChar2 = iterator.now();
                    }
                    iterator.next();
                    nextChar();
                    return;
                }else{
                    return;
                }
            }else if (Character.isSpaceChar(c) || c == '\r' || c == '\n' || c == '\t') {
                iterator.next();
            }else {
                return;
            }
        }
    }

    private Terminal getByLexeme(String lex) {
        String type;
        switch (lex){
            case "main" : type = Terminal.MAINTK; break;
            case "const" : type = Terminal.CONSTTK; break;
            case "break" : type = Terminal.BREAKTK; break;
            case "continue" : type = Terminal.CONTINUETK; break;
            case "int" : type = Terminal.INTTK; break;
            case "if" : type = Terminal.IFTK; break;
            case "else" : type = Terminal.ELSETK; break;
            case "while" : type = Terminal.WHILETK; break;
            case "getint" : type = Terminal.GETINTTK; break;
            case "printf" : type = Terminal.PRINTFTK; break;
            case "return" : type = Terminal.RETURNTK; break;
            case "void" : type = Terminal.VOIDTK; break;
            default: type = Terminal.IDENFR; break;
        }
        return new Terminal(type, lex);
    }
    //遇到'"'正常返回，没有则直接报错
    private String getSTRCON(){
        if(iterator.now() != '"'){
            throw new CompileException();
        }
        iterator.next();
        StringBuilder ret = new StringBuilder("\"");
        Character c = iterator.now();
        while(c != null){
            ret.append(c);
            iterator.next();
            if(c == '"'){
                return ret.toString();
            }
            c = iterator.now();
        }
        throw new CompileException();
    }
    private Terminal getOperator(){
        char c = iterator.now();
        iterator.next();
        String value = Character.toString(c);
        String type = null;
        switch (c) {
            case '!':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = Terminal.NEQ;
                } else {
                    type = Terminal.NOT;
                }
                break;
            case '&':
                if (iterator.now() == '&') {
                    value += '&';
                    iterator.next();
                    type = Terminal.AND;
                } else {
                    throw new CompileException();
                }
                break;
            case '|':
                if (iterator.now() == '|') {
                    value += '|';
                    iterator.next();
                    type = Terminal.OR;
                } else {
                    throw new CompileException();
                }
                break;
            case '<':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = Terminal.LEQ;
                } else {
                    type = Terminal.LSS;
                }
                break;
            case '>':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = Terminal.GEQ;
                } else {
                    type = Terminal.GRE;
                }
                break;
            case '=':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = Terminal.EQL;
                } else {
                    type = Terminal.ASSIGN;
                }
                break;
            case '+' :
                type = Terminal.PLUS;
                break;
            case '-':
                type = Terminal.MINU;
                break;
            case '*':
                type = Terminal.MULT;
                break;
            case '/':
                type = Terminal.DIV;
                break;
            case '%':
                type = Terminal.MOD;
                break;
            case ';':
                type = Terminal.SEMICN;
                break;
            case ',':
                type = Terminal.COMMA;
                break;
            case '(':
                type = Terminal.LPARENT;
                break;
            case ')':
                type = Terminal.RPARENT;
                break;
            case '[':
                type = Terminal.LBRACK;
                break;
            case ']':
                type = Terminal.RBRACK;
                break;
            case '{':
                type = Terminal.LBRACE;
                break;
            case '}':
                type = Terminal.RBRACE;
                break;
        }
        return new Terminal(type, value);
    }
}

