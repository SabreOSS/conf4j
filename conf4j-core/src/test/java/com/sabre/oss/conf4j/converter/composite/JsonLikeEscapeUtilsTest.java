/*
 * MIT License
 *
 * Copyright 2017 Sabre GLBL Inc.
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

package com.sabre.oss.conf4j.converter.composite;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonLikeEscapeUtilsTest {

    @Test
    public void shouldProperlyEscapeCompactJsonCharacters() {
        // given
        String in = "/\\/n/t/b/r,:}] \"[{";
        // when
        String out = JsonLikeEscapeUtils.ESCAPE_COMPACT_JSON.translate(in);
        // then
        assertThat(out).isEqualTo("\\/\\\\\\/n\\/t\\/b\\/r\\,\\:\\}\\] \"[{");
    }

    @Test
    public void shouldProperlyUnescapeCompactJsonCharacters() {
        // given
        String in = "\\/\\\\\\/n\\/t\\/b\\/r\\,\\:\\}\\] \"[{";
        // when
        String out = JsonLikeEscapeUtils.UNESCAPE_COMPACT_JSON.translate(in);
        // then
        assertThat(out).isEqualTo("/\\/n/t/b/r,:}] \"[{");
    }
}
