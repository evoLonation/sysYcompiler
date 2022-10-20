package midcode.value;

public class Phi implements RValue {
    private Variable variable1;
    private Variable variable2;

    public Phi(Variable variable1, Variable variable2) {
        this.variable1 = variable1;
        this.variable2 = variable2;
    }
}
