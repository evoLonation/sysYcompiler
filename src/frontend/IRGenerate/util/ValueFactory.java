package frontend.IRGenerate.util;

import frontend.lexer.Ident;
import frontend.type.ArrayType;
import frontend.type.IntType;
import frontend.type.PointerType;
import frontend.type.SymbolTable;
import midcode.value.*;

import java.util.*;

public class ValueFactory {
    private final SymbolTable symbolTable = SymbolTable.getInstance();

    private int tempNumber;

    public Temp newTemp(){
        return new Temp("temp%" + ++tempNumber);
    }

    private final Map<SymbolTable.VariableInfo, Variable> variableMap = new HashMap<>();

    /**
     * 指针类型变量并不会被改变，因此不用重新赋值（非SSA形式）
     */
    public AddressValue newPointer(Ident ident, RValue offset){
        String symbol = ident.getValue();
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.getType() instanceof PointerType;
        PointerType type = (PointerType) variableInfo.getType();
        if(type instanceof ArrayType){
            return new ArrayValue(symbol + "#" + variableInfo.getLayer(), variableInfo.getOffset(), offset, variableInfo.isGlobal());
        }else{
            return new PointerValue(symbol + "#" + variableInfo.getLayer(), variableInfo.getOffset(), offset);
        }
    }


    public Variable newVariable(Ident ident) {
        String symbol = ident.getValue();
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.getType() instanceof IntType;
        if(variableMap.containsKey(variableInfo)){
            return variableMap.get(variableInfo);
        }else{
            Variable ret;
            if(variableInfo.getFunction().isPresent()){
                ret = new Variable(symbol + "#" + variableInfo.getLayer(), variableInfo.getFunction().get(), variableInfo.getOffset());
            }else{
                ret = new Variable(symbol + "#" + variableInfo.getLayer(), variableInfo.getOffset());
            }
            variableMap.put(variableInfo, ret);
            numberMap.put(ret, 0);
            return ret;
        }
    }


    private final Map<Variable, Integer> numberMap = new HashMap<>();

    public Variable newSubscriptVariable(Variable variable){
        int count = numberMap.get(variable);
        numberMap.put(variable, count + 1);
        if(variable.getFunction().isPresent()){
            return new Variable(variable.getName() + "@" + count, variable.getFunction().get(), variable.getOffset());
        }else{
            return new Variable(variable.getName() + "@" + count, variable.getOffset());
        }
    }

    public Variable getNewestVariable(Ident ident){
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.getType() instanceof IntType;
        return variableMap.get(variableInfo);
    }

    static private final ValueFactory instance = new ValueFactory();
    static public ValueFactory getInstance(){
        return instance;
    }
}
