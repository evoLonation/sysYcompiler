package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.*;
import util.VoidExecution;

public class BasicBlockGenerator {
    private final MipsSegment mipsSegment;
    private final LocalActive localActive;
    private final StorageManager storageManager;

    private int paramOffset = 0;


    public BasicBlockGenerator(BasicBlock basicBlock, int offset) {
        this.localActive = new LocalActive(basicBlock);
        this.mipsSegment = new MipsSegment(basicBlock.getName());
        this.storageManager = new StorageManager(localActive, mipsSegment, offset);
        instrumentExecution.inject();
        jumpExecution.inject();
    }

    public String generate(){
        mipsSegment.sw(Register.getRa(), Register.getSp(), 4);
        localActive.forEach(instrument -> {
            mipsSegment.comment(instrument.print());
            instrumentExecution.exec(instrument);
        },
        storageManager::saveAllVariable,
        instrument-> {
            mipsSegment.comment(instrument.print());
            jumpExecution.exec(instrument);
        });
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
                    leftRegister = storageManager.loadValue(left);
                }else{
                    assert param.getLeft() instanceof Constant;
                    leftConst = ((Constant) param.getLeft()).getNumber();
                }
                int rightConst = 0;
                Register rightRegister = null;
                if(param.getRight() instanceof LValue){
                    LValue right = (LValue)param.getRight();
                    rightRegister = storageManager.loadValue(right);
                }else{
                    assert param.getRight() instanceof Constant;
                    rightConst = ((Constant) param.getRight()).getNumber();
                }

                LValue resultLValue = param.getResult();
                Register resultReg = storageManager.toComputeValue(resultLValue);
                // resultReg中的值要变化，因此它与之前的lvalue都取消了绑定
                assert leftRegister != null || rightRegister != null;
//                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
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
            });

            inject(Assignment.class, param -> {
                Register register;
                LValue left = param.getLeft();
                RValue right = param.getRight();
//                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                if (right instanceof LValue) {
                    register = storageManager.loadValue(right);
                    storageManager.toAssign(left, register);
                } else {
                    assert right instanceof Constant;
                    register = storageManager.toComputeValue(left);
                    mipsSegment.li(register, ((Constant) right).getNumber());
                }
            });

            inject(Call.class, param -> {
                mipsSegment.addi(Register.getSp(), Register.getSp(), -(storageManager.getNowMaxOffset() + 1) * 4);
                mipsSegment.jal(param.getFunction().getEntry().getName());
                mipsSegment.addi(Register.getSp(), Register.getSp(), (storageManager.getNowMaxOffset() + 1) * 4);
                param.getRet().ifPresent(temp -> {
                    Register register = storageManager.toComputeValue(temp);
                    mipsSegment.addi(register, Register.getV0(), 0);
                });
            });

            inject(Param.class, param -> {
                Register register;
                Value value = param.getValue();
                if(value instanceof AddressValue){
                    register = storageManager.loadAddress((AddressValue) value);
                }else{
                    register = storageManager.loadValue(value);
                }
                mipsSegment.sw(register, Register.getSp(), -(storageManager.getNowMaxOffset() + paramOffset) * 4);
                paramOffset ++;
            });

            inject(Load.class, load -> {
                storageManager.loadFromAddressValue(load.getLeft(), load.getRight());
            });

            inject(Store.class, store -> {
                storageManager.storeToAddressValue(store.getRight(), store.getLeft());
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
                Register register = storageManager.loadValue(cond);
                mipsSegment.bne(register, Register.getZero(), go.getTrueBasicBlock().getName());
                mipsSegment.j(go.getFalseBasicBlock().getName());
            });
            inject(Return.class, param -> {
                param.getReturnValue().ifPresent(value -> {
                    if(value instanceof Constant){
                        mipsSegment.li(Register.getV0() ,((Constant) value).getNumber());
                    }else{
                        assert value instanceof LValue;
                        Register register = storageManager.loadValue(value);
                        mipsSegment.addi(Register.getV0(), register, 0);
                    }
                });
                mipsSegment.lw(Register.getRa(), Register.getSp(), 4);
                mipsSegment.jr(Register.getRa());
            });
        }
    };




}
