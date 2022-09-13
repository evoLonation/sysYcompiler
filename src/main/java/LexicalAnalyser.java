import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

abstract class Word {
    protected String type;

    abstract public String getType();

    @Override
    public String toString() {
        return "type : " + getType();
    }
}

abstract class ValueWord<T> extends Word {
    protected T value;

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " , value : " + getValue();
    }
}
class IntSY extends ValueWord<Integer>{
    public IntSY(int num){
        value = num;
    }

    @Override
    public String getType() {
        return "INTSY";
    }
}

class ColonSY extends Word{
    @Override
    public String getType() {
        return "COLONSY";
    }
}
class PluSY extends Word{
    @Override
    public String getType() {
        return "PLUSSY";
    }
}
class StarSY extends Word{
    @Override
    public String getType() {
        return "STARSY";
    }
}
class ComSY extends Word{
    @Override
    public String getType() {
        return "COMSY";
    }
}
class LparSY extends Word{
    @Override
    public String getType() {
        return "LPARSY";
    }
}
class RparSY extends Word{
    @Override
    public String getType() {
        return "RPARSY";
    }
}
class AssignSY extends Word{
    @Override
    public String getType() {
        return "ASSIGNSY";
    }
}
class BeginSY extends Word{
    @Override
    public String getType() {
        return "BEGINSY";
    }
}
class EndSY extends Word{
    @Override
    public String getType() {
        return "ENDSY";
    }
}
class ForSY extends Word{
    @Override
    public String getType() {
        return "FORSY";
    }
}
class DoSY extends Word{
    @Override
    public String getType() {
        return "DOSY";
    }
}
class IfSY extends Word{
    @Override
    public String getType() {
        return "IFSY";
    }
}
class ElseSY extends Word{
    @Override
    public String getType() {
        return "ELSESY";
    }
}
class IdSY extends ValueWord<String>{
    public IdSY(String ident){
        value = ident;
    }
    @Override
    public String getType() {
        return "IDSY";
    }
}

class LexicalAnalyser {
    public Optional<Word> analysis(ListIterator<Character> iterator) {
        AtomicReference<Optional<Word>> ret = new AtomicReference<>();
        ret.set(Optional.empty());
        getNextChar(iterator).ifPresent((Character c) -> {
            if (Character.isLetter(c)) {
                StringBuilder lexeme = new StringBuilder(c.toString());
                while (iterator.hasNext()) {
                    c = iterator.next();
                    if (Character.isLetter(c) || Character.isDigit(c)) {
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
                ret.set(Optional.of(new IntSY(Integer.parseInt(digits.toString()))));
            } else{
                switch (c){
                    case ':':
                        if(iterator.next() == '='){
                            ret.set(Optional.of(new AssignSY()));
                        }else{
                            iterator.previous();
                            ret.set(Optional.of(new ColonSY()));
                        }
                        break;
                    case '+':
                        ret.set(Optional.of(new PluSY()));
                        break;
                    case '*':
                        ret.set(Optional.of(new StarSY()));
                        break;
                    case ',':
                        ret.set(Optional.of(new ComSY()));
                        break;
                    case '(':
                        ret.set(Optional.of(new LparSY()));
                        break;
                    case ')':
                        ret.set(Optional.of(new RparSY()));
                        break;
                }
            }
        });
        return ret.get();
    }

    // »ñÈ¡·Ç¿Õ×Ö·û
    private Optional<Character> getNextChar(Iterator<Character> iterator) {
        while (iterator.hasNext()) {
            char c = iterator.next();
            if (c == ' ' || c == '\n') {
                continue;
            }
            return Optional.of(c);
        }
        return Optional.empty();
    }

    private Word getByLexeme(String lex) {
        switch (lex){
            case "BEGIN" : return new BeginSY();
            case "END" : return new EndSY();
            case "FOR" : return new ForSY();
            case "DO" : return new DoSY();
            case "IF" : return new IfSY();
            case "ELSE" : return new ElseSY();
            default: return new IdSY(lex);
        }
    }
}
class Test{
    public static void main(String[] args) {
        LexicalAnalyser analyser = new LexicalAnalyser();
        String str = "BEGIN FOR( IF(cadfa) (a+3 b:=123*456) : giao ELSE  )END";
        List<Character> charList = new ArrayList<>();
        for (char c : str.toCharArray()) {
            charList.add(c);
        }
        ListIterator<Character> iterator = charList.listIterator();
        while(true){
            Optional<Word> optional = analyser.analysis(iterator);
            if (optional.isPresent()){
                System.out.println(optional.get());
            }else{
                break;
            }
        }
    }
}