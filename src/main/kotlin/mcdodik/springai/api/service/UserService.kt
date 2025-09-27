package mcdodik.springai.api.service

import mcdodik.springai.api.dto.user.CreateUserRequest
import mcdodik.springai.api.dto.user.UserResponse
import mcdodik.springai.db.entity.user.UserRecord
import mcdodik.springai.db.mybatis.mapper.UserMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userMapper: UserMapper,
) {
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        val existingUser = userMapper.findByLogin(request.login)
        if (existingUser != null) {
            throw IllegalStateException("User with login '${request.login}' already exists.")
        }

        val userRecord = UserRecord(login = request.login)
        userMapper.insert(userRecord)

        return UserResponse(
            id = userRecord.id!!,
            login = userRecord.login,
        )
    }

    fun findUserByLogin(login: String): UserResponse {
        val user =
            userMapper.findByLogin(login)
                ?: throw NoSuchElementException("User with login '$login' not found.")

        return UserResponse(id = user.id!!, login = user.login)
    }
}
