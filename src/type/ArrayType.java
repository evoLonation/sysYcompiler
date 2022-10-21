package type;

import java.util.Optional;

public class ArrayType extends PointerType {
    private final int[] constValue;
    private final int size;

    public ArrayType(int[] constValue) {
        super();
        this.constValue = constValue;
        this.size = constValue.length;
    }
    public ArrayType(int[] constValue, int secondLen) {
        super(secondLen);
        this.constValue = constValue;
        this.size = constValue.length;
    }

    public ArrayType(int size) {
        super();
        this.constValue = null;
        this.size = size;
    }

    public ArrayType(int size, int secondLen) {
        super(secondLen);
        this.constValue = null;
        this.size = size;
    }

    public Optional<int[]> getConstValue(){
        return Optional.ofNullable(constValue);
    }

    public int getSize() {
        return size;
    }
}
