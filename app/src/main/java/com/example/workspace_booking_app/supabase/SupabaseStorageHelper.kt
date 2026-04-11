package com.example.workspace_booking_app.supabase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.UUID

class SupabaseStorageHelper {

    private val client = OkHttpClient()
    private val TAG = "SupabaseStorage"

    fun uploadImage(
        context: Context,
        bitmap: Bitmap,
        folder: String = "rooms",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Thread {
            try {
                val fileName = "${folder}/${UUID.randomUUID()}.jpg"

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val imageBytes = baos.toByteArray()

                val url = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/${SupabaseConfig.STORAGE_BUCKET}/$fileName"
                Log.d(TAG, "Uploading to: $url (${imageBytes.size} bytes)")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "image/jpeg")
                    .post(imageBytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val publicUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET}/$fileName"
                    Log.d(TAG, "Upload success: $publicUrl")
                    onSuccess(publicUrl)
                } else {
                    Log.e(TAG, "Upload failed: ${response.code} $responseBody")
                    onFailure(Exception("Upload failed: ${response.code} $responseBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload exception", e)
                onFailure(e)
            }
        }.start()
    }

    fun uploadImageFromUri(
        context: Context,
        uri: Uri,
        folder: String = "rooms",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Thread {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open image")
                val imageBytes = inputStream.readBytes()
                inputStream.close()

                val fileName = "${folder}/${UUID.randomUUID()}.jpg"
                val url = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/${SupabaseConfig.STORAGE_BUCKET}/$fileName"
                Log.d(TAG, "Uploading URI to: $url (${imageBytes.size} bytes)")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "image/jpeg")
                    .post(imageBytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val publicUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET}/$fileName"
                    Log.d(TAG, "Upload success: $publicUrl")
                    onSuccess(publicUrl)
                } else {
                    Log.e(TAG, "Upload failed: ${response.code} $responseBody")
                    onFailure(Exception("Upload failed: ${response.code} $responseBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload URI exception", e)
                onFailure(e)
            }
        }.start()
    }

    fun deleteImage(
        imageUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Thread {
            try {
                val prefix = "/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET}/"
                val filePath = imageUrl.substringAfter(prefix)

                val url = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/${SupabaseConfig.STORAGE_BUCKET}/$filePath"
                Log.d(TAG, "Deleting: $url")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d(TAG, "Delete success")
                    onSuccess()
                } else {
                    Log.e(TAG, "Delete failed: ${response.code}")
                    onFailure(Exception("Delete failed: ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Delete exception", e)
                onFailure(e)
            }
        }.start()
    }

    fun uploadMultipleImages(
        context: Context,
        bitmaps: List<Bitmap>,
        folder: String = "rooms",
        onAllUploaded: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val urls = mutableListOf<String>()
        var completedCount = 0
        val totalCount = bitmaps.size

        if (bitmaps.isEmpty()) {
            onAllUploaded(emptyList())
            return
        }

        for (bitmap in bitmaps) {
            uploadImage(context, bitmap, folder,
                onSuccess = { url ->
                    synchronized(urls) {
                        urls.add(url)
                        completedCount++
                        if (completedCount == totalCount) {
                            onAllUploaded(urls)
                        }
                    }
                },
                onFailure = { e ->
                    onFailure(e)
                }
            )
        }
    }
}
