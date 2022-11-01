package backend;

public class MipsSegment {
    private final boolean COMMENT = true;

    private String content;

    private void addLine(String instrument){
        content += "\t" + instrument + "\n";
    }

    public String print(){
        return content;
    }

    public MipsSegment(String labelName){
        content = labelName + " :\n";
    }

    void comment(String info){
        if(COMMENT){
            addLine("# " + info);
        }
    }

    void add(Register register1, Register register2, Register register3){
        addLine(String.format("add %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void addi(Register register1, Register register2, int number){
        addLine(String.format("addi %s, %s, %d", register1.print(), register2.print(), number));
    }
    void sub(Register register1, Register register2, Register register3){
        addLine(String.format("sub %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void lw(Register register, Register address, int offset){
        addLine(String.format("lw %s, %s, %d", register.print(), address.print(), offset));
    }

    void lw(Register register, int address){
        addLine(String.format("lw %s, %d", register.print(), address));
    }

    void sw(Register register, Register address, int offset) {
        addLine(String.format("sw %s, %s, %d", register.print(), address.print(), offset));
    }

    void sw(Register register, int address) {
        addLine(String.format("sw %s, %d", register.print(), address));
    }

    void li(Register register, int number){
        addLine(String.format("lw %s, %d", register.print(), number));
    }
    void sll(Register register1, Register register2, int number){
        addLine(String.format("sll %s, %s, %d", register1.print(), register2.print(), number));
    }
    void jal(String label){
        addLine(String.format("jal %s", label));
    }

    void j(String label){
        addLine(String.format("j %s", label));
    }
    void jr(Register register){
        addLine(String.format("jr %s", register.print()));
    }

    void bne(Register register1, Register register2, String label){
        addLine(String.format("bne %s, %s, %s", register1.print(), register2.print(), label));
    }

}
