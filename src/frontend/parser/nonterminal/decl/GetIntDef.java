package frontend.parser.nonterminal.decl;

import frontend.lexer.Ident;
import frontend.parser.nonterminal.exp.Exp;

import java.util.List;

public class GetIntDef extends VarDef{
    public GetIntDef(Ident ident, List<Exp> constExps) {
        super(ident, constExps);
    }
}
