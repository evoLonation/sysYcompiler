import java.util.ArrayList;
import java.util.List;

public class SemanticChecker {
    private final SymbolTable rootSymbolTable;
    private SymbolTable nowSymbolTable;

    private final Node root;
    private Node nowNode;

    public SemanticChecker(Node root) {
        rootSymbolTable = new SymbolTable();
        this.root = root;
    }

    public SymbolTable symbolTable(){
        return rootSymbolTable;
    }

    public void exec(){
        nowSymbolTable = rootSymbolTable;
        nowNode = root;
        CompUnit();
    }
    private void CompUnit (){
        int i = 0;
        while(is(i, Nonterminal.Decl)){
            Decl(i);
            i ++;
        }
        while(is(i, Nonterminal.FuncDef)){
            FuncDef(i);
            i ++;
        }
        MainFuncDef(i);
    }

    private void MainFuncDef (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        Block(4);

        nowNode = lastNode;
    }
    private void Decl (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        if(is(0, Nonterminal.ConstDecl)){
            ConstDecl(0);
        }else if(is(0, Nonterminal.VarDecl)){
            VarDecl(0);
        }

        nowNode = lastNode;
    }

    public void FuncDef(int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        
        if(is(3, Nonterminal.FuncFParams)){
            List<Param> params = FuncFParams(3);
            List<VarType> types = new ArrayList<>();
            params.forEach((p) -> {types.add(p.type);});
            addSymbol(Ident(1), new FuncType(FuncType(0), types));
            Block(5, params);
        }else{
            addSymbol(Ident(1), new FuncType(FuncType(0), new ArrayList<>()));
            Block(4);
        }

        nowNode = lastNode;
    }

    private boolean FuncType (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        boolean ret = is(0, Terminal.INTTK);

        nowNode = lastNode;
        return ret;
    }
    
    static class Param{
        public String ident;
        public VarType type;
        public Param(String ident, VarType type) {
            this.ident = ident;
            this.type = type;
        }
    }
    private Param FuncFParam (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        String ident = Ident(1);
        checkSymbolRepeat(ident);
        int dimension;
        VarType ret = null;
        if(nowNode.listSons().size() >= 5){
            dimension = 2;
            ret = new VarType(dimension, ConstExp(5));
        }else if(nowNode.listSons().size() >= 3){
            dimension = 1;
            ret = new VarType(dimension);
        }else {
            dimension = 0;
            ret = new VarType(dimension);
        }

        nowNode = lastNode;
        return new Param(ident, ret);
    }

    private List<Param> FuncFParams(int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        List<Param> ret = new ArrayList<>();
        int i = 0;
        while(true){
            ret.add(FuncFParam(i));
            i ++;
            if(!is(i, Terminal.COMMA)){
                break;
            }
            i++;
        }

        nowNode = lastNode;
        return ret;
    }

    private void Block (int location, List<Param> params){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        down();
        params.forEach((p) -> {addSymbol(p.ident, p.type);});
        int i = 1;
        while(is(i, Nonterminal.BlockItem)){
            BlockItem(i);
            i++;
        }
        up();

        nowNode = lastNode;
    }

    private void Block(int location){
        Block(location, new ArrayList<>());
    }

    private void BlockItem (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        if(is(0, Nonterminal.Decl)){
            Decl(0);
        }

        nowNode = lastNode;
    }

    private void ConstDecl (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        int i = 2;
        while(true){
            ConstDef(i);
            i++;
            if(!is(i, Terminal.COMMA)){
                break;
            }
            i++;
        }
        nowNode = lastNode;
    }
    private void VarDecl (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        int i = 1;
        while(true){
            VarDef(i);
            i++;
            if(!is(i, Terminal.COMMA)){
                break;
            }
            i++;
        }
        nowNode = lastNode;
    }

    private void ConstDef (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        String ident = Ident(0);
        checkSymbolRepeat(ident);
        int dimension = (nowNode.listSons().size() - 3) /3;
        DeclType type = null;
        if(dimension == 2){
            type = new DeclType(true, dimension, ConstExp(2), ConstExp(5));
            type.init(ConstInitVal2(8));
        }else if(dimension == 1){
            type = new DeclType(true, dimension, ConstExp(2));
            type.init(ConstInitVal1(5));
        }else if(dimension == 0){
            type = new DeclType(true, dimension);
            type.init(ConstInitVal0(2));
        }
        addSymbol(ident, type);

        nowNode = lastNode;
    }

    private void VarDef (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        String ident = Ident(0);
        checkSymbolRepeat(ident);
        int dimension = 0;
        if(is(nowNode.listSons().size() - 1, Nonterminal.InitVal)){
            dimension = (nowNode.listSons().size() - 3) /3;
        }else{
            dimension = (nowNode.listSons().size() - 1) /3;
        }
        DeclType type = null;
        if(dimension == 2){
            type = new DeclType(dimension, ConstExp(2), ConstExp(5));
        }else if(dimension == 1){
            type = new DeclType(dimension, ConstExp(2));
        }else if(dimension == 0){
            type = new DeclType(dimension);
        }

        addSymbol(ident, type);

        nowNode = lastNode;
    }

