package frontend.error;

import frontend.type.VarType;

import java.util.TreeSet;

public class ErrorRecorder {
    private final TreeSet<Error> errorSet = new TreeSet<>();

    public TreeSet<Error> getErrorSet() {
        return errorSet;
    }

    public void illegalChar(int lineno, char c){
        Error error = new Error(lineno, 'a', "illegal char in format string: " + c);
        errorSet.add(error);
    }
    public void redefined(int lineno, String name){
        Error error = new Error(lineno, 'b', "name " + name + " redefined");
        errorSet.add(error);
    }
    public void undefined(int lineno, String name){
        Error error = new Error(lineno, 'c', "name " + name + " undefined");
        errorSet.add(error);
    }
    public void paramNumNotMatch(int lineno, String funcName, int need, int real){
        Error error = new Error(lineno, 'd', "function " + funcName + "parameter number not match, need " + need+ " , give " + real );
        errorSet.add(error);
    }
    public void paramTypeNotMatch(int lineno, String funcName, VarType needType, VarType realType){
        Error error = new Error(lineno, 'e', "function " + funcName + "parameter frontend.type not match, need " + needType + " , give " + realType );
        errorSet.add(error);
    }
    public void paramTypeNotMatch(int lineno, String funcName, VarType needType){
        Error error = new Error(lineno, 'e', "function " + funcName + "parameter frontend.type not match, need " + needType + " , give void" );
        errorSet.add(error);
    }
    public void voidFuncReturnValue(int lineno) {
        Error error = new Error(lineno, 'f', "function has no return but the return statement has value");
        errorSet.add(error);
    }
    public void returnLack(int lineno) {
        Error error = new Error(lineno, 'g', "function has no return statement");
        errorSet.add(error);
    }
    public void changeConst(int lineno, String name){
        Error error = new Error(lineno, 'h', "symbol "+name+" is constant, can't change its value");
        errorSet.add(error);
    }
    public void semicolonLack(int lineno){
        Error error = new Error(lineno, 'i', "lack of semicolon");
        errorSet.add(error);
    }
    public void rParentLack(int lineno) {
        Error error = new Error(lineno, 'j', "lack of right parent");
        errorSet.add(error);
    }
    public void rBracketLack(int lineno) {
        Error error = new Error(lineno, 'k', "lack of right bracket");
        errorSet.add(error);
    }
    public void printfParamNotMatch(int lineno, int need, int real){
        Error error = new Error(lineno, 'l', "function printf parameter number not match, need " + need+ " , give " + real );
        errorSet.add(error);
    }
    public void wrongBreak(int lineno){
        Error error = new Error(lineno, 'm', "break statement should not exist in here");
        errorSet.add(error);
    }
    public void wrongContinue(int lineno){
        Error error = new Error(lineno, 'm', "continue statement should not exist in here");
        errorSet.add(error);
    }
    public void other(int lineno){
        Error error = new Error(lineno, 'o', "other frontend.error");
        errorSet.add(error);
    }

    private ErrorRecorder(){}
    static private final ErrorRecorder instance = new ErrorRecorder();
    static public ErrorRecorder getInstance(){
        return instance;
    }

}
