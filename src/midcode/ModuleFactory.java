package midcode;

public class ModuleFactory {
    private Module module;
    public void newModule(){
        module = new Module();
    }
    public void setStaticData(int[] staticData){
        module.staticData = staticData;
    }
    public void addFunction(Function function){
        module.functions.add(function);
    }
    public void setMain(Function function){
        module.mainFunc = function;
    }
    public Module done(){
        assert module != null;
        assert module.staticData != null;
        assert module.mainFunc != null;
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
