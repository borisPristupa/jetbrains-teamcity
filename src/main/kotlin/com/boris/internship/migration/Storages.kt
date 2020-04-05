package com.boris.internship.migration

object OldStorage : ClearableSourceStorage {
    override val path = "http://localhost:8080/oldStorage/files"
    override val name = "Old storage"
}

object NewStorage : ClearableDestinationStorage, SourceStorage {
    override val path = "http://localhost:8080/newStorage/files"
    override val name = "New storage"
}