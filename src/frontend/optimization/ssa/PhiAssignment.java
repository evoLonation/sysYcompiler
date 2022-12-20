package frontend.optimization.ssa;

import midcode.BasicBlock;
import midcode.instruction.Sequence;
import midcode.value.Variable;

public class PhiAssignment implements Sequence {
    private Variable left;
    private final Phi phi;
    private final BasicBlock basicBlock;

    public PhiAssignment(Variable left, BasicBlock basicBlock) {
        this.phi = new Phi();
        this.left = left;
        this.basicBlock = basicBlock;
    }

    public void setLeft(Variable left) {
        this.left = left;
    }

    public Variable getLeft() {
        return left;
    }

    public Phi getPhi() {
        return phi;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PhiAssignment)) {
            return false;
        } else {
            return left.equals(((PhiAssignment) obj).left) && basicBlock.equals(((PhiAssignment) obj).basicBlock);
        }
    }

    @Override
    public int hashCode() {
        return left.hashCode() + basicBlock.hashCode();
    }

    @Override
    public String print() {
        return left.print() + " = " + phi.print();
    }
}
