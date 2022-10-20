package semantic;

import common.SemanticException;
import error.ErrorRecorder;
import lexer.FormatString;
import parser.nonterminal.*;
import parser.nonterminal.decl.*;
import parser.nonterminal.exp.*;
import parser.nonterminal.stmt.*;
import type.*;

import java.util.*;

/* 有如下几个任务
错误检查
表达式的常量判定，以及如果是常量则计算值
表达式的类型判定，int还是bool还是一维、二维数组
符号表的建立

 */
public class Semantic {

    private final ErrorRecorder errorRecorder;

    private SymbolTable symbolTable;

    private final List<MidCode> midCodes;


    private Map<Class<?>, Exec<? extends AST>> map = new HashMap<>();

    CompUnit compUnit;

    public Semantic(CompUnit compUnit, ErrorRecorder errorRecorder) {
        this.compUnit = compUnit;
        this.errorRecorder = errorRecorder;
        this.midCodes = new ArrayList<>();
        symbolTable = new SymbolTable(errorRecorder);
        inject();
    }

    private void inject(){
        map.put(ConstDef.class, new ConstDefExec());
        map.put(VarDef.class, new VarDefExec());
        map.put(BinaryExp.class, new BinaryExpExec());
        map.put(UnaryExp.class, new UnaryExpExec());
        map.put(LVal.class, new LValExec());
        map.put(ArrayInitVal.class, new ArrayInitValExec());
        map.put(IntInitVal.class, new IntInitValExec());
        map.put(FuncCall.class, new FuncCallExec());
        map.put(Block.class, new BlockExec());
        map.put(FuncDef.class, new FuncDefExec());
        map.put(FuncDef.FuncFParam.class, new FuncFParamExec());
        map.put(Assign.class, new AssignExec());
        map.put(Printf.class, new PrintfExec());
        map.put(MainFuncDef.class, new MainFuncDefExec());
    }

    private final Exec<AST> exec = new Exec<>();

    private Exec<AST> getExec(AST AST){
        Exec<AST> ret = (Exec<AST>) map.get(AST.getClass());
        return ret == null ? exec : ret;
    }


    public void analysis(){
        getExec(compUnit).exec(compUnit);
    }

    private class Exec<T extends AST>{
        void exec(T ast){
            execSons(ast);
        }
        final void  execSons(T ast){
            for(AST son : ast.sons()){
                getExec(son).exec(son);
            }
        }
    }

    private boolean isAllConst(List<? extends ExpTyper> exps){
        for(ExpTyper exp : exps){
            if(!exp.getOptionType().isPresent())return false;
            if(!(exp.getOptionType().get().isConst())){
                return false;
            }
        }
        return true;
    }
    private void check(boolean cond){
        if(!cond){
            throw new SemanticException();
        }
    }

    // 后置：填充符号表
    private class ConstDefExec extends Exec<ConstDef> {
        @Override
        void exec(ConstDef ast) {
            execSons(ast);
            // check const exps
            // must all const
            check(isAllConst(ast.getConstExps()));
            // dimension <= 2
            int dimension = ast.getConstExps().size();
            check(dimension <= 2);
            // check init value
            InitVal initVal = ast.getInitVal();
            // must be const
            check(initVal.getType().isConst());
            // dimension must equal
            check(dimension == initVal.getType().getDimension());
            VarType type;
            // number must equal
            if(dimension >= 1){
                int number1 = ast.getConstExps().get(0).getOptionType().get().getConstValue();
                check(ast.getInitVal() instanceof ArrayInitVal && ((ArrayInitVal) ast.getInitVal()).getInitVals().size() == number1);
                if(dimension == 2){
                    int number2 = ast.getConstExps().get(1).getOptionType().get().getConstValue();
                    check(number2 == initVal.getType().getSecondLen());
                    type = new Array2Type(number2,initVal.getType().getConstValue2());
                }else{
                    type = new ArrayType(initVal.getType().getConstValue1());
                }
            }else{
                type = new IntType(initVal.getType().getConstValue());
            }
            symbolTable.addVariable(ast.getIdent(), type);
        }
    }
    private class VarDefExec extends Exec<VarDef> {
        @Override
        void exec(VarDef ast) {
            execSons(ast);
            // check const exps
            // must all const
            check(isAllConst(ast.getConstExps()));
            // dimension <= 2
            int dimension = ast.getConstExps().size();
            check(dimension <= 2);
            VarType type;
            // check init value
            if(ast.getInitVal().isPresent()){
                InitVal initVal = ast.getInitVal().get();
                check(dimension == initVal.getType().getDimension());
                if(dimension >= 1){
                    int number1 = ast.getConstExps().get(0).getOptionType().get().getConstValue();
                    check(ast.getInitVal().get() instanceof ArrayInitVal && ((ArrayInitVal) ast.getInitVal().get()).getInitVals().size() == number1);
                    if(dimension == 2){
                        int number2 = ast.getConstExps().get(1).getOptionType().get().getConstValue();
                        check(number2 == initVal.getType().getSecondLen());
                        type = new Array2Type(number2);
                    }else{
                        type = new ArrayType();
                    }
                }else{
                    type = new IntType();
                }
            }else{
                switch (dimension){
                    case 0 : type = new IntType();break;
                    case 1 : type = new ArrayType(); break;
                    case 2 : type = new Array2Type(ast.getConstExps().get(1).getOptionType().get().getConstValue()); break;
                    default: throw new SemanticException();
                }
            }
            symbolTable.addVariable(ast.getIdent(), type);
        }
    }


