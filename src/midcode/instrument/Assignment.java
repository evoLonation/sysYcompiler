package midcode.instrument;

import midcode.value.LValue;
import midcode.value.RValue;

public class Assignment implements Instrument{
    private LValue left;
    private RValue right;

    public Assignment(LValue left, RValue right) {
        this.left = left;
        this.right = right;
    }
}
