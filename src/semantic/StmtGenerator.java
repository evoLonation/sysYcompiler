package semantic;

import midcode.instrument.Goto;
import midcode.instrument.Label;
import midcode.instrument.LabelFactory;
import midcode.instrument.MidCode;
import midcode.value.ValueFactory;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.stmt.Assign;
import parser.nonterminal.stmt.If;
import parser.nonterminal.stmt.Stmt;
import type.SymbolTable;

import java.util.List;

public class StmtGenerator extends VoidExecution<Stmt>{
    private SymbolTable symbolTable;
    private Stmt stmt;
    private ValueFactory valueFactory;
    private LabelFactory labelFactory;

    private List<MidCode> midCodes;

    public StmtGenerator(SymbolTable symbolTable, Stmt stmt, ValueFactory valueFactory, LabelFactory labelFactory) {
        this.symbolTable = symbolTable;
        this.stmt = stmt;
        this.valueFactory = valueFactory;
        this.labelFactory = labelFactory;
        inject(If.class, ifExecutor);
    }

    private Executor<If> ifExecutor = stmt -> {
        CondGenerator condGenerator = new CondGenerator(symbolTable, stmt.getCond(), valueFactory, labelFactory);
        midCodes.addAll(condGenerator.getMidCodes());
        midCodes.add(condGenerator.getTrueLabel());
        stmt.getIfStmt().ifPresent(ifStmt -> {
            StmtGenerator ifStmtGenerator = new StmtGenerator(symbolTable, ifStmt, valueFactory, labelFactory);
            ifStmtGenerator.generate();
            midCodes.addAll(ifStmtGenerator.getMidCodes());
        });
        Label label = labelFactory.newLabel();
        midCodes.add(new Goto(label));
        midCodes.add(condGenerator.getFalseLabel());
        stmt.getElseStmt().ifPresent(elseStmt -> {
            StmtGenerator elseStmtGenerator = new StmtGenerator(symbolTable, elseStmt, valueFactory, labelFactory);
            elseStmtGenerator.generate();
            midCodes.addAll(elseStmtGenerator.getMidCodes());
        });
        midCodes.add(label);
    };

    private Executor<Exp> expExecutor = stmt -> {
        AddExpGenerator addExpGenerator = new AddExpGenerator(symbolTable, stmt, valueFactory);
        addExpGenerator.generate();
        midCodes.addAll(addExpGenerator.getMidCodes());
    };

    private Executor<Assign> assignExecutor = stmt -> {

    }

    public List<MidCode> getMidCodes() {
        return midCodes;
    }

    public void generate(){
        exec(stmt);
    }
}
