package org.myshelf.pipelines;

public class ThreadStep implements PipelineStep<String, String> {
    @Override
    public String apply(String s) {
        return Thread.currentThread().getName() + " :: " + s;
    }
}