    // 后置：得到InitVal的Type
    private class ArrayInitValExec extends Exec<ArrayInitVal> {
        @Override
        void exec(ArrayInitVal ast) {
            execSons(ast);
            // 所有子InitVal都是一维数组或者单个数字
            List<InitVal> initVals = ast.getInitVals();
            // 子initval不能是2维数组
            check(!initVals.get(0).getType().is(GenericType.ARRAY2));
            // 检查每个InitVal的类型，必须为数组或者int
            // 检查每个InitVal 的类型是否一致
            boolean subIsArray = initVals.get(0).getType().is(GenericType.ARRAY);
            for(int i = 1; i < ast.getInitVals().size(); i++){
                check(subIsArray == initVals.get(i).getType().is(GenericType.ARRAY));
            }
            // 检查每个initVal是否为Const
            boolean isConst = true;
            for(InitVal initVal : initVals){
                if(!initVal.getType().isConst()){
                    isConst = false;
                }
            }
            VarType type;
            if(subIsArray){
                // 检查每个initVal的元素数量是否一致
                int subNum = ((ArrayInitVal)initVals.get(0)).getInitVals().size();
                for(int i = 1; i < initVals.size(); i++){
                    check(subNum == ((ArrayInitVal)initVals.get(i)).getInitVals().size());
                }
                if(isConst){
                    int[][] constValue = new int[ast.getInitVals().size()][];
                    for(int i = 0; i < initVals.size(); i++){
                        constValue[i] = initVals.get(i).getType().getConstValue1();
                    }
                    type = new Array2Type(subNum, constValue);
                }else{
                    type = new Array2Type(subNum);
                }
            }else{
                if(isConst){
                    int[] constValue = new int[initVals.size()];
                    for(int i = 0; i < initVals.size(); i++){
                        constValue[i] = initVals.get(i).getType().getConstValue();
                    }
                    type = new ArrayType(constValue);
                }else{
                    type = new ArrayType();
                }
            }
            ast.setType(type);
        }
    }

    private class IntInitValExec extends Exec<IntInitVal>{
        @Override
        void exec(IntInitVal ast) {
            execSons(ast);
            check(ast.getExp().getOptionType().isPresent());
            ast.setType(ast.getExp().getOptionType().get());
        }
    }


    // 后置：判断其是否为Const，如果是则计算值(换言之，setType)
    private class BinaryExpExec extends Exec<BinaryExp> {
        @Override
        void exec(BinaryExp ast) {
            execSons(ast);
            Exp exp1 = ast.getExp1();
            Exp exp2 = ast.getExp2();
            BinaryOp op = ast.getOp();
            //考虑非常数数组的直接加减，后面的exps必须全部是int
            if(exp1.getOptionType().isPresent()){
                VarType firstType = exp1.getOptionType().get();
                if(!firstType.isConst() && firstType.is(GenericType.ARRAY, GenericType.ARRAY2)){
                    check(exp2.getOptionType().isPresent() && exp2.getOptionType().get().is(GenericType.INT));
                    check(op == BinaryOp.PLUS || op == BinaryOp.MINU);
                    ast.setType(exp1.getOptionType().get());
                    return;
                }
            }
            // 所有exp必须为int
            boolean isConst = checkIsConstInt(exp1) && checkIsConstInt(exp2);
            if(isConst){
                int value = compute(exp1.getOptionType().get().getConstValue(), op, exp2.getOptionType().get().getConstValue());
                ast.setType(new IntType(value));
            }else{
                ast.setType(new IntType());
            }
        }
        int compute(int a, BinaryOp op, int b){
            switch (op){
                case OR: return toInt(toBool(a) || toBool(b));
                case AND: return toInt(toBool(a) && toBool(b));
                case EQL: return toInt( a == b);
                case NEQ: return toInt(a != b);
                case GEQ: return toInt(a >= b);
                case GRE: return toInt(a > b);
                case LEQ: return toInt(a <= b);
                case LSS: return toInt(a < b);
                case PLUS: return a + b;
                case MINU: return a - b;
                case MULT: return a * b;
                case DIV: return  a / b;
                case MOD: return a % b;
                default: throw new SemanticException();
            }
        }
        boolean toBool(int a){
            return a != 0;
        }
        int toInt(boolean a){
            return a ? 1 : 0;
        }
    }

    private class UnaryExpExec extends Exec<UnaryExp>{
        @Override
        void exec(UnaryExp ast) {
            execSons(ast);
            Exp exp = ast.getExp();
            UnaryOp op = ast.getOp();
            if(exp instanceof UnaryExp){
                check(op != ((UnaryExp) exp).getOp());
            }
            boolean isConst = checkIsConstInt(exp);
            if(isConst){
                int value = exp.getOptionType().get().getConstValue();
                switch (op){
                    case MINU: value = - value; break;
                    case NOT: value = value != 0 ? 0 : 1; break;
                    case PLUS: break;
                }
                ast.setType(new IntType(value));
            }else{
                ast.setType(new IntType());
            }
        }
    }

    private class LValExec extends Exec<LVal>{
        @Override
        void exec(LVal ast) {
            execSons(ast);
            Optional<VarType> ret = symbolTable.getVariableSymbol(ast.getIdent());
            if(!ret.isPresent()){
                ast.setType(new IntType());
                return;
            }
            VarType identType = ret.get();
            int dimension = identType.getDimension();
            List<Exp> exps = ast.getExps();
            int expSize = exps.size();
            check(dimension >= expSize);
            VarType type;
            if(identType.isConst()){
                // 如果LVal是常量，则结果一定是一个0维数字，因为数组类型只出现在赋值或者函数中，但是常量数组又不会作为函数或者赋值语句的左值
                check(dimension == expSize);
                if(isAllConst(exps)){
                    switch (dimension){
                        case 0 : type = new IntType(identType.getConstValue()); break;
                        case 1 : type = new IntType(identType.getConstValue1()[exps.get(0).getOptionType().get().getConstValue()]); break;
                        case 2 : type = new IntType(identType.getConstValue2()[exps.get(0).getOptionType().get().getConstValue()][exps.get(1).getOptionType().get().getConstValue()]); break;
                        default: throw new SemanticException();
                    }
                }else {
                    type = new IntType();
                }
            }else{
                switch (identType.getDimension() - ast.getExps().size()) {
                    case 0:
                        type = new IntType();
                        break;
                    case 1:
                        type = new ArrayType();
                        break;
                    case 2:
                        type = identType;
                        break;
                    default:
                        throw new SemanticException();
                }
            }
            ast.setType(type);
        }

    }

    // if is const, return true; if one of is not int, error
    private boolean checkIsConstInt(List<? extends ExpTyper> exps){
        boolean isConst = true;
        for(ExpTyper exp : exps){
            if(!checkIsConstInt(exp))isConst = false;
        }
        return isConst;
    }
    // if is const, return true; if one of is not int, error
    private boolean checkIsConstInt(ExpTyper exp){
        check(exp.getOptionType().isPresent() && exp.getOptionType().get().getDimension() == 0);
        return exp.getOptionType().get().isConst();
    }

    private class FuncCallExec extends Exec<FuncCall>{
        @Override
        void exec(FuncCall ast) {
            execSons(ast);
            Optional<FuncType> ret = symbolTable.getFuncSymbol(ast.getIdent());
            if(!ret.isPresent()) return;
            FuncType identType = ret.get();
            List<Exp> exps = ast.getExps();
            if(identType.getParamNumber() != exps.size()){
                errorRecorder.paramNumNotMatch(ast.getIdent().line(), ast.getIdent().getValue(), identType.getParamNumber(), ast.getExps().size());
            }else{
                for(int i = 0; i < exps.size(); i++){
                    if(!exps.get(i).getOptionType().isPresent()){
                        errorRecorder.paramTypeNotMatch(ast.getIdent().line(), ast.getIdent().getValue(), identType.getParams().get(i).getDimension(), -1);
                    }else if(!identType.getParams().get(i).match(exps.get(i).getOptionType().get())){
                        errorRecorder.paramTypeNotMatch(ast.getIdent().line(), ast.getIdent().getValue(), identType.getParams().get(i).getDimension(), exps.get(i).getOptionType().get().getDimension());
                    }
                }
            }
            if(identType.isReturn()){
                ast.setType(new IntType());
            }
        }
    }

