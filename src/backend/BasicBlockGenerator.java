package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.*;
import util.VoidExecution;

public class BasicBlockGenerator {
    private final MipsSegment mipsSegment = MipsSegment.getInstance();
    private final LocalActive localActive;
    private final StorageManager storageManager;
    private final StringRepo stringRepo = StringRepo.getInstance();
    private final BasicBlock basicBlock;

    private int paramOffset = 1;
    private boolean isStartParam;
    private final boolean isMain;
    private final boolean isEntry;


    public BasicBlockGenerator(BasicBlock basicBlock, int offset, boolean isMain, boolean isEntry) {
        this.isMain = isMain;
        this.isEntry = isEntry;
        this.basicBlock = basicBlock;
        this.isStartParam = false;
        this.localActive = new LocalActive(basicBlock);
        this.storageManager = new StorageManager(localActive, offset);
        stringRepo.scanBasicBlock(basicBlock);
        instrumentExecution.inject();
        jumpExecution.inject();
    }

    public String generate(){
        mipsSegment.newSegment(basicBlock.getName());
        if(!isMain && isEntry) mipsSegment.sw(Register.getRa(), Register.getSp(), 4);
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
            // todo 待优化：常量计算
            inject(BinaryOperation.class,  param ->{
                Register leftRegister = storageManager.loadValue(param.getLeft());
                Register rightRegister = storageManager.loadValue(param.getRight());
                Register resultReg = storageManager.toComputeValue(param.getResult());
                switch (param.getOp()) {
                    case PLUS: mipsSegment.add(resultReg, leftRegister, rightRegister); break;
                    case MINU: mipsSegment.sub(resultReg, leftRegister, rightRegister); break;
                    case MULT: mipsSegment.mul(resultReg, leftRegister, rightRegister); break;
                    case DIV: mipsSegment.div(resultReg, leftRegister, rightRegister); break;
                    case MOD: mipsSegment.mod(resultReg, leftRegister, rightRegister); break;
                    case LEQ: mipsSegment.sle(resultReg, leftRegister, rightRegister);break;
                    case GEQ: mipsSegment.sge(resultReg, leftRegister, rightRegister);break;
                    case GRE: mipsSegment.sgt(resultReg, leftRegister, rightRegister); break;
                    case LSS: mipsSegment.slt(resultReg, leftRegister, rightRegister); break;
                    case NEQ: mipsSegment.sne(resultReg, leftRegister, rightRegister);break;
                    case EQL: mipsSegment.seq(resultReg, leftRegister, rightRegister);break;
                    default: throw new SemanticException();
                }
            });

            inject(UnaryOperation.class, ope -> {
                Register register = storageManager.loadValue(ope.getValue());
                switch (ope.getOp()) {
                    case PLUS: {
                        storageManager.assign(ope.getResult(), register);
                        break;
                    }
                    case MINU: {
                        Register resultReg = storageManager.toComputeValue(ope.getResult());
                        mipsSegment.sub(resultReg, Register.getZero(), register);
                        break;
                    }
                    case NOT: {
                        Register resultReg = storageManager.toComputeValue(ope.getResult());
                        mipsSegment.seq(resultReg, Register.getZero(), register);
                        break;
                    }
                    default: throw new SemanticException();
                }
            });

            inject(Assignment.class, param -> {
                Register register;
                LValue left = param.getLeft();
                RValue right = param.getRight();
//                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                if (right instanceof LValue) {
                    register = storageManager.loadValue(right);
                    storageManager.assign(left, register);
                } else {
                    assert right instanceof Constant;
                    register = storageManager.toComputeValue(left);
                    mipsSegment.li(register, ((Constant) right).getNumber());
                }
            });

            inject(Call.class, param -> {
//                storageManager.storeAllValue();
                if(!isStartParam){
                    // todo 重大bug：param之后有可能会存储temp，导致本函数的maxoffset上升，从而导致目标函数读不到param
                    //前提假设：所有的param都紧挨着对应的funccall之前
                    storageManager.storeAllValue();
                    isStartParam = true;
                }
                isStartParam = false;
                storageManager.clearRegister();
                paramOffset = 1;
                mipsSegment.addi(Register.getSp(), Register.getSp(), -(storageManager.getNowMaxOffset() + 1) * 4);
                mipsSegment.jal(param.getFunction().getEntry().getName());
                mipsSegment.addi(Register.getSp(), Register.getSp(), (storageManager.getNowMaxOffset() + 1) * 4);
                param.getRet().ifPresent(temp -> {
                    Register register = storageManager.toComputeValue(temp);
                    mipsSegment.addi(register, Register.getV0(), 0);
                });
            });

            inject(Param.class, param -> {
                if(!isStartParam){
                    // todo 重大bug：param之后有可能会存储temp，导致本函数的maxoffset上升，从而导致目标函数读不到param
                    //前提假设：所有的param都紧挨着对应的funccall之前
                    storageManager.storeAllValue();
                    isStartParam = true;
                }
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

            inject(GetInt.class, getInt -> {
                mipsSegment.li(Register.getV0(), 5);
                mipsSegment.syscall();
                mipsSegment.move(storageManager.toComputeValue(getInt.getlValue()), Register.getV0());
            });

            inject(PrintInt.class, printf -> {
                mipsSegment.move(Register.getA0(), storageManager.loadValue(printf.getRValue()));
                mipsSegment.li(Register.getV0(), 1);
                mipsSegment.syscall();
            });

            inject(PrintString.class, printf->{
                mipsSegment.la(Register.getA0(), stringRepo.getStringLabel(printf));
                mipsSegment.li(Register.getV0(), 4);
                mipsSegment.syscall();
            });

        }
    };

    private final VoidExecution<Jump> jumpExecution = new VoidExecution<Jump>() {
        @Override
        public void inject() {
            inject(Goto.class, go -> mipsSegment.j(go.getBasicBlock().getName()));
            inject(CondGoto.class, go -> {
                LValue cond = go.getCond();
                Register register = storageManager.loadValue(cond);
                mipsSegment.bne(register, Register.getZero(), go.getTrueBasicBlock().getName());
                mipsSegment.j(go.getFalseBasicBlock().getName());
            });
            inject(Return.class, param -> {
                if(isMain) {
                    mipsSegment.li(Register.getV0(), 10);
                    mipsSegment.syscall();
                }else{
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
                }
            });
        }
    };

}
