package semantic;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.Function;
import midcode.value.Constant;
import midcode.value.RValue;
import parser.nonterminal.Block;
import parser.nonterminal.FuncDef;
import parser.nonterminal.exp.Exp;
import type.IntType;
import type.PointerType;

import java.util.ArrayList;
import java.util.Optional;


public class FuncDefGenerator extends Generator{
    private final FuncDef funcDef;

    FuncDefGenerator(FuncDef funcDef) {
        this.funcDef = funcDef;
        generate();
    }

    private Function function;

    public Function getFunction() {
        return function;
    }

    @Override
    protected void generate() {
        function = basicBlockFactory.newFunction(funcDef.getIdent().getValue());
        symbolTable.newFuncDomain(function, funcDef.getIdent(), funcDef.isInt());
        for(FuncDef.FuncFParam funcFParam : funcDef.getFuncFParams()){
            if(funcFParam instanceof FuncDef.IntFParam){
                symbolTable.addParam(funcFParam.getIdent(), new IntType());
            }else if(funcFParam instanceof FuncDef.PointerFParam){
                Optional<Exp> constExp = ((FuncDef.PointerFParam) funcFParam).getConstExp();
                if(constExp.isPresent()){
                    RValue result = new ExpGenerator(constExp.get()).getRValueResult();
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
        new FuncBlockGenerator(block);
        symbolTable.outBlock();
        basicBlockFactory.outFunction(symbolTable.getMaxOffset());
    }
}
