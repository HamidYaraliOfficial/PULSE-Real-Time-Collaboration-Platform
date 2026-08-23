package com.pulse.controller

import com.pulse.dto.*
import com.pulse.service.UserService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun me(): UserSummary = userService.get(currentUserId())

    @PatchMapping("/me")
    fun updateProfile(@RequestBody request: UpdateProfileRequest): UserSummary =
        userService.updateProfile(currentUserId(), request)

    @PostMapping("/me/change-password")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest) {
        userService.changePassword(currentUserId(), request.currentPassword, request.newPassword)
    }

    @GetMapping("/search")
    fun search(@RequestParam q: String): List<UserSummary> = userService.search(q)
}
