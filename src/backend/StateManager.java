package backend;

import common.SemanticException;
import common.ValueGetter;
import midcode.value.*;

import java.util.*;
import java.util.stream.Collectors;

// 保存基本快中通用寄存器使用信息
public class StateManager {
    private final LocalActive localActive;
    private final ValueGetter valueGetter = ValueGetter.getInstance();
    private final MipsSegment mipsSegment = MipsSegment.getInstance();


    // 包括lvalue和addressValue
    private final Map<Register, Set<LValue>> registerDescriptors;
    // 包括lvalue和addressValue
    private final Map<LValue, Set<Register>> valueRegisterMap;

    private final Map<LValue, Memory> valueMemoryMap;

    private final Set<Memory> otherMemories;
    private final Map<Variable, Memory> variableMemories;


    private static final int dataAddress = 0x10010000;
    private int nowMaxOffset;
    static class Memory {
        int offset;
        boolean isGlobal;
        Memory(int offset, boolean isGlobal) {
            this.offset = offset;
            this.isGlobal = isGlobal;
        }
        Memory(Variable variable){
            this(variable.getOffset(), variable.isGlobal());
        }
    }


    StateManager(LocalActive localActive, int offset){
        this.localActive = localActive;
        this.nowMaxOffset = offset;
        this.variableMemories = new HashMap<>();
        this.valueMemoryMap = new HashMap<>();
        this.otherMemories = new HashSet<>();
        localActive.getAllLValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .forEach(value -> {
                    Memory variableMemory = new Memory((Variable) value);
                    variableMemories.put((Variable) value, variableMemory);
                    valueMemoryMap.put(value, variableMemory);
                });

        valueRegisterMap = localActive.getAllLValues().stream().collect(Collectors.toMap(value -> value, value -> new HashSet<>()));
        registerDescriptors = Register.getLocalRegister().stream().collect(Collectors.toMap(register -> register, register -> new HashSet<>()));
        this.busyRegisters = new HashSet<>();
    }

    static int getDataAddress() {
        return dataAddress;
    }

    int getNowMaxOffset() {
        return nowMaxOffset;
    }

    Optional<Register> getEmptyReg(){
        return registerDescriptors.entrySet().parallelStream()
                .filter(entry -> !isBusy(entry.getKey()))
                .filter(entry ->entry.getValue().isEmpty())
                .findAny().map(Map.Entry::getKey);
    }

    Set<LValue> getLValues(Register register){
        return registerDescriptors.get(register);
    }

    private final Set<Register> busyRegisters;

    private boolean isBusy(Register register){
        return busyRegisters.contains(register);
    }

    /**
     * @param register 该寄存器无法被getreg获取到了
     */
    void setBusy(Register register){
        assert Register.getLocalRegister().contains(register);
        busyRegisters.add(register);
    }

    void setFree(Register register){
        assert Register.getLocalRegister().contains(register);
        busyRegisters.remove(register);
    }

    void clearRegister(){
        valueRegisterMap.clear();
        valueRegisterMap.putAll(localActive.getAllLValues().stream().collect(Collectors.toMap(value -> value, value -> new HashSet<>())));
        registerDescriptors.clear();
        registerDescriptors.putAll(Register.getLocalRegister().stream().collect(Collectors.toMap(register -> register, register -> new HashSet<>())));
    }

    void bind(Register register, LValue value) {
        registerDescriptors.get(register).add(value);
        valueRegisterMap.get(value).add(register);
    }

    void change(Register register){
        if(!registerDescriptors.containsKey(register)){
            throw new SemanticException();
        }
        registerDescriptors.get(register).clear();
        valueRegisterMap.values().forEach(registers -> registers.remove(register));
    }

    Memory store(LValue value){
        if(value instanceof Variable){
            valueMemoryMap.put(value, variableMemories.get((Variable) value));
        }else{
            if(!valueMemoryMap.containsKey(value)){
                // 查找空闲tempMemory
                Memory tempMemory =
                        otherMemories.stream()
                                .filter(memory -> !valueMemoryMap.containsValue(memory)).findAny()
                                .orElseGet(() -> {
                                    Memory newTempMemory = new Memory(nowMaxOffset++, false);
                                    otherMemories.add(newTempMemory);
                                    return newTempMemory;
                                });
                valueMemoryMap.put(value, tempMemory);
                return tempMemory;
            }
        }
        return valueMemoryMap.get(value);
    }

