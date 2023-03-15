package midcode.instruction;

import midcode.value.LValue;

public class LoadParam implements Sequence{
    private LValue left;
    private final int offset;

    public LoadParam(LValue left, int offset) {
        this.left = left;
        this.offset = offset;
    }

    public LValue getLeft() {
        return left;
    }

    public int getOffset() {
        return offset;
    }

    public void setLeft(LValue left) {
        this.left = left;
    }

    @Override
    public String print() {
        return "load param " + left.print();
    }
}
