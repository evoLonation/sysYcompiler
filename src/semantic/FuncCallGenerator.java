package semantic;

import common.SemanticException;
import lexer.Ident;
import midcode.Function;
import midcode.instrument.*;
import midcode.value.*;
import parser.nonterminal.exp.*;
import parser.nonterminal.exp.Number;
import type.*;

import java.util.List;
import java.util.Optional;

public class FuncCallGenerator extends InstrumentGenerator{
    private final FuncCall funcCall;

    FuncCallGenerator(FuncCall funcCall) {
        this.funcCall = funcCall;
        generate();
    }

    private LValue result = null;

    public Optional<LValue> getResult() {
        return Optional.ofNullable(result);
    }


    @Override
    protected void generate() {
        Ident ident = funcCall.getIdent();
        List<Exp> exps = funcCall.getExps();
        Optional<SymbolTable.FunctionInfo> infoOptional = symbolTable.getFunction(ident);
        if(!infoOptional.isPresent()){
            Temp temp = valueFactory.newTemp();
            addInstrument(new Assignment(temp, new Constant(0)));
            result = temp;
        }else{
            FuncType funcType = infoOptional.get().type;
            Function function = infoOptional.get().function;
            if(funcType.getParamNumber() != exps.size()){
                errorRecorder.paramNumNotMatch(ident.line(), ident.getValue(), funcType.getParamNumber(), exps.size());
            }
            for(int i = 0; i < funcType.getParamNumber(); i++){
                Exp exp;
                if(exps.size() <= i){
                    exp = new Number(0);
                }else{
                    exp = exps.get(i);
                }
                VarType paramType = funcType.getParams().get(i);
                ExpGenerator.Result expResult = new ExpGenerator(exp).getResult();
                if(expResult instanceof ExpGenerator.RValueResult){
                    if(!paramType.match(new IntType())){
                        errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType, new IntType());
                    }
                    addInstrument(new Param(((ExpGenerator.RValueResult) expResult).rValue));
                }else if(expResult instanceof ExpGenerator.PointerResult){
                    if(!paramType.match(((ExpGenerator.PointerResult) expResult).type)){
                        errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType, ((ExpGenerator.PointerResult) expResult).type);
                    }
                    addInstrument(new Param(((ExpGenerator.PointerResult) expResult).value));
                }else{
                    errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType);
                    addInstrument(new Param(new Constant(0)));
                }
            }
            if(funcType.isReturn()){
                Temp ret = valueFactory.newTemp();
                addInstrument(new Call(function, exps.size(), ret));
                result = ret;
            }else{
                addInstrument(new Call(function, exps.size()));
            }
        }
    }


}
