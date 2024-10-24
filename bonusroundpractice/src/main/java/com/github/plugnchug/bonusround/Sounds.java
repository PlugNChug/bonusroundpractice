package com.github.plugnchug.bonusround;

import java.net.*;

import javax.sound.sampled.*;


public class Sounds {
    private Clip clip;
    private URL url;

    // Constructor enables us to play multiple sounds at once
    public Sounds(String fileName) {
        try {
            clip = AudioSystem.getClip();
            url = getClass().getResource(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method overloads
    public void play(float volume) {
        play(volume, false, 0);
    }
    public void play(float volume, boolean loop) {
        play(volume, loop, 0);
    }
    public void play(float volume, boolean loop, int loopStart) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
                    if (!clip.isOpen()) {
                        clip.open(inputStream);
                    }
                    
                    // Set the volume
                    ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(20f * (float) Math.log10(volume));

                    // If loop=true is passed in, loop indefinitely
                    if (loop) {
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                        clip.setLoopPoints(loopStart, clip.getFrameLength() - 1);
                    }

                    clip.setFramePosition(0);
                    clip.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public boolean isPlaying() {
        if (clip != null && clip.isRunning()) {
            return true;
        } else {
            return false;
        }
    }
}
