package it.polito.wa2.login_service.entities

import org.springframework.security.core.GrantedAuthority

enum class ERole: GrantedAuthority {
    CUSTOMER,
    EMBEDDED_SYSTEM,
    ADMIN,
    ADMIN_E;

    override fun getAuthority(): String {
        return this.name
    }
}