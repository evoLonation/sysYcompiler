package backend;

import midcode.BasicBlock;
import midcode.MidCode;
import midcode.instruction.Instruction;
import midcode.instruction.Jump;
import midcode.instruction.Sequence;
import midcode.value.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 计算基本快中每个中间代码的每个lvalue的后继活跃信息
 * 假设基本块中的所有variable都可能会在基本快结束后活跃
  */
public class LocalActive {
    private final ValueGetter valueGetter = ValueGetter.getInstance();
    //value是key对应的activeinfo，每个instrument都会对应一个activeInfo，对应的是从该instrument找起第一个activeInfo
    private final Map<LValue, Map<Instruction, ActiveInfo>> activeInfosMap = new HashMap<>();
    private final Map<LValue, ActiveInfo> firstActiveInfos = new HashMap<>();
    private final Set<LValue> allValues;
    private final List<Sequence> sequences;
    private final Jump lastJump;
    private int nowIndex;
    private Sequence nowSequence;

    Sequence getNowSequence() throws NoSuchElementException {
        if(nowSequence == null)throw new NoSuchElementException();
        return nowSequence;
    }

    Jump getLastJump(){
        return lastJump;
    }

    Instruction getNowInstruction(){
        if(nowSequence == null) return lastJump;
        return nowSequence;
    }

    boolean hasNowSequence(){
        return nowSequence != null;
    }

    void next() {
        if(nowIndex >= sequences.size()){
            nowSequence = null;
        }else{
            nowSequence = sequences.get(nowIndex++);
        }
    }

    private static class OutInstruction implements Instruction {
        @Override
        public String print() {
            return "#outInstruction";
        }
    }

    LocalActive(BasicBlock basicBlock) {
        OutInstruction outInstruction = new OutInstruction();
        sequences = basicBlock.getSequenceList();
        lastJump = basicBlock.getJump();
        //假设基本块中的所有variable都可能会在基本快结束后活跃
        allValues = basicBlock.getInstructionList().stream().flatMap(midCode -> valueGetter.getAllValues(midCode).stream()).collect(Collectors.toSet());
        for(LValue value : allValues) {
            if(value instanceof Variable){
                ActiveInfo lastActiveInfo = new ActiveInfo(outInstruction, true, false);
                Map<Instruction, ActiveInfo> activeInfoMap = new HashMap<>();
                activeInfoMap.put(outInstruction, lastActiveInfo);
                activeInfosMap.put(value, activeInfoMap);
                firstActiveInfos.put(value, lastActiveInfo);
            }else {
                activeInfosMap.put(value, new HashMap<>());
            }
        }
        List<Instruction> instructionsWithOut = Stream.concat(basicBlock.getInstructionList().stream(), Stream.of(outInstruction)).collect(Collectors.toList());
        ListIterator<Instruction> listIterator = instructionsWithOut.listIterator(instructionsWithOut.size());
        while(listIterator.hasPrevious()){
            Instruction instruction = listIterator.previous();
            Map<LValue, ActiveInfo> nowActiveInfos = new HashMap<>();
            for(LValue value : valueGetter.getUseValues(instruction)){
                nowActiveInfos.put(value, new ActiveInfo(instruction, true, false));
            }
            valueGetter.getDefValue(instruction).ifPresent(value->{
                if(nowActiveInfos.containsKey(value)){
                    nowActiveInfos.get(value).isDef = true;
                }else{
                    nowActiveInfos.put(value, new ActiveInfo(instruction, false, true));
                }
            });

            for(Map.Entry<LValue, ActiveInfo> entry : nowActiveInfos.entrySet()){
                LValue value = entry.getKey();
                ActiveInfo nowActiveInfo = entry.getValue();
                if(firstActiveInfos.containsKey(value)){
                    nowActiveInfo.next = firstActiveInfos.get(value);
                }
                firstActiveInfos.put(value, nowActiveInfo);
            }
            for(LValue value : allValues){
                ActiveInfo firstActiveInfo = firstActiveInfos.get(value);
                if(firstActiveInfo != null){
                    activeInfosMap.get(value).put(instruction, firstActiveInfo);
                }
            }
        }
        System.out.println(check(basicBlock));
        next();
    }

    // 如果use和def都同时为true，则使用肯定在定义前面
    static class ActiveInfo{
        // nullable
        ActiveInfo next;
        MidCode midCode;
        boolean isUse;
        boolean isDef;

        public ActiveInfo(MidCode midCode, boolean isUse, boolean isDef) {
            this.midCode = midCode;
            this.isUse = isUse;
            this.isDef = isDef;
            this.next = null;
        }
    }


    /**
     * 检查lvalue在instruction之前的定义从instruction开始（或之后开始）还是不是活跃的
     * 具体算法：从当前instruction往后遍历activeInfo，如果先找到use就代表是活跃的，如果先找到只有def没有use或者没找到，那么就是不活跃的
     * @param startToAfter 为true代表从instruction之后开始寻找使用
     * @return check if lvalue's last def (not include this instruction) will be used
     */
    boolean isStillUse(LValue value, boolean startToAfter){
//         todo bug: 如果value是一个全局变量，且value在当前基本快中的下一次是定义，则该函数一定返回false；不过实际上有可能还会活跃，因为这期间其他函数有可能使用
        if(value instanceof Variable && ((Variable) value).isGlobal()){
            // todo 需要检查其他函数的使用情况？
            return true;
        }
        ActiveInfo activeInfo = activeInfosMap.get(value).get(getNowInstruction());
        if(startToAfter){
            // check now
            if(activeInfo == null){
                return false;
            }
            if(activeInfo.midCode == getNowInstruction()){
                if(activeInfo.isDef) return false;
                activeInfo = activeInfo.next;
            }
        }
        while(activeInfo != null){
            if(activeInfo.isUse){
                return true;
            }else if(activeInfo.isDef){
                return false;
            }
            activeInfo = activeInfo.next;
        }
        return false;
    }

    private String check(BasicBlock basicBlock){
        List<MidCode> midCodes = new ArrayList<>(basicBlock.getSequenceList());
        midCodes.add(basicBlock.getJump());
        StringBuilder ret = new StringBuilder();
        for(Map.Entry<LValue, ActiveInfo> entry : firstActiveInfos.entrySet()){
            Value value = entry.getKey();
            ActiveInfo activeInfo = entry.getValue();
            ret.append(value.print()).append(" ");
            while (activeInfo != null){
                int no = midCodes.indexOf(activeInfo.midCode) + 1;
                if(activeInfo.isUse){
                    ret.append(no).append(", ");
                }
                if(activeInfo.isDef){
                    ret.append("| ").append(no).append(": ");
                }
                activeInfo = activeInfo.next;
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    public Set<LValue> getAllLValues() {
        return allValues;
    }
}
