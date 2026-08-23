package com.pulse.controller

import com.pulse.service.AiAssistantService
import com.pulse.util.currentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class AiRunRequest(
    val task: String,           // SUMMARIZE_CONVERSATION | MEETING_SUMMARY | EXTRACT_ACTION_ITEMS |
                                 // SUGGEST_TASKS | SUMMARIZE_DOCUMENT | REWRITE | TRANSLATE | PROJECT_STATUS | SMART_REPLY
    val context: String,        // caller-supplied, already permission-checked text (messages, doc content, task list...)
    val contextType: String? = null,
    val contextId: UUID? = null
)

data class AiRunResponse(val result: String)

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/ai")
class AiController(private val aiAssistantService: AiAssistantService) {

    @PostMapping("/run")
    fun run(@PathVariable workspaceId: UUID, @RequestBody request: AiRunRequest): AiRunResponse {
        val result = aiAssistantService.run(
            currentUserId(), workspaceId, request.contextType, request.contextId, request.task, request.context
        )
        return AiRunResponse(result)
    }
}
