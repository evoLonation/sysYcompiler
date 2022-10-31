package backend;

import midcode.BasicBlock;
import midcode.Module;

public class Generator {
    static final boolean DEBUG = true;
    Module module;
    public Generator(Module module){
        this.module = module;
    }

    public String generate(){
        BasicBlock basicBlock = module.getMainFunc().getEntry();
        return new BasicBlockGenerator(basicBlock, module.getMainFunc().getOffset()).generate();

    }
}
