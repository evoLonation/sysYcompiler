package frontend.IRGenerate;

import frontend.IRGenerate.util.BasicBlockFactory;
import frontend.IRGenerate.util.ValueFactory;
import frontend.error.ErrorRecorder;
import frontend.type.SymbolTable;

public abstract class Generator {



    protected final SymbolTable symbolTable = SymbolTable.getInstance();
    protected final BasicBlockFactory basicBlockFactory = BasicBlockFactory.getInstance();
    protected final ValueFactory valueFactory = ValueFactory.getInstance();
    protected final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
    protected final WhileStmtDealer whileStmtDealer = WhileStmtDealer.getInstance();

    Generator() {}

}
