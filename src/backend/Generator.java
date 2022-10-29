package backend;

import midcode.BasicBlock;
import midcode.Module;

public class Generator {
    MipsSegment mipsSegment;
    Module module;
    public Generator(Module module, MipsSegment mipsSegment){
        this.module = module;
        this.mipsSegment = mipsSegment;
    }

    public void generate(){
        BasicBlock basicBlock = module.getMainFunc().getEntry();
        LocalActive localActive = new LocalActive(basicBlock);
        RegisterAllocator registerAllocator = new RegisterAllocator(basicBlock, localActive, mipsSegment);
        new BasicBlockGenerator(basicBlock, mipsSegment, registerAllocator).generate();
    }
}
