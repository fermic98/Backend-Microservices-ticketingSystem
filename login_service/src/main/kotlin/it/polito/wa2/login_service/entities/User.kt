package it.polito.wa2.login_service.entities

import javax.persistence.*

@Entity
@Table(name = "users")
class User(

    @Column(nullable = false, unique = true)
    var nickname: String = "",
    @Column(nullable = false)
    var password: String = "",
    @Column(nullable = false, unique = true)
    var email: String = "",

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
    var activation: Activation? = null,

    @Column(nullable = false)
    var active: Boolean = false,

    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(Role(1,ERole.CUSTOMER))


) : EntityBase<Long>() {


    fun addRole(role: Role) {
        roles.add(role)
        role.users.add(this)
    }
}



