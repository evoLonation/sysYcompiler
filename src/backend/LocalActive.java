package backend;

import midcode.BasicBlock;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.instrument.UnaryOperation;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Variable;
import util.Execution;
import util.VoidExecution;

import java.util.*;

/**
 * 计算基本快中每个中间代码的每个lvalue的后继活跃信息
 * 假设基本块中的所有variable都可能会在基本快结束后活跃
  */
public class LocalActive {
    LocalActive(BasicBlock basicBlock){
        ListIterator<Instrument> listIterator = basicBlock.getInstruments().listIterator(basicBlock.getInstruments().size());
        Map<LValue, Instrument> lastUse = new HashMap<>();
        //假设基本块中的所有variable都可能会在基本快结束后活跃
        for(Variable variable : getVariables(basicBlock)){
            lastUse.put(variable, outInstrument);
        }
        while(listIterator.hasPrevious()){
            Instrument instrument = listIterator.previous();
            if(instrument instanceof BinaryOperation){
                BinaryOperation operation = (BinaryOperation)instrument;
                dealInstrument(instrument, lastUse, operation.getResult(), operation.getLeft(), operation.getRight());
            }else if(instrument instanceof UnaryOperation){
                UnaryOperation operation = (UnaryOperation) instrument;
                dealInstrument(instrument, lastUse, operation.getResult(), operation.getValue());
            }
        }
        System.out.println(check(basicBlock));
    }

    private Collection<Variable> getVariables(BasicBlock basicBlock){
        Set<Variable> ret = new HashSet<>();
        VoidExecution<Instrument> execution = new VoidExecution<Instrument>() {
            @Override
            public void inject() {
                inject(BinaryOperation.class, param -> addIfVariable(ret, param.getResult(), param.getLeft(), param.getRight()));
            }
        };
        execution.inject();
        for(Instrument instrument : basicBlock.getInstruments()){
            execution.exec(instrument);
        }
        return ret;
    }
    private void addIfVariable(Set<Variable> set, RValue... rValues){
        for(RValue rValue : rValues){
            if(rValue instanceof Variable){
                set.add((Variable) rValue);
            }
        }
    }

    private void dealInstrument(Instrument instrument, Map<LValue, Instrument> lastUse, LValue def, RValue... uses){
        Map<LValue, Instrument> nextUse = new HashMap<>();
        nextUse.put(def, lastUse.get(def));
        lastUse.remove(def);
        for(RValue use : uses){
            if(use instanceof LValue){
                nextUse.put((LValue) use, lastUse.get(use));
                lastUse.put((LValue) use, instrument);
            }
        }
        activeInfo.put(instrument, nextUse);
    }



    // value是key对应的中间代码的相关lvalue的后继使用信息
    // value 是map，该map的value是对应key的lvalue下次被哪个instrument使用，value(instrument)为null代表已经不再使用，为outInstrument代表在基本块之外被使用
    Map<Instrument, Map<LValue, Instrument>> activeInfo = new HashMap<>();
    static class Nop implements Instrument{
        @Override
        public String print() {
            return "nop";
        }
    }
    private final Instrument outInstrument = new Nop();

    boolean isActiveAfter(LValue lValue, Instrument instrument){
        return activeInfo.get(instrument).get(lValue) != null;
    }

    private String check(BasicBlock basicBlock){
        Map<LValue, List<Integer>> readyToPrint = new LinkedHashMap<>();
        for(Instrument instrument : basicBlock.getInstruments()){
            for(Map.Entry<LValue, Instrument> node : activeInfo.get(instrument).entrySet()){
                LValue lValue = node.getKey();
                if(!readyToPrint.containsKey(lValue)){
                    List<Integer> useList = new ArrayList<>();
                    Instrument useInstrument = node.getValue();
                    while(useInstrument != null){
                        if(useInstrument == outInstrument){
                            useList.add(basicBlock.getInstruments().size());
                            break;
                        }
                        useList.add(basicBlock.getInstruments().indexOf(useInstrument));
                        useInstrument = activeInfo.get(useInstrument).get(lValue);
                    }
                    readyToPrint.put(node.getKey(), useList);
                }
            }
        }
        StringBuilder ret = new StringBuilder();
        for(Map.Entry<LValue, List<Integer>> entry : readyToPrint.entrySet()){
            ret.append(entry.getKey().print()).append(": ");
            for(Integer no : entry.getValue()){
                ret.append(no).append(", ");
            }
            ret.append("\n");
        }
        return ret.toString();
    }
}
