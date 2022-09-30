package it.polito.wa2.login_service.entities

import javax.persistence.*


@Entity
@Table(name = "roles")
class Role(
        @Id
        @Column(updatable = false, nullable = false)
        val id: Long?,

        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        var name: ERole? = null,

        @ManyToMany(mappedBy = "roles")
        val users: MutableSet<User> = mutableSetOf()
) {

    fun addUser(user: User) {
        users.add(user)
        user.roles.add(this)
    }

}