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

import com.sabre.oss.conf4j.converter.TypeConverter;

import javax.swing.text.Segment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

import static com.sabre.oss.conf4j.converter.composite.JsonLikeEscapeUtils.*;
import static com.sabre.oss.conf4j.converter.composite.TypeConversionUtils.NOT_FOUND;
import static com.sabre.oss.conf4j.converter.composite.TypeConversionUtils.notEscapedIndexOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringEscapeUtils.ESCAPE_JSON;
import static org.apache.commons.lang3.StringEscapeUtils.UNESCAPE_JSON;

/**
 * {@code JsonLikeTypeConverter} is a {@link TypeConverter} that is able to parse and generate JSON-like representation
 * of structured JSON types (JSON Arrays and JSON Objects). For support of value types (e.g. strings,
 * numbers, booleans etc.) {@link #innerTypeConverter} has to be registered.
 * <p>
 * <b>NOTE:</b> JSON must not contain whitespaces.
 * <p>
 * Due to limitations of the JSON format to directly represent Java {@link Map}s as JSON Objects (as JSON Object's property names
 * are limited to JSON String), the format was enhanced to support JSON Objects with <b>any</b> JSON as the property name. For example:
 * <pre>
 *  {
 *     null : "value2",                                         // Valid JSON and JSON-like (after removing whitespaces) entry
 *     ["key2part1", "key2part2"] : [""],                       // Invalid JSON but valid JSON-like entry with structured "property name"
 *     {"key3" : ["item3", "item4"]} : ["value3.1", "value3.2"] // Invalid JSON but valid JSON-like entry with structured "property name"
 *  }
 * </pre>
 * <p>
 * Another enhancement to the JSON format is a {@link #compactMode}, i.e. mode, is that double-quotes
 * can be omitted (double-quotes in compat. mode does not have any special meaning and is treated like the double-quote character).
 * <p>
 * The above example can be converted into a compacted JSON-like format:
 * <pre>{\@null:value1,[key2part1,key2part2]:[\@empty],{key3:[item3,item4]}:[value3.1,value3.2]}</pre>
 *
 * For purposes of compact mode, two new literals were introduced: <b>{@code \@null}</b> for representing
 * null value (the same function to <b>{@code null}</b> literal in standard JSON format) and <b>{@code \@empty}</b> for representing
 * an empty string, but the latter should only be used in one case: to express JSON Array with only one (empty) element
 * (<b>{@code [\@empty]}</b>). It should be noted here, that <b>{@code []}</b> means empty list but <b>{@code [,]}</b>
 * is a two-element list with empty elements.
 *
 * In contrast to JSON special characters escapes, in compact mode, double-quote must not be escaped. The characters
 * must be: ','(comma), ':'(colon), '}'(right curly brace) and ']'(right square bracket).
 */
public class JsonLikeTypeConverter implements TypeConverter<Object> {

    public static final String EMPTY_STRING = "";
    public static final String JSON_NULL = "null";
    public static final String COMPACT_JSON_NULL = "\\@null";     // must not start with non-escaped double-quote char
    public static final String COMPACT_JSON_EMPTY = "\\@empty";   // must not start with non-escaped double-quote char

    private static final String MSG_EXPECTED_BUT_NOT_FOUND = "Expected %s but found '%c' at position %d of string %s";
    private static final String MSG_EXPECTED_BUT_NOT_FOUND_STARTING = "Expected %s starting at position %d of string %s";
    private static final String MSG_CONSUMED_ALL_BUT_NOT_FOUND = "Consumed all characters but not found ending %s in string %s";
    private static final String MSG_UNEXPECTED_EXTRA_DATA = "Object successfully constructed but not all characters have been consumed";
    private static final String MSG_UNEXPECTED_END_OF_DATA = "Unexpected end of string: %s";

