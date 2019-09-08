package org.myshelf.pipelines;

import java.util.function.Function;

public interface PipelineStep<C, P> extends Function<C, P> {
    static <C, P> PipelineStepBuilder<C, P> pipe(PipelineStep<C, P> step) {
        return new PipelineStepBuilder<>(step);
    }
}
