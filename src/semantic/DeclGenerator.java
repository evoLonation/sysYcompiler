package semantic;

import lexer.Ident;
import midcode.instrument.Assignment;
import midcode.instrument.Store;
import midcode.value.Constant;
import midcode.value.RValue;
import parser.nonterminal.decl.*;
import parser.nonterminal.exp.Exp;
import type.ArrayType;
import type.IntType;

import java.util.ArrayList;
import java.util.List;

public class DeclGenerator extends VoidExecution<Def>{
    private final Decl decl;

    public DeclGenerator(Decl decl) {
        this.decl = decl;
    }

    @Override
    protected DeclGenerator generate() {
        for(Def def : decl.getDefs()){
            exec(def);
        }
        return this;
    }

    @Override
    protected void inject() {
        inject(VarDef.class, def -> {
            Ident ident = def.getIdent();
            List<Integer> lens = getLens(def.getConstExps());
            check(lens.size() <= 2);
            int dimension = lens.size();

            if(def.getInitVal().isPresent()) {
                InitVal initVal = def.getInitVal().get();
                switch (dimension) {
                    case 0: {
                        assert initVal instanceof IntInitVal;
                        assignment0(ident, (IntInitVal) initVal, false);
                        break;
                    }
                    case 1: {
                        assert initVal instanceof ArrayInitVal;
                        assignment1(ident, lens.get(0), (ArrayInitVal) initVal, false);
                        break;
                    }
                    case 2: {
                        assert initVal instanceof ArrayInitVal;
                        assignment2(ident, lens.get(0), lens.get(1), (ArrayInitVal) initVal, false);
                        break;
                    }
                }
            }else{
                switch (dimension) {
                    case 0 : {
                        symbolTable.addVariable(ident, new IntType());
                        break;
                    }
                    case 1 :{
                        symbolTable.addVariable(ident, new ArrayType());
                        addMidCode(new Alloc(valueFactory.getArrayVariable(ident), lens.get(0)));
                        break;
                    }
                    case 2 : {
                        symbolTable.addVariable(ident, new ArrayType(lens.get(1)));
                        addMidCode(new Alloc(valueFactory.getArrayVariable(ident), lens.get(0) * lens.get(1)));
                        break;
                    }
                }
            }
        });

        inject(ConstDef.class, def -> {
            Ident ident = def.getIdent();
            List<Integer> lens = getLens(def.getConstExps());
            check(lens.size() <= 2);
            int dimension = lens.size();
            InitVal initVal = def.getInitVal();
            switch (dimension) {
                case 0: {
                    assert initVal instanceof IntInitVal;
                    assignment0(ident, (IntInitVal) initVal, true);
                    break;
                }
                case 1: {
                    assert initVal instanceof ArrayInitVal;
                    assignment1(ident, lens.get(0), (ArrayInitVal) initVal, true);
                    break;
                }
                case 2: {
                    assert initVal instanceof ArrayInitVal;
                    assignment2(ident, lens.get(0), lens.get(1), (ArrayInitVal) initVal, true);
                    break;
                }
            }
        });
    }
    private List<Integer> getLens(List<Exp> constExps){
        List<Integer> lens = new ArrayList<>();
        for(Exp exp : constExps){
            AddExpGenerator generator = new AddExpGenerator(exp).generate();
            addMidCode(generator.getMidCodes());
            check(generator.getResult() instanceof Constant);
            lens.add(((Constant)generator.getResult()).getNumber());
        }
        return lens;
    }


    private void assignment0(Ident ident, IntInitVal initVal, boolean isConst){
        AddExpGenerator generator = new AddExpGenerator(initVal.getExp()).generate();
        addMidCode(generator.getMidCodes());
        RValue result = generator.getResult();
        if(result instanceof Constant){
            symbolTable.addVariable(ident, new IntType(((Constant) result).getNumber()));
            return;
        }
        symbolTable.addVariable(ident, new IntType());
        addMidCode(new Assignment(valueFactory.newIntVariable(ident), result));
    }

    private void assignment1(Ident ident, int firstLen, ArrayInitVal initVal, boolean isConst){
        List<InitVal> initVals = initVal.getInitVals();
        check(initVals.size() == firstLen);
        List<RValue> results = new ArrayList<>();
        for(InitVal subInitVal : initVals){
            assert subInitVal instanceof IntInitVal;
            AddExpGenerator generator = new AddExpGenerator(((IntInitVal)subInitVal).getExp()).generate();
            addMidCode(generator.getMidCodes());
            results.add(generator.getResult());
        }
        if(isConst){
            int[] constValue = new int[firstLen];
            for(int i = 0; i < results.size(); i++){
                assert results.get(0) instanceof Constant;
                constValue[i] = ((Constant) results.get(0)).getNumber();
            }
            symbolTable.addVariable(ident, new ArrayType(constValue));
        }else{
            symbolTable.addVariable(ident, new ArrayType());
        }
        addMidCode(new Alloc(valueFactory.getArrayVariable(ident), results.size()));
        // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
        for(int i = 0; i < results.size(); i++){
            addMidCode(new Store(valueFactory.getArrayVariable(ident), new Constant(i), results.get(0)));
        }
    }

    private void assignment2(Ident ident, int firstLen, int secondLen, ArrayInitVal initVal, boolean isConst){
        List<InitVal> initVals = initVal.getInitVals();
        check(initVals.size() == firstLen);
        List<RValue> results = new ArrayList<>();
        for(InitVal sub1InitVal : initVals){
            assert sub1InitVal instanceof ArrayInitVal;
            List<InitVal> sub2InitVals = ((ArrayInitVal) sub1InitVal).getInitVals();
            assert secondLen == sub2InitVals.size();
            for(InitVal sub2InitVal : sub2InitVals) {
                assert sub2InitVal instanceof IntInitVal;
                AddExpGenerator generator = new AddExpGenerator(((IntInitVal)sub2InitVal).getExp()).generate();
                addMidCode(generator.getMidCodes());
                results.add(generator.getResult());
            }
        }
        if(isConst){
            int[] constValue = new int[firstLen];
            for(int i = 0; i < results.size(); i++){
                assert results.get(0) instanceof Constant;
                constValue[i] = ((Constant) results.get(0)).getNumber();
            }
            symbolTable.addVariable(ident, new ArrayType(constValue, secondLen));
        }else{
            symbolTable.addVariable(ident, new ArrayType(secondLen));
        }
        addMidCode(new Alloc(valueFactory.getArrayVariable(ident), results.size()));
        // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
        for(int i = 0; i < results.size(); i++){
            addMidCode(new Store(valueFactory.getArrayVariable(ident), new Constant(i), results.get(0)));
        }
    }

}
