package type;

import java.util.List;

public class FuncType extends Type {
    List<VarType> params;
    boolean isReturn;

    public FuncType(boolean isReturn, List<VarType> argList) {
        this.isReturn = isReturn;
        this.params = argList;
        this.gType = GenericType.FUNC;
    }

    public List<VarType> getParams() {
        return params;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public int getParamNumber() {
        return params.size();
    }

}
