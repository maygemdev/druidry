package in.zapr.druid.druidry.lookUpSpec;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SimpleMapLookup extends LookUpSpec {

    private Map<String, String> map;

    public SimpleMapLookup(Map<String, String> map) {
        type = "map";
        this.map = map;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimpleMapLookup)) {
            return false;
        }
        SimpleMapLookup that = (SimpleMapLookup) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, map);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("map", map)
                .toString();
    }
}

