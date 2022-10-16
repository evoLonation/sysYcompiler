package parser.nonterminal;

import java.util.ArrayList;
import java.util.List;


public abstract class ASTDefault implements AST {
    private final List<AST> sons = new ArrayList<>();
    @Override
    public final List<AST> sons() {
        return sons;
    }

    @SafeVarargs
    protected final void addSon(List<? extends AST>... candidates){
        for(List<? extends AST> list : candidates){
            sons.addAll(list);
        }
    }
    protected final void addSon(AST... candidates){
        for(AST AST : candidates){
            if(AST != null){
                sons.add(AST);
            }
        }
    }
}
