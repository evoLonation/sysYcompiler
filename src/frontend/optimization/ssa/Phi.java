package frontend.optimization.ssa;

import midcode.value.RValue;
import midcode.value.Value;
import midcode.value.Variable;

import java.util.ArrayList;
import java.util.List;

public class Phi implements Value {
    private final List<Variable> parameters = new ArrayList<>();

    Phi() {}

    void addParameter(Variable variable){
        parameters.add(variable);
    }

    public List<Variable> getParameters() {
        return parameters;
    }

    @Override
    public String print() {
        StringBuilder stringBuilder = new StringBuilder();
        parameters.forEach(variable -> stringBuilder.append(variable.getName()).append(", "));
        return "phi ( " + stringBuilder.toString() + " )";
    }
}
