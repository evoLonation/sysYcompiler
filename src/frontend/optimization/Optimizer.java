package frontend.optimization;

import frontend.optimization.cfg.CFGSimplify;
import frontend.optimization.ssa.PhiConverter;
import frontend.optimization.ssa.SSA;
import midcode.Function;
import midcode.Module;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Optimizer {
    private final Module module;
    public Optimizer(Module module){
        this.module = module;
    }
    public void optimize(){
        getAllFunction().forEach(function -> {
            new CFGSimplify(function).exec();
            new SSA(function).execute();
            new PhiConverter(function).execute();
        });

    }
    private Set<Function> getAllFunction(){
        return Stream.concat(Stream.of(module.getMainFunc()), module.getOtherFunctions().stream()).collect(Collectors.toSet());
    }
}
