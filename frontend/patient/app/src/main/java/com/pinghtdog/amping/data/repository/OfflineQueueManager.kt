package com.pinghtdog.amping.data.repository

import android.content.Context
import com.pinghtdog.amping.data.model.QueueEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

object OfflineQueueManager {
    private const val QUEUE_FILE = "offline_queue.json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private fun getQueueFile(context: Context): File {
        return File(context.filesDir, QUEUE_FILE)
    }

    fun getQueue(context: Context): List<QueueEntry> {
        val file = getQueueFile(context)
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<QueueEntry>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveQueue(context: Context, queue: List<QueueEntry>) {
        val file = getQueueFile(context)
        try {
            file.writeText(json.encodeToString(queue))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addEntry(context: Context, path: String, profile: String, adherenceDayID: Long? = null): QueueEntry {
        val queue = getQueue(context).toMutableList()
        val entry = QueueEntry(
            id = UUID.randomUUID().toString(),
            localEncryptedPath = path,
            timestamp = System.currentTimeMillis(),
            profile = profile,
            adherenceDayID = adherenceDayID
        )
        queue.add(entry)
        saveQueue(context, queue)
        return entry
    }

    fun updateEntryStatus(context: Context, id: String, status: String, retryCount: Int? = null) {
        val queue = getQueue(context).toMutableList()
        val index = queue.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = queue[index]
            queue[index] = old.copy(
                status = status,
                retryCount = retryCount ?: old.retryCount
            )
            saveQueue(context, queue)
        }
    }

    fun removeEntry(context: Context, id: String) {
        val queue = getQueue(context).toMutableList()
        val entry = queue.find { it.id == id }
        if (entry != null) {
            try {
                val file = File(entry.localEncryptedPath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            queue.remove(entry)
            saveQueue(context, queue)
        }
    }
}
