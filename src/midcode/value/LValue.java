package midcode.value;

import java.util.Objects;

/**
 * 可以储存东西进去的
 */
public abstract class LValue implements RValue {
    LValue(String name) {
        this.name = name;
    }

    protected String name;
    public String getName() {
        return name;
    }

    @Override
    public String print() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LValue){
            return name.equals(((LValue) obj).name);
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
