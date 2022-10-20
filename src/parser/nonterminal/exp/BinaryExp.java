package parser.nonterminal.exp;

import type.VarType;

import java.util.Optional;


public class BinaryExp implements Exp{
    private final Exp exp1;
    private final Exp exp2;
    private final BinaryOp op;


    public BinaryExp(Exp exp1, BinaryOp op, Exp exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.op = op;
    }

    public Exp getExp1() {
        return exp1;
    }

    public Exp getExp2() {
        return exp2;
    }

    public BinaryOp getOp() {
        return op;
    }

}
