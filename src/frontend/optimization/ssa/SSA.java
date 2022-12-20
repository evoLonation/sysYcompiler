package frontend.optimization.ssa;

import common.ValueGetter;
import common.SemanticException;
import midcode.BasicBlock;
import midcode.Function;
import midcode.value.GlobalVariable;
import midcode.value.LValue;
import frontend.IRGenerate.util.ValueFactory;
import midcode.value.Variable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SSA {
    public SSA(Function function) {
        this.entry = function.getEntry();
        this.otherBasicBlocks = function.getOtherBasicBlocks();
        this.allBasicBlock = Stream.concat(otherBasicBlocks.stream(), Stream.of(entry)).collect(Collectors.toSet());
    }

    private final BasicBlock entry;
    private final Set<BasicBlock> otherBasicBlocks;
    private final Set<BasicBlock> allBasicBlock;

    private final ValueGetter valueGetter = ValueGetter.getInstance();


    public void execute() {
        computePredecessor();
        computeDominatorTree();
        computeDominanceFrontier();
        generatePhi();
        rename();
        insertPhi();
    }

    private Set<BasicBlock> getAllBasicBlock(){
        return allBasicBlock;
    }
    private BasicBlock getEntry(){
        return entry;
    }
    private Set<BasicBlock> getOtherBasicBlock(){
        return otherBasicBlocks;
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
    private final Map<BasicBlock, Set<BasicBlock>> dominatorTreeSuccessorMap = new HashMap<>();


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

    private final Set<BasicBlock> visitedSet = new HashSet<>();
    private void dfs(BasicBlock basicBlock, BasicBlock father){
        if(visitedSet.contains(basicBlock))return;
        visitedSet.add(basicBlock);
        if(father != null){
            iDOMMap.put(basicBlock, father);
        }
        valueGetter.getJumpBasicBlock(basicBlock.getJump()).forEach(successor -> {
            dfs(successor, basicBlock);
        });

    }

    private void computeDominatorTree(){
        //先构建出一个生成树
        //使用DFS构建生成树
        dfs(entry, null);
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
        getAllBasicBlock().forEach(basicBlock -> {
            dominatorTreeSuccessorMap.put(basicBlock, new HashSet<>());
        });
        iDOMMap.forEach((basicBlock, predecessor) -> {
            dominatorTreeSuccessorMap.get(predecessor).add(basicBlock);
        });
    }

    //只能用于非entry
    private BasicBlock getIDOM(BasicBlock basicBlock){
        assert !basicBlock.equals(getEntry());
        return iDOMMap.get(basicBlock);
    }
    private Set<BasicBlock> getDOMSuccessor(BasicBlock basicBlock){
        return dominatorTreeSuccessorMap.get(basicBlock);
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
    //todo blockMap中可能没有整个程序的全局变量
    private final Map<Variable, Set<BasicBlock>> blockMap = new HashMap<>();
    private final Map<BasicBlock, Set<PhiAssignment>> phiMap = new HashMap<>();

    private void generatePhi(){

        getAllBasicBlock().forEach(basicBlock ->{
            Set<LValue> varKillMap = new HashSet<>();
            basicBlock.getInstructionList().forEach(instruction -> {
                valueGetter.getLValueUseValues(instruction).forEach(lValue -> {
                    if(!varKillMap.contains(lValue)){
                        assert lValue instanceof Variable || lValue instanceof GlobalVariable;
                        if(lValue instanceof Variable){
                            globals.add((Variable) lValue);
                        }
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
    private final Map<Variable, Variable> originVariableMap = new HashMap<>();

    //variable必须是未SSA之前的
    public Variable newSubscriptVariable(Variable variable){
        int count = numberMap.get(variable);
        numberMap.put(variable, count + 1);
        variableStackMap.get(variable).push(count);
        return specifySubscriptVariable(variable, count);
    }
    private Variable specifySubscriptVariable(Variable variable, int count){
        Variable ret;
        ret = new Variable(variable.getName() + "@" + count, variable.getFunction(), variable.getOffset());
        originVariableMap.put(ret, variable);
        return ret;
    }

    //如果本身就是为加下标的则直接返回自身
    private Variable getOrigin(Variable variable){
        return originVariableMap.getOrDefault(variable, variable);
    }

    //该局部变量在当前上下文是否被定义
    private boolean isDefVariable(Variable variable){
        return !variableStackMap.get(variable).empty();
    }
    //variable必须是未SSA之前的
    public Variable getTopVariable(Variable variable){
        return specifySubscriptVariable(variable, variableStackMap.get(variable).peek());
    }
    public void popStack(Variable variable){
        variableStackMap.get(variable).pop();
    }




    private void rename(){
        //初始化数据结构
        getAllBasicBlock().forEach(basicBlock -> {
            basicBlock.getInstructionList().forEach(instruction -> {
                valueGetter.getAllValues(instruction).forEach(lValue -> {
                    if(lValue instanceof Variable){
                        numberMap.put((Variable) lValue, 0);
                        variableStackMap.put((Variable) lValue, new Stack<>());
                    }
                });
            });
        });
        //开始递归
        rename(getEntry());
    }

    private void rename(BasicBlock basicBlock){
        List<Variable> popList = new ArrayList<>();
        phiMap.get(basicBlock).forEach(phiAssignment -> {
            popList.add(phiAssignment.getLeft());
            phiAssignment.setLeft(newSubscriptVariable(phiAssignment.getLeft()));
        });
        basicBlock.getInstructionList().forEach(instruction -> {
            valueGetter.getUseGetterSetter(instruction).forEach(gs -> {
                if(gs.get() instanceof Variable){
                    gs.set(getTopVariable((Variable) gs.get()));
                }
            });
            valueGetter.getDefGetterSetter(instruction).ifPresent(gs -> {
                if(gs.get() instanceof Variable){
                    popList.add((Variable) gs.get());
                    gs.set(newSubscriptVariable((Variable) gs.get()));
                }
            });
        });
        valueGetter.getJumpBasicBlock(basicBlock.getJump()).forEach(successor -> {
            phiMap.get(successor).forEach(phiAssignment -> {
                Phi phi = phiAssignment.getPhi();
                Variable variable = getOrigin(phiAssignment.getLeft());
                if(isDefVariable(variable)){
                    phi.addParameter(getTopVariable(variable), basicBlock);
                }
            });
        });
        getDOMSuccessor(basicBlock).forEach(this::rename);
        popList.forEach(this::popStack);
    }

    private void insertPhi(){
        phiMap.forEach((b, phis) -> {
            phis.forEach(phiAssignment -> {
                b.getSequenceList().add(0, phiAssignment);
            });
        });
    }
}
