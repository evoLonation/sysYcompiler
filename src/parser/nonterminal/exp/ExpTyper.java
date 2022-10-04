package parser.nonterminal.exp;

import type.VarType;

import java.util.Optional;


// exp 存在函数调用，类型存在取决于函数返回值是否为int
public interface ExpTyper {
    Optional<VarType> getType();
}
