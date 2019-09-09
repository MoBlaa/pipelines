package org.myshelf.pipelines;

import io.reactivex.rxjava3.core.Flowable;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;

public class ElasticsearchSource {
    public static Flowable<SearchHits> search(RestHighLevelClient client, SearchRequest request, RequestOptions options) throws IOException {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(2));
        request.scroll(scroll);

        SearchResponse rootResponse = client.search(request, options);
        String scrollId = rootResponse.getScrollId();

        // Request doesn't contain a scroll
        if (scrollId == null || "".equals(scrollId)) {
            return Flowable.fromArray(rootResponse.getHits());
        }

        // TODO: Clear Scroll on finish
        return Flowable.fromArray(rootResponse.getHits()).mergeWith(Flowable.generate(() -> scrollId, (state, emitter) -> {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(state);
            scrollRequest.scroll(scroll);
            SearchResponse response = client.scroll(scrollRequest, options);
            SearchHits hits = response.getHits();
            emitter.onNext(hits);
            if (hits.getHits() != null && hits.getHits().length > 0) {
                return response.getScrollId();
            } else {
                emitter.onComplete();
                return "";
            }
        })).doFinally(() -> {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            client.clearScroll(clearScrollRequest, options);
        });
    }
}
