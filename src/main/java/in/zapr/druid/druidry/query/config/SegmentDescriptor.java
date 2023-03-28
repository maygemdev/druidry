package in.zapr.druid.druidry.query.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class SegmentDescriptor {
    @JsonProperty("itvl")
    private Interval interval;
    @JsonProperty("ver")
    private String version;
    @JsonProperty("part")
    private int partitionNumber;

    public SegmentDescriptor(@NonNull Interval interval, @NonNull String version, @NonNull int partitionNumber) {
        this.interval = interval;
        this.version = version;
        this.partitionNumber = partitionNumber;
    }

}
