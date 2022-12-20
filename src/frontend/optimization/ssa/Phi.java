package frontend.optimization.ssa;

import midcode.BasicBlock;
import midcode.value.RValue;
import midcode.value.Value;
import midcode.value.Variable;

import java.util.*;

public class Phi implements Value {
    private final Map<Variable, BasicBlock> parametersMap = new LinkedHashMap<>();

    Phi() {}

    void addParameter(Variable variable, BasicBlock basicBlock){
        parametersMap.put(variable, basicBlock);
    }

    public Map<Variable, BasicBlock> getParametersMap() {
        return parametersMap;
    }

    @Override
    public String print() {
        StringBuilder stringBuilder = new StringBuilder();
        parametersMap.forEach((variable, v) -> stringBuilder.append(variable.getName()).append(", "));
        return "phi ( " + stringBuilder.toString() + " )";
    }
}
