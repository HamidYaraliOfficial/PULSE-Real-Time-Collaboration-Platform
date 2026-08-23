package com.pulse.exception

class ApiException(val status: Int, override val message: String) : RuntimeException(message)
