package parser.nonterminal.exp;

import lexer.IntConst;
import parser.nonterminal.ASDDefault;
import type.IntType;
import type.Type;
import type.VarType;

import java.util.Optional;

public class Number extends ASDDefault implements PrimaryExp {
    private final IntConst intConst;

    public Number(IntConst intConst) {
        this.intConst = intConst;
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
