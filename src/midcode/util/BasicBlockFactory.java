package midcode.util;

import frontend.IRGenerate.util.BasicBlockManager;
import midcode.BasicBlock;

public class BasicBlockFactory {
    private static int basicBlockNumber = 0;

    public static BasicBlock newBasicBlock(){
        return  new BasicBlock("basicBlock$" + ++basicBlockNumber);
    }
    public static BasicBlock newEntryBasicBlock(String name){
        return new BasicBlock("function$" + name);
    }

}
