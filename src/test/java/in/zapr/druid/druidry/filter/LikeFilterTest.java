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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import in.zapr.druid.druidry.extractionFunctions.LowerExtractionFunction;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LikeFilterTest {

    private static JsonMapper objectMapper;

    @BeforeClass
    public void init() {
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    public void testAllFields() throws JSONException, JacksonException {
        LikeFilter filter = LikeFilter.builder()
                .dimension("dimension")
                .pattern("_test%")
                .escape("'")
                .extractionFunction(new LowerExtractionFunction())
                .build();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "like");
        jsonObject.put("dimension", "dimension");
        jsonObject.put("pattern", "_test%");
        jsonObject.put("escape", "'");
        jsonObject.put("extractionFn", new JSONObject().put("type", "lower"));

        String actualJSON = objectMapper.writeValueAsString(filter);
        String expectedJSON = jsonObject.toString();
        JSONAssert.assertEquals(expectedJSON, actualJSON, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testDimensionMissing() {
        LikeFilter filter = new LikeFilter(null, "World", null, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPatternMissing() {
        LikeFilter filter = new LikeFilter("Hello", null, null, null);
    }

}
