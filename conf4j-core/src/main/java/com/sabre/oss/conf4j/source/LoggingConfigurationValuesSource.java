/*
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

package com.sabre.oss.conf4j.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Configuration value source which logs every access to the configuration. Useful for debugging purposes.
 */
public class LoggingConfigurationValuesSource implements ConfigurationValuesSource {
    private static final Logger log = LoggerFactory.getLogger(LoggingConfigurationValuesSource.class);

    private final ConfigurationValuesSource source;

    /**
     * Construct value source and delegates all invocations to {@code source}.
     *
     * @param source value source all invocations are delegated.
     */
    public LoggingConfigurationValuesSource(ConfigurationValuesSource source) {
        this.source = requireNonNull(source, "source cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalValue<String> getValue(String key) {
        OptionalValue<String> value = source.getValue(key);
        log.info("{}={}", key, value);
        return value;
    }

    @Override
    public OptionalValue<String> getValue(String key, Attributes attributes) {
        OptionalValue<String> value = source.getValue(key, attributes);

        log.info("{}={} {}", new Object[]{key, value, attributes});
        return value;
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys) {
        ConfigurationEntry entry = source.findEntry(keys);
        if (log.isInfoEnabled()) {
            log.info("{}=[{}]", join(keys, ", "), entry);
        }
        return entry;
    }

    @Override
    public ConfigurationEntry findEntry(Collection<String> keys, Attributes attributes) {
        ConfigurationEntry entry = source.findEntry(keys, attributes);
        if (log.isInfoEnabled()) {
            log.info("{}=[{}] {}", new Object[]{join(keys, ", "), entry, Objects.toString(attributes, EMPTY)});
        }
        return entry;
    }
}
