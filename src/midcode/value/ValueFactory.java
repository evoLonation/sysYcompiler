package midcode.value;

import lexer.Ident;
import type.ArrayType;
import type.IntType;
import type.PointerType;
import type.SymbolTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValueFactory {
    private final SymbolTable symbolTable = SymbolTable.getInstance();

    private int tempNumber;

    public Temp newTemp(){
        return new Temp("temp%" + ++tempNumber);
    }

    private final Map<SymbolTable.VariableInfo, Integer> numberMap = new HashMap<>();
    private final Map<SymbolTable.VariableInfo, Variable> variableMap = new HashMap<>();

    /**
     * 指针类型变量并不会被改变，因此不用重新赋值（非SSA形式）
     */
    public PointerValue newPointer(Ident ident, RValue offset){
        String symbol = ident.getValue();
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.type instanceof PointerType;
        PointerType type = (PointerType) variableInfo.type;
        PointerValue.Type pointerType;
        if(type instanceof ArrayType){
            pointerType = PointerValue.Type.array;
        }else{
            pointerType = PointerValue.Type.pointer;
        }
        return new PointerValue(symbol, offset, false, variableInfo.offset, pointerType);
    }

    public Variable newVariable(Ident ident) {
        String symbol = ident.getValue();
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.type instanceof IntType;
        int number;
        if(numberMap.containsKey(variableInfo)){
            number = numberMap.get(variableInfo) + 1;
        }else{
            number = 1;
        }
        numberMap.replace(variableInfo, number);
        Variable ret = new Variable(symbol + "%" + number, variableInfo.isGlobal, variableInfo.offset);
        variableMap.replace(variableInfo, ret);
        return ret;
    }

    public Variable getNewestVariable(Ident ident){
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.type instanceof IntType;
        return variableMap.get(variableInfo);
    }

    static private final ValueFactory instance = new ValueFactory();
    static public ValueFactory getInstance(){
        return instance;
    }
}
