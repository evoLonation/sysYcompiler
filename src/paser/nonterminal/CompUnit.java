package paser.nonterminal;

import java.util.List;

public class CompUnit {
    private List<Decl> decls;
    private List<FuncDef> funcDefs;

    public CompUnit(List<Decl> decls, List<FuncDef> funcDefs) {
        this.decls = decls;
        this.funcDefs = funcDefs;
    }

    public List<Decl> getDecls() {
        return decls;
    }

    public List<FuncDef> getFuncDefs() {
        return funcDefs;
    }
}

