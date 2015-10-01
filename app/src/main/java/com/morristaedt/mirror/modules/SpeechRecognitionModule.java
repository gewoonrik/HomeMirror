package com.morristaedt.mirror.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rik on 01/10/15.
 */
public class SpeechRecognitionModule {
    private SpeechRecognizer mSpeechRecognizer;
    private Context mContext;
    private static final String TAG = "SpeechRecognitionModule";
    private Map<String, SpeechRecognitionListener> listenerMap  = new HashMap<>();
    private List<String> mActions = new ArrayList<>();

    private static Intent mIntent;
    static {
        mIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "speech begin");
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "speech ended");
            SpeechRecognitionModule.this.start();

        }

        @Override
        public void onError(int error) {
            SpeechRecognitionModule.this.start();
        }

        @Override
        public void onResults(Bundle results) {
            List<String> strResults =  results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String action = matchWords(strResults, mActions);
            if(action != null)  {
                listenerMap.get(action).onRecognizedSpeech();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


    private String matchWords(List<String> results, List<String> potentialActions) {
        for(String result : results)    {
            for(String potentialAction : potentialActions)  {
                if(result.contains(potentialAction))    {
                    return potentialAction;
                }
            }
        }
        return null;
    }

    public SpeechRecognitionModule(Context context) {
        mContext = context;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        mSpeechRecognizer.setRecognitionListener(listener);
    }

    public void addListener(String word, SpeechRecognitionListener listener)    {
        listenerMap.put(word, listener);
        mActions.add(word);
    }

    public void start() {
        mSpeechRecognizer.startListening(mIntent);
    }

    public void stop()  {
        mSpeechRecognizer.stopListening();
    }

    public interface SpeechRecognitionListener    {
        public void onRecognizedSpeech();
    }

}
