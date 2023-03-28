package in.zapr.druid.druidry.lookUpSpec;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SimpleMapLookup extends LookUpSpec {

    private Map<String, String> map;

    public SimpleMapLookup() {
        type = "map";
    }

    public SimpleMapLookup(Map<String, String> map) {
        type = "map";
        this.map = map;
    }


}

