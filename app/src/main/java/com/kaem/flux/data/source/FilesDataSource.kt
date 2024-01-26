package com.kaem.flux.data.source

import com.kaem.flux.model.UserFile

interface FilesDataSource {

    suspend fun getFiles() : List<UserFile>

}