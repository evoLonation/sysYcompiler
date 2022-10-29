package frontend.type;

import java.util.ArrayList;
import java.util.List;

public class FuncType{
    List<VarType> params = new ArrayList<>();
    boolean isReturn;

    FuncType(boolean isReturn) {
        this.isReturn = isReturn;
    }

    void addParam(VarType type){
        this.params.add(type);
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
