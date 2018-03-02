/*
 * MIT License
 *
 * Copyright 2017-2018 Sabre GLBL Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sabre.oss.conf4j.json.source;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

final class FlattenerUtils {
    /**
     * Property name used when source does not represent map.
     * <p>
     * For example:
     * <pre class="code">
     * some string
     * </pre>
     * is transformed into:
     * <pre class="code">
     * document=some string
     * </pre>
     */
    static final String DEFAULT_PROPERTY = "document";

    private FlattenerUtils() {
    }

    /**
     * Builds a normalized map from a given {@code source}. If the {@code source} is not a {@link Map},
     * {@link FlattenerUtils#DEFAULT_PROPERTY} will be used as a key.
     * <p>
     * Hierarchical objects are exposed by nested paths separated by a dot.
     * <p>
     * Objects nested in a list are indexed, starting with 0, and can be accessed using {@code []} array notation.
     * <p>
     * For example this map:
     * <pre class="code">
     * &nbsp; {
     * &nbsp;   colors = {
     * &nbsp;     red = [
     * &nbsp;       FireBrick,
     * &nbsp;       Crimson,
     * &nbsp;       DarkRed
     * &nbsp;     ],
     * &nbsp;     green = [
     * &nbsp;       Lime,
     * &nbsp;       DarkGreen
     * &nbsp;     ],
     * &nbsp;     yellow = [
     * &nbsp;       Gold,
     * &nbsp;       Moccasin,
     * &nbsp;       Khaki,
     * &nbsp;       LightYellow
     * &nbsp;     ]
     * &nbsp;   }
     * &nbsp; }
     * </pre>
     * is normalized into:
     * <pre class="code">
     * &nbsp; {
     * &nbsp;   colors.red[0]=FireBrick,
     * &nbsp;   colors.red[1]=Crimson,
     * &nbsp;   colors.red[2]=DarkRed,
     * &nbsp;   colors.green[0]=Lime,
     * &nbsp;   colors.green[1]=DarkGreen,
     * &nbsp;   colors.yellow[0]=Gold,
     * &nbsp;   colors.yellow[1]=Moccasin,
     * &nbsp;   colors.yellow[2]=Khaki,
     * &nbsp;   colors.yellow[3]=LightYellow
     * &nbsp; }
     * </pre>
     *
     * @param source source.
     * @return normalized map build from the source.
     */
    static Map<String, String> toFlatMap(Object source) {
        Map<String, String> result = new LinkedHashMap<>();
        buildFlattenedMap(result, buildMap(source), null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> buildMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put(DEFAULT_PROPERTY, object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = buildMap(value);
            }

            String mapKey = key instanceof Number
                    ? keyIndex(((Number) key).intValue())
                    : key.toString();

            result.put(mapKey, value);
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void buildFlattenedMap(Map<String, String> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (isNotEmpty(path)) {
                key = (key.charAt(0) == '[') ? (path + key) : (path + '.' + key);
            }
            if (value instanceof String) {
                result.put(key, (String) value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                int idx = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, singletonMap(keyIndex(idx++), object), key);
                }
            } else {
                // todo
                result.put(key, Objects.toString(value, EMPTY));
            }
        });
    }

    private static String keyIndex(int idx) {
        return "[" + idx + ']';
    }
}
