package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.MidCode;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Call;
import midcode.instrument.Instrument;
import midcode.value.*;
import util.Execution;

import java.util.*;

// 保存基本快中寄存器和栈内存的使用信息
public class RegisterAllocator {
    private final LocalActive localActive;
    private final MipsSegment mipsSegment;

    private static final int regNum = 5;
    private final Map<Register, Set<LValue>> registerDescriptors = new HashMap<>();
    private final Map<LValue, AddressDescriptor> addressDescriptors = new HashMap<>();
    private final Set<Variable> variables = new HashSet<>();
    private int nowMaxOffset;
    private final Register tempRegister = new Register(24);
    // 用于计算指针时左移两位
    private final Register tempRegister2 = new Register(25);
    private final int dataAddress = 0x10010000;
    private final Register stackRegister = new Register(29);
    private final Register returnRegister = new Register(2);

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
        Memory memory;
        Set<Register> registers;

        AddressDescriptor(Memory memory, Set<Register> registers) {
            this.memory = memory;
            this.registers = registers;
        }
    }

    RegisterAllocator(BasicBlock basicBlock, LocalActive localActive, MipsSegment mipsSegment, int functionOffset){
        this.localActive = localActive;
        this.mipsSegment = mipsSegment;
        this.nowMaxOffset = functionOffset;
        // init addressDescriptors
        for(Instrument instrument : basicBlock.getInstruments()) {
            ValueGetter valueGetter = ValueGetter.getInstance();
            for(RValue rValue : valueGetter.getAllValues(instrument)){
                if(rValue instanceof Variable){
                    addressDescriptors.put((LValue) rValue, new AddressDescriptor(new Memory((Variable) rValue), new HashSet<>()));
                    variables.add((Variable) rValue);
                }else if(rValue instanceof Temp){
                    addressDescriptors.put((LValue) rValue, new AddressDescriptor(null, new HashSet<>()));
                }
            }
        }
        // init registerDescriptors
        for(int i = 0; i < regNum; i++) {
            registerDescriptors.put(new Register(i + 7), new HashSet<>());
        }
    }


    // 相比于factor，result在这个指令中肯定是要变化的

    /**
     * @return 为存储结果的地方分配一个寄存器，保证该寄存器中的其他值：
     * 要不就是 被操作数
     * 要不就是 在别的地方存着
     */
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

    Register getFactorReg(LValue factor, MidCode midCode){
        return getFactorReg(factor, null, midCode);
    }

    // factor 指 x = y + z 中的y，z
    // factor 在该指令中是被操作数
    // 还没有将factor存在register中
    /**
     * @param mustKeep nullable 绝对不能分配的寄存器
     * @return 分配一个寄存器，保证该寄存器中的其他值在别的地方存着
     */
    Register getFactorReg(LValue factor, Register mustKeep, MidCode midCode){
        AddressDescriptor leftAddressDescriptor = addressDescriptors.get(factor);
        if(!leftAddressDescriptor.registers.isEmpty()){
            // todo 如果factor要改变了，寄存器里的其他值怎么办？
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
                        if(localActive.isStillUse(lValue, midCode)){
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
                        AddressDescriptor addressDescriptor = addressDescriptors.get(lValue);
                        if(addressDescriptor.registers.size() <= 1 && addressDescriptor.memory == null){
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
    // todo 如果没地方了需要存在内存里？
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
        if(addressDescriptors.get(lValue).memory != null) return;
        Register register = addressDescriptors.get(lValue).registers.iterator().next();
        Memory memory;
        if(lValue instanceof Temp){
            Temp temp = (Temp) lValue;
            memory = new Memory(nowMaxOffset, false);
            nowMaxOffset += 1;
        }else {
            assert lValue instanceof Variable;
            Variable variable = (Variable) lValue;
            memory = new Memory(variable);
        }
        addSaveInstrument(register, memory, lValue);
        addressDescriptors.get(lValue).memory = memory;
    }

    boolean isRegContain(Register register, LValue lValue){
        return registerDescriptors.get(register).contains(lValue);
    }

    // if lvalue in register, do nothing
    void loadLValue(LValue lValue, Register register){
        if(isRegContain(register, lValue)){
            return;
        }
        Memory memory = addressDescriptors.get(lValue).memory;
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
        addressDescriptors.get(lValue).memory = null;
        addressDescriptors.get(lValue).registers.clear();
        for(Set<LValue> lValues : registerDescriptors.values()){
            lValues.remove(lValue);
        }

        linkValueReg(register, lValue);
    }

    void saveAllVariable(){
        for(Variable variable : variables){
            if(addressDescriptors.get(variable).memory == null){
                storeLValue(variable);
            }
        }
    }
    Register findLValue(LValue lValue, MidCode midCode){
        if(addressDescriptors.get(lValue).registers.isEmpty()){
            loadLValue(lValue, getFactorReg(lValue, midCode));
        }
        return addressDescriptors.get(lValue).registers.iterator().next();
    }

    /**
     * @param lValue just for debug
     */
    void addLoadInstrument(Register register, Memory memory, LValue lValue){
        if(Generator.DEBUG){
            mipsSegment.addInstrument("lw " + register.print() + ", " + memory.print() + "  : load " + lValue.print() + " to " + register.print());
        }else{
            mipsSegment.addInstrument("lw " + register.print() + ", " + memory.print());
        }
    }

    /**
     * @param lValue just for debug
     */
    void addSaveInstrument(Register register, Memory memory, LValue lValue){
        if(Generator.DEBUG){
            mipsSegment.addInstrument("sw " + register.print() + ", " + memory.print() + "  : save " + register.print() + " to " + lValue.print());
        }else{
            mipsSegment.addInstrument("sw " + register.print() + ", " + memory.print());
        }
    }



    /**
     * pointer value store in temp register
     */
    Register getPointerValue(PointerValue pointerValue, Instrument pointerInstrument){
        int staticOffset = pointerValue.getStaticOffset();
        if(pointerValue.isGlobal()){
            if(pointerValue.getOffset() instanceof Constant){
                // li %s 0x1000f0+staticOffset
                mipsSegment.addInstrument(String.format("li %s, %d", tempRegister.print(), dataAddress + (staticOffset + ((Constant) pointerValue.getOffset()).getNumber()) * 4));
            }else {
                assert pointerValue.getOffset() instanceof LValue;
                // li %s 0x1000 + staticOffset
                // addi %s, %s, %s
                LValue offset = (LValue) pointerValue.getOffset();
                Register offsetRegister = getFactorReg(offset, pointerInstrument);
                loadLValue(offset, offsetRegister);
                mipsSegment.addInstrument(String.format("sll %s, %s, 2", tempRegister2.print(), offsetRegister.print()));
                mipsSegment.addInstrument(String.format("li %s, %d", tempRegister.print(), dataAddress + staticOffset * 4));
                mipsSegment.addInstrument(String.format("add %s, %s, %s", tempRegister.print(), tempRegister.print(), tempRegister2.print()));
            }
        }else{
            if(pointerValue.getType() == PointerValue.Type.array){
                if(pointerValue.getOffset() instanceof Constant){
                    mipsSegment.addInstrument(String.format("addi %s, %s, %d", tempRegister.print(), stackRegister.print(), staticOffset + (((Constant) pointerValue.getOffset()).getNumber()) * 4));
                }else {
                    assert pointerValue.getOffset() instanceof LValue;
                    LValue offset = (LValue) pointerValue.getOffset();
                    Register offsetRegister = getFactorReg(offset, pointerInstrument);
                    loadLValue(offset, offsetRegister);
                    mipsSegment.addInstrument(String.format("sll %s, %s, 2", tempRegister2.print(), offsetRegister.print()));
                    mipsSegment.addInstrument(String.format("addi %s, %s, %d", tempRegister.print(), stackRegister.print(), staticOffset * 4));
                    mipsSegment.addInstrument(String.format("add %s, %s, %s", tempRegister.print(), tempRegister.print(), tempRegister2.print()));
                }
            }else{
                mipsSegment.addInstrument(String.format("lw %s, %d(%s)", tempRegister.print(), staticOffset * 4, stackRegister.print()));
                if(pointerValue.getOffset() instanceof Constant){
                    mipsSegment.addInstrument(String.format("addi %s, %s, %d", tempRegister.print(), tempRegister.print(), ((Constant) pointerValue.getOffset()).getNumber() * 4));
                }else {
                    assert pointerValue.getOffset() instanceof LValue;
                    LValue offset = (LValue) pointerValue.getOffset();
                    Register offsetRegister = getFactorReg(offset, pointerInstrument);
                    loadLValue(offset, offsetRegister);
                    mipsSegment.addInstrument(String.format("sll %s, %s, 2", tempRegister2.print(), offsetRegister.print()));
                    mipsSegment.addInstrument(String.format("add %s, %s, %s", tempRegister.print(), tempRegister.print(), tempRegister2.print()));
                }
            }
        }
        return tempRegister;
    }

    void storeParam(int index, Register register){
        addSaveInstrument(register, new Memory(nowMaxOffset + index, false), null);
    }

    void pushSp(){
        mipsSegment.addInstrument(String.format("addi %s, %s, %d", stackRegister.print(), stackRegister.print(), -((nowMaxOffset + 1) * 4)));
    }
    void popSp(){
        mipsSegment.addInstrument(String.format("addi %s, %s, %d", stackRegister.print(), stackRegister.print(), ((nowMaxOffset + 1) * 4)));
    }
}
