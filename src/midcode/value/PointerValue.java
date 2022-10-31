package midcode.value;

public class PointerValue extends AddressValue{
    public PointerValue(String name, int staticOffset, RValue offset) {
        super(name, staticOffset, offset);
    }
}
