package com.unimi.lim.hmi.entity;

import java.io.Serializable;

public class Timbre implements Serializable {

    // This field is strictly related to ui purposes
    // Since there is only one field we leave it here and avoid to create decorator objects
    private transient boolean checked = false;

    public final static float DEFAULT_TAP_HYSTERESIS = 0.05f;

    public enum Controller {
        NONE,
        VOLUME,
        PITCH,
        HARMONICS,
        VIBRATO,
        TREMOLO
    }

    private String id;
    private String name;
    private int volume = 100;
    private int harmonics = 50;
    private float tapHysteresis = DEFAULT_TAP_HYSTERESIS;
    private Lfo tremolo;
    private Lfo vibrato;
    private Asr volumeAsr;
    private Asr pitchAsr;
    private Asr harmonicsAsr;
    private Equalizer equalizer;
    private Controller controller1;
    private Controller controller2;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getHarmonics() {
        return harmonics;
    }

    public void setHarmonics(int harmonics) {
        this.harmonics = harmonics;
    }

    public float getTapHysteresis() {
        return tapHysteresis;
    }

    public void setTapHysteresis(float tapHysteresis) {
        this.tapHysteresis = tapHysteresis;
    }

    public Lfo getTremolo() {
        return tremolo;
    }

    public void setTremolo(Lfo tremolo) {
        this.tremolo = tremolo;
    }

    public Lfo getVibrato() {
        return vibrato;
    }

    public void setVibrato(Lfo vibrato) {
        this.vibrato = vibrato;
    }

    public Asr getVolumeAsr() {
        return volumeAsr;
    }

    public void setVolumeAsr(Asr volumeAsr) {
        this.volumeAsr = volumeAsr;
    }

    public Asr getPitchAsr() {
        return pitchAsr;
    }

    public void setPitchAsr(Asr pitchAsr) {
        this.pitchAsr = pitchAsr;
    }

    public Asr getHarmonicsAsr() {
        return harmonicsAsr;
    }

    public void setHarmonicsAsr(Asr harmonicsAsr) {
        this.harmonicsAsr = harmonicsAsr;
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public void setEqualizer(Equalizer equalizer) {
        this.equalizer = equalizer;
    }

    public Controller getController1() {
        return controller1;
    }

    public void setController1(Controller controller1) {
        this.controller1 = controller1;
    }

    public Controller getController2() {
        return controller2;
    }

    public void setController2(Controller controller2) {
        this.controller2 = controller2;
    }

    public static class Lfo implements Serializable {

        private float rate = 6;
        private int depth = 50;

        public Lfo() {
        }

        public Lfo(float rate, int depth) {
            this.rate = rate;
            this.depth = depth;
        }

        public float getRate() {
            return rate;
        }

        public void setRate(float rate) {
            this.rate = rate;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }
    }

    public static class Asr implements Serializable {

        private float initialValue;
        private float finalValue;
        private float attackTime;
        private float releaseTime;

        public Asr() {

        }

        public Asr(float initialValue, float finalValue, float attackTime, float releaseTime) {
            this.initialValue = initialValue;
            this.finalValue = finalValue;
            this.attackTime = attackTime;
            this.releaseTime = releaseTime;
        }

        public float getInitialValue() {
            return initialValue;
        }

        public void setInitialValue(float initialValue) {
            this.initialValue = initialValue;
        }

        public float getFinalValue() {
            return finalValue;
        }

        public void setFinalValue(float finalValue) {
            this.finalValue = finalValue;
        }

        public float getAttackTime() {
            return attackTime;
        }

        public void setAttackTime(float attackTime) {
            this.attackTime = attackTime;
        }

        public float getReleaseTime() {
            return releaseTime;
        }

        public void setReleaseTime(float releaseTime) {
            this.releaseTime = releaseTime;
        }
    }

    public static class Equalizer implements Serializable {

        private float bass;
        private float mid;
        private float high;

        public float getBass() {
            return bass;
        }

        public void setBass(float bass) {
            this.bass = bass;
        }

        public float getMid() {
            return mid;
        }

        public void setMid(float mid) {
            this.mid = mid;
        }

        public float getHigh() {
            return high;
        }

        public void setHigh(float high) {
            this.high = high;
        }
    }

}
