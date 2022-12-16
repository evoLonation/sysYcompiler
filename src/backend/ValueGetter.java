package backend;

import midcode.MidCode;
import midcode.instruction.*;
import midcode.value.*;
import util.Execution;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueGetter {
    // repeatable
    List<LValue> getAllValues(MidCode midCode){
        return Stream.concat(getUseValues(midCode).stream(),
                getDefValue(midCode).map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    /**
     * 这里面包括了lvalue和addressValue，没有包括constant（因为一般constant不需要存到寄存器里）
      */
    List<LValue> getUseValues(MidCode midCode){
//        return new ArrayList<>(useValueExecution.exec(midCode));
        return useValueExecution.exec(midCode).stream()
                .map(value -> value instanceof AddressValue ? ((AddressValue) value).getOffset() : value)
                .filter(value -> value instanceof LValue).map(value -> (LValue) value)
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
            inject(Return.class, param -> param.getReturnValue().map(r -> Collections.singletonList((Value)r)).orElse(Collections.emptyList()));
            inject(CondGoto.class, param -> Collections.singletonList(param.getCond()));
            inject(Param.class, param -> Collections.singletonList(param.getValue()));
            inject(PrintInt.class, param -> Collections.singletonList(param.getRValue()));
            inject(Store.class, param -> Arrays.asList(param.getRight(), param.getLeft()));
            inject(Load.class, param -> Collections.singletonList(param.getRight()));
        }
    };

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
