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
    private final MipsSegment mipsSegment = MipsSegment.getInstance();


    // 包括lvalue和addressValue
    private final Map<Register, Set<Value>> registerDescriptors;
    // 包括lvalue和addressValue
    private final Map<Value, Set<Register>> valueRegisterMap;

    private final Map<Value, Memory> valueMemoryMap;

    private final Set<Memory> otherMemories;
    private final Map<Variable, Memory> variableMemories;


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


    StorageManager(LocalActive localActive, int offset){
        this.localActive = localActive;
        this.nowMaxOffset = offset;
        this.variableMemories = new HashMap<>();
        this.valueMemoryMap = new HashMap<>();
        this.otherMemories = new HashSet<>();
        localActive.getAllValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .forEach(value -> {
                    Memory variableMemory = new Memory((Variable) value);
                    variableMemories.put((Variable) value, variableMemory);
                    valueMemoryMap.put(value, variableMemory);
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



    private void bind(Register register, Value value) {
        registerDescriptors.get(register).add(value);
        valueRegisterMap.get(value).add(register);
    }

    private void unBind(Register register){
        registerDescriptors.get(register).clear();
        valueRegisterMap.values().forEach(registers -> registers.remove(register));
    }

    private void store(Value value){
        if(value instanceof Variable){
            valueMemoryMap.put(value, variableMemories.get((Variable) value));
        }else{
            if(!valueMemoryMap.containsKey(value)){
                // 查找空闲tempMemory
                Memory tempMemory =
                        otherMemories.stream()
                                .filter(memory -> !valueMemoryMap.containsValue(memory)).findAny()
                                .orElseGet(() -> new Memory(nowMaxOffset++, false));
                valueMemoryMap.put(value, tempMemory);
            }
        }
    }

    private void unBind(Value value){
        valueRegisterMap.get(value).clear();
        registerDescriptors.values().forEach(lValues -> lValues.remove(value));
        valueMemoryMap.remove(value);
    }

    private boolean isStore(Value value){
        return valueMemoryMap.containsKey(value);
    }

    private Optional<Register> anyRegister(Value value){
        return valueRegisterMap.get(value).stream().findAny();
    }

    /**
     * @return 获得寄存器中从下一条中间指令开始还需要使用的且没在内存中的值的集合
     */
    private Set<Value> getNeedStore(Register register){
        return registerDescriptors.get(register).stream()
                .filter(value -> !isStore(value))
                .filter(value -> localActive.isStillUse(value, true))
                .collect(Collectors.toSet());
    }

    // 寄存器经过指令操作被改变，用来表征value这个值
    private void operate(Register register, Value value){
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
    }

    /**
     * 仅仅用于为一个中间代码的def值分配寄存器时！因为该方法会考虑分配当前中间代码的use值
     * @return 启发式的为目标指令要定义的值（产生的结果）分配一个寄存器
     * 值得注意的是，本指令中正在使用的寄存器只要之后不再使用就是允许的
     */
    private Register getResultReg() {
        return getReg(true);
    }

    /**
     * 启发式的为当前中间代码的一个use值分配一个寄存器
     */
    private Register getFactorReg(Value factor) {
        assert valueGetter.getUseValues(localActive.getNowMidCode()).contains(factor);
        // 先找已经有的
        return anyRegister(factor).orElseGet(() -> getReg(false));
    }

    /**
     * 分配一个寄存器，该寄存器可以直接与其他值绑定，因为寄存器存的值已经存到内存了
     * @param couldBeCurrentUse 指是否可以从当前指令的use值中拿寄存器
     */
    private Register getReg(boolean couldBeCurrentUse){
        Register register = registerDescriptors.entrySet().stream()
                .filter(entry-> {
                    // 所有的value从这条指令之后都不再使用
                    return entry.getValue().stream()
                            .noneMatch(value -> localActive.isStillUse(value, couldBeCurrentUse));
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
                    couldBeCurrentUse ||
                            // 在所有的值都不包含在本指令要使用的值中时，保留
                            entry.getValue().stream()
                                    .noneMatch(value -> valueGetter.getUseValues(localActive.getNowMidCode()).contains(value))
            ).map(Map.Entry::getKey)
            .forEach(register1 -> {
                Set<Value> nowNeedStore = getNeedStore(register1);
                if(needStoreReference.get() == null || needStoreReference.get().size() > nowNeedStore.size()){
                    needStoreReference.set(nowNeedStore);
                    registerReference.set(register1);
                }
            });
            needStoreReference.get().forEach(this::storeValue);
            return registerReference.get();
        }
    }


    void storeValue(Value value) {
//        mipsSegment.comment(String.format("store %s", value.print()));
        if(valueMemoryMap.containsKey(value)) return;
        Register register = anyRegister(value).orElseThrow(SemanticException::new);
        store(value);
        Memory memory = valueMemoryMap.get(value);
        if(memory.isGlobal){
            mipsSegment.sw(register, dataAddress + memory.offset * 4);
        }else{
            mipsSegment.sw(register, Register.getSp(), -memory.offset * 4);
        }
    }

    Register loadValue(Value useValue) {
//        mipsSegment.comment(String.format("load %s", value.print()));\
        return anyRegister(useValue).orElseGet(() -> {
            if(useValue instanceof Constant){
                Register register = getFactorReg(useValue);
                operate(register, useValue);
                mipsSegment.li(register, ((Constant) useValue).getNumber());
                return register;
            }else{
                Memory memory = valueMemoryMap.get(useValue);
                Objects.requireNonNull(memory);
                Register register = getFactorReg(useValue);
                // todo bug: 不应该将usevalue和memory解绑，但是这里的operate解绑了
//                operate(register, useValue);
                unBind(register);
                bind(register, useValue);
                if(memory.isGlobal){
                    mipsSegment.lw(register, dataAddress + memory.offset * 4);
                }else{
                    mipsSegment.lw(register, Register.getSp(), -memory.offset * 4);
                }
                return register;
            }
        });
    }

    private static class Address{
        // nullable
        Register register;
        int offset;

        Address(Register register, int offset) {
            this.register = register;
            this.offset = offset;
        }

        Address(Register register) {
            this.register = register;
        }

        Address(int offset) {
            this.offset = offset;
        }
    }

    private Address getAddress(AddressValue addressValue){
        int staticOffset = addressValue.getStaticOffset();
        if(addressValue instanceof ArrayValue){
            boolean isGlobal = ((ArrayValue) addressValue).isGlobal();
            if(addressValue.getOffset() instanceof Constant){
                // li %s 0x1000f0+staticOffset
                if(isGlobal){
                    return new Address(dataAddress + (staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                } else{
                    return new Address(Register.getSp(), -(staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                }
            }else {
                assert addressValue.getOffset() instanceof LValue;
                // li %s 0x1000 + staticOffset
                // addi %s, %s, %s
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                operate(offsetRegister, offset);

                if(isGlobal){
                    return new Address(offsetRegister, dataAddress + staticOffset * 4);
                }else{
                    Register register = getReg(false);
                    mipsSegment.add(register, Register.getSp() ,offsetRegister);
                    unBind(register);
                    return new Address(register, -staticOffset*4);
                }
            }
        } else {
            assert addressValue instanceof PointerValue;
            Register register = getReg(false);
            mipsSegment.lw(register, Register.getSp(),-staticOffset * 4);
            operate(register, addressValue);
            if(addressValue.getOffset() instanceof Constant){
                return new Address(register, ((Constant) addressValue.getOffset()).getNumber() * 4);
            }else {
                assert addressValue.getOffset() instanceof LValue;
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                operate(offsetRegister, offset);
                mipsSegment.add(register, register, offsetRegister);
                operate(register, addressValue);
                return new Address(register);
            }
        }
    }

    /**
     * pointer value store in temp register
     */
    Register loadAddress(AddressValue addressValue){
        Address address = getAddress(addressValue);
        Register register = getFactorReg(addressValue);
        operate(register, addressValue);
        if(address.register == null){
             mipsSegment.li(register, address.offset);
        }else{
            mipsSegment.addi(register, address.register, address.offset);
        }
        return register;
    }

    Register toComputeValue(Value value){
        Register register = getResultReg();
        operate(register, value);
        return register;
    }

    void saveAllVariable() {
        localActive.getAllValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .forEach(this::storeValue);
    }
    void storeAllValue(){
        localActive.getAllValues().stream()
                .filter(value -> localActive.isStillUse(value, true))
                .filter(value -> !(value instanceof Constant) && !(value instanceof ArrayValue))
                .forEach(this::storeValue);
    }
    void clearRegister(){
        registerDescriptors.forEach(((register, values) -> values.clear()));
        valueRegisterMap.forEach(((value, registers) -> registers.clear()));
    }

    void loadFromAddressValue(Value value, AddressValue addressValue){
        Address address = getAddress(addressValue);
        Register register = toComputeValue(value);
        if(address.register == null) {
            address.register = Register.getZero();
        }
        mipsSegment.lw(register, address.register, address.offset);
    }

    void storeToAddressValue(Value value, AddressValue addressValue){
        Address address = getAddress(addressValue);
        Register register = loadValue(value);
        if(address.register == null) {
            address.register = Register.getZero();
        }
        mipsSegment.sw(register, address.register, address.offset);
    }
}
