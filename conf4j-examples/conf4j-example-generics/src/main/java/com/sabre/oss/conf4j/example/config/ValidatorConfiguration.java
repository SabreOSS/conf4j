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

package com.sabre.oss.conf4j.example.config;

import com.sabre.oss.conf4j.annotation.AbstractConfiguration;
import com.sabre.oss.conf4j.annotation.DefaultValue;
import com.sabre.oss.conf4j.annotation.Description;
import com.sabre.oss.conf4j.annotation.Key;

/**
 * This configuration shows how to parametrized configurations.
 * <p>
 * Each validator configuration has <i>name</i> and <i>enabled</i> properties and set of associated constraints.
 * Constraints can be expressed as a separate type and provided via {@code C} parameter.
 * </p>
 *
 * @param <C> validation constraint type.
 */
@AbstractConfiguration
@Key
public interface ValidatorConfiguration<C> {
    @Key
    @Description("Validator name.")
    String getName();

    @Key
    @DefaultValue("true")
    @Description("Indicates if the validator is enabled.")
    boolean isEnabled();

    @Description("Validation constraints.")
    C getConstraints();
}
