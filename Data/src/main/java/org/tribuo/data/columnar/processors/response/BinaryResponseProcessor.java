/*
 * Copyright (c) 2015-2020, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tribuo.data.columnar.processors.response;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.ConfigurableName;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;
import org.tribuo.Output;
import org.tribuo.OutputFactory;
import org.tribuo.data.columnar.ResponseProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *  A {@link ResponseProcessor} that takes a single value of the
 *  field as the positive class and all other values as the negative
 *  class.
 */
public class BinaryResponseProcessor<T extends Output<T>> implements ResponseProcessor<T> {

    @Config(description="The field name to read, you should use only one of this or fieldNames")
    @Deprecated
    private String fieldName;

    @Config(description="The string which triggers a positive response.")
    private String positiveResponse;

    @Config(mandatory = true, description="Output factory to use to create the response.")
    private OutputFactory<T> outputFactory;

    public static final String POSITIVE_NAME = "1";
    public static final String NEGATIVE_NAME = "0";

    @Config(description="The positive response to emit.")
    private String positiveName = POSITIVE_NAME;

    @Config(description="The negative response to emit.")
    private String negativeName = NEGATIVE_NAME;

    @Config(description = "A list of field names to read, you should use only one of this or fieldName.")
    private List<String> fieldNames;

    @Config(description = "A list of strings that trigger positive responses; it should be the same length as fieldNames or empty")
    private List<String> positiveResponses;

    @Config(description = "Whether to display field names as part of the generated output, defaults to false")
    private boolean displayField;

    @ConfigurableName
    private String configName;

    @Override
    public void postConfig() {
        if (fieldName != null && fieldNames != null) { // we can only have one path
            throw new PropertyException(configName, "fieldName, FieldNames", "only one of fieldName or fieldNames can be populated");
        } else if (fieldNames != null) {
            if(positiveResponse != null) {
                positiveResponses = positiveResponses == null ? Collections.nCopies(fieldNames.size(), positiveResponse) : positiveResponses;
                if(positiveResponses.size() != fieldNames.size()) {
                    throw new PropertyException(configName, "positiveResponses", "must either be empty or match the length of fieldNames");
                }
            } else {
                throw new PropertyException(configName, "positiveResponse, positiveResponses", "one of positiveResponse or positiveResponses must be populated");
            }
        } else if (fieldName != null)  {
            if(positiveResponses != null) {
                throw new PropertyException(configName, "positiveResponses", "if fieldName is populated, positiveResponses must be blank");
            }
            fieldNames = Collections.singletonList(fieldName);
            if(positiveResponse != null) {
                positiveResponses = Collections.singletonList(positiveResponse);
            } else {
                throw new PropertyException(configName, "positiveResponse", "if fieldName is populated positiveResponse must be populated");
            }
        } else {
            throw new PropertyException(configName, "fieldName, fieldNames", "One of fieldName or fieldNames must be populated");
        }
    }

    /**
     * for OLCUT.
     */
    private BinaryResponseProcessor() {}

    /**
     * Constructs a binary response processor which emits a positive value for a single string
     * and a negative value for all other field values. Defaults to {@link #POSITIVE_NAME} for positive outputs and {@link #NEGATIVE_NAME}
     * for negative outputs.
     * @param fieldName The field name to read.
     * @param positiveResponse The positive response to look for.
     * @param outputFactory The output factory to use.
     */
    public BinaryResponseProcessor(String fieldName, String positiveResponse, OutputFactory<T> outputFactory) {
        this(Collections.singletonList(fieldName), positiveResponse, outputFactory);
    }

    /**
     * Constructs a binary response processor which emits a positive value for a single string
     * and a negative value for all other field values. Defaults to {@link #POSITIVE_NAME} for positive outputs and {@link #NEGATIVE_NAME}
     * for negative outputs.
     * @param fieldNames The field names to read.
     * @param positiveResponse The positive response to look for.
     * @param outputFactory The output factory to use.
     */
    public BinaryResponseProcessor(List<String> fieldNames, String positiveResponse, OutputFactory<T> outputFactory) {
        this(fieldNames, Collections.nCopies(fieldNames.size(), positiveResponse), outputFactory);
    }

