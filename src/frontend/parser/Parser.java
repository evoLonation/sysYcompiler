package frontend.parser;

import common.ParserException;
import common.PreIterator;
import frontend.error.ErrorRecorder;
import frontend.lexer.*;
import frontend.parser.nonterminal.*;
import frontend.parser.nonterminal.decl.*;
import frontend.parser.nonterminal.exp.*;
import frontend.parser.nonterminal.exp.Number;
import frontend.parser.nonterminal.stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final PreIterator<Terminal> iterator;

    private final List<String> postOrderList = new ArrayList<>();

    private final ErrorRecorder errorRecorder = ErrorRecorder.getInstance();

    public Parser(PreIterator<Terminal> iterator) {
        this.iterator = iterator;
    }

    public Parser(List<Terminal> list) {
        this(new PreIterator<>(list));
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
        int enter;
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
        Ident ident = getIdent();
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
        Ident ident = getIdent();
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
        Ident ident = getIdent();
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
            if(dimension == 0){
                ret = new FuncDef.IntFParam(ident);
            }else {
                ret = new FuncDef.PointerFParam(ident);
            }
        }else{
            ret = new FuncDef.PointerFParam(ident, exp);
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
            Optional<Stmt> elseStmt;
            if(is(TerminalType.ELSETK)){
                skipTerminal();
                elseStmt = Stmt();
            }else{
                elseStmt = Optional.empty();
            }
            ret = ifStmt.map(
                            i -> elseStmt
                            .map(e -> new If(exp, i, e))
                            .orElseGet(() -> new If(exp, i, true)))
                        .orElseGet(
                            () -> elseStmt
                            .map(e -> new If(exp, e, false))
                            .orElseGet(() -> new If(exp)));
        }else if(is(TerminalType.WHILETK)){
            skipTerminal();
            check(TerminalType.LPARENT);
            Exp exp = Cond();
            checkRParent();
            Optional<Stmt> stmt = Stmt();
            ret = stmt.map(value -> new While(exp, value)).orElseGet(() -> new While(exp));
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
            FormatString formatString = getTerminal(FormatString.class);
            List<Exp> exps = new ArrayList<>();
            while(is(TerminalType.COMMA)){
                skipTerminal();
                exps.add(Exp());
            }
            checkRParent();
            checkSemicolon();
            ret = new PrintfNode(formatString, exps, line);
        }else if(is(TerminalType.RETURNTK)){
            int line = now().line();
            skipTerminal();
            if(isExp()){
                ret = new ReturnNode(Exp(), line);
            }else{
                ret = new ReturnNode(line);
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
            if(is(1, TerminalType.LPARENT)){
                // must be exp
                ret = Exp();
                checkSemicolon();
            }else{
                // must be lval, maybe exp
                LVal lVal = LVal();
                if(isBinaryOp()){
                    // must be exp
                    ret = Exp(lVal);
                    checkSemicolon();
                }else if(is(TerminalType.ASSIGN)){
                    // must be lval
                    skipTerminal();
                    if(is(TerminalType.GETINTTK)){
                        skipTerminal();
                        check(TerminalType.LPARENT);
                        checkRParent();
                        checkSemicolon();
                        ret = new GetIntNode(lVal);
                    }else if(isExp()){
                        Exp exp = Exp();
                        checkSemicolon();
                        ret = new Assign(lVal, exp);
                    }else{
                        throw new ParserException();
                    }
                }else{
                    // must be exp
                    ret = Exp(lVal);
                    checkSemicolon();
                }
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

    private LVal LVal () {
        LVal ret;
        Ident ident = getIdent();
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
        Exp ret = BinaryExp(BinaryExpLayer.ADD, lVal);
        postOrderList.add("<Exp>");
        return ret;
    }


    private Exp BinaryExp(BinaryExpLayer layer){
        return BinaryExp(layer, null);
    }


    private BinaryOp getBinaryOp(BinaryExpLayer layer){
        TerminalType terminalType =  getTerminal().getTerminalType();
        switch (layer){
            case ADD:
                switch (terminalType){
                    case PLUS: return BinaryOp.PLUS;
                    case MINU: return BinaryOp.MINU;
                }
                break;
            case MUL:
                switch (terminalType){
                    case MULT: return BinaryOp.MULT;
                    case DIV: return BinaryOp.DIV;
                    case MOD: return BinaryOp.MOD;
                }
                break;
            case EQ:
                switch (terminalType){
                    case EQL: return BinaryOp.EQL;
                    case NEQ: return BinaryOp.NEQ;
                }
                break;
            case LOR:
                if (terminalType == TerminalType.OR) {
                    return BinaryOp.OR;
                }
                break;
            case LAND:
                if (terminalType == TerminalType.AND) {
                    return BinaryOp.AND;
                }
                break;
            case REL:
                switch (terminalType){
                    case LEQ: return BinaryOp.LEQ;
                    case LSS: return BinaryOp.LSS;
                    case GEQ: return BinaryOp.GEQ;
                    case GRE: return BinaryOp.GRE;
                }
                break;
        }
        throw new ParserException();
    }
    /**
     * @param firstLVal nullable
     */
    private Exp BinaryExp(BinaryExpLayer layer, LVal firstLVal){
        if(layer == BinaryExpLayer.MUL){
            Exp ret = UnaryExp(firstLVal);
            postOrderList.add("<MulExp>");
             while(isBinaryOp(layer)){
                ret = new BinaryExp(ret, getBinaryOp(layer), UnaryExp());
                postOrderList.add("<MulExp>");
            }
            return ret;
        }
        BinaryExpLayer nextLayer;
        String wordName;
        switch (layer){
            case REL: nextLayer = BinaryExpLayer.ADD; wordName = "<RelExp>"; break;
            case LAND: nextLayer = BinaryExpLayer.EQ; wordName = "<LAndExp>";break;
            case LOR: nextLayer = BinaryExpLayer.LAND; wordName = "<LOrExp>";break;
            case ADD: nextLayer = BinaryExpLayer.MUL; wordName = "<AddExp>";break;
            case EQ: nextLayer = BinaryExpLayer.REL; wordName = "<EqExp>";break;
            default: throw new ParserException();
        }
        Exp ret = BinaryExp(nextLayer, firstLVal);
        postOrderList.add(wordName);
        while(isBinaryOp(layer)){
            ret = new BinaryExp(ret, getBinaryOp(layer), BinaryExp(nextLayer));
            postOrderList.add(wordName);
        }
        return ret;
    }

    private Exp AddExp () {
        return BinaryExp(BinaryExpLayer.ADD);
    }
    private Exp LOrExp () {
        return BinaryExp(BinaryExpLayer.LOR);
    }


    private Exp UnaryExp(){
        return UnaryExp(null);
    }

    private UnaryOp getUnaryOp(){
        UnaryOp ret;
        switch (getTerminal().getTerminalType()){
            case PLUS: ret = UnaryOp.PLUS; break;
            case MINU: ret = UnaryOp.MINU; break;
            case NOT: ret = UnaryOp.NOT; break;
            default:  throw new ParserException();
        }
        postOrderList.add("<UnaryOp>");
        return ret;
    }

    /**
     * @param firstLVal nullable
     */
    private Exp UnaryExp (LVal firstLVal) {
        if(firstLVal != null){
            postOrderList.add("<PrimaryExp>");
            postOrderList.add("<UnaryExp>");
            return firstLVal;
        }
        Exp ret;
        if(isUnaryOp()){
            ret = new UnaryExp(getUnaryOp(), UnaryExp());
        }else if(is(TerminalType.LPARENT, TerminalType.INTCON) || is(TerminalType.IDENFR) && !is(1, TerminalType.LPARENT)){
            ret = PrimaryExp();
        }else if(is(TerminalType.IDENFR)){
            Ident ident = getIdent();
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
            ret = new FuncCall(ident, exps);
        }else{
            throw new ParserException();
        }
        postOrderList.add("<UnaryExp>");
        return ret;
    }

    private Exp PrimaryExp () {
        Exp ret;
        if(is(TerminalType.LPARENT)){
            skipTerminal();
            ret = Exp();
            checkRParent();
        }else if(is(TerminalType.IDENFR)){
            ret = LVal();
        }else if(is(TerminalType.INTCON)){
            ret = new Number(getIntConst());
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

    private boolean isBinaryOp(BinaryExpLayer layer){
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

    private Ident getIdent(){
        return getTerminal(Ident.class);
    }
    private IntConst getIntConst(){
        return getTerminal(IntConst.class);
    }
    private Terminal getTerminal(){
        return getTerminal(Terminal.class);
    }

    private <T extends Terminal> T getTerminal(Class<T> clazz){
       try {
           T now = clazz.cast(iterator.now());
           postOrderList.add(now.toString());
           iterator.next();
           return now;
       }catch (ClassCastException e) {
           throw new ParserException();
       }
    }

    private boolean is(int pre, TerminalType... terminals){
        for(TerminalType terminal : terminals){
            if(iterator.pre(pre).getTerminalType() == terminal){
                return true;
            }
        }
        return false;
    }

    private boolean is(TerminalType... terminals) {
        for(TerminalType terminal : terminals){
            if(iterator.now().getTerminalType() == terminal){
                return true;
            }
        }
        return false;
    }

}
