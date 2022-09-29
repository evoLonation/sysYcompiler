package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private List<SymbolTable> sons = new ArrayList<>();
    private SymbolTable father;
    private Map<String, Type> symbols = new HashMap<>();
    public SymbolTable(SymbolTable father) {
        this.father = father;
        father.sons.add(this);
    }

    public SymbolTable() {

    }

    public Type getSymbol(String ident){
        if(isSymbolExist(ident)){
            return symbols.get(ident);
        }else if(father != null){
            return father.getSymbol(ident);
        }else{
            return null;
        }
    }

    /**
     * 只会检查本层是否有
     * @param ident
     * @return
     */
    public boolean isSymbolExist(String ident){
        return symbols.containsKey(ident);
    }

    public SymbolTable father(){
        return father;
    }

    public void addSymbol(String ident, Type type){
        symbols.put(ident, type);
    }

}
abstract class Type {
}

class FuncType extends Type {
    List<VarType> params;
    boolean isReturn;
    public FuncType(boolean isReturn, List<VarType> argList) {
        this.isReturn = isReturn;
        this.params = argList;
    }
}

class VarType extends Type{
    private boolean isConst;
    protected int dimension;
    // 第二维（若有）的长度
    protected int secondLen;
    public boolean isConst() {
        return isConst;
    }
    public VarType(boolean isConst, int dimension, int secondLen) {
        this.dimension = dimension;
        this.secondLen = secondLen;
        this.isConst = isConst;
    }
    public VarType(boolean isConst, int dimension) {
        this(isConst, dimension, 0);
    }
    public VarType(int dimension, int secondLen) {
        this(false, dimension, secondLen);
    }
    public VarType(int dimension) {
        this(false, dimension, 0);
    }


    public boolean match(VarType varType){
        if(!varType.isConst()) {
            if (dimension == varType.dimension) {
                if(dimension != 2 || varType.secondLen == secondLen){
                    return true;
                }
            }
        }
        return false;
    }
}

// 在类型的基础上增加了初始化时需要的信息(第一维的长度)
class DeclType extends VarType {
    // 当且仅当dimension是2时len有意义，代表第二个维度的长度
    private int firstLen;
    public int getFirstLen() {
        return firstLen;
    }
    public DeclType(boolean isConst, int dimension, int... lens) {
        super(isConst, dimension);
        if(dimension == 2){
            firstLen = lens[0];
            secondLen = lens[1];
        }else if(dimension == 1){
            firstLen = lens[0];
        }
    }
    public DeclType(int dimension, int... lens) {
        this(false, dimension, lens);
    }

    private int initVal0;
    private int[] initVal1;
    private int[][] initVal2;

    // init仅当const为1时有用
    public void init(int initVal){
        initVal0 = initVal;
    }
    public void init(int[] initVal){
        initVal1 = initVal;
    }
    public void init(int[][] initVal){
        initVal2 = initVal;
    }

    public int get(){
        return initVal0;
    }
    public int get(int i){
        return initVal1[i];
    }
    public int get(int i, int j){
        return initVal2[i][j];
    }

}



