package midcode.value;

/**
 * 代表着一个已定义符号的左值（也就意味着它在逻辑上占用至少一个内存）
 * 取值：
 * 对pointer来说，取得是对应函数入参指针的值
 * 对array来说，取得是数组第一个元素的地址值
 * 对int来说，取得是数字值
 * 存值：
 * 存在对应内存位置中即可
 *
 * offset: 假设此时的栈顶为sp(sp上面是本函数的地盘)，则内存位置为sp+offset
 */
public class Variable extends LValue{
    public enum variableType {
        // 该单位的内存存的是一个数组的第一个元素
        array,
        // 该单位的内存存的是一个指向数组的指针
        pointer,
        // 该单位的内存存的是一个数字
        integer
    }
    private boolean isGlobal;
    private int offset;
}
