package com.pulse.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pulse.domain.AiSession
import com.pulse.repository.AiSessionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

/**
 * AI Collaboration Assistant.
 *
 * Permission-aware by construction: this service never queries the database
 * itself. Callers (controllers) must first load only the data the requesting
 * user is already authorized to see (a channel they belong to, a document
 * they can open, a project they're a member of) and pass that text in as
 * `context`. The assistant only ever reasons over what it's handed.
 *
 * Any AI action that would *change* workspace data (e.g. "create a task from
 * this conversation") must go through the normal REST endpoints after the
 * user reviews and confirms the AI's suggestion - this service only ever
 * returns text, it never calls TaskService/DocumentService/etc. directly.
 *
 * Indexing workspace content into a proper vector store for retrieval-
 * augmented search over the *entire* workspace (RAG) is intentionally not
 * included in this scaffold - see the README "Scope" section. What's wired
 * up here is real, working summarization / rewrite / action-item extraction
 * over explicitly supplied context, calling the Anthropic Messages API.
 */
@Service
class AiAssistantService(
    private val aiSessionRepository: AiSessionRepository,
    @Value("\${pulse.ai.api-key}") private val apiKey: String,
    @Value("\${pulse.ai.model}") private val model: String
) {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()
    private val mapper: ObjectMapper = jacksonObjectMapper()

    fun run(userId: UUID, workspaceId: UUID, contextType: String?, contextId: UUID?, task: String, context: String): String {
        if (apiKey.isBlank()) {
            return "AI features are not configured. Set the AI_API_KEY environment variable to enable the assistant."
        }

        val systemPrompt = buildSystemPrompt(task)
        val userMessage = "Context:\n$context\n\nInstruction: $task"

        val body = mapper.writeValueAsString(
            mapOf(
                "model" to model,
                "max_tokens" to 1024,
                "system" to systemPrompt,
                "messages" to listOf(mapOf("role" to "user", "content" to userMessage))
            )
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val text = extractText(response.body())

        aiSessionRepository.save(
            AiSession(
                userId = userId, workspaceId = workspaceId, contextType = contextType, contextId = contextId,
                prompt = userMessage, response = text, actionTaken = task
            )
        )
        return text
    }

    private fun buildSystemPrompt(task: String): String = when (task) {
        "SUMMARIZE_CONVERSATION" -> "Summarize this team conversation in 3-5 concise bullet points. Focus on decisions and open questions."
        "MEETING_SUMMARY" -> "Summarize this meeting transcript and list clear action items with an owner if mentioned."
        "EXTRACT_ACTION_ITEMS" -> "Extract a checklist of concrete, assignable action items from the text. One per line."
        "SUGGEST_TASKS" -> "Propose a short list of discrete tasks (title + one-line description) that could be created from this text."
        "SUMMARIZE_DOCUMENT" -> "Summarize this document in a short paragraph followed by up to 5 key points."
        "REWRITE" -> "Rewrite the given text to be clearer and more professional, preserving the original meaning."
        "TRANSLATE" -> "Translate the given text, preserving tone and formatting."
        "PROJECT_STATUS" -> "Given this list of tasks, write a short project status update: what's done, what's in progress, what's at risk."
        "SMART_REPLY" -> "Suggest three short, distinct reply options to the last message in this conversation."
        else -> "Help the user with their request based only on the provided context."
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractText(rawBody: String): String {
        val parsed = mapper.readValue(rawBody, Map::class.java)
        val content = parsed["content"] as? List<Map<String, Any>> ?: return "The assistant did not return a response."
        return content.filter { it["type"] == "text" }.joinToString("\n") { it["text"] as? String ?: "" }
    }
}
