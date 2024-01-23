package com.kaem.flux.data.source

interface FilesDataSource {

    suspend fun getFiles() : List<String>

}