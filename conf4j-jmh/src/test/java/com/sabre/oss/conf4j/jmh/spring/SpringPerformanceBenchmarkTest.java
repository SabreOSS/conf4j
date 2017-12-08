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

package com.sabre.oss.conf4j.jmh.spring;

import com.sabre.oss.conf4j.jmh.JmhConfiguration;
import com.sabre.oss.conf4j.spring.annotation.ConfigurationScan;
import com.sabre.oss.conf4j.spring.annotation.EnableConf4j;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SpringPerformanceBenchmarkTest {
    ConfigurableApplicationContext applicationContext;
    JmhConfiguration configuration;

    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.SampleTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(3))
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
        new SpringPerformanceBenchmarkTest().launchBenchmark();
    }

    @Setup
    public void initialize() {
        applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        configuration = applicationContext.getBean(JmhConfiguration.class);
    }

    @TearDown
    public void tearDown() {
        applicationContext.close();
    }

    @Benchmark
    public void accessConfigurationProperties(Blackhole blackhole) {
        blackhole.consume(configuration.getBooleanProperty());
        blackhole.consume(configuration.getDoubleProperty());
        blackhole.consume(configuration.getIntegerProperty());
        blackhole.consume(configuration.getLongProperty());
        blackhole.consume(configuration.isSimpleBooleanProperty());
        blackhole.consume(configuration.getSimpleDoubleProperty());
        blackhole.consume(configuration.getSimpleIntegerProperty());
        blackhole.consume(configuration.getSimpleLongProperty());
        blackhole.consume(configuration.getStringProperty());
        blackhole.consume(configuration.getListOfStringsProperty());
    }

    @EnableConf4j
    @ConfigurationScan(basePackageClasses = JmhConfiguration.class)
    @PropertySource("classpath:spring/conf4j.test.properties")
    static class SpringConfiguration {
    }
}
