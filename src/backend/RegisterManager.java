package backend;

import midcode.MidCode;
import midcode.instrument.Instrument;
import midcode.value.*;

import java.util.*;
import java.util.stream.Collectors;

// 保存基本快中通用寄存器使用信息
public class RegisterManager {
    private final LocalActive localActive;
    private final ValueGetter valueGetter = ValueGetter.getInstance();


    // 包括lvalue和addressValue
    private final Map<Register, Set<Value>> registerDescriptors;
    // 包括lvalue和addressValue
    private final Map<Value, Register> valueRegisterMap;

    private final Map<Value, Memory> valueMemoryMap;
    // 这是temp申请过得，但是最后又与temp解绑了的
    private final Set<Memory> freeLocation;
    private final int dataAddress = 0x10010000;
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
    private int nowMaxOffset;

    RegisterManager(LocalActive localActive, int offset){
        freeLocation = new HashSet<>();
        valueMemoryMap = localActive.getAllValues().stream()
                .filter(lValue -> lValue instanceof Variable).map(lValue -> (Variable)lValue)
                .collect(Collectors.toMap(variable -> variable, Memory::new));
//        memoryDescriptors = valueMemoryMap.entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, entry -> Stream.of(entry.getKey()).collect(Collectors.toSet())));
        this.localActive = localActive;
        valueRegisterMap = new HashMap<>();
        registerDescriptors = Register.getLocalRegister().stream()
                .collect(Collectors.toMap(r -> r, r -> new HashSet<>()));
        nowMaxOffset = offset;
    }

    int getDataAddress() {
        return dataAddress;
    }

    int getNowMaxOffset() {
        return nowMaxOffset;
    }

    private Optional<Register> getEmptyReg(){
        return registerDescriptors.entrySet().parallelStream()
                .filter(entry ->entry.getValue().isEmpty())
                .findAny().map(Map.Entry::getKey);
    }

    //
    // 状态的变化
    //

    private void bind(Register register, Value value) {
        registerDescriptors.get(register).add(value);
        valueRegisterMap.put(value, register);
    }

    private void bind(Memory memory, Value value){
        valueMemoryMap.put(value, memory);
    }

    private void unBind(Value lValue){
        valueRegisterMap.remove(lValue);
        registerDescriptors.values().forEach(lValues -> lValues.remove(lValue));
        valueMemoryMap.remove(lValue);
    }

    private void unBind(Register register){
        registerDescriptors.get(register).clear();
        valueRegisterMap.entrySet().removeIf(entry -> entry.getValue() == register);
    }

    private void unBind(Memory memory){
        valueMemoryMap.entrySet().removeIf(entry->entry.getValue() == memory);
    }

    Set<Value> getValues(Register register){
        return registerDescriptors.get(register);
    }

    // 某个内存的值载入寄存器，则内存中的值要与寄存器绑定
    void load(Memory memory, Register register) {
        unBind(register);
        valueMemoryMap.forEach((key, value) -> {
            if (value == memory) {
                bind(register, key);
            }
        });
    }


    // 某个寄存器被存入内存，则内存中的值要与寄存器绑定
    void store(Register register, Memory memory){
        unBind(memory);
        registerDescriptors.get(register).forEach(value -> bind(memory, value));
    }

    // 某个寄存器被指令改变，则要与之前绑定的值取消绑定
    void change(Register register) {
        unBind(register);
    }

    void change(Register register, Value value){
        change(register);
        unBind(value);
        bind(register ,value);
    }

    /**
     * 寄存器不变，将value的值绑定到寄存器
     */
    void bindTo(Register register, Value value){
        unBind(value);
        bind(register, value);
    }



    Optional<Register> getRegister(Value value){
        return Optional.ofNullable(valueRegisterMap.get(value));
    }

    Optional<Memory> getMemory(Value value){
        return Optional.ofNullable(valueMemoryMap.get(value));
    }

    // 分配一个给temp的空闲的空间
    Memory getNewMemory(){
        if(freeLocation.isEmpty()){
            freeLocation.add(new Memory(nowMaxOffset, false));
            nowMaxOffset ++;
        }
        return freeLocation.iterator().next();
    }

    Memory getVariableMemory(Variable variable){
        return new Memory(variable);
    }

    /**
     * @return 启发式的为目标指令要定义的值（产生的结果）分配一个寄存器
     * 值得注意的是，本指令中正在使用的寄存器只要之后不再使用就是允许的
     */
    Register getResultReg() {
        return getReg(true);
    }

    /**
     * 启发式的为指定的value分配一个寄存器
     */
    Register getFactorReg(Value factor) {
        // 先找已经有的
        return Optional.ofNullable(valueRegisterMap.get(factor))
            .orElseGet(() -> getReg(false));
    }

    Register getReg(boolean isResult){
        return registerDescriptors.entrySet().stream()
                .filter(entry-> {
                    // 所有的value从这条指令之后都不再使用
                    return entry.getValue().stream()
                            .noneMatch(value -> localActive.isStillUse(value, isResult));
                }).findAny().map(Map.Entry::getKey)
        .orElseGet(() -> {
            // 再找空闲的reg
            return getEmptyReg()
            .orElseGet(() -> {
                // 对每个Set<LValue>，计算需要存储最少次数的register
                return registerDescriptors.entrySet().stream()
                // 排除掉包含了本指令要使用的值的register
                .filter(entry ->
                    // 如果是结果，则不需要排除
                    isResult ||
                    // 在所有的值都不包含在本指令要使用的值中时，保留
                    entry.getValue().stream()
                        .noneMatch(value -> valueGetter.getUseValues(localActive.getNowMidCode()).contains(value))
                )
                .min((entry1, entry2)-> {
                    int number1 = needStoreNumber(entry1.getValue());
                    int number2 = needStoreNumber(entry2.getValue());
                    return Integer.compare(number1, number2);
                })
                .orElse(registerDescriptors.entrySet().iterator().next())
                .getKey();
            });
        });
    }

    private int needStoreNumber(Set<Value> values){
        return (int) values.stream()
                .filter(value -> localActive.isStillUse(value, true))
                .filter(value -> !getMemory(value).isPresent())
                .count();
    }

}
