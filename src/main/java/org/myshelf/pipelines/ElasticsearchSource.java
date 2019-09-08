package org.myshelf.pipelines;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ElasticsearchSource {
    public static Flowable<SearchHits> search(RestHighLevelClient client, SearchRequest request, RequestOptions options) throws IOException {
        SearchResponse response = client.search(request, options);
        String scrollId = response.getScrollId();

        // Request doesn't contain a scroll
        if (scrollId == null || "".equals(scrollId)) {
            return Flowable.fromArray(response.getHits());
        }

        // TODO: Clear Scroll on finish
        return Flowable.generate(() -> scrollId, (state, emitter) -> {
            if (state == null || state.equals("")) {
                emitter.onNext(response.getHits());
                emitter.onComplete();
                return "";
            } else {
                emitter.onNext(response.getHits());
                return response.getScrollId();
            }
        });
    }
}
