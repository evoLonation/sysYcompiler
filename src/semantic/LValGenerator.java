package semantic;

import common.SemanticException;
import lexer.Ident;
import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.value.*;
import parser.nonterminal.exp.BinaryExp;
import parser.nonterminal.exp.BinaryOp;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.exp.LVal;
import parser.nonterminal.exp.Number;
import type.*;

import java.util.List;
import java.util.Optional;

/**
 * 当IdentType是指针时，最后返回的value肯定是一个地址，此时如果type为int，你可以进行取值操作，如果type为pointer，那么可以直接用
 * 当对应变量是int时，返回的value肯定是一个值
 */
public class LValGenerator extends InstrumentGenerator {
    private final LVal lVal;
    private final boolean changeable;

    public LValGenerator(List<Instrument> instruments, LVal lVal, boolean changeable) {
        super(instruments);
        this.lVal = lVal;
        this.changeable = changeable;
    }

    public static enum IdentType {
        Pointer,
        Integer,
    }

    public static class Result{
        public RValue value;
        public VarType type;
        public IdentType identType;

        public Result(RValue value, VarType type, IdentType identType) {
            this.value = value;
            this.type = type;
            this.identType = identType;
        }
    }

    private Result result;

    public Result getResult() {
        return result;
    }





    @Override
    protected void generate() {
        Ident ident = lVal.getIdent();
        List<Exp> exps = lVal.getExps();
        Optional<SymbolTable.VariableInfo> infoOptional = symbolTable.getVariable(ident);
        if(!infoOptional.isPresent()){
            Temp temp = valueFactory.newTemp();
            addInstrument(new Assignment(temp, new Constant(0)));
            result = new Result(temp, new IntType(), IdentType.Integer);
        }else{
            SymbolTable.VariableInfo info = infoOptional.get();
            VarType type = info.type;
            IdentType identType;
            if(type instanceof IntType) {
                identType = IdentType.Integer;
                assert exps.size() == 0;
                if(((IntType) type).getConstValue().isPresent()){
                    assert !changeable;
                    result = new Result(new Constant(((IntType) type).getConstValue().get()), new IntType(), identType);
                }else{
                    result = new Result(valueFactory.newVariable(ident), new IntType(), identType);
                }
            }else if(type instanceof ArrayType){
                check(exps.size() <= 2);
                check(!((ArrayType) type).getConstValue().isPresent() || !changeable);
                RValue offset;
                Exp offsetExp;
                VarType resultType;

                if(((ArrayType) type).getSecondLen().isPresent()){
                    int secondLen;
                    secondLen = ((ArrayType) type).getSecondLen().get();
                    Exp exp1 = new Number(0);
                    Exp exp2 = new Number(0);
                    resultType = new PointerType(secondLen);
                    if(exps.size() >= 1){
                        resultType = new PointerType();
                        exp1 = exps.get(0);
                    }
                    if(exps.size() == 2){
                        resultType = new IntType();
                        exp2 = exps.get(1);
                    }
                    offsetExp =  new BinaryExp(new BinaryExp(exp1, BinaryOp.MULT, new Number(secondLen)), BinaryOp.PLUS, exp2);
                }else{
                    check(exps.size() <= 1);
                    if(exps.size() == 0){
                        resultType = new PointerType();
                        offsetExp = new Number(0);
                    }else{
                        resultType = new IntType();
                        offsetExp = exps.get(0);
                    }
                }
                ExpGenerator offsetGenerator = new ExpGenerator(instruments, offsetExp);
                assert offsetGenerator.getResult().type instanceof IntType;
                offset = offsetGenerator.getResult().value;
                if(offset instanceof Constant && ((ArrayType) type).getConstValue().isPresent() && resultType instanceof IntType) {
                    int[] constValue = ((ArrayType) type).getConstValue().get();
                    result = new Result(new Constant(constValue[((Constant) offset).getNumber()]), resultType, IdentType.Integer);
                }else{
                    Temp temp = valueFactory.newTemp();
                    addInstrument(new BinaryOperation(valueFactory.getNewestVariable(ident), offset, BinaryOp.PLUS, temp));
                    result = new Result(temp, resultType, IdentType.Pointer);
                }
            }else{
                throw new SemanticException();
            }
        }
    }
}
