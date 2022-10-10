package parser;

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
                if(is(1, TerminalType.MAINTK)){
                    enter = 3;
                }else if(is(1, TerminalType.IDENFR)){
                    /*
                    decl:
                        int ident [
                        int ident ;
                        int ident ,
                        int ident =
                    funcdef:
                        int ident (
                     */
                    if(is(2, TerminalType.LPARENT)){
                        enter = 2;
                    }else if(is(2, TerminalType.LBRACK, TerminalType.SEMICN, TerminalType.COMMA, TerminalType.ASSIGN)){
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
            skipTerminal();
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
        Ident ident = getTerminal();
        List<Exp> exps = new ArrayList<>();
        while(is(TerminalType.LBRACK)){
            skipTerminal();
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
                skipTerminal();
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
            skipTerminal();
            List<InitVal> initVals = new ArrayList<>();
            initVals.add(InitVal(isConst));
            while(is(TerminalType.COMMA)){
                skipTerminal();
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
            skipTerminal();
            postOrderList.add("<FuncType>");
        }else if(is(TerminalType.VOIDTK)){
            isInt = false;
            skipTerminal();
            postOrderList.add("<FuncType>");
        }else{
            throw new ParserException();
        }
        Ident ident = getTerminal();
        check(TerminalType.LPARENT);
        List<FuncDef.FuncFParam> funcFParams = new ArrayList<>();
        if(is(TerminalType.INTTK)){
            funcFParams = FuncFParams();
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
            skipTerminal();
            ret.add(FuncFParam());
        }
        postOrderList.add("<FuncFParams>");
        return ret;
    }
    private FuncDef.FuncFParam FuncFParam () {
        FuncDef.FuncFParam ret;
        BType();
        Ident ident = getTerminal();
        int dimension = 0;
        Exp exp = null;
        if(is(TerminalType.LBRACK)){
            skipTerminal();
            checkRBracket();
            dimension ++;
            if(is(TerminalType.LBRACK)){
                skipTerminal();
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
            BlockItem().ifPresent(blockItems::add);
        }
        int endLine = now().line();
        check(TerminalType.RBRACE);
        ret = new Block(blockItems, endLine);
        postOrderList.add("<Block>");
        return ret;
    }

    // maybe null
    private Optional<BlockItem> BlockItem () {
        BlockItem ret;
        if(isDecl()){
            ret = Decl();
        }else if(isStmt()){
            ret = Stmt().orElse(null);
        }else {
            throw new ParserException();
        }
        postOrderList.add("<BlockItem>");
        return Optional.ofNullable(ret);
    }


    // maybe  just semicolon, so optional
    private Optional<Stmt> Stmt () {
        Stmt ret;
        if(is(TerminalType.IFTK)){
            skipTerminal();
            check(TerminalType.LPARENT);
            Exp exp = Cond();
            checkRParent();
            Optional<Stmt> ifStmt = Stmt();
            if(is(TerminalType.ELSETK)){
                skipTerminal();
                ret = new If(exp, ifStmt, Stmt());
            }else{
                ret = new If(exp, ifStmt, Optional.empty());
            }
        }else if(is(TerminalType.WHILETK)){
            skipTerminal();
            check(TerminalType.LPARENT);
            Exp exp = Cond();
            checkRParent();
            Optional<Stmt> stmt = Stmt();
            ret = new While(exp, stmt);
        }else if(is(TerminalType.BREAKTK)){
            ret = new Break(now().line());
            skipTerminal();
            checkSemicolon();
        }else if(is(TerminalType.CONTINUETK)){
            ret = new Continue(now().line());
            skipTerminal();
            checkSemicolon();
        }else if(is(TerminalType.PRINTFTK)){
            int line = now().line();
            skipTerminal();
            check(TerminalType.LPARENT);
            FormatString formatString = getTerminal();
            List<Exp> exps = new ArrayList<>();
            while(is(TerminalType.COMMA)){
                skipTerminal();
                exps.add(Exp());
            }
            checkRParent();
            checkSemicolon();
            ret = new Printf(formatString, exps, line);
        }else if(is(TerminalType.RETURNTK)){
            int line = now().line();
            skipTerminal();
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
                skipTerminal();
                LVal lVal = tmp.lVal;
                if(is(TerminalType.GETINTTK)){
                    skipTerminal();
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
                ret = Exp(tmp.lVal);
                checkSemicolon();
            }
        }else if(isExp()){
            ret = Exp();
            checkSemicolon();
        }else if(is(TerminalType.SEMICN)) {
            skipTerminal();
            ret = null;
        }else {
            throw new ParserException();
        }
        postOrderList.add("<Stmt>");
        return Optional.ofNullable(ret);
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
        if(is(1, TerminalType.LPARENT)){
            return new LValOrExp(Exp());
        }else{
            // must be lval, maybe exp
            LVal lVal = LVal();
            if(isBinaryOp()){
                return new LValOrExp(Exp(lVal));
            }else{
                return new LValOrExp(lVal);
            }
        }
    }
    
    private LVal LVal () {
        LVal ret;
        Ident ident = getTerminal();
        List<Exp> exps = new ArrayList<>();
        if(is(TerminalType.LBRACK)){
            skipTerminal();
            exps.add(Exp());
            checkRBracket();
        }
        if(is(TerminalType.LBRACK)){
            skipTerminal();
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

    private Exp Exp(LVal lVal){
        Exp ret = LayerExp(ExpLayer.ADD, Optional.of(lVal));
        postOrderList.add("<Exp>");
        return ret;
    }

    private Exp LayerExp(ExpLayer layer, Optional<LVal> firstLVal){
        if(layer == ExpLayer.MUL){
            Exp ret;
            Exp first = UnaryExp(firstLVal);
            List<Exp> exps = new ArrayList<>();
            List<TerminalType> ops = new ArrayList<>();
            while(isBinaryOp(layer)){
                postOrderList.add("<MulExp>");
                ops.add(getTerminal().getTerminalType());
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
        while(isBinaryOp(layer)){
            postOrderList.add(wordName);
            ops.add(getTerminal().getTerminalType());
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
            postOrderList.add("<PrimaryExp>");
            postOrderList.add("<UnaryExp>");
            return new UnaryExp(new ArrayList<>(), firstLVal.get());
        }
        UnaryExp ret;
        PrimaryExp primaryExp;
        int time = 1;
        List<TerminalType> unaryOps = new ArrayList<>();
        while(isUnaryOp()){
            time ++;
            unaryOps.add(getTerminal().getTerminalType());
            postOrderList.add("<UnaryOp>");
        }
        if(is(TerminalType.LPARENT, TerminalType.INTCON) || is(TerminalType.IDENFR) && !is(1, TerminalType.LPARENT)){
            primaryExp = PrimaryExp();
        }else if(is(TerminalType.IDENFR)){
            Ident ident = getTerminal();
            List<Exp> exps = new ArrayList<>();
            check(TerminalType.LPARENT);
            if(isExp()){
                exps.add(Exp());
                while (is(TerminalType.COMMA)) {
                    skipTerminal();
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
            skipTerminal();
            ret = new SubExp(Exp());
            checkRParent();
        }else if(is(TerminalType.IDENFR)){
            ret = LVal();
        }else if(is(TerminalType.INTCON)){
            ret = new Number(getTerminal());
            postOrderList.add("<Number>");
        }else {
            throw new ParserException();
        }
        postOrderList.add("<PrimaryExp>");
        return ret;
    }

    private boolean isUnaryOp() {
        return is(TerminalType.PLUS, TerminalType.MINU, TerminalType.NOT);
    }

    private boolean isBinaryOp(ExpLayer layer){
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
    private boolean isBinaryOp(){
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
    private boolean isAddExp(){
        return is(TerminalType.LPARENT, TerminalType.IDENFR, TerminalType.INTCON, TerminalType.PLUS, TerminalType.MINU, TerminalType.NOT);
    }
    private boolean isExp(){
        return isAddExp();
    }

    private Terminal now(){
        return iterator.now();
    }

    private void check(TerminalType terminal){
        if(!is(terminal)){
            throw new ParserException(iterator.now().line());
        }
        skipTerminal();
    }

    private void checkRBracket(){
        if(!is(TerminalType.RBRACK)){
            errorRecorder.rBracketLack(iterator.previous().line());
            return;
        }
        skipTerminal();
    }
    private void checkRParent(){
        if(!is(TerminalType.RPARENT)){
            errorRecorder.rParentLack(iterator.previous().line());
            return;
        }
        skipTerminal();
    }
    private void checkSemicolon(){
        if(!is(TerminalType.SEMICN)){
            errorRecorder.semicolonLack(iterator.previous().line());
            return;
        }
        skipTerminal();
    }

    private void skipTerminal(){
        postOrderList.add(iterator.now().toString());
        iterator.next();
    }

    private <T extends Terminal>  T getTerminal(){
        postOrderList.add(iterator.now().toString());
        Terminal now = iterator.now();
        iterator.next();
        return (T) now;
    }

    private boolean is(int pre, TerminalType... terminals){
        if(iterator.pre(pre) == null)return false;
        for(TerminalType terminal : terminals){
            if(iterator.pre(pre).getTerminalType() == terminal){
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
