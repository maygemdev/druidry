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

package in.zapr.druid.druidry.postAggregator;

import in.zapr.druid.druidry.dimension.enums.OutputType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ExpressionScriptPostAggregator extends DruidPostAggregator {

    private final static String EXPRESSION_POST_AGGREGATOR_TYPE = "expression";

    private String expression;

    private OutputType outputType;

    @Builder
    private ExpressionScriptPostAggregator(@NonNull String name, @NonNull String expression, @NonNull OutputType outputType) {
        type = EXPRESSION_POST_AGGREGATOR_TYPE;
        this.name = name;
        this.expression = expression;
        this.outputType = outputType;
    }
}
