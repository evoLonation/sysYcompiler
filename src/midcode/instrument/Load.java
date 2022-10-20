package midcode.instrument;

import midcode.value.LValue;
import midcode.value.RValue;

/**
 * 将right的值看做一个地址，把地址对应的值传给left
 */
public class Load implements Instrument{
    private LValue left;
    // 理应是一个指针
    private RValue right;
}
