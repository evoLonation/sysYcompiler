package parser.nonterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ASDDefault implements ASD {
    private final List<ASD> sons = new ArrayList<>();
    @Override
    public final List<ASD> sons() {
        return sons;
    }

    protected final void addSon(List<? extends ASD>... candidates){
        for(List<? extends ASD> list : candidates){
            sons.addAll(list);
        }
    }
    protected final void addSon(ASD... candidates){
        for(ASD asd : candidates){
            if(asd != null){
                sons.add(asd);
            }
        }
    }

}
