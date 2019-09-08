package org.myshelf.pipelines;

import org.elasticsearch.search.SearchHits;

public class FooStep implements PipelineStep<SearchHits, String> {
    @Override
    public String apply(SearchHits s) {
        return s.totalHits + " Foos";
    }
}
