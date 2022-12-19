package midcode.instruction;

import midcode.value.Variable;


// 局部变量的隐式初始化
public class ImplicitDef implements Sequence{
    private Variable variable;

    public ImplicitDef(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String print() {
        return "def " + variable.print();
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}
