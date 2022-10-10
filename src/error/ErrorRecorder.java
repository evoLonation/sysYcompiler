package error;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ErrorRecorder {
    private TreeSet<Error> errorSet = new TreeSet<>();

    public TreeSet<Error> getErrorSet() {
        return errorSet;
    }

    public Error illegalChar(int lineno, char c){
        Error error = new Error(lineno, 'a', "illegal char in format string: " + c);
        errorSet.add(error);
        return error;
    }
    public Error redefined(int lineno, String name){
        Error error = new Error(lineno, 'b', "name " + name + " redefined");
        errorSet.add(error);
        return error;
    }
    public Error undefined(int lineno, String name){
        Error error = new Error(lineno, 'c', "name " + name + " undefined");
        errorSet.add(error);
        return error;
    }
    public Error paramNumNotMatch(int lineno, String funcName, int need, int real){
        Error error = new Error(lineno, 'd', "function " + funcName + "parameter number not match, need " + need+ " , give " + real );
        errorSet.add(error);
        return error;
    }
    public Error paramTypeNotMatch(int lineno, String funcName, int needDimension, int realDimension){
        String need = needDimension == 0 ? "int" : needDimension + " dimension array";
        String real = realDimension == 0 ? "int" : realDimension + " dimension array";
        Error error = new Error(lineno, 'e', "function " + funcName + "parameter type not match, need " + need+ " , give " + real );
        errorSet.add(error);
        return error;
    }
    public Error voidFuncReturnValue(int lineno) {
        Error error = new Error(lineno, 'f', "function has no return but the return statement has value");
        errorSet.add(error);
        return error;
    }
    public Error returnLack(int lineno) {
        Error error = new Error(lineno, 'g', "function has no return statement");
        errorSet.add(error);
        return error;
    }
    public Error changeConst(int lineno, String name){
        Error error = new Error(lineno, 'h', "symbol "+name+" is constant, can't change its value");
        errorSet.add(error);
        return error;
    }
    public Error semicolonLack(int lineno){
        Error error = new Error(lineno, 'i', "lack of semicolon");
        errorSet.add(error);
        return error;
    }
    public Error rParentLack(int lineno) {
        Error error = new Error(lineno, 'j', "lack of right parent");
        errorSet.add(error);
        return error;
    }
    public Error rBracketLack(int lineno) {
        Error error = new Error(lineno, 'k', "lack of right bracket");
        errorSet.add(error);
        return error;
    }
    public Error printfParamNotMatch(int lineno, int need, int real){
        Error error = new Error(lineno, 'l', "function printf parameter number not match, need " + need+ " , give " + real );
        errorSet.add(error);
        return error;
    }
    public Error wrongBreak(int lineno){
        Error error = new Error(lineno, 'm', "break statement should not exist in here");
        errorSet.add(error);
        return error;
    }
    public Error wrongContinue(int lineno){
        Error error = new Error(lineno, 'm', "continue statement should not exist in here");
        errorSet.add(error);
        return error;
    }
    public Error other(int lineno){
        Error error = new Error(lineno, 'o', "other error");
        errorSet.add(error);
        return error;
    }


}
