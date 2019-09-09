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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                        // TODO remove need of explicit setting type here
                        .pipe((SearchHits hits) -> hits.getHits().length)
                        .pipe(counter::addAndGet)
                        .pipe(l -> {
                            long waiting = (long) (Math.random() * 1000);
                            try {
                                Thread.sleep(waiting);
                            } catch (InterruptedException ignored) {
                            }
                            return "Took " + waiting + "ms :: " + l;
                        })
                        .pipe(l -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH.mm.ss.SSS")) + " :: " + l)
                        .pipe((l) -> String.format("%1$" + 26 + "s",Thread.currentThread().getName()) + " :: " + l)
                )
                .build();

        pipeline.exec().doFinally(client::close).subscribe(System.out::println);
    }
}
