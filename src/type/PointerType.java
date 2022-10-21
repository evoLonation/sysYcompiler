package type;

import java.util.Optional;

public class PointerType extends VarType{
    protected final Integer secondLen;
    public PointerType() {
        this.secondLen = null;
    }
    public PointerType(int secondLen) {
        this.secondLen = secondLen;
    }
    public Optional<Integer> getSecondLen() {
        return Optional.ofNullable(secondLen);
    }
}
