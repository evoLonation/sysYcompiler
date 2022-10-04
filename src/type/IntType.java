package type;

public class IntType extends VarType {
    private final int constValue;

    public int getConstValue() {
        return constValue;
    }

    private IntType(boolean isConst, int constValue) {
        super(isConst);
        this.constValue = constValue;
        this.gType = GenericType.INT;
    }

    public IntType(int constValue) {
        this(true, constValue);
    }

    public IntType() {
        this(false, 0);
    }

    public int getDimension() {
        return 0;
    }


}
