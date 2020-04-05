package com.boris.internship

import com.boris.internship.migration.*

fun main() {
    enableLogging = true

    println("Files in old storage: ${Migrator.fileAmount(OldStorage)}")
    println("Files in new storage: ${Migrator.fileAmount(NewStorage)}")
    println()

    Migrator.migrate(OldStorage, NewStorage)

    println()
    println("Files in old storage: ${Migrator.fileAmount(OldStorage)}")
    println("Files in new storage: ${Migrator.fileAmount(NewStorage)}")
}
