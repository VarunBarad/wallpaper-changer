package com.varunbarad

import java.io.File
import java.util.*

/**
 * Creator: vbarad
 * Date: 2016-09-16
 * Project: wallpaper-changer
 */

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
    val escapedPath = filePath.replace("\"", "\\\"").replace("\'", "\\\'")
    return "\"file://$escapedPath\""
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
        val filePaths = getFiles(baseFile).filter { s -> s.matches(Regex("(.+\\.(?i)(jpg|png|gif|bmp))$")) }
        filePaths.map(::toFileUri).forEach(::println)


        val process: Process? = Runtime.getRuntime().exec("gsettings get org.gnome.desktop.background picture-uri")

        if (process != null) {
            val input: Scanner = Scanner(process.inputStream)
            println(input.nextLine())
            input.close()
        }
    } else {
        println("The specified file/directory does not exist. Try again.")
    }
}
