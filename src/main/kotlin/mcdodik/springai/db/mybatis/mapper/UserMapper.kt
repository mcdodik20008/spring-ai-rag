package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.user.UserRecord
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface UserMapper {
    fun findByLogin(
        @Param("login") login: String,
    ): UserRecord?

    /**
     * Вставляет нового пользователя и обновляет поле 'id' в переданном объекте.
     */
    fun insert(user: UserRecord)
}
