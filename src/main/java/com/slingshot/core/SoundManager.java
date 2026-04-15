package com.slingshot.core;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class SoundManager {

    private static SoundManager instance;

    private MediaPlayer bgMusicPlayer;
    private AudioClip sniperSound;
    private AudioClip artillerySound;
    private AudioClip explosionSound;

    private SoundManager() {
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        try {
            // 1. Cargar Música de Fondo
            URL musicUrl = getClass().getResource("/assets/sounds/bg_music.wav");
            if (musicUrl != null) {
                Media bgMedia = new Media(musicUrl.toString());
                bgMusicPlayer = new MediaPlayer(bgMedia);
                bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // LOOP INFINITO
                bgMusicPlayer.setVolume(0.5); // Volumen normal de menús
            }

            // 2. Cargar Efectos (AudioClip es mejor para SFX rápidos)
            URL sniperUrl = getClass().getResource("/assets/sounds/shoot_sniper.wav");
            if (sniperUrl != null) sniperSound = new AudioClip(sniperUrl.toString());

            URL artilleryUrl = getClass().getResource("/assets/sounds/shoot_artillery.mp3");
            if (artilleryUrl != null) artillerySound = new AudioClip(artilleryUrl.toString());

            URL explosionUrl = getClass().getResource("/assets/sounds/explosion.wav");
            if (explosionUrl != null) explosionSound = new AudioClip(explosionUrl.toString());

        } catch (Exception e) {
            System.err.println("Error cargando sonidos. Revisa la ruta de los archivos.");
        }
    }

    // --- CONTROLES DE MÚSICA ---
    public void playBGM() {
        if (bgMusicPlayer != null && bgMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            bgMusicPlayer.play();
        }
    }

    public void setMenuVolume() {
        if (bgMusicPlayer != null) bgMusicPlayer.setVolume(0.5); // 50%
    }

    public void setGameVolume() {
        if (bgMusicPlayer != null) bgMusicPlayer.setVolume(0.15); // 15% (Bajito para escuchar balas)
    }

    // --- CONTROLES DE EFECTOS ---
    public void playSniper() {
        if (sniperSound != null) sniperSound.play(0.8); // Volumen 80%
    }

    public void playArtillery() {
        if (artillerySound != null) artillerySound.play(0.9); // Volumen 90%
    }

    public void playExplosion() {
        if (explosionSound != null) explosionSound.play(1.0); // Volumen 100%
    }
}