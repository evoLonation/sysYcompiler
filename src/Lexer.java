import java.util.*;

class Lexer {
    SourceIterator iterator;
    public Lexer(Iterator<Character> iterator){
        this.iterator = new SourceIterator(iterator);
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
        iterator.skip();
        if(iterator.now() != null){
            Character c = iterator.now();
            int nowLine = iterator.line();
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
                return getByLexeme(lexeme.toString(), nowLine);
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
                return new IntConst(Integer.parseInt(digits.toString()), nowLine);
            } else if(c == '"') {
                return getSTRCON();
            } else{
                return getOperator();
            }
        }
        return null;
    }

    private Terminal getByLexeme(String lex, int nowLine) {
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
        return new Terminal(type, lex, nowLine);
    }
    //遇到'"'正常返回，没有则直接报错
    private FormatString getSTRCON(){
        if(iterator.now() != '"'){
            throw new CompileException();
        }
        int line = iterator.line();
        iterator.next();
        List<Char> charList = new ArrayList<>();
        while(true){
            if(isNormalChar()){
                if(iterator.now() == '\\'){
                    iterator.next();
                    if(iterator.now() == 'n'){
                        charList.add(new NormalChar('\n'));
                        iterator.next();
                    }else{
                        throw new CompileException();
                    }
                }else{
                    charList.add(new NormalChar(iterator.now()));
                    iterator.next();
                }
            }else if(iterator.now() == '%'){
                iterator.next();
                if(iterator.now() == 'd'){
                    charList.add(new FormatChar());
                    iterator.next();
                }else{
                    throw new CompileException();
                }
            }else if(iterator.now() == '"') {
                iterator.next();
                break;
            }else {
                throw new CompileException();
            }
        }
        return new FormatString(charList, line);
    }

    private boolean isNormalChar(){
        Character c = iterator.now();
        return c == 32 || c == 33 || c >= 40 && c <= 126;
    }

    private Terminal getOperator(){
        char c = iterator.now();
        int nowLine = iterator.line();
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
        return new Terminal(type, value, nowLine);
    }
}

