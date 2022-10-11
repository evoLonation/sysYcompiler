package parser.nonterminal.stmt;

import parser.nonterminal.ASDDefault;
import parser.nonterminal.exp.Exp;

import java.util.Optional;

public class If extends ASDDefault implements Stmt {
    private final Exp cond;
    private final Stmt ifStmt;
    private final Stmt elseStmt;

    public If(Exp cond, Stmt ifStmt, Stmt elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
        addSon(cond, ifStmt, elseStmt);
    }
    public If(Exp cond, Stmt stmt, boolean ifOrElse) {
        this(cond, ifOrElse ? stmt : null, ifOrElse ? null : stmt);
    }
    public If(Exp cond) {
        this(cond, null, null);
    }

    public Exp getCond() {
        return cond;
    }

    public Optional<Stmt> getIfStmt() {
        return Optional.ofNullable(ifStmt);
    }

    public Optional<Stmt> getElseStmt() {
        return Optional.ofNullable(elseStmt);
    }
}

