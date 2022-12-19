package midcode;

import java.util.ArrayList;
import java.util.List;

public class Module implements MidCode{
     private int[] staticData;
     private Function mainFunc;
     private final List<Function> functions = new ArrayList<>();


    @Override
    public String print() {
        StringBuilder ret = new StringBuilder("staticData:\n");
        for(int i = 0; i < staticData.length; i++){
            ret.append(staticData[i]);
            if((i+1)%5 == 0){
                ret.append("\n");
            }
        }
        for(Function function : functions){
            ret.append("\n").append(function.print());
        }
        ret.append("\n").append(mainFunc.print());
        return ret.toString();
    }

    public int[] getStaticData() {
        return staticData;
    }

    public Function getMainFunc() {
        return mainFunc;
    }

    public List<Function> getOtherFunctions() {
        return functions;
    }

    public void setStaticData(int[] staticData) {
        this.staticData = staticData;
    }

    public void setMainFunc(Function mainFunc) {
        this.mainFunc = mainFunc;
    }
}
