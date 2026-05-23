package com.ttsengine.tts

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioMerger {

    fun mergeWavFiles(inputFiles: List<File>, outputFile: File) {
        if (inputFiles.size == 1) {
            inputFiles.first().copyTo(outputFile, overwrite = true)
            return
        }

        var totalDataSize = 0L
        val headers = mutableListOf<WavHeader>()

        for (f in inputFiles) {
            val header = readWavHeader(f)
            headers.add(header)
            totalDataSize += header.dataSize
        }

        val sampleRate = headers.first().sampleRate
        val bitsPerSample = headers.first().bitsPerSample
        val channels = headers.first().channels

        FileOutputStream(outputFile).use { out ->
            val dataSize = totalDataSize
            val fileSize = 36 + dataSize

            writeLE(out, "RIFF".toByteArray())
            writeLE(out, fileSize.toInt())
            writeLE(out, "WAVE".toByteArray())
            writeLE(out, "fmt ".toByteArray())
            writeLE(out, 16)
            writeLE(out, 1.toShort())
            writeLE(out, channels.toShort())
            writeLE(out, sampleRate)
            writeLE(out, sampleRate * channels * bitsPerSample / 8)
            writeLE(out, (channels * bitsPerSample / 8).toShort())
            writeLE(out, bitsPerSample.toShort())
            writeLE(out, "data".toByteArray())
            writeLE(out, dataSize.toInt())

            for (f in inputFiles) {
                FileInputStream(f).use { `in` ->
                    val data = `in`.readBytes()
                    val start = findSubArray(data, "data".toByteArray()) + 8
                    out.write(data, start, data.size - start)
                }
            }
        }
    }

    private data class WavHeader(
        val sampleRate: Int,
        val bitsPerSample: Int,
        val channels: Int,
        val dataSize: Long
    )

    private fun readWavHeader(file: File): WavHeader {
        FileInputStream(file).use { stream ->
            val riff = ByteArray(4)
            stream.read(riff)
            val fileSize = readLEInt(stream)
            val wave = ByteArray(4)
            stream.read(wave)

            while (true) {
                val chunkId = ByteArray(4)
                if (stream.read(chunkId) != 4) throw Exception("Unexpected end of file")
                val chunkSize = readLEInt(stream)

                if (String(chunkId) == "fmt ") {
                    val audioFormat = readLEShort(stream)
                    val channels = readLEShort(stream).toInt()
                    val sampleRate = readLEInt(stream)
                    stream.skip(6)
                    val bitsPerSample = readLEShort(stream).toInt()

                    val bytesRead = 16
                    if (chunkSize > bytesRead) stream.skip((chunkSize - bytesRead).toLong())
                    return WavHeader(sampleRate, bitsPerSample, channels, 0)
                } else if (String(chunkId) == "data") {
                    continue
                } else {
                    stream.skip(chunkSize.toLong())
                }
            }
        }
    }

    private fun readLEInt(stream: java.io.InputStream): Int {
        val b = ByteArray(4)
        stream.read(b)
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).int
    }

    private fun readLEShort(stream: java.io.InputStream): Short {
        val b = ByteArray(2)
        stream.read(b)
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).short
    }

    private fun writeLE(stream: FileOutputStream, data: ByteArray) {
        stream.write(data)
    }

    private fun writeLE(stream: FileOutputStream, value: Int) {
        val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        stream.write(buf.array())
    }

    private fun writeLE(stream: FileOutputStream, value: Short) {
        val buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value)
        stream.write(buf.array())
    }

    private fun findSubArray(data: ByteArray, pattern: ByteArray): Int {
        outer@ for (i in 0..data.size - pattern.size) {
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) continue@outer
            }
            return i
        }
        return -1
    }
}
