package midcode.value;

/**
 * 可以储存东西进去的
 */
public abstract class LValue implements RValue, Value {
    protected String name;
    public String getName() {
        return name;
    }
}
