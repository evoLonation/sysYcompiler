package backend;

import midcode.BasicBlock;
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
    Map<LValue, Map<Instrument, ActiveInfo>> activeInfosMap = new HashMap<>();
    Map<LValue, ActiveInfo> firstActiveInfos = new HashMap<>();

    LocalActive(BasicBlock basicBlock) {
        List<LValue> allLValues = new ArrayList<>();
        //假设基本块中的所有variable都可能会在基本快结束后活跃
        for(Instrument instrument : basicBlock.getInstruments()){
            for(RValue value : valueGetter.getAllValues(instrument)) {
                if(value instanceof Variable){
                    allLValues.add((LValue) value);
                    ActiveInfo lastActiveInfo = new ActiveInfo(outInstrument, true, false);
                    Map<Instrument, ActiveInfo> activeInfoMap = new HashMap<>();
                    activeInfoMap.put(outInstrument, lastActiveInfo);
                    activeInfosMap.put((LValue) value, activeInfoMap);
                    firstActiveInfos.put((LValue) value, lastActiveInfo);
                }else if(value instanceof Temp){
                    allLValues.add((LValue) value);
                    activeInfosMap.put((LValue) value, new HashMap<>());
                }
            }
        }
        ListIterator<Instrument> listIterator = basicBlock.getInstruments().listIterator(basicBlock.getInstruments().size());
        while(listIterator.hasPrevious()){
            Instrument instrument = listIterator.previous();
            Map<LValue, ActiveInfo> nowActiveInfos = new HashMap<>();
            for(Value value : valueGetter.getUseValues(instrument)){
                if(value instanceof LValue){
                    nowActiveInfos.put((LValue) value, new ActiveInfo(instrument, true, false));
                }
            }
            Optional<LValue> defValue = valueGetter.getDefValue(instrument);
            if(defValue.isPresent()){
                if(nowActiveInfos.containsKey(defValue.get())){
                    nowActiveInfos.get(defValue.get()).isDef = true;
                }else{
                    nowActiveInfos.put(defValue.get(), new ActiveInfo(instrument, false, true));
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
                    activeInfosMap.get(lValue).put(instrument, firstActiveInfo);
                }
            }
        }
        System.out.println(check(basicBlock));
    }

    // 如果use和def都同时为true，则使用肯定在定义前面
    static class ActiveInfo{
        // nullable
        ActiveInfo next;
        Instrument instrument;
        boolean isUse;
        boolean isDef;

        public ActiveInfo(Instrument instrument, boolean isUse, boolean isDef) {
            this.instrument = instrument;
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
    boolean isStillUse(LValue lValue, Instrument instrument){
        ActiveInfo activeInfo = activeInfosMap.get(lValue).get(instrument);
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
    boolean isStillUseAfter(LValue lValue, Instrument instrument){
        ActiveInfo activeInfo = activeInfosMap.get(lValue).get(instrument);
        // check now is use
        if(activeInfo == null){
            return false;
        }
        if(activeInfo.instrument == instrument){
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
        Map<LValue, List<List<Integer>>> readyToPrint = new LinkedHashMap<>();
        StringBuilder ret = new StringBuilder();
        for(Map.Entry<LValue, ActiveInfo> entry : firstActiveInfos.entrySet()){
            LValue lValue = entry.getKey();
            ActiveInfo activeInfo = entry.getValue();
            ret.append(lValue.print()).append(" ");
            while (activeInfo != null){
                int no = basicBlock.getInstruments().indexOf(activeInfo.instrument) + 1;
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
