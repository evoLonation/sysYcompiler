import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    private List<SymbolTable> sons = new ArrayList<>();
    private SymbolTable father;
    private List<Symbol> symbols = new ArrayList<>();
    public SymbolTable(SymbolTable father) {
        this.father = father;
        father.sons.add(this);
    }

    public SymbolTable() {

    }



    public SymbolTable father(){
        return father;
    }

    /**
     * @throws CompileException
     */
    public void addFuncSymbol(String ident, List<Integer> argList){
        symbols.add(new FuncSymbol(ident, argList));
    }

    public void addVariable(String ident, boolean isConst, int dimension){
        symbols.add(new VariableSymbol(ident, isConst, dimension));
    }



}
abstract class Symbol {
    private String value;

    public String getValue() {
        return value;
    }

    public Symbol(String value) {
        this.value = value;
    }
}
class VariableSymbol extends Symbol{
    boolean isConst;
    int dimension;
    public VariableSymbol(String ident, boolean isConst, int dimension) {
        super(ident);
        this.isConst = isConst;
        this.dimension = dimension;
    }

}
class FuncSymbol extends Symbol{
    List<Integer> argList;
    public FuncSymbol(String ident, List<Integer> argList) {
        super(ident);
        this.argList = argList;
    }
}


