package com.boris.internship.migration

import org.apache.http.impl.client.CloseableHttpClient
import java.io.File

interface Storage {
    val path: String
    val name: String

    fun listOfFiles(httpClient: CloseableHttpClient): Collection<String> =
        withConnection(httpClient).run {
            repeatWhileThrows(this::listOfFileNames)
        }
}

interface SourceStorage: Storage {
    fun getFile(fileName: String, httpClient: CloseableHttpClient): File =
        withConnection(httpClient).run {
            repeatWhileThrows { getFile(fileName) }
        }
}

interface ClearableStorage: Storage {
    fun deleteFile(fileName: String, httpClient: CloseableHttpClient): Unit =
        withConnection(httpClient).run {
            repeatWhileThrows { deleteFile(fileName) }
        }
}

interface ClearableSourceStorage: SourceStorage, ClearableStorage

interface DestinationStorage: Storage {
    fun postFile(file: File, httpClient: CloseableHttpClient): Unit =
        withConnection(httpClient).run {
            repeatWhileThrows { postFile(file) }
        }
}

interface ClearableDestinationStorage: DestinationStorage, ClearableStorage {
    override fun postFile(file: File, httpClient: CloseableHttpClient): Unit =
        withConnection(httpClient).run {
            repeatWhileThrows {
                try {
                    postFile(file)
                } catch (e: FileAlreadyExistsException) {
                    warning("File already exists. It will be rewritten")
                    deleteFile(file.name)
                    postFile(file)
                }
            }
        }
}

internal fun <T> repeatWhileThrows(block: () -> T): T {
    var result: T? = null
    var calledMoreThanOnce = false
    while (result == null) {
        if (calledMoreThanOnce)
            warning("Retrying")
        calledMoreThanOnce = true

        result = try {
            block()
        } catch (e: FaultyResponseException) {
            severe(e.message ?: "Server sent bad response.")
            null
        } catch (e: Exception) {
            severe("Something bad happened.")
            e.printStackTrace()
            null
        }
    }
    return result
}
