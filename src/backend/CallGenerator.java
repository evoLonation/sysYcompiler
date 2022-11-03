package backend;

import midcode.instrument.Call;
import midcode.instrument.Param;
import midcode.value.Temp;
import midcode.value.Value;
import midcode.value.Variable;

import java.util.stream.IntStream;

/**
 * 先逐次处理param指令，但是并不会真正生成传参的指令，会压在队列里（maxoffset等待填充）
 * 然后在函数调用处，生成保存所有从函数调用开始还要用到的指令（函数调用本身的返回值不用存储，因为它新定义了）
 * 生成结束后，知道了现在的maxoffset，回填到队列的指令中
 * 生成调用函数的指令
 * 清除寄存器的状态
 */
public class CallGenerator {
    private final LocalActive localActive;
    private final StateManager stateManager;
    private final MipsSegment mipsSegment = MipsSegment.getInstance();
    private final StoreLoadManager storeLoadManager;
    private boolean isStartParam;

    public CallGenerator(LocalActive localActive, StateManager stateManager, StoreLoadManager storeLoadManager) {
        this.localActive = localActive;
        this.stateManager = stateManager;
        this.storeLoadManager = storeLoadManager;
    }

    void generate() {
        // 先将localActive迭代到call之后
        while(localActive.getNowInstrument() instanceof Param){
            generateParam();
            localActive.next();
        }
        assert localActive.getNowInstrument() instanceof Call;
        storeAllLValue();
        backFill();
        generateCall();
        stateManager.clearRegister();
        ((Call) localActive.getNowInstrument()).getRet().ifPresent(this::getReturn);
    }

    private int paramNumber = 0;

    private void generateParam(){
        mipsSegment.comment(localActive.getNowInstrument().print());
        paramNumber ++;
        Value value = ((Param)localActive.getNowInstrument()).getValue();
        Register register = storeLoadManager.loadValue(value);
        mipsSegment.swBackFill(register, Register.getSp());
    }

    private void backFill(){
        IntStream.range(1, paramNumber + 1)
                .forEach(i -> mipsSegment.backFill(- (stateManager.getNowMaxOffset() + i) * 4));
    }

    void generateCall(){
        mipsSegment.comment(localActive.getNowInstrument().print());
        assert !mipsSegment.isInBackFill();
        mipsSegment.addi(Register.getSp(), Register.getSp(), - (stateManager.getNowMaxOffset() + 1) * 4);
        mipsSegment.jal(((Call)localActive.getNowInstrument()).getFunction().getEntry().getName());
        mipsSegment.addi(Register.getSp(), Register.getSp(), (stateManager.getNowMaxOffset() + 1) * 4);

    }

    void getReturn(Temp temp){
        Register resultRegister = stateManager.getResultReg();
        stateManager.getNeedStore(resultRegister, true).forEach(storeLoadManager::storeLValue);
        mipsSegment.move(resultRegister, Register.getV0());
        stateManager.operate(resultRegister, temp);
    }






    /**
     * 其实只需要存该函数调用之后还需要使用的值
     */
    void storeAllLValue(){
        localActive.getAllLValues().stream()
                .filter(value -> localActive.isStillUse(value, false))
                .forEach(storeLoadManager::storeLValue);
    }

}
