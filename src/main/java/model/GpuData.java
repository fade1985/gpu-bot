package model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GpuData {

    private int id;
    private String modelName;
    private String url;
}
