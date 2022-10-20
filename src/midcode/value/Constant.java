package midcode.value;

public class Constant implements RValue{
    private final int number;

    public Constant(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
