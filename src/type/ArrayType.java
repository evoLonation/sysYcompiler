package type;

import java.util.Optional;

public class ArrayType extends PointerType {
    private final int size;

    public ArrayType(int size) {
        super();
        this.size = size;
    }

    public ArrayType(int size, int secondLen) {
        super(secondLen);
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
