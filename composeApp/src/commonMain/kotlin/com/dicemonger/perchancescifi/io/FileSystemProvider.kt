package com.dicemonger.perchancescifi.io

import okio.FileSystem

expect object FileSystemProvider {
    val system: FileSystem
}