package type;

import java.util.Optional;

public class IntType extends VarType {
    private final Integer constValue;

    public Optional<Integer> getConstValue() {
        return Optional.ofNullable(constValue);
    }

    public IntType(int constValue) {
        this.constValue = constValue;
    }

    public IntType() {
        this.constValue = null;
    }


}
