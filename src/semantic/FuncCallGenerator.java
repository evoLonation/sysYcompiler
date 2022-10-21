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
            assert funcType.getParamNumber() == exps.size();
            for(int i = 0; i < exps.size(); i++){
                Exp exp = exps.get(i);
                VarType paramType = funcType.getParams().get(i);
                ExpGenerator.Result expResult = new ExpGenerator(instruments, exp).getResult();
                if(expResult instanceof ExpGenerator.RValueResult){
                    assert paramType.match(new IntType());
                    addInstrument(new Param(((ExpGenerator.RValueResult) expResult).rValue));
                }else if(expResult instanceof ExpGenerator.PointerResult){
                    assert paramType.match(((ExpGenerator.PointerResult) expResult).type);
                    addInstrument(new Param(((ExpGenerator.PointerResult) expResult).value));
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
