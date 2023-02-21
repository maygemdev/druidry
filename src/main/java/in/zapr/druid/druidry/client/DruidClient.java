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

package in.zapr.druid.druidry.client;

import java.util.List;

import in.zapr.druid.druidry.query.DruidQuery;

public interface DruidClient extends AutoCloseable {
    /**
     * Close the client. No more client methods must be called after this method call.
     */
    void close() throws RuntimeIoException;

    /**
     * Perform Druid query and return raw string response.
     *
     * In case of any client or server error Druid returns code and description withing response.
     * This information can be read from {@link DruidException}.
     * See <a href="https://druid.apache.org/docs/latest/querying/querying.html">Druid documentation</a>
     * for more details.
     */
    String query(DruidQuery query) throws RuntimeIoException, DruidException;

    /**
     * Perform Druid query and return structured response parsed as a target model list.
     * See also documentation for {@link #query(DruidQuery)}
     */
    <T> List<T> query(DruidQuery query, Class<T> clazz) throws RuntimeIoException, DruidException;
}