    void change(LValue value){
        valueRegisterMap.get(value).clear();
        registerDescriptors.values().forEach(lValues -> lValues.remove(value));
        valueMemoryMap.remove(value);

    }

    boolean isStore(LValue value){
        return getMemory(value).isPresent();
    }

    Optional<Register> anyRegister(LValue value){
        return valueRegisterMap.get(value).stream().findAny();
    }

    Optional<Memory> getMemory(LValue lValue){
        return Optional.ofNullable(valueMemoryMap.get(lValue));
    }

    /**
     * @return 获得寄存器中从下一条中间指令开始还需要使用的且没在内存中的值的集合
     */
    Set<LValue> getNeedStore(Register register, boolean startToAfter){
        return registerDescriptors.get(register).stream()
                .filter(value -> !isStore(value))
                .filter(value -> localActive.isStillUse(value, startToAfter))
                .collect(Collectors.toSet());
    }

    // 寄存器经过指令操作被改变，用来表征value这个值
    void operate(Register register, LValue value){
        change(register);
        change(value);
        bind(register ,value);
    }

    // 寄存器被加载为value值
    void load(Register register, LValue value){
        change(register);
        bind(register, value);
    }

    /**
     * 寄存器不变，将value的值改变为寄存器的值
     */
    void assign(LValue value, Register register){
        change(value);
        bind(register, value);
    }

    /**
     * 仅仅用于为一个中间代码的def值分配寄存器时！因为该方法会考虑分配当前中间代码的use值
     * @return 启发式的为目标指令要定义的值（产生的结果）分配一个寄存器
     * 值得注意的是，本指令中正在使用的寄存器只要之后不再使用就是允许的
     */
    Register getResultReg() {
        return getReg(true);
    }

    /**
     * 启发式的为当前中间代码的一个use值分配一个寄存器
     */
    Register getFactorReg(LValue factor) {
        assert valueGetter.getLValueUseValues(localActive.getNowSequence()).contains(factor);
        // 先找已经有的
        return anyRegister(factor).orElseGet(this::getReg);
    }

    Register getReg(){
        return getReg(false);
    }


    // todo 寄存器可能没有存入值，可能需要将其他值存出去
    /**
     * 分配一个寄存器
     * @param overrideCurrentUse 指是否可以从当前指令的use值中拿寄存器
     */
    Register getReg(boolean overrideCurrentUse){
        return
        registerDescriptors.entrySet().stream()
            .filter(entry->
                // 所有的value从这条指令之后都不再使用
                entry.getValue().stream()
                        .noneMatch(value -> localActive.isStillUse(value, overrideCurrentUse)) &&
                        !isBusy(entry.getKey()))
            .findAny().map(Map.Entry::getKey)
            // 再找空闲的reg
            .orElseGet(() -> getEmptyReg()
            .orElse(
                // 再找要存储次数最少的
                // 对每个Set<LValue>，计算需要存储最少次数的register
                registerDescriptors.entrySet().stream()
                    // 排除掉包含了本指令要使用的值的register
                    .filter(entry -> {
                            if(isBusy(entry.getKey())){
                                return false;
                            }else {
                                return // 如果是结果，则不需要排除
                                        overrideCurrentUse ||
                                        // 在所有的值都不包含在本指令要使用的值中时，保留
                                        entry.getValue().stream().noneMatch(value -> valueGetter.getLValueUseValues(localActive.getNowInstruction()).contains(value));
                            }
                    }).map(Map.Entry::getKey)
                    .min((register1, register2)-> {
                        // 如果override为ture，也即当前指令的不用存；如果为false，当前指令的要存
                        int number1 = getNeedStore(register1, overrideCurrentUse).size();
                        int number2 = getNeedStore(register2, overrideCurrentUse).size();
                        return Integer.compare(number1, number2);
                    }).orElseThrow(SemanticException::new)
            ));

//            needStoreReference.get().forEach(this::storeValue);
    }


}
