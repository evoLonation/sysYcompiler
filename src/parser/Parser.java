package parser;

import common.CompileException;
import common.ParserException;
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
import java.util.Optional;

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
                        throw new ParserException();
                    }
                }else{
                    throw new ParserException();
                }
            }else{
                throw new ParserException();
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
            throw new ParserException();
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
            throw new ParserException();
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
            throw new ParserException();
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
        ret = new FuncDef(isInt, ident, funcFParams, block);
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
        int endLine = now().line();
        check(TerminalType.RBRACE);
        ret = new Block(blockItems, endLine);
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
            throw new ParserException();
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
            ret = new Break(now().line());
            addTerminal();
            checkSemicolon();
        }else if(is(TerminalType.CONTINUETK)){
            ret = new Continue(now().line());
            addTerminal();
            checkSemicolon();
        }else if(is(TerminalType.PRINTFTK)){
            int line = now().line();
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
            ret = new Printf(formatString, exps, line);
        }else if(is(TerminalType.RETURNTK)){
            int line = now().line();
            addTerminal();
            if(isExp()){
                ret = new Return(Exp(), line);
            }else{
                ret = new Return(line);
            }
            checkSemicolon();
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
            // if just lval : get lval , add a assign, and find if is getint
            // if exp : add semic.
            LValOrExp tmp = checkIsLValOrExp();
            if(tmp.isExp){
                ret = tmp.exp;
                checkSemicolon();
            }else if(is(TerminalType.ASSIGN)){
                addTerminal();
                LVal lVal = tmp.lVal;
                if(is(TerminalType.GETINTTK)){
                    addTerminal();
                    check(TerminalType.LPARENT);
                    checkRParent();
                    checkSemicolon();
                    ret = new GetInt(lVal);
                }else if(isExp()){
                    Exp exp = Exp();
                    checkSemicolon();
                    ret = new Assign(lVal, exp);
                }else{
                    throw new ParserException();
                }
            }else{
                ret = LayerExp(ExpLayer.ADD, Optional.of(tmp.lVal));
                checkSemicolon();
            }
        }else if(isExp()){
            ret = Exp();
            checkSemicolon();
        }else if(is(TerminalType.SEMICN)) {
            addTerminal();
            ret = null;
        }else {
            throw new ParserException();
        }
        postOrderList.add("<Stmt>");
        return ret;
    }
    private class LValOrExp{
        boolean isExp;
        Exp exp;
        LVal lVal;
        private LValOrExp(boolean isExp, Exp exp, LVal lVal) {
            this.isExp = isExp;
            this.exp = exp;
            this.lVal = lVal;
        }
        public LValOrExp(Exp exp) {
            this(true, exp, null);
        }
        public LValOrExp(LVal lVal) {
            this(false, null, lVal);
        }
    }
    // precondition : now must is ident
    // precondition : if is lval return false, else return true
    private LValOrExp checkIsLValOrExp(){
        if(isPre(1, TerminalType.LPARENT)){
            return new LValOrExp(Exp());
        }else{
            // must be lval, maybe exp
            LVal lVal = LVal();
            if(isOp()){
                return new LValOrExp(LayerExp(ExpLayer.ADD, Optional.of(lVal)));
            }else{
                return new LValOrExp(lVal);
            }
        }
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


    private Exp LayerExp(ExpLayer layer, Optional<LVal> firstLVal){
        if(layer == ExpLayer.MUL){
            Exp ret;
            Exp first = UnaryExp(firstLVal);
            List<Exp> exps = new ArrayList<>();
            List<TerminalType> ops = new ArrayList<>();
            while(isOp(layer)){
                postOrderList.add("<MulExp>");
                ops.add(get().getTerminalType());
                exps.add(UnaryExp(Optional.empty()));
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
            default: throw new ParserException();
        }
        Exp ret;
        Exp first = LayerExp(nextLayer, firstLVal);
        List<Exp> exps = new ArrayList<>();
        List<TerminalType> ops = new ArrayList<>();
        while(isOp(layer)){
            postOrderList.add(wordName);
            ops.add(get().getTerminalType());
            exps.add(LayerExp(nextLayer, Optional.empty()));
        }
        ret = new BinaryExp(first, exps, ops, layer);
        postOrderList.add(wordName);
        return ret;
    }

    private Exp AddExp () {
        return LayerExp(ExpLayer.ADD, Optional.empty());
    }
    private Exp LOrExp () {
        return LayerExp(ExpLayer.LOR, Optional.empty());
    }
    
    private UnaryExp UnaryExp (Optional<LVal> firstLVal) {
        if(firstLVal.isPresent()){
            return new UnaryExp(new ArrayList<>(), firstLVal.get());
        }
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
            throw new ParserException();
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
            ret = new SubExp(Exp());
            checkRParent();
        }else if(is(TerminalType.IDENFR)){
            ret = LVal();
        }else if(is(TerminalType.INTCON)){
            ret = new Number((IntConst)get());
            postOrderList.add("<Number>");
        }else {
            throw new ParserException();
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
        throw new ParserException();
    }
    private boolean isOp(){
        return is(TerminalType.EQL, TerminalType.NEQ, TerminalType.PLUS, TerminalType.MINU,
                TerminalType.OR, TerminalType.AND, TerminalType.MULT, TerminalType.DIV, TerminalType.MOD,
                TerminalType.LEQ, TerminalType.GEQ, TerminalType.GRE, TerminalType.LSS);
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
            throw new ParserException(iterator.now().line());
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
            throw new ParserException();
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
