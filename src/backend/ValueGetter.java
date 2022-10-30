package backend;

import midcode.MidCode;
import midcode.instrument.*;
import midcode.value.LValue;
import midcode.value.PointerValue;
import midcode.value.RValue;
import util.Execution;

import java.util.*;
import java.util.stream.Collectors;

public class ValueGetter {
    // repeatable
    List<RValue> getAllValues(MidCode midCode){
        List<RValue> ret = new ArrayList<>(useValueExecution.exec(midCode));
        ret.addAll(defValueExecution.exec(midCode).map(Collections::singletonList).orElse(new ArrayList<>()));
        return ret;
    }
    List<RValue> getUseValues(MidCode midCode){
        return useValueExecution.exec(midCode);
    }
    Optional<LValue> getDefValue(MidCode midCode){
        return defValueExecution.exec(midCode);
    }

    private final Execution<MidCode, List<RValue>> useValueExecution = new Execution<MidCode, List<RValue>>() {
        @Override
        public void inject() {
            inject(param -> new ArrayList<>());
            inject(BinaryOperation.class , param -> Arrays.asList(param.getLeft(), param.getRight()));
            inject(UnaryOperation.class, param -> Collections.singletonList(param.getValue()));
            inject(Assignment.class, param -> Collections.singletonList(param.getRight()));
            inject(Return.class, param -> param.getReturnValue().map(Collections::singletonList).orElse(new ArrayList<>()));
            inject(CondGoto.class, param -> Collections.singletonList(param.getCond()));
            inject(Call.class, param ->
               param.getParams().stream().map(value -> {
                   if (value instanceof RValue) {
                       return (RValue) value;
                   } else {
                       assert value instanceof PointerValue;
                       return ((PointerValue) value).getOffset();
                   }
               }).collect(Collectors.toList())
            );
        }
    };
    private final Execution<MidCode, Optional<LValue>> defValueExecution = new Execution<MidCode, Optional<LValue>>() {
        @Override
        public void inject() {
            inject(param -> Optional.empty());
            inject(BinaryOperation.class , param -> Optional.of(param.getResult()));
            inject(UnaryOperation.class, param -> Optional.of(param.getResult()));
            inject(Assignment.class, param -> Optional.of(param.getLeft()));
            inject(Call.class, param -> param.getRet().map(value -> (LValue) value));
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
