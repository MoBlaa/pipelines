package org.myshelf.pipelines;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, IOException {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        SearchRequest request = new SearchRequest("shakespeare");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        sourceBuilder.size(1_000);
        request.source(sourceBuilder);
        request.scroll(TimeValue.timeValueMinutes(1L));

        Flowable<SearchHits> source = ElasticsearchSource.search(client, request, RequestOptions.DEFAULT);

        AtomicInteger counter = new AtomicInteger(0);

        PipelineConfig config = PipelineConfig.builder()
                .parallel(Runtime.getRuntime().availableProcessors())
                .build();
        Pipeline<SearchHits, String> pipeline = Pipeline.<SearchHits, String>builder()
                .source(source)
                .config(config)
                .steps(PipelineStep
                        .pipe((SearchHits hits) -> hits.getHits().length)
                        .pipe(counter::addAndGet)
                        .pipe((l) -> Thread.currentThread().getName() + ": " + l.toString())
                )
                .build();

        PrintWriter writer = new PrintWriter(new FileWriter("output.log"));

        pipeline.exec().doFinally(() -> System.out.println(Thread.currentThread().getName() + ": Finished writing!")).subscribe(writer::println);

        writer.flush();
        System.out.println("Finalizing!");
        writer.close();
        client.close();
    }
}
