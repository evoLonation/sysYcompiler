package frontend.IRGenerate;

import common.SemanticException;
import midcode.Function;
import midcode.instruction.ImplicitDef;
import midcode.value.Constant;
import midcode.value.RValue;
import frontend.parser.nonterminal.Block;
import frontend.parser.nonterminal.FuncDef;
import frontend.parser.nonterminal.exp.Exp;
import frontend.type.IntType;
import frontend.type.PointerType;

import java.util.Optional;


public class FuncDefGenerator extends Generator{
    private final FuncDef funcDef;

    FuncDefGenerator(FuncDef funcDef) {
        this.funcDef = funcDef;
    }


    Function generate() {
        Function function;
        function = basicBlockManager.newFunction(funcDef.getIdent().getValue());
        symbolTable.newFuncDomain(function, funcDef.getIdent(), funcDef.isInt());
        for(FuncDef.FuncFParam funcFParam : funcDef.getFuncFParams()){
            if(funcFParam instanceof FuncDef.IntFParam){
                symbolTable.addParam(funcFParam.getIdent(), new IntType());
                basicBlockManager.addSequence(new ImplicitDef(valueFactory.newVariable(funcFParam.getIdent())));
            }else if(funcFParam instanceof FuncDef.PointerFParam){
                Optional<Exp> constExp = ((FuncDef.PointerFParam) funcFParam).getConstExp();
                if(constExp.isPresent()){
                    RValue result = new ExpGenerator(constExp.get()).generate().getRValueResult();
                    assert result instanceof Constant;
                    symbolTable.addParam(funcFParam.getIdent(), new PointerType(((Constant) result).getNumber()));
                }else{
                    symbolTable.addParam(funcFParam.getIdent(), new PointerType());
                }
            }else{
                throw new SemanticException();
            }
        }
        Block block = funcDef.getBlock();
        new FuncBlockGenerator(block).generate();
        symbolTable.outBlock();
        basicBlockManager.outFunction(symbolTable.getMaxOffset());
        return function;
    }
}
