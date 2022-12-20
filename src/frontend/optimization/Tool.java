package frontend.optimization;

import common.ValueGetter;
import midcode.BasicBlock;
import midcode.Function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tool {
    static ValueGetter valueGetter = ValueGetter.getInstance();

    static public Map<BasicBlock, Set<BasicBlock>> getPredecessorMap(Set<BasicBlock> basicBlocks){
        Map<BasicBlock, Set<BasicBlock>> ret = new HashMap<>();
        basicBlocks.forEach(basicBlock -> ret.put(basicBlock, new HashSet<>()));
        basicBlocks.forEach(basicBlock -> {
            Set<BasicBlock> succeeds = valueGetter.getJumpBasicBlock(basicBlock.getJump());
            succeeds.forEach(succeed -> ret.get(succeed).add(basicBlock));
        });
        return ret;
    }
}
