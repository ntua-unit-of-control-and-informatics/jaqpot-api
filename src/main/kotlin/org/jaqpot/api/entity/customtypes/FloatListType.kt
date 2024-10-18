package org.jaqpot.api.entity.customtypes

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class FloatListType : UserType<List<Float>> {
    override fun equals(p0: List<Float>?, p1: List<Float>?): Boolean {
        return p0?.equals(p1) ?: false
    }

    override fun hashCode(p0: List<Float>?): Int {
        return p0.hashCode()
    }

    override fun getSqlType(): Int {
        return Types.ARRAY
    }

    override fun returnedClass(): Class<List<Float>> {
        return List::class.java as Class<List<Float>>
    }

    override fun nullSafeGet(
        rs: ResultSet?,
        position: Int,
        session: SharedSessionContractImplementor?,
        owner: Any?
    ): List<Float> {
        val array = rs?.getArray(position)?.array
        return if (array is List<*>) {
            array as List<Float>
        } else {
            emptyList()
        }
    }

    override fun nullSafeSet(
        st: PreparedStatement?,
        value: List<Float>?,
        index: Int,
        session: SharedSessionContractImplementor?
    ) {
        if (value != null) {
            st?.setArray(index, st.connection.createArrayOf("float8", value.toTypedArray()))
        } else {
            st?.setNull(index, Types.ARRAY)
        }
    }


    override fun deepCopy(p0: List<Float>?): List<Float> {
        TODO("Not yet implemented")
    }


    override fun isMutable(): Boolean = true
    override fun disassemble(value: List<Float>?): Serializable {
        return value as Serializable
    }

    override fun assemble(cached: Serializable?, owner: Any?): List<Float> {
        return cached as List<Float>
    }
}
