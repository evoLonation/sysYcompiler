package midcode.instruction;

import midcode.value.LValue;

public class GetInt implements Sequence{
    private final LValue lValue;

    public GetInt(LValue lValue) {
        this.lValue = lValue;
    }

    @Override
    public String print() {
        return lValue.print() + " = getint";
    }

    public LValue getlValue() {
        return lValue;
    }
}
