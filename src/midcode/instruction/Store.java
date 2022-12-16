package midcode.instruction;

import midcode.value.AddressValue;
import midcode.value.RValue;

/**
 * 将left的值看作一个地址，将right的值传到该地址中
 */
public class Store implements Sequence {
    private AddressValue left;
    private RValue right;

    public Store(AddressValue left, RValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String print() {
        return left.print() + " *= " + right.print();
    }

    public AddressValue getLeft() {
        return left;
    }

    public RValue getRight() {
        return right;
    }
}
