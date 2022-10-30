package backend;

import midcode.instrument.Assignment;
import midcode.instrument.BinaryOperation;
import midcode.instrument.Instrument;
import midcode.instrument.UnaryOperation;
import midcode.value.LValue;
import midcode.value.RValue;
import midcode.value.Value;
import util.Execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ValueGetter {
    // repeatable
    List<RValue> getAllValues(Instrument instrument){
        return allValueExecution.exec(instrument);
    }
    List<RValue> getUseValues(Instrument instrument){
        return useValueExecution.exec(instrument);
    }
    Optional<LValue> getDefValue(Instrument instrument){
        return defValueExecution.exec(instrument);
    }

    private final Execution<Instrument, List<RValue>> allValueExecution = new Execution<Instrument, List<RValue>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class , param -> Arrays.asList(param.getLeft(), param.getRight(), param.getResult()));
            inject(UnaryOperation.class, param -> Arrays.asList(param.getValue(), param.getResult()));
            inject(Assignment.class, param -> Arrays.asList(param.getLeft(), param.getRight()));
        }
    };
    private final Execution<Instrument, List<RValue>> useValueExecution = new Execution<Instrument, List<RValue>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class , param -> Arrays.asList(param.getLeft(), param.getRight()));
            inject(UnaryOperation.class, param -> Collections.singletonList(param.getValue()));
            inject(Assignment.class, param -> Collections.singletonList(param.getRight()));
        }
    };
    private final Execution<Instrument, Optional<LValue>> defValueExecution = new Execution<Instrument, Optional<LValue>>() {
        @Override
        public void inject() {
            inject(BinaryOperation.class , param -> Optional.of(param.getResult()));
            inject(UnaryOperation.class, param -> Optional.of(param.getResult()));
            inject(Assignment.class, param -> Optional.of(param.getLeft()));
        }
    };


    private ValueGetter() {
        allValueExecution.inject();
        useValueExecution.inject();
        defValueExecution.inject();
    }
    static private final ValueGetter instance = new ValueGetter();
    static public ValueGetter getInstance(){
        return instance;
    }
}
