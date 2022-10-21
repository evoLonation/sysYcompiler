package type;

import error.ErrorRecorder;
import lexer.Ident;
import midcode.Function;

import java.util.*;

public class SymbolTable {
    private final Stack<Map<String, VariableInfo>> localVariableStack = new Stack<>();
    private final Stack<Integer> offsetStack = new Stack<>();
    private final Map<String, FunctionInfo> functionMap = new HashMap<>();
    private final Map<String, VariableInfo> globalVariableMap = new HashMap<>();
    private int currentGlobalOffset = 0;
    private int currentTotalOffset = 0;
    private int currentBlockOffset = 0;
    private String currentFunction;


    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();

    public SymbolTable() {}

    public static class VariableInfo {
        public VarType type;
        public int offset;
        public boolean isGlobal;
        public boolean isArray;

        public VariableInfo(VarType type, int offset, boolean isGlobal) {
            this.type = type;
            this.offset = offset;
            this.isGlobal = isGlobal;
        }
    }

    public Optional<VariableInfo> getVariable(Ident ident) {
        String symbol = ident.getValue();
        int line = ident.line();
        for(int i = localVariableStack.capacity() - 1; i >= 0; i --){
            Map<String, VariableInfo> localVariableMap = localVariableStack.get(i);
            if( localVariableMap.containsKey(symbol) ) {
                return Optional.of(localVariableMap.get(symbol));
            }
        }
        if(globalVariableMap.containsKey(symbol)){
            return Optional.of(globalVariableMap.get(symbol));
        }
        errorRecorder.undefined(line, symbol);
        return Optional.empty();
    }

    public static class FunctionInfo {
        public FuncType type;
        public Function function;

        public FunctionInfo(FuncType type, Function function) {
            this.type = type;
            this.function = function;
        }
    }

    public Optional<FunctionInfo> getFunction(Ident ident) {
        String symbol = ident.getValue();
        int line = ident.line();
        if(functionMap.containsKey(symbol)){
            return Optional.of(functionMap.get(ident.getValue()));
        }
        errorRecorder.undefined(line, symbol);
        return Optional.empty();
    }

    // 检查当前作用域的变量重名或者函数重名
    private boolean isLocalConflict(String symbol){
        return localVariableStack.peek().containsKey(symbol) || functionMap.containsKey(symbol);
    }
    private boolean isGlobalConflict(String symbol){
        return globalVariableMap.containsKey(symbol) || functionMap.containsKey(symbol);
    }

    public void addLocalVariable(Ident ident, VarType type){
        String symbol = ident.getValue();
        int line = ident.line();
        if(isLocalConflict(symbol)){
            errorRecorder.redefined(line, symbol);
        }else{
            localVariableStack.peek().put(symbol, new VariableInfo(type, currentTotalOffset, false));
            currentTotalOffset += type.getSize();
            currentBlockOffset += type.getSize();
        }
    }

    public void addGlobalVariable(Ident ident, VarType type){
        String symbol = ident.getValue();
        int line = ident.line();
        if(isGlobalConflict(symbol)){
            errorRecorder.redefined(line, symbol);
        }else{
            globalVariableMap.put(symbol, new VariableInfo(type, currentGlobalOffset, true));
            currentGlobalOffset += type.getSize();
        }
    }

    // 该方法会进入一个函数的嵌套作用域
    public Function addFunc(Ident ident, boolean isReturn) {
        assert localVariableStack.isEmpty();
        String symbol = ident.getValue();
        int line = ident.line();
        Function ret = new Function();
        if(isGlobalConflict(symbol)){
            errorRecorder.redefined(line, symbol);
        }else {
            FuncType type = new FuncType(isReturn);
            functionMap.put(symbol, new FunctionInfo(type, ret));
        }
        currentFunction = symbol;
        localVariableStack.push(new HashMap<>());
        return ret;
    }

    public void addParam(Ident ident, VarType type){
        functionMap.get(currentFunction).type.addParam(type);
        addLocalVariable(ident, type);
    }

    public void newBlock() {
        localVariableStack.push(new HashMap<>());
        offsetStack.push(currentBlockOffset);
    }


    public int outBlock() {
        localVariableStack.pop();
        int ret = currentTotalOffset;
        currentTotalOffset -= currentBlockOffset;
        currentBlockOffset = offsetStack.pop();
        return ret;
    }

    static private final SymbolTable instance = new SymbolTable();
    static public SymbolTable getInstance(){
        return instance;
    }

}



