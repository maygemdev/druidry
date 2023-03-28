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

package in.zapr.druid.druidry.extractionFunctions;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CascadeExtractionFunction extends ExtractionFunction {

    private List<ExtractionFunction> extractionFns;

    public CascadeExtractionFunction() {
        type = ExtractionFunction.CASCADE_TYPE;
    }

    @Builder
    private CascadeExtractionFunction(@NonNull List<ExtractionFunction> extractionFns) {
        type = ExtractionFunction.CASCADE_TYPE;
        this.extractionFns = extractionFns;
    }
}
