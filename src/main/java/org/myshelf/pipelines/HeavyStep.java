package org.myshelf.pipelines;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeavyStep implements PipelineStep<String, String> {
    private final long millis;

    @Override
    public String apply(String s) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return s;
    }
}
