package org.myshelf.pipelines;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
@Builder
public class PipelineConfig {
    @Default
    private final int parallel = -1;
}
