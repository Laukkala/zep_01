/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teragrep.zep_01.realm;

import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.apache.shiro.realm.ldap.DefaultLdapContextFactory;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * A {@link org.apache.shiro.realm.Realm} that authenticates with an active directory LDAP
 * server to determine the roles for a particular user.  This implementation
 * queries for the user's groups and then maps the group names to roles using the
 * {@link #groupRolesMap}.
 *
 * @since 0.1
 */
public class ActiveDirectoryGroupRealm extends AbstractLdapRealm {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveDirectoryGroupRealm.class);

  private static final String ROLE_NAMES_DELIMETER = ",";

  private static final String KEYSTORE_PASS = "activeDirectoryRealm.systemPassword";

  private String userSearchAttributeName = "sAMAccountName";

  private String hadoopSecurityCredentialPath;

  public String getUserSearchAttributeName() {
    return userSearchAttributeName;
  }

  public void setUserSearchAttributeName(String userSearchAttributeName) {
    this.userSearchAttributeName = userSearchAttributeName;
  }


  public void setHadoopSecurityCredentialPath(String hadoopSecurityCredentialPath) {
    this.hadoopSecurityCredentialPath = hadoopSecurityCredentialPath;
  }

  /**
   * Mapping from fully qualified active directory
   * group names (e.g. CN=Group,OU=Company,DC=MyDomain,DC=local)
   * as returned by the active directory LDAP server to role names.
   */
  private final Map<String, String> groupRolesMap = new LinkedHashMap<>();

  public void setGroupRolesMap(Map<String, String> groupRolesMap) {
    this.groupRolesMap.putAll(groupRolesMap);
  }

  LdapContextFactory ldapContextFactory;

  @Override
  protected void onInit() {
    super.onInit();
    this.getLdapContextFactory();
  }

  public LdapContextFactory getLdapContextFactory() {
    if (this.ldapContextFactory == null) {
      LOGGER.debug("No LdapContextFactory specified - creating a default instance.");
      DefaultLdapContextFactory defaultFactory = new DefaultLdapContextFactory();
      defaultFactory.setPrincipalSuffix(this.principalSuffix);
      defaultFactory.setSearchBase(this.searchBase);
      defaultFactory.setUrl(this.url);
      defaultFactory.setSystemUsername(this.systemUsername);
      defaultFactory.setSystemPassword(getSystemPassword());
      this.ldapContextFactory = defaultFactory;
    }

    return this.ldapContextFactory;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
          throws AuthenticationException {
    try {
      return this.queryForAuthenticationInfo(token,
          this.getLdapContextFactory());
    } catch (javax.naming.AuthenticationException var5) {
      throw new AuthenticationException("LDAP authentication failed.", var5);
    } catch (NamingException var6) {
      String msg = "LDAP naming error while attempting to authenticate user.";
      throw new AuthenticationException(msg, var6);
    }
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    try {
      return this.queryForAuthorizationInfo(principals,
          this.getLdapContextFactory());
    } catch (NamingException var5) {
      String msg = "LDAP naming error while attempting to " +
          "retrieve authorization for user [" + principals + "].";
      throw new AuthorizationException(msg, var5);
    }
  }

  private String getSystemPassword() {
    String password;
    if (StringUtils.isEmpty(this.hadoopSecurityCredentialPath)) {
      password = this.systemPassword;
    } else {
      password = LdapRealm.getSystemPassword(hadoopSecurityCredentialPath, KEYSTORE_PASS);
    }
    return password;
  }

  /**
   * Builds an {@link AuthenticationInfo} object by querying the active directory LDAP context for
   * the specified username.  This method binds to the LDAP server using the provided username
   * and password - which if successful, indicates that the password is correct.
   * <p/>
   * This method can be overridden by subclasses to query the LDAP server in a more complex way.
   *
   * @param token              the authentication token provided by the user.
   * @param ldapContextFactory the factory used to build connections to the LDAP server.
   * @return an {@link AuthenticationInfo} instance containing information retrieved from LDAP.
   * @throws NamingException if any LDAP errors occur during the search.
   */
  @Override
  protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token,
          LdapContextFactory ldapContextFactory) throws NamingException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;

    // Binds using the username and password provided by the user.
    LdapContext ctx = null;
    try {
      String userPrincipalName = upToken.getUsername();
      if (!isValidPrincipalName(userPrincipalName)) {
        return null;
      }
      if (this.principalSuffix != null && userPrincipalName.indexOf('@') < 0) {
        userPrincipalName = upToken.getUsername() + this.principalSuffix;
      }
      ctx = ldapContextFactory.getLdapContext(
          userPrincipalName, upToken.getPassword());
    } finally {
      LdapUtils.closeContext(ctx);
    }

    return buildAuthenticationInfo(upToken.getUsername(), upToken.getPassword());
  }

  private boolean isValidPrincipalName(String userPrincipalName) {
    if (StringUtils.isEmpty(userPrincipalName)) {
      return false;
    }
    if (userPrincipalName.contains("@")) {
      String userPrincipalWithoutDomain = userPrincipalName.split("@")[0].trim();
      return StringUtils.isNotEmpty(userPrincipalWithoutDomain);
    }
    return true;
  }

  protected AuthenticationInfo buildAuthenticationInfo(String username, char[] password) {
    if (this.principalSuffix != null && username.indexOf('@') > 1) {
      username = username.split("@")[0];
    }
    return new SimpleAuthenticationInfo(username, password, getName());
  }

  /**
   * Builds an {@link org.apache.shiro.authz.AuthorizationInfo} object by querying the active
   * directory LDAP context for the groups that a user is a member of.  The groups are then
   * translated to role names by using the configured {@link #groupRolesMap}.
   * <p/>
   * This implementation expects the <tt>principal</tt> argument to be a String username.
   * <p/>
   * Subclasses can override this method to determine authorization data (roles, permissions, etc)
   * in a more complex way.  Note that this default implementation does not support permissions,
   * only roles.
   *
   * @param principals         the principal of the Subject whose account is being retrieved.
   * @param ldapContextFactory the factory used to create LDAP connections.
   * @return the AuthorizationInfo for the given Subject principal.
   * @throws NamingException if an error occurs when searching the LDAP server.
   */
  @Override
  protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
          LdapContextFactory ldapContextFactory) throws NamingException {
    String username = (String) getAvailablePrincipal(principals);

    // Perform context search
    LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();

    Set<String> roleNames;

    try {
      roleNames = getRoleNamesForUser(username, ldapContext);
    } finally {
      LdapUtils.closeContext(ldapContext);
    }

    return buildAuthorizationInfo(roleNames);
  }

  protected AuthorizationInfo buildAuthorizationInfo(Set<String> roleNames) {
    return new SimpleAuthorizationInfo(roleNames);
  }

  public List<String> searchForUserName(String containString, LdapContext ldapContext,
      int numUsersToFetch)
          throws NamingException {
    List<String> userNameList = new ArrayList<>();

    SearchControls searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchCtls.setCountLimit(numUsersToFetch);

    String searchFilter = String.format("(&(objectClass=*)(%s=*%s*))", this.getUserSearchAttributeName(), containString);

    Object[] searchArguments = new Object[]{containString};

    NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, searchFilter,
        searchArguments,
        searchCtls);

    while (answer.hasMoreElements()) {
      SearchResult sr = answer.next();

      LOGGER.debug("Retrieving userprincipalname names for user [{}]", sr.getName());

      Attributes attrs = sr.getAttributes();
      if (attrs != null) {
        NamingEnumeration<? extends Attribute> ae = attrs.getAll();
        while (ae.hasMore()) {
          Attribute attr = ae.next();
          if (attr.getID().equalsIgnoreCase(this.getUserSearchAttributeName())) {
            userNameList.addAll(LdapUtils.getAllAttributeValues(attr));
          }
        }
      }
    }
    return userNameList;
  }

  public Map<String, String> getListRoles() {
    Map<String, String> roles = new HashMap<>();
    for (Map.Entry<String, String> pair : groupRolesMap.entrySet()) {
      roles.put(pair.getValue(), "*");
    }
    return roles;
  }

  private Set<String> getRoleNamesForUser(String username, LdapContext ldapContext)
          throws NamingException {
    Set<String> roleNames = new LinkedHashSet<>();

    SearchControls searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    String userPrincipalName = username;
    if (this.principalSuffix != null && userPrincipalName.indexOf('@') > 1) {
      userPrincipalName = userPrincipalName.split("@")[0];
    }

    String searchFilter = String.format("(&(objectClass=*)(%s=%s))", this.getUserSearchAttributeName(), userPrincipalName);
    Object[] searchArguments = new Object[]{userPrincipalName};

    NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, searchFilter, searchArguments,
        searchCtls);

    while (answer.hasMoreElements()) {
      SearchResult sr = answer.next();

      LOGGER.debug("Retrieving group names for user [{}]", sr.getName());

      Attributes attrs = sr.getAttributes();

      if (attrs != null) {
        NamingEnumeration<? extends Attribute> ae = attrs.getAll();
        while (ae.hasMore()) {
          Attribute attr = ae.next();

          if (attr.getID().equals("memberOf")) {
            Collection<String> groupNames = LdapUtils.getAllAttributeValues(attr);
            LOGGER.debug("Groups found for user [{}]: {}", username, groupNames);
            roleNames.addAll(getRoleNamesForGroups(groupNames));
          }
        }
      }
    }
    return roleNames;
  }

  /**
   * This method is called by the default implementation to translate Active Directory group names
   * to role names.  This implementation uses the {@link #groupRolesMap} to map group names to role
   * names.
   *
   * @param groupNames the group names that apply to the current user.
   * @return a collection of roles that are implied by the given role names.
   */
  protected Collection<String> getRoleNamesForGroups(Collection<String> groupNames) {
    Set<String> roleNames = new HashSet<>(groupNames.size());

    for (String groupName : groupNames) {
      String strRoleNames = groupRolesMap.get(groupName);
      if (strRoleNames != null) {
        for (String roleName : strRoleNames.split(ROLE_NAMES_DELIMETER)) {
          LOGGER.debug("User is member of group [{}] so adding role [{}]", groupName, roleName);
          roleNames.add(roleName);
        }
      }
    }
    return roleNames;
  }
}
