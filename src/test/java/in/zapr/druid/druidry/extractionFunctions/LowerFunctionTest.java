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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

public class LowerFunctionTest {

    private static JsonMapper objectMapper;

    @BeforeClass
    public void init() {
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    public void testAllFields() throws JacksonException, JSONException {
        Locale locale = Locale.FRENCH;

        LowerExtractionFunction lowerExtractionFunction = LowerExtractionFunction.builder()
                .locale(locale)
                .build();

        String actualJSON = objectMapper.writeValueAsString(lowerExtractionFunction);

        String expectedJSONString = "{\n\"type\" : \"lower\",\n    \"locale\" : \"fr\"\n}";

        JSONAssert.assertEquals(expectedJSONString, actualJSON, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void TestRequiredFields() throws JacksonException, JSONException {
        LowerExtractionFunction lowerExtractionFunction = LowerExtractionFunction.builder().build();

        String actualJSON = objectMapper.writeValueAsString(lowerExtractionFunction);
        String expectedJSONString = "{ \"type\" : \"lower\" }\n";

        JSONAssert.assertEquals(expectedJSONString, actualJSON, JSONCompareMode.NON_EXTENSIBLE);
    }
}