package com.example.speechnodialog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int REQUEST_RECORD_PERMISSION = 100;
    private static final String IMAGE_UTTERANCE_PREFIX = "imutt_";
    private static final String STOP_UTTERANCE = "stop";
    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private ImageView imageView;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private ArrayList<ImageUtterance> imageUtterances;
    Random r;
    private ImageUtterance currentImage;
    private ToggleButton toggleContinue;
    private TextView wasCorrectText;
    private CountDownTimer countDownTimer;
    private TextView countDownText;
    private int correct;
    private int incorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        returnedText = findViewById(R.id.textView1);
        wasCorrectText = findViewById(R.id.textView2);
        progressBar = findViewById(R.id.progressBar1);
        toggleButton = findViewById(R.id.toggleButton1);
        toggleContinue = findViewById(R.id.toggleContinue);
        imageView = findViewById(R.id.imageView1);
        countDownText = findViewById(R.id.countDownText);

        r = new Random();
        correct = 0;
        incorrect = 0;

        progressBar.setVisibility(View.INVISIBLE);
        toggleButton.setEnabled(false);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    ActivityCompat.requestPermissions
                            (MainActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_PERMISSION);
                    toggleContinue.setChecked(true);
                    toggleButton.setEnabled(true);
                    showNextImage();
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });

        toggleContinue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    countDownTimer.start();
                    toggleButton.setChecked(true);
                }
            }
        });

        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                countDownText.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                toggleContinue.setChecked(false);
                updateScore();
            }
        };

        initImages();
    }

    private void initImages() {
        imageUtterances = new ArrayList<>();
        Field[] drawablesFields = R.drawable.class.getFields();

        String text = "";
        String nameAux = "";
        for (Field field : drawablesFields) {
            try {
                if(field != null && field.getName() != null && field.getName().startsWith(IMAGE_UTTERANCE_PREFIX)){
                    ImageUtterance imageUtterance = new ImageUtterance();
                    imageUtterance.setDrawable(getResources().getDrawable(field.getInt(null)));
                    nameAux = field.getName().replace(IMAGE_UTTERANCE_PREFIX, "");
                    nameAux = nameAux.replace("_", " ");
                    imageUtterance.setName(nameAux);
                    imageUtterances.add(imageUtterance);
                    text += imageUtterance.getName() + "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //returnedText.setText(text);
    }

    private void showNextImage() {
        int nextIndex = r.nextInt(imageUtterances.size());
        currentImage = imageUtterances.get(nextIndex);
        imageView.setImageDrawable(currentImage.getDrawable());
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        try {
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String utterance = matches.get(0).toLowerCase().trim();


            if (STOP_UTTERANCE.equals(utterance)){
                toggleContinue.setChecked(false);
                updateScore();
            }
            else {
                String wasCorrect = "";
                if (utterance.equals(currentImage.getName())) {
                    wasCorrect = utterance + " ✅ Correct!";
                    correct++;
                } else {
                    wasCorrect = utterance + " ❌ Incorrect! ( was " + currentImage.getName() + ")";
                    incorrect++;
                }

                wasCorrectText.setText(wasCorrect);
                updateScore();

                if (toggleContinue.isChecked()) {
                    toggleButton.setChecked(true);
                    showNextImage();
                }
            }
        }
        catch(Exception e){
            Log.i(LOG_TAG, "onResults error");
        }
    }

    private void updateScore() {
        String score = "Correct: " + correct + ( incorrect > 0 ? " Incorrect: " + incorrect : "");
        returnedText.setText(score);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
