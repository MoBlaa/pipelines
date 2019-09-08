package org.myshelf.pipelines;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.ReplaySubject;

import java.util.concurrent.TimeUnit;

public class ElasticsearchSource {
    public static Flowable<String> search() {

        return Flowable.generate(() -> 0L, (state, emitter) -> {
            if(state < 50) {
                emitter.onNext("" + state);
            } else {
                emitter.onComplete();
            }
            return state + 1;
        });
    }
}
