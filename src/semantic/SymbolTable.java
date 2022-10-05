package semantic;

import error.ErrorRecorder;
import lexer.Ident;
import type.FuncType;
import type.Type;
import type.VarType;

import java.util.*;

public class SymbolTable {
    private final List<SymbolTable> sons = new ArrayList<>();
    private SymbolTable father;
    private final Map<String, VarType> variableSymbols = new HashMap<>();
    private final Map<String, FuncType> funcSymbols = new HashMap<>();
    private final ErrorRecorder errorRecorder;
    public SymbolTable(SymbolTable father) {
        this.father = father;
        father.sons.add(this);
        this.errorRecorder = father.errorRecorder;
    }

    public SymbolTable(ErrorRecorder errorRecorder) {
        this.errorRecorder = errorRecorder;
    }

    public Optional<VarType> getVariableSymbol(Ident ident){
        if(isSymbolExist(ident.getValue())){
            return Optional.of(variableSymbols.get(ident.getValue()));
        }else if(father != null){
            return father.getVariableSymbol(ident);
        }else{
            errorRecorder.undefined(ident.line(), ident.getValue());
            return Optional.empty();
        }
    }
    public Optional<FuncType> getFuncSymbol(Ident ident) {
        if(isSymbolExist(ident.getValue())){
            return Optional.of(funcSymbols.get(ident.getValue()));
        }else if(father != null){
            return father.getFuncSymbol(ident);
        }else{
            errorRecorder.undefined(ident.line(), ident.getValue());
            return Optional.empty();
        }
    }

    /**
     * 只会检查本层是否有
     * @param ident
     * @return
     */
    public boolean isSymbolExist(String ident){
        return variableSymbols.containsKey(ident) || funcSymbols.containsKey(ident);
    }

    public SymbolTable father(){
        return father;
    }

    public void addVariableSymbol(Ident ident, VarType type){
        if(isSymbolExist(ident.getValue())){
            errorRecorder.redefined(ident.line(), ident.getValue());
        }else{
            variableSymbols.put(ident.getValue(), type);
        }
    }
    public void addFuncSymbol(Ident ident, FuncType type){
        if(isSymbolExist(ident.getValue())){
            errorRecorder.redefined(ident.line(), ident.getValue());
        }else {
            funcSymbols.put(ident.getValue(), type);
        }
    }



}



