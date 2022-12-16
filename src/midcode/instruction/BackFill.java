package midcode.instruction;

import midcode.BasicBlock;

import java.util.ArrayList;
import java.util.List;

public class BackFill {
    private final List<Goto> gotos = new ArrayList<>();
    private final List<CondGotoInfo> condGotos = new ArrayList<>();
    private static class CondGotoInfo{
        CondGoto condGoto;
        boolean isFirst;

        public CondGotoInfo(CondGoto condGoto, boolean isFirst) {
            this.condGoto = condGoto;
            this.isFirst = isFirst;
        }
    }
    public void add(Goto aGoto){
        gotos.add(aGoto);
    }

    public void add(CondGoto condGoto, boolean isFirst){
        condGotos.add(new CondGotoInfo(condGoto, isFirst));
    }

    public void fill(BasicBlock basicBlock){
        for(Goto go: gotos){
            go.basicBlock = basicBlock;
        }
        for(CondGotoInfo condGotoInfo : condGotos){
            if(condGotoInfo.isFirst){
                condGotoInfo.condGoto.trueBasicBlock = basicBlock;
            }else{
                condGotoInfo.condGoto.falseBasicBlock = basicBlock;
            }
        }
        gotos.clear();
        condGotos.clear();
    }

    public void deliverTo(BackFill backFill){
        backFill.condGotos.addAll(condGotos);
        backFill.gotos.addAll(gotos);
        gotos.clear();
        condGotos.clear();
    }

}
