package frontend.IRGenerate;

import common.SemanticException;
import frontend.IRGenerate.util.ModuleFactory;
import frontend.lexer.Ident;
import midcode.*;
import midcode.Module;
import midcode.value.Constant;
import midcode.value.RValue;
import frontend.parser.nonterminal.Block;
import frontend.parser.nonterminal.CompUnit;
import frontend.parser.nonterminal.FuncDef;
import frontend.parser.nonterminal.MainFuncDef;
import frontend.parser.nonterminal.decl.*;
import frontend.parser.nonterminal.exp.Exp;
import frontend.type.ArrayType;

import java.util.ArrayList;
import java.util.List;

public class ModuleGenerator extends Generator{
    private final CompUnit compUnit;
    private ModuleFactory moduleFactory = ModuleFactory.getInstance();
    public ModuleGenerator(CompUnit compUnit) {
        this.compUnit = compUnit;
    }


    public Module generate() {
        moduleFactory.newModule();
        moduleFactory.setStaticData(dealDecl(compUnit.getDecls()));
        for(FuncDef funcDef : compUnit.getFuncDefs()){
            moduleFactory.addFunction(new FuncDefGenerator(funcDef).generate());
        }
        moduleFactory.setMain(dealMain(compUnit.getMainFuncDef()));
        return moduleFactory.done();
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
                        // 对于常量int声明，不应该加在staticData中
                        int tmp = assignment0(ident, (IntInitVal) initVal, isConst);
                        if(isConst){
                            tmp2 = new int[0];
                        }else{
                            tmp2 = new int[]{tmp};
                        }
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
            RValue rValue = new ExpGenerator(exp).generate().getRValueResult();
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
            RValue rValue = new ExpGenerator(initVal.getExp()).generate().getRValueResult();
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
                RValue rvalue = new ExpGenerator(((IntInitVal)subInitVal).getExp()).generate().getRValueResult();
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
                    RValue rvalue = new ExpGenerator(((IntInitVal)sub2InitVal).getExp()).generate().getRValueResult();
                    assert rvalue instanceof Constant;
                    ret[i*secondLen + j] = ((Constant) rvalue).getNumber();
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
        function = basicBlockFactory.newMainFunction();
        Block block = mainFuncDef.getBlock();
        symbolTable.newMain(function);
        new FuncBlockGenerator(block).generate();
        symbolTable.outBlock();
        basicBlockFactory.outFunction(symbolTable.getMaxOffset());
        return function;
    }
}
