package midcode.value;

/**
 * 对应着逻辑上的一个寄存器
 * 取值：
 */
public class Temp extends LValue {
    public enum valueType {
        // 该寄存器寸的是一个指针
        pointer,
        // 该寄存器存的是数字
        integer
    }
    private Temp() {
    }
}
