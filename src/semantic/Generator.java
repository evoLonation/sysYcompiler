package semantic;

import common.SemanticException;
import error.ErrorRecorder;
import midcode.value.ValueFactory;
import type.SymbolTable;

public abstract class Generator {
    protected final SymbolTable symbolTable = SymbolTable.getInstance();
    protected final ValueFactory valueFactory = ValueFactory.getInstance();
    protected final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();

    protected void check(boolean cond){
        if(!cond){
            throw new SemanticException();
        }
    }

    public Generator() {
        generate();
    }

    protected abstract void generate();

}
