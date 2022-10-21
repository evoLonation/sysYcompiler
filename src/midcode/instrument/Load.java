package midcode.instrument;

import midcode.value.LValue;
import midcode.value.PointerValue;
import midcode.value.RValue;

/**
 * 将right的值看做一个地址，把地址对应的值传给left
 */
public class Load implements Instrument{
    private LValue left;
    private PointerValue right;

    public Load(LValue left, PointerValue right) {
        this.left = left;
        this.right = right;
    }
}