    /**
     * Constructs a binary response processor which emits a positive value for a single string
     * and a negative value for all other field values. The lengths of fieldNames and positiveResponses
     * must be the same. Defaults to {@link #POSITIVE_NAME} for positive outputs and {@link #NEGATIVE_NAME}
     * for negative outputs.
     * @param fieldNames The field names to read.
     * @param positiveResponses The positive responses to look for.
     * @param outputFactory The output factory to use.
     */
    public BinaryResponseProcessor(List<String> fieldNames, List<String> positiveResponses, OutputFactory<T> outputFactory) {
        this(fieldNames, positiveResponses, outputFactory, false);
    }

    /**
     * Constructs a binary response processor which emits a positive value for a single string
     * and a negative value for all other field values. The lengths of fieldNames and positiveResponses
     * must be the same. Defaults to {@link #POSITIVE_NAME} for positive outputs and {@link #NEGATIVE_NAME}
     * for negative outputs.
     * @param fieldNames The field names to read.
     * @param positiveResponses The positive responses to look for.
     * @param outputFactory The output factory to use.
     * @param displayField whether to include field names in the generated labels.
     */
    public BinaryResponseProcessor(List<String> fieldNames, List<String> positiveResponses, OutputFactory<T> outputFactory, boolean displayField) {
        this(fieldNames, positiveResponses, outputFactory, POSITIVE_NAME, NEGATIVE_NAME, displayField);
    }

    /**
     * Constructs a binary response processor which emits a positive value for a single string
     * and a negative value for all other field values. The lengths of fieldNames and positiveResponses
     * must be the same.
     * @param fieldNames The field names to read.
     * @param positiveResponses The positive responses to look for.
     * @param outputFactory The output factory to use.
     * @param positiveName The value of a 'positive' output
     * @param negativeName the value of a 'negative' output
     * @param displayField whether to include field names in the generated labels.
     */
    public BinaryResponseProcessor(List<String> fieldNames, List<String> positiveResponses, OutputFactory<T> outputFactory, String positiveName, String negativeName, boolean displayField) {
        if(fieldNames.size() != positiveResponses.size()) {
            throw new IllegalArgumentException("fieldNames and positiveResponses must be the same length");
        }
        this.fieldNames = fieldNames;
        this.positiveResponses = positiveResponses;
        this.outputFactory = outputFactory;
        this.positiveName = positiveName;
        this.negativeName = negativeName;
        this.displayField = displayField;
    }

    @Override
    public OutputFactory<T> getOutputFactory() {
        return outputFactory;
    }

    @Deprecated
    @Override
    public String getFieldName() {
        return fieldNames.get(0);
    }

    @Deprecated
    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Deprecated
    @Override
    public Optional<T> process(String value) {
        return process(Collections.singletonList(value));
    }

    @Override
    public Optional<T> process(List<String> values) {
        if(values.size() != fieldNames.size()) {
            throw new IllegalArgumentException("values must have the same length as fieldNames. Got values: " + values.size() + " fieldNames: "  + fieldNames.size());
        }
        List<String> responses = new ArrayList<>();
        String prefix = "";
        for(int i=0; i < fieldNames.size(); i++) {
            if(displayField) {
                prefix = fieldNames.get(i) + "=";
            }
            responses.add(prefix + (positiveResponses.get(i).equals(values.get(i)) ? positiveName : negativeName));
        }
        return Optional.of(outputFactory.generateOutput(fieldNames.size() == 1 ? responses.get(0) : responses));
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public String toString() {
        return "BinaryResponseProcessor(fieldNames="+ fieldNames.toString() +", positiveResponses="+ positiveResponses.toString() +", positiveName="+positiveName +", negativeName="+negativeName+")";
    }

    @Override
    public ConfiguredObjectProvenance getProvenance() {
        return new ConfiguredObjectProvenanceImpl(this,"ResponseProcessor");
    }
}
