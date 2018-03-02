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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

/**
 * Configuration source which supports JSON. It flattens the JSON structure to key-value properties.
 * <p>
 * Hierarchical objects are exposed by nested paths.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; {
 * &nbsp;  "teams": {
 * &nbsp;    "ferrari": {
 * &nbsp;      "engine": "ferrari",
 * &nbsp;      "score": 522,
 * &nbsp;      "private": false
 * &nbsp;     },
 * &nbsp;     "williams": {
 * &nbsp;       "engine": "mercedes",
 * &nbsp;       "score": 83,
 * &nbsp;       "private": true
 * &nbsp;     }
 * &nbsp;   }
 * &nbsp; }
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * teams.ferrari.engine=ferrari
 * teams.ferrari.score=522
 * teams.ferrari.private=false
 * teams.williams.engine=mercedes
 * teams.williams.score=83
 * teams.williams.private=true
 * </pre>
 * {@code []} brackets are used to split lists into property keys.
 * <p>
 * For example:
 * <pre class="code">
 * &nbsp; {
 * &nbsp;   "months": {
 * &nbsp;     "odd": [
 * &nbsp;       "January",
 * &nbsp;       "March",
 * &nbsp;       "May",
 * &nbsp;       "July",
 * &nbsp;       "August",
 * &nbsp;       "October",
 * &nbsp;       "December"
 * &nbsp;     ],
 * &nbsp;     "even": [
 * &nbsp;       "April",
 * &nbsp;       "June",
 * &nbsp;       "September",
 * &nbsp;       "November"
 * &nbsp;     ],
 * &nbsp;     "both": "February"
 * &nbsp;   }
 * &nbsp; }
 * </pre>
 * is transformed into these properties:
 * <pre class="code">
 * months.odd[0]=January
 * months.odd[1]=March
 * months.odd[2]=May
 * months.odd[3]=July
 * months.odd[4]=August
 * months.odd[5]=October
 * months.odd[6]=December
 * months.even[0]=April
 * months.even[1]=June
 * months.even[2]=September
 * months.even[3]=November
 * months.both=February
 * </pre>
 */
public class JsonConfigurationSource extends AbstractJacksonConfigurationSource {
    /**
     * Constructs value source from {@link InputStream}.
     * <p>
     * <em>Note:</em> JSON is loaded and processed in the constructor and {@code inputStream} is closed
     * at the end of processing.
     *
     * @param inputStream input stream which provides JSON.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(InputStream inputStream) {
        super(new ObjectMapper(), inputStream);
    }

    /**
     * Constructs value source from {@link Reader}.
     * <p>
     * <em>Note:</em> JSON is loaded and processed in the constructor and {@code reader} is closed
     * at the end of processing.
     *
     * @param reader reader which provides JSON source.
     * @throws UncheckedIOException when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(Reader reader) {
        super(new ObjectMapper(), reader);
    }

    /**
     * Constructs value source from {@link File}.
     *
     * @param file file which contains JSON source.
     * @throws IllegalArgumentException when provided file not found.
     * @throws UncheckedIOException     when {@link IOException} is thrown during processing.
     */
    public JsonConfigurationSource(File file) {
        super(new ObjectMapper(), file);
    }
}
