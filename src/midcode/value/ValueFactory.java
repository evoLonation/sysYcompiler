package midcode.value;

import frontend.lexer.Ident;
import frontend.type.ArrayType;
import frontend.type.IntType;
import frontend.type.PointerType;
import frontend.type.SymbolTable;

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
    // todo need clean
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


    // todo ssa
    // now is not ssa
    // the same local variable will return the same variable object
    public Variable newVariable(Ident ident) {
        String symbol = ident.getValue();
        Optional<SymbolTable.VariableInfo> optionalVariableInfo = symbolTable.getVariable(ident);
        assert optionalVariableInfo.isPresent();
        SymbolTable.VariableInfo variableInfo = optionalVariableInfo.get();
        assert variableInfo.getType() instanceof IntType;
        if(variableMap.containsKey(variableInfo)){
            return variableMap.get(variableInfo);
        }else{
            Variable ret = new Variable(symbol + "#" + variableInfo.getLayer(), variableInfo.isGlobal(), variableInfo.getOffset());
            variableMap.put(variableInfo, ret);
            return ret;
        }
//        int number;
//        if(numberMap.containsKey(variableInfo)){
//            number = numberMap.get(variableInfo) + 1;
//        }else{
//            number = 1;
//        }
//        numberMap.put(variableInfo, number);
//        Variable ret = new Variable(symbol + "#" + variableInfo.getLayer() + "%" + number, variableInfo.isGlobal(), variableInfo.getOffset());
//        variableMap.put(variableInfo, ret);
//        return ret;
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
