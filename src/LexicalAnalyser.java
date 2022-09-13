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
    public boolean isIdentifierNoDigit(char c){
        return Character.isLetter(c) || c == '_';
    }

    public Optional<WordInterface> analysis(ListIterator<Character> iterator) {
        // 先获得字符判断接下来是数字还是标识符还是其他符号
        // 一共两种特殊符号，一种是全英文，一种是符号
        AtomicReference<Optional<WordInterface>> ret = new AtomicReference<>();
        ret.set(Optional.empty());
        getNextChar(iterator).ifPresent((Character c) -> {
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
            }else{
                iterator.previous();
                ret.set(Optional.of(getOperator(iterator)));
            }
        });
        return ret.get();
    }

    // 获取非空字符
    private Optional<Character> getNextChar(Iterator<Character> iterator) {
        while (iterator.hasNext()) {
            char c = iterator.next();
            if (Character.isSpaceChar(c)) {
                continue;
            }
            return Optional.of(c);
        }
        return Optional.empty();
    }

    private Word getByLexeme(String lex) {
        String type;
        switch (lex){
            case "main" : type = "MAINTK";
            case "const" : type = "CONSTTK";
            case "break" : type = "BREAKTK";
            case "continue" : type = "CONTINUETK";
            case "int" : type = "INTTK";
            case "if" : type = "IFTK";
            case "else" : type = "ELSETK";
            case "while" : type = "WHILETK";
            case "getint" : type = "GETINTTK";
            case "printf" : type = "PRINTFTK";
            case "return" : type = "RETURNTK";
            case "void" : type = "VOIDTK";
            default: type = "IDENFR";
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
                type = "MINUS";
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

class Test{
    public static void main(String[] args) {
        String inputFile = "input.txt";
        List<Character> charList = new ArrayList<>();
        try{
            File file = new File(inputFile);
            FileInputStream is = null;
            is = new FileInputStream(file);
            while (true) {
                int c = is.read();
                if(c == -1){
                    break;
                }
                charList.add((char) c);
            }
        }catch (Exception e){
            e.printStackTrace();
        }



        LexicalAnalyser analyser = new LexicalAnalyser();

        ListIterator<Character> iterator = charList.listIterator();
        while(true){
            Optional<WordInterface> optional = analyser.analysis(iterator);
            if (optional.isPresent()){
                System.out.println(optional.get());
            }else{
                break;
            }
        }

    }
}