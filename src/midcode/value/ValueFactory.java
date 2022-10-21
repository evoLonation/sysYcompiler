package midcode.value;

import lexer.Ident;

public class ValueFactory {
    public Temp newTemp(){
        throw new UnsupportedOperationException();
    }

    /**
     * 指针类型变量并不会被改变，因此不用重新赋值（非SSA形式）
     */
    public PointerValue newPointer(Ident ident, RValue offset){
        throw new UnsupportedOperationException();
    }

    public Variable newVariable(Ident ident) {
        throw new UnsupportedOperationException();
    }

    public Variable getNewestVariable(Ident ident){
        throw new UnsupportedOperationException();
    }

    static private final ValueFactory instance = new ValueFactory();
    static public ValueFactory getInstance(){
        return instance;
    }
}
