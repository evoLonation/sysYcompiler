package midcode.instrument;

import midcode.BasicBlock;
import midcode.value.RValue;

public class CondGoto implements Jump{
    private BasicBlock trueBasicBlock;
    private BasicBlock falseBasicBlock;
    private RValue cond;

}
