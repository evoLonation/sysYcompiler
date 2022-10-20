package type;

import java.util.Optional;

public class PointerType extends VarType{
    private final Integer secondLen;
    public PointerType() {
        super(1);
        this.secondLen = null;
    }
    public PointerType(int secondLen) {
        super(1);
        this.secondLen = secondLen;
    }
    public Optional<Integer> getSecondLen() {
        return Optional.ofNullable(secondLen);
    }
}
