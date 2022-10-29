package frontend.lexer;

import java.util.List;

public class FormatString extends Terminal{

    private final int  formatCharNumber;
    private final List<Char> charList;
    public FormatString(List<Char> charList, int lineno) {
        super(TerminalType.STRCON, "" , lineno);
        int number = 0;
        StringBuilder builder = new StringBuilder("\"");
        for(Char c: charList){
            if(c instanceof FormatChar){
                number ++;
            }
            builder.append(c);
        }
        value = builder.append("\"").toString();
        this.charList = charList;
        this.formatCharNumber = number;
    }

    public int getFormatCharNumber() {
        return formatCharNumber;
    }

    public List<Char> getCharList() {
        return charList;
    }

    public interface Char{

    }
    public static class FormatChar implements Char{
        @Override
        public String toString() {
            return "%d";
        }
    }
    public static class NormalChar implements Char{
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

}
