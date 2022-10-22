package midcode.value;

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
}
