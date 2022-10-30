package backend;

import midcode.BasicBlock;
import midcode.MidCode;
import midcode.instrument.Instrument;
import midcode.value.*;

import java.util.*;

/**
 * 计算基本快中每个中间代码的每个lvalue的后继活跃信息
 * 假设基本块中的所有variable都可能会在基本快结束后活跃
  */
public class LocalActive {
    private ValueGetter valueGetter = ValueGetter.getInstance();
    //value是key对应的activeinfo，每个instrument都会对应一个activeInfo，对应的是从该instrument找起第一个activeInfo
    Map<LValue, Map<MidCode, ActiveInfo>> activeInfosMap = new HashMap<>();
    Map<LValue, ActiveInfo> firstActiveInfos = new HashMap<>();

    LocalActive(BasicBlock basicBlock) {
        List<LValue> allLValues = new ArrayList<>();
        //假设基本块中的所有variable都可能会在基本快结束后活跃
        List<MidCode> midCodes = new ArrayList<>(basicBlock.getInstruments());
        midCodes.add(basicBlock.getLastInstrument());
        for(MidCode midCode : midCodes){
            for(RValue value : valueGetter.getAllValues(midCode)) {
                if(value instanceof Variable){
                    allLValues.add((LValue) value);
                    ActiveInfo lastActiveInfo = new ActiveInfo(outInstrument, true, false);
                    Map<MidCode, ActiveInfo> activeInfoMap = new HashMap<>();
                    activeInfoMap.put(outInstrument, lastActiveInfo);
                    activeInfosMap.put((LValue) value, activeInfoMap);
                    firstActiveInfos.put((LValue) value, lastActiveInfo);
                }else if(value instanceof Temp){
                    allLValues.add((LValue) value);
                    activeInfosMap.put((LValue) value, new HashMap<>());
                }
            }
        }
        ListIterator<MidCode> listIterator = midCodes.listIterator(basicBlock.getInstruments().size());
        while(listIterator.hasPrevious()){
            MidCode midCode = listIterator.previous();
            Map<LValue, ActiveInfo> nowActiveInfos = new HashMap<>();
            for(Value value : valueGetter.getUseValues(midCode)){
                if(value instanceof LValue){
                    nowActiveInfos.put((LValue) value, new ActiveInfo(midCode, true, false));
                }
            }
            Optional<LValue> defValue = valueGetter.getDefValue(midCode);
            if(defValue.isPresent()){
                if(nowActiveInfos.containsKey(defValue.get())){
                    nowActiveInfos.get(defValue.get()).isDef = true;
                }else{
                    nowActiveInfos.put(defValue.get(), new ActiveInfo(midCode, false, true));
                }
            }
            for(Map.Entry<LValue, ActiveInfo> entry : nowActiveInfos.entrySet()){
                LValue lValue = entry.getKey();
                ActiveInfo nowActiveInfo = entry.getValue();
                if(firstActiveInfos.containsKey(lValue)){
                    nowActiveInfo.next = firstActiveInfos.get(lValue);
                }
                firstActiveInfos.put(lValue, nowActiveInfo);
            }
            for(LValue lValue : allLValues){
                ActiveInfo firstActiveInfo = firstActiveInfos.get(lValue);
                if(firstActiveInfo != null){
                    activeInfosMap.get(lValue).put(midCode, firstActiveInfo);
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

    private static class Nop implements Instrument{
        @Override
        public String print() {
            return "nop";
        }
    }
    private final Instrument outInstrument = new Nop();

//    boolean isActiveAfter(LValue lValue, Instrument instrument){
//        return activeInfo.get(instrument).get(lValue) != null;
//    }

    /**
     * 检查lvalue在instrument之前的定义从instrument开始还是不是活跃的
     * 具体算法：从当前instrument往后遍历activeInfo，如果先找到use就代表是活跃的，如果先找到只有def没有use或者没找到，那么就是不活跃的
     * @return check if lvalue's last def (not include this instrument) will be used
     */
    boolean isStillUse(LValue lValue, MidCode midCode){
        ActiveInfo activeInfo = activeInfosMap.get(lValue).get(midCode);
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
    /**
     * 检查lvalue在instrument之前的定义从instrument的下一条命令开始还是不是活跃的
     * 注意：对于当前instrument来说变量活跃是无所谓的
     * @return check if lvalue's last def (not include this instrument) will be used
     */
    boolean isStillUseAfter(LValue lValue, MidCode midCode){
        ActiveInfo activeInfo = activeInfosMap.get(lValue).get(midCode);
        // check now is use
        if(activeInfo == null){
            return false;
        }
        if(activeInfo.midCode == midCode){
            if(activeInfo.isDef) return false;
            activeInfo = activeInfo.next;
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
        for(Map.Entry<LValue, ActiveInfo> entry : firstActiveInfos.entrySet()){
            LValue lValue = entry.getKey();
            ActiveInfo activeInfo = entry.getValue();
            ret.append(lValue.print()).append(" ");
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
}
