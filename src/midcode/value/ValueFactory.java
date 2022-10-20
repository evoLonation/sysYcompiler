package midcode.value;

import lexer.Ident;

public class ValueFactory {
    public Temp newTemp(){
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
