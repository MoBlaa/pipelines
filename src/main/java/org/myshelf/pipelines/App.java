package org.myshelf.pipelines;

import io.reactivex.rxjava3.disposables.Disposable;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, IOException {
        PipelineConfig config = PipelineConfig.builder()
                .parallel(Runtime.getRuntime().availableProcessors())
                .build();

        Pipeline<String, String> pipeline = Pipeline.<String, String>builder()
                .source(ElasticsearchSource.search())
                .config(config)
                .steps(PipelineStep
                        .pipe(new FooStep())
                        .pipe(new ThreadStep())
                        .pipe(new HeavyStep(500))
                )
                .build();

        Disposable disp = pipeline.exec().subscribe(res -> System.out.println("Mssg: " + res));

        System.in.read();
    }
}
