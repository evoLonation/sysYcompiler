package semantic;

import lexer.Ident;
import midcode.instrument.*;
import midcode.value.*;
import parser.nonterminal.exp.*;
import type.ArrayType;
import type.FuncType;
import type.IntType;
import type.VarType;

import java.util.List;
import java.util.Optional;

public class FuncCallGenerator extends Generator{
    private final FuncCall funcCall;

    public FuncCallGenerator(FuncCall funcCall) {
        this.funcCall = funcCall;
    }

    private LValue result = null;

    public Optional<LValue> getResult() {
        return Optional.ofNullable(result);
    }


    @Override
    protected FuncCallGenerator generate() {
        Ident ident = funcCall.getIdent();
        List<Exp> exps = funcCall.getExps();
        Optional<FuncType> typeOptional = symbolTable.getFuncSymbol(ident);
        if(!typeOptional.isPresent()){
            Temp temp = valueFactory.newTemp();
            addMidCode(new Assignment(temp, new Constant(0)));
            result = temp;
        }else{
            FuncType type = typeOptional.get();
            check(type.getParamNumber() == exps.size());
            // 每个exp类型是数组当且仅当其为BinaryExp且exp1为数组的Lval,或者其本身为LVal
            for(int i = 0; i < exps.size(); i++){
                Exp exp = exps.get(i);
                VarType paramType = type.getParams().get(i);
                if(exp instanceof LVal){
                    LVal lVal = (LVal) exp;
                    LValGenerator lValGenerator = new LValGenerator(lVal).generate();
                    addMidCode(lValGenerator.getMidCodes());
                    switch (lValGenerator.getResultType()){
                        case Array1:{
                            LValGenerator.ArrayResult arrayResult = lValGenerator.getArray01();
                            check(paramType instanceof ArrayType && !((ArrayType) paramType).isArray2());
                            addMidCode(new ParamArray(arrayResult.arrayVariable, arrayResult.offset));
                            return this;
                        }
                        case Array2:{
                            LValGenerator.Array2Result arrayResult = lValGenerator.getArray2();
                            check(paramType instanceof ArrayType &&
                                    ((ArrayType) paramType).getSecondLen().isPresent() &&
                                    arrayResult.secondLen == ((ArrayType) paramType).getSecondLen().get());
                            addMidCode(new ParamArray(arrayResult.arrayVariable, arrayResult.offset));
                            return this;
                        }
                    }
                }else if(exp instanceof BinaryExp && ((BinaryExp) exp).getExp1() instanceof LVal){
                    LVal lVal = (LVal) ((BinaryExp) exp).getExp1();
                    BinaryExp binaryExp = (BinaryExp) exp;
                    LValGenerator lValGenerator = new LValGenerator(lVal).generate();
                    addMidCode(lValGenerator.getMidCodes());
                    switch (lValGenerator.getResultType()){
                        case Array1: {
                            LValGenerator.ArrayResult arrayResult = lValGenerator.getArray01();
                            check(binaryExp.getOp() == BinaryOp.PLUS);
                            check(paramType instanceof ArrayType && !((ArrayType) paramType).isArray2());
                            AddExpGenerator expGenerator = new AddExpGenerator(binaryExp.getExp2()).generate();
                            addMidCode(expGenerator.midCodes);
                            Temp offset = valueFactory.newTemp();
                            addMidCode(new BinaryOperation(arrayResult.offset, expGenerator.getResult(), BinaryOp.PLUS, offset));
                            addMidCode(new ParamArray(arrayResult.arrayVariable, offset));
                            return this;
                        }
                        case Array2: {
                            LValGenerator.Array2Result arrayResult = lValGenerator.getArray2();
                            check(binaryExp.getOp() == BinaryOp.PLUS);
                            check(paramType instanceof ArrayType &&
                                    ((ArrayType) paramType).getSecondLen().isPresent() &&
                                    arrayResult.secondLen == ((ArrayType) paramType).getSecondLen().get());
                            AddExpGenerator expGenerator = new AddExpGenerator(binaryExp.getExp2()).generate();
                            addMidCode(expGenerator.midCodes);
                            Temp offset = valueFactory.newTemp();
                            addMidCode(new BinaryOperation(arrayResult.offset, expGenerator.getResult(), BinaryOp.PLUS, offset));
                            addMidCode(new ParamArray(arrayResult.arrayVariable, offset));
                            return this;
                        }
                    }
                }
                // 现在开始确定exp是int
                AddExpGenerator expGenerator = new AddExpGenerator(exp).generate();
                addMidCode(expGenerator.getMidCodes());
                check(paramType instanceof IntType);
                addMidCode(new Param(expGenerator.getResult()));
            }
            if(type.isReturn()){
                Temp ret = valueFactory.newTemp();
                addMidCode(new Call(ident.getValue(), exps.size(), ret));
                result = ret;
            }else{
                addMidCode(new Call(ident.getValue(), exps.size()));
            }
        }
        return this;
    }
}
