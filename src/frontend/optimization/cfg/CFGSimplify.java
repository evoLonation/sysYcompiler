package frontend.optimization.cfg;

import frontend.optimization.Tool;
import midcode.BasicBlock;
import midcode.Function;
import midcode.instruction.CondGoto;
import midcode.instruction.Goto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 简化控制流图
 * 其二是简化控制流图
 * 其一是去掉多余的基本块（没有前驱的）
 */
public class CFGSimplify {
    private final BasicBlock entry;
    private final Set<BasicBlock> otherBasicBlocks;
    public CFGSimplify(Function function){
        this.entry = function.getEntry();
        this.otherBasicBlocks = function.getOtherBasicBlocks();
    }
    public void exec(){
        Set<BasicBlock> allBasicBlock = Stream.concat(Stream.of(entry), otherBasicBlocks.stream()).collect(Collectors.toSet());
        allBasicBlock.forEach(basicBlock -> {
            boolean changed = true;
            while (changed){
                changed = false;
                if(basicBlock.getJump() instanceof Goto){
                    BasicBlock successor = ((Goto) basicBlock.getJump()).getBasicBlock();
                    if(successor.getSequenceList().isEmpty()){
                        basicBlock.setLastJump(successor.getJump());
                        changed = true;
                    }
                }else if(basicBlock.getJump() instanceof CondGoto){
                    CondGoto condGoto = (CondGoto) basicBlock.getJump();
                    BasicBlock successor = condGoto.getTrueBasicBlock();
                    if(successor.getSequenceList().isEmpty() && successor.getJump() instanceof Goto){
                        condGoto.setTrueBasicBlock(((Goto) successor.getJump()).getBasicBlock());
                        changed = true;
                    }
                    successor = condGoto.getFalseBasicBlock();
                    if(successor.getSequenceList().isEmpty() && successor.getJump() instanceof Goto){
                        condGoto.setFalseBasicBlock(((Goto) successor.getJump()).getBasicBlock());
                        changed = true;
                    }
                }

            }
        });
        Map<BasicBlock, Set<BasicBlock>> predecessorMap = Tool.getPredecessorMap(allBasicBlock);
        predecessorMap.forEach((key, value) -> {
            if(value.isEmpty() && !key.equals(entry)){
                otherBasicBlocks.remove(key);
            }
        });
    }

}
