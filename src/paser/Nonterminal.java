package paser;

import common.Word;

public class Nonterminal extends Word {
    public static final String CompUnit = "CompUnit";
    public static final String Decl = "Decl";
    public static final String FuncDef = "FuncDef";
    public static final String MainFuncDef = "MainFuncDef";
    public static final String ConstDecl = "ConstDecl";
    public static final String VarDecl = "VarDecl";
    public static final String BType = "BType";
    public static final String ConstDef = "ConstDef";
    public static final String ConstInitVal = "ConstInitVal";
    public static final String VarDef = "VarDef";
    public static final String InitVal = "InitVal";
    public static final String FuncType = "FuncType";
    public static final String FuncFParams = "FuncFParams";
    public static final String FuncFParam = "FuncFParam";
    public static final String Block = "Block";
    public static final String BlockItem = "BlockItem";
    public static final String Exp = "Exp";
    public static final String Cond = "Cond";
    public static final String LVal = "LVal";
    public static final String PrimaryExp = "PrimaryExp";
    public static final String Number = "Number";
    public static final String UnaryExp = "UnaryExp";
    public static final String UnaryOp = "UnaryOp";
    public static final String FuncRParams = "FuncRParams";
    public static final String MulExp = "MulExp";
    public static final String AddExp = "AddExp";
    public static final String RelExp = "RelExp";
    public static final String EqExp = "EqExp";
    public static final String LAndExp = "LAndExp";
    public static final String LOrExp = "LOrExp";
    public static final String ConstExp = "ConstExp";
    public static final String Stmt = "Stmt";
    public Nonterminal(String type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "<" + type + ">";
    }
}
