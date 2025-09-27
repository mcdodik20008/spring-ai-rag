package mcdodik.springai.api.restcontroller

import mcdodik.springai.api.dto.user.CreateUserRequest
import mcdodik.springai.api.dto.user.UserResponse
import mcdodik.springai.api.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/search")
    fun getUserByLogin(
        @RequestParam login: String,
    ): ResponseEntity<UserResponse> {
        val user = userService.findUserByLogin(login)
        return ResponseEntity.ok(user)
    }

    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val createdUser = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }
}