    private class BlockExec extends Exec<Block>{
        @Override
        void exec(Block ast) {
            symbolTable = new SymbolTable(symbolTable);
            execSons(ast);
            symbolTable = symbolTable.father();
        }
    }
    private class FuncDefExec extends Exec<FuncDef>{
        @Override
        void exec(FuncDef ast) {
            // 先获取参数的type
            List<VarType> types = new ArrayList<>();
            for(FuncDef.FuncFParam funcFParam : ast.getFuncFParams()){
                getExec(funcFParam).exec(funcFParam);
                types.add(funcFParam.getType());
            }
            symbolTable.addFunc(ast.getIdent(), new FuncType(ast.isInt(), types));
            symbolTable = new SymbolTable(symbolTable);
            for(FuncDef.FuncFParam funcFParam : ast.getFuncFParams()){
                symbolTable.addVariable(funcFParam.getIdent(), funcFParam.getType());
            }
            checkWhileBreak(ast.getBlock());
            List<BlockItem> blockItems = ast.getBlock().getBlockItems();
            for(BlockItem blockItem : blockItems){
                getExec(blockItem).exec(blockItem);
                if(blockItem instanceof Return){
                    if(!ast.isInt() && ((Return) blockItem).getExp().isPresent()){
                        errorRecorder.voidFuncReturnValue(((Return) blockItem).line());
                    }
                }
            }
            if(ast.isInt() && (blockItems.size() == 0 || !(blockItems.get(blockItems.size() - 1) instanceof Return) || !((Return) blockItems.get(blockItems.size() - 1)).getExp().isPresent()) ){
                errorRecorder.returnLack(ast.getBlock().endLine());
            }
            symbolTable = symbolTable.father();
        }
    }

    // 后置：设置type
    private class FuncFParamExec extends Exec<FuncDef.FuncFParam>{
        @Override
        void exec(FuncDef.FuncFParam ast) {
            execSons(ast);
            if(ast.getDimension() == 0){
                ast.setType(new IntType());
            }else if(ast.getDimension() == 1){
                ast.setType(new ArrayType());
            }else if(ast.getDimension() == 2){
                check(ast.getConstExp().isPresent());
                if(!checkIsConstInt(ast.getConstExp().get())){
                    errorRecorder.other(0);
                }
                ast.setType(new Array2Type(ast.getConstExp().get().getOptionType().get().getConstValue()));
            }else{
                errorRecorder.other(0);
            }
        }
    }
    private class MainFuncDefExec extends Exec<MainFuncDef>{
        @Override
        void exec(MainFuncDef ast) {
            symbolTable = new SymbolTable(symbolTable);
            checkWhileBreak(ast.getBlock());
            List<BlockItem> blockItems = ast.getBlock().getBlockItems();
            for(BlockItem blockItem : blockItems){
                getExec(blockItem).exec(blockItem);
                if(blockItem instanceof Return){
                    if(!((Return) blockItem).getExp().isPresent()){
                        errorRecorder.voidFuncReturnValue(((Return) blockItem).line());
                    }
                }
            }
            if((blockItems.size() == 0 || !(blockItems.get(blockItems.size() - 1) instanceof Return) || !((Return) blockItems.get(blockItems.size() - 1)).getExp().isPresent()) ){
                errorRecorder.returnLack(ast.getBlock().endLine());
            }
            symbolTable = symbolTable.father();
        }
    }

    private class AssignExec extends Exec<Assign> {
        @Override
        void exec(Assign ast) {
            execSons(ast);
            LVal lVal = ast.getLVal();
            if(lVal.getType().isConst()){
                errorRecorder.changeConst(lVal.getIdent().line(), lVal.getIdent().getValue());
            }
        }
    }
    private class PrintfExec extends Exec<Printf>{
        @Override
        void exec(Printf ast) {
            execSons(ast);
            List<Exp> exps = ast.getExps();
            FormatString formatString = ast.getFormatString();
            if(formatString.getFormatCharNumber() != exps.size()){
                errorRecorder.printfParamNotMatch(ast.getLine(), formatString.getFormatCharNumber(), exps.size());
            }
        }
    }

    // input a stmt that should not have continue or break directly
    private void checkWhileBreak(Stmt stmt){
        if(stmt instanceof Block){
            for(BlockItem blockItem : ((Block) stmt).getBlockItems()){
                if(blockItem instanceof Stmt){
                    checkWhileBreak((Stmt) blockItem);
                }
            }
        }else if(stmt instanceof Continue){
            errorRecorder.wrongContinue(((Continue) stmt).line());
        }else if(stmt instanceof Break){
            errorRecorder.wrongBreak(((Break) stmt).line());
        }else if(stmt instanceof If){
            If ifstmt = (If) stmt;
            ifstmt.getIfStmt().ifPresent(this::checkWhileBreak);
            if(ifstmt.getElseStmt().isPresent()){
                checkWhileBreak(ifstmt.getElseStmt().get());
            }
        }
    }






}
