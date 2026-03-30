package com.mskd.flux.data.source.file

import com.mskd.flux.model.UserFile

interface FilesSource {

    suspend fun getFiles() : List<UserFile>

    suspend fun checkIfFileExists(path: String) : Boolean

}