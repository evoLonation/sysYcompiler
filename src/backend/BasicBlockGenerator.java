package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.*;
import util.VoidExecution;

import java.util.Collections;
import java.util.Set;

public class BasicBlockGenerator {
    private final MipsSegment mipsSegment = MipsSegment.getInstance();
    private final StoreLoadManager storeLoadManager;
    private final LocalActive localActive;
    private final StateManager stateManager;
    private final StringRepo stringRepo = StringRepo.getInstance();
    private final BasicBlock basicBlock;


    private final boolean isMain;
    private final boolean isEntry;


    public BasicBlockGenerator(BasicBlock basicBlock, int offset, boolean isMain, boolean isEntry) {
        this.isMain = isMain;
        this.isEntry = isEntry;
        this.basicBlock = basicBlock;
        this.localActive = new LocalActive(basicBlock);
        this.stateManager = new StateManager(localActive, offset);
        this.storeLoadManager = new StoreLoadManager(stateManager);
        stringRepo.scanBasicBlock(basicBlock);
        instrumentExecution.inject();
        jumpExecution.inject();
    }

    void generate(){
        mipsSegment.newSegment(basicBlock.getName());
        if(!isMain && isEntry) mipsSegment.sw(Register.getRa(), Register.getSp(), 4);
        while(localActive.hasNowInstrument()){
            Instrument instrument = localActive.getNowInstrument();
            if(!(instrument instanceof Param) && !(instrument instanceof Call)){
                mipsSegment.comment(instrument.print());
            }
            instrumentExecution.exec(instrument);
            localActive.next();
        }
        Jump lastJump = localActive.getLastJump();
//        if(!(lastJump instanceof Return)){
        // todo 如果是return，则需要保存全局变量，不需要保存局部变量
            saveAllVariable();
//        }
        mipsSegment.comment(lastJump.print());
        jumpExecution.exec(lastJump);
    }

    void saveAllVariable() {
        localActive.getAllLValues().stream()
                .filter(lValue -> lValue instanceof Variable)
                .filter(value -> localActive.isStillUse(value, true))
                .forEach(storeLoadManager::storeLValue);
    }



    private final VoidExecution<Instrument> instrumentExecution = new VoidExecution<Instrument>() {
        @Override
        public void inject() {
            // todo 待优化：常量计算
            inject(BinaryOperation.class,  param ->{
                Register leftRegister = storeLoadManager.loadRValue(param.getLeft());
                stateManager.setBusy(leftRegister);
                Register rightRegister = storeLoadManager.loadRValue(param.getRight());
                stateManager.setFree(leftRegister);
                Register resultReg = stateManager.getResultReg();
                stateManager.getNeedStore(resultReg, true).forEach(storeLoadManager::storeLValue);
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
                stateManager.operate(resultReg, param.getResult());
            });

            inject(UnaryOperation.class, ope -> {
                Register register = storeLoadManager.loadRValue(ope.getValue());
                if(ope.getOp() == UnaryOperation.UnaryOp.PLUS){
                    stateManager.assign(ope.getResult(), register);
                }else{
                    Register resultReg = stateManager.getResultReg();
                    stateManager.getNeedStore(resultReg, true).forEach(storeLoadManager::storeLValue);
                    switch (ope.getOp()) {
                        case MINU: mipsSegment.sub(resultReg, Register.getZero(), register);break;
                        case NOT: mipsSegment.seq(resultReg, Register.getZero(), register);break;
                        default: throw new SemanticException();
                    }
                    stateManager.operate(resultReg, ope.getResult());
                }
            });

            inject(Assignment.class, param -> {
                Register register = storeLoadManager.loadRValue(param.getRight());
                stateManager.assign(param.getLeft(), register);
            });

            inject(GetInt.class, getInt -> {
                mipsSegment.li(Register.getV0(), 5);
                mipsSegment.syscall();
                Register resultReg = stateManager.getResultReg();
                stateManager.getNeedStore(resultReg, true).forEach(storeLoadManager::storeLValue);
                mipsSegment.move(resultReg, Register.getV0());
                stateManager.operate(resultReg, getInt.getlValue());
            });

            inject(PrintInt.class, printf -> {
                mipsSegment.move(Register.getA0(), storeLoadManager.loadRValue(printf.getRValue()));
                mipsSegment.li(Register.getV0(), 1);
                mipsSegment.syscall();
            });

            inject(PrintString.class, printf->{
                mipsSegment.la(Register.getA0(), stringRepo.getStringLabel(printf));
                mipsSegment.li(Register.getV0(), 4);
                mipsSegment.syscall();
            });

            inject(Load.class, load -> {
                Register register = storeLoadManager.loadFromAddressValue(load.getRight(), false);
                stateManager.assign(load.getLeft(), register);
            });

            inject(Store.class, store -> {
                Register register = storeLoadManager.loadRValue(store.getRight());
                stateManager.setBusy(register);
                storeLoadManager.storeToAddressValue(register, store.getLeft());
                stateManager.setFree(register);
            });

            inject(Param.class, param -> new CallGenerator(localActive, stateManager, storeLoadManager).generate());
            inject(Call.class, param -> new CallGenerator(localActive, stateManager, storeLoadManager).generate());

        }
    };


    private final VoidExecution<Jump> jumpExecution = new VoidExecution<Jump>() {
        @Override
        public void inject() {
            inject(Goto.class, go -> mipsSegment.j(go.getBasicBlock().getName()));

            inject(CondGoto.class, go -> {
                LValue cond = go.getCond();
                Register register = storeLoadManager.loadValue(cond);
                mipsSegment.bne(register, Register.getZero(), go.getTrueBasicBlock().getName());
                mipsSegment.j(go.getFalseBasicBlock().getName());
            });

            inject(Return.class, param -> {
                if(isMain) {
                    mipsSegment.li(Register.getV0(), 10);
                    mipsSegment.syscall();
                }else{
                    param.getReturnValue().ifPresent(value -> {
                        Register register = storeLoadManager.loadValue(value);
                        mipsSegment.move(Register.getV0(), register);
                    });
                    mipsSegment.lw(Register.getRa(), Register.getSp(), 4);
                    mipsSegment.jr(Register.getRa());
                }
            });
        }
    };

}
