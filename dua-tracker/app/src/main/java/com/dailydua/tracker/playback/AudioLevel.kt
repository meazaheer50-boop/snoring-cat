package com.dailydua.tracker.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/** How loud the recitation is right now, from 0 to 1. The background reads this to move with the voice. */
object AudioLevel {
    var level by mutableFloatStateOf(0f)
}

/** Passes the audio through untouched and measures its loudness on the way. */
class LevelAudioProcessor : BaseAudioProcessor() {
    private var smoothed = 0f

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val view = inputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        var sum = 0.0
        var count = 0
        while (view.remaining() >= 2) {
            val s = view.short.toInt()
            sum += s.toDouble() * s
            count++
            // Look at every 6th sample, which is plenty for a loudness reading.
            if (view.remaining() >= 12) view.position(view.position() + 10)
        }
        val rms = if (count > 0) sqrt(sum / count).toFloat() / 32768f else 0f
        val target = ((rms * 3f) - 0.1f).coerceIn(0f, 1f)
        smoothed = if (target > smoothed) smoothed + (target - smoothed) * 0.5f else smoothed * 0.93f
        AudioLevel.level = smoothed
        replaceOutputBuffer(size).put(inputBuffer).flip()
    }

    override fun onReset() {
        smoothed = 0f
        AudioLevel.level = 0f
    }
}
