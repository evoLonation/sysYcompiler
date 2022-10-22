package midcode;

import java.util.List;

public class Module implements MidCode{
    public int[] staticData;
    public Function mainFunc;
    public List<Function> functions;


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
}
