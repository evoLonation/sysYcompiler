package lexer;

import java.util.ArrayList;
import java.util.List;

public class FormatString extends Terminal{

    private List<Char> charList = new ArrayList<>();
    public FormatString(List<Char> charList, int lineno) {
        super(TerminalType.STRCON, "" , lineno);
        StringBuilder builder = new StringBuilder("\"");
        for(Char c: charList){
            builder.append(c);
        }
        value = builder.append("\"").toString();
        this.charList = charList;
    }
    public List<Char> getCharList() {
        return charList;
    }
}
interface Char{

}
class FormatChar implements Char{
    @Override
    public String toString() {
        return "%d";
    }
}
class NormalChar implements Char{
    private final char value;

    public char getValue() {
        return value;
    }

    public NormalChar(char value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if(value == '\n'){
            return "\\n";
        }
        return Character.toString(value);
    }
}
