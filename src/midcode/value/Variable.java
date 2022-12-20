package midcode.value;

import midcode.Function;

import java.util.Optional;

/**
 * offset: 假设此时的栈顶为sp(sp上面是本函数的地盘)，则内存位置为sp+offset
 */
public class Variable extends LValue{

    public Variable(String name, Function function, int offset) {
        super(name);
        this.function = function;
        this.offset = offset;
    }

    private final int offset;
    private final Function function;

    public int getOffset() {
        return offset;
    }

    public Function getFunction(){
        return function;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)){
            Variable variable = (Variable) obj;
            return variable.function.equals(function);
        }else{
            return false;
        }
    }
}
