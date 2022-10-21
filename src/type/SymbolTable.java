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
    private final Map<VarType, Integer> constIntMap = new HashMap<>();
    private final Map<VarType, int[]> constArrayMap = new HashMap<>();
    private int currentGlobalOffset = 0;
    private int currentTotalOffset = 0;
    private int currentBlockOffset = 0;
    private String currentFunction;


    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();


    public static class VariableInfo {
        public VarType type;
        public int offset;
        public boolean isGlobal;

        public VariableInfo(VarType type, int offset, boolean isGlobal) {
            this.type = type;
            this.offset = offset;
            this.isGlobal = isGlobal;
        }

        public Optional<Integer> getConstInteger(){
            return Optional.empty();
        }
        public Optional<int[]> getConstArray(){
            return Optional.empty();
        }
    }
    private static class ConstIntVariableInfo extends VariableInfo{
        private final int constValue;

        public ConstIntVariableInfo(VarType type, int offset, boolean isGlobal, int constValue) {
            super(type, offset, isGlobal);
            this.constValue = constValue;
        }
        public Optional<Integer> getConstInteger(){
            return Optional.of(constValue);
        }
    }
    private static class ConstArrayVariableInfo extends VariableInfo{
        private final int[] constValue;

        public ConstArrayVariableInfo(VarType type, int offset, boolean isGlobal, int[] constValue) {
            super(type, offset, isGlobal);
            this.constValue = constValue;
        }
        public Optional<int[]> getConstArray(){
            return Optional.of(constValue);
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

    public void newInteger(Ident ident, boolean isGlobal){
        addVariable(ident, new VariableInfo(new IntType(), currentGlobalOffset, isGlobal));
    }

    public void newInteger(Ident ident, boolean isGlobal, int constValue){
        addVariable(ident, new ConstIntVariableInfo(new IntType(), currentGlobalOffset, isGlobal, constValue));
    }



    public void newArray(Ident ident, boolean isGlobal, ArrayType type, int[] constValue) {
        addVariable(ident, new ConstArrayVariableInfo(type, currentGlobalOffset, isGlobal, constValue));
    }

    public void newArray(Ident ident, boolean isGlobal, ArrayType type){
        addVariable(ident, new VariableInfo(type, currentGlobalOffset, isGlobal));
    }

    private void addVariable(Ident ident, VariableInfo variableInfo) {
        boolean isGlobal = variableInfo.isGlobal;
        String symbol = ident.getValue();
        int line = ident.line();
        int size = variableInfo.type.getSize();
        if(isGlobal){
            if(isGlobalConflict(symbol)){
                errorRecorder.redefined(line, symbol);
            }else{
                globalVariableMap.put(symbol, variableInfo);
                currentGlobalOffset += size;
            }
        }else{
            if(isLocalConflict(symbol)){
                errorRecorder.redefined(line, symbol);
            }else{
                localVariableStack.peek().put(symbol, variableInfo);
                currentTotalOffset += size;
                currentBlockOffset += size;
            }
        }
    }

    // 该方法会进入一个函数的嵌套作用域
    public void addFunc(Function function, Ident ident, boolean isReturn) {
        assert localVariableStack.isEmpty();
        String symbol = ident.getValue();
        int line = ident.line();
        if(isGlobalConflict(symbol)){
            errorRecorder.redefined(line, symbol);
        }else {
            FuncType type = new FuncType(isReturn);
            functionMap.put(symbol, new FunctionInfo(type, function));
        }
        currentFunction = symbol;
        localVariableStack.push(new HashMap<>());
    }

    public void addParam(Ident ident, VarType type){
        assert ! (type instanceof ArrayType);
        functionMap.get(currentFunction).type.addParam(type);
        addVariable(ident, new VariableInfo(type, currentTotalOffset, false));
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

    public int getCurrentTotalOffset() {
        return currentTotalOffset;
    }

    public int getCurrentGlobalOffset() {
        return currentGlobalOffset;
    }

    private SymbolTable() {}
    static private final SymbolTable instance = new SymbolTable();
    static public SymbolTable getInstance(){
        return instance;
    }

}



