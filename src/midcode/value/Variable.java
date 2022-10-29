package midcode.value;

/**
 * offset: 假设此时的栈顶为sp(sp上面是本函数的地盘)，则内存位置为sp+offset
 */
public class Variable extends LValue{
    Variable(String name, boolean isGlobal, int offset) {
        super(name);
        this.isGlobal = isGlobal;
        this.offset = offset;
    }

    private final boolean isGlobal;
    private final int offset;

    public boolean isGlobal() {
        return isGlobal;
    }

    public int getOffset() {
        return offset;
    }

}
