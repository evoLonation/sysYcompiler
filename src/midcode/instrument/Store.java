package midcode.instrument;

import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Variable;

/**
 * 将left的值看作一个地址，将right的值传到该地址中
 */
public class Store implements Instrument{
    private RValue left;
    private RValue right;
}
