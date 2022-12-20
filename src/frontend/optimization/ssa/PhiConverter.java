package frontend.optimization.ssa;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.Function;
import midcode.instruction.Assignment;
import midcode.instruction.CondGoto;
import midcode.instruction.Goto;
import midcode.instruction.Sequence;
import midcode.util.BasicBlockFactory;
import midcode.value.Variable;

import java.util.*;

/**
 * 将具有phi的CFG转换为没有phi的CFG
 * 前提：
 * 1、entry没有phi
 * 2、所有的phi都在一个基本快的前面
 */
public class PhiConverter {
    public PhiConverter(Function function){
        basicBlocks = function.getOtherBasicBlocks();
    }
    private final Set<BasicBlock> basicBlocks;
    public void execute(){
        basicBlocks.forEach(basicBlock -> {
            Iterator<Sequence> iterator = basicBlock.getSequenceList().iterator();
            Map<BasicBlock, Set<Assignment>> predecessorVariableMap = new HashMap<>();
            while (iterator.hasNext()){
                Sequence sequence = iterator.next();
                if(!(sequence instanceof PhiAssignment))break;
                PhiAssignment phiAssignment = (PhiAssignment) sequence;
                Variable left = phiAssignment.getLeft();
                phiAssignment.getPhi().getParametersMap().forEach(((variable, predecessor) -> {
                    Assignment assignment = new Assignment(left, variable);
                    if(!predecessorVariableMap.containsKey(predecessor)){
                        predecessorVariableMap.put(predecessor, new HashSet<>());
                    }
                    predecessorVariableMap.get(predecessor).add(assignment);
                }));
                iterator.remove();
            }
            predecessorVariableMap.forEach((predecessor, assignments) -> {
                if(predecessor.getJump() instanceof Goto){
                    assignments.forEach(assignment -> predecessor.getSequenceList().add(assignment));
                }else{
                    assert predecessor.getJump() instanceof CondGoto;
                    CondGoto condGoto = (CondGoto) predecessor.getJump();
                    BasicBlock newBasicBlock = BasicBlockFactory.newBasicBlock();
                    Goto newGoto = new Goto();
                    newGoto.setBasicBlock(basicBlock);
                    assignments.forEach(assignment -> newBasicBlock.getInstructionList().add(assignment));
                    if(condGoto.getTrueBasicBlock().equals(basicBlock)){
                        condGoto.setTrueBasicBlock(newBasicBlock);
                    }else if(condGoto.getFalseBasicBlock().equals(basicBlock)){
                        condGoto.setFalseBasicBlock(newBasicBlock);
                    }else{
                        throw new SemanticException();
                    }
                    newBasicBlock.setLastJump(newGoto);
                }
            });

        });
    }
}
