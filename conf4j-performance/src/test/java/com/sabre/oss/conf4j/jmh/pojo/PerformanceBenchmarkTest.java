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

package com.sabre.oss.conf4j.jmh.pojo;

import com.sabre.oss.conf4j.factory.ConfigurationFactory;
import com.sabre.oss.conf4j.factory.javassist.JavassistDynamicConfigurationFactory;
import com.sabre.oss.conf4j.jmh.JmhConfiguration;
import com.sabre.oss.conf4j.source.ConfigurationSource;
import com.sabre.oss.conf4j.source.MapConfigurationSource;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.RandomStringUtils.random;

public class PerformanceBenchmarkTest {

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.SampleTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(5))
                .threads(2)
                .forks(0)
                .syncIterations(true)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .jvmArgs("-Xms1G", "-Xmx1G", "-XX:MaxGCPauseMillis=10", "-XX:GCPauseIntervalMillis=100")
                .build();
        new Runner(opt).run();
    }

    public static void main(String... args) throws Exception {
        new PerformanceBenchmarkTest().launchBenchmark();
    }

    @Benchmark
    public void createConfigurationBenchmark(BenchmarkState state, Blackhole blackhole) {
        JmhConfiguration configInstance = state.factory.createConfiguration(JmhConfiguration.class, state.valueSource);
        blackhole.consume(configInstance);
    }

    @Benchmark
    public void accessConfigurationProperties(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.configInstance.getBooleanProperty());
        blackhole.consume(state.configInstance.getIntegerProperty());
        blackhole.consume(state.configInstance.getDoubleProperty());
        blackhole.consume(state.configInstance.getLongProperty());
        blackhole.consume(state.configInstance.isSimpleBooleanProperty());
        blackhole.consume(state.configInstance.getSimpleDoubleProperty());
        blackhole.consume(state.configInstance.getSimpleIntegerProperty());
        blackhole.consume(state.configInstance.getSimpleLongProperty());
        blackhole.consume(state.configInstance.getStringProperty());
        blackhole.consume(state.configInstance.getListOfStringsProperty());
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        ConfigurationSource valueSource;
        ConfigurationFactory factory;
        JmhConfiguration configInstance;

        @Setup(Level.Trial)
        public void initialize() {
            Map<String, String> properties = range(1, 1000).boxed().collect(toMap(i -> random(8), i -> random(256)));
            valueSource = new MapConfigurationSource(properties);
            factory = new JavassistDynamicConfigurationFactory();
            configInstance = this.factory.createConfiguration(JmhConfiguration.class, this.valueSource);
        }
    }
}
