package com.alvaroquintana.data.repository

sealed class RepositoryException(message: String) : Exception(message) {
    class DataNotFoundException(message: String = "Data not found") : RepositoryException(message)
    class NoConnectionException(message: String = "No connection") : RepositoryException(message)
}
