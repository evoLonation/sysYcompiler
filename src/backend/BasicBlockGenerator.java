package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.MidCode;
import midcode.instrument.*;
import midcode.value.*;
import util.VoidExecution;

public class BasicBlockGenerator {
    private final MipsSegment mipsSegment;
    private final LocalActive localActive;
    private final RegisterManager registerManager;


    public BasicBlockGenerator(BasicBlock basicBlock, int offset) {
        this.localActive = new LocalActive(basicBlock);
        this.registerManager = new RegisterManager(localActive, offset);
        this.mipsSegment = new MipsSegment(basicBlock.getName());
        instrumentExecution.inject();
        jumpExecution.inject();
    }

    public String generate(){
        localActive.forEach(instrumentExecution::exec, this::saveAllVariable, jumpExecution::exec);
        return mipsSegment.print();
    }



    private final VoidExecution<Instrument> instrumentExecution = new VoidExecution<Instrument>() {
        @Override
        public void inject() {
            inject(param -> {});
            inject(BinaryOperation.class,  param ->{
                int leftConst = 0;
                Register leftRegister = null;
                if(param.getLeft() instanceof LValue){
                    LValue left = (LValue)param.getLeft();
                    leftRegister = loadLValue(left);
                }else{
                    assert param.getLeft() instanceof Constant;
                    leftConst = ((Constant) param.getLeft()).getNumber();
                }
                int rightConst = 0;
                Register rightRegister = null;
                if(param.getRight() instanceof LValue){
                    LValue right = (LValue)param.getRight();
                    rightRegister = loadLValue(right);
                }else{
                    assert param.getRight() instanceof Constant;
                    rightConst = ((Constant) param.getRight()).getNumber();
                }

                LValue resultLValue = param.getResult();
                Register resultReg = registerManager.getResultReg();
                // resultReg中的值要变化，因此它与之前的lvalue都取消了绑定
                assert leftRegister != null || rightRegister != null;
//                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                mipsSegment.debug(param.print());
                if(leftRegister == null || rightRegister == null){
                    Register factor1;
                    int factor2;
                    if(leftRegister == null) {
                        factor1 = rightRegister;
                        factor2 = leftConst;
                    }else{
                        factor1 = leftRegister;
                        factor2 = rightConst;
                    }
                    switch (param.getOp()) {
                        case PLUS:  mipsSegment.addi(resultReg, factor1, factor2); break;
                        case MINU: mipsSegment.addi(resultReg, factor1, -factor2); break;
                        // todo mult 和 div 可以移位计算
                        case MULT: break;
                        default: throw new SemanticException();
                    }
                } else{
                    switch (param.getOp()) {
                        case PLUS: mipsSegment.add(resultReg, leftRegister, rightRegister); break;
                        case MINU: mipsSegment.sub(resultReg, leftRegister, rightRegister); break;
                        case MULT: break;
                        default: throw new SemanticException();
                    }
                }
                registerManager.change(resultReg, resultLValue);
            });

            inject(Assignment.class, param -> {
                Register register;
                LValue left = param.getLeft();
                RValue right = param.getRight();
//                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                mipsSegment.debug(param.print());
                if (right instanceof LValue) {
                    register = loadLValue((LValue) right);
                    registerManager.bindTo(register, left);
                } else {
                    assert right instanceof Constant;
                    register = loadNumber(((Constant) right).getNumber());
                    registerManager.bindTo(register, left);
                }
            });

            inject(Call.class, param -> {
                int index = 0;
                for(Value value : param.getParams()){
                    Register register;
                    mipsSegment.debug(param.print());
                    if(value instanceof AddressValue){
                        register = loadAddressValue((AddressValue) value);
                        mipsSegment.sw(register, Register.getSp(), -(registerManager.getNowMaxOffset()+index) * 4);
                    }else{
                        register = loadLValue((LValue) value);
                        mipsSegment.sw(register, Register.getSp(), -(registerManager.getNowMaxOffset()+index) * 4);
                    }
                    index ++;
                }
                mipsSegment.addi(Register.getSp(), Register.getSp(), -registerManager.getNowMaxOffset() * 4);
                mipsSegment.jal(param.getFunction().getEntry().getName());
                mipsSegment.addi(Register.getSp(), Register.getSp(), registerManager.getNowMaxOffset() * 4);
                param.getRet().ifPresent(temp -> {
                    Register register = registerManager.getResultReg();
                    mipsSegment.addi(register, Register.getV0(), 0);
                    registerManager.change(register, temp);
                });
            });


        }
    };

