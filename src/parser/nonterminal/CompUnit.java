package parser.nonterminal;

import parser.nonterminal.decl.Decl;

import java.util.List;

public class CompUnit extends ASDDefault implements ASD{
    private final List<Decl> decls;
    private final List<FuncDef> funcDefs;
    private final MainFuncDef mainFuncDef;

    public CompUnit(List<Decl> decls, List<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
        addSon(decls, funcDefs);
        addSon(mainFuncDef);
    }

    public List<Decl> getDecls() {
        return decls;
    }

    public List<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }
}

