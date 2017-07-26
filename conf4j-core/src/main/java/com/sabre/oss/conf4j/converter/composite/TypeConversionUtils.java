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

import java.util.Objects;

import static java.lang.Math.max;

final class TypeConversionUtils {
    static final int NOT_FOUND = -1;

    private static final char ESCAPE_CHAR = '\\';

    private TypeConversionUtils() {
    }

    static int notEscapedIndexOf(CharSequence value, int startPos, char character) {
        Objects.requireNonNull(value, "value must not be null");

        char prev = 0xffff;
        for (int i = max(startPos, 0), len = value.length(); i < len; i++) {
            char current = value.charAt(i);
            if (current == character) {
                if (prev == ESCAPE_CHAR) {
                    int m = 1;
                    int l = i - 2;
                    while (l >= 0 && value.charAt(l) == ESCAPE_CHAR) {
                        l--;
                        m++;
                    }
                    if (m % 2 == 0) {
                        return i;
                    }
                } else {
                    return i;
                }
            }
            prev = current;
        }
        return NOT_FOUND;
    }

    static int notEscapedIndexOf(CharSequence value, int startPos, char... characters) {
        Objects.requireNonNull(value, "value must not be null");

        int charsLen = characters.length;
        char prev = 0xffff;
        for (int i = max(startPos, 0), len = value.length(); i < len; i++) {
            char current = value.charAt(i);
            for (char character : characters) {
                if (current == character) {
                    if (prev != ESCAPE_CHAR) {
                        return i;
                    } else {
                        int m = 1;
                        int l = i - 2;
                        while (l >= 0 && value.charAt(l) == ESCAPE_CHAR) {
                            l--;
                            m++;
                        }
                        if (m % 2 == 0) {
                            return i;
                        }
                    }
                }
            }
            prev = current;
        }
        return NOT_FOUND;
    }
}