    private static final StringEscaper JSON_ESCAPER = new StringEscaper(ESCAPE_JSON::translate, UNESCAPE_JSON::translate);
    private static final StringEscaper COMPACT_JSON_ESCAPER = new StringEscaper(ESCAPE_COMPACT_JSON::translate, UNESCAPE_COMPACT_JSON::translate);

    private TypeConverter<Object> innerTypeConverter;

    /**
     * Compact mode is a JSON mode with omitted beginning and ending DOUBLE-QUOTEs for string values
     * (i.e. DOUBLE-QUOTEs in strings should (and must) not be escaped).
     */
    private boolean compactMode = true;

    public JsonLikeTypeConverter() {
    }

    @SuppressWarnings("unchecked")
    public JsonLikeTypeConverter(TypeConverter<?> innerTypeConverter) {
        this.innerTypeConverter = (TypeConverter<Object>) requireNonNull(innerTypeConverter, "innerTypeConverter must not be null");
    }

    @Override
    public boolean isApplicable(Type type) {
        requireNonNull(type, "type cannot be null");

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class) {
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (List.class.isAssignableFrom(rawType) && actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    return isApplicable(itemType) || innerTypeConverter.isApplicable(itemType);
                } else if (Map.class.isAssignableFrom(rawType) && actualTypeArguments.length == 2) {
                    Type keyType = actualTypeArguments[0];
                    Type valueType = actualTypeArguments[1];
                    return (isApplicable(keyType) || innerTypeConverter.isApplicable(keyType))
                            && (isApplicable(valueType) || innerTypeConverter.isApplicable(valueType));
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object fromString(Type type, String value) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }
        ObjectBuilder builder = new ObjectBuilder();
        char[] array = value.toCharArray();
        CharSequence charSequence = new Segment(array, 0, array.length);
        if (consumeItem(type, charSequence, 0, builder) != value.length()) {
            throw new IllegalArgumentException(MSG_UNEXPECTED_EXTRA_DATA);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString(Type type, Object value) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        try {
            StringBuilder out = new StringBuilder();
            return (List.class.isAssignableFrom(rawType)
                    ? listToString(parameterizedType, (List<Object>) value, out)
                    : mapToString(parameterizedType, (Map<Object, Object>) value, out)).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setInnerTypeConverter(TypeConverter<Object> innerTypeConverter) {
        this.innerTypeConverter = requireNonNull(innerTypeConverter, "innerTypeConverter cannot be null");
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }

    protected List<Object> createList(ParameterizedType type) {
        return new ArrayList<>();
    }

    protected Map<Object, Object> createMap(ParameterizedType type) {
        return type.getRawType() instanceof Class && SortedMap.class.isAssignableFrom((Class<?>) type.getRawType())
                ? new TreeMap<>()
                : new LinkedHashMap<>();
    }

    private int listFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder) {
        try {
            return doListFromString(type, value, builder);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(format(MSG_UNEXPECTED_END_OF_DATA, value), e);
        }
    }

    @SuppressWarnings("unchecked")
    private int doListFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder) {
        Type itemType = type.getActualTypeArguments()[0];

        int current = 0;
        if (value.charAt(current++) != LEFT_SQUARE_BRACKET) {
            int consumed = consumeNull(value, --current, builder);
            if (consumed > 0) {
                return consumed;
            }
            throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                    "'['", value.charAt(current), current, value));
        }
        builder.beginList(createList(type));
        while (current < value.length()) {
            if (value.charAt(current) == RIGHT_SQUARE_BRACKET) {
                builder.endList();
                return current + 1;
            }

            // Consume list item
            current += consumeItem(itemType, value, current, builder);

            // Expected ',' or ']'
            if (value.charAt(current) != COMMA && value.charAt(current) != RIGHT_SQUARE_BRACKET) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                        "':' or ']'", value.charAt(current), current, value));
            }
            if (value.charAt(current) == COMMA) {
                current++;
                if (value.charAt(current) == RIGHT_SQUARE_BRACKET) {
                    // support for proper ,] sequence in compact mode
                    current += consumeItem(itemType, value, current, builder);
                }
            }
        }
        throw new IllegalArgumentException(format(MSG_CONSUMED_ALL_BUT_NOT_FOUND, "']'", value));
    }

    private int mapFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder) {
        try {
            return doMapFromString(type, value, builder);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(format(MSG_UNEXPECTED_END_OF_DATA, value), e);
        }
    }

    @SuppressWarnings("unchecked")
    private int doMapFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder) {
        Type keyType = type.getActualTypeArguments()[0];
        Type valueType = type.getActualTypeArguments()[1];

        int current = 0;
        if (value.charAt(current++) != LEFT_CURLY_BRACE) {
            int consumed = consumeNull(value, --current, builder);
            if (consumed > 0) {
                return consumed;
            }
            throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                    "'{'", value.charAt(current), current, value));
        }

        builder.beginMap(createMap(type));
        while (current < value.length()) {
            if (value.charAt(current) == RIGHT_CURLY_BRACE) {
                builder.endMap();
                return current + 1;
            }
            // Consume Key
            ObjectBuilder keyBuilder = new ObjectBuilder();
            current += consumeItem(keyType, value, current, keyBuilder);
            builder.addKey(keyBuilder.build());

            // Consume ':'
            if (value.charAt(current++) != COLON) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                        "':'", value.charAt(--current), current, value));
            }

            // Consume Value
            current += consumeItem(valueType, value, current, builder);

            // Expected ',' or '}' but not both - checked below.
            if (value.charAt(current) != COMMA && value.charAt(current) != RIGHT_CURLY_BRACE) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                        "',' or '}'", value.charAt(current), current, value));
            }
            if (value.charAt(current) == COMMA) {
                current++;
                if (value.charAt(current) == RIGHT_CURLY_BRACE) {
                    throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                            "object key", '}', current, value));
                }
            }
        }
        throw new IllegalArgumentException(format(MSG_CONSUMED_ALL_BUT_NOT_FOUND, "'}'", value));
    }

    private int consumeItem(Type type, CharSequence value, int current, ObjectBuilder builder) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return listFromString(parameterizedType, value.subSequence(current, value.length()), builder);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return mapFromString(parameterizedType, value.subSequence(current, value.length()), builder);
            }
        }

        // Process item as a string value
        if (compactMode) {
            int found = notEscapedIndexOf(value, current, COMMA, COLON, RIGHT_CURLY_BRACE, RIGHT_SQUARE_BRACKET);
            if (found == NOT_FOUND) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND_STARTING,
                        "',', ':', '}' or ']'", current, value));
            }
            CharSequence foundString = value.subSequence(current, found);
            // we do not use consumeNull() here because  we already have all needed data and this part of code
            // is executed very frequently (in compact-mode)
            boolean nullFound = value.charAt(current) == COMPACT_JSON_NULL.charAt(0)
                    && found == COMPACT_JSON_NULL.length() + current
                    && COMPACT_JSON_NULL.equals(foundString.toString());
            boolean emptyFound = !nullFound && value.charAt(current) == COMPACT_JSON_EMPTY.charAt(0)
                    && found == COMPACT_JSON_EMPTY.length() + current
                    && COMPACT_JSON_EMPTY.equals(foundString.toString());
            builder.addValue(nullFound
                    ? null
                    : innerTypeConverter.fromString(type, COMPACT_JSON_ESCAPER.unescape(emptyFound ? EMPTY_STRING : foundString)));
            return found - current;
        }

        if (value.charAt(current) != DOUBLE_QUOTE) {
            int consumed = consumeNull(value, current, builder);
            if (consumed > 0) {
                return consumed;
            }
            throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                    "opening '\"'", value.charAt(current), current, value));
        }
        int found = notEscapedIndexOf(value, current + 1, DOUBLE_QUOTE);
        if (found == NOT_FOUND) {
            throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND_STARTING,
                    "closing '\"'", current, value));
        }
        CharSequence foundString = value.subSequence(current + 1, found);
        builder.addValue(innerTypeConverter.fromString(type, JSON_ESCAPER.unescape(foundString)));
        return found - current + 1;
    }

    int consumeNull(CharSequence value, int current, ObjectBuilder builder) {
        int found = value.length();
        boolean consume = false;
        if (compactMode) {
            if (value.charAt(current) == COMPACT_JSON_NULL.charAt(0)) {
                if (found != COMPACT_JSON_NULL.length() + current) {
                    found = notEscapedIndexOf(value, current, COMMA, COLON, RIGHT_CURLY_BRACE, RIGHT_SQUARE_BRACKET);
                }
                consume = found == COMPACT_JSON_NULL.length() + current && COMPACT_JSON_NULL.equals(value.subSequence(current, found).toString());
            }
        } else {
            if (value.charAt(current) == JSON_NULL.charAt(0)) {
                if (found != JSON_NULL.length() + current) {
                    found = notEscapedIndexOf(value, current, COMMA, COLON, RIGHT_CURLY_BRACE, RIGHT_SQUARE_BRACKET);
                }
                consume = found == JSON_NULL.length() + current && JSON_NULL.equals(value.subSequence(current, found).toString());
            }
        }
        if (consume) {
            builder.addValue(null);
            return found - current;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private Appendable listToString(ParameterizedType type, List<Object> value, Appendable out) throws IOException {
        if (value == null) {
            return compactMode
                    ? out.append(COMPACT_JSON_NULL)
                    : out.append(JSON_NULL);
        }

        out.append(LEFT_SQUARE_BRACKET);
        Type itemType = type.getActualTypeArguments()[0];
        Iterator<Object> iterator = value.iterator();

        // The only case to encode empty string in compact mode is one element List
        boolean encodeEmptyCompactString = value.size() == 1;
        while (iterator.hasNext()) {
            append(itemType, iterator.next(), out, encodeEmptyCompactString);
            if (iterator.hasNext()) {
                out.append(COMMA);
            }
        }
        return out.append(RIGHT_SQUARE_BRACKET);
    }

    private Appendable mapToString(ParameterizedType type, Map<Object, Object> value, Appendable out) throws IOException {
        if (value == null) {
            return compactMode
                    ? out.append(COMPACT_JSON_NULL)
                    : out.append(JSON_NULL);
        }

        out.append(LEFT_CURLY_BRACE);
        Type keyType = type.getActualTypeArguments()[0];
        Type valueType = type.getActualTypeArguments()[1];
        Iterator<Entry<Object, Object>> iterator = value.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> entry = iterator.next();
            append(keyType, entry.getKey(), out);
            out.append(COLON);
            append(valueType, entry.getValue(), out);
            if (iterator.hasNext()) {
                out.append(COMMA);
            }
        }
        out.append(RIGHT_CURLY_BRACE);
        return out;
    }

    private Appendable append(Type type, Object value, Appendable appendable) throws IOException {
        return append(type, value, appendable, false);
    }

    @SuppressWarnings("unchecked")
    private Appendable append(Type type, Object value, Appendable out, boolean encodeCompactEmptyString) throws IOException {
        if (value == null) {
            return out.append(compactMode
                    ? COMPACT_JSON_NULL
                    : JSON_NULL);
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return listToString(parameterizedType, (List<Object>) value, out);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return mapToString(parameterizedType, (Map<Object, Object>) value, out);
            }
        }

        // Not a List/Map, treated as a string literal
        if (compactMode) {
            String valueStr = innerTypeConverter.toString(type, value);
            return out.append(encodeCompactEmptyString && EMPTY_STRING.equals(valueStr)
                    ? COMPACT_JSON_EMPTY
                    : COMPACT_JSON_ESCAPER.escape(valueStr));
        }
        return out
                .append(DOUBLE_QUOTE)
                .append(JSON_ESCAPER.escape(innerTypeConverter.toString(type, value)))
                .append(DOUBLE_QUOTE);
    }
}
