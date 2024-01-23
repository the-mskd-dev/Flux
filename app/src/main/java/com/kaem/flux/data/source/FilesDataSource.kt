package com.kaem.flux.data.source

import com.kaem.flux.model.FileSource

interface FilesDataSource {

    suspend fun getFiles() : List<FileSource>

}