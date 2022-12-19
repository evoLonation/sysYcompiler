package backend;

import midcode.Function;
import midcode.Module;

public class Generator {
    private final StringRepo stringRepo = StringRepo.getInstance();
    private final MipsSegment mipsSegment = MipsSegment.getInstance();

    Module module;

    public Generator(Module module){
        this.module = module;
    }

    public String generate(){
        mipsSegment.word("static_data", module.getStaticData());
        generateFunction(module.getMainFunc(), true);
        module.getOtherFunctions().forEach(function ->  generateFunction(function, false));
        stringRepo.print();
        return mipsSegment.print();
    }

    void generateFunction(Function function, boolean isMain){
        new BasicBlockGenerator(function.getEntry(), function.getOffset(), isMain, true).generate();
        function.getOtherBasicBlocks().forEach(basicBlock -> new BasicBlockGenerator(basicBlock, function.getOffset(), isMain, false).generate());
    }
}