    private final VoidExecution<Jump> jumpExecution = new VoidExecution<Jump>() {
        @Override
        public void inject() {
            inject(param -> {});
            inject(Goto.class, go -> mipsSegment.j(go.getBasicBlock().getName()));
            inject(CondGoto.class, go -> {
                LValue cond = go.getCond();
                Register register = loadLValue(cond);
                mipsSegment.debug(go.print());
                mipsSegment.bne(register, Register.getZero(), go.getTrueBasicBlock().getName());
                mipsSegment.j(go.getFalseBasicBlock().getName());
            });
            inject(Return.class, param -> {
                mipsSegment.debug(param.print());
                param.getReturnValue().ifPresent(value -> {
                    if(value instanceof Constant){
                        mipsSegment.li(Register.getV0() ,((Constant) value).getNumber());
                    }else{
                        assert value instanceof LValue;
                        Register register = loadLValue((LValue)value);
                        mipsSegment.addi(Register.getV0(), register, 0);
                    }
                });
                mipsSegment.lw(Register.getRa(), Register.getSp(), 4);
                mipsSegment.jr(Register.getRa());
            });
        }
    };


    private void storeLValue(LValue lValue) {
        mipsSegment.debug(String.format("store %s", lValue.print()));
        if(registerManager.getMemory(lValue).isPresent()) return;
        Register register = registerManager.getRegister(lValue).orElseThrow(SemanticException::new);
        RegisterManager.Memory memory;
        if(lValue instanceof Temp){
            memory = registerManager.getNewMemory();
        }else {
            memory = registerManager.getVariableMemory((Variable) lValue);
        }
        if(memory.isGlobal){
            mipsSegment.sw(register, registerManager.getDataAddress() + memory.offset * 4);
        }else{
            mipsSegment.sw(register, Register.getSp(), -memory.offset * 4);
        }
        registerManager.store(register, memory);
    }

    Register loadLValue(LValue lValue){
        mipsSegment.debug(String.format("load %s", lValue.print()));
        return registerManager.getRegister(lValue).orElseGet(()->{
            RegisterManager.Memory memory = registerManager.getMemory(lValue).orElseThrow(SemanticException::new);
            Register register = registerManager.getFactorReg(lValue);
            registerManager.getValues(register).stream().filter(value -> value instanceof LValue)
                    .forEach(value -> storeLValue((LValue) value));
            if(memory.isGlobal){
                mipsSegment.lw(register, registerManager.getDataAddress() + memory.offset * 4);
            }else{
                mipsSegment.lw(register, Register.getSp(), -memory.offset * 4);
            }
            registerManager.load(memory, register);
            return register;
        });
    }

    Register loadNumber(int number) {
        Register register = registerManager.getReg(false);
        mipsSegment.li(register, number);
        registerManager.getValues(register).stream().filter(value -> value instanceof LValue)
                .forEach(value -> storeLValue((LValue) value));
        registerManager.change(register);
        return register;
    }

    // todo 数组每个元素的地址是从小到大逐渐变大的，因此在栈里也要从小到大排列
    /**
     * pointer value store in temp register
     */
    Register loadAddressValue(AddressValue addressValue){
        mipsSegment.debug(String.format("load %s", addressValue.print()));
        int staticOffset = addressValue.getStaticOffset();
        if(addressValue instanceof ArrayValue){
            boolean isGlobal = ((ArrayValue) addressValue).isGlobal();
            if(addressValue.getOffset() instanceof Constant){
                Register register = registerManager.getReg(false);
                // li %s 0x1000f0+staticOffset
                if(isGlobal){
                    mipsSegment.li(register, registerManager.getDataAddress() + (staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                } else{
                    mipsSegment.addi(register, Register.getSp(), + -(staticOffset + ((Constant) addressValue.getOffset()).getNumber()) * 4);
                }
                registerManager.change(register, addressValue);
                return register;
            }else {
                assert addressValue.getOffset() instanceof LValue;
                // li %s 0x1000 + staticOffset
                // addi %s, %s, %s
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadLValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                registerManager.change(offsetRegister, offset);

                Register register = registerManager.getReg(false);
                if(isGlobal){
                    mipsSegment.li(register, registerManager.getDataAddress() + staticOffset * 4);
                }else{
                    mipsSegment.addi(register, Register.getSp() ,-staticOffset * 4);
                }
                mipsSegment.add(register, register, offsetRegister);
                registerManager.change(register, addressValue);
                return register;
            }
        } else {
            assert addressValue instanceof PointerValue;
            Register register = registerManager.getReg(false);
            mipsSegment.lw(register, Register.getSp(),-staticOffset * 4);
            registerManager.change(register, addressValue);
            if(addressValue.getOffset() instanceof Constant){
                mipsSegment.addi(register, register, ((Constant) addressValue.getOffset()).getNumber() * 4);
                registerManager.change(register, addressValue);
            }else {
                assert addressValue.getOffset() instanceof LValue;
                LValue offset = (LValue) addressValue.getOffset();
                Register offsetRegister = loadLValue(offset);
                mipsSegment.sll(offsetRegister, offsetRegister, 2);
                registerManager.change(offsetRegister, offset);
                mipsSegment.add(register, register, offsetRegister);
                registerManager.change(register, addressValue);
            }
            return register;
        }
    }

    void saveAllVariable() {
        localActive.getAllValues().stream().filter(lValue -> lValue instanceof Variable)
                .forEach(value -> storeLValue((LValue) value));
    }


}
