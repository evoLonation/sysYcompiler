package frontend.semantic;

import common.SemanticException;
import frontend.lexer.Ident;
import midcode.instrument.Assignment;
import midcode.value.*;
import frontend.parser.nonterminal.exp.BinaryExp;
import frontend.parser.nonterminal.exp.BinaryOp;
import frontend.parser.nonterminal.exp.Exp;
import frontend.parser.nonterminal.exp.LVal;
import frontend.parser.nonterminal.exp.Number;
import frontend.type.*;

import java.util.List;
import java.util.Optional;

/**
 * LVal的用法：
 * 1、在exp中，且要作为调用函数的参数。这时LVal不能是指向常量数组的，类型为指针的PointerType。
 * 2、在exp中，用于赋值等等。这时得到的Type必须是IntType。
 * 3、在assign中的左边，用于存值。这时得到的Type必须是IntType，且Ident的Type不能是Const。
 * 当Value是PointerValue时，类型可能是IntType，也可能是PointerType
 * 当Value是RValue时，类型一定是IntType
 *
 * 当ident是int变量时，返回Variable
 * 当ident是int常量时，返回Constant
 * 当ident时数组时，
 * 如果是常量数组：
 *      当最后的type是指针时，报错
 *      当最后的type是int
 *          如果exps是常量，返回Constant
 *          如果exps不是常量，返回PointerValue
 * 如果是非常量数组：
 *      当最后的type是指针时，返回ident和offset和type
 *      当最后的type是int，返回PointerValue
 *
 *  可以总结如下4种可能性：
 *  1、variable，用于赋值或者取值
 *  2、constant，用于取值
 *  3、pointerValue，用于赋值或者取值（int）
 *  4、ident，offset，pointerType，用于进一步计算得到最终的pointerValue
 */
public class LValGenerator extends InstrumentGenerator {
    private final LVal lVal;

    LValGenerator(LVal lVal) {
        this.lVal = lVal;
    }


    public static class LValueResult extends Result{
        LValueResult(LValue lVal) {
            this.lVal = lVal;
        }

        public LValue lVal;
    }
    public static class ConstantResult extends Result{
        ConstantResult(Constant constant) {
            this.constant = constant;
        }

        public Constant constant;
    }
    public static class IntPointerResult extends Result{
        IntPointerResult(PointerValue pointerValue) {
            this.pointerValue = pointerValue;
        }

        public PointerValue pointerValue;
    }
    public static class ArrayPointerResult extends Result{
        public PointerType pointerType;
        public Ident ident;
        public Exp offsetExp;

        ArrayPointerResult(PointerType pointerType, Ident ident, Exp offsetExp) {
            this.pointerType = pointerType;
            this.ident = ident;
            this.offsetExp = offsetExp;
        }
    }

    public static abstract class Result {
    }


    Result generate() {
        Result result;
        Ident ident = lVal.getIdent();
        List<Exp> exps = lVal.getExps();
        Optional<SymbolTable.VariableInfo> infoOptional = symbolTable.getVariable(ident);
        if(!infoOptional.isPresent()) {
            Temp temp = valueFactory.newTemp();
            addInstrument(new Assignment(temp, new Constant(0)));
            result = new LValueResult(temp);
        }else{
            SymbolTable.VariableInfo info = infoOptional.get();
            VarType type = info.getType();
            if(type instanceof IntType) {
                assert exps.size() == 0;
                if(info.getConstInteger().isPresent()){
                    result = new ConstantResult(new Constant(info.getConstInteger().get()));
                }else {
                    result = new LValueResult(valueFactory.newVariable(ident));
                }
            }else if(type instanceof PointerType){
                assert exps.size() <= 2;
                Exp offsetExp;
                VarType resultType = null;
                if(((PointerType) type).getSecondLen().isPresent()){
                    int secondLen;
                    secondLen = ((PointerType) type).getSecondLen().get();
                    switch (exps.size()){
                        case 0 : {
                            resultType = new PointerType(secondLen);
                            offsetExp = new Number(0);
                            break;
                        }
                        case 1 : {
                            resultType = new PointerType();
                            offsetExp =  new BinaryExp(exps.get(0), BinaryOp.MULT, new Number(secondLen));
                            break;
                        }
                        case 2 : {
                            resultType = new IntType();
                            offsetExp =  new BinaryExp(new BinaryExp(exps.get(0), BinaryOp.MULT, new Number(secondLen)), BinaryOp.PLUS, exps.get(1));
                            break;
                        }
                        default:throw new SemanticException();
                    }
                }else{
                    assert exps.size() <= 1;
                    if(exps.size() == 0){
                        resultType = new PointerType();
                        offsetExp = new Number(0);
                    }else{
                        resultType = new IntType();
                        offsetExp = exps.get(0);
                    }
                }

                /* 可能有三种情况：
                 * 1、type是int，都是常量
                 * 2、type是int，但是有一个不是常量
                 * 3、type不是int，这时数组必须不是常量
                 */
                if(resultType instanceof IntType) {
                    RValue offset = new ExpGenerator(offsetExp).generate().getRValueResult();
                    if(offset instanceof Constant && info.getConstArray().isPresent() ){
                        int[] constValue = info.getConstArray().get();
                        result = new ConstantResult(new Constant(constValue[((Constant) offset).getNumber()]));
                    }else {
                        result = new IntPointerResult(valueFactory.newPointer(ident, offset));
                    }
                } else {
                    assert !info.getConstArray().isPresent();
                    result = new ArrayPointerResult((PointerType) resultType, ident, offsetExp);
                }
            }else{
                throw new SemanticException();
            }
        }
        return result;
    }
}
