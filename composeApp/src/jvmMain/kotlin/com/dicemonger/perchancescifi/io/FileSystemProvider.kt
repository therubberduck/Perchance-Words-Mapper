package com.dicemonger.perchancescifi.io

import okio.FileSystem

actual object FileSystemProvider {
    actual val system: FileSystem = FileSystem.SYSTEM
}