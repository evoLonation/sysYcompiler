import java.util.PrimitiveIterator;

public class GrammarParser {
    private final MyIterator<Terminal> iterator;

    public GrammarParser(MyIterator<Terminal> iterator) {
        this.iterator = iterator;
    }

    GrammarNode analysis(){
        iterator.next();
        // 进入识别符号的递归子程序
        return CompUnit();
    }

    private void ConstExp (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.ConstExp));
        AddExp(node);
        father.addSon(node);
    }

    private void AddExp (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.AddExp));
        MulExp(node);
        while(is(Terminal.PLUS, Terminal.MINU)){
            node = new GrammarNode(new Nonterminal(Nonterminal.AddExp)).addSon(node);
            addTerminal(node);
            MulExp(node);
        }
        father.addSon(node);
    }

    private void MulExp(GrammarNode father){
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.MulExp));
        UnaryExp(node);
        while(is(Terminal.MULT, Terminal.DIV, Terminal.MOD)){
            node = new GrammarNode(new Nonterminal(Nonterminal.MulExp)).addSon(node);
            addTerminal(node);
            UnaryExp(node);
        }
        father.addSon(node);
    }

    private void UnaryExp(GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.UnaryExp));
        int enter = 0;
        // first{ LPARENT, Ident, Int }
        // first{ ident }
        // first{ +, -, !}
        if(is(Terminal.LPARENT, Terminal.INTCON)){
            enter = 1;
        }else if(isUnaryOp()){
            enter = 3;
        }else if(is(Terminal.IDENFR)){
            //pre read if ( then 2
            if(iterator.pre(1).typeOf(Terminal.LPARENT)){
                enter = 2;
            }else{
                enter = 1;
            }
        }
        switch (enter){
            case 1 :
                PrimaryExp(node);
                break;
            case 2 :
                addTerminal(node);
                addTerminal(node);
                if(isFuncRParams()){
                    FuncRParams(node);
                }
                check(Terminal.RPARENT);
                addTerminal(node);
                break;
            case 3 :
                UnaryOp(node);
                UnaryExp(node);
                break;
        }
        father.addSon(node);
    }

    private void FuncRParams (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.FuncRParams));
        Exp(node);
        while(is(Terminal.COMMA)){
            addTerminal(node);
            Exp(node);
        }
        father.addSon(node);
    }

    private void UnaryOp(GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.UnaryOp));
        if(!is(Terminal.PLUS,  Terminal.MINU, Terminal.NOT)) {
            throw new LexException();
        }
        addTerminal(node);
        father.addSon(node);
    }

    private void PrimaryExp(GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.PrimaryExp));
        if(is(Terminal.LPARENT)){
            addTerminal(node);
            Exp(node);
            check(Terminal.RPARENT);
            addTerminal(node);
        }else if(is(Terminal.IDENFR)){
            LVal(node);
        }else if(is(Terminal.INTCON)){
            Number(node);
        }else{
            throw new LexException();
        }
        father.addSon(node);
    }

    private void Exp(GrammarNode father){
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Exp));
        AddExp(node);
        father.addSon(node);
    }

    private void Number(GrammarNode father){
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Number));
        if(!is(Terminal.INTCON)){
            throw new LexException();
        }
        addTerminal(node);
        father.addSon(node);
    }
    private void LVal (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.LVal));
        check(Terminal.IDENFR);
        addTerminal(node);
        while(is(Terminal.LBRACK)){
            addTerminal(node);
            Exp(node);
            check(Terminal.RBRACK);
            addTerminal(node);
        }
        father.addSon(node);
    }
    
    private void Cond (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Cond));
        LOrExp(node);
        father.addSon(node);
    }
    private void LOrExp(GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.LOrExp));
        LAndExp(node);
        while (is(Terminal.OR)){
            node = new GrammarNode(new Nonterminal(Nonterminal.LOrExp)).addSon(node);
            addTerminal(node);
            LAndExp(node);
        }
        father.addSon(node);
    }
    private void LAndExp(GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.LAndExp));
        EqExp(node);
        while (is(Terminal.AND)){
            node = new GrammarNode(new Nonterminal(Nonterminal.LAndExp)).addSon(node);
            addTerminal(node);
            EqExp(node);
        }
        father.addSon(node);
    }
    private void EqExp (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.EqExp));
        RelExp(node);
        while (is(Terminal.EQL, Terminal.NEQ)){
            node = new GrammarNode(new Nonterminal(Nonterminal.EqExp)).addSon(node);
            addTerminal(node);
            RelExp(node);
        }
        father.addSon(node);
    }
    private void RelExp (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.RelExp));
        AddExp(node);
        while (is(Terminal.LSS, Terminal.LEQ, Terminal.GRE, Terminal.GEQ)){
            node = new GrammarNode(new Nonterminal(Nonterminal.RelExp)).addSon(node);
            addTerminal(node);
            AddExp(node);
        }
        father.addSon(node);
    }

    private void Decl (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Decl));
        if(is(Terminal.CONSTTK)){
            ConstDecl(node);
        }else if(is(Terminal.INTTK)){
            VarDecl(node);
        }
        father.addSon(node);
    }
    private void ConstDecl (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.ConstDecl));
        check(Terminal.CONSTTK);
        addTerminal(node);
        BType(node);
        ConstDef(node);
        while(is(Terminal.COMMA)){
            addTerminal(node);
            ConstDef(node);
        }
        check(Terminal.SEMICN);
        addTerminal(node);
        father.addSon(node);
    }
    private void VarDecl (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.VarDecl));
        BType(node);
        VarDef(node);
        while(is(Terminal.COMMA)){
            addTerminal(node);
            VarDef(node);
        }
        check(Terminal.SEMICN);
        addTerminal(node);
        father.addSon(node);
    }


    private void Stmt (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Stmt));
        if(is(Terminal.LBRACE)){
            Block(node);
        }else if(is(Terminal.IFTK)){
            addTerminal(node);
            check(Terminal.LPARENT);
            addTerminal(node);
            Cond(node);
            check(Terminal.RPARENT);
            addTerminal(node);
            Stmt(node);
            if(is(Terminal.ELSETK)){
                addTerminal(node);
                Stmt(node);
            }
        }else if(is(Terminal.WHILETK)){
            addTerminal(node);
            check(Terminal.LPARENT);
            addTerminal(node);
            Cond(node);
            check(Terminal.RPARENT);
            addTerminal(node);
            Stmt(node);
        }else if(is(Terminal.BREAKTK, Terminal.CONTINUETK)){
            addTerminal(node);
            check(Terminal.SEMICN);
            addTerminal(node);
        }else if(is(Terminal.RETURNTK)){
            addTerminal(node);
            if(is(Terminal.SEMICN)){
                addTerminal(node);
            }else{
                Exp(node);
                check(Terminal.SEMICN);
                addTerminal(node);
            }
        }else if(is(Terminal.PRINTFTK)){
            addTerminal(node);
            check(Terminal.LPARENT);
            addTerminal(node);
            check(Terminal.STRCON);
            addTerminal(node);
            while(is(Terminal.COMMA)){
                addTerminal(node);
                Exp(node);
            }
            check(Terminal.RPARENT);
            addTerminal(node);
            check(Terminal.SEMICN);
            addTerminal(node);
        }else if(is(Terminal.SEMICN)){
            addTerminal(node);
        }else{
            int enter = 0;
            // LVal = getint ();    1
            //        Exp ;         3
            //        LVal = Exp;   2
            if(is(Terminal.IDENFR)){
                int i = 1;
                while(iterator.pre(i) != null){
                    if(isPre(i, Terminal.SEMICN)){
                        enter = 3;
                        break;
                    } else if(isPre(i, Terminal.ASSIGN)){
                        if(isPre(i + 1, Terminal.GETINTTK)){
                            enter = 1;
                        }else if(iterator.pre(i + 1) != null){
                            enter = 2;
                        }
                        break;
                    }
                    i++;
                }
            }else if(isExp()) {
                Exp(node);
                check(Terminal.SEMICN);
                addTerminal(node);
            }else{
                throw new LexException();
            }
            if(enter == 1){
                LVal(node);
                // =
                addTerminal(node);
                // getint
                addTerminal(node);
                check(Terminal.LPARENT);
                addTerminal(node);
                check(Terminal.RPARENT);
                addTerminal(node);
                check(Terminal.SEMICN);
                addTerminal(node);
            }else if(enter == 2){
                LVal(node);
                // =
                addTerminal(node);
                Exp(node);
                check(Terminal.SEMICN);
                addTerminal(node);
            }else if(enter == 3){
                Exp(node);
                check(Terminal.SEMICN);
                addTerminal(node);
            }
        }
        /*
        LVal = getint ();
        Exp ;
        LVal = Exp;

         */
        // lval = : int
        // exp : lval, number, (, ident()
        // block : {
        // lval = getint
        father.addSon(node);
    }

    private void BlockItem (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.BlockItem));
        // decl : const, int
        // stmt : ident, ;, (, number, {, if, while, break, return, continue, printf
        if(is(Terminal.CONSTTK, Terminal.INTTK)){
            Decl(node);
        }else if(isStmt()){
            Stmt(node);
        }else{
            throw new LexException();
        }
        father.addSon(node);
    }
    private void Block (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.Block));
        check(Terminal.LBRACE);
        addTerminal(node);
        while(isBlockItem()){
            BlockItem(node);
        }
        check(Terminal.RBRACE);
        addTerminal(node);
        father.addSon(node);
    }
    private void FuncFParam (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.FuncFParam));
        BType(node);
        check(Terminal.IDENFR);
        addTerminal(node);
        if(is(Terminal.LBRACK)){
            addTerminal(node);
            check(Terminal.RBRACK);
            addTerminal(node);
            while(is(Terminal.LBRACK)){
                addTerminal(node);
                ConstExp(node);
                check(Terminal.RBRACK);
                addTerminal(node);
            }
        }
        father.addSon(node);
    }
    private void FuncFParams (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.FuncFParams));
        FuncFParam(node);
        while(is(Terminal.COMMA)){
            addTerminal(node);
            FuncFParam(node);
        }
        father.addSon(node);
    }

    private void FuncType (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.FuncType));
        if(is(Terminal.VOIDTK, Terminal.INTTK)){
            addTerminal(node);
        }else{
            throw new LexException();
        }
        father.addSon(node);
    }
    private void MainFuncDef (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.MainFuncDef));
        check(Terminal.INTTK);
        addTerminal(node);
        check(Terminal.MAINTK);
        addTerminal(node);
        check(Terminal.LPARENT);
        addTerminal(node);
        check(Terminal.RPARENT);
        addTerminal(node);
        Block(node);
        father.addSon(node);
    }
    private void FuncDef (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.FuncDef));
        FuncType(node);
        check(Terminal.IDENFR);
        addTerminal(node);
        check(Terminal.LPARENT);
        addTerminal(node);
        if(is(Terminal.INTTK)){
            FuncFParams(node);
        }
        check(Terminal.RPARENT);
        addTerminal(node);
        Block(node);
        father.addSon(node);
    }
    private void InitVal (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.InitVal));
        if(isExp()){
            Exp(node);
        }else if(is(Terminal.LBRACE)){
            addTerminal(node);
            if(isInitVal()){
                InitVal(node);
                while(is(Terminal.COMMA)){
                    addTerminal(node);
                    InitVal(node);
                }
            }
            check(Terminal.RBRACE);
            addTerminal(node);
        }else{
            throw new LexException();
        }
        father.addSon(node);
    }

    private void VarDef (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.VarDef));
        check(Terminal.IDENFR);
        addTerminal(node);
        while(is(Terminal.LBRACK)){
            addTerminal(node);
            ConstExp(node);
            check(Terminal.RBRACK);
            addTerminal(node);
        }
        if(is(Terminal.ASSIGN)){
            addTerminal(node);
            InitVal(node);
        }
        father.addSon(node);
    }

    private void ConstInitVal (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.ConstInitVal));
        if(isConstExp()){
            ConstExp(node);
        }else if(is(Terminal.LBRACE)){
            addTerminal(node);
            if(isConstInitVal()){
                ConstInitVal(node);
                while(is(Terminal.COMMA)){
                    addTerminal(node);
                    ConstInitVal(node);
                }
            }
            check(Terminal.RBRACE);
            addTerminal(node);
        }else{
            throw new LexException();
        }
        father.addSon(node);
    }
    private void ConstDef (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.ConstDef));
        check(Terminal.IDENFR);
        addTerminal(node);
        while(is(Terminal.LBRACK)){
            addTerminal(node);
            ConstExp(node);
            check(Terminal.RBRACK);
            addTerminal(node);
        }
        check(Terminal.ASSIGN);
        addTerminal(node);
        ConstInitVal(node);
        father.addSon(node);
    }
    private void BType (GrammarNode father) {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.BType));
        check(Terminal.INTTK);
        addTerminal(node);
        father.addSon(node);
    }

    private GrammarNode CompUnit () {
        GrammarNode node = new GrammarNode(new Nonterminal(Nonterminal.CompUnit));
        // decl : int, const
        // funcdef : int, void
        // mainfuncdef : int
        int enter = 0;
        while(true){
            if(is(Terminal.CONSTTK)){
                enter = 1;
            }else if(is(Terminal.VOIDTK)){
                enter = 2;
            }else if(is(Terminal.INTTK)){
                if(isPre(1, Terminal.MAINTK)){
                    enter = 3;
                }else if(isPre(1, Terminal.IDENFR)){
                    /*
                    decl:
                        int ident [
                        int ident ;
                        int ident ,
                        int ident =
                    funcdef:
                        int ident (
                     */
                    if(isPre(2, Terminal.LPARENT)){
                        enter = 2;
                    }else if(isPre(2, Terminal.LBRACK, Terminal.SEMICN, Terminal.COMMA, Terminal.ASSIGN)){
                        enter = 1;
                    }else {
                        throw new LexException();
                    }
                }else{
                    throw new LexException();
                }
            }else{
                throw new LexException();
            }
            if(enter == 1){
                Decl(node);
            }else if(enter == 2){
                FuncDef(node);
            }else if(enter == 3){
                break;
            }else{
                throw new LexException();
            }
        }
        MainFuncDef(node);
        return node;
    }
    private boolean isFuncRParams() {
        return isExp();
    }
    private boolean isUnaryOp() {
        return is(Terminal.PLUS, Terminal.MINU, Terminal.NOT);
    }

    private boolean isBlockItem(){
        return isDecl() || isStmt();
    }
    private boolean isDecl(){
        return is(Terminal.CONSTTK, Terminal.INTTK);
    }
    private boolean isStmt(){
        return is(Terminal.IDENFR, Terminal.SEMICN, Terminal.LPARENT, Terminal.LBRACE, Terminal.IFTK,
                Terminal.WHILETK, Terminal.BREAKTK, Terminal.CONTINUETK, Terminal.RETURNTK, Terminal.PRINTFTK) || isExp();
    }
    private boolean isConstInitVal(){
        return isConstExp() || is(Terminal.LBRACE);
    }
    private boolean isConstExp(){
        return isAddExp();
    }
    private boolean isInitVal(){
        return isExp() || is(Terminal.LBRACE);
    }
    private boolean isAddExp(){
        return is(Terminal.LPARENT, Terminal.IDENFR, Terminal.INTCON, Terminal.PLUS, Terminal.MINU, Terminal.NOT);
    }
    private boolean isExp(){
        return isAddExp();
    }



    private void check(String terminal){
        if(!iterator.now().typeOf(terminal)){
            throw new LexException();
        }
    }
    private boolean is(String terminal){
        if(iterator.now() == null)return false;
        if(iterator.now().typeOf(terminal)){
            return true;
        }
        return false;
    }
    private boolean isPre(int i, String terminal){
        if(iterator.pre(i) == null)return false;
        if(iterator.pre(i).typeOf(terminal)){
            return true;
        }
        return false;
    }
    private boolean isPre(int i, String... terminals){
        if(iterator.pre(i) == null)return false;
        for(String terminal : terminals){
            if(iterator.pre(i).typeOf(terminal)){
                return true;
            }
        }
        return false;
    }
    private boolean is(String... terminals){
        if(iterator.now() == null)return false;
        if(iterator.now().oneOf(terminals)){
            return true;
        }
        return false;
    }
    private void addTerminal(GrammarNode node){
        node.addSon(new GrammarNode(iterator.now()));
        iterator.next();
    }


}
