/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.spring.internal;

import static java.lang.String.format;
import static org.mule.metadata.java.api.JavaTypeLoader.JAVA;
import static org.mule.runtime.api.meta.Category.COMMUNITY;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;
import static org.mule.runtime.api.meta.model.error.ErrorModelBuilder.newError;
import static org.mule.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.ANY;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.NOT_PERMITTED;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.SERVER_SECURITY;

import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExternalLibraryModel;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.error.ErrorModel;
import org.mule.runtime.core.api.security.SecurityProvider;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

/**
 * Spring module {@link org.mule.runtime.api.meta.model.ExtensionModel} generator.
 *
 * @since 1.0
 */
public class SpringModuleExtensionModelGenerator implements ExtensionLoadingDelegate {

  public static final String EXTENSION_NAME = "Spring";
  public static final String PREFIX_NAME = "spring";
  public static final String EXTENSION_DESCRIPTION = "Spring Module Plugin";
  public static final String VENDOR = "Mulesoft";
  public static final String VERSION = "1.0.0-SNAPSHOT";
  public static final MuleVersion MIN_MULE_VERSION = new MuleVersion("4.0");
  public static final String XSD_FILE_NAME = "mule-spring.xsd";
  private static final String UNESCAPED_LOCATION_PREFIX = "http://";
  private static final String SCHEMA_LOCATION = "www.mulesoft.org/schema/mule/spring";
  private static final String SCHEMA_VERSION = "current";
  private static final String SPRING_VERSION = "4.1.9.RELEASE";
  private static final String SPRING_SECURITY_VERSION = "4.0.4.RELEASE";
  private static final String SPRING_GROUP_ID = "org.springframework";
  private static final String SPRING_SECURITY_GROUP_ID = "org.springframework.security";

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext extensionLoadingContext) {
    XmlDslModel xmlDslModel = XmlDslModel.builder()
        .setPrefix(PREFIX_NAME)
        .setXsdFileName(XSD_FILE_NAME)
        .setSchemaVersion(VERSION)
        .setSchemaLocation(format("%s/%s/%s", UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION, SCHEMA_VERSION, XSD_FILE_NAME))
        .setNamespace(UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION)
        .build();

    ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
    BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);

    extensionDeclarer.named(EXTENSION_NAME)
        .describedAs(EXTENSION_DESCRIPTION)
        .fromVendor(VENDOR)
        .onVersion(VERSION)
        .withCategory(COMMUNITY)
        .withXmlDsl(xmlDslModel);

    // config
    final ConfigurationDeclarer springConfig = extensionDeclarer.withConfig("config")
        .describedAs("Spring configuration that allows to define a set of spring XML files and create an application context with objects to be used in the mule artifact.");
    ParameterGroupDeclarer parameterGroupDeclarer = springConfig.onDefaultParameterGroup();
    parameterGroupDeclarer.withRequiredParameter("files").withExpressionSupport(NOT_SUPPORTED)
        .withRole(BEHAVIOUR).ofType(typeLoader.load(String.class));

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Beans")
        .withDescription("Spring Beans (http://projects.spring.io/spring-framework). Based on the application usage of the Spring Framework, other spring dependencies may be required.")
        .withCoordinates(SPRING_GROUP_ID + ":spring-beans:" + SPRING_VERSION)
        .withType(DEPENDENCY).build());

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Context")
        .withCoordinates(SPRING_GROUP_ID + ":spring-context:" + SPRING_VERSION)
        .withDescription("Spring Context (http://projects.spring.io/spring-framework). Based on the application usage of the Spring Framework, other spring dependencies may be required.")
        .withType(DEPENDENCY).build());

    // spring-security
    final ConfigurationDeclarer securityManager = extensionDeclarer.withConfig("security-manager")
        .describedAs("This is the security provider type that is used to configure spring-security related functionality.");
    securityManager.onDefaultParameterGroup().withRequiredParameter("providers").withExpressionSupport(NOT_SUPPORTED)
        .withRole(BEHAVIOUR).ofType(typeBuilder.arrayType().of(typeLoader.load(SecurityProvider.class)).build());

    ErrorModel anyError = newError(ANY).build();
    final OperationDeclarer authorizationFilter = extensionDeclarer.withOperation("authorization-filter")
        .describedAs("Authorize users against a required set of authorities.")
        .withErrorModel(newError(SERVER_SECURITY).withParent(anyError).build())
        .withErrorModel(newError(NOT_PERMITTED).withParent(anyError).build());

    authorizationFilter.withOutput().ofType(typeBuilder.voidType().build());
    authorizationFilter.withOutputAttributes().ofType(typeBuilder.voidType().build());
    authorizationFilter.onDefaultParameterGroup().withRequiredParameter("requiredAuthorities")
        .withExpressionSupport(NOT_SUPPORTED)
        .withRole(BEHAVIOUR).ofType(typeLoader.load(String.class));

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Security Core")
        .withCoordinates(SPRING_SECURITY_GROUP_ID + ":spring-security-core:" + SPRING_SECURITY_VERSION)
        .withDescription("Spring Security Context (http://spring.io/spring-security). Based on the application usage of the Spring Framework, other spring/spring-security dependencies may be required.")
        .withType(DEPENDENCY).build());

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Security Config")
        .isOptional(true)
        .withCoordinates(SPRING_SECURITY_GROUP_ID + ":spring-security-config:" + SPRING_SECURITY_VERSION)
        .withDescription("Spring Security Config (http://spring.io/spring-security). Based on the application usage of the Spring Framework, other spring/spring-security dependencies may be required.")
        .withType(DEPENDENCY).build());

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Security Web")
        .isOptional(true)
        .withCoordinates(SPRING_SECURITY_GROUP_ID + ":spring-security-web:" + SPRING_SECURITY_VERSION)
        .withDescription("Spring Security Web (http://spring.io/spring-security). Based on the application usage of the Spring Framework, other spring/spring-security dependencies may be required.")
        .withType(DEPENDENCY).build());

    extensionDeclarer.withExternalLibrary(ExternalLibraryModel.builder()
        .withName("Spring Security LDAP")
        .isOptional(true)
        .withCoordinates(SPRING_SECURITY_GROUP_ID + ":spring-security-ldap:" + SPRING_SECURITY_VERSION)
        .withDescription("Spring Security LDAP (http://spring.io/spring-security). Based on the application usage of the Spring Framework, other spring/spring-security dependencies may be required.")
        .withType(DEPENDENCY).build());
  }
}
