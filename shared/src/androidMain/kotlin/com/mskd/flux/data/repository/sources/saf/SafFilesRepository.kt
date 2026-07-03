package com.mskd.flux.data.repository.sources.saf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.data.repository.sources.SourcesFilesRepository
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFile
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SafFilesRepository(
    private val sourcesRepository: SourcesRepository,
    private val safVideoFilesDataSource: SafVideoFilesDataSource
) : SourcesFilesRepository {

    companion object {
        private const val TAG = "SafFilesRepository"
    }

    override suspend fun getFiles(): List<UserFile> = withContext(Dispatchers.IO) {
        val availableFolders = sourcesRepository.getFolders()
            .filter { it.status == UserFolder.Status.AVAILABLE }

        availableFolders.flatMap { folder ->
            safVideoFilesDataSource.getVideoFiles(folder.path.toUri())
        }
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubtitlesFor(file: UserFile): File? {
        TODO("Not yet implemented")
    }

}