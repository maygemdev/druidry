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

import com.fasterxml.jackson.annotation.JsonInclude;
import in.zapr.druid.druidry.dimension.enums.OutputType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DefaultDimension extends DimensionSpec {

    private static final String DEFAULT_TYPE = "default";

    public DefaultDimension() {
        type = DefaultDimension.DEFAULT_TYPE;
    }

    @Builder
    public DefaultDimension(@NonNull String dimension, @NonNull String outputName,
            OutputType outputType) {
        type = DefaultDimension.DEFAULT_TYPE;
        this.dimension = dimension;
        this.outputName = outputName;
        this.outputType = outputType;
    }
}
