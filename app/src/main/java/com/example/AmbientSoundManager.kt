package com.example

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.math.sin
import kotlin.math.PI

class AmbientSoundManager {
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null
    private val sampleRate = 44100
    private var isPlaying = false

    enum class SoundType {
        RAIN, LOFI, WHITE_NOISE, OCEAN, FOREST
    }

    fun playSound(type: SoundType) {
        stopSound()
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        job = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)
            var phase = 0.0
            
            // For brown noise (Rain)
            var lastValue = 0.0
            
            // For Lo-fi (chords/melody simulation)
            var noteIndex = 0
            val freqs = arrayOf(261.63, 329.63, 392.00, 261.63/2.0)
            var timePerNote = 44100 * 2 // 2 seconds per note

            while (isPlaying && isActive) {
                for (i in buffer.indices) {
                    var sample = 0.0
                    when (type) {
                        SoundType.WHITE_NOISE -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            sample = white * 0.1
                        }
                        SoundType.OCEAN -> {
                            // Noise with low frequency amplitude modulation for waves
                            val white = Random.nextDouble(-1.0, 1.0)
                            lastValue = (lastValue + (0.01 * white)) / 1.01
                            
                            phase += 2.0 * PI * 0.1 / sampleRate // 0.1 Hz for wave swell
                            if (phase >= 2.0 * PI) phase -= 2.0 * PI
                            val swell = (sin(phase) + 1.0) / 2.0 // 0.0 to 1.0
                            
                            sample = lastValue * (0.5 + swell * 1.5)
                        }
                        SoundType.FOREST -> {
                            // Base wind noise
                            val white = Random.nextDouble(-1.0, 1.0)
                            lastValue = (lastValue + (0.02 * white)) / 1.02
                            sample = lastValue * 0.5
                            
                            // Occasional bird chirp
                            if (Random.nextDouble() > 0.99995 && noteIndex == 0) { // Trigger chirp
                                noteIndex = 1
                            }
                            
                            if (noteIndex in 1..4000) {
                                // High pitched sine sweep
                                val freq = 2500.0 + (noteIndex / 4000.0) * 1500.0
                                phase += 2.0 * PI * freq / sampleRate
                                if (phase >= 2.0 * PI) phase -= 2.0 * PI
                                val envelope = if (noteIndex < 1000) noteIndex / 1000.0 else (4000 - noteIndex) / 3000.0
                                sample += sin(phase) * 0.05 * envelope
                                noteIndex++
                            } else {
                                noteIndex = 0
                            }
                        }
                        SoundType.RAIN -> {
                            // Brown noise approximation
                            val white = Random.nextDouble(-1.0, 1.0)
                            lastValue = (lastValue + (0.02 * white)) / 1.02
                            sample = lastValue * 3.0
                            
                            // add a little hiss
                            sample += Random.nextDouble(-0.02, 0.02)
                        }
                        SoundType.LOFI -> {
                            val freq = freqs[(noteIndex / timePerNote) % freqs.size]
                            phase += 2.0 * PI * freq / sampleRate
                            if (phase >= 2.0 * PI) phase -= 2.0 * PI
                            
                            // Sine wave with a low pass filter vibe (soft sound)
                            sample = sin(phase) * 0.1
                            
                            // add vinyl crackle
                            if (Random.nextDouble() > 0.999) {
                                sample += Random.nextDouble(-0.1, 0.1)
                            }
                            
                            noteIndex++
                        }
                    }

                    // clamp
                    if (sample > 1.0) sample = 1.0
                    if (sample < -1.0) sample = -1.0

                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        isPlaying = false
        job?.cancel()
        job = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
