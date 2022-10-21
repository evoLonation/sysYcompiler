package semantic;

import lexer.Ident;
import midcode.Function;
import midcode.instrument.*;
import midcode.value.*;
import parser.nonterminal.exp.*;
import type.*;

import java.util.List;
import java.util.Optional;

public class FuncCallGenerator extends InstrumentGenerator{
    private final FuncCall funcCall;

    public FuncCallGenerator(List<Instrument> instruments, FuncCall funcCall) {
        super(instruments);
        this.funcCall = funcCall;
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
            check(funcType.getParamNumber() == exps.size());
            // 每个exp类型是数组当且仅当其为BinaryExp且exp1为数组的Lval,或者其本身为LVal
            for(int i = 0; i < exps.size(); i++){
                Exp exp = exps.get(i);
                VarType paramType = funcType.getParams().get(i);
                ExpGenerator.Result expResult = new ExpGenerator(instruments, exp).getResult();
                check(paramType.match(expResult.type));
                addInstrument(new Param(expResult.value));
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
