package org.example;

import java.io.*;
import javax.sound.sampled.*;
import javax.swing.JOptionPane;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws IOException {
        Scanner sc=new Scanner(System.in);
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        try {
            AudioFormat a = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

            DataLine.Info df = new DataLine.Info(TargetDataLine.class, a);
            if (!AudioSystem.isLineSupported(df)) {
                System.out.println("Not supported");
            }

            TargetDataLine td = (TargetDataLine) AudioSystem.getLine(df);
            td.open();
            JOptionPane.showMessageDialog(null, "Hit OK to start recording");
            td.start();

            Thread audioRecorder = new Thread(() -> {
                AudioInputStream rs = new AudioInputStream(td);
                File outp = new File("record.wav");
                try {
                    AudioSystem.write(rs, AudioFileFormat.Type.WAVE, outp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Stopped recording");
            });
            audioRecorder.start();
            JOptionPane.showMessageDialog(null, "Hit OK to stop recording");
            td.stop();
            td.close();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        System.out.println("enter  question");
/*
        // for converting speech to text
       Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("file:///D:/efiles/cmusphinx-en-in-5.2/acousticmodel/en_in.cd_cont_5000");
        configuration.setDictionaryPath("file:///D:/efiles/cmusphinx-en-in-5.2/dicitionarymodel/en_in.dic");                // kinda not recognzing correctly.
        configuration.setLanguageModelPath("file:///D:/efiles/cmusphinx-en-in-5.2/languagemodel/en-us.lm.bin");


        try {
            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);

            File audioFile = new File("record.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            recognizer.startRecognition(true);
            String recognizedText = recognizer.getResult().getHypothesis();
            recognizer.stopRecognition();

            System.out.println("Recognized Text: " + recognizedText);
        } catch (Exception e) {
            e.printStackTrace();
        }

*/
        String searchQuery = sc.nextLine();
        String encodedQuery = URLEncoder.encode(searchQuery, "UTF-8");
        String apiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=" + encodedQuery;


        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JsonElement jsonElement = JsonParser.parseString(response.toString());
        JsonObject jsonResponse = jsonElement.getAsJsonObject();

        if (jsonResponse.has("query")) {
            JsonObject queryObject = jsonResponse.getAsJsonObject("query");
            if (queryObject.has("search")) {
                JsonArray searchResults = queryObject.getAsJsonArray("search");
                for (JsonElement resultElement : searchResults) {
                    JsonObject result = resultElement.getAsJsonObject();
                    String title = result.get("title").getAsString();
                    String snippet = result.get("snippet").getAsString();
                    String cleanedSnippet = Jsoup.parse(snippet).text();
                    System.out.println("Title: " + title);
                    System.out.println("Snippet: " + cleanedSnippet);

                    speak(cleanedSnippet);
                    System.out.println("--------");
                }
            }
        }
    }
    private static void speak(String text) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice voice = voiceManager.getVoice("kevin16");
        voice.setPitch(80);
        voice.setDurationStretch(1);
        voice.setRate(200);
        try{

            if (voice != null) {
                voice.allocate();
                voice.speak(text);
                voice.deallocate();
            }
            else {
                System.err.println("Error: Voice not available.");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}

















