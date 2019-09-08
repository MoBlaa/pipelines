package org.myshelf.pipelines;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.function.Function;

@Builder
@AllArgsConstructor
public class Pipeline<S, T> {
    private final Flowable<S> source;
    private final PipelineConfig config;
    private final PipelineStepBuilder<S, T> steps;

    public Flowable<T> exec() {
        return this.source
                .parallel(this.config.getParallel())
                .runOn(Schedulers.newThread())
                .map(steps.build()::apply)
                .sequential();
    }
}
