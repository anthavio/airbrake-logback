/**
 * Copyright (C) 2014 Anthavio (http://dev.anthavio.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anthavio.airbrake.http;

import io.airbrake.javabrake.Notice;

/**
 * Created by m.vanek on 30/06/2017.
 */
public interface RequestEnhancer<T> {

    /**
     * Server API
     */
    void setRequest(T request);

    /**
     * Server API
     */
    void endRequest(T request);

    /**
     * Notification API
     */
    void enhance(Notice notice);
}
