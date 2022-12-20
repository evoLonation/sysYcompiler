package midcode.value;

public class GlobalVariable extends LValue{
    private final int offset;

    public GlobalVariable(String name, int offset) {
        super(name);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
