package com.kaem.flux.data.source.file

import com.kaem.flux.model.UserFile

interface FilesDataSource {

    suspend fun getFiles() : List<UserFile>

    suspend fun checkIfFileExists(path: String) : Boolean

}