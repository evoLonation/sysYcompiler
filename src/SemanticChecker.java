import java.util.ArrayList;
import java.util.List;

public class SemanticChecker {
    private final SymbolTable rootSymbolTable;
    private SymbolTable nowSymbolTable;

    private Node root;
    private Node nowNode;

    public SemanticChecker(Node root) {
        rootSymbolTable = new SymbolTable();
        this.root = root;
    }

    public SymbolTable symbolTable(){
        return rootSymbolTable;
    }

    public void check(){
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
            nowSymbolTable.addFuncSymbol(ident(1), FuncFParamsToFuncDef(3));
            nowSymbolTable = new SymbolTable(nowSymbolTable);
            FuncFParamsToBlock(3);
            FuncBlock(5);
            nowSymbolTable = nowSymbolTable.father();
        }else{
            nowSymbolTable.addFuncSymbol(ident(1), new ArrayList<>());
            nowSymbolTable = new SymbolTable(nowSymbolTable);
            FuncBlock(4);
            nowSymbolTable = nowSymbolTable.father();
        }
        nowNode = lastNode;
    }

    private void FuncFParamsToBlock (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        int i = 0;
        while(true){
            FuncFParamToBlock(i);
            i ++;
            if(!is(i, Terminal.COMMA)){
                break;
            }
            i++;
        }
        nowNode = lastNode;
    }
    private void FuncFParamToBlock (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        int dimension = 0;
        if(nowNode.listSons().size() >= 5){
            dimension = 2;
        }else if(nowNode.listSons().size() >= 3){
            dimension = 1;
        }
        nowSymbolTable.addVariable(((Terminal)nowNode.son(1).word()).getValue(), false, dimension);
        nowNode = lastNode;
    }

    private List<Integer> FuncFParamsToFuncDef(int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        List<Integer> ret = new ArrayList<>();
        int i = 0;
        while(true){
            ret.add(FuncFParamToFuncDef(i));
            i ++;
            if(!is(i, Terminal.COMMA)){
                break;
            }
            i++;
        }
        nowNode = lastNode;
        return ret;
    }
    private int FuncFParamToFuncDef (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        int dimension = 0;
        if(nowNode.listSons().size() >= 5){
            dimension = 2;
        }else if(nowNode.listSons().size() >= 3){
            dimension = 1;
        }
        nowNode = lastNode;
        return dimension;
    }

    private void FuncBlock (int i){
        Node lastNode = nowNode;
        nowNode = nowNode.son(i);
        DealBlock();
        nowNode = lastNode;
    }

    private void Block(int i){
        Node lastNode = nowNode;
        nowNode = nowNode.son(i);
        nowSymbolTable = new SymbolTable(nowSymbolTable);
        DealBlock();
        nowSymbolTable = nowSymbolTable.father();
        nowNode = lastNode;
    }
    private void DealBlock(){
        int i = 1;
        while(is(i, Nonterminal.BlockItem)){
            BlockItem(i);
            i++;
        }
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
        String ident = ident(0);
        int dimension = (nowNode.listSons().size() - 3) /3;
        nowSymbolTable.addVariable(ident, true, dimension);
        nowNode = lastNode;
    }
    private void VarDef (int location){
        Node lastNode = nowNode;
        nowNode = nowNode.son(location);
        String ident = ident(0);
        int dimension = (nowNode.listSons().size() - 1) /3;
        nowSymbolTable.addVariable(ident, false, dimension);
        nowNode = lastNode;
    }



    private String ident(int i){
        return ((Terminal)nowNode.son(i).word()).getValue();
    }
    private boolean is(String type){
        return nowNode.word().typeOf(type);
    }
    private boolean is(int i, String type){
        if(nowNode.listSons().size() <= i)return false;
        return nowNode.son(i).word().typeOf(type);
    }
    private void father(){
        nowNode = nowNode.father();
    }
    private void son(int i){
        nowNode = nowNode.listSons().get(i);
    }



}
