package com.cassio.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null

    private val micPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening() else setStatus("Preciso do microfone para ouvir seus comandos.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        tts = TextToSpeech(this, this)
        findViewById<Button>(R.id.listenButton).setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startListening()
            else micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
        findViewById<Button>(R.id.timeButton).setOnClickListener { tellTime() }
        findViewById<Button>(R.id.calculatorButton).setOnClickListener { openCalculator() }
        findViewById<Button>(R.id.youtubeButton).setOnClickListener { openYouTube() }
        findViewById<Button>(R.id.cameraButton).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.browserButton).setOnClickListener { openBrowser() }
        findViewById<Button>(R.id.galleryButton).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.settingsButton).setOnClickListener { openSettings() }
        findViewById<Button>(R.id.appsButton).setOnClickListener { openAppManager() }
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) tts.language = Locale("pt", "BR")
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("O reconhecimento de voz não está disponível neste celular.")
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { setStatus("Estou ouvindo...") }
            override fun onBeginningOfSpeech() { setStatus("Pode falar.") }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                handleCommand(matches?.firstOrNull()?.lowercase(Locale("pt", "BR")) ?: "")
            }
            override fun onError(error: Int) { setStatus("Não entendi. Toque novamente e tente falar mais perto do microfone.") }
            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun handleCommand(command: String) {
        setStatus("Comando: $command")
        when {
            command.contains("hora") -> tellTime()
            command.contains("calculadora") -> openCalculator()
            command.contains("youtube") -> openYouTube()
            command.contains("bateria") -> tellBattery()
            command.contains("configuração") || command.contains("configurações") -> openSettings()
            command.contains("câmera") || command.contains("camera") -> openCamera()
            command.contains("navegador") || command.contains("internet") -> openBrowser()
            command.contains("galeria") || command.contains("fotos") -> openGallery()
            command.contains("telefone") || command.contains("ligação") || command.contains("ligações") -> openPhone()
            command.contains("mensagens") || command.contains("mensagem") -> openMessages()
            command.contains("wi-fi") || command.contains("wifi") -> openWifi()
            command.contains("aumenta") && command.contains("volume") -> changeVolume(true)
            command.contains("aumentar") && command.contains("volume") -> changeVolume(true)
            command.contains("diminui") && command.contains("volume") -> changeVolume(false)
            command.contains("diminuir") && command.contains("volume") -> changeVolume(false)
            command.startsWith("abrir ") -> openSelectedApp(command.removePrefix("abrir ").trim())
            command.startsWith("abre ") -> openSelectedApp(command.removePrefix("abre ").trim())
            else -> respond("Ainda não conheço esse comando. Tente outro comando local.")
        }
    }

    private fun openAppManager() {
        startActivity(Intent(this, AppSelectionActivity::class.java))
    }

    private fun openSelectedApp(requestedName: String) {
        val selected = getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
            .getStringSet("selected_apps", emptySet()) ?: emptySet()

        if (selected.isEmpty()) {
            respond("Nenhum aplicativo foi autorizado ainda. Abra Gerenciar aplicativos e escolha os apps.")
            return
        }

        val apps = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }, 0
        ).filter { it.activityInfo.packageName in selected }

        val normalizedRequest = normalizeText(requestedName)
            .removePrefix("o ").removePrefix("a ")
            .removePrefix("app ").removePrefix("aplicativo ").trim()

        val matches = apps.filter {
            normalizeText(it.loadLabel(packageManager).toString()) == normalizedRequest ||
            normalizeText(it.loadLabel(packageManager).toString()).contains(normalizedRequest)
        }.distinctBy { it.activityInfo.packageName }

        when {
            matches.size == 1 -> {
                val app = matches.first()
                val launchIntent = packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
                if (launchIntent != null) {
                    respond("Abrindo " + app.loadLabel(packageManager).toString() + ".")
                    startActivity(launchIntent)
                } else respond("Não consegui abrir esse aplicativo.")
            }
            matches.size > 1 -> respond("Encontrei mais de um aplicativo com esse nome. Seja mais específico.")
            else -> respond("Esse aplicativo não está na sua lista de aplicativos autorizados.")
        }
    }

    private fun normalizeText(value: String): String {
        return Normalizer.normalize(value.lowercase(Locale("pt", "BR")), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .trim()
    }

    private fun tellTime() {
        val time = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())
        respond("Agora são $time.")
    }

    private fun tellBattery() {
        val manager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        respond("A bateria está em " + level + " por cento.")
    }

    private fun openCalculator() {
        launchOrRespond(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CALCULATOR) },
            "Abrindo a calculadora.", "Não encontrei uma calculadora instalada.")
    }

    private fun openYouTube() {
        val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        if (launchIntent != null) {
            respond("Abrindo o YouTube.")
            startActivity(launchIntent)
        } else openUrl("https://www.youtube.com", "O aplicativo do YouTube não está instalado. Abrindo o site.")
    }

    private fun openCamera() {
        launchOrRespond(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Abrindo a câmera.", "Não encontrei um aplicativo de câmera.")
    }

    private fun openBrowser() = openUrl("https://www.google.com", "Abrindo o navegador.")

    private fun openGallery() {
        launchOrRespond(Intent(Intent.ACTION_VIEW).apply { type = "image/*" },
            "Abrindo a galeria.", "Não encontrei uma galeria compatível.")
    }

    private fun openPhone() {
        launchOrRespond(Intent(Intent.ACTION_DIAL), "Abrindo o telefone.", "Não encontrei um aplicativo de telefone.")
    }

    private fun openMessages() {
        launchOrRespond(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) },
            "Abrindo as mensagens.", "Não encontrei um aplicativo de mensagens.")
    }

    private fun openWifi() {
        launchOrRespond(Intent(Settings.ACTION_WIFI_SETTINGS),
            "Abrindo as configurações de Wi-Fi.", "Não consegui abrir as configurações de Wi-Fi.")
    }

    private fun changeVolume(increase: Boolean) {
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
            if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI)
        respond(if (increase) "Aumentando o volume." else "Diminuindo o volume.")
    }

    private fun openSettings() {
        respond("Abrindo as configurações.")
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun openUrl(url: String, message: String) {
        launchOrRespond(Intent(Intent.ACTION_VIEW, Uri.parse(url)), message, "Não encontrei um navegador instalado.")
    }

    private fun launchOrRespond(intent: Intent, success: String, failure: String) {
        if (intent.resolveActivity(packageManager) != null) {
            respond(success)
            startActivity(intent)
        } else respond(failure)
    }

    private fun respond(message: String) {
        setStatus(message)
        if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response")
    }

    private fun setStatus(message: String) {
        runOnUiThread { status.text = message }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}