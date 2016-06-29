/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.query.ast;

import java.util.List;
import java.util.Set;

import javax.jcr.PropertyType;

import org.apache.jackrabbit.oak.api.PropertyValue;
import org.apache.jackrabbit.oak.query.index.FilterImpl;
import org.apache.jackrabbit.oak.spi.query.PropertyValues;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.apache.jackrabbit.oak.api.Type.STRING;
import static org.apache.jackrabbit.oak.api.Type.STRINGS;

/**
 * The function "lower(..)".
 */
public class LowerCaseImpl extends DynamicOperandImpl {

    private final DynamicOperandImpl operand;

    public LowerCaseImpl(DynamicOperandImpl operand) {
        this.operand = operand;
    }

    public DynamicOperandImpl getOperand() {
        return operand;
    }

    @Override
    boolean accept(AstVisitor v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "lower(" + operand + ')';
    }
    
    @Override
    public PropertyExistenceImpl getPropertyExistence() {
        return operand.getPropertyExistence();
    }
    
    @Override
    public Set<SelectorImpl> getSelectors() {
        return operand.getSelectors();
    }

    @Override
    public PropertyValue currentProperty() {
        PropertyValue p = operand.currentProperty();
        if (p == null) {
            return null;
        }
        // TODO toLowerCase(): document the Turkish locale problem
        if (p.getType().isArray()) {
            Iterable<String> lowerCase = transform(p.getValue(STRINGS),
                    new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            return input.toLowerCase();
                        }
                    });
            return PropertyValues.newString(lowerCase);
        } else {
            String value = p.getValue(STRING);
            return PropertyValues.newString(value.toLowerCase());
        }
    }

    @Override
    public void restrict(FilterImpl f, Operator operator, PropertyValue v) {
        // LOWER(x) implies x is not null
        operand.restrict(f, Operator.NOT_EQUAL, null);
        operand.restrictFunction(f, "lower", operator, v);
    }
    
    @Override
    public void restrictList(FilterImpl f, List<PropertyValue> list) {
        // "LOWER(x) IN (A, B)" implies x is not null
        operand.restrict(f, Operator.NOT_EQUAL, null);
    }

    @Override
    public void restrictFunction(FilterImpl f, String functionName, Operator operator, PropertyValue v) {
        // optimizations of the type "lower(upper(x)) = 'x'" are not currently supported
    }

    @Override
    public boolean canRestrictSelector(SelectorImpl s) {
        return operand.canRestrictSelector(s);
    }
    
    @Override
    int getPropertyType() {
        return PropertyType.STRING;
    }

    @Override
    public DynamicOperandImpl createCopy() {
        return new LowerCaseImpl(operand.createCopy());
    }

}