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

package com.sabre.oss.conf4j.converter.standard;

import com.sabre.oss.conf4j.converter.TypeConverter;
import com.sabre.oss.conf4j.internal.utils.spring.ConcurrentReferenceHashMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * <p>This class converts {@link Boolean} to/from string.</p>
 * <p>The converter supports {@value #FORMAT} attribute (provided in the attributes map) which specifies
 * the format used during conversion.</p>
 * <p>Possible formats are: value_when_true/value_when_false</p>
 * <p>
 * When the format is not specified, {@link Objects#toString() } method is used.
 */
public class BooleanTypeConverter implements TypeConverter<Boolean> {

    /**
     * Format attribute name.
     */
    public static final String FORMAT = "format";

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final ConcurrentMap<String, BooleanFormatter> cache = new ConcurrentReferenceHashMap<>();

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        return type instanceof Class<?> &&
                (Boolean.class.isAssignableFrom((Class<?>) type) || Boolean.TYPE.isAssignableFrom((Class<?>) type));
    }

    /**
     * Converts String to {@link Boolean}
     *
     * @param type       actual type definition.
     * @param value      string representation of the value which is converted to {@link Boolean}.
     *                   In case it is {@code null}, the converter should return either {@code null} or a value
     *                   that is equivalent (for example an empty list).
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, the value for {@value #FORMAT} key will be used during conversion
     *                   as a formatting pattern.
     * @return value converted to {@link Boolean}
     * @throws IllegalArgumentException when {@code value} cannot be converted to {@link Boolean} because of
     *                                  invalid format of {@code value} string or invalid formatting pattern.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public Boolean fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }

        if (attributes != null && attributes.containsKey(FORMAT)) {
            String formattingPattern = attributes.get(FORMAT);
            return cache.computeIfAbsent(formattingPattern, BooleanFormatter::getInstance).parseString(value);
        }

        if (TRUE.equals(value)) {
            return Boolean.TRUE;
        }
        if (FALSE.equals(value)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(format("Unable to convert to Boolean. Unknown value: %s", value));
    }

    /**
     * Converts value from {@link Boolean} to String.
     *
     * @param type       actual type definition.
     * @param value      value that needs to be converted to string.
     * @param attributes additional meta-data attributes which may be used by converter. It can be {@code null}.
     *                   If present, value for "format" key will be used as formatting pattern.
     * @return string representation of the {@code value}
     * @throws IllegalArgumentException when {@code value} cannot be converted to String because of
     *                                  invalid formatting pattern or error during printing.
     * @throws NullPointerException     when {@code type} is {@code null}.
     */
    @Override
    public String toString(Type type, Boolean value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (attributes != null && attributes.containsKey(FORMAT)) {
            String formattingPattern = attributes.get(FORMAT);
            return cache.computeIfAbsent(formattingPattern, BooleanFormatter::getInstance).formatBoolean(value);
        }

        return Objects.toString(value, null);
    }

    private static final class BooleanFormatter {
        private String trueString;
        private String falseString;
        private String formattingPattern;

        private BooleanFormatter() {
        }

        static BooleanFormatter getInstance(String format) {
            BooleanFormatter bf = new BooleanFormatter();
            bf.setFormattingPattern(format);
            bf.parseFormat();
            return bf;
        }

        String formatBoolean(Boolean value) {
            return value ? trueString : falseString;
        }

        private void setFormattingPattern(String formattingPattern) {
            this.formattingPattern = formattingPattern;
        }

        Boolean parseString(String value) {
            if (value.equals(trueString)) {
                return true;
            }
            if (value.equals(falseString)) {
                return false;
            }
            throw new IllegalArgumentException(String.format(
                    "Provided value: %s does not match specified format: %s", value, formattingPattern));
        }

        private void parseFormat() {
            String[] options = formattingPattern.split("/");
            if (options.length == 2 && !options[0].isEmpty() && !options[1].isEmpty()) {
                trueString = options[0];
                falseString = options[1];
            } else {
                throw new IllegalArgumentException(String.format("Provided formatting pattern cannot be parsed: %s", formattingPattern));
            }
        }
    }
}
