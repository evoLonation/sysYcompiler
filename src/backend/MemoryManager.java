//package backend;
//
//import common.SemanticException;
//import midcode.MidCode;
//import midcode.value.*;
//
//public class MemoryManager {
//    private final RegisterManager registerManager;
//    private final MipsSegment mipsSegment;
//    private final LocalActive localActive;
//
//    public MemoryManager(RegisterManager registerManager, MipsSegment mipsSegment, LocalActive localActive) {
//        this.registerManager = registerManager;
//        this.mipsSegment = mipsSegment;
//        this.localActive = localActive;
//    }
//
//    private void storeLValue(LValue lValue) {
//        if(registerManager.getMemory(lValue).isPresent()) return;
//        Register register = registerManager.getRegister(lValue).orElseThrow(SemanticException::new);
//        RegisterManager.Memory memory;
//        if(lValue instanceof Temp){
//            memory = registerManager.getNewMemory();
//        }else {
//            memory = registerManager.getVariableMemory((Variable) lValue);
//        }
//        if(memory.isGlobal){
//            mipsSegment.sw(register, registerManager.getDataAddress() + memory.offset * 4);
//        }else{
//            mipsSegment.sw(register, Register.getSp(), -memory.offset * 4);
//        }
//        registerManager.store(register, memory);
//    }
//
//    Register loadLValue(LValue lValue){
//        return registerManager.getRegister(lValue).orElseGet(()->{
//            RegisterManager.Memory memory = registerManager.getMemory(lValue).orElseThrow(SemanticException::new);
//            Register register = registerManager.getFactorReg(lValue);
//            registerManager.getValues(register).stream().filter(value -> value instanceof LValue)
//                    .forEach(value -> storeLValue((LValue) value));
//            if(memory.isGlobal){
//                mipsSegment.lw(register, registerManager.getDataAddress() + memory.offset * 4);
//            }else{
//                mipsSegment.lw(register, Register.getSp(), -memory.offset * 4);
//            }
//            registerManager.load(memory, register);
//            return register;
//        });
//    }
//
//    Register loadNumber(int number) {
//        Register register = registerManager.getReg(false);
//        mipsSegment.li(register, number);
//        registerManager.getValues(register).stream().filter(value -> value instanceof LValue)
//                .forEach(value -> storeLValue((LValue) value));
//        registerManager.change(register);
//        return register;
//    }
//
//    // todo 数组每个元素的地址是从小到大逐渐变大的，因此在栈里也要从小到大排列
//    /**
//     * pointer value store in temp register
//     */
//    Register loadAddressValue(AddressValue addressValue){
//        int staticOffset = addressValue.getStaticOffset();
//        if(addressValue instanceof ArrayValue){
//            boolean isGlobal = ((ArrayValue) addressValue).isGlobal();
//            if(addressValue.getOffset() instanceof Constant){
//                Register register = registerManager.getReg(false);
//                // li %s 0x1000f0+staticOffset
//                if(isGlobal){
//                    mipsSegment.li(register, registerManager.getDataAddress() + (staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
//                } else{
//                    mipsSegment.addi(register, Register.getSp(), + -(staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
//                }
//                registerManager.change(register, addressValue);
//                return register;
//            }else {
//                assert addressValue.getOffset() instanceof LValue;
//                // li %s 0x1000 + staticOffset
//                // addi %s, %s, %s
//                LValue offset = (LValue) addressValue.getOffset();
//                Register offsetRegister = loadLValue(offset);
//                mipsSegment.sll(offsetRegister, offsetRegister, 2);
//                registerManager.change(offsetRegister, offset);
//
//                Register register = registerManager.getReg(false);
//                if(isGlobal){
//                    mipsSegment.li(register, registerManager.getDataAddress() + staticOffset * 4);
//                }else{
//                    mipsSegment.addi(register, Register.getSp() ,-staticOffset * 4);
//                }
//                mipsSegment.add(register, register, offsetRegister);
//                registerManager.change(register, addressValue);
//                return register;
//            }
//        } else {
//            assert addressValue instanceof PointerValue;
//            Register register = registerManager.getReg(false);
//            mipsSegment.lw(register, Register.getSp(),-staticOffset * 4);
//            registerManager.change(register, addressValue);
//            if(addressValue.getOffset() instanceof Constant){
//                mipsSegment.addi(register, register, ((Constant) addressValue.getOffset()).getNumber() * 4);
//                registerManager.change(register, addressValue);
//            }else {
//                assert addressValue.getOffset() instanceof LValue;
//                LValue offset = (LValue) addressValue.getOffset();
//                Register offsetRegister = loadLValue(offset);
//                mipsSegment.sll(offsetRegister, offsetRegister, 2);
//                registerManager.change(offsetRegister, offset);
//                mipsSegment.add(register, register, offsetRegister);
//                registerManager.change(register, addressValue);
//            }
//            return register;
//        }
//    }
//
//    void saveAllVariable() {
//        localActive.getAllValues().stream().filter(lValue -> lValue instanceof Variable)
//                .forEach(value -> storeLValue((LValue) value));
//    }
//}
