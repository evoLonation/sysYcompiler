package backend;

import midcode.BasicBlock;
import midcode.MidCode;
import midcode.instrument.Call;
import midcode.instrument.Instrument;
import midcode.instrument.Jump;
import midcode.value.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 计算基本快中每个中间代码的每个lvalue的后继活跃信息
 * 假设基本块中的所有variable都可能会在基本快结束后活跃
  */
public class LocalActive {
    private final ValueGetter valueGetter = ValueGetter.getInstance();
    //value是key对应的activeinfo，每个instrument都会对应一个activeInfo，对应的是从该instrument找起第一个activeInfo
    private final Map<Value, Map<MidCode, ActiveInfo>> activeInfosMap = new HashMap<>();
    private final Map<Value, ActiveInfo> firstActiveInfos = new HashMap<>();
    private final Set<Value> allValues;
    private final List<MidCode> midCodes;
    private MidCode nowMidCode;

    public void forEach(Consumer<? super Instrument> instrumentAction, Runnable beforeJump, Consumer<? super Jump> jumpAction) {
        for (MidCode midCode : midCodes) {
            nowMidCode = midCode;
            if(midCode instanceof Jump){
                beforeJump.run();
                jumpAction.accept((Jump) midCode);
                break;
            }
            instrumentAction.accept((Instrument) midCode);
        }
    }

    public MidCode getNowMidCode() {
        return nowMidCode;
    }

    private static class OutInstrument implements Instrument{
        @Override
        public String print() {
            return "#outInstrument";
        }
    }

    LocalActive(BasicBlock basicBlock) {
        OutInstrument outInstrument = new OutInstrument();
        midCodes = Stream.concat(basicBlock.getInstruments().stream(),
                Stream.of(basicBlock.getLastInstrument()
                        , outInstrument))
                .collect(Collectors.toList());
        //假设基本块中的所有variable都可能会在基本快结束后活跃
        allValues = midCodes.stream()
                .flatMap(midCode -> valueGetter.getAllValues(midCode).stream()).collect(Collectors.toSet());
        for(Value value : allValues) {
            if(value instanceof Variable){
                ActiveInfo lastActiveInfo = new ActiveInfo(outInstrument, true, false);
                Map<MidCode, ActiveInfo> activeInfoMap = new HashMap<>();
                activeInfoMap.put(outInstrument, lastActiveInfo);
                activeInfosMap.put(value, activeInfoMap);
                firstActiveInfos.put(value, lastActiveInfo);
            }else if(value instanceof Temp){
                activeInfosMap.put(value, new HashMap<>());
            }
        }

        ListIterator<MidCode> listIterator = midCodes.listIterator(midCodes.size());
        while(listIterator.hasPrevious()){
            MidCode midCode = listIterator.previous();
            Map<Value, ActiveInfo> nowActiveInfos = new HashMap<>();
            for(Value value : valueGetter.getUseValues(midCode)){
                nowActiveInfos.put(value, new ActiveInfo(midCode, true, false));
            }
            valueGetter.getDefValue(midCode).ifPresent(value->{
                if(nowActiveInfos.containsKey(value)){
                    nowActiveInfos.get(value).isDef = true;
                }else{
                    nowActiveInfos.put(value, new ActiveInfo(midCode, false, true));
                }
            });

            for(Map.Entry<Value, ActiveInfo> entry : nowActiveInfos.entrySet()){
                Value value = entry.getKey();
                ActiveInfo nowActiveInfo = entry.getValue();
                if(firstActiveInfos.containsKey(value)){
                    nowActiveInfo.next = firstActiveInfos.get(value);
                }
                firstActiveInfos.put(value, nowActiveInfo);
            }
            for(Value value : allValues){
                ActiveInfo firstActiveInfo = firstActiveInfos.get(value);
                if(firstActiveInfo != null){
                    activeInfosMap.get(value).put(midCode, firstActiveInfo);
                }
            }
        }
        System.out.println(check(basicBlock));
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
     * 检查lvalue在instrument之前的定义从instrument开始（或之后开始）还是不是活跃的
     * 具体算法：从当前instrument往后遍历activeInfo，如果先找到use就代表是活跃的，如果先找到只有def没有use或者没找到，那么就是不活跃的
     * @param startToAfter 为true代表从instrument之后开始寻找使用
     * @return check if lvalue's last def (not include this instrument) will be used
     */
    boolean isStillUse(Value value, boolean startToAfter){
        ActiveInfo activeInfo = activeInfosMap.get(value).get(nowMidCode);
        if(startToAfter){
            // check now
            if(activeInfo == null){
                return false;
            }
            if(activeInfo.midCode == nowMidCode){
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
        List<MidCode> midCodes = new ArrayList<>(basicBlock.getInstruments());
        midCodes.add(basicBlock.getLastInstrument());
        StringBuilder ret = new StringBuilder();
        for(Map.Entry<Value, ActiveInfo> entry : firstActiveInfos.entrySet()){
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

    public Set<Value> getAllValues() {
        return allValues;
    }
}
