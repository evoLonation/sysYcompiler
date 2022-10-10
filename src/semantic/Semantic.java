package semantic;

import common.CompileException;
import common.SemanticException;
import error.ErrorRecorder;
import lexer.FormatString;
import lexer.TerminalType;
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

    ErrorRecorder errorRecorder;

    SymbolTable symbolTable;

    Map<Class<?>, Exec<? extends ASD>> map = new HashMap<>();

    CompUnit compUnit;

    public Semantic(CompUnit compUnit, ErrorRecorder errorRecorder) {
        this.compUnit = compUnit;
        this.errorRecorder = errorRecorder;
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
        map.put(SubExp.class, new SubExpExec());
    }
    private final Exec<ASD> exec = new Exec<>();

    private Exec<ASD> getExec(ASD asd){
        Exec<ASD> ret = (Exec<ASD>) map.get(asd.getClass());
        return ret == null ? exec : ret;
    }


    public void analysis(){
        getExec(compUnit).exec(compUnit);
    }

    private class Exec<T extends ASD>{
        void exec(T asd){
            execSons(asd);
        }
        final void  execSons(T asd){
            for(ASD son : asd.sons()){
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
            errorRecorder.other(0);
        }
    }

    // 后置：填充符号表
    private class ConstDefExec extends Exec<ConstDef> {
        @Override
        void exec(ConstDef asd) {
            execSons(asd);
            // check const exps
            // must all const
            check(isAllConst(asd.getConstExps()));
            // dimension <= 2
            int dimension = asd.getConstExps().size();
            check(dimension <= 2);
            // check init value
            InitVal initVal = asd.getInitVal();
            // must be const
            check(initVal.getType().isConst());
            // dimension must equal
            check(dimension == initVal.getType().getDimension());
            VarType type;
            // number must equal
            if(dimension >= 1){
                int number1 = asd.getConstExps().get(0).getOptionType().get().getConstValue();
                check(number1 == initVal.getNumber());
                if(dimension == 2){
                    int number2 = asd.getConstExps().get(1).getOptionType().get().getConstValue();
                    check(number2 == initVal.getType().getSecondLen());
                    type = new Array2Type(number2,initVal.getType().getConstValue2());
                }else{
                    type = new ArrayType(initVal.getType().getConstValue1());
                }
            }else{
                type = new IntType(initVal.getType().getConstValue());
            }
            symbolTable.addVariableSymbol(asd.getIdent(), type);
        }
    }
    private class VarDefExec extends Exec<VarDef> {
        @Override
        void exec(VarDef asd) {
            execSons(asd);
            // check const exps
            // must all const
            check(isAllConst(asd.getConstExps()));
            // dimension <= 2
            int dimension = asd.getConstExps().size();
            check(dimension <= 2);
            VarType type;
            // check init value
            if(asd.getInitVal().isPresent()){
                InitVal initVal = asd.getInitVal().get();
                check(dimension == initVal.getType().getDimension());
                if(dimension >= 1){
                    int number1 = asd.getConstExps().get(0).getOptionType().get().getConstValue();
                    check(number1 == initVal.getNumber());
                    if(dimension == 2){
                        int number2 = asd.getConstExps().get(1).getOptionType().get().getConstValue();
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
                    case 2 : type = new Array2Type(asd.getConstExps().get(1).getOptionType().get().getConstValue()); break;
                    default: throw new SemanticException();
                }
            }
            symbolTable.addVariableSymbol(asd.getIdent(), type);
        }
    }


    // 后置：得到InitVal的Type
    private class ArrayInitValExec extends Exec<ArrayInitVal> {
        @Override
        void exec(ArrayInitVal asd) {
            execSons(asd);
            // 所有子InitVal都是一维数组或者单个数字
            List<InitVal> initVals = asd.getInitVals();
            // 子initval不能是2维数组
            check(!initVals.get(0).getType().is(GenericType.ARRAY2));
            // 检查每个InitVal的类型，必须为数组或者int
            // 检查每个InitVal 的类型是否一致
            boolean subIsArray = initVals.get(0).getType().is(GenericType.ARRAY);
            for(int i = 1; i < asd.getInitVals().size(); i++){
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
                int subNum = asd.getInitVals().get(0).getNumber();
                for(int i = 1; i < initVals.size(); i++){
                    check(subNum == initVals.get(i).getNumber());
                }
                if(isConst){
                    int[][] constValue = new int[asd.getInitVals().size()][];
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
            asd.setType(type);
        }
    }

    private class IntInitValExec extends Exec<IntInitVal>{
        @Override
        void exec(IntInitVal asd) {
            execSons(asd);
            check(asd.getExp().getOptionType().isPresent());
            asd.setType(asd.getExp().getOptionType().get());
        }
    }


    // 后置：判断其是否为Const，如果是则计算值(换言之，setType)
    private class BinaryExpExec extends Exec<BinaryExp> {
        @Override
        void exec(BinaryExp asd) {
            execSons(asd);
            Exp first = asd.getFirst();
            List<Exp> exps = asd.getExps();
            if(exps.size() == 0){
                // 如果只有first，则直接继承即可
                if(first.getOptionType().isPresent()){
                    asd.setType(first.getOptionType().get());
                }
            }else{
                //考虑非常数数组的直接加减，后面的exps必须全部是int
                if(first.getOptionType().isPresent()){
                    VarType firstType = first.getOptionType().get();
                    if(!firstType.isConst() && firstType.is(GenericType.ARRAY, GenericType.ARRAY2)){
                        for(Exp exp : exps){
                            check(exp.getOptionType().isPresent() && exp.getOptionType().get().is(GenericType.INT));
                        }
                        for(TerminalType op : asd.getOps()){
                            check(op == TerminalType.PLUS || op == TerminalType.MINU);
                        }
                        asd.setType(firstType);
                        return;
                    }
                }
                // 如果有exps，则所有exp参与了运算，所有exp包括first必须为int
                boolean isConst = checkIsConstInt(first) && checkIsConstInt(exps);
                if(isConst){
                    int value = first.getOptionType().get().getConstValue();
                    for(int i = 0; i < exps.size(); i ++){
                        value = compute(value, asd.getOps().get(i), exps.get(i).getOptionType().get().getConstValue());
                    }
                    asd.setType(new IntType(value));
                }else{
                    asd.setType(new IntType());
                }
            }
        }
        int compute(int a, TerminalType op, int b){
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
        void exec(UnaryExp asd) {
            execSons(asd);
            PrimaryExp primaryExp = asd.getPrimaryExp();
            List<TerminalType> ops = asd.getUnaryOps();
            if(ops.size() == 0){
                if(primaryExp.getOptionType().isPresent()){
                    asd.setType(primaryExp.getOptionType().get());
                }
            }else{
                boolean isConst = checkIsConstInt(primaryExp);
                if(isConst){
                    int value = primaryExp.getOptionType().get().getConstValue();
                    for(int i = ops.size() - 1; i >= 0; i--){
                        switch (ops.get(i)){
                            case MINU: value = - value; break;
                            case NOT: value = value != 0 ? 0 : 1; break;
                            case PLUS: break;
                            default: errorRecorder.other(0);
                        }
                    }
                    asd.setType(new IntType(value));
                }else{
                    asd.setType(new IntType());
                }
            }
        }
    }

    private class LValExec extends Exec<LVal>{
        @Override
        void exec(LVal asd) {
            execSons(asd);
            Optional<VarType> ret = symbolTable.getVariableSymbol(asd.getIdent());
            if(!ret.isPresent()){
                asd.setType(new IntType());
                return;
            }
            VarType identType = ret.get();
            int dimension = identType.getDimension();
            List<Exp> exps = asd.getExps();
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
                        case 2 : type = new IntType(identType.getConstValue2()[exps.get(0).getOptionType().get().getConstValue()][exps.get(0).getOptionType().get().getConstValue()]); break;
                        default: throw new SemanticException();
                    }
                }else {
                    type = new IntType();
                }
            }else{
                switch (identType.getDimension() - asd.getExps().size()) {
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
            asd.setType(type);
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
        void exec(FuncCall asd) {
            execSons(asd);
            Optional<FuncType> ret = symbolTable.getFuncSymbol(asd.getIdent());
            if(!ret.isPresent()) return;
            FuncType identType = ret.get();
            List<Exp> exps = asd.getExps();
            if(identType.getParamNumber() != exps.size()){
                errorRecorder.paramNumNotMatch(asd.getIdent().line(), asd.getIdent().getValue(), identType.getParamNumber(), asd.getExps().size());
            }else{
                for(int i = 0; i < exps.size(); i++){
                    if(!exps.get(i).getOptionType().isPresent()){
                        errorRecorder.paramTypeNotMatch(asd.getIdent().line(), asd.getIdent().getValue(), identType.getParams().get(i).getDimension(), -1);
                    }else if(!identType.getParams().get(i).match(exps.get(i).getOptionType().get())){
                        errorRecorder.paramTypeNotMatch(asd.getIdent().line(), asd.getIdent().getValue(), identType.getParams().get(i).getDimension(), exps.get(i).getOptionType().get().getDimension());
                    }
                }
            }
            if(identType.isReturn()){
                asd.setType(new IntType());
            }
        }
    }
    private class SubExpExec extends Exec<SubExp>{
        @Override
        void exec(SubExp asd) {
            execSons(asd);
            if(asd.getExp().getOptionType().isPresent()){
                asd.setType(asd.getExp().getOptionType().get());
            }
        }
    }

    private class BlockExec extends Exec<Block>{
        @Override
        void exec(Block asd) {
            symbolTable = new SymbolTable(symbolTable);
            execSons(asd);
            symbolTable = symbolTable.father();
        }
    }
    private class FuncDefExec extends Exec<FuncDef>{
        @Override
        void exec(FuncDef asd) {
            // 先获取参数的type
            List<VarType> types = new ArrayList<>();
            for(FuncDef.FuncFParam funcFParam : asd.getFuncFParams()){
                getExec(funcFParam).exec(funcFParam);
                types.add(funcFParam.getType());
            }
            symbolTable.addFuncSymbol(asd.getIdent(), new FuncType(asd.isInt(), types));
            symbolTable = new SymbolTable(symbolTable);
            for(FuncDef.FuncFParam funcFParam : asd.getFuncFParams()){
                symbolTable.addVariableSymbol(funcFParam.getIdent(), funcFParam.getType());
            }
            checkWhileBreak(asd.getBlock());
            List<BlockItem> blockItems = asd.getBlock().getBlockItems();
            for(BlockItem blockItem : blockItems){
                getExec(blockItem).exec(blockItem);
                if(blockItem instanceof Return){
                    if(!asd.isInt() && ((Return) blockItem).getExp().isPresent()){
                        errorRecorder.voidFuncReturnValue(((Return) blockItem).line());
                    }
                }
            }
            if(asd.isInt() && (blockItems.size() == 0 || !(blockItems.get(blockItems.size() - 1) instanceof Return) || !((Return) blockItems.get(blockItems.size() - 1)).getExp().isPresent()) ){
                errorRecorder.returnLack(asd.getBlock().endLine());
            }
            symbolTable = symbolTable.father();
        }
    }

    // 后置：设置type
    private class FuncFParamExec extends Exec<FuncDef.FuncFParam>{
        @Override
        void exec(FuncDef.FuncFParam asd) {
            execSons(asd);
            if(asd.getDimension() == 0){
                asd.setType(new IntType());
            }else if(asd.getDimension() == 1){
                asd.setType(new ArrayType());
            }else if(asd.getDimension() == 2){
                check(asd.getConstExp().isPresent());
                if(!checkIsConstInt(asd.getConstExp().get())){
                    errorRecorder.other(0);
                }
                asd.setType(new Array2Type(asd.getConstExp().get().getOptionType().get().getConstValue()));
            }else{
                errorRecorder.other(0);
            }
        }
    }
    private class MainFuncDefExec extends Exec<MainFuncDef>{
        @Override
        void exec(MainFuncDef asd) {
            symbolTable = new SymbolTable(symbolTable);
            checkWhileBreak(asd.getBlock());
            List<BlockItem> blockItems = asd.getBlock().getBlockItems();
            for(BlockItem blockItem : blockItems){
                getExec(blockItem).exec(blockItem);
                if(blockItem instanceof Return){
                    if(!((Return) blockItem).getExp().isPresent()){
                        errorRecorder.voidFuncReturnValue(((Return) blockItem).line());
                    }
                }
            }
            if((blockItems.size() == 0 || !(blockItems.get(blockItems.size() - 1) instanceof Return) || !((Return) blockItems.get(blockItems.size() - 1)).getExp().isPresent()) ){
                errorRecorder.returnLack(asd.getBlock().endLine());
            }
            symbolTable = symbolTable.father();
        }
    }

    private class AssignExec extends Exec<Assign> {
        @Override
        void exec(Assign asd) {
            execSons(asd);
            LVal lVal = asd.getLVal();
            if(lVal.getType().isConst()){
                errorRecorder.changeConst(lVal.getIdent().line(), lVal.getIdent().getValue());
            }
        }
    }
    private class PrintfExec extends Exec<Printf>{
        @Override
        void exec(Printf asd) {
            execSons(asd);
            List<Exp> exps = asd.getExps();
            FormatString formatString = asd.getFormatString();
            if(formatString.getFormatCharNumber() != exps.size()){
                errorRecorder.printfParamNotMatch(asd.getLine(), formatString.getFormatCharNumber(), exps.size());
            }
        }
    }

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
