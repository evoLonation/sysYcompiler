package backend;

import midcode.MidCode;
import midcode.instrument.*;
import midcode.value.*;
import util.Execution;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueGetter {
    // repeatable
    List<Value> getAllValues(MidCode midCode){
        return Stream.concat(getUseValues(midCode).stream(),
                getDefValue(midCode).map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    /**
     * 这里面包括了lvalue和addressValue，没有包括constant（因为一般constant不需要存到寄存器里）
      */
    List<Value> getUseValues(MidCode midCode){
        return useValueExecution.exec(midCode).stream()
                .filter(value -> !(value instanceof Constant))
                .collect(Collectors.toList());
    }
    Optional<LValue> getDefValue(MidCode midCode){
        return defValueExecution.exec(midCode);
    }

    private final Execution<MidCode, List<Value>> useValueExecution = new Execution<MidCode, List<Value>>() {
        @Override
        public void inject() {
            inject(param -> new ArrayList<>());
            inject(BinaryOperation.class , param -> Arrays.asList(param.getLeft(), param.getRight()));
            inject(UnaryOperation.class, param -> Collections.singletonList(param.getValue()));
            inject(Assignment.class, param -> Collections.singletonList(param.getRight()));
            inject(Return.class, param -> param.getReturnValue().map(r -> Collections.singletonList((Value)r)).orElse(new ArrayList<>()));
            inject(CondGoto.class, param -> Collections.singletonList(param.getCond()));
            inject(Call.class, param ->
               param.getParams().stream().flatMap(value -> {
                   if (value instanceof RValue) {
                       return Stream.of(value);
                   } else {
                       assert value instanceof AddressValue;
                       // address value 不仅自己是个value，它还包含了一个value
                       return getAddressValue((AddressValue) value);
                   }
               }).collect(Collectors.toList())
            );
            inject(Printf.class, param -> new ArrayList<>(param.getRValues()));
            inject(Store.class, param -> getAddressValue(param.getLeft()).collect(Collectors.toList()));
            inject(Load.class, param -> getAddressValue(param.getRight()).collect(Collectors.toList()));
        }
    };
    private  Stream<Value> getAddressValue(AddressValue addressValue){
        return Stream.of(addressValue, addressValue.getOffset());
    }
    private final Execution<MidCode, Optional<LValue>> defValueExecution = new Execution<MidCode, Optional<LValue>>() {
        @Override
        public void inject() {
            inject(param -> Optional.empty());
            inject(BinaryOperation.class , param -> Optional.of(param.getResult()));
            inject(UnaryOperation.class, param -> Optional.of(param.getResult()));
            inject(Assignment.class, param -> Optional.of(param.getLeft()));
            inject(Call.class, param -> param.getRet().map(value -> value));
            inject(GetInt.class, param -> Optional.of(param.getlValue()));
            inject(Load.class, param -> Optional.of(param.getLeft()));
        }
    };

    private ValueGetter() {
        useValueExecution.inject();
        defValueExecution.inject();
    }
    static private final ValueGetter instance = new ValueGetter();
    static public ValueGetter getInstance(){
        return instance;
    }
}
