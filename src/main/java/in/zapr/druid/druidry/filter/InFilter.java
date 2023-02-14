/*
 * Copyright 2018-present Red Brick Lane Marketing Solutions Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.zapr.druid.druidry.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.zapr.druid.druidry.extractionFunctions.ExtractionFunction;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode(callSuper = true)
public class InFilter extends DruidFilter {
    private static String IN_DRUID_FILTER_TYPE = "in";

    private String dimension;
    private List<String> values;
    @JsonProperty("extractionFn")
    private ExtractionFunction extractionFunction;

    public InFilter(@NonNull String dimension, @NonNull List<String> values) {
        type = IN_DRUID_FILTER_TYPE;
        this.dimension = dimension;
        this.values = values;
    }

    public InFilter(@NonNull String dimension, @NonNull List<String> values, ExtractionFunction extractionFunction) {
        this(dimension, values);
        this.extractionFunction = extractionFunction;
    }
}