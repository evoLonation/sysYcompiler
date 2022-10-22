package midcode.instrument;

import lexer.FormatString;
import midcode.value.RValue;

import java.util.List;

public class Printf implements Instrument{
    private final FormatString formatString;
    private final List<RValue> rValues;

    public Printf(FormatString formatString, List<RValue> rValues) {
        this.formatString = formatString;
        this.rValues = rValues;
        assert formatString.getFormatCharNumber() == rValues.size();
    }

    @Override
    public String print() {
        StringBuilder ret = new StringBuilder("print \"" + formatString.getValue() + "\" ");
        for(RValue rValue : rValues){
            ret.append(",").append(rValue.print());
        }
        return ret.toString();
    }
}
