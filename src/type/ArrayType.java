package type;

import java.util.Optional;

public class ArrayType extends PointerType {
    private final int size;

    public ArrayType(int firstLen) {
        super();
        this.size = firstLen;
    }

    public ArrayType(int firstLen, int secondLen) {
        super(secondLen);
        this.size = firstLen * secondLen;
    }

    public int getSize() {
        return size;
    }
}
