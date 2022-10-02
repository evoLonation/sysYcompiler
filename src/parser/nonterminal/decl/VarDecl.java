package parser.nonterminal.decl;

import java.util.List;

public class VarDecl implements Decl {
    private List<Def> varDefs;

    public VarDecl(List<Def> varDefs) {
        this.varDefs = varDefs;
    }
}
