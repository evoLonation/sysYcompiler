package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Temp;
import midcode.value.Variable;
import util.Execution;

import java.util.*;

// 保存基本快中寄存器和栈内存的使用信息
public class RegisterAllocator {
    private final LocalActive localActive;
    private final MipsSegment mipsSegment;
    private final ValueGetter valueGetter = ValueGetter.getInstance();

    private static final int regNum = 5;
    private final Map<Register, Set<LValue>> registerDescriptors = new HashMap<>();
    private final Map<LValue, AddressDescriptor> addressDescriptors = new HashMap<>();
    private int nowMaxOffset;

    static class Register {
        private final int no;

        public Register(int no) {
            this.no = no;
        }
        public int getNo() {
            return no;
        }
        String print(){
            return "$" + no;
        }
    }
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
        String print(){
            if(isGlobal){
                throw new SemanticException();
            }else{
                return  -(offset * 4) + "($sp)";
            }
        }
    }

    static class AddressDescriptor {
        Set<Memory> memories;
        Set<Register> registers;

        AddressDescriptor(Set<Memory> memories, Set<Register> registers) {
            this.memories = memories;
            this.registers = registers;
        }
    }

    RegisterAllocator(BasicBlock basicBlock, LocalActive localActive, MipsSegment mipsSegment, int functionOffset){
        this.localActive = localActive;
        this.mipsSegment = mipsSegment;
        this.nowMaxOffset = functionOffset;
        // init addressDescriptors
        for(Instrument instrument : basicBlock.getInstruments()) {
            for(RValue rValue : valueGetter.getAllValues(instrument)){
                if(rValue instanceof Variable){
                    Set<Memory> memories = new HashSet<>();
                    memories.add(new Memory((Variable) rValue));
                    addressDescriptors.put((LValue) rValue, new AddressDescriptor(memories, new HashSet<>()));
                }else if(rValue instanceof Temp){
                    addressDescriptors.put((LValue) rValue, new AddressDescriptor(new HashSet<>(), new HashSet<>()));
                }
            }
        }
        // init registerDescriptors
        for(int i = 0; i < regNum; i++) {
            registerDescriptors.put(new Register(i + 7), new HashSet<>());
        }
    }


    // 相比于factor，result在这个指令中肯定是要变化的
    Register getResultReg(LValue result, List<RValue> factors, Instrument instrument) {
        // 先需要实现相比于getFactorReg要多考虑的可能性
        // 优先寻找只存放了x的寄存器
        for(Register register : addressDescriptors.get(result).registers){
            if(registerDescriptors.get(register).size() == 1){
                return register;
            }
        }
        // 寻找factor不会再使用且只存放了factor的寄存器
        for(RValue factor : factors){
            if(factor instanceof LValue){
                LValue lValue = (LValue) factor;
                if(!localActive.isStillUseAfter(lValue, instrument)){
                    // 寻找只放了factor的寄存器
                    for(Register register : addressDescriptors.get(lValue).registers){
                        if(registerDescriptors.get(register).size() == 1){
                            return register;
                        }
                    }
                }
            }
        }
        return getFactorReg(result, instrument);
    }

    Register getFactorReg(LValue factor, Instrument instrument){
        return getFactorReg(factor, null, instrument);
    }

    // factor 指 x = y + z 中的y，z
    // factor 在该指令中可能是不变的
    // 还没有将factor存在register中
    /**
     * @param mustKeep nullable 绝对不能分配的寄存器
     */
    Register getFactorReg(LValue factor, Register mustKeep, Instrument instrument){
        AddressDescriptor leftAddressDescriptor = addressDescriptors.get(factor);
        if(!leftAddressDescriptor.registers.isEmpty()){
            return leftAddressDescriptor.registers.iterator().next();
        } else {
            Optional<Register> emptyReg = getEmptyReg();
            if(emptyReg.isPresent()){
                return emptyReg.get();
            }else{
                int minStoreNumber = 0x3fffffff;
                Register minRegister = null;
                for(Map.Entry<Register, Set<LValue>> registerDescriptor : registerDescriptors.entrySet()){
                    Set<LValue> lValues = registerDescriptor.getValue();
                    Register register = registerDescriptor.getKey();
                    if(register == mustKeep) continue;
                    // first check if all lvalue are in another register
                    boolean isAllInAnother = true;
                    for(LValue lValue : lValues){
                        if(addressDescriptors.get(lValue).registers.size() <= 1){
                            isAllInAnother = false;
                            break;
                        }
                    }
                    if(isAllInAnother){
                        return register;
                    }
                    // second check if all lvalue never used after
                    boolean isAllNeverUse = true;
                    for(LValue lValue : lValues) {
                        if(localActive.isStillUse(lValue, instrument)){
                            isAllNeverUse = false;
                            break;
                        }
                    }
                    if(isAllNeverUse){
                        return register;
                    }
                    // 需要计算把该寄存器中的变量都存起来需要几条store指令
                    int storeNumber = 0;
                    for(LValue lValue : lValues){
                        if(addressDescriptors.get(lValue).registers.size() <= 1){
                            storeNumber++;
                        }
                    }
                    if(storeNumber < minStoreNumber){
                        minStoreNumber = storeNumber;
                        minRegister = register;
                    }
                }
                assert minRegister != null;
                for(LValue lValue : registerDescriptors.get(minRegister)){
                    if(addressDescriptors.get(lValue).registers.size() <= 1){
                        storeLValue(lValue);
                    }
                }
                return minRegister;
            }
        }
    }

    // 清除所有与目标寄存器的绑定
    void clearReg(Register register){
        registerDescriptors.get(register).clear();
        for(AddressDescriptor descriptor : addressDescriptors.values()){
            descriptor.registers.remove(register);
        }
    }

    private Optional<Register> getEmptyReg(){
        for(Map.Entry<Register, Set<LValue>> registerDescriptor : registerDescriptors.entrySet()){
            if(registerDescriptor.getValue().isEmpty()){
                return Optional.of(registerDescriptor.getKey());
            }
        }
        return Optional.empty();
    }

    private void storeLValue(LValue lValue){
        if(lValue instanceof Temp){
            storeTemp((Temp) lValue);
        }else {
            assert lValue instanceof Variable;
            storeVariable((Variable) lValue);
        }
    }

    private void storeTemp(Temp temp){
        Register register = addressDescriptors.get(temp).registers.iterator().next();
        Memory memory = new Memory(nowMaxOffset, false);
        addSaveInstrument(register, memory, temp);
        addressDescriptors.get(temp).memories.add(memory);
        nowMaxOffset += 1;
    }

    private void storeVariable(Variable variable){
        Register register = addressDescriptors.get(variable).registers.iterator().next();
        Memory memory = new Memory(variable);
        addSaveInstrument(register, memory, variable);
        addressDescriptors.get(variable).memories.add(memory);
    }

    boolean isRegContain(Register register, LValue lValue){
        return registerDescriptors.get(register).contains(lValue);
    }

    // if lvalue in register, do nothing
    void loadLValue(LValue lValue, Register register){
        if(isRegContain(register, lValue)){
            return;
        }
        Memory memory = addressDescriptors.get(lValue).memories.iterator().next();
        addLoadInstrument(register, memory, lValue);
        clearReg(register);
        linkValueReg(register, lValue);
    }

    private void linkValueReg(Register register, LValue lValue){
        registerDescriptors.get(register).add(lValue);
        addressDescriptors.get(lValue).registers.add(register);
    }



    // lvalue 变化了，并且变化后的新值在register寄存器中
    void defLValue(LValue lValue, Register register) {
        addressDescriptors.get(lValue).memories.clear();
        addressDescriptors.get(lValue).registers.clear();
        for(Set<LValue> lValues : registerDescriptors.values()){
            lValues.remove(lValue);
        }

        linkValueReg(register, lValue);
    }

    void addLoadInstrument(Register register, Memory memory, LValue lValue){
        if(Generator.DEBUG){
            mipsSegment.addInstrument("lw " + register.print() + ", " + memory.print() + "  : load " + lValue.print() + " to " + register.print());
        }else{
            mipsSegment.addInstrument("lw " + register.print() + ", " + memory.print());
        }
    }
    void addSaveInstrument(Register register, Memory memory, LValue lValue){
        if(Generator.DEBUG){
            mipsSegment.addInstrument("sw " + register.print() + ", " + memory.print() + "  : save " + register.print() + " to " + lValue.print());
        }else{
            mipsSegment.addInstrument("sw " + register.print() + ", " + memory.print());
        }

    }


}
