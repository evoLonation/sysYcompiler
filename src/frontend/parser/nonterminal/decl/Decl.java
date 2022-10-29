package frontend.parser.nonterminal.decl;

import frontend.parser.nonterminal.BlockItem;

import java.util.List;

public class Decl implements BlockItem {
    private final List<Def> defs;

    public List<Def> getDefs() {
        return defs;
    }

    public Decl(List<Def> defs) {
        this.defs = defs;
    }
}
