package backend;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.instrument.*;
import midcode.value.Constant;
import midcode.value.LValue;
import util.Execution;

import java.text.Format;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    }

    public void generate(){
        for(Instrument instrument : basicBlock.getInstruments()){
            String mips = instrumentExecution.exec(instrument);
            if(mips != null){
                mipsSegment.addInstrument(mips);
            }
        }
    }



    private final Execution<Instrument, String> instrumentExecution = new Execution<Instrument, String>() {
        @Override
        public void inject() {
        inject(BinaryOperation.class,  param ->{
            Map<LValue, Integer> regs = registerAllocator.getReg(param);
            String left;
            boolean isLeftConstant = false;
            if(param.getLeft() instanceof LValue){
                LValue leftLValue = (LValue) param.getLeft();
                int reg = regs.get(leftLValue);
                registerAllocator.loadLValue(leftLValue, reg);
                left = registerAllocator.getRealRegister(reg);
            }else{
                assert param.getLeft() instanceof Constant;
                isLeftConstant = true;
                left = Integer.toString(((Constant) param.getLeft()).getNumber());
            }
            String right;
            boolean isRightConstant = false;
            if(param.getRight() instanceof LValue){
                LValue rightLValue = (LValue) param.getRight();
                int reg = regs.get(rightLValue);
                registerAllocator.loadLValue(rightLValue, reg);
                right = registerAllocator.getRealRegister(reg);
            }else{
                assert param.getRight() instanceof Constant;
                isRightConstant = true;
                right = Integer.toString(((Constant) param.getRight()).getNumber());
            }

            LValue resultLValue = param.getResult();
            int resultReg = regs.get(resultLValue);
            String result = registerAllocator.getRealRegister(resultReg);
            registerAllocator.changeLValue(resultLValue, resultReg);
            assert !(isLeftConstant && isRightConstant);
            if(isLeftConstant || isRightConstant){
                if(isLeftConstant){
                    String tmp = left;
                    left = right;
                    right = tmp;
                }
                switch (param.getOp()) {
                    case PLUS: return String.format("addi %s, %s, %s", result, left, right);
                    case MINU: return String.format("addi %s, %s, -%s", result, left, right);
                    // todo mult 和 div 可以移位计算
                    case MULT: return null;
                    default: throw new SemanticException();
                }
            } else{
                switch (param.getOp()) {
                    case PLUS: return String.format("add %s, %s, %s", result, left, right);
                    case MINU: return String.format("sub %s, %s, %s", result, left, right);
                    case MULT: return null;
                    default: throw new SemanticException();
                }
            }

        });
        inject(Assignment.class, param -> {
            Map<LValue, Integer> regs = registerAllocator.getReg(param);
            LValue left = param.getLeft();
            String leftStr = registerAllocator.getRealRegister(regs.get(left));
            if(param.getRight() instanceof Constant){
                registerAllocator.changeLValue(left, regs.get(left));
                return String.format("li %s, %d", leftStr, ((Constant) param.getRight()).getNumber());
            }else {
                assert param.getRight() instanceof LValue;
                LValue right = (LValue) param.getRight();
                // 假设x=y中的x和y总是分配一个寄存器
                assert Objects.equals(regs.get(left), regs.get(right));
                int reg = regs.get(param.getLeft());
                registerAllocator.loadLValue(right, reg);
                registerAllocator.changeLValue(left, reg);
                return null;
            }
        });

        }
    };

    private final Execution<Jump, String> jumpExecution = new Execution<Jump, String>() {
        @Override
        public void inject() {
            inject(Goto.class, go -> "j" + go.getBasicBlock().getName());
            inject(CondGoto.class, go ->{
                return null;
            });
        }
    };


}
