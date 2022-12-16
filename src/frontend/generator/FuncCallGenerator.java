package frontend.generator;

import frontend.lexer.Ident;
import midcode.Function;
import midcode.instruction.*;
import midcode.value.*;
import frontend.parser.nonterminal.exp.*;
import frontend.parser.nonterminal.exp.Number;
import frontend.type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FuncCallGenerator extends SequenceGenerator {
    private final FuncCall funcCall;

    FuncCallGenerator(FuncCall funcCall) {
        this.funcCall = funcCall;
    }

    Optional<LValue> generate() {
        LValue result = null;
        Ident ident = funcCall.getIdent();
        List<Exp> exps = funcCall.getExps();
        Optional<SymbolTable.FunctionInfo> infoOptional = symbolTable.getFunction(ident);
        if(!infoOptional.isPresent()){
            Temp temp = valueFactory.newTemp();
            addSequence(new Assignment(temp, new Constant(0)));
            result = temp;
        }else{
            FuncType funcType = infoOptional.get().type;
            Function function = infoOptional.get().function;
            if(funcType.getParamNumber() != exps.size()){
                errorRecorder.paramNumNotMatch(ident.line(), ident.getValue(), funcType.getParamNumber(), exps.size());
            }
            List<Value> params = new ArrayList<>();
            for(int i = 0; i < funcType.getParamNumber(); i++){
                Exp exp;
                if(exps.size() <= i){
                    exp = new Number(0);
                }else{
                    exp = exps.get(i);
                }
                VarType paramType = funcType.getParams().get(i);
                ExpGenerator.Result expResult = new ExpGenerator(exp).generate();
                if(expResult instanceof ExpGenerator.RValueResult){
                    if(!paramType.match(new IntType())){
                        errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType, new IntType());
                    }
                    params.add(((ExpGenerator.RValueResult) expResult).rValue);
//                    addInstrument(new Param(((ExpGenerator.RValueResult) expResult).rValue));
                }else if(expResult instanceof ExpGenerator.PointerResult){
                    if(!paramType.match(((ExpGenerator.PointerResult) expResult).type)){
                        errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType, ((ExpGenerator.PointerResult) expResult).type);
                    }
                    params.add(((ExpGenerator.PointerResult) expResult).value);
//                    addInstrument(new Param(((ExpGenerator.PointerResult) expResult).value));
                }else{
                    errorRecorder.paramTypeNotMatch(ident.line(), ident.getValue(), paramType);
                    params.add(new Constant(0));
//                    addInstrument(new Param(new Constant(0)));
                }
            }
            params.forEach(value -> addSequence(new Param(value)));
            if(funcType.isReturn()){
                Temp ret = valueFactory.newTemp();
                addSequence(new Call(function, ret));
                result = ret;
            } else {
                addSequence(new Call(function, params));
            }
        }
        return Optional.ofNullable(result);
    }


}
