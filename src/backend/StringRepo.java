package backend;

import midcode.BasicBlock;
import midcode.instrument.PrintString;

import java.util.HashMap;
import java.util.Map;

public class StringRepo {
    private final MipsSegment mipsSegment = MipsSegment.getInstance();
    private final Map<PrintString, String> labelMap = new HashMap<>();
    private int index = 0;


    void scanBasicBlock(BasicBlock basicBlock){
        basicBlock.getInstruments().stream().filter(instrument -> instrument instanceof PrintString)
                .forEach((instrument) -> labelMap.put((PrintString) instrument, "string" + index++));
    }
    void print(){
        labelMap.forEach((key, value) -> mipsSegment.string(value, key.getString()));
    }

    String getStringLabel(PrintString printString){
        return labelMap.get(printString);
    }

    private StringRepo() {}
    static private final StringRepo instance = new StringRepo();
    static public StringRepo getInstance(){
        return instance;
    }
}
