package semantic;

import common.SemanticException;
import error.ErrorRecorder;
import midcode.BasicBlockFactory;
import midcode.value.ValueFactory;
import type.SymbolTable;

public abstract class Generator {



    protected final SymbolTable symbolTable = SymbolTable.getInstance();
    protected final BasicBlockFactory basicBlockFactory = BasicBlockFactory.getInstance();
    protected final ValueFactory valueFactory = ValueFactory.getInstance();
    protected final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
    protected final WhileStmtDealer whileStmtDealer = WhileStmtDealer.getInstance();

    Generator() {}

    protected abstract void generate();

}
