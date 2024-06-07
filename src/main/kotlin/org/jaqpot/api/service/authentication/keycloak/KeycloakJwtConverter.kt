package org.jaqpot.api.service.authentication.keycloak

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component


/**
 * Adds keycloak assigned roles to the principal authorities
 */
@Component
class KeycloakJwtConverter() : Converter<Jwt, AbstractAuthenticationToken> {

    companion object {
        const val REALM_ACCESS_KEY = "realm_access"
        const val ROLES_KEY = "roles"
    }

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val defaultAuthorities = JwtGrantedAuthoritiesConverter().convert(source)
        val keycloakAuthorities = extractKeycloakAuthorities(source)

        return JwtAuthenticationToken(source, listOf(defaultAuthorities, keycloakAuthorities).flatten())
    }

    private fun extractKeycloakAuthorities(jwt: Jwt): MutableCollection<GrantedAuthority> {

        val realmAccess = jwt.getClaim<LinkedTreeMap<String, Any>>(REALM_ACCESS_KEY) ?: return mutableListOf()

        val roles = realmAccess[ROLES_KEY] ?: return mutableListOf()

        val keycloakAuthorities = mutableListOf<GrantedAuthority>()

        (roles as List<String>).forEach { role ->
            keycloakAuthorities.add(SimpleGrantedAuthority(role))
        }

        return keycloakAuthorities
    }


}
