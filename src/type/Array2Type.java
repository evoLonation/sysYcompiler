package type;

public class Array2Type extends VarType{
    private final int secondLen;
    private final int[][] constValue;
    private Array2Type(boolean isConst, int[][] constValue, int secondLen) {
        super(isConst);
        this.secondLen = secondLen;
        this.constValue = constValue;
        this.gType = GenericType.ARRAY2;
    }

    @Override
    public int getSecondLen() {
        return secondLen;
    }

    public Array2Type(int secondLen , int[][] constValue) {
        this(true, constValue, secondLen);
    }

    public Array2Type(int secondLen) {
        this(false, null, secondLen);
    }
    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public int[][] getConstValue2() {
        return constValue;
    }
}
