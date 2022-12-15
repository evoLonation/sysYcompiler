package backend;

import common.SemanticException;
import midcode.value.*;

import java.util.Collections;
import java.util.Set;

public class StoreLoadManager {
    private final StateManager stateManager;
    private final MipsSegment mipsSegment = MipsSegment.getInstance();

    public StoreLoadManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }
    void storeLValue(LValue value) {
//        mipsSegment.comment(String.format("store %s", value.print()));
        if(stateManager.isStore(value)) return;
        Register register = stateManager.anyRegister(value).orElseThrow(() -> new SemanticException(value.toString()));
        StateManager.Memory memory = stateManager.store(value);
        if(memory.isGlobal){
            mipsSegment.sw(register, StateManager.getDataAddress() + memory.offset * 4);
        }else{
            mipsSegment.sw(register, Register.getSp(), -memory.offset * 4);
        }
    }


    //todo 请及时为返回的寄存器分配一个值（它当前什么值都没有分配）
    Register loadRValue(RValue value){
        if(value instanceof Constant){
            Register register = stateManager.getReg();
            stateManager.getNeedStore(register, false).forEach(this::storeLValue);
            mipsSegment.li(register, ((Constant) value).getNumber());
            stateManager.change(register);
            return register;
        }else{
            return loadLValue((LValue) value);
        }
    }

    Register loadLValue(LValue useValue){
        return loadLValue(useValue, false);
    }

    Register loadLValue(LValue useValue, boolean overrideCurrentUse) {
//        mipsSegment.comment(String.format("load %s", value.print()));
        return stateManager.anyRegister(useValue).orElseGet(() -> {
            StateManager.Memory memory = stateManager.getMemory(useValue).orElseThrow(SemanticException::new);
            Register register = stateManager.getReg(overrideCurrentUse);
            stateManager.getNeedStore(register, overrideCurrentUse).forEach(this::storeLValue);
            if(memory.isGlobal){
                mipsSegment.lw(register, StateManager.getDataAddress() + memory.offset * 4);
            }else{
                mipsSegment.lw(register, Register.getSp(), -memory.offset * 4);
            }
            stateManager.load(register, useValue);
            return register;
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

    // todo bug： 返回的address的register有可能是非通用寄存器
    /**
     * @return 保证返回的register是可用的（即没有要保存起来的值）
     */
    private Address getAddress(AddressValue addressValue){
        int staticOffset = addressValue.getStaticOffset();
        if(addressValue instanceof ArrayValue){
            boolean isGlobal = ((ArrayValue) addressValue).isGlobal();
            if(addressValue.getOffset() instanceof Constant){
                // li %s 0x1000f0+staticOffset
                if(isGlobal){
                    return new Address(StateManager.getDataAddress() + (staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                } else{
                    return new Address(Register.getSp(), (-staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                }
            }else {
                assert addressValue.getOffset() instanceof LValue;
                // li %s 0x1000 + staticOffset
                // addi %s, %s, %s
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadLValue(offset);
                stateManager.setBusy(offsetRegister);
                Register realRegister = stateManager.getReg(false);
                stateManager.setFree(offsetRegister);
                stateManager.getNeedStore(realRegister, false).forEach(this::storeLValue);
                mipsSegment.sll(realRegister, offsetRegister, 2);
                stateManager.change(realRegister);
                if(isGlobal){
                    return new Address(realRegister, StateManager.getDataAddress() + staticOffset * 4);
                }else{
                    mipsSegment.add(realRegister, Register.getSp(), realRegister);
                    stateManager.change(realRegister);
                    return new Address(realRegister, -staticOffset*4);
                }
            }
        } else {
            assert addressValue instanceof PointerValue;
            Register register = stateManager.getReg(false);
            stateManager.getNeedStore(register, false).forEach(this::storeLValue);
            mipsSegment.lw(register, Register.getSp(),-staticOffset * 4);
            stateManager.change(register);
            if(addressValue.getOffset() instanceof Constant){
                return new Address(register, ((Constant) addressValue.getOffset()).getNumber() * 4);
            }else {
                assert addressValue.getOffset() instanceof LValue;
                LValue offset = (LValue) addressValue.getOffset();
                stateManager.setBusy(register);
                Register offsetRegister = loadLValue(offset);
                Register realRegister = stateManager.getReg(false);
                stateManager.setFree(register);
                stateManager.getNeedStore(realRegister, false).forEach(this::storeLValue);
                mipsSegment.sll(realRegister, offsetRegister, 2);
                stateManager.change(realRegister);
                mipsSegment.add(register, register, realRegister);
                stateManager.change(register);
                return new Address(register);
            }
        }
    }

    /**
     * pointer value store in temp register
     */
    Register loadAddress(AddressValue addressValue){
        Address address = getAddress(addressValue);
        if(address.register == null){
            Register register = stateManager.getReg(false);
            stateManager.getNeedStore(register, false).forEach(this::storeLValue);
            mipsSegment.li(register, address.offset);
            stateManager.change(register);
            return register;
        }else{
            // 注意，address.register可能是sp，这时不能随便用
            Register register = stateManager.getReg(false);
            stateManager.getNeedStore(register, false).forEach(this::storeLValue);
            mipsSegment.addi(register, address.register, address.offset);
            stateManager.change(register);
            return register;
        }
    }

    Register loadFromAddressValue(AddressValue addressValue, boolean overrideCurrentUse){
        Address address = getAddress(addressValue);
        Register register = stateManager.getReg(overrideCurrentUse);
        stateManager.getNeedStore(register, overrideCurrentUse).forEach(this::storeLValue);
        if(address.register == null) {
            address.register = Register.getZero();
        }
        mipsSegment.lw(register, address.register, address.offset);
        stateManager.change(register);
        return register;
    }

    void storeToAddressValue(Register register, AddressValue addressValue){
        Address address = getAddress(addressValue);
        if(address.register == null) {
            address.register = Register.getZero();
        }
        mipsSegment.sw(register, address.register, address.offset);
    }


    Register loadValue(Value value){
        if(value instanceof AddressValue){
            return loadAddress((AddressValue) value);
        }else{
            assert value instanceof RValue;
            return loadRValue((RValue) value);
        }
    }
}
