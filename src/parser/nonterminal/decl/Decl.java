package parser.nonterminal.decl;

import parser.nonterminal.ASTDefault;
import parser.nonterminal.BlockItem;

import java.util.List;

public class Decl extends ASTDefault implements BlockItem {
    private final List<Def> defs;

    public List<Def> getDefs() {
        return defs;
    }

    public Decl(List<Def> defs) {
        this.defs = defs;
        addSon(defs);
    }
}
