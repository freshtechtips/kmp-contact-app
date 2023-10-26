package com.ahmedhnewa.composemultiplatform.core.presentation.image_storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile

actual class ImageStorage {

    private val fileManager = NSFileManager.defaultManager
    private val documentDirectory = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).first() as NSString

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun saveImage(bytes: ByteArray): String {
        return withContext(Dispatchers.IO) {
            val fileName = NSUUID.UUID().UUIDString() + ".jpg"
            val fullPath = documentDirectory.stringByAppendingPathComponent(fileName)
            val data = bytes.usePinned {
                NSData.create(
                    bytes = it.addressOf(0),
                    length = bytes.size.toULong()
                )
            }

            data.writeToFile(
                path = fullPath,
                atomically = true
            )
            fullPath
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getImage(fileName: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            memScoped {
                NSData.dataWithContentsOfFile(fileName)?.let { bytes ->
                    val array = ByteArray(bytes.length.toInt())
                    bytes.getBytes(array.refTo(0).getPointer(this), bytes.length)
                    return@withContext array
                }
            }
            return@withContext null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun deleteImage(fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            fileManager.removeItemAtPath(fileName, null)
        }
    }
}