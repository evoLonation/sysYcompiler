package midcode.instrument;

import midcode.value.LValue;
import midcode.value.PointerValue;
import midcode.value.RValue;
import midcode.value.Variable;

/**
 * 将left的值看作一个地址，将right的值传到该地址中
 */
public class Store implements Instrument{
    private PointerValue left;
    private RValue right;

    public Store(PointerValue left, RValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String print() {
        return left.print() + " *= " + right.print();
    }

    public PointerValue getLeft() {
        return left;
    }

    public RValue getRight() {
        return right;
    }
}
