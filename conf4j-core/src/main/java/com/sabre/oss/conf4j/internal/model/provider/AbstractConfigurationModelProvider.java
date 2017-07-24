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

package com.sabre.oss.conf4j.internal.model.provider;

import com.sabre.oss.conf4j.internal.model.ConfigurationModel;
import com.sabre.oss.conf4j.internal.model.ConfigurationModelProvider;
import com.sabre.oss.conf4j.internal.model.PropertyModel;
import com.sabre.oss.conf4j.internal.utils.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.sabre.oss.conf4j.internal.utils.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Base class which simplifies implementation of {@link ConfigurationModelProvider}.
 * It provides caching capability, and default validations.
 */
public abstract class AbstractConfigurationModelProvider implements ConfigurationModelProvider {
    protected final ConcurrentMap<Class<?>, ConfigurationModel> modelCache = new ConcurrentReferenceHashMap<>(64, SOFT);
    protected final ConcurrentMap<Class<?>, Boolean> isConfigurationCache = new ConcurrentReferenceHashMap<>(64, SOFT);

    protected final MetadataExtractor metadataExtractor;
    protected final MethodsProvider methodsProvider = new MethodsProvider();
    protected List<PropertyMethodParser<?>> methodParsers;
    private final CycleDetector cycleDetector = new CycleDetector();

    protected AbstractConfigurationModelProvider(MetadataExtractor metadataExtractor) {
        this.metadataExtractor = requireNonNull(metadataExtractor, "metadataExtractor cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationType(Class<?> type) {
        requireNonNull(type, "type cannot be null");

        return isConfigurationCache.computeIfAbsent(type, (key) -> metadataExtractor.isConfigurationClass(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationModel getConfigurationModel(Class<?> configurationType) {
        requireNonNull(configurationType, "configurationType cannot be null");

        return modelCache.computeIfAbsent(configurationType, (key) -> parseConfiguration(configurationType));
    }

    protected ConfigurationModel parseConfiguration(Class<?> configurationType) {
        requireNonNull(configurationType, "configurationType cannot be null");

        if (!isConfigurationType(configurationType)) {
            throw new IllegalArgumentException(format("%s is not a configuration type", configurationType));
        }

        List<PropertyModel> properties;

        try (CycleDetector.Handle handle = cycleDetector.push(configurationType)) {
            Collection<Method> methods = methodsProvider.getAllDeclaredMethods(configurationType);
            properties = methods.stream()
                    .map(handle::method)
                    .map(method -> parseProperty(configurationType, method))
                    .filter(Objects::nonNull)
                    .collect(toList());
        }

        ConfigurationModel configurationModel = parseConfiguration(configurationType, properties);
        checkConfigurationModel(configurationModel);

        return configurationModel;
    }

    protected void checkConfigurationModel(ConfigurationModel configurationModel) {
        List<String> duplicatedProperties = configurationModel.getProperties().stream()
                .collect(Collectors.groupingBy(PropertyModel::getPropertyName))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey() + e.getValue().stream().map(p -> p.getMethod().toString()).collect(joining(", ")))
                .collect(toList());

        if (!duplicatedProperties.isEmpty()) {
            throw new IllegalArgumentException(format("%s configuration type has duplicated properties: %s",
                    configurationModel.getConfigurationType(), duplicatedProperties));
        }
    }

    protected PropertyModel parseProperty(Class<?> configurationType, Method method) {
        for (PropertyMethodParser<?> parser : methodParsers) {
            if (parser.applies(configurationType, method)) {
                return parser.process(configurationType, method);
            }
        }
        throw new IllegalArgumentException("Unable to find parser for property method " + method);
    }

    protected ConfigurationModel parseConfiguration(Class<?> configurationType, List<PropertyModel> properties) {
        return new ConfigurationModel(
                configurationType,
                metadataExtractor.getDescription(configurationType),
                metadataExtractor.isAbstractConfiguration(configurationType),
                metadataExtractor.getPrefixes(configurationType),
                metadataExtractor.getCustomAttributes(configurationType),
                properties);
    }

    /**
     * Detects cycles in the configurations.
     */
    private static class CycleDetector {
        private static final ThreadLocal<Deque<Holder>> threadLocal = new ThreadLocal<>();

        Handle push(Class<?> configurationType) {
            Deque<Holder> deque = threadLocal.get();
            if (deque == null) {
                deque = new LinkedList<>();
                threadLocal.set(deque);
            }

            if (deque.stream().anyMatch(d -> d.configurationType.equals(configurationType))) {
                String path = deque.stream()
                        .map(d -> d.currentMethod.toString())
                        .collect(joining(" -> "));

                throw new IllegalArgumentException("Cycle between configurations detected for " + configurationType + " via " + path);
            }
            deque.addLast(new Holder(configurationType));
            return new Handle();
        }

        private static class Handle implements AutoCloseable {
            @Override
            public void close() {
                Deque<Holder> deque = threadLocal.get();
                deque.removeLast();
                if (deque.isEmpty()) {
                    threadLocal.remove();
                }
            }

            public Method method(Method method) {
                threadLocal.get().getLast().currentMethod = method;
                return method;
            }
        }

        private static class Holder {
            private final Class<?> configurationType;
            private Method currentMethod;

            private Holder(Class<?> configurationType) {
                this.configurationType = configurationType;
            }
        }
    }
}
