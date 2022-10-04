package semantic;

import type.FuncType;
import type.Type;
import type.VarType;

import java.util.*;

public class SymbolTable {
    private final List<SymbolTable> sons = new ArrayList<>();
    private SymbolTable father;
    private final Map<String, VarType> variableSymbols = new HashMap<>();
    private final Map<String, FuncType> funcSymbols = new HashMap<>();
    public SymbolTable(SymbolTable father) {
        this.father = father;
        father.sons.add(this);
    }

    public SymbolTable() {

    }

    public Optional<VarType> getVariableSymbol(String ident){
        if(isSymbolExist(ident)){
            return Optional.of(variableSymbols.get(ident));
        }else if(father != null){
            return father.getVariableSymbol(ident);
        }else{
            return Optional.empty();
        }
    }
    public Optional<FuncType> getFuncSymbol(String ident) {
        if(isSymbolExist(ident)){
            return Optional.of(funcSymbols.get(ident));
        }else if(father != null){
            return father.getFuncSymbol(ident);
        }else{
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

    public void addVariableSymbol(String ident, VarType type){
        variableSymbols.put(ident, type);
    }
    public void addFuncSymbol(String ident, FuncType type){
        funcSymbols.put(ident, type);
    }



}



