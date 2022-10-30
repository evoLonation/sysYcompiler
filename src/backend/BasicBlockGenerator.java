package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.Constant;
import midcode.value.LValue;
import midcode.value.RValue;
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

    public BasicBlockGenerator(BasicBlock basicBlock, MipsSegment mipsSegment, RegisterAllocator registerAllocator) {
        this.basicBlock = basicBlock;
        this.mipsSegment = mipsSegment;
        this.registerAllocator = registerAllocator;
        instrumentExecution.inject();
    }

    public void generate(){
        for(Instrument instrument : basicBlock.getInstruments()){
            instrumentExecution.exec(instrument);
        }
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

        }
    };


    private final Execution<Jump, String> jumpExecution = new Execution<Jump, String>() {
        @Override
        public void inject() {
            inject(param -> null);
            inject(Goto.class, go -> "j" + go.getBasicBlock().getName());
            inject(CondGoto.class, go ->{
                return null;
            });
        }
    };


}
