package frontend.IRGenerate.util;

import midcode.Function;
import midcode.Module;

public class ModuleFactory {
    private Module module;
    public void newModule(){
        module = new Module();
    }
    public void setStaticData(int[] staticData){
        module.setStaticData(staticData);
    }
    public void addFunction(Function function){
        module.getOtherFunctions().add(function);
    }
    public void setMain(Function function){
        module.setMainFunc(function);
    }
    public Module done(){
        assert module != null;
        assert module.getStaticData() != null;
        assert module.getMainFunc() != null;
        Module module = this.module;
        this.module = null;
        return module;
    }


    private ModuleFactory() {}
    static private final ModuleFactory instance = new ModuleFactory();
    static public ModuleFactory getInstance(){
        return instance;
    }
}
