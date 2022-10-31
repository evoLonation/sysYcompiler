package midcode.value;

/**
 * Sys对指针的所有操作包括：
 * 声明数组的时候创建了一个指针；
 * 函数形参为指针；
 * 对指针做出一定的偏移；
 * 取出指针指向的地址的值；
 * 可以看出对于每个类型是指针（包括数组）的变量，其本身不会变化。
 * 类型的不同，最后取得指针的值的方式不同
 * 区别是数组变量通过使用栈指针和offset计算得到，指针变量是实参，从内存中取得。
 *
 */
public abstract class AddressValue implements Value {
    private final String name;
    private final RValue offset;

    // staticOffset是在编译器就计算好的，可以用来与 sp/静态base得到基地址；然后再生成与offset相加的指令
    private final int staticOffset;

    AddressValue(String name, int staticOffset, RValue offset) {
        this.name = name;
        this.offset = offset;
        this.staticOffset = staticOffset;
    }

    @Override
    public String print() {
        return "&" + name + " + " + offset.print();
    }

    public String getName() {
        return name;
    }

    public RValue getOffset() {
        return offset;
    }

    public int getStaticOffset() {
        return staticOffset;
    }
}
