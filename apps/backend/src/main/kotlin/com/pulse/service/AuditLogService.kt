package com.pulse.service

import com.pulse.domain.AuditLog
import com.pulse.repository.AuditLogRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuditLogService(private val auditLogRepository: AuditLogRepository) {

    fun log(workspaceId: UUID?, actorId: UUID?, action: String, entityType: String, entityId: UUID?, metadata: String = "{}") {
        auditLogRepository.save(
            AuditLog(
                workspaceId = workspaceId,
                actorId = actorId,
                action = action,
                entityType = entityType,
                entityId = entityId,
                metadata = metadata
            )
        )
    }

    fun listForWorkspace(workspaceId: UUID) = auditLogRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
}
