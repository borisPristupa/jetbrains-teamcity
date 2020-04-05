package com.boris.internship.migration

import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.log4j.Logger

object Migrator {
    fun fileAmount(storage: Storage): Int = doOperations { storage.listOfFiles(it).size }

    fun migrate(source: ClearableSourceStorage, destination: DestinationStorage) {
        info("Starting migration from ${source.name} to ${destination.name}: ${fileAmount(source)} to migrate.")

        copy(source, destination)
        clear(source)

        info("Migration complete.")
    }

    fun copy(source: SourceStorage, destination: DestinationStorage) = doOperations { httpClient ->
        info("Starting copying files from ${source.name} to ${destination.name}...")
        source.listOfFiles(httpClient).forEach {
            info("Getting file '$it' from ${source.name}...")
            val file = source.getFile(it, httpClient)

            info("Writing file '$it' to ${destination.name}...")
            destination.postFile(file, httpClient)

            info("File copied.\n")
        }
        info("Copied all files.")
    }

    fun clear(clearable: ClearableStorage) = doOperations { httpClient ->
        info("Starting deleting files from ${clearable.name}...")
        clearable.listOfFiles(httpClient).forEach {
            info("Deleting file '$it' from ${clearable.name}...")
            clearable.deleteFile(it, httpClient)
        }
        info("Deleted all files from ${clearable.name}")
    }
}

internal fun <T> doOperations(operations: (CloseableHttpClient) -> T) =
    HttpClients.createDefault().use { operations(it) }

private val logger = Logger.getLogger(Migrator::class.java)!!
var enableLogging = false

internal fun info(msg: String): Unit = if (enableLogging) logger.info(msg) else {}
internal fun warning(msg: String) = if (enableLogging) logger.warn(msg) else {}
internal fun severe(msg: String) = if (enableLogging) logger.error(msg) else {}