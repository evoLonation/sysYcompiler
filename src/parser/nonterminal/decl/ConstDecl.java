package parser.nonterminal.decl;

import java.util.List;

public class ConstDecl implements Decl {
    private final List<Def> constDefs;

    public ConstDecl(List<Def> constDefs) {
        this.constDefs = constDefs;
    }
}
