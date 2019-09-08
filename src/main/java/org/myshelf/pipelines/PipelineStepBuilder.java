package org.myshelf.pipelines;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public
class PipelineStepBuilder<C, P> {
    private Function<C, P> current;

    public <NP> PipelineStepBuilder<C, NP> pipe(PipelineStep<P, NP> step) {
        Function<C, NP> newStep = step.compose(this.current);
        return new PipelineStepBuilder<>(newStep);
    }

    public Function<C, P> build() {
        return this.current;
    }
}
