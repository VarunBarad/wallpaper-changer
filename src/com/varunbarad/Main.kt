package com.varunbarad

import java.io.File
import java.security.SecureRandom
import java.util.*

/**
 * Creator: vbarad
 * Date: 2016-09-16
 * Project: wallpaper-changer
 */

fun isImageFile(fileUri: String): Boolean {
    return fileUri.matches(Regex("(.+\\.(?i)(jpg|png|gif|bmp))$"))
}

fun getRandomElementIndex(length: Int): Int {
    val random = SecureRandom()
    return random.nextInt(length)
}

fun getFiles(file: File): MutableList<String> {
    val files: MutableList<String> = mutableListOf()

    if (file.isDirectory) {
        val buffer: MutableList<File> = mutableListOf()
        for (f in file.listFiles()) {
            if (f.isDirectory) {
                buffer.add(f)
            } else {
                files.add(f.path)
            }
        }

        for (f in buffer) {
            files.addAll(getFiles(f))
        }
    } else {
        files.add(file.path)
    }

    return files
}

fun toFileUri(filePath: String): String {
    val escapedPath = filePath.replace("\\\"", "\"").replace("\\\'", "\'").replace("\"", "\\\"").replace("\'", "\\\'")
    return "\"file://$escapedPath\""
}

fun extractFilePathFromUri(fileUri: String): String {
    return fileUri.substring(7)
}

fun getCurrentBackgroundUri(): String {
    var currentBackground: String
    val process: Process? = Runtime.getRuntime().exec("gsettings get org.gnome.desktop.background picture-uri")

    if (process != null) {
        val input: Scanner = Scanner(process.inputStream)

        currentBackground = input.nextLine().substringAfter('\'').substringBeforeLast('\'')
        input.close()
    } else {
        currentBackground = "file://"
    }

    currentBackground = extractFilePathFromUri(currentBackground)
    currentBackground = toFileUri(currentBackground)

    return currentBackground
}

fun setBackground(fileUri: String): Unit {
    val process: Process? = Runtime.getRuntime().exec("gsettings set org.gnome.desktop.background picture-uri \"$fileUri\"")

    if (process != null) {
        println("Background changed successfully to $fileUri")
    } else {
        println("Failed to change background")
    }
}

fun main(args: Array<String>) {
    val baseDirectory: String
    if (args.isNotEmpty()) {
        baseDirectory = args[0]
    } else {
        baseDirectory = "/home/vbarad/Pictures"
    }

    val baseFile: File = File(baseDirectory)
    if (baseFile.exists()) {
        val filePaths = getFiles(baseFile)
                .filter(::isImageFile)
                .map(::toFileUri)

        var newBackgroundUri: String
        do {
            newBackgroundUri = filePaths[getRandomElementIndex(filePaths.size)]
        } while (newBackgroundUri == getCurrentBackgroundUri())

        setBackground(newBackgroundUri)
    } else {
        println("The specified file/directory does not exist. Try again.")
    }
}
