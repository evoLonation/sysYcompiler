package semantic;

import common.SemanticException;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.Function;
import midcode.instrument.Return;
import midcode.value.Constant;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.FuncDef;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.stmt.*;
import type.IntType;
import type.PointerType;

import java.util.ArrayList;
import java.util.Optional;


public class FuncDefGenerator extends Generator{
    private final FuncDef funcDef;

    public FuncDefGenerator(FuncDef funcDef) {
        this.funcDef = funcDef;
        generate();
    }

    private Function function;

    public Function getFunction() {
        return function;
    }

    @Override
    protected void generate() {
        isReturn = funcDef.isInt();
        function = BasicBlockFactory.getInstance().newFunction(funcDef.getIdent(), funcDef.isInt());
        for(FuncDef.FuncFParam funcFParam : funcDef.getFuncFParams()){
            if(funcFParam instanceof FuncDef.IntFParam){
                symbolTable.addParam(funcFParam.getIdent(), new IntType());
            }else if(funcFParam instanceof FuncDef.PointerFParam){
                Optional<Exp> constExp = ((FuncDef.PointerFParam) funcFParam).getConstExp();
                if(constExp.isPresent()){
                    ExpGenerator.Result result = new ExpGenerator(new ArrayList<>(), constExp.get()).getResult();
                    assert result instanceof ExpGenerator.RValueResult;
                    assert ((ExpGenerator.RValueResult) result).rValue instanceof Constant;
                    symbolTable.addParam(funcFParam.getIdent(), new PointerType(((Constant) ((ExpGenerator.RValueResult) result).rValue).getNumber()));
                }else{
                    symbolTable.addParam(funcFParam.getIdent(), new PointerType());
                }
            }else{
                throw new SemanticException();
            }
        }

        BasicBlock funcBasicBlock = function.getEntry();
        Block block = funcDef.getBlock();
        new FuncBlockGenerator(funcBasicBlock, block);
    }
}
