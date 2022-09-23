import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

interface WordInterface {
}
class LexException extends RuntimeException {
}
class DigitWord implements WordInterface{
    protected String type;
    protected int value;
    public String getType(){
        return type;
    }
    public int getValue(){
        return value;
    }

    public DigitWord(String type, int value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return getType() + " " + getValue();
    }
}
class Word implements WordInterface{
    protected String type;
    protected String value;
    public String getType(){
        return type;
    }
    public String getValue(){
        return value;
    }

    public Word(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return getType() + " " + getValue();
    }
}

class LexicalAnalyser {
    ListIterator<Character> iterator;
    public LexicalAnalyser(ListIterator<Character> iterator){
        this.iterator = iterator;
    }

    public boolean isIdentifierNoDigit(char c){
        return Character.isLetter(c) || c == '_';
    }

    public List<WordInterface> analysis(){
        List<WordInterface> wordList = new ArrayList<>();
        while(true){
            Optional<WordInterface> optional = analysisOne();
            if (optional.isPresent()){
                wordList.add(optional.get());
            }else{
                return wordList;
            }
        }
    }
    public Optional<WordInterface> analysisOne() {
        // 先获得字符判断接下来是数字还是标识符还是其他符号
        // 一共两种特殊符号，一种是全英文，一种是符号
        AtomicReference<Optional<WordInterface>> ret = new AtomicReference<>();
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
                ret.set(Optional.of(new DigitWord("INTCON", Integer.parseInt(digits.toString()))));
            } else if(c == '"') {
                ret.set(Optional.of(new Word("STRCON" , "\"" + getChars(iterator) +"\"")));
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

    private Word getByLexeme(String lex) {
        Map<String, String> reservedWordMap = new HashMap<>();


        String type;
        switch (lex){
            case "main" : type = "MAINTK"; break;
            case "const" : type = "CONSTTK"; break;
            case "break" : type = "BREAKTK"; break;
            case "continue" : type = "CONTINUETK"; break;
            case "int" : type = "INTTK"; break;
            case "if" : type = "IFTK"; break;
            case "else" : type = "ELSETK"; break;
            case "while" : type = "WHILETK"; break;
            case "getint" : type = "GETINTTK"; break;
            case "printf" : type = "PRINTFTK"; break;
            case "return" : type = "RETURNTK"; break;
            case "void" : type = "VOIDTK"; break;
            default: type = "IDENFR"; break;
        }
        return new Word(type, lex);
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
    private Word getOperator(ListIterator<Character> iterator){
        char c = iterator.next();
        String value = Character.toString(c);
        String type = null;
        switch (c) {
            case '!':
                if (iterator.next() == '=') {
                    value += '=';
                    type = "NEQ";
                } else {
                    iterator.previous();
                    type = "NOT";
                }
                break;
            case '&':
                if (iterator.next() == '&') {
                    value += '&';
                    type = "AND";
                } else {
                    throw new LexException();
                }
                break;
            case '|':
                if (iterator.next() == '|') {
                    value += '|';
                    type = "OR";
                } else {
                    throw new LexException();
                }
                break;
            case '<':
                if (iterator.next() == '=') {
                    value += '=';
                    type = "LEQ";
                } else {
                    iterator.previous();
                    type = "LSS";
                }
                break;
            case '>':
                if (iterator.next() == '=') {
                    value += '=';
                    type = "GEQ";
                } else {
                    iterator.previous();
                    type = "GRE";
                }
                break;
            case '=':
                if (iterator.next() == '=') {
                    value += '=';
                    type = "EQL";
                } else {
                    iterator.previous();
                    type = "ASSIGN";
                }
                break;
            case '+' :
                type = "PLUS";
                break;
            case '-':
                type = "MINU";
                break;
            case '*':
                type = "MULT";
                break;
            case '/':
                type = "DIV";
                break;
            case '%':
                type = "MOD";
                break;
            case ';':
                type = "SEMICN";
                break;
            case ',':
                type = "COMMA";
                break;
            case '(':
                type = "LPARENT";
                break;
            case ')':
                type = "RPARENT";
                break;
            case '[':
                type = "LBRACK";
                break;
            case ']':
                type = "RBRACK";
                break;
            case '{':
                type = "LBRACE";
                break;
            case '}':
                type = "RBRACE";
                break;
        }
        return new Word(type, value);
    }
}

