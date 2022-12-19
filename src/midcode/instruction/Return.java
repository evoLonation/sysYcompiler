package midcode.instruction;

import midcode.value.RValue;

import java.util.Optional;

public class Return implements Jump{
    private RValue returnValue;

    public Return() {}

    public Return(RValue returnValue) {
        this.returnValue = returnValue;
    }

    public Optional<RValue> getReturnValue() {
        return Optional.ofNullable(returnValue);
    }

    public void setReturnValue(RValue returnValue) {
        this.returnValue = returnValue;
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
