import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

class Lexer {
    ListIterator<Character> iterator;
    public Lexer(ListIterator<Character> iterator){
        this.iterator = iterator;
    }

    public boolean isIdentifierNoDigit(char c){
        return Character.isLetter(c) || c == '_';
    }

    public List<Terminal> analysis(){
        List<Terminal> wordList = new ArrayList<>();
        while(true){
            Optional<Terminal> optional = analysisOne();
            if (optional.isPresent()){
                wordList.add(optional.get());
            }else{
                return wordList;
            }
        }
    }
    public Optional<Terminal> analysisOne() {
        // 先获得字符判断接下来是数字还是标识符还是其他符号
        // 一共两种特殊符号，一种是全英文，一种是符号
        AtomicReference<Optional<Terminal>> ret = new AtomicReference<>();
        ret.set(Optional.empty());
        getNextChar().ifPresent((Character c) -> {
            if (isIdentifierNoDigit(c)) {
                StringBuilder lexeme = new StringBuilder(c.toString());
                while (iterator.hasNext()) {
                    c = iterator.next();
                    if (isIdentifierNoDigit(c) || Character.isDigit(c)) {
                        lexeme.append(c);
                    }else{
                        iterator.previous();
                        break;
                    }
                }
                ret.set(Optional.of(getByLexeme(lexeme.toString())));
            } else if (Character.isDigit(c)) {
                StringBuilder digits = new StringBuilder(c.toString());
                while (iterator.hasNext()) {
                    c = iterator.next();
                    if (Character.isDigit(c)) {
                        digits.append(c);
                    }else{
                        iterator.previous();
                        break;
                    }
                }
                ret.set(Optional.of(new Terminal(Integer.parseInt(digits.toString()))));
            } else if(c == '"') {
                ret.set(Optional.of(new Terminal(Terminal.STRCON, "\"" + getChars(iterator) +"\"")));
            }else if(c == '/' && iterator.next() == '/'){

            }else{
                if(c == '/') iterator.previous();
                iterator.previous();
                ret.set(Optional.of(getOperator(iterator)));
            }
        });
        return ret.get();
    }

    // 获取非空字符
    private Optional<Character> getNextChar() {
        while (iterator.hasNext()) {
            char c = iterator.next();
            if(c == '/'){
                char nextC = iterator.next();
                if(nextC == '/'){
                    char commentChar = iterator.next();
                    while(commentChar != '\n' && commentChar != '\r'){
                        commentChar = iterator.next();
                    }
                    return getNextChar();
                }else if(nextC == '*') {
                    char commentChar1 = iterator.next();
                    char commentChar2 = iterator.next();
                    while(commentChar1 != '*' || commentChar2 != '/'){
                        commentChar1 = commentChar2;
                        commentChar2 = iterator.next();
                    }
                    return getNextChar();
                }else{
                    iterator.previous();
                }
            }
            if (Character.isSpaceChar(c) || c == '\r' || c == '\n' || c == '\t') {
                continue;
            }
            return Optional.of(c);
        }
        return Optional.empty();
    }

    private Terminal getByLexeme(String lex) {
        Map<String, String> reservedWordMap = new HashMap<>();


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
    private String getChars(ListIterator<Character> iterator){
        StringBuilder ret = new StringBuilder();

        while(iterator.hasNext()){
            char c = iterator.next();
            if(c == '"'){
                return ret.toString();
            }
            ret.append(c);
        }
        throw new LexException();
    }
    private Terminal getOperator(ListIterator<Character> iterator){
        char c = iterator.next();
        String value = Character.toString(c);
        String type = null;
        switch (c) {
            case '!':
                if (iterator.next() == '=') {
                    value += '=';
                    type = Terminal.NEQ;
                } else {
                    iterator.previous();
                    type = Terminal.NOT;
                }
                break;
            case '&':
                if (iterator.next() == '&') {
                    value += '&';
                    type = Terminal.AND;
                } else {
                    throw new LexException();
                }
                break;
            case '|':
                if (iterator.next() == '|') {
                    value += '|';
                    type = Terminal.OR;
                } else {
                    throw new LexException();
                }
                break;
            case '<':
                if (iterator.next() == '=') {
                    value += '=';
                    type = Terminal.LEQ;
                } else {
                    iterator.previous();
                    type = Terminal.LSS;
                }
                break;
            case '>':
                if (iterator.next() == '=') {
                    value += '=';
                    type = Terminal.GEQ;
                } else {
                    iterator.previous();
                    type = Terminal.GRE;
                }
                break;
            case '=':
                if (iterator.next() == '=') {
                    value += '=';
                    type = Terminal.EQL;
                } else {
                    iterator.previous();
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

