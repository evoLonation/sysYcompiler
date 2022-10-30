package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.*;
import util.Execution;
import util.VoidExecution;

import java.text.Format;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class BasicBlockGenerator {
    private final BasicBlock basicBlock;
    private final MipsSegment mipsSegment;
    private final RegisterAllocator registerAllocator;
    private int paramOffset = 0;

    public BasicBlockGenerator(BasicBlock basicBlock, MipsSegment mipsSegment, RegisterAllocator registerAllocator) {
        this.basicBlock = basicBlock;
        this.mipsSegment = mipsSegment;
        this.registerAllocator = registerAllocator;
        instrumentExecution.inject();
        jumpExecution.inject();
    }

    public void generate(){
        mipsSegment.addInstrument(basicBlock.getName() + ": ");
        mipsSegment.addInstrument("sw $ra, 4($sp)");
        for(Instrument instrument : basicBlock.getInstruments()){
            instrumentExecution.exec(instrument);
        }
        // 结束时对于所有没有在自己的内存中的，存在内存中
        registerAllocator.saveAllVariable();
        // 然后生成跳转指令
        jumpExecution.exec(basicBlock.getLastInstrument());
    }



    private final VoidExecution<Instrument> instrumentExecution = new VoidExecution<Instrument>() {
        @Override
        public void inject() {
            inject(param -> {});
            inject(BinaryOperation.class,  param ->{
    //            Map<LValue, RegisterAllocator.Register> regs = registerAllocator.getReg(param);
                String left;
                boolean isLeftConstant = false;
                RegisterAllocator.Register leftRegister = null;
                if(param.getLeft() instanceof LValue){
                    LValue lValue = (LValue)param.getLeft();
                    leftRegister = registerAllocator.getFactorReg(lValue, param);
                    registerAllocator.loadLValue(lValue, leftRegister);
                    left = leftRegister.print();
                }else{
                    assert param.getLeft() instanceof Constant;
                    isLeftConstant = true;
                    left = Integer.toString(((Constant) param.getLeft()).getNumber());
                }
                String right;
                boolean isRightConstant = false;
                if(param.getRight() instanceof LValue){
                    LValue lValue = (LValue)param.getRight();
                    RegisterAllocator.Register register = registerAllocator.getFactorReg(lValue, leftRegister, param);
                    registerAllocator.loadLValue(lValue, register);
                    right = register.print();
                }else{
                    assert param.getRight() instanceof Constant;
                    isRightConstant = true;
                    right = Integer.toString(((Constant) param.getRight()).getNumber());
                }

                LValue resultLValue = param.getResult();
                RegisterAllocator.Register resultReg = registerAllocator.getResultReg(resultLValue, Arrays.asList(param.getRight(), param.getLeft()), param);
                String result = resultReg.print();
                // resultReg中的值要变化，因此它与之前的lvalue都取消了绑定
                registerAllocator.clearReg(resultReg);
                registerAllocator.defLValue(resultLValue, resultReg);
                assert !(isLeftConstant && isRightConstant);
                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                if(isLeftConstant || isRightConstant){
                    if(isLeftConstant){
                        String tmp = left;
                        left = right;
                        right = tmp;
                    }
                    switch (param.getOp()) {
                        case PLUS:  mipsSegment.addInstrument(String.format("addi %s, %s, %s", result, left, right)); break;
                        case MINU: mipsSegment.addInstrument(String.format("addi %s, %s, -%s", result, left, right)); break;
                        // todo mult 和 div 可以移位计算
                        case MULT: return;
                        default: throw new SemanticException();
                    }
                } else{
                    switch (param.getOp()) {
                        case PLUS: mipsSegment.addInstrument(String.format("add %s, %s, %s", result, left, right)); break;
                        case MINU: mipsSegment.addInstrument(String.format("sub %s, %s, %s", result, left, right)); break;
                        case MULT: return;
                        default: throw new SemanticException();
                    }
                }

            });

            inject(Assignment.class, param -> {
                RegisterAllocator.Register register;
                LValue left = param.getLeft();
                RValue right = param.getRight();
                if(Generator.DEBUG) mipsSegment.addInstrument(param.print());
                if (right instanceof LValue) {
                    register = registerAllocator.getFactorReg((LValue) right, param);
                    registerAllocator.loadLValue((LValue) right, register);
                    // 并不会改变寄存器的值，只是将left与寄存器绑定起来
                    registerAllocator.defLValue(left, register);
                } else {
                    assert right instanceof Constant;
                    register = registerAllocator.getResultReg(left, new ArrayList<>(), param);
                    String leftStr = register.print();
                    registerAllocator.clearReg(register);
                    registerAllocator.defLValue(left, register);
                    mipsSegment.addInstrument(String.format("li %s, %d", leftStr, ((Constant) param.getRight()).getNumber()));
                }
            });

            inject(Call.class, param -> {
                int index = 0;
                for(Value value : param.getParams()){
                    RegisterAllocator.Register register;
                    if(value instanceof PointerValue){
                        register = registerAllocator.getPointerValue((PointerValue) value, param);
                    }else{
                        register = registerAllocator.getFactorReg((LValue) value, param);
                        registerAllocator.loadLValue((LValue) value, register);
                    }
                    registerAllocator.storeParam(index, register);
                    index ++;
                }
                registerAllocator.pushSp();
                mipsSegment.addInstrument(String.format("jal %s", param.getFunction().getEntry().getName()));
                registerAllocator.popSp();
                param.getRet().ifPresent(temp -> {
                    RegisterAllocator.Register register = registerAllocator.getResultReg(temp, new ArrayList<>(), param);
                    registerAllocator.clearReg(register);
                    registerAllocator.defLValue(temp, register);
                    mipsSegment.addInstrument(String.format("addi %s, $v0, 0", register.print()));
                });
            });


        }
    };


    private final VoidExecution<Jump> jumpExecution = new VoidExecution<Jump>() {
        @Override
        public void inject() {
            inject(param -> {});
            inject(Goto.class, go -> mipsSegment.addInstrument("j " + go.getBasicBlock().getName()));
            inject(CondGoto.class, go -> {
                mipsSegment.addInstrument(String.format("bne %s, $0, %s", registerAllocator.findLValue(go.getCond(), go), go.getTrueBasicBlock().getName()));
                mipsSegment.addInstrument(String.format("j %s", go.getFalseBasicBlock().getName()));
            });
            inject(Return.class, param -> {
                param.getReturnValue().ifPresent(value -> {
                    if(value instanceof Constant){
                        mipsSegment.addInstrument(String.format("li $v0, %d", ((Constant) value).getNumber()));
                    }else{
                        assert value instanceof LValue;
                        mipsSegment.addInstrument(String.format("addi $v0, %s, $0", registerAllocator.findLValue((LValue) value, param).print()));
                    }
                });
                mipsSegment.addInstrument("lw %ra, 4($sp)");
                mipsSegment.addInstrument("jr $ra");
            });
        }
    };


}