    private DeclType getDefType(int dimension){
        DeclType type = null;
        if(dimension == 2){
            type = new DeclType(true, dimension, ConstExp(2), ConstExp(5));
            type.init(ConstInitVal2(8));
        }else if(dimension == 1){
            type = new DeclType(true, dimension, ConstExp(2));
        }else if(dimension == 0){
            type = new DeclType(true, dimension);
        }
        return type;
    }

    private int ConstInitVal0 (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int ret = ConstExp(0);

        nowNode = lastNode;
        return ret;
    }

    private int[] ConstInitVal1 (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int len = (nowNode.listSons().size() - 1) / 2;
        int[] ret = new int[len];
        for(int i = 0; i < len; i ++){
            ret[i] = ConstInitVal0(i * 2 + 1);
        }

        nowNode = lastNode;
        return ret;
    }

    private int[][] ConstInitVal2 (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int len = (nowNode.listSons().size() - 1) / 2;
        int[][] ret = new int[len][];
        for(int i = 0; i < len; i ++){
            ret[i] = ConstInitVal1(i * 2 + 1);
        }

        nowNode = lastNode;
        return ret;
    }

    private int ConstExp (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int ret = ConstAddMulExp(0);

        nowNode = lastNode;
        return ret;
    }
    private int ConstAddMulExp (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int ret = 0;
        if(!nowNode.word().typeEqual(nowNode.son(0).word())){
            if(is(0, Nonterminal.UnaryExp)){
                ret = ConstUnaryExp(0);
            }else{
                ret = ConstAddMulExp(0);
            }
        }else{
            switch (nowNode.son(1).word().getType()){
                case Terminal.PLUS:
                    ret = ConstAddMulExp(0) + ConstAddMulExp(2);
                    break;
                case Terminal.MINU:
                    ret = ConstAddMulExp(0) - ConstAddMulExp(2);
                    break;
                case Terminal.MULT:
                    ret = ConstAddMulExp(0) * ConstUnaryExp(2);
                    break;
                case Terminal.DIV:
                    ret = ConstAddMulExp(0) / ConstUnaryExp(2);
                    break;
                case Terminal.MOD:
                    ret = ConstAddMulExp(0) % ConstUnaryExp(2);
                    break;
            }
        }

        nowNode = lastNode;
        return ret;
    }
    private int ConstUnaryExp(int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        if(is(0, Terminal.IDENFR)){
            throw new CompileException();
        }
        int ret = ConstPrimaryExp(0);

        nowNode = lastNode;
        return ret;
    }

    private int ConstPrimaryExp (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int ret = 0;
        if(is(0, Terminal.LPARENT)){
            ret = ConstExp(1);
        }else if(is(0, Nonterminal.LVal)){
            ret = ConstLVal(0);
        }else if(is(0, Nonterminal.Number)){
            ret = Number(0);
        }

        nowNode = lastNode;
        return  ret;
    }

    private int ConstLVal (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);

        int ret = 0;
        DeclType type = (DeclType) nowSymbolTable.getSymbol(((Terminal)nowNode.son(0).word()).getValue());
        if(type.dimension == 0){
            ret = type.get();
        }else if(type.dimension == 1){
            ret = type.get(ConstExp(2));
        }else if(type.dimension == 2){
            ret = type.get(ConstExp(2), ConstExp(5));
        }

        nowNode = lastNode;
        return ret;
    }




    private void checkSymbolRepeat(String ident){
        if(nowSymbolTable.isSymbolExist(ident)){
            throw new CompileException();
        }
    }

    private void checkSymbolUndefined(String ident){
        if(!nowSymbolTable.isSymbolExist(ident)){
            throw new CompileException();
        }
    }

    private void checkFuncParam(String ident, List<VarType> params){
        Type func = nowSymbolTable.getSymbol(ident);
        if(!(func instanceof FuncType)){
            throw new CompileException();
        }
        if(((FuncType) func).params.size() != params.size()){
            throw new CompileException();
        }
        for(int i = 0; i < ((FuncType) func).params.size(); i++){
            if(!((FuncType) func).params.get(i).match(params.get(i))){
                throw new CompileException();
            }
        }
    }

    private void checkConstLValModify(){

    }

    private void checkHasBreakOrContinue(){

    }
    private void checkReturn(boolean hasReturnValue){

    }




    private void checkInitVal(int dimension){

    }

    private String Ident(int i){
        return ((Terminal)nowNode.son(i).word()).getValue();
    }
    private int Number(int i){
        return ((IntConst)nowNode.son(i).son(0).word()).getDigitValue();
    }


    private boolean is(String type){
        return nowNode.word().typeOf(type);
    }
    private boolean is(int i, String type){
        if(nowNode.listSons().size() <= i)return false;
        return nowNode.son(i).word().typeOf(type);
    }

    // 新增符号表
    private void down(){
        nowSymbolTable = new SymbolTable(nowSymbolTable);
    }
    private void up(){
        nowSymbolTable = nowSymbolTable.father();
    }

    private void addSymbol(String ident, Type type){
        nowSymbolTable.addSymbol(ident, type);
    }


}
