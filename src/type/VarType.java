package type;


public abstract class VarType{
    private final int size;

    public VarType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
