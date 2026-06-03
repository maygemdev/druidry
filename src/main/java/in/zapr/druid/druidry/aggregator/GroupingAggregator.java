package in.zapr.druid.druidry.aggregator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import in.zapr.druid.druidry.aggregator.DruidAggregator;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class GroupingAggregator extends DruidAggregator {

    public static final String GROUPING_AGGREGATOR_TYPE = "grouping";

    private final List<String> groupings;

    public GroupingAggregator(List<String> groupings) {
        this.type = GROUPING_AGGREGATOR_TYPE;
        this.name = GROUPING_AGGREGATOR_TYPE;
        this.groupings = List.copyOf(groupings);
    }

    public List<String> getGroupings() {
        return groupings;
    }
}
