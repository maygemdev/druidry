/*
 * Copyright 2018-present Red Brick Lane Marketing Solutions Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package in.zapr.druid.druidry.dimension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleDimension extends DruidDimension {

    @JsonValue
    private String dimension;

    public SimpleDimension() {
    }

    public SimpleDimension(String dimension) {
        this.dimension = dimension;
    }

    private static String convertValue(Object dimension) {
        final String retVal;

        if (dimension instanceof String) {
            retVal = (String) dimension;
        } else {
            throw new IllegalStateException("Unknown type[" + dimension.getClass() + "] for dimension[" + dimension + "]");
        }

        return retVal;
    }

    @JsonCreator
    public SimpleDimension(Object dimension) {
        this(convertValue(dimension));
    }

}
