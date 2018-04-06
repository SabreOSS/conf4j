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

import javax.swing.text.Segment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

import static com.sabre.oss.conf4j.converter.JsonLikeEscapeUtils.*;
import static com.sabre.oss.conf4j.converter.TypeConverterUtils.NOT_FOUND;
import static com.sabre.oss.conf4j.converter.TypeConverterUtils.notEscapedIndexOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.text.StringEscapeUtils.ESCAPE_JSON;
import static org.apache.commons.text.StringEscapeUtils.UNESCAPE_JAVA;

/**
 * {@code JsonLikeConverter} is a {@link TypeConverter} that is able to parse and generate JSON-like representation
 * of structured JSON types (JSON Arrays and JSON Objects). For support of value types (e.g. strings,
 * numbers, booleans etc.) {@link #innerTypeConverter} has to be registered.
 *
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
 * Another enhancement to the JSON format is a {@link #defaultCompactMode}, i.e. mode, is that double-quotes
 * can be omitted (double-quotes in compat. mode does not have any special meaning and is treated like the double-quote character).
 * <p>
 * The above example can be converted into a compacted JSON-like format:
 * <pre>{\@null:value1,[key2part1,key2part2]:[\@empty],{key3:[item3,item4]}:[value3.1,value3.2]}</pre>
 * <p>
 * For purposes of compact mode, two new literals were introduced: <b>{@code \@null}</b> for representing
 * null value (the same function to <b>{@code null}</b> literal in standard JSON format) and <b>{@code \@empty}</b> for representing
 * an empty string, but the latter should only be used in one case: to express JSON Array with only one (empty) element
 * (<b>{@code [\@empty]}</b>). It should be noted here, that <b>{@code []}</b> means empty list but <b>{@code [,]}</b>
 * is a two-element list with empty elements.
 * <p>
 * In contrast to JSON special characters escapes, in compact mode, double-quote must not be escaped. The characters
 * must be: ','(comma), ':'(colon), '}'(right curly brace) and ']'(right square bracket).
 * <p>
 * {@code JsonLikeConverter} supports {@value FORMAT} meta-attribute which can be used to override
 * {@link #defaultCompactMode} in {@link #fromString(Type, String, Map)} and {@link #toString(Type, Object, Map)}.
 * <p>
 * When {@value FORMAT} meta-attribute value is:
 * <ul>
 * <li>{@value COMPACT} - compact format is used.</li>
 * <li>{@value JSON} - JSON-like format is used.</li>
 * </ul>
 */
public class JsonLikeConverter implements TypeConverter<Object> {
    public static final String FORMAT = "format";
    public static final String COMPACT = "compact";
    public static final String JSON = "json";

    public static final String EMPTY_STRING = "";
    public static final String JSON_NULL = "null";
    public static final String COMPACT_JSON_NULL = "\\@null";     // must not start with non-escaped double-quote char
    public static final String COMPACT_JSON_EMPTY = "\\@empty";   // must not start with non-escaped double-quote char

    private static final String MSG_EXPECTED_BUT_NOT_FOUND = "Expected %s but found '%c' at position %d of string %s";
    private static final String MSG_EXPECTED_BUT_NOT_FOUND_STARTING = "Expected %s starting at position %d of string %s";
    private static final String MSG_CONSUMED_ALL_BUT_NOT_FOUND = "Consumed all characters but not found ending %s in string %s";
    private static final String MSG_UNEXPECTED_EXTRA_DATA = "Object successfully constructed but not all characters have been consumed";
    private static final String MSG_UNEXPECTED_END_OF_DATA = "Unexpected end of string: %s";

    private static final StringEscaper JSON_ESCAPER = new StringEscaper(ESCAPE_JSON::translate, UNESCAPE_JAVA::translate);
    private static final StringEscaper COMPACT_JSON_ESCAPER = new StringEscaper(ESCAPE_COMPACT_JSON::translate, UNESCAPE_COMPACT_JSON::translate);

    private final TypeConverter<Object> innerTypeConverter;

    /**
     * Compact mode is a JSON mode with omitted beginning and ending DOUBLE-QUOTEs for string values
     * (i.e. DOUBLE-QUOTEs in strings should (and must) not be escaped).
     */
    private final boolean defaultCompactMode;

    public JsonLikeConverter(TypeConverter<?> innerTypeConverter) {
        this(innerTypeConverter, true);
    }

