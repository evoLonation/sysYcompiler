package midcode.instruction;

import midcode.value.LValue;
import midcode.value.AddressValue;

/**
 * 将right的值看做一个地址，把地址对应的值传给left
 */
public class Load implements Sequence {
    private LValue left;
    private AddressValue right;

    public Load(LValue left, AddressValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String print() {
        return left.print() + " =* " + right.print();
    }

    public LValue getLeft() {
        return left;
    }

    public AddressValue getRight() {
        return right;
    }
}
