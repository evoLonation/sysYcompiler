package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class If extends ASDDefault implements Stmt {
    private Exp cond;
    private Stmt ifStmt;
    private Optional<Stmt> elseStmt;

    public If(Exp cond, Stmt ifStmt, Stmt elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = Optional.of(elseStmt);
        addSon(cond, ifStmt, elseStmt);
    }
    public If(Exp cond, Stmt ifStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        addSon(cond, ifStmt);
    }

    public Exp getCond() {
        return cond;
    }

    public Stmt getIfStmt() {
        return ifStmt;
    }

    public Optional<Stmt> getElseStmt() {
        return elseStmt;
    }
}

