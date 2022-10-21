package semantic;

import lexer.Ident;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.instrument.Store;
import midcode.value.Constant;
import midcode.value.RValue;
import midcode.value.Temp;
import parser.nonterminal.decl.*;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
import type.ArrayType;
import type.IntType;
import type.VarType;

import java.util.ArrayList;
import java.util.List;

public class DeclGenerator extends InstrumentGenerator{
    private final Decl decl;

    public DeclGenerator(List<Instrument> instruments, Decl decl) {
        super(instruments);
        this.decl = decl;
    }

    @Override
    protected void generate() {
        for(Def def : decl.getDefs()){
            execution.exec(def);
        }
    }


    private final VoidExecution<Def> execution = new VoidExecution<Def>() {
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
                        case 0 :
                            symbolTable.addLocalVariable(ident, new IntType());
                            break;
                        case 1 :
                            symbolTable.addLocalVariable(ident, new ArrayType(lens.get(0)));
                            break;
                        case 2 :
                            symbolTable.addLocalVariable(ident, new ArrayType(lens.get(0) * lens.get(1), lens.get(1)));
                            break;
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
    };


    private List<Integer> getLens(List<Exp> constExps){
        List<Integer> lens = new ArrayList<>();
        for(Exp exp : constExps){
            ExpGenerator generator = new ExpGenerator(instruments, exp);
            check(generator.getResult().value instanceof Constant);
            lens.add(((Constant)generator.getResult().value).getNumber());
        }
        return lens;
    }


    private void assignment0(Ident ident, IntInitVal initVal, boolean isConst){
        ExpGenerator generator = new ExpGenerator(instruments, initVal.getExp());
        RValue value = generator.getResult().value;
        VarType type = generator.getResult().type;
        if(isConst){
            assert value instanceof Constant;
            symbolTable.addLocalVariable(ident, new IntType(((Constant) value).getNumber()));
            return;
        }
        symbolTable.addLocalVariable(ident, new IntType());
        addInstrument(new Assignment(valueFactory.newVariable(ident), value));
    }

    private void assignment1(Ident ident, int firstLen, ArrayInitVal initVal, boolean isConst) {
        List<InitVal> initVals = initVal.getInitVals();
        check(initVals.size() == firstLen);
        List<RValue> results = new ArrayList<>();
        for(InitVal subInitVal : initVals){
            assert subInitVal instanceof IntInitVal;
            ExpGenerator generator = new ExpGenerator(instruments, ((IntInitVal)subInitVal).getExp());
            assert generator.getResult().type instanceof IntType;
            results.add(generator.getResult().value);
        }
        if(isConst){
            int[] constValue = new int[firstLen];
            for(int i = 0; i < results.size(); i++){
                assert results.get(0) instanceof Constant;
                constValue[i] = ((Constant) results.get(0)).getNumber();
            }
            symbolTable.addLocalVariable(ident, new ArrayType(constValue));
        }else{
            symbolTable.addLocalVariable(ident, new ArrayType(firstLen));
        }
        // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
        initArray(ident, results);
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
                ExpGenerator generator = new ExpGenerator(instruments, ((IntInitVal)sub2InitVal).getExp());
                assert generator.getResult().type instanceof IntType;
                results.add(generator.getResult().value);
            }
        }
        if(isConst){
            int[] constValue = new int[firstLen*secondLen];
            for(int i = 0; i < results.size(); i++){
                assert results.get(0) instanceof Constant;
                constValue[i] = ((Constant) results.get(i)).getNumber();
            }
            symbolTable.addLocalVariable(ident, new ArrayType(constValue, secondLen));
        }else{
            symbolTable.addLocalVariable(ident, new ArrayType(firstLen * secondLen, secondLen));
        }
        // 所有的exp都计算完才能将该def填入符号表，否则不能排除initval中的符号是def本地的情况
        initArray(ident, results);
    }

    private void initArray(Ident ident, List<RValue> results) {
        for(int i = 0; i < results.size(); i++){
            Temp address = valueFactory.newTemp();
            addInstrument(new BinaryOperation(valueFactory.getNewestVariable(ident), new Constant(i), BinaryOp.PLUS, address));
            addInstrument(new Store(address, results.get(i)));
        }
    }

}
