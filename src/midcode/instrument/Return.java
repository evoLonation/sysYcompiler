package midcode.instrument;

import midcode.value.RValue;

public class Return implements Jump{
    private RValue returnValue;

    public Return() {
    }

    public Return(RValue returnValue) {
        this.returnValue = returnValue;
    }

    public RValue getReturnValue() {
        return returnValue;
    }

    @Override
    public String print() {
        if(returnValue == null){
            return "return ";
        }else{
            return "return " + returnValue.print();
        }
    }
}
