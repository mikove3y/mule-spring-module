/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.spring.internal.security;

import static java.util.Collections.unmodifiableMap;

import org.mule.runtime.api.security.Authentication;

import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

public class SpringAuthenticationAdapter implements Authentication {

  private static final long serialVersionUID = -5906282218126929871L;

  private org.springframework.security.core.Authentication delegate;
  private Map<String, Object> properties;

  public SpringAuthenticationAdapter(org.springframework.security.core.Authentication authentication) {
    this(authentication, null);
  }

  public SpringAuthenticationAdapter(org.springframework.security.core.Authentication authentication,
                                     Map<String, Object> properties) {
    this.delegate = authentication;
    this.properties = properties != null ? unmodifiableMap(properties) : null;
  }

  public org.springframework.security.core.GrantedAuthority[] getAuthorities() {
    return delegate.getAuthorities().toArray(new GrantedAuthority[delegate.getAuthorities().size()]);
  }

  @Override
  public Object getCredentials() {
    return delegate.getCredentials();
  }

  public Object getDetails() {
    return delegate.getDetails();
  }

  @Override
  public Object getPrincipal() {
    return delegate.getPrincipal();
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object another) {
    return delegate.equals(another);
  }

  public String getName() {
    return delegate.getName();
  }

  public org.springframework.security.core.Authentication getDelegate() {
    return delegate;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public SpringAuthenticationAdapter setProperties(Map<String, Object> properties) {
    return new SpringAuthenticationAdapter(delegate, properties);
  }
}
