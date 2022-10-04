package type;

public abstract class VarType extends Type {
    public VarType(boolean isConst) {
        this.isConst = isConst;
    }

    private final boolean isConst;
    public boolean isConst() {
        return isConst;
    }

    public abstract int getDimension();
    public int getConstValue(){
        throw new UnsupportedOperationException();
    }
    public int[] getConstValue1(){
        throw new UnsupportedOperationException();
    }
    public int[][] getConstValue2(){
        throw new UnsupportedOperationException();
    }
    public int getSecondLen(){
        throw new UnsupportedOperationException();
    }
}
