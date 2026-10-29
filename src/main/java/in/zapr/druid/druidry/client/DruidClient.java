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

package in.zapr.druid.druidry.client;

import in.zapr.druid.druidry.query.DruidQuery;
import java.util.List;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public interface DruidClient extends AutoCloseable {
    /**
     * Close the client. No more client methods must be called after this method call.
     */
    @Override
    void close() throws RuntimeIoException;

    /**
     * Perform Druid query and return raw string response.
     *
     * In case of any client or server error Druid returns code and description withing response. This information can be
     * read from {@link DruidException}. See <a href="https://druid.apache.org/docs/latest/querying/querying.html">Druid
     * documentation</a> for more details.
     */
    String query(DruidQuery query) throws RuntimeIoException, DruidException;

    /**
     * Perform Druid query and return structured response parsed as a target model list. See also documentation for
     * {@link #query(DruidQuery)}
     */
    <T> List<T> query(DruidQuery query, Class<T> clazz) throws RuntimeIoException, DruidException;

    /**
     * {@link #query(DruidQuery, Class)} with its own request config, e.g. a response timeout longer than the client
     * default for a long-running query. The config replaces the client default for this request.
     */
    <T> List<T> query(DruidQuery query, Class<T> clazz, RequestConfig requestConfig) throws RuntimeIoException, DruidException;

    CloseableHttpResponse queryAsInputStream(DruidQuery query) throws RuntimeIoException, DruidException;

    /**
     * {@link #queryAsInputStream(DruidQuery)} with its own request config, as in
     * {@link #query(DruidQuery, Class, RequestConfig)}.
     */
    CloseableHttpResponse queryAsInputStream(DruidQuery query, RequestConfig requestConfig) throws RuntimeIoException, DruidException;

    CloseableHttpResponse queryAsInputStream(String host, DruidQuery query) throws RuntimeIoException, DruidException;

    /**
     * Cancel the queries with the given context queryId. A broker cancels only the queries it runs itself, so the request
     * goes to every address the broker host name resolves to, as the Druid router does. Throws when a broker could not be
     * reached or refused the request; the remaining brokers are still asked.
     */
    void cancelQuery(String queryId) throws RuntimeIoException;
}
