/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.transformer.java;

import com.google.api.codegen.config.FieldConfig;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.MethodContext;
import com.google.api.codegen.config.ResourceNameMessageConfigs;
import com.google.api.codegen.config.ResourceNameType;
import com.google.api.codegen.transformer.DefaultFeatureConfig;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JavaFeatureConfig extends DefaultFeatureConfig {

  @Override
  public abstract boolean enableStringFormatFunctions();

  @Override
  public abstract boolean useStaticCreateMethodForOneofs();

  @Override
  public boolean resourceNameTypesEnabled() {
    return true;
  }

  @Override
  public boolean useResourceNameFormatOptionInSample(
      MethodContext context, FieldConfig fieldConfig) {
    boolean hasResourceNameFormatOption =
        resourceNameTypesEnabled()
            && fieldConfig != null
            && (fieldConfig.useResourceNameType() || fieldConfig.useResourceNameTypeInSampleOnly());

    if (!hasResourceNameFormatOption) {
      return false;
    }

    // For an any resource name, we choose a random single resource name defined in the API for
    // sample generation. If there are no single resource names at all in the API, we set this
    // value to false and use a string literal to instantiate a resource name string.
    boolean apiHasSingleResources =
        context.getProductConfig().getSingleResourceNameConfigs().iterator().hasNext();
    if (fieldConfig.getResourceNameType() == ResourceNameType.ANY && !apiHasSingleResources) {
      return false;
    }

    return !(context.isFlattenedMethodContext() && fieldConfig.getField().isRepeated());
  }

  @Override
  public boolean useResourceNameConvertersInSampleOnly(
      MethodContext context, FieldConfig fieldConfig) {
    return !resourceNameProtoAccessorsEnabled()
        && useResourceNameFormatOptionInSampleOnly(fieldConfig)
        && !(context.isFlattenedMethodContext() && fieldConfig.getField().isRepeated());
  }

  @Override
  public boolean useInheritanceForOneofs() {
    return true;
  }

  @Override
  public boolean enableMixins() {
    return true;
  }

  @Override
  public boolean enableRawOperationCallSettings() {
    return true;
  }

  public static Builder newBuilder() {
    return new AutoValue_JavaFeatureConfig.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder enableStringFormatFunctions(boolean value);

    abstract Builder useStaticCreateMethodForOneofs(boolean value);

    abstract JavaFeatureConfig build();
  }

  public static JavaFeatureConfig create(GapicProductConfig productConfig) {
    boolean enableStringFormatFunctions;

    if (productConfig.enableStringFormattingFunctionsOverride() != null) {
      enableStringFormatFunctions =
          productConfig.enableStringFormattingFunctionsOverride().booleanValue();
    } else {
      ResourceNameMessageConfigs resourceNameMessageConfigs =
          productConfig.getResourceNameMessageConfigs();
      enableStringFormatFunctions =
          resourceNameMessageConfigs == null || resourceNameMessageConfigs.isEmpty();
    }
    return JavaFeatureConfig.newBuilder()
        .enableStringFormatFunctions(enableStringFormatFunctions)
        .useStaticCreateMethodForOneofs(productConfig.getProtoParser().isProtoAnnotationsEnabled())
        .build();
  }
}
