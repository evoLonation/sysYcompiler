package frontend.semantic;

import common.SemanticException;
import frontend.lexer.Ident;
import midcode.instrument.Assignment;
import midcode.instrument.Store;
import midcode.value.Constant;
import midcode.value.PointerValue;
import midcode.value.RValue;
import frontend.parser.nonterminal.decl.*;
import frontend.parser.nonterminal.exp.Exp;
import frontend.type.ArrayType;

import java.util.ArrayList;
import java.util.List;

/**
 * 仅适用于局部变量的声明
 */
public class DeclGenerator extends InstrumentGenerator{
    private final Decl decl;

    DeclGenerator(Decl decl) {
        this.decl = decl;
    }

    void generate() {
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
            switch (dimension) {
                case 0: {
                    assert initVal == null || initVal instanceof IntInitVal;
                    assignment0(ident, (IntInitVal) initVal, isConst);
                    break;
                }
                case 1: {
                    assert initVal == null || initVal instanceof ArrayInitVal;
                    assignment1(ident, lens.get(0), (ArrayInitVal) initVal, isConst);
                    break;
                }
                case 2: {
                    assert initVal == null || initVal instanceof ArrayInitVal;
                    assignment2(ident, lens.get(0), lens.get(1), (ArrayInitVal) initVal, isConst);
                    break;
                }
            }
        }
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

    private void assignment0(Ident ident, IntInitVal initVal, boolean isConst){
        if(initVal == null){
            symbolTable.newInteger(ident, false);
        }else{
            RValue rValue = new ExpGenerator(initVal.getExp()).generate().getRValueResult();
            if(isConst){
                assert rValue instanceof Constant;
                symbolTable.newInteger(ident, false, ((Constant) rValue).getNumber());
            }else{
                symbolTable.newInteger(ident, false);
                addInstrument(new Assignment(valueFactory.newVariable(ident), rValue));
            }
        }
    }

    private void assignment1(Ident ident, int firstLen, ArrayInitVal initVal, boolean isConst) {
        if(initVal == null){
            symbolTable.newArray(ident, false, new ArrayType(firstLen));
        }else{
            List<InitVal> initVals = initVal.getInitVals();
            assert initVals.size() == firstLen;
            List<RValue> results = new ArrayList<>();
            for(InitVal subInitVal : initVals){
                assert subInitVal instanceof IntInitVal;
                results.add(new ExpGenerator(((IntInitVal)subInitVal).getExp()).generate().getRValueResult());
            }
            if(isConst){
                int[] constValue = new int[firstLen];
                for(int i = 0; i < results.size(); i++){
                    assert results.get(i) instanceof Constant;
                    constValue[i] = ((Constant) results.get(i)).getNumber();
                }
                symbolTable.newArray(ident, false, new ArrayType(firstLen), constValue);
            }else{
                symbolTable.newArray(ident, false, new ArrayType(firstLen));
            }
            // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
            initArray(ident, results);
        }
    }

    private void assignment2(Ident ident, int firstLen, int secondLen, ArrayInitVal initVal, boolean isConst){
        if(initVal == null){
            symbolTable.newArray(ident, false, new ArrayType(firstLen, secondLen));
        }else{
            List<InitVal> initVals = initVal.getInitVals();
            assert initVals.size() == firstLen;
            List<RValue> results = new ArrayList<>();
            for(InitVal sub1InitVal : initVals){
                assert sub1InitVal instanceof ArrayInitVal;
                List<InitVal> sub2InitVals = ((ArrayInitVal) sub1InitVal).getInitVals();
                assert secondLen == sub2InitVals.size();
                for(InitVal sub2InitVal : sub2InitVals) {
                    assert sub2InitVal instanceof IntInitVal;
                    results.add(new ExpGenerator(((IntInitVal)sub2InitVal).getExp()).generate().getRValueResult());
                }
            }
            if(isConst){
                int[] constValue = new int[firstLen * secondLen];
                for(int i = 0; i < results.size(); i++){
                    assert results.get(0) instanceof Constant;
                    constValue[i] = ((Constant) results.get(i)).getNumber();
                }
                symbolTable.newArray(ident, false, new ArrayType(firstLen, secondLen), constValue);
            }else{
                symbolTable.newArray(ident, false, new ArrayType(firstLen, secondLen));
            }
            // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
            initArray(ident, results);
        }
    }

    private void initArray(Ident ident, List<RValue> results) {
        for(int i = 0; i < results.size(); i++){
            PointerValue address = valueFactory.newPointer(ident, new Constant(i));
            addInstrument(new Store(address, results.get(i)));
        }
    }

}
