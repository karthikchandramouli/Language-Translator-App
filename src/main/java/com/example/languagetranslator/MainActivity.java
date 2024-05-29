package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextInputEditText sourcedt;
    private TextView Tvtranslated;
    private MaterialButton translatedbtn;
    private Spinner fromspinner, tospinner;
    private ImageView micimg;
    private ImageView spek;

    private TextToSpeech textToSpeech;

    String[] fromlanguages = {"From", "Arabic", "Bengali", "Bulgarian", "Chinese", "Danish", "English", "French", "German",
            "Gujarati", "Hindi", "Italian", "Japanese", "Korean", "Latvian", "Malay", "Marathi", "Polish", "Portuguese", "Romanian",
            "Russian", "Tamil", "Telugu", "Urdu", "Kannada"};
    String[] tolanguages = {"To", "Arabic", "Bengali", "Bulgarian", "Chinese", "Danish", "English", "French", "German",
            "Gujarati", "Hindi", "Italian", "Japanese", "Korean", "Latvian", "Malay", "Marathi", "Polish", "Portuguese", "Romanian",
            "Russian", "Tamil", "Telugu", "Urdu", "Kannada"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languagecode, fromlanguagecode, tolanguagecode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourcedt = findViewById(R.id.edttext);
        Tvtranslated = findViewById(R.id.tvtranslated);
        translatedbtn = findViewById(R.id.btntranslate);
        fromspinner = findViewById(R.id.fromspinner);
        tospinner = findViewById(R.id.tospinner);
        micimg = findViewById(R.id.idmic);
        spek = findViewById(R.id.spek);

        textToSpeech = new TextToSpeech(this, this);

        spek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });

        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlanguagecode = getLanguagecode(fromlanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromlanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromAdapter);

        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                tolanguagecode = getLanguagecode(tolanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, tolanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toAdapter);

        translatedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tvtranslated.setText("");
                if (sourcedt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                } else if (fromlanguagecode == 0) {
                    Toast.makeText(MainActivity.this, "Please select your Language", Toast.LENGTH_SHORT).show();
                } else if (tolanguagecode == 0) {
                    Toast.makeText(MainActivity.this, "Please select the language to translate", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromlanguagecode, tolanguagecode, sourcedt.getText().toString());
                }
            }
        });

        micimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Convert into Text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourcedt.setText(result.get(0));
            }
        }
    }

    private void speakOut() {
        String text = Tvtranslated.getText().toString().trim();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            } else {
                // TTS engine successfully initialized
                // You can also move the code to initialize the language spinners here
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void translateText(int fromLanguageCode, int toLanguageCode, String sourceText) {
        Tvtranslated.setText("Downloading Model....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Tvtranslated.setText("Translating.....");
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Tvtranslated.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to download a language model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguagecode(String language) {
        int languagecode = 0;
        switch (language) {
            case "Arabic":
                languagecode = FirebaseTranslateLanguage.AR;
                break;
            case "Bengali":
                languagecode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languagecode = FirebaseTranslateLanguage.BG;
                break;
            case "Chinese":
                languagecode = FirebaseTranslateLanguage.CA;
                break;
            case "Danish":
                languagecode = FirebaseTranslateLanguage.DA;
                break;
            case "English":
                languagecode = FirebaseTranslateLanguage.EN;
                break;
            case "French":
                languagecode = FirebaseTranslateLanguage.FR;
                break;
            case "German":
                languagecode = FirebaseTranslateLanguage.DE;
                break;
            case "Gujarati":
                languagecode = FirebaseTranslateLanguage.GU;
                break;
            case "Hindi":
                languagecode = FirebaseTranslateLanguage.HI;
                break;
            case "Italian":
                languagecode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languagecode = FirebaseTranslateLanguage.JA;
                break;
            case "Korean":
                languagecode = FirebaseTranslateLanguage.KO;
                break;
            case "Latvian":
                languagecode = FirebaseTranslateLanguage.LV;
                break;
            case "Malay":
                languagecode = FirebaseTranslateLanguage.MS;
                break;
            case "Marathi":
                languagecode = FirebaseTranslateLanguage.MR;
                break;
            case "Polish":
                languagecode = FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languagecode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languagecode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languagecode = FirebaseTranslateLanguage.RU;
                break;
            case "Tamil":
                languagecode = FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languagecode = FirebaseTranslateLanguage.TE;
                break;
            case "Urdu":
                languagecode = FirebaseTranslateLanguage.UR;
                break;
            case "Kannada":
                languagecode = FirebaseTranslateLanguage.KN;
                break;
            default:
                languagecode = 0;
                break;
        }
        return languagecode;
    }
}
