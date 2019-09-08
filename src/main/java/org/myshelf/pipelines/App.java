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

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException, IOException {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "https")));
        SearchRequest request = new SearchRequest("test");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        sourceBuilder.size(100);
        request.source(sourceBuilder);
        request.scroll(TimeValue.timeValueMinutes(1L));

        Flowable<SearchHits> source = ElasticsearchSource.search(client, request, RequestOptions.DEFAULT);

        PipelineConfig config = PipelineConfig.builder()
                .parallel(Runtime.getRuntime().availableProcessors())
                .build();
        Pipeline<SearchHits, String> pipeline = Pipeline.<SearchHits, String>builder()
                .source(source)
                .config(config)
                .steps(PipelineStep
                        .pipe(new FooStep())
                        .pipe(new ThreadStep())
                        .pipe(new HeavyStep(500))
                )
                .build();

        pipeline.exec().subscribe(res -> System.out.println("Mssg: " + res));

        System.in.read();
    }
}
