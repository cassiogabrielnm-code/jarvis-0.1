package com.cassio.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null

    private val micPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
            else setStatus("Preciso do microfone para ouvir seus comandos.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.listenButton).setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                micPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        findViewById<Button>(R.id.timeButton).setOnClickListener { tellTime() }
        findViewById<Button>(R.id.calculatorButton).setOnClickListener { openCalculator() }
        findViewById<Button>(R.id.youtubeButton).setOnClickListener { openYouTube() }
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale("pt", "BR")
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("O reconhecimento de voz não está disponível neste celular.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                setStatus("Estou ouvindo...")
            }

            override fun onBeginningOfSpeech() {
                setStatus("Pode falar.")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull()?.lowercase(Locale("pt", "BR")) ?: ""
                handleCommand(command)
            }

            override fun onError(error: Int) {
                setStatus("Não entendi. Toque novamente e tente falar mais perto do microfone.")
            }

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
            else -> respond("Ainda não conheço esse comando. Esta é a versão 0.1.")
        }
    }

    private fun tellTime() {
        val time = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())
        respond("Agora são $time.")
    }

    private fun tellBattery() {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        respond("A bateria está em $level por cento.")
    }

    private fun openCalculator() {
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
        }
        if (intent.resolveActivity(packageManager) != null) {
            respond("Abrindo a calculadora.")
            startActivity(intent)
        } else {
            respond("Não encontrei uma calculadora instalada.")
        }
    }

    private fun openYouTube() {
        val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        if (launchIntent != null) {
            respond("Abrindo o YouTube.")
            startActivity(launchIntent)
        } else {
            respond("O YouTube não está instalado.")
        }
    }

    private fun openSettings() {
        respond("Abrindo as configurações.")
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun respond(message: String) {
        setStatus(message)
        if (::tts.isInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response")
        }
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
