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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type", visible = true, defaultImpl = SimpleDimension.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultDimension.class, name = "default"),
        @JsonSubTypes.Type(value = ExtractionDimension.class, name = "extraction"),
        @JsonSubTypes.Type(value = LookupDimension.class, name = "lookup"),
        @JsonSubTypes.Type(value = ListFilteredDimension.class, name = "listFiltered"),
        @JsonSubTypes.Type(value = PrefixFilteredDimension.class, name = "prefixFiltered"),
        @JsonSubTypes.Type(value = RegexFilteredDimension.class, name = "regexFiltered")
})
public abstract class DruidDimension {

    protected String type;
}
