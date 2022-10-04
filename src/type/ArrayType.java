package type;

public class ArrayType extends VarType{
    private final int[] constValue;

    private ArrayType(boolean isConst, int[] constValue) {
        super(isConst);
        this.constValue = constValue;
        this.gType = GenericType.ARRAY;
    }

    public ArrayType(int[] constValue) {
        this(true, constValue);
    }

    public ArrayType() {
        this(false, null);
    }
    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public int[] getConstValue1() {
        return constValue;
    }
}
