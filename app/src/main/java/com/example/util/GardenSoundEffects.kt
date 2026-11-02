package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Synthesizes subtle, organic sound effects for Sanctuary Garden interactions
 * (water jug pouring and plant level-up chimes) using Android's native AudioTrack API.
 */
object GardenSoundEffects {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a soft bubbling / water stream trickle sound effect.
     */
    fun playWaterPouringSound() {
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 450
                val numSamples = sampleRate * durationMs / 1000
                val sample = ByteArray(numSamples * 2)

                var idx = 0
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val freq = 420.0 + 320.0 * Math.sin(i * 0.04) + (if ((i / 400) % 2 == 0) 100.0 else 0.0)
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val envelope = Math.sin(progress * Math.PI) * 0.22 // max 22% volume
                    val valShort = (Math.sin(angle) * 32767 * envelope).toInt().toShort()

                    sample[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    sample[idx++] = (valShort.toInt() and 0xff00 shr 8).toByte()
                }

                playPcmBuffer(sample, sampleRate)
            } catch (_: Exception) {
                // Graceful audio fallback
            }
        }
    }

    /**
     * Plays a joyful ascending level-up chime (C5 -> E5 -> G5 -> C6).
     */
    fun playLevelUpChime() {
        scope.launch {
            try {
                val sampleRate = 22050
                val notes = doubleArrayOf(523.25, 659.25, 784.00, 1046.50) // C5, E5, G5, C6
                val noteDurationMs = 110
                val totalSamples = sampleRate * (notes.size * noteDurationMs) / 1000
                val sample = ByteArray(totalSamples * 2)

                var idx = 0
                notes.forEach { freq ->
                    val noteSamples = sampleRate * noteDurationMs / 1000
                    for (i in 0 until noteSamples) {
                        val progress = i.toFloat() / noteSamples
                        val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                        val envelope = (1.0 - progress * 0.5) * 0.28 // max 28% volume
                        val valShort = (Math.sin(angle) * 32767 * envelope).toInt().toShort()

                        sample[idx++] = (valShort.toInt() and 0x00ff).toByte()
                        sample[idx++] = (valShort.toInt() and 0xff00 shr 8).toByte()
                    }
                }

                playPcmBuffer(sample, sampleRate)
            } catch (_: Exception) {
                // Graceful audio fallback
            }
        }
    }

    private fun playPcmBuffer(buffer: ByteArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        scope.launch {
            delay(1200)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
