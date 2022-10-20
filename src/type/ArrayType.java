package type;

import java.util.Optional;

public class ArrayType extends VarType {
    private final int[] constValue;
    private final Integer secondLen;

    public ArrayType(int[] constValue) {
        super(constValue.length);
        this.constValue = constValue;
        this.secondLen = null;
    }
    public ArrayType(int[] constValue, int secondLen) {
        super(constValue.length);
        this.constValue = constValue;
        this.secondLen = secondLen;
    }

    public ArrayType(int size) {
        super(size);
        this.constValue = null;
        this.secondLen = null;
    }

    public ArrayType(int size, int secondLen) {
        super(size);
        this.constValue = null;
        this.secondLen = secondLen;
    }

    public Optional<int[]> getConstValue(){
        return Optional.ofNullable(constValue);
    }

    public Optional<Integer> getSecondLen() {
        return Optional.ofNullable(secondLen);
    }
}
