package frontend.type;

import frontend.error.ErrorRecorder;
import frontend.lexer.Ident;
import midcode.Function;

import java.util.*;

// 管理作用域与作用域中的符号
public class SymbolTable {
    private final Stack<Map<String, VariableInfo>> localVariableStack = new Stack<>();
    private final Stack<Integer> offsetStack = new Stack<>();
    private final Map<String, FunctionInfo> functionMap = new HashMap<>();
    private final Map<String, VariableInfo> globalVariableMap = new HashMap<>();
    private int currentGlobalOffset = 0;

    private int currentTotalOffset = 0;
    private int currentBlockOffset = 0;
    private int currentMaxOffset = 0;

    private FunctionInfo currentFunction;

    public boolean nowIsReturn(){
        assert !localVariableStack.empty();
        return currentFunction.type.isReturn;
    }


    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();


    public class VariableInfo {
        private final VarType type;
        private final int offset;
        private final int layer;

        public VariableInfo(VarType type, int layer) {
            this.type = type;
            this.offset = layer == 0 ? currentGlobalOffset : currentTotalOffset + type.getSize() - 1;
            this.layer = layer;
        }

        public Optional<Integer> getConstInteger(){
            return Optional.empty();
        }
        public Optional<int[]> getConstArray(){
            return Optional.empty();
        }

        public VarType getType() {
            return type;
        }

        public int getOffset() {
            return offset;
        }

        public int getLayer() {
            return layer;
        }

        public boolean isGlobal(){
            return layer == 0;
        }

    }
    private class ConstIntVariableInfo extends VariableInfo{
        private final int constValue;

        public ConstIntVariableInfo(VarType type, int layer, int constValue) {
            super(type, layer);
            this.constValue = constValue;
        }
        public Optional<Integer> getConstInteger(){
            return Optional.of(constValue);
        }
    }

    private class ConstArrayVariableInfo extends VariableInfo{
        private final int[] constValue;

        public ConstArrayVariableInfo(VarType type, int layer, int[] constValue) {
            super(type, layer);
            this.constValue = constValue;
        }
        public Optional<int[]> getConstArray(){
            return Optional.of(constValue);
        }
    }

    public Optional<VariableInfo> getVariable(Ident ident) {
        String symbol = ident.getValue();
        int line = ident.line();
        for(int i = localVariableStack.size() - 1; i >= 0; i --){
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
        return localVariableStack.peek().containsKey(symbol);
    }
    private boolean isGlobalConflict(String symbol){
        return globalVariableMap.containsKey(symbol) || functionMap.containsKey(symbol);
    }

    public void newInteger(Ident ident, boolean isGlobal){
        addVariable(ident, new VariableInfo(new IntType(), computeLayer(isGlobal)), isGlobal);
    }

    public void newInteger(Ident ident, boolean isGlobal, int constValue){
        addVariable(ident, new ConstIntVariableInfo(new IntType(), computeLayer(isGlobal), constValue), isGlobal);
    }



    public void newArray(Ident ident, boolean isGlobal, ArrayType type, int[] constValue) {
        addVariable(ident, new ConstArrayVariableInfo(type, computeLayer(isGlobal), constValue), isGlobal);
    }

    public void newArray(Ident ident, boolean isGlobal, ArrayType type){
        addVariable(ident, new VariableInfo(type, computeLayer(isGlobal)), isGlobal);
    }

    private int computeLayer(boolean isGlobal){
        return isGlobal ?0 : localVariableStack.size();
    }

    private void addVariable(Ident ident, VariableInfo variableInfo, boolean isGlobal) {
        String symbol = ident.getValue();
        int line = ident.line();
        if(isGlobal){
            if(isGlobalConflict(symbol)){
                errorRecorder.redefined(line, symbol);
            }else{
                globalVariableMap.put(symbol, variableInfo);
                pushStack(variableInfo, true);
            }
        }else{
            if(isLocalConflict(symbol)){
                errorRecorder.redefined(line, symbol);
            }else{
                localVariableStack.peek().put(symbol, variableInfo);
                pushStack(variableInfo, false);
            }
        }
    }

    private void pushStack(VariableInfo variableInfo, boolean isGlobal){
        // 常量int不用压栈
        if((variableInfo.getConstArray().isPresent() || variableInfo.getConstInteger().isPresent()) && variableInfo.type instanceof IntType){
            return;
        }
        int size = variableInfo.type.getSize();
        if(isGlobal){
            currentGlobalOffset += size;
        }else{
            currentTotalOffset += size;
            if(currentTotalOffset > currentMaxOffset) currentMaxOffset = currentTotalOffset;
            currentBlockOffset += size;
        }
    }

    // 该方法会进入一个函数的嵌套作用域
    public void newFuncDomain(Function function, Ident ident, boolean isReturn) {
        assert localVariableStack.isEmpty();
        currentMaxOffset = 0;
        String symbol = ident.getValue();
        int line = ident.line();
        FunctionInfo functionInfo = new FunctionInfo(new FuncType(isReturn), function);
        if(isGlobalConflict(symbol)){
            errorRecorder.redefined(line, symbol);
            functionMap.replace(symbol, functionInfo);
        }else{
            functionMap.put(symbol, functionInfo);
        }
        currentFunction = functionInfo;
        newBlock();
    }

    public void addParam(Ident ident, VarType type){
        assert ! (type instanceof ArrayType);
        currentFunction.type.addParam(type);
        addVariable(ident, new VariableInfo(type, computeLayer(false)), false);
    }

    public void newMain() {
        assert localVariableStack.isEmpty();
        currentMaxOffset = 0;
        currentFunction = new FunctionInfo(new FuncType(true), null);
        newBlock();
    }

    public void newBlock() {
        localVariableStack.push(new HashMap<>());
        offsetStack.push(currentBlockOffset);
        currentBlockOffset = 0;
    }


    public void outBlock() {
        localVariableStack.pop();
        currentTotalOffset -= currentBlockOffset;
        currentBlockOffset = offsetStack.pop();
    }

    public int getMaxOffset() {
        assert localVariableStack.empty();
        return currentMaxOffset;
    }

    private SymbolTable() {}
    static private final SymbolTable instance = new SymbolTable();
    static public SymbolTable getInstance(){
        return instance;
    }

}



