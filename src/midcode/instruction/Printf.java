//package midcode.instruction;
//
//import frontend.lexer.FormatString;
//import midcode.value.RValue;
//
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//
///**
// * "start %d str %d str ... %d end"
// */
//public class Printf implements Instrument{
//
//    public static class FormatString{}
//
//    public static class NormalString extends FormatString{
//        private final String string;
//        NormalString(String string) {
//            this.string = string;
//        }
//        String getString() {
//            return string;
//        }
//    }
//    public static class NumberString extends FormatString{
//        private final RValue rValue;
//        NumberString(RValue rValue) {
//            this.rValue = rValue;
//        }
//        RValue getRValue() {
//            return rValue;
//        }
//    }
//
//
//    private final List<FormatString> formatStrings;
//
//    public Printf(frontend.lexer.FormatString formatString, List<RValue> rValues) {
//        formatStrings = new LinkedList<>();
//        Iterator<RValue> iterator = rValues.iterator();
//        StringBuilder str = null;
//        for(frontend.lexer.FormatString.Char c : formatString.getCharList()){
//            if(c instanceof frontend.lexer.FormatString.NormalChar){
//                if(str != null){
//                    str.append(c);
//                }else{
//                    str = new StringBuilder(String.valueOf(c));
//                }
//            }else{
//                if(str != null){
//                    formatStrings.add(new NormalString(str.toString()));
//                    str = null;
//                }
//                formatStrings.add(new NumberString(iterator.next()));
//            }
//        }
//    }
//
//    @Override
//    public String print() {
//        StringBuilder ret = new StringBuilder("printf \" ");
//        for(FormatString formatString : formatStrings){
//            if(formatString instanceof NormalString){
//                ret.append(((NormalString) formatString).string);
//            }else if(formatString instanceof NumberString){
//                ret.append("{{").append(((NumberString) formatString).rValue).append("}}");
//            }
//        }
//        return ret.toString();
//    }
//
//    public List<FormatString> getFormatStrings() {
//        return formatStrings;
//    }
//}
