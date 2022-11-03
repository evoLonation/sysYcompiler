package backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// todo 分类（通用、传参、返回值等等）
public class Register {
    private final int no;

    private Register(int no) {
        this.no = no;
    }
    public int getNo() {
        return no;
    }
    String print(){
        switch (no){
            case 29 : return "$sp";
            case 31 : return "$ra";
            case 2 : return "$v0";
            case 4 : return "$a0";
            default: return "$" + no;
        }
    }
    static private final Map<Integer, Register> registers = new HashMap<>();

    static {
        for(int i : IntStream.range(0, 32).toArray()){
            registers.put(i, new Register(i));
        }
    }


    static Set<Register> getLocalRegister(){
        return registers.values().stream().filter(r -> r.no >= 8 && r.no <= 15).collect(Collectors.toSet());
    }
    // 用于存储栈指针
    static Register getSp(){
        return registers.get(29);
    }
    // 用于存储返回地址
    static Register getRa(){
        return registers.get(31);
    }
    // 用于存储返回值
    static Register getV0(){
        return registers.get(2);
    }
    static Register getZero(){
        return registers.get(0);
    }
    static Register getA0(){return registers.get(4);}
}
