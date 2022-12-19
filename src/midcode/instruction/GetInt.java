package midcode.instruction;

import midcode.value.LValue;

public class GetInt implements Sequence{
    private LValue left;

    public GetInt(LValue left) {
        this.left = left;
    }

    @Override
    public String print() {
        return left.print() + " = getint";
    }

    public LValue getLeft() {
        return left;
    }

    public void setLeft(LValue left) {
        this.left = left;
    }
}
