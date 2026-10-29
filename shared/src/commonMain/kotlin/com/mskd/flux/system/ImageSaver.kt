package com.mskd.flux.system

/**
 * Saves the bytes of an image into the image storage of the device, where the user can find it in its gallery.
 */
interface ImageSaver {

    /**
     * Never throws: a failure is returned as a failed [Result].
     *
     * @param bytes Content of the image file, saved untouched.
     * @param fileName Name of the file, with its extension.
     * @param mimeType MIME type of the image.
     */
    suspend fun save(bytes: ByteArray, fileName: String, mimeType: String): Result<Unit>

}
