package in.zapr.druid.druidry.dimension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import in.zapr.druid.druidry.lookUpSpec.SimpleMapLookup;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LookupDimension extends DimensionSpec {

    private static final String EXTRACTION_TYPE = "lookup";

    private Boolean retainMissingValue;
    private String replaceMissingValueWith;
    private SimpleMapLookup lookup;

    public LookupDimension() {
        type = EXTRACTION_TYPE;
    }

    public LookupDimension(String dimension, String outputName,
            Boolean retainMissingValue, String replaceMissingValueWith, SimpleMapLookup lookup) {
        this.retainMissingValue = retainMissingValue;
        this.replaceMissingValueWith = replaceMissingValueWith;
        this.lookup = lookup;
        type = EXTRACTION_TYPE;
        this.dimension = dimension;
        this.outputName = outputName;
    }

    public Boolean getRetainMissingValue() {
        return retainMissingValue;
    }

    public String getReplaceMissingValueWith() {
        return replaceMissingValueWith;
    }

    public SimpleMapLookup getLookup() {
        return lookup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LookupDimension)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LookupDimension that = (LookupDimension) o;
        return Objects.equals(retainMissingValue, that.retainMissingValue) &&
                Objects.equals(replaceMissingValueWith, that.replaceMissingValueWith) &&
                Objects.equals(lookup, that.lookup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), retainMissingValue, replaceMissingValueWith, lookup);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("retainMissingValue", retainMissingValue)
                .add("replaceMissingValueWith", replaceMissingValueWith)
                .add("lookup", lookup)
                .toString();
    }
}
