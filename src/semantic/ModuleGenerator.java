package semantic;

import common.SemanticException;
import lexer.Ident;
import midcode.BasicBlock;
import midcode.BasicBlockFactory;
import midcode.Function;
import midcode.Module;
import midcode.instrument.Assignment;
import midcode.value.Constant;
import midcode.value.RValue;
import parser.nonterminal.Block;
import parser.nonterminal.CompUnit;
import parser.nonterminal.FuncDef;
import parser.nonterminal.MainFuncDef;
import parser.nonterminal.decl.*;
import parser.nonterminal.exp.Exp;
import type.ArrayType;
import type.IntType;
import type.PointerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModuleGenerator extends Generator{
    private final CompUnit compUnit;
    public ModuleGenerator(CompUnit compUnit) {
        this.compUnit = compUnit;
        generate();
    }

    private final Module module = new Module();

    public Module getModule() {
        return module;
    }

    @Override
    protected void generate() {
        module.staticData = dealDecl(compUnit.getDecls());
        module.functions = new ArrayList<>();
        for(FuncDef funcDef : compUnit.getFuncDefs()){
            module.functions.add(new FuncDefGenerator(funcDef).getFunction());
        }
        module.mainFunc = dealMain(compUnit.getMainFuncDef());
    }

    private int[] dealDecl(List<Decl> decls){
        int[] ret = new int[0];
        for(Decl decl : decls){
            for(Def def : decl.getDefs()){
                Ident ident = def.getIdent();
                List<Integer> lens = getLens(def.getConstExps());
                assert  lens.size() <= 2;
                int dimension = lens.size();
                InitVal initVal;
                boolean isConst;
                if(def instanceof ConstDef){
                    isConst = true;
                    initVal = ((ConstDef) def).getInitVal();
                }else if(def instanceof VarDef){
                    isConst = false;
                    initVal = ((VarDef)def).getInitVal().orElse(null);
                }else{
                    throw new SemanticException();
                }
                int[] tmp2;
                switch (dimension) {
                    case 0: {
                        assert initVal == null || initVal instanceof IntInitVal;
                        tmp2 = new int[]{assignment0(ident, (IntInitVal) initVal, isConst)};
                        break;
                    }
                    case 1: {
                        assert initVal == null || initVal instanceof ArrayInitVal;
                        tmp2 = assignment1(ident, lens.get(0), (ArrayInitVal) initVal, isConst);
                        break;
                    }
                    case 2: {
                        assert initVal == null || initVal instanceof ArrayInitVal;
                        tmp2 = assignment2(ident, lens.get(0), lens.get(1), (ArrayInitVal) initVal, isConst);
                        break;
                    }
                    default: throw new SemanticException();
                }
                int[] tmp1 = ret;
                ret = new int[tmp2.length + tmp1.length];
                System.arraycopy(tmp1, 0, ret, 0, tmp1.length);
                System.arraycopy(tmp2, 0, ret, tmp1.length, tmp2.length);
            }
        }
        return ret;
    }

    protected List<Integer> getLens(List<Exp> constExps) {
        List<Integer> lens = new ArrayList<>();
        for(Exp exp : constExps){
            RValue rValue = new ExpGenerator(new ArrayList<>(), exp).getRValueResult();
            assert rValue instanceof Constant;
            lens.add(((Constant)rValue).getNumber());
        }
        return lens;
    }


    private int assignment0(Ident ident, IntInitVal initVal, boolean isConst){
        if(initVal == null){
            symbolTable.newInteger(ident, true);
            return 0;
        }else{
            RValue rValue = new ExpGenerator(new ArrayList<>(), initVal.getExp()).getRValueResult();
            assert rValue instanceof Constant;
            int number = ((Constant) rValue).getNumber();
            if(isConst){
                symbolTable.newInteger(ident, true, number);
            }else {
                symbolTable.newInteger(ident, true);
            }
            return number;
        }
    }

    private int[] assignment1(Ident ident, int firstLen, ArrayInitVal initVal, boolean isConst) {
        int[] ret = new int[firstLen];
        if(initVal == null){
            symbolTable.newArray(ident, true, new ArrayType(firstLen));
        }else{
            List<InitVal> initVals = initVal.getInitVals();
            assert initVals.size() == firstLen;
            for(int i = 0; i < firstLen; i++){
                InitVal subInitVal = initVals.get(i);
                assert subInitVal instanceof IntInitVal;
                RValue rvalue = new ExpGenerator(new ArrayList<>(), ((IntInitVal)subInitVal).getExp()).getRValueResult();
                assert rvalue instanceof Constant;
                ret[i] = ((Constant) rvalue).getNumber();
            }
            if(isConst){
                symbolTable.newArray(ident, true, new ArrayType(firstLen), ret);
            }else{
                symbolTable.newArray(ident, true, new ArrayType(firstLen));
            }
        }
        return ret;
    }

    private int[] assignment2(Ident ident, int firstLen, int secondLen, ArrayInitVal initVal, boolean isConst){
        int[] ret = new int[firstLen * secondLen];
        if(initVal == null){
            symbolTable.newArray(ident, true, new ArrayType(firstLen, secondLen));
        } else {
            List<InitVal> initVals = initVal.getInitVals();
            assert initVals.size() == firstLen;
            for(int i = 0; i < firstLen; i++){
                InitVal sub1InitVal = initVals.get(i);
                assert sub1InitVal instanceof ArrayInitVal;
                List<InitVal> sub2InitVals = ((ArrayInitVal) sub1InitVal).getInitVals();
                assert secondLen == sub2InitVals.size();
                for(int j = 0; j < secondLen; j++){
                    InitVal sub2InitVal = sub2InitVals.get(j);
                    assert sub2InitVal instanceof IntInitVal;
                    RValue rvalue = new ExpGenerator(new ArrayList<>(), ((IntInitVal)sub2InitVal).getExp()).getRValueResult();
                    assert rvalue instanceof Constant;
                    ret[i*firstLen + j] = ((Constant) rvalue).getNumber();
                }
            }
            if(isConst){
                symbolTable.newArray(ident, true, new ArrayType(firstLen, secondLen), ret);
            }else{
                symbolTable.newArray(ident, true, new ArrayType(firstLen, secondLen));
            }
        }
        return ret;
    }
    private Function dealMain(MainFuncDef mainFuncDef){
        Function function;
        isReturn = true;
        function = BasicBlockFactory.getInstance().newMainFunction();
        BasicBlock funcBasicBlock = function.getEntry();
        Block block = mainFuncDef.getBlock();
        new FuncBlockGenerator(funcBasicBlock, block);
        return function;
    }
}
