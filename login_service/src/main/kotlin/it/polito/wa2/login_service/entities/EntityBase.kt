package it.polito.wa2.login_service.entities

import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class EntityBase<T: Serializable> {
    companion object{
        private const val serialVersionUID = -43869754L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false, unique = true)
    private var id: T? = null

    fun getId(): T? = id

    override fun toString(): String {
        return "Entity ${this.javaClass.name} with ID [${id}]"
    }

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other === this) return true //checking the references we avoid to controll all other fields
        if(javaClass != ProxyUtils.getUserClass(other)) return false

        other as EntityBase<*>

        return  if(id == null) false
        else this.id == other.id
    }

    override fun hashCode(): Int {
        return 31
    }
}