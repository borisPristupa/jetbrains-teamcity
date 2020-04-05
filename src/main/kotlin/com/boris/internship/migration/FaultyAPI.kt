package com.boris.internship.migration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.CloseableHttpClient
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path

internal class FaultyResponseException(msg: String) : RuntimeException(msg)
internal class FileAlreadyExistsException(fileName: String) : RuntimeException("File already exists: $fileName")

internal class ConnectionToStorage<S : Storage>(
    internal val storage: S,
    internal val httpClient: CloseableHttpClient
)

internal fun <S : Storage> S.withConnection(httpClient: CloseableHttpClient) = ConnectionToStorage(this, httpClient)

internal fun <T : Storage> ConnectionToStorage<T>.listOfFileNames(): Collection<String> =
    HttpGet(storage.path).run {
        setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.mimeType)
        httpClient.execute(this).use {
            val json = it.entity.content.bufferedReader().use(BufferedReader::readText)
            try {
                json.readValue()
            } catch (e: JsonProcessingException) {
                throw FaultyResponseException("Bad response from the server: '$json'")
            }
        }
    }

private inline fun <reified T> String.readValue(): T =
    ObjectMapper().readValue<T>(this, object : TypeReference<T>() {})

internal fun <T : SourceStorage> ConnectionToStorage<T>.getFile(fileName: String): File =
    HttpGet("${storage.path}/$fileName").run {
        setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_OCTET_STREAM.mimeType)
        httpClient.execute(this).use {
            val tmpDir = System.getProperty("java.io.tmpdir")
            File(Path.of(tmpDir, fileName).toUri()).apply {
                deleteOnExit()
                writeText(it.entity.content.bufferedReader().readText())
            }
        }
    }

internal fun <T : DestinationStorage> ConnectionToStorage<T>.postFile(file: File): Unit =
    HttpPost(storage.path).run {
        setHeader(HttpHeaders.ACCEPT, ContentType.WILDCARD.mimeType)

        entity = MultipartEntityBuilder.create()
            .addPart("file", FileBody(file))
            .setContentType(ContentType.MULTIPART_FORM_DATA)
            .build()

        httpClient.execute(this).use {
            if (it.statusLine.statusCode != HttpStatus.SC_OK) {
                if (it.statusLine.statusCode == HttpStatus.SC_CONFLICT)
                    throw FileAlreadyExistsException(file.name)
                else
                    throw FaultyResponseException("Error while posting ${file.name}: ${it.statusLine.statusCode}")
            }
        }
    }

internal fun <T : ClearableStorage> ConnectionToStorage<T>.deleteFile(fileName: String): Unit =
    HttpDelete("${storage.path}/$fileName").run {
        setHeader(HttpHeaders.ACCEPT, ContentType.WILDCARD.mimeType)
        httpClient.execute(this).use {
            if (it.statusLine.statusCode != HttpStatus.SC_OK) {
                throw FaultyResponseException("Error while deleting $fileName: ${it.statusLine.statusCode}")
            }
            it.statusLine.statusCode == HttpStatus.SC_OK
        }
    }