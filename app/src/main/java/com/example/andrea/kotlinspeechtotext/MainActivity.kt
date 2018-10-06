package com.example.andrea.kotlinspeechtotext

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast


class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var voiceFragment: VoiceFragment
    var speakButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //boton principal
        speakButton = findViewById(R.id.btnMic)
        speakButton!!.setOnClickListener(this)

        // crea el fragmento de voz
        voiceFragment = VoiceFragment()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, voiceFragment).commit()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            //verific si hay permiso para usar el microfono
            REQUEST_RECORD_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    voiceFragment.recordSpeak()
                } else {
                    Toast.makeText(applicationContext, "Tu dispositivo no permite la función de text to speech", Toast.LENGTH_SHORT).show()
                }
            }
        }// los otros casos pueden ser otros permisos que la palicación necesite verificar que tiene
    }

    /*
    onClick del boton principal
     */
    override fun onClick(v: View) {
        if (v.id == R.id.btnMic) {
            voiceFragment.recordSpeak()
        }
    }

    companion object {

        val REQUEST_RECORD_PERMISSION = 100
    }

}
