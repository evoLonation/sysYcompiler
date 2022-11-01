package backend;

import common.SemanticException;
import midcode.value.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 保存基本快中通用寄存器使用信息
public class StorageManager {
    private final LocalActive localActive;
    private final ValueGetter valueGetter = ValueGetter.getInstance();
    private final MipsSegment mipsSegment;


    // 包括lvalue和addressValue
    private final Map<Register, Set<Value>> registerDescriptors;
    // 包括lvalue和addressValue
    private final Map<Value, Set<Register>> valueRegisterMap;

    private final Map<Value, Set<Memory>> valueMemoryMap;
    private final Map<Memory, Set<Value>> memoryDescriptors;

    private final Map<Variable, Memory> variableMemoryMap;
    private final Set<Memory> tempMemoryMap;


    private final int dataAddress = 0x10010000;
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


    StorageManager(LocalActive localActive, MipsSegment mipsSegment, int offset){
        this.mipsSegment = mipsSegment;
        this.localActive = localActive;
        this.nowMaxOffset = offset;


        this.variableMemoryMap = new HashMap<>();
        this.valueMemoryMap = localActive.getAllValues().stream().collect(Collectors.toMap(value -> value, value -> new HashSet<>()));
        this.memoryDescriptors = new HashMap<>();
        this.tempMemoryMap = new HashSet<>();
        localActive.getAllValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .forEach(value -> {
                    Memory variableMemory = new Memory((Variable) value);
                    variableMemoryMap.put((Variable) value, variableMemory);
                    valueMemoryMap.put(value, Stream.of(variableMemory).collect(Collectors.toSet()));
                    memoryDescriptors.put(variableMemory, Stream.of(value).collect(Collectors.toSet()));
                });

        valueRegisterMap = localActive.getAllValues().stream().collect(Collectors.toMap(value -> value, value -> new HashSet<>()));
        registerDescriptors = Register.getLocalRegister().stream().collect(Collectors.toMap(register -> register, register -> new HashSet<>()));
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
    // 状态的变化（类比为图的边的建立和解除）
    //

    private void bind(Register register, Value value) {
        registerDescriptors.get(register).add(value);
        valueRegisterMap.get(value).add(register);
    }

    private void bind(Memory memory, Value value){
        memoryDescriptors.get(memory).add(value);
        valueMemoryMap.get(value).add(memory);
    }

    private void unBind(Value value){
        valueRegisterMap.get(value).clear();
        registerDescriptors.values().forEach(lValues -> lValues.remove(value));
        valueMemoryMap.get(value).clear();
        memoryDescriptors.values().forEach(values -> values.remove(value));
    }

    private void unBind(Register register){
        registerDescriptors.get(register).clear();
        valueRegisterMap.values().forEach(registers -> registers.remove(register));
    }

    private void unBind(Memory memory){
        memoryDescriptors.get(memory).clear();
        valueMemoryMap.values().forEach(memories-> memories.remove(memory));
    }

    Optional<Memory> anyMemory(Value value){
        return valueMemoryMap.get(value).stream().findAny();
    }
    Optional<Register> anyRegister(Value value){
        return valueRegisterMap.get(value).stream().findAny();
    }

    Set<Value> getNeedStore(Register register, boolean excludeCurrentInstrument){
        return registerDescriptors.get(register).stream()
                .filter(value -> !anyMemory(value).isPresent())
                .filter(value -> localActive.isStillUse(value, excludeCurrentInstrument))
                .collect(Collectors.toSet());
    }

    // 某个内存的值载入寄存器，则内存中的值要与寄存器绑定
    void load(Memory memory, Register register) {
        unBind(register);
        // 内存中的所有值绑定到寄存器
        memoryDescriptors.get(memory).forEach(value -> bind(register, value));
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

    // 寄存器改变后将值绑定到寄存器
    void change(Register register, Value value){
        unBind(register);
        unBind(value);
        bind(register ,value);
    }

    /**
     * 寄存器不变，将value的值改变为寄存器的值
     */
    void assign(Value value, Register register){
        unBind(value);
        bind(register, value);
        registerDescriptors.get(register).forEach(otherValue -> {
            valueMemoryMap.get(otherValue).forEach(memory -> bind(memory, value));
        });
    }

    // 分配一个给temp的空闲的空间
    // 可以直接store
    Memory getTempMemory() {
        return memoryDescriptors.entrySet().parallelStream()
                .filter(entry ->entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .filter(tempMemoryMap::contains)
                .findAny().orElseGet(() -> {
                    Memory memory = new Memory(nowMaxOffset, false);
                    nowMaxOffset ++;
                    memoryDescriptors.put(memory, new HashSet<>());
                    tempMemoryMap.add(memory);
                    return memory;
                });
    }

    // 获得variable对应的memory，提前保存占用的
    private Memory getVariableMemory(Variable variable){
        Memory variableMemory = variableMemoryMap.get(variable);
        memoryDescriptors.get(variableMemory).stream().filter(value -> value != variable).forEach(this::storeValue);
        return variableMemory;
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
        return anyRegister(factor).orElseGet(() -> getReg(false));
    }

    // 分配一个寄存器，该寄存器可以直接与其他值绑定，因为寄存器存的值已经存到内存了

    /**
     * @param isResult 指要分配的寄存器在指令中是以输入还是输出
     */
    private Register getReg(boolean isResult){
        Register register = registerDescriptors.entrySet().stream()
                .filter(entry-> {
                    // 所有的value从这条指令之后都不再使用
                    return entry.getValue().stream()
                            .noneMatch(value -> localActive.isStillUse(value, isResult));
                }).findAny().map(Map.Entry::getKey)
                // 再找空闲的reg
                .orElseGet(() -> getEmptyReg().orElse(null));
        if(register != null){
            return register;
        }else{
            AtomicReference<Register> registerReference = new AtomicReference<>();
            AtomicReference<Set<Value>> needStoreReference = new AtomicReference<>();
            Set<Value> needStore = new HashSet<>();
            // 再找要存储次数最少的
            // 对每个Set<LValue>，计算需要存储最少次数的register
            registerDescriptors.entrySet().stream()
            // 排除掉包含了本指令要使用的值的register
            .filter(entry ->
                    // 如果是结果，则不需要排除
                    isResult ||
                            // 在所有的值都不包含在本指令要使用的值中时，保留
                            entry.getValue().stream()
                                    .noneMatch(value -> valueGetter.getUseValues(localActive.getNowMidCode()).contains(value))
            ).forEach(entry -> {
                Set<Value> nowNeedStore = needStore(entry.getValue());
                if(needStoreReference.get() == null || needStoreReference.get().size() > nowNeedStore.size()){
                    needStoreReference.set(nowNeedStore);
                    registerReference.set(entry.getKey());
                }
            });
            needStoreReference.get().forEach(this::storeValue);
            return registerReference.get();
        }
    }

    // 保证所有的value都不是本指令要使用的
    private Set<Value> needStore(Set<Value> values){
        return values.stream()
                .filter(value -> localActive.isStillUse(value, true))
                .filter(value -> !anyMemory(value).isPresent())
                .collect(Collectors.toSet());
    }

    void storeValue(Value value) {
//        mipsSegment.comment(String.format("store %s", value.print()));
        if(anyMemory(value).isPresent()) return;
        Register register = anyRegister(value).orElseThrow(SemanticException::new);
        Memory memory;
        if(value instanceof Temp){
            memory = getTempMemory();
        }else {
            memory = getVariableMemory((Variable) value);
        }
        if(memory.isGlobal){
            mipsSegment.sw(register, dataAddress + memory.offset * 4);
        }else{
            mipsSegment.sw(register, Register.getSp(), -memory.offset * 4);
        }
        store(register, memory);
    }

    Register loadValue(Value value){
//        mipsSegment.comment(String.format("load %s", value.print()));
        return anyRegister(value).orElseGet(() -> {
            Memory memory = anyMemory(value).orElseThrow(SemanticException::new);
            Register register = getFactorReg(value);
            if(memory.isGlobal){
                mipsSegment.lw(register, dataAddress + memory.offset * 4);
            }else{
                mipsSegment.lw(register, Register.getSp(), -memory.offset * 4);
            }
            load(memory, register);
            return register;
        });
    }

    // todo 数组每个元素的地址是从小到大逐渐变大的，因此在栈里也要从小到大排列
    /**
     * pointer value store in temp register
     */
    Register loadAddressValue(AddressValue addressValue){
//        mipsSegment.comment(String.format("load %s", addressValue.print()));
        int staticOffset = addressValue.getStaticOffset();
        if(addressValue instanceof ArrayValue){
            boolean isGlobal = ((ArrayValue) addressValue).isGlobal();
            if(addressValue.getOffset() instanceof Constant){
                Register register = getReg(false);
                // li %s 0x1000f0+staticOffset
                if(isGlobal){
                    mipsSegment.li(register, dataAddress + (staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                } else{
                    mipsSegment.addi(register, Register.getSp(), + -(staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                }
                change(register, addressValue);
                return register;
            }else {
                assert addressValue.getOffset() instanceof LValue;
                // li %s 0x1000 + staticOffset
                // addi %s, %s, %s
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                change(offsetRegister, offset);

                Register register = getReg(false);
                if(isGlobal){
                    mipsSegment.li(register, dataAddress + staticOffset * 4);
                }else{
                    mipsSegment.addi(register, Register.getSp() ,-staticOffset * 4);
                }
                mipsSegment.add(register, register, offsetRegister);
                change(register, addressValue);
                return register;
            }
        } else {
            assert addressValue instanceof PointerValue;
            Register register = getReg(false);
            mipsSegment.lw(register, Register.getSp(),-staticOffset * 4);
            change(register, addressValue);
            if(addressValue.getOffset() instanceof Constant){
                mipsSegment.addi(register, register, ((Constant) addressValue.getOffset()).getNumber() * 4);
                change(register, addressValue);
            }else {
                assert addressValue.getOffset() instanceof LValue;
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                change(offsetRegister, offset);
                mipsSegment.add(register, register, offsetRegister);
                change(register, addressValue);
            }
            return register;
        }
    }

    void saveAllVariable() {
        localActive.getAllValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .forEach(this::storeValue);
    }
}
