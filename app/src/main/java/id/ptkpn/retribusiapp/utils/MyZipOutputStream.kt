package id.ptkpn.retribusiapp.utils

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception


class MyZipOutputStream {
    @Throws(IOException::class)
    fun initialize(
        outputZipFile: File?, filesToAdd: List<File>, password: CharArray,
        compressionMethod: CompressionMethod, encrypt: Boolean,
        encryptionMethod: EncryptionMethod, aesKeyStrength: AesKeyStrength
    ): File {
        if (outputZipFile != null) {
            val zipParameters =
                buildZipParameters(compressionMethod, encrypt, encryptionMethod, aesKeyStrength)
            val buff = ByteArray(4096)
            var readLen: Int
            initializeZipOutputStream(outputZipFile, encrypt, password).use { zos ->
                for (fileToAdd in filesToAdd) {

                    // Entry size has to be set if you want to add entries of STORE compression method (no compression)
                    // This is not required for deflate compression
                    if (zipParameters.compressionMethod == CompressionMethod.STORE) {
                        zipParameters.entrySize = fileToAdd.length()
                    }
                    zipParameters.fileNameInZip = fileToAdd.getName()
                    zos.putNextEntry(zipParameters)
                    FileInputStream(fileToAdd).use { inputStream ->
                        while (inputStream.read(buff).also {
                                readLen = it
                            } != -1) {
                            zos.write(buff, 0, readLen)
                        }
                    }
                    zos.closeEntry()
                }
            }
            return outputZipFile
        } else {
            throw Exception("Output File Not Exist")
        }
    }

    @Throws(IOException::class)
    private fun initializeZipOutputStream(
        outputZipFile: File,
        encrypt: Boolean,
        password: CharArray
    ): ZipOutputStream {
        val fos = FileOutputStream(outputZipFile)
        return if (encrypt) {
            ZipOutputStream(fos, password)
        } else ZipOutputStream(fos)
    }

    private fun buildZipParameters(
        compressionMethod: CompressionMethod, encrypt: Boolean,
        encryptionMethod: EncryptionMethod, aesKeyStrength: AesKeyStrength
    ): ZipParameters {
        val zipParameters = ZipParameters()
        zipParameters.compressionMethod = compressionMethod
        zipParameters.encryptionMethod = encryptionMethod
        zipParameters.aesKeyStrength = aesKeyStrength
        zipParameters.isEncryptFiles = encrypt
        return zipParameters
    }
}