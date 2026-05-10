package com.mskd.flux.data.repository.files

import com.mskd.flux.model.UserFile

interface FilesRepository {

    suspend fun getFiles() : List<UserFile>

    suspend fun checkIfFileExists(file: UserFile) : Boolean

}