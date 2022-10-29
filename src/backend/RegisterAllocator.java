package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.instrument.UnaryOperation;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Temp;
import midcode.value.Variable;
import util.Execution;
import util.VoidExecution;

import java.util.*;

// 保存基本快中寄存器和栈内存的使用信息
// todo 现在只是假设所有variable都是局部变量，没有考虑全局变量
public class RegisterAllocator {
    private final LocalActive activeInfo;
    private final MipsSegment mipsSegment;

    private static final int regNum = 10;
    private final List<Set<LValue>> registerDescriptors = new ArrayList<>(regNum);
    //todo need init
    private final Map<LValue, AddressDescriptor> addressDescriptors = new HashMap<>();
    private int nowMaxOffset;


    RegisterAllocator(BasicBlock basicBlock, LocalActive activeInfo, MipsSegment mipsSegment){
        this.activeInfo = activeInfo;
        this.mipsSegment = mipsSegment;
        variableInitial.inject();
        for(Instrument instrument : basicBlock.getInstruments()) {
            variableInitial.exec(instrument);
        }
        for(int i = 0; i < regNum; i++){
            registerDescriptors.add(new HashSet<>());
        }
        getRegExecution.inject();
    }

    private final VoidExecution<Instrument> variableInitial = new VoidExecution<Instrument>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class, instrument -> addVariable(instrument.getResult(), instrument.getRight(), instrument.getLeft()));
            inject(UnaryOperation.class, param -> addVariable(param.getValue(), param.getResult()));
        }
    };

    private void addVariable(RValue... rValues){
        for(RValue rValue : rValues){
            if(rValue instanceof Variable){
                Set<Integer> memories = new HashSet<>();
                memories.add(((Variable) rValue).getOffset());
                addressDescriptors.put((LValue) rValue, new AddressDescriptor(memories, new HashSet<>()));
            }else if(rValue instanceof Temp){
                addressDescriptors.put((LValue) rValue, new AddressDescriptor(new HashSet<>(), new HashSet<>()));
            }
        }
    }

    static class AddressDescriptor {
        Set<Integer> memories;
        Set<Integer> registers;

        AddressDescriptor(Set<Integer> memories, Set<Integer> registers) {
            this.memories = memories;
            this.registers = registers;
        }
    }


    Map<LValue, Integer> getReg(Instrument instrument){
        return getRegExecution.exec(instrument);
    }



    // 注意，这个操作并不改变寄存器描述符本身，只是找到一个寄存器
    private final Execution<Instrument, Map<LValue, Integer>> getRegExecution = new Execution<Instrument, Map<LValue, Integer>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class, operation -> {
                Map<LValue, Integer> ret = new HashMap<>();
                LValue result = operation.getResult();
                RValue left = operation.getLeft();
                RValue right = operation.getRight();
                // choose left
                if(left instanceof LValue){
                    ret.put((LValue) left, getFactorReg((LValue) left, result, right, operation));
                }
                if(right instanceof LValue) {
                    ret.put((LValue) right, getFactorReg((LValue) right, result, left, operation));
                }
                ret.put(result, getResultReg(result, left, right, operation));
                return ret;
            });

            inject(Assignment.class, assign -> {
                Map<LValue, Integer> ret = new HashMap<>();
                LValue left = assign.getLeft();
                RValue right = assign.getRight();
                if(right instanceof LValue){
                    int no = getFactorReg((LValue) right, left, null, assign);
                    ret.put((LValue) right, no);
                    ret.put(left, no);
                }else{
                    ret.put(left, getResultReg(left, null, null, assign));
                }
                return ret;
            });
        }
    };

    private int getResultReg(LValue result, RValue firstFactor, RValue secondFactor, Instrument instrument){
        // 先需要实现相比于getFactorReg要多考虑的可能性
        // 寻找只存放了x的寄存器
        for(Integer no : addressDescriptors.get(result).registers){
            if(registerDescriptors.get(no).size() == 1){
                return no;
            }
        }
        if(firstFactor instanceof LValue && !activeInfo.isActiveAfter((LValue) firstFactor, instrument)){
            // 寻找只放了y的寄存器
            for(Integer no : addressDescriptors.get(firstFactor).registers){
                if(registerDescriptors.get(no).size() == 1){
                    return no;
                }
            }
        }
        if(secondFactor instanceof LValue && !activeInfo.isActiveAfter((LValue) secondFactor, instrument)){
            // 寻找只放了z的寄存器
            for(Integer no : addressDescriptors.get(secondFactor).registers){
                if(registerDescriptors.get(no).size() == 1){
                    return no;
                }
            }
        }
        return getFactorReg(result, null, null, instrument);
    }

    // factor 指 x = y + z 中的y，z
    // other factor and result is nullable
    private int getFactorReg(LValue factor, LValue result, RValue otherFactor, Instrument instrument){
        if(!(otherFactor instanceof LValue)){
            otherFactor = null;
        }
        AddressDescriptor leftAddressDescriptor = addressDescriptors.get(factor);
        if(leftAddressDescriptor.registers.isEmpty()){
            Optional<Integer> emptyReg = getEmptyReg();
            if(emptyReg.isPresent()){
                return emptyReg.get();
            }else{
                int minStoreNumber = 0x3fffffff;
                int minRegisterNo = -1;
                for(int i = 0; i < registerDescriptors.size(); i++){
                    LValue lValue = registerDescriptors.get(i).iterator().next();
                    if(addressDescriptors.get(lValue).registers.size() > 1 || !addressDescriptors.get(lValue).memories.isEmpty()){
                        return i;
                    }else if(lValue == result && result != otherFactor){
                        return i;
                    } else if(!activeInfo.isActiveAfter(factor, instrument)) {
                        return i;
                    } else{
                        // 需要计算把该寄存器中的变量都存起来需要几条store指令
                        int storeNumber = registerDescriptors.get(i).size();
                        if(storeNumber < minStoreNumber){
                            minStoreNumber = storeNumber;
                            minRegisterNo = i;
                        }
                    }
                }
                for(LValue needStore : registerDescriptors.get(minRegisterNo)){
                    if(needStore instanceof Variable){
                        storeVariable((Variable) needStore);
                    }else if(needStore instanceof Temp){
                        storeTemp((Temp) needStore);
                    }else {
                        throw new SemanticException();
                    }
                }
                return minRegisterNo;
            }
        }else{
            return leftAddressDescriptor.registers.iterator().next();
        }
    }

    private Optional<Integer> getEmptyReg(){
        int index = 0;
        for(Set<LValue> lValues : registerDescriptors){
            if(lValues.isEmpty()){
                return Optional.of(index);
            }
            index ++;
        }
        return Optional.empty();
    }

    private void storeTemp(Temp temp){
        int registerNo = addressDescriptors.get(temp).registers.iterator().next();
        String address = -(nowMaxOffset * 4) + "($sp)";
        mipsSegment.addInstrument("sw " + getRealRegister(registerNo) + ", " + address);
        addressDescriptors.get(temp).memories.add(nowMaxOffset);
        nowMaxOffset += 1;
    }

    private void storeVariable(Variable variable){
        int registerNo = addressDescriptors.get(variable).registers.iterator().next();
        String address = -(variable.getOffset() * 4) + "($sp)";
        mipsSegment.addInstrument("sw " + getRealRegister(registerNo) + ", " + address);
        addressDescriptors.get(variable).memories.add(nowMaxOffset);
    }

    String getRealRegister(int no){
        if(no <= 7){
            return "$" + (no + 8);
        }else {
            return "$" + (no + 16);
        }
    }

    boolean isRegContain(int register, LValue lValue){
        return registerDescriptors.get(register).contains(lValue);
    }

    // if lvalue in register, do nothing
    void loadLValue(LValue lValue, int register){
        if(isRegContain(register, lValue)){
            return;
        }
        int memory = addressDescriptors.get(lValue).memories.iterator().next();
        String address = -(memory * 4) + "($sp)";
        mipsSegment.addInstrument("lw " + getRealRegister(register) + ", " + address);
        registerDescriptors.get(register).clear();
        registerDescriptors.get(register).add(lValue);
    }

    void changeLValue(LValue lValue, int register) {
        registerDescriptors.get(register).clear();
        registerDescriptors.get(register).add(lValue);
        addressDescriptors.get(lValue).memories.clear();
        addressDescriptors.get(lValue).registers.clear();
        addressDescriptors.get(lValue).registers.add(register);
    }


}
