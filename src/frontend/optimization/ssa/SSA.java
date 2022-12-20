package frontend.optimization.ssa;

import common.ValueGetter;
import common.SemanticException;
import midcode.BasicBlock;
import midcode.instruction.Assignment;
import midcode.instruction.Instruction;
import midcode.value.LValue;
import frontend.IRGenerate.util.ValueFactory;
import midcode.value.Variable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SSA {
    /**
     * @param entry 起始基本块
     * @param basicBlocks 其余的基本块
     * 前两个参数共同构成一个控制流图
     */
    public SSA(BasicBlock entry, Set<BasicBlock> basicBlocks) {
        this.entry = entry;
        this.basicBlocks = basicBlocks;
        this.allBasicBlock = Stream.concat(basicBlocks.stream(), Stream.of(entry)).collect(Collectors.toSet());
    }

    private final BasicBlock entry;
    private final Set<BasicBlock> basicBlocks;
    private final Set<BasicBlock> allBasicBlock;

    private final ValueGetter valueGetter = ValueGetter.getInstance();
    private final ValueFactory valueFactory = ValueFactory.getInstance();

    private boolean isExecute;

    public void execute() {
        if(isExecute){
            throw new SemanticException();
        }
        isExecute = true;
        computePredecessor();
        computeDominatorTree();
        computeDominanceFrontier();
        insertPhi();
    }

    private Set<BasicBlock> getAllBasicBlock(){
        return allBasicBlock;
    }
    private BasicBlock getEntry(){
        return entry;
    }
    private Set<BasicBlock> getOtherBasicBlock(){
        return basicBlocks;
    }

    // 前驱节点计算
    private final HashMap<BasicBlock, Set<BasicBlock>> predecessorMap = new HashMap<>();

    private void computePredecessor(){
        getAllBasicBlock().forEach(basicBlock -> predecessorMap.put(basicBlock, new HashSet<>()));
        getAllBasicBlock().forEach(basicBlock -> {
            Set<BasicBlock> succeeds = valueGetter.getJumpBasicBlock(basicBlock.getJump());
            succeeds.forEach(succeed -> predecessorMap.get(succeed).add(basicBlock));
        });
    }

    private Set<BasicBlock> getPredecessors(BasicBlock basicBlock){
        return predecessorMap.get(basicBlock);
    }


    //支配性，对于基本块A，B在entry到A的所有路径上时，称B支配A
    private final Map<BasicBlock, Set<BasicBlock>> domMap = new HashMap<>();

    private void computeDOM(){
        domMap.put(getEntry(), Collections.singleton(getEntry()));
        getOtherBasicBlock().forEach(basicBlock -> domMap.put(basicBlock, getAllBasicBlock()));
        boolean changed = true;
        while(changed){
            changed = false;
            for(BasicBlock basicBlock : getOtherBasicBlock()){
                Set<BasicBlock> temp = new HashSet<>();
                temp.add(basicBlock);
                temp.addAll(getPredecessors(basicBlock).stream()
                        .map(domMap::get)
                        .reduce((union, set) ->  {union.retainAll(set); return union;}).orElse(Collections.emptySet()));
                if(!temp.equals(domMap.get(basicBlock))){
                    domMap.put(basicBlock, temp);
                    changed = true;
                }
            }
        }
    }


    //一个基本块的IDOM指他的严格支配节点集中与该基本快最接近的节点
    // key中不包括entry，因为entry没有idom
    private final Map<BasicBlock, BasicBlock> iDOMMap = new HashMap<>();


    private BasicBlock getNearestCommonAncestor(BasicBlock b1, BasicBlock b2){
        BasicBlock ancestor = b1;
        while(!isAncestor(b2, ancestor)){
            ancestor = iDOMMap.get(ancestor);
        }
        return ancestor;
    }

    //ancestor包括它自身
    private boolean isAncestor(BasicBlock basicBlock, BasicBlock ancestor){
        BasicBlock parent = basicBlock;
        while(parent != null && parent != ancestor){
            parent = iDOMMap.get(parent);
        }
        return parent != null;
    }

    private void computeDominatorTree(){
        //先构建出一个生成树
        //构建方式：只需要将前驱map拿来，每个节点的前驱删的就剩一个就行了
        predecessorMap.forEach((basicBlock, basicBlocks) -> {
            if (basicBlock.equals(getEntry())) return;
            iDOMMap.put(basicBlock, basicBlocks.stream().filter(b -> !b.equals(basicBlock)).findAny().orElseThrow(SemanticException::new));
        });
        AtomicBoolean changed = new AtomicBoolean(true);
        while (changed.get()) {
            changed.set(false);
            getOtherBasicBlock().forEach(v -> {
                getPredecessors(v).forEach(u -> {
                    BasicBlock nca = getNearestCommonAncestor(u, iDOMMap.get(v));
                    if(!iDOMMap.get(v).equals(u) && !iDOMMap.get(v).equals(nca)){
                        iDOMMap.put(v, nca);
                        changed.set(true);
                    }
                });
            });
        }
    }

    //只能用于非entry
    private BasicBlock getIDOM(BasicBlock basicBlock){
        assert !basicBlock.equals(getEntry());
        return iDOMMap.get(basicBlock);
    }


    // 每个基本块的df集合
    // 一个基本块的df集合指从该基本快可达但是不支配的第一个节点
    private final HashMap<BasicBlock, Set<BasicBlock>> DFMap = new HashMap<>();

    private void computeDominanceFrontier(){
        getAllBasicBlock().forEach(basicBlock -> DFMap.put(basicBlock, new HashSet<>()));
        getAllBasicBlock().forEach(basicBlock -> {
            Set<BasicBlock> predecessors = getPredecessors(basicBlock);
            if(predecessors.size() > 1){
                predecessors.forEach(predecessor -> {
                    BasicBlock runner = predecessor;
                    while(!runner.equals(getIDOM(basicBlock))){
                        DFMap.get(runner).add(basicBlock);
                        runner = getIDOM(runner);
                    }
                });
            }
        });
    }

    private Set<BasicBlock> getDominateFrontier(BasicBlock basicBlock){
        return DFMap.get(basicBlock);
    }


    //先求全局变量
    private final Set<Variable> globals = new HashSet<>();
    private final Map<Variable, Set<BasicBlock>> blockMap = new HashMap<>();
    private final Map<BasicBlock, Set<PhiAssignment>> phiMap = new HashMap<>();
    private Set<Variable> getGlobalVariables(){
        return globals;
    }

    private void insertPhi(){

        getAllBasicBlock().forEach(basicBlock ->{
            Set<LValue> varKillMap = new HashSet<>();
            basicBlock.getInstructionList().forEach(instruction -> {
                valueGetter.getLValueUseValues(instruction).forEach(lValue -> {
                    if(!varKillMap.contains(lValue)){
                        assert lValue instanceof Variable;
                        globals.add((Variable) lValue);
                    }
                });
                valueGetter.getDefValue(instruction).ifPresent(lValue -> {
                    varKillMap.add(lValue);
                    if(lValue instanceof Variable){
                        if(blockMap.get(lValue) == null){
                            blockMap.put((Variable) lValue, new HashSet<>(Collections.singleton(basicBlock)));
                        }else{
                            blockMap.get(lValue).add(basicBlock);
                        }
                    }
                });
            });
        });

        Map<BasicBlock, Set<Variable>> phiVariableMap = new HashMap<>();
        getAllBasicBlock().forEach(basicBlock -> {
            phiMap.put(basicBlock, new HashSet<>());
            phiVariableMap.put(basicBlock, new HashSet<>());
        });
        globals.forEach(x -> {
            Set<BasicBlock> workList = new HashSet<>(blockMap.get(x));
            Set<BasicBlock> temp1WorkList = new HashSet<>(workList);
            Set<BasicBlock> temp2WorkList = new HashSet<>();
            while(true){
                Set<BasicBlock> finalTemp2WorkList = temp2WorkList;
                temp1WorkList.forEach(b -> {
                    getDominateFrontier(b).forEach(d -> {
                        if(!phiVariableMap.get(d).contains(x)){
                            phiMap.get(d).add(new PhiAssignment(x, d));
                            phiVariableMap.get(d).add(x);
                            if(!workList.contains(d)){
                                finalTemp2WorkList.add(d);
                                workList.add(d);
                            }
                        }
                    });
                });
                if(temp2WorkList.isEmpty()){
                    break;
                }
                temp1WorkList = temp2WorkList;
                temp2WorkList = new HashSet<>();
            }

        });

    }

    private final Map<Variable, Integer> numberMap = new HashMap<>();
    private final Map<Variable, Stack<Integer>> variableStackMap = new HashMap<>();

    //variable必须是未SSA之前的
    public Variable newSubscriptVariable(Variable variable){
        int count = numberMap.get(variable);
        numberMap.put(variable, count + 1);
        variableStackMap.get(variable).push(count);
        return specifySubscriptVariable(variable, count);
    }
    private Variable specifySubscriptVariable(Variable variable, int count){
        if(variable.getFunction().isPresent()){
            return new Variable(variable.getName() + "@" + count, variable.getFunction().get(), variable.getOffset());
        }else{
            return new Variable(variable.getName() + "@" + count, variable.getOffset());
        }
    }

    //variable必须是未SSA之前的
    public Variable getTopVariable(Variable variable){
        return specifySubscriptVariable(variable, variableStackMap.get(variable).peek());
    }
    public void popStack(Variable variable){
        variableStackMap.get(variable).pop();
    }



    private void rename(){
        getGlobalVariables().forEach(variable -> {
            numberMap.put(variable, 0);
            variableStackMap.put(variable, new Stack<>());
        });
        rename(getEntry());
        getAllBasicBlock().forEach(basicBlock -> {
            phiMap.get(basicBlock).forEach(phiAssignment -> {
                basicBlock.getSequenceList().add(0, phiAssignment);
            });
        });
    }

    private void rename(BasicBlock basicBlock){
        phiMap.get(basicBlock).forEach(phiAssignment -> {
            phiAssignment.setLeft(newSubscriptVariable(phiAssignment.getLeft()));
        });
        basicBlock.getInstructionList().forEach(instruction -> {

            if(instruction instanceof Assignment && ((Assignment) instruction).getRight() instanceof Phi){

            }else{
                valueGetter.getDefValue(instruction).ifPresent(lValue -> {
                    if(lValue instanceof Variable){

                    }
                });
            }

        });
    }
}
