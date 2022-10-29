package frontend.parser.nonterminal.stmt;

import frontend.parser.nonterminal.exp.Exp;

import java.util.Optional;

public class If implements Stmt {
    private final Exp cond;
    private final Stmt ifStmt;
    private final Stmt elseStmt;

    public If(Exp cond, Stmt ifStmt, Stmt elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
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

