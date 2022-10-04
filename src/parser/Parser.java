package parser;

import common.CompileException;
import common.PreIterator;
import error.ErrorRecorder;
import lexer.*;
import parser.nonterminal.*;
import parser.nonterminal.decl.*;
import parser.nonterminal.exp.*;
import parser.nonterminal.exp.Number;
import parser.nonterminal.stmt.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final PreIterator<Terminal> iterator;

    private final List<String> postOrderList = new ArrayList<>();

    private final ErrorRecorder errorRecorder;

    public Parser(PreIterator<Terminal> iterator, ErrorRecorder errorRecorder) {
        this.iterator = iterator;
        this.errorRecorder = errorRecorder;
    }

    public Parser(List<Terminal> list, ErrorRecorder errorRecorder) {
        this(new PreIterator<Terminal>(list), errorRecorder);
    }

    public CompUnit analysis(){
        // 进入识别符号的递归子程序
        return CompUnit();
    }

    public List<String> getPostOrderList(){
        return postOrderList;
    }



    private CompUnit CompUnit () {
        List<Decl> decls = new ArrayList<>();
        List<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef;
        // decl : int, const
        // funcdef : int, void
        // mainfuncdef : int
        int enter = 0;
        while(true){
            if(is(TerminalType.CONSTTK)){
                enter = 1;
            }else if(is(TerminalType.VOIDTK)){
                enter = 2;
            }else if(is(TerminalType.INTTK)){
                if(isPre(1, TerminalType.MAINTK)){
                    enter = 3;
                }else if(isPre(1, TerminalType.IDENFR)){
                    /*
                    decl:
                        int ident [
                        int ident ;
                        int ident ,
                        int ident =
                    funcdef:
                        int ident (
                     */
                    if(isPre(2, TerminalType.LPARENT)){
                        enter = 2;
                    }else if(isPre(2, TerminalType.LBRACK, TerminalType.SEMICN, TerminalType.COMMA, TerminalType.ASSIGN)){
                        enter = 1;
                    }else {
                        throw new CompileException();
                    }
                }else{
                    throw new CompileException();
                }
            }else{
                throw new CompileException();
            }
            if(enter == 1){
                decls.add(Decl());
            }else if(enter == 2){
                funcDefs.add(FuncDef());
            }else {
                break;
            }
        }
        mainFuncDef = MainFuncDef();
        postOrderList.add("<CompUnit>");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    private Decl Decl () {
        Decl decl;
        if(is(TerminalType.CONSTTK)){
            decl = Decl(true);
        }else if(is(TerminalType.INTTK)){
            decl = Decl(false);
        }else {
            throw new CompileException();
        }
        postOrderList.add("<Decl>");
        return decl;
    }

    private Decl Decl(boolean isConst) {
        Decl ret;
        if(isConst){
            check(TerminalType.CONSTTK);
        }
        BType();
        List<Def> defs = new ArrayList<>();
        defs.add(Def(isConst));
        while(is(TerminalType.COMMA)){
            addTerminal();
            defs.add(Def(isConst));
        }
        checkSemicolon();
        ret = new Decl(defs);
        if(isConst){
            postOrderList.add("<ConstDecl>");
        }else {
            postOrderList.add("<VarDecl>");
        }
        return ret;
    }

    private void BType () {
        check(TerminalType.INTTK);
        postOrderList.add("<BType>");
    }

    private Def Def (boolean isConst) {
        Ident ident = Ident();
        List<Exp> exps = new ArrayList<>();
        while(is(TerminalType.LBRACK)){
            addTerminal();
            exps.add(ConstExp());
            checkRBracket();
        }
        InitVal initVal;
        if(isConst){
            check(TerminalType.ASSIGN);
            initVal = InitVal(true);
            postOrderList.add("<ConstDef>");
            return new ConstDef(ident, exps, initVal);
        }else{
            if(is(TerminalType.ASSIGN)){
                addTerminal();
                initVal = InitVal(false);
                postOrderList.add("<VarDef>");
                return new VarDef(ident, exps, initVal);
            } else {
                postOrderList.add("<VarDef>");
                return new VarDef(ident, exps);
            }
        }
    }
    private InitVal InitVal (boolean isConst) {
        InitVal ret;
        if(isExp()){
            ret = new IntInitVal(Exp(isConst));
        }else if(is(TerminalType.LBRACE)){
            addTerminal();
            List<InitVal> initVals = new ArrayList<>();
            initVals.add(InitVal(isConst));
            while(is(TerminalType.COMMA)){
                addTerminal();
                initVals.add(InitVal(isConst));
            }
            check(TerminalType.RBRACE);
            ret = new ArrayInitVal(initVals);
        }else {
            throw new CompileException();
        }
        if(isConst){
            postOrderList.add("<ConstInitVal>");
        }else {
            postOrderList.add("<InitVal>");
        }
        return ret;
    }

    private MainFuncDef MainFuncDef () {
        MainFuncDef ret;
        check(TerminalType.INTTK);
        check(TerminalType.MAINTK);
        check(TerminalType.LPARENT);
        checkRParent();
        ret = new MainFuncDef(Block());
        postOrderList.add("<MainFuncDef>");
        return ret;
    }

    private FuncDef FuncDef () {
        FuncDef ret;
        boolean isInt;
        if(is(TerminalType.INTTK)){
            isInt = true;
            addTerminal();
            postOrderList.add("<FuncType>");
        }else if(is(TerminalType.VOIDTK)){
            isInt = false;
            addTerminal();
            postOrderList.add("<FuncType>");
        }else{
            throw new CompileException();
        }
        Ident ident = Ident();
        check(TerminalType.LPARENT);
        List<FuncDef.FuncFParam> funcFParams = null;
        if(is(TerminalType.INTTK)){
            funcFParams = FuncFParams();
        }else{
            funcFParams = new ArrayList<>();
        }
        checkRParent();
        Block block = Block();
        ret = new FuncDef(isInt, ident, funcFParams, block.getBlockItems());
        postOrderList.add("<FuncDef>");
        return ret;
    }

    private List<FuncDef.FuncFParam> FuncFParams() {
        List<FuncDef.FuncFParam> ret = new ArrayList<>();
        ret.add(FuncFParam());
        while (is(TerminalType.COMMA)){
            addTerminal();
            ret.add(FuncFParam());
        }
        postOrderList.add("<FuncFParams>");
        return ret;
    }
    private FuncDef.FuncFParam FuncFParam () {
        FuncDef.FuncFParam ret;
        BType();
        Ident ident = Ident();
        int dimension = 0;
        Exp exp = null;
        if(is(TerminalType.LBRACK)){
            addTerminal();
            checkRBracket();
            dimension ++;
            if(is(TerminalType.LBRACK)){
                addTerminal();
                exp = ConstExp();
                checkRBracket();
                dimension ++;
            }
        }
        if(exp == null){
            ret = new FuncDef.FuncFParam(ident, dimension);
        }else{
            ret = new FuncDef.FuncFParam(ident, dimension, exp);
        }
        postOrderList.add("<FuncFParam>");
        return ret;
    }

    private Block Block () {
        Block ret;
        List<BlockItem> blockItems = new ArrayList<>();
        check(TerminalType.LBRACE);
        while(isBlockItem()){
            BlockItem blockItem = BlockItem();
            if(blockItem != null){
                blockItems.add(blockItem);
            }
        }
        check(TerminalType.RBRACE);
        ret = new Block(blockItems);
        postOrderList.add("<Block>");
        return ret;
    }

    // maybe null
    private BlockItem BlockItem () {
        BlockItem ret;
        if(isDecl()){
            ret = Decl();
        }else if(isStmt()){
            ret = Stmt();
        }else {
            throw new CompileException();
        }
        postOrderList.add("<BlockItem>");
        return ret;
    }


    // maybe null when just semicolon
    private Stmt Stmt () {
        Stmt ret;
        if(is(TerminalType.IFTK)){
            addTerminal();
            check(TerminalType.LPARENT);
            Exp exp = Cond();
            checkRParent();
            Stmt ifstmt = Stmt();
            Stmt elsestmt = null;
            if(is(TerminalType.ELSETK)){
                addTerminal();
                elsestmt = Stmt();
            }
            ret = new If(exp, ifstmt, elsestmt);
        }else if(is(TerminalType.WHILETK)){
            addTerminal();
            check(TerminalType.LPARENT);
            Exp exp = Cond();
            checkRParent();
            Stmt stmt = Stmt();
            ret = new While(exp, stmt);
        }else if(is(TerminalType.BREAKTK)){
            addTerminal();
            checkSemicolon();
            ret = new Break();
        }else if(is(TerminalType.CONTINUETK)){
            addTerminal();
            checkSemicolon();
            ret = new Continue();
        }else if(is(TerminalType.PRINTFTK)){
            addTerminal();
            check(TerminalType.LPARENT);
            FormatString formatString = (FormatString)checkAndGet(TerminalType.STRCON);
            List<Exp> exps = new ArrayList<>();
            while(is(TerminalType.COMMA)){
                addTerminal();
                exps.add(Exp());
            }
            checkRParent();
            checkSemicolon();
            ret = new Printf(formatString, exps);
        }else if(is(TerminalType.RETURNTK)){
            addTerminal();
            Exp exp = null;
            if(isExp()){
                exp = Exp();
            }
            checkSemicolon();
            ret = new Return(exp);
        }else if(is(TerminalType.LBRACE)){
            ret = Block();
        }else if(is(TerminalType.IDENFR)){
            /*
            LVal = getint ();
            Exp ;
            LVal = Exp;
             */
            // lval : ident   ident[exp]
            // exp : lval,   ident(

            if(isPre(1, TerminalType.LPARENT)){
                // exp
                ret = Exp();
                checkSemicolon();
            }else {
                // maybe exp or lval
                // find assign
                int i = 1;
                while(true){
                    if(isPre(i, TerminalType.ASSIGN)){
                        if(isPre(i + 1, TerminalType.GETINTTK)){
                            ret = new GetInt(LVal());
                            addTerminal();
                            addTerminal();
                            check(TerminalType.LPARENT);
                            checkRParent();
                            checkSemicolon();
                        }else if(isExp(i + 1)){
                            LVal lVal = LVal();
                            addTerminal();
                            Exp exp = Exp();
                            checkSemicolon();
                            ret = new Assign(lVal, exp);
                        }else {
                            throw new CompileException();
                        }
                        break;
                    }else if(isPre(i, TerminalType.SEMICN)){
                        ret = Exp();
                        checkSemicolon();
                        break;
                    }else if(now() == null){
                        throw new CompileException();
                    }
                    i++;
                }
            }
        }else if(isExp()){
            ret = Exp();
            checkSemicolon();
        }else if(is(TerminalType.SEMICN)) {
            addTerminal();
            ret = null;
        }else {
            throw new CompileException();
        }
        postOrderList.add("<Stmt>");
        return ret;
    }
    
    private LVal LVal () {
        LVal ret;
        Ident ident = Ident();
        List<Exp> exps = new ArrayList<>();
        if(is(TerminalType.LBRACK)){
            addTerminal();
            exps.add(Exp());
            checkRBracket();
        }
        if(is(TerminalType.LBRACK)){
            addTerminal();
            exps.add(Exp());
            checkRBracket();
        }
        ret = new LVal(ident, exps);
        postOrderList.add("<LVal>");
        return ret;
    }

    private Exp ConstExp() {
        return Exp(true);
    }
    private Exp Exp(){
        return Exp(false);
    }

    private Exp Exp (boolean isConst) {
        Exp ret = AddExp();
        if(isConst){
            postOrderList.add("<ConstExp>");
        }else {
            postOrderList.add("<Exp>");
        }
        return ret;
    }

    private Exp Cond () {
        Exp ret = LOrExp();
        postOrderList.add("<Cond>");
        return ret;
    }


    private Exp LayerExp(ExpLayer layer){
        if(layer == ExpLayer.MUL){
            Exp ret;
            Exp first = UnaryExp();
            List<Exp> exps = new ArrayList<>();
            List<TerminalType> ops = new ArrayList<>();
            while(isOp(layer)){
                postOrderList.add("<MulExp>");
                ops.add(get().getTerminalType());
                exps.add(UnaryExp());
            }
            ret = new BinaryExp(first, exps, ops, layer);
            postOrderList.add("<MulExp>");
            return ret;
        }
        ExpLayer nextLayer;
        String wordName;
        switch (layer){
            case REL: nextLayer = ExpLayer.ADD; wordName = "<RelExp>"; break;
            case LAND: nextLayer = ExpLayer.EQ; wordName = "<LAndExp>";break;
            case LOR: nextLayer = ExpLayer.LAND; wordName = "<LOrExp>";break;
            case ADD: nextLayer = ExpLayer.MUL; wordName = "<AddExp>";break;
            case EQ: nextLayer = ExpLayer.REL; wordName = "<EqExp>";break;
            default: throw new CompileException();
        }
        Exp ret;
        Exp first = LayerExp(nextLayer);
        List<Exp> exps = new ArrayList<>();
        List<TerminalType> ops = new ArrayList<>();
        while(isOp(layer)){
            postOrderList.add(wordName);
            ops.add(get().getTerminalType());
            exps.add(LayerExp(nextLayer));
        }
        ret = new BinaryExp(first, exps, ops, layer);
        postOrderList.add(wordName);
        return ret;
    }

    private Exp AddExp () {
        return LayerExp(ExpLayer.ADD);
    }
    private Exp MulExp () {
        return LayerExp(ExpLayer.MUL);
    }
    
    private Exp RelExp () {
        return LayerExp(ExpLayer.REL);
    }
    
    private Exp EqExp () {
        return LayerExp(ExpLayer.EQ);
    }
    
    private Exp LAndExp () {
        return LayerExp(ExpLayer.LAND);
    }
    
    private Exp LOrExp () {
        return LayerExp(ExpLayer.LOR);
    }
    
    private UnaryExp UnaryExp () {
        UnaryExp ret;
        PrimaryExp primaryExp;
        int time = 1;
        List<TerminalType> unaryOps = new ArrayList<>();
        while(isUnaryOp()){
            time ++;
            unaryOps.add(get().getTerminalType());
            postOrderList.add("<UnaryOp>");
        }
        if(is(TerminalType.LPARENT, TerminalType.INTCON) || is(TerminalType.IDENFR) && !isPre(1, TerminalType.LPARENT)){
            primaryExp = PrimaryExp();
        }else if(is(TerminalType.IDENFR)){
            Ident ident = Ident();
            List<Exp> exps = new ArrayList<>();
            check(TerminalType.LPARENT);
            if(isExp()){
                exps.add(Exp());
                while (is(TerminalType.COMMA)) {
                    addTerminal();
                    exps.add(Exp());
                }
                postOrderList.add("<FuncRParams>");
            }
            checkRParent();
            primaryExp = new FuncCall(ident, exps);
        }else{
            throw new CompileException();
        }
        while(time-- != 0){
            postOrderList.add("<UnaryExp>");
        }
        ret = new UnaryExp(unaryOps, primaryExp);
        return ret;
    }

    private PrimaryExp PrimaryExp () {
        PrimaryExp ret;
        if(is(TerminalType.LPARENT)){
            addTerminal();
            ret = Exp();
            checkRParent();
        }else if(is(TerminalType.IDENFR)){
            ret = LVal();
        }else if(is(TerminalType.INTCON)){
            ret = new Number((IntConst)get());
            postOrderList.add("<Number>");
        }else {
            throw new CompileException();
        }
        postOrderList.add("<PrimaryExp>");
        return ret;
    }

    private boolean isFuncRParams() {
        return isExp();
    }
    private boolean isUnaryOp() {
        return is(TerminalType.PLUS, TerminalType.MINU, TerminalType.NOT);
    }

    private boolean isOp(ExpLayer layer){
        switch (layer){
            case EQ: return is(TerminalType.EQL, TerminalType.NEQ);
            case ADD: return is(TerminalType.PLUS, TerminalType.MINU);
            case LOR: return is(TerminalType.OR);
            case LAND: return is(TerminalType.AND);
            case MUL: return is(TerminalType.MULT, TerminalType.DIV, TerminalType.MOD);
            case REL: return is(TerminalType.LEQ, TerminalType.GEQ, TerminalType.GRE, TerminalType.LSS);
        }
        throw new CompileException();
    }

    private boolean isBlockItem(){
        return isDecl() || isStmt();
    }
    private boolean isDecl(){
        return is(TerminalType.CONSTTK, TerminalType.INTTK);
    }
    private boolean isStmt(){
        return is(TerminalType.IDENFR, TerminalType.SEMICN, TerminalType.LPARENT, TerminalType.LBRACE, TerminalType.IFTK,
                TerminalType.WHILETK, TerminalType.BREAKTK, TerminalType.CONTINUETK, TerminalType.RETURNTK, TerminalType.PRINTFTK) || isExp();
    }
    private boolean isConstInitVal(){
        return isConstExp() || is(TerminalType.LBRACE);
    }
    private boolean isConstExp(){
        return isAddExp();
    }
    private boolean isInitVal(){
        return isExp() || is(TerminalType.LBRACE);
    }
    private boolean isAddExp(){
        return is(TerminalType.LPARENT, TerminalType.IDENFR, TerminalType.INTCON, TerminalType.PLUS, TerminalType.MINU, TerminalType.NOT);
    }
    private boolean isExp(){
        return isAddExp();
    }

    private boolean isExp(int pre){
        return isPre(pre, TerminalType.LPARENT, TerminalType.IDENFR, TerminalType.INTCON, TerminalType.PLUS, TerminalType.MINU, TerminalType.NOT);
    }


    private Ident Ident(){
        return (Ident)checkAndGet(TerminalType.IDENFR);
    }
    private Terminal now(){
        return iterator.now();
    }
    private void next() {
        iterator.next();
    }


    private IntConst IntConst(){
        check(TerminalType.INTCON);
        return (IntConst) iterator.now();
    }

    private void check(TerminalType terminal){
        if(!is(terminal)){
            throw new CompileException(iterator.now().line());
        }
        addTerminal();
    }

    private void checkRBracket(){
        if(!is(TerminalType.RBRACK)){
            errorRecorder.rBracketLack(iterator.previous().line());
            return;
        }
        addTerminal();
    }
    private void checkRParent(){
        if(!is(TerminalType.RPARENT)){
            errorRecorder.rParentLack(iterator.previous().line());
            return;
        }
        addTerminal();
    }
    private void checkSemicolon(){
        if(!is(TerminalType.SEMICN)){
            errorRecorder.semicolonLack(iterator.previous().line());
            return;
        }
        addTerminal();
    }

    private Terminal checkAndGet(TerminalType terminal){
        if(!is(terminal)){
            throw new CompileException();
        }
        Terminal ret = iterator.now();
        addTerminal();
        return ret;
    }

    private void addTerminal(){
        postOrderList.add(iterator.now().toString());
        iterator.next();
    }
    private Terminal get(){
        postOrderList.add(iterator.now().toString());
        Terminal now = iterator.now();
        iterator.next();
        return now;
    }

    private boolean isPre(int i, TerminalType... terminals){
        if(iterator.pre(i) == null)return false;
        for(TerminalType terminal : terminals){
            if(iterator.pre(i).getTerminalType() == terminal){
                return true;
            }
        }
        return false;
    }

    private boolean is(TerminalType... terminals) {
        if(iterator.now() == null)return false;
        for(TerminalType terminal : terminals){
            if(iterator.now().getTerminalType() == terminal){
                return true;
            }
        }
        return false;
    }




}
