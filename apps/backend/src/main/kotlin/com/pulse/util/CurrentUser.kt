package com.pulse.util

import com.pulse.security.SecurityUser
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

/** Small helper so controllers don't repeat the SecurityContext cast everywhere. */
fun currentUserId(): UUID {
    val principal = SecurityContextHolder.getContext().authentication.principal
    return (principal as SecurityUser).id
}
