package midcode.value;

public class ArrayValue extends AddressValue{
    private final boolean isGlobal;

    public ArrayValue(String name, int staticOffset, RValue offset, boolean isGlobal) {
        super(name, staticOffset, offset);
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
