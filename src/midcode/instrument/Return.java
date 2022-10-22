package midcode.instrument;

import midcode.value.RValue;

import java.util.Optional;

public class Return implements Jump{
    private RValue returnValue;

    public Return() {
    }

    public Return(RValue returnValue) {
        this.returnValue = returnValue;
    }

    public Optional<RValue> getReturnValue() {
        return Optional.ofNullable(returnValue);
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