    @SuppressWarnings("unchecked")
    public JsonLikeConverter(TypeConverter<?> innerTypeConverter, boolean compactMode) {
        this.innerTypeConverter = (TypeConverter<Object>) requireNonNull(innerTypeConverter, "innerTypeConverter must not be null");
        this.defaultCompactMode = compactMode;
    }

    @Override
    public boolean isApplicable(Type type, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class) {
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (List.class.isAssignableFrom(rawType) && actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    return isApplicable(itemType, attributes) || innerTypeConverter.isApplicable(itemType, attributes);
                } else if (Map.class.isAssignableFrom(rawType) && actualTypeArguments.length == 2) {
                    Type keyType = actualTypeArguments[0];
                    Type valueType = actualTypeArguments[1];
                    return (isApplicable(keyType, attributes) || innerTypeConverter.isApplicable(keyType, attributes))
                            && (isApplicable(valueType, attributes) || innerTypeConverter.isApplicable(valueType, attributes));
                }
            }
        }
        return false;
    }

    @Override
    public Object fromString(Type type, String value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }
        ObjectBuilder builder = new ObjectBuilder();
        char[] array = value.toCharArray();
        CharSequence charSequence = new Segment(array, 0, array.length);
        if (consumeItem(type, charSequence, 0, builder, isCompactMode(attributes)) != value.length()) {
            throw new IllegalArgumentException(MSG_UNEXPECTED_EXTRA_DATA);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString(Type type, Object value, Map<String, String> attributes) {
        requireNonNull(type, "type cannot be null");

        if (value == null) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        try {
            boolean compact = isCompactMode(attributes);
            StringBuilder out = new StringBuilder();
            return (List.class.isAssignableFrom(rawType)
                    ? listToString(parameterizedType, (List<Object>) value, out, compact)
                    : mapToString(parameterizedType, (Map<Object, Object>) value, out, compact)).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected List<Object> createList(ParameterizedType type) {
        return new ArrayList<>();
    }

    protected Map<Object, Object> createMap(ParameterizedType type) {
        return type.getRawType() instanceof Class && SortedMap.class.isAssignableFrom((Class<?>) type.getRawType())
                ? new TreeMap<>()
                : new LinkedHashMap<>();
    }

    private int listFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder, boolean compact) {
        try {
            return doListFromString(type, value, builder, compact);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(format(MSG_UNEXPECTED_END_OF_DATA, value), e);
        }
    }

    private int doListFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder, boolean compact) {
        Type itemType = type.getActualTypeArguments()[0];

        int current = 0;
        if (value.charAt(current++) != LEFT_SQUARE_BRACKET) {
            int consumed = consumeNull(value, --current, builder, compact);
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
            current += consumeItem(itemType, value, current, builder, compact);

            // Expected ',' or ']'
            if (value.charAt(current) != COMMA && value.charAt(current) != RIGHT_SQUARE_BRACKET) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                        "':' or ']'", value.charAt(current), current, value));
            }
            if (value.charAt(current) == COMMA) {
                current++;
                if (value.charAt(current) == RIGHT_SQUARE_BRACKET) {
                    // support for proper ,] sequence in compact mode
                    current += consumeItem(itemType, value, current, builder, compact);
                }
            }
        }
        throw new IllegalArgumentException(format(MSG_CONSUMED_ALL_BUT_NOT_FOUND, "']'", value));
    }

    private int mapFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder, boolean compact) {
        try {
            return doMapFromString(type, value, builder, compact);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(format(MSG_UNEXPECTED_END_OF_DATA, value), e);
        }
    }

    private int doMapFromString(ParameterizedType type, CharSequence value, ObjectBuilder builder, boolean compact) {
        Type keyType = type.getActualTypeArguments()[0];
        Type valueType = type.getActualTypeArguments()[1];

        int current = 0;
        if (value.charAt(current++) != LEFT_CURLY_BRACE) {
            int consumed = consumeNull(value, --current, builder, compact);
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
            current += consumeItem(keyType, value, current, keyBuilder, compact);
            builder.addKey(keyBuilder.build());

            // Consume ':'
            if (value.charAt(current++) != COLON) {
                throw new IllegalArgumentException(format(MSG_EXPECTED_BUT_NOT_FOUND,
                        "':'", value.charAt(--current), current, value));
            }

            // Consume Value
            current += consumeItem(valueType, value, current, builder, compact);

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

    private int consumeItem(Type type, CharSequence value, int current, ObjectBuilder builder, boolean compact) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return listFromString(parameterizedType, value.subSequence(current, value.length()), builder, compact);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return mapFromString(parameterizedType, value.subSequence(current, value.length()), builder, compact);
            }
        }

        // Process item as a string value
        if (compact) {
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
                    : innerTypeConverter.fromString(type, COMPACT_JSON_ESCAPER.unescape(emptyFound ? EMPTY_STRING : foundString), null));
            return found - current;
        }

        if (value.charAt(current) != DOUBLE_QUOTE) {
            int consumed = consumeNull(value, current, builder, compact);
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
        builder.addValue(innerTypeConverter.fromString(type, JSON_ESCAPER.unescape(foundString), null));
        return found - current + 1;
    }

    int consumeNull(CharSequence value, int current, ObjectBuilder builder, boolean compact) {
        int found = value.length();
        boolean consume = false;
        if (compact) {
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

    private Appendable listToString(ParameterizedType type, List<Object> value, Appendable out, boolean compact) throws IOException {
        if (value == null) {
            return compact
                    ? out.append(COMPACT_JSON_NULL)
                    : out.append(JSON_NULL);
        }

        out.append(LEFT_SQUARE_BRACKET);
        Type itemType = type.getActualTypeArguments()[0];
        Iterator<Object> iterator = value.iterator();

        // The only case to encode empty string in compact mode is one element List
        boolean encodeEmptyCompactString = value.size() == 1;
        while (iterator.hasNext()) {
            append(itemType, iterator.next(), out, encodeEmptyCompactString, compact);
            if (iterator.hasNext()) {
                out.append(COMMA);
            }
        }
        return out.append(RIGHT_SQUARE_BRACKET);
    }

    private Appendable mapToString(ParameterizedType type, Map<Object, Object> value, Appendable out, boolean compact) throws IOException {
        if (value == null) {
            return compact
                    ? out.append(COMPACT_JSON_NULL)
                    : out.append(JSON_NULL);
        }

        out.append(LEFT_CURLY_BRACE);
        Type keyType = type.getActualTypeArguments()[0];
        Type valueType = type.getActualTypeArguments()[1];
        Iterator<Entry<Object, Object>> iterator = value.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> entry = iterator.next();
            append(keyType, entry.getKey(), out, compact);
            out.append(COLON);
            append(valueType, entry.getValue(), out, compact);
            if (iterator.hasNext()) {
                out.append(COMMA);
            }
        }
        out.append(RIGHT_CURLY_BRACE);
        return out;
    }

    private Appendable append(Type type, Object value, Appendable appendable, boolean compact) throws IOException {
        return append(type, value, appendable, false, compact);
    }

    @SuppressWarnings("unchecked")
    private Appendable append(Type type, Object value, Appendable out, boolean encodeCompactEmptyString, boolean compact) throws IOException {
        if (value == null) {
            return out.append(compact
                    ? COMPACT_JSON_NULL
                    : JSON_NULL);
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return listToString(parameterizedType, (List<Object>) value, out, compact);
            } else if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return mapToString(parameterizedType, (Map<Object, Object>) value, out, compact);
            }
        }

        // Not a List/Map, treated as a string literal
        if (compact) {
            String valueStr = innerTypeConverter.toString(type, value, null);
            return out.append(encodeCompactEmptyString && EMPTY_STRING.equals(valueStr)
                    ? COMPACT_JSON_EMPTY
                    : COMPACT_JSON_ESCAPER.escape(valueStr));
        }
        return out
                .append(DOUBLE_QUOTE)
                .append(JSON_ESCAPER.escape(innerTypeConverter.toString(type, value, null)))
                .append(DOUBLE_QUOTE);
    }

    private boolean isCompactMode(Map<String, String> attributes) {
        String format = (attributes == null) ? null : attributes.get(FORMAT);
        if (format == null) {
            return defaultCompactMode;
        }
        switch (format) {
            case COMPACT:
                return true;
            case JSON:
                return false;
            default:
                throw new IllegalArgumentException(format(
                        "Invalid '%s' meta-attribute value, it must be either '%s' or '%s', but '%s' is provided.",
                        FORMAT, COMPACT, JSON, format));
        }
    }
}
