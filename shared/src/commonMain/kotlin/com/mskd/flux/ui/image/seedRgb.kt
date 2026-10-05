package com.mskd.flux.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.score.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ImageBitmap.seedRgb(): Int? = withContext(Dispatchers.Default) {
    val pixels = IntArray(width * height)
    readPixels(pixels)
    Score.score(QuantizerCelebi.quantize(pixels, 128), 1).firstOrNull()
}