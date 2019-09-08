package org.myshelf.pipelines;

public class FooStep implements PipelineStep<String, String> {
    @Override
    public String apply(String s) {
        return s + "Foo";
    }
}
