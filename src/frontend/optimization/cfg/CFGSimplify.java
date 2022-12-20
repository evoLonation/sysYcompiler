package frontend.optimization.cfg;

import common.SemanticException;
import frontend.optimization.Tool;
import midcode.BasicBlock;
import midcode.Function;
import midcode.instruction.CondGoto;
import midcode.instruction.Goto;
import midcode.instruction.Jump;
import midcode.instruction.Return;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private Jump cloneJump(Jump jump){
        if(jump instanceof Goto){
            return new Goto(((Goto) jump).getBasicBlock());
        }else if(jump instanceof CondGoto){
            return new CondGoto(((CondGoto) jump).getTrueBasicBlock(), ((CondGoto) jump).getFalseBasicBlock(), ((CondGoto) jump).getCond());
        }else if(jump instanceof Return){
            return new Return(((Return) jump).getReturnValue().orElse(null));
        }
        throw new SemanticException();
    }

    private Set<BasicBlock> computeAllBassicBlock(){
        return Stream.concat(Stream.of(entry), otherBasicBlocks.stream()).collect(Collectors.toSet());
    }

    public void exec(){
        computeAllBassicBlock().forEach(basicBlock -> {
            // todo 如果有诸如while(1)的代码会死循环
            int count = 0;
            boolean changed = true;
            while (changed && count < 100){
                count++;
                changed = false;
                if(basicBlock.getJump() instanceof Goto){
                    BasicBlock successor = ((Goto) basicBlock.getJump()).getBasicBlock();
                    if(successor.getSequenceList().isEmpty()){
                        basicBlock.setLastJump(cloneJump(successor.getJump()));
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
        AtomicBoolean changed = new AtomicBoolean(true);
        while(changed.get()){
            changed.set(false);
            Map<BasicBlock, Set<BasicBlock>> predecessorMap = Tool.getPredecessorMap(computeAllBassicBlock());
            predecessorMap.forEach((key, value) -> {
                if(value.isEmpty() && !key.equals(entry)){
                    otherBasicBlocks.remove(key);
                    changed.set(true);
                }
            });
        }

    }

}
