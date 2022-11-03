package backend;

import java.util.*;

public class MipsSegment {
    private final boolean COMMENT = true;

    private String textContent = "\n.text\n";
    private String dataContent = ".data\n";

    private void addDataLine(String instrument){
        dataContent += "\t" + instrument + "\n";
    }
    private void addTextLine(String instrument){
        if(isInBackFill()){
            backFillQueue.add(instrument);
            needFillQueue.add(false);
        }else{
            textContent += "\t" + instrument + "\n";
        }
    }

    public String print(){
        return dataContent + textContent;
    }

    void newSegment(String labelName) {
        textContent += "\n" + labelName + " :\n";
    }

    void comment(String info){
        if(COMMENT){
            addTextLine("# " + info);
        }
    }

    void string(String label, String string){
        addDataLine(String.format("%s: .ascii \"%s\"", label, string));
        addDataLine(" .word 0");
    }

    void word(String label, int[] data){
        StringBuilder dataStr = new StringBuilder();
        for(int num : data){
            dataStr.append(num).append(", ");
        }
        addDataLine(String.format("%s: .word %s", label, dataStr));
    }

    void add(Register register1, Register register2, Register register3){
        addTextLine(String.format("add %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void addi(Register register1, Register register2, int number){
        addTextLine(String.format("addi %s, %s, %d", register1.print(), register2.print(), number));
    }

    void move(Register register1, Register register2){
        addTextLine(String.format("move %s, %s", register1.print(), register2.print()));
    }

    void sub(Register register1, Register register2, Register register3){
        addTextLine(String.format("sub %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void mul(Register register1, Register register2, Register register3){
        addTextLine(String.format("mul %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void div(Register register1, Register register2, Register register3){
        addTextLine(String.format("div %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void mod(Register register1, Register register2, Register register3){
        addTextLine(String.format("div %s, %s", register2.print(), register3.print()));
        addTextLine(String.format("mfhi %s", register1.print()));
    }

    // 1 = 2 >= 3 ? 1 : 0
    void sge(Register register1, Register register2, Register register3){
        addTextLine(String.format("sge %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void sgt(Register register1, Register register2, Register register3){
        addTextLine(String.format("sgt %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void sle(Register register1, Register register2, Register register3){
        addTextLine(String.format("sle %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void slt(Register register1, Register register2, Register register3){
        addTextLine(String.format("slt %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void seq(Register register1, Register register2, Register register3){
        addTextLine(String.format("seq %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }
    void sne(Register register1, Register register2, Register register3){
        addTextLine(String.format("sne %s, %s, %s", register1.print(), register2.print(), register3.print()));
    }

    void lw(Register register, Register address, int offset){
        addTextLine(String.format("lw %s, %d(%s)", register.print(), offset, address.print()));
    }

    void lw(Register register, int address){
        addTextLine(String.format("lw %s, %d", register.print(), address));
    }

    void sw(Register register, Register address, int offset) {
        addTextLine(String.format("sw %s, %d(%s)", register.print(), offset, address.print()));
    }

    void sw(Register register, int address) {
        addTextLine(String.format("sw %s, %d", register.print(), address));
    }

    void li(Register register, int number){
        addTextLine(String.format("li %s, %d", register.print(), number));
    }
    void la(Register register, String label){
        addTextLine(String.format("la %s, %s", register.print(), label));
    }

    void sll(Register register1, Register register2, int number){
        addTextLine(String.format("sll %s, %s, %d", register1.print(), register2.print(), number));
    }

    void jal(String label){
        addTextLine(String.format("jal %s", label));
    }

    void j(String label){
        addTextLine(String.format("j %s", label));
    }

    void jr(Register register){
        addTextLine(String.format("jr %s", register.print()));
    }

    void bne(Register register1, Register register2, String label){
        addTextLine(String.format("bne %s, %s, %s", register1.print(), register2.print(), label));
    }

    void syscall(){
        addTextLine("syscall");
    }

    private final Deque<String> backFillQueue = new LinkedList<>();
    private final Deque<Boolean> needFillQueue = new LinkedList<>();
    private int backFillNumber = 0;

    // offset wait to back fill
    void swBackFill(Register register, Register address){
        backFillNumber ++;
        backFillQueue.add(String.format("sw %s, ", register.print()) + "%d" + String.format("(%s)", address.print()));
        needFillQueue.add(true);
    }

    void backFill(int offset){
        while(!backFillQueue.isEmpty()){
            if(needFillQueue.pop()){
                textContent += "\t" + String.format(backFillQueue.pop(), offset) + "\n";
                backFillNumber --;
                break;
            }
            textContent += "\t" + backFillQueue.pop() + "\n";
        }
        if(backFillNumber == 0){
            while(!backFillQueue.isEmpty()){
                needFillQueue.pop();
                textContent += "\t" + backFillQueue.pop() + "\n";
            }
        }
    }

    boolean isInBackFill(){
        return backFillNumber != 0;
    }


    private MipsSegment() {}
    static private final MipsSegment instance = new MipsSegment();
    static public MipsSegment getInstance(){
        return instance;
    }
}
