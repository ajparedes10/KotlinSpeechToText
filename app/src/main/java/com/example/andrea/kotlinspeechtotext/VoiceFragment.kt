package com.example.andrea.kotlinspeechtotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import java.text.Normalizer
import java.util.Locale


class VoiceFragment : Fragment() {

    var result: TextView? = null
    var progressBar: ProgressBar? = null

    var toSpeech: TextToSpeech? = null
    var res: Int = 0
    var sr: SpeechRecognizer? = null
    var text: String? = null
    val LOG_TAG = "SpeechToTextActivity"
    val options = arrayOf("llevame a", "donde estoy", "frente")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // infla el layout del fragmento
        val myView = inflater.inflate(R.layout.fragment_voice, container, false)
        result = myView.findViewById(R.id.textSpeech)
        progressBar = myView.findViewById(R.id.progressBar)
        progressBar!!.visibility = View.INVISIBLE

        //crea el SpeechRecognizer y su listener
        sr = SpeechRecognizer.createSpeechRecognizer(activity)
        sr!!.setRecognitionListener(listener())
        return myView
    }

    override fun onDestroy() {
        sr!!.destroy()
        sr = null
        super.onDestroy()
    }

    /*
     * listener del SpeechRecognizer.
     */
    internal inner class listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {

            Log.d(TAG, "onBeginningOfSpeech")
            progressBar!!.isIndeterminate = false
            progressBar!!.max = 10
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged")
            progressBar!!.progress = rmsdB.toInt()
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.d(TAG, "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech")
            progressBar!!.isIndeterminate = true
            progressBar!!.visibility = View.INVISIBLE
        }

        override fun onError(error: Int) {
            Log.d(TAG, "error $error")
        }

        override fun onResults(results: Bundle) {

            Log.d(TAG, "onResults $results")
            // Lista de resultados obtenidos por el SpeechRecognizer
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            //guarda el primer resultado
            if (matches != null && !matches.isEmpty())
                logthis(matches[0])

        }

        override fun onPartialResults(partialResults: Bundle) {
            Log.d(TAG, "onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.d(TAG, "onEvent $eventType")
        }
    }

    fun recordSpeak() {
        //si la app no tiene permiso para usar microfono, lo pide
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "asking for permissions")
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.RECORD_AUDIO), MainActivity.REQUEST_RECORD_PERMISSION)
        } else {
            progressBar!!.visibility = View.VISIBLE
            progressBar!!.isIndeterminate = true

            //se crea el intent para escuchar al usuario
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            sr!!.startListening(intent)
            Log.i(TAG, "Intent sent")
        }
    }

    /*
     * método para añadir la información resivida al TextView, analizarla y reproducirla
     */
    fun logthis(newinfo: String) {
        if (newinfo.compareTo("") != 0) {
            text = newinfo
            analizeSpeech()
            result!!.text = text
            textToVoice(text)
        }
    }

    /*
     * método que debe revisar el comando recibido por el usuario
     */
    fun analizeSpeech() {
        var speech = text
        speech = Normalizer.normalize(speech, Normalizer.Form.NFD)
        speech = Regex("\\p{InCombiningDiacriticalMarks}+").replace(speech, "")
        speech = speech.toLowerCase()
        var opt = -1
        Log.i(LOG_TAG, "analize: normalizó a $speech")
        var i = 0
        while (i < options.size && opt == -1) {
            if (speech.contains(options[i])) opt = i
            i++
        }
        Log.i(LOG_TAG, "analize: opcion $opt")
        when (opt) {
            0 -> if (speech.contains(" a ")) {
                //puede ser a, al, a la
                val div = speech.split(" a ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                text = "calculando ruta a destino: " + div[1]
            }
            1 -> text = "te encuentras en: ..."
            2 -> text = "al frente tienes una persona"

            else -> text = "Lo siento, esa no es una opción disponible. Intenta de nuevo porfavor"
        }
        Log.i(LOG_TAG, "text es: " + text!!)
    }

    /*
     * método para pasar de texto a voz
     */
    fun textToVoice(message: String?) {
        toSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                res = toSpeech!!.setLanguage(Locale.getDefault())
            }
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(activity, "Tu dispositivo no soporta la función de text to speech", Toast.LENGTH_SHORT).show()
            } else if (message != null) {
                Log.i(LOG_TAG, "entra else textToSpeach")
                toSpeech!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        })
    }

    companion object {

        private val TAG = "MainFragment"
    }

}// Required empty public constructor
