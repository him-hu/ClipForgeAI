package com.clipforge.ai

import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

class MainActivity : ComponentActivity() {
    private var inputUri: Uri? = null
    private var downloadId: Long = -1L
    private lateinit var status: TextView
    private lateinit var urlInput: EditText
    private lateinit var startInput: EditText
    private lateinit var endInput: EditText
    private lateinit var cutButton: Button
    private var activeTransformer: Transformer? = null

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            inputUri = uri
            status.text = "Video selected. Start/end seconds enter karke Export dabao."
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val uri = manager.getUriForDownloadedFile(downloadId)
            if (uri != null) {
                inputUri = uri
                status.text = "Direct video downloaded. Ab clip export kar sakte ho."
            } else {
                status.text = "Download fail hua. Link direct MP4/WebM file ka hona chahiye."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            RECEIVER_NOT_EXPORTED)
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(0xFF10131B.toInt())
        }
        fun label(text: String, size: Float = 16f): TextView =
            TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(0xFFF4F6FA.toInt())
                setPadding(0, 12, 0, 10)
            }
        fun field(hint: String): EditText = EditText(this).apply {
            this.hint = hint
            setSingleLine(true)
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF9AA3B2.toInt())
            setPadding(16, 8, 16, 8)
            setBackgroundColor(0xFF252B38.toInt())
        }
        fun button(text: String, action: () -> Unit): Button =
            Button(this).apply { this.text = text; setOnClickListener { action() } }

        layout.addView(label("CLIPFORGE AI", 27f))
        layout.addView(label("Gaming stream clipper • On-device export"))
        urlInput = field("Direct video URL (MP4/WebM)")
        layout.addView(urlInput)
        layout.addView(button("Download direct video link") { downloadDirectUrl() })
        layout.addView(button("Choose video from phone") { pickVideo.launch("video/*") })
        layout.addView(label("Clip start (seconds)"))
        startInput = field("Example: 125")
        startInput.setText("0")
        layout.addView(startInput)
        layout.addView(label("Clip end (seconds)"))
        endInput = field("Example: 155")
        endInput.setText("30")
        layout.addView(endInput)
        cutButton = button("EXPORT MP4 CLIP") { exportClip() }
        layout.addView(cutButton)
        status = label("Ready. Apni video select karo ya direct media-file URL do.")
        layout.addView(status)
        layout.addView(label("Note: YouTube watch links supported nahi hain. Sirf direct media-file URLs ya locally accessible videos use karo.", 13f))
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun downloadDirectUrl() {
        val raw = urlInput.text.toString().trim()
        if (!(raw.startsWith("https://") || raw.startsWith("http://"))) {
            status.text = "Valid direct video URL enter karo."
            return
        }
        try {
            val request = DownloadManager.Request(Uri.parse(raw))
                .setTitle("ClipForge source video")
                .setDescription("Downloading source video")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_MOVIES, "clipforge_source")
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadId = manager.enqueue(request)
            status.text = "Download started. Complete hone ka wait karo."
        } catch (e: Exception) {
            status.text = "Download start nahi hua: ${e.message}"
        }
    }

    private fun exportClip() {
        val uri = inputUri
        if (uri == null) {
            status.text = "Pehle video select ya download karo."
            return
        }
        val start = startInput.text.toString().toDoubleOrNull()
        val end = endInput.text.toString().toDoubleOrNull()
        if (start == null || end == null || start < 0 || end <= start) {
            status.text = "Start/end seconds sahi enter karo. End, start se bada hona chahiye."
            return
        }
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        if (outputDir.freeSpace < 150L * 1024L * 1024L) {
            status.text = "Storage bahut kam hai. Kam se kam 150 MB free karo."
            return
        }
        val output = File(outputDir, "ClipForge_${System.currentTimeMillis()}.mp4")
        cutButton.isEnabled = false
        status.text = "Clip export ho raha hai. App ko background mein mat bhejna."
        try {
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs((start * 1000).toLong())
                        .setEndPositionMs((end * 1000).toLong())
                        .build()
                ).build()
            val edited = EditedMediaItem.Builder(mediaItem).build()
            activeTransformer = Transformer.Builder(this)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                        runOnUiThread {
                            cutButton.isEnabled = true
                            status.text = "Export complete! File: ${output.absolutePath}"
                            shareButton(output)
                        }
                    }
                    override fun onError(
                        composition: androidx.media3.transformer.Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        runOnUiThread {
                            cutButton.isEnabled = true
                            status.text = "Export error: ${exportException.message}"
                            output.delete()
                        }
                    }
                }).build()
            activeTransformer?.start(edited, output.absolutePath)
        } catch (e: Exception) {
            cutButton.isEnabled = true
            status.text = "Could not export: ${e.message}"
            output.delete()
        }
    }

    private fun shareButton(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share exported clip"))
    }

    override fun onDestroy() {
        try { unregisterReceiver(downloadReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }
}
