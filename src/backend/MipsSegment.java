package backend;

public class MipsSegment {

    private String content;

    private void addInstrument(String instrument){
        content += "\t" + instrument + "\n";
    }

    void debug(String info){
        if(Generator.DEBUG){
            addInstrument(info);
        }
    }

    public String print(){
        return content;
    }

    public MipsSegment(String labelName){
        content = labelName + " :\n";
    }

    void add(Register register1, Register register2, Register register3){
        addInstrument(String.format("add %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void addi(Register register1, Register register2, int number){
        addInstrument(String.format("addi %s, %s, %d", register1.print(), register2.print(), number));
    }
    void sub(Register register1, Register register2, Register register3){
        addInstrument(String.format("sub %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void lw(Register register, Register address, int offset){
        addInstrument(String.format("lw %s, %s, %d", register.print(), address.print(), offset));
    }

    void lw(Register register, int address){
        addInstrument(String.format("lw %s, %d", register.print(), address));
    }

    void sw(Register register, Register address, int offset) {
        addInstrument(String.format("sw %s, %s, %d", register.print(), address.print(), offset));
    }

    void sw(Register register, int address) {
        addInstrument(String.format("sw %s, %d", register.print(), address));
    }

    void li(Register register, int number){
        addInstrument(String.format("lw %s, %d", register.print(), number));
    }
    void sll(Register register1, Register register2, int number){
        addInstrument(String.format("sll %s, %s, %d", register1.print(), register2.print(), number));
    }
    void jal(String label){
        addInstrument(String.format("jal %s", label));
    }

    void j(String label){
        addInstrument(String.format("j %s", label));
    }
    void jr(Register register){
        addInstrument(String.format("jr %s", register.print()));
    }

    void bne(Register register1, Register register2, String label){
        addInstrument(String.format("bne %s, %s, %s", register1.print(), register2.print(), label));
    }

}
