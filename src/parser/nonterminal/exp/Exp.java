package parser.nonterminal.exp;

import parser.nonterminal.decl.Def;
import parser.nonterminal.stmt.Stmt;

public interface Exp extends Stmt, PrimaryExp, Def.InitVal {
}
