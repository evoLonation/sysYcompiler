package parser.nonterminal.exp;

import lexer.IntConst;
import parser.nonterminal.AST;
import parser.nonterminal.ASTDefault;
import type.IntType;
import type.VarType;

import java.util.Optional;

public class Number extends ASTDefault implements AST, ExpTyper, Exp {

    public Number(IntConst intConst) {
        type = new IntType(intConst.getDigitValue());
    }

    private final VarType type;

    @Override
    public Optional<VarType> getOptionType() {
        return Optional.of(type);
    }

    public VarType getType() {
        return type;
    }
}
