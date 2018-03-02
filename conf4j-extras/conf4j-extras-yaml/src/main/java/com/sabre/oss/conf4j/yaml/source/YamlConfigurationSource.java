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

package com.sabre.oss.conf4j.yaml.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sabre.oss.conf4j.json.source.AbstractJacksonConfigurationSource;

import java.io.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

/**
 * Configuration source which supports YAML. It flattens the YAML structure to key-value properties.
 * <p>
 * Hierarchical objects are exposed by nested paths.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; users:
 * &nbsp;   admin:
 * &nbsp;     name: John Smith
 * &nbsp;     age: 30
 * &nbsp;     country: USA
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * users.admin.name=John Smith
 * users.admin.age=30
 * users.admin.country=USA
 * </pre>
 * {@code []} brackets are used to split lists into property keys.
 * <p>
 * For example:
 * <pre class="code">
 * continents:
 * &nbsp;  - Asia
 * &nbsp;  - Africa
 * &nbsp;  - North America
 * &nbsp;  - South America
 * &nbsp;  - Antarctica
 * &nbsp;  - Europe
 * &nbsp;  - Australia
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * continents[0]=Asia
 * continents[1]=Africa
 * continents[2]=North America
 * continents[3]=South America
 * continents[4]=Antarctica
 * continents[5]=Europe
 * continents[6]=Australia
 * </pre>
 */
public class YamlConfigurationSource extends AbstractJacksonConfigurationSource {

    /**
     * Constructs value source from {@link InputStream}.
     * <p>
     * <em>Note:</em> YAML is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param inputStream input stream which provides YAML.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(InputStream inputStream) {
        super(createObjectMaper(), inputStream);
    }

    /**
     * Constructs value source from {@link Reader}.
     * <p>
     * <em>Note:</em> YAML is loaded and processed in the constructor and {@code reader} is closed
     * at the end of processing.
     *
     * @param reader reader which provides YAML source.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(Reader reader) {
        super(createObjectMaper(), reader);
    }

    /**
     * Constructs value source from {@link File}.
     *
     * @param file file which contains YAML source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when {@link IOException} is thrown during processing.
     */
    public YamlConfigurationSource(File file) {
        super(createObjectMaper(), file);
    }

    private static ObjectMapper createObjectMaper() {
        YAMLFactory yamlFactory = new YAMLFactory()
                .enable(MINIMIZE_QUOTES)
                .disable(WRITE_DOC_START_MARKER);

        return new ObjectMapper(yamlFactory);
    }
}
