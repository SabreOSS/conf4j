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

package com.sabre.oss.conf4j.converter;

import org.apache.commons.lang3.text.translate.*;

final class JsonLikeEscapeUtils {
    static final char COMMA = ',';
    static final char COLON = ':';
    static final char DOUBLE_QUOTE = '"';
    static final char LEFT_CURLY_BRACE = '{';
    static final char RIGHT_CURLY_BRACE = '}';
    static final char LEFT_SQUARE_BRACKET = '[';
    static final char RIGHT_SQUARE_BRACKET = ']';

    private JsonLikeEscapeUtils() {
    }

    static final String[][] COMPACT_JSON_STRING_ESCAPE = {
            {"\\", "\\\\"},
            {"/", "\\/"},
            {"" + COMMA, "\\" + COMMA},
            {"" + COLON, "\\" + COLON},
            {"" + RIGHT_CURLY_BRACE, "\\" + RIGHT_CURLY_BRACE},
            {"" + RIGHT_SQUARE_BRACKET, "\\" + RIGHT_SQUARE_BRACKET}
    };

    static final CharSequenceTranslator ESCAPE_COMPACT_JSON =
            new AggregateTranslator(
                    new LookupTranslator(COMPACT_JSON_STRING_ESCAPE),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()),
                    JavaUnicodeEscaper.outsideOf(32, 0x7f));

    static final CharSequenceTranslator UNESCAPE_COMPACT_JSON =
            new AggregateTranslator(
                    new UnicodeUnescaper(),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
                    new LookupTranslator(EntityArrays.invert(COMPACT_JSON_STRING_ESCAPE)));
}
