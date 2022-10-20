package semantic;

import common.SemanticException;
import midcode.instrument.Assignment;
import midcode.value.*;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.LVal;
import parser.nonterminal.exp.Number;
import type.ArrayType;
import type.IntType;
import type.VarType;

import java.util.List;
import java.util.Optional;


public class LValGenerator extends Generator {
    private final LVal lVal;

    public LValGenerator(LVal lVal) {
        this.lVal = lVal;
    }

    public enum ResultType {
        IntVariable,
        Constant,
        Array0,
        Array1,
        Array2,
    }

    private ResultType resultType;
    private Object result;

    public static class ArrayResult {
        public ArrayVariable arrayVariable;
        public RValue offset;

        private ArrayResult(ArrayVariable arrayVariable, RValue offset) {
            this.arrayVariable = arrayVariable;
            this.offset = offset;
        }
    }
    public static class Array2Result extends ArrayResult{
        public int secondLen;
        private Array2Result(ArrayVariable arrayVariable, RValue offset, int secondLen) {
            super(arrayVariable, offset);
            this.secondLen = secondLen;
        }
    }

    public ResultType getResultType() {
        return resultType;
    }

    public IntVariable getIntVariable(){
        check(resultType == ResultType.IntVariable);
        return (IntVariable)result;
    }
    public Constant getConstant(){
        check(resultType == ResultType.Constant);
        return (Constant) result;
    }

    public ArrayResult getArray01(){
        check(resultType == ResultType.Array0 || resultType == ResultType.Array1);
        return (ArrayResult) result;
    }

    public Array2Result getArray2() {
        check(resultType == ResultType.Array2);
        return (Array2Result) result;
    }



    @Override
    protected LValGenerator generate() {
        String ident = lVal.getIdent().getValue();
        Optional<VarType> typeOptional = symbolTable.getVariableSymbol(lVal.getIdent());
        if(!typeOptional.isPresent()){
            Temp temp = valueFactory.newTemp();
            addMidCode(new Assignment(temp, new Constant(0)));
            result = temp;
            resultType = ResultType.IntVariable;
        }else{
            VarType type = typeOptional.get();
            if(type instanceof IntType){
                if(((IntType) type).getConstValue().isPresent()){
                    result = new Constant(((IntType) type).getConstValue().get());
                    resultType = ResultType.Constant;
                }else{
                    result = valueFactory.newIntVariable(lVal.getIdent().getValue());
                    resultType = ResultType.IntVariable;
                }
            }else if(type instanceof ArrayType){
                List<Exp> exps = lVal.getExps();
                check(exps.size() <= 2);
                RValue offset;
                Exp offsetExp;
                int secondLen = 0;
                if(((ArrayType) type).getSecondLen().isPresent()){
                    secondLen = ((ArrayType) type).getSecondLen().get();
                    Exp exp1 = new Number(0);
                    Exp exp2 = new Number(0);
                    resultType = ResultType.Array2;
                    if(exps.size() == 1){
                        resultType = ResultType.Array1;
                        exp1 = exps.get(0);
                    }
                    if(exps.size() == 2){
                        resultType = ResultType.Array0;
                        exp2 = exps.get(1);
                    }
                    offsetExp =  new BinaryExp(new BinaryExp(exp1, BinaryOp.MULT, new Number(secondLen)), BinaryOp.PLUS, exp2);
                }else{
                    check(exps.size() <= 1);
                    if(exps.size() == 0){
                        resultType = ResultType.Array1;
                        offsetExp = new Number(0);
                    }else{
                        resultType = ResultType.Array0;
                        offsetExp = exps.get(0);
                    }
                }

                AddExpGenerator generator = new AddExpGenerator(offsetExp).generate();
                addMidCode(generator.getMidCodes());
                offset = generator.getResult();
                if(offset instanceof Constant && ((ArrayType) type).getConstValue().isPresent() && resultType == ResultType.Array0){
                    int[] constValue = ((ArrayType) type).getConstValue().get();
                    result = new Constant(constValue[((Constant) offset).getNumber()]);
                    resultType = ResultType.Constant;
                }else{
                    if(resultType == ResultType.Array2){
                        result = new Array2Result(valueFactory.getArrayVariable(ident), offset, secondLen);
                    }else{
                        result = new ArrayResult(valueFactory.getArrayVariable(ident), offset);
                    }
                }
            }else{
                throw new SemanticException();
            }
        }
        return this;
    }
}
