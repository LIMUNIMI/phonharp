package com.unimi.lim.hmi.entity;

public class Timbre {

    // This field is strictly related to ui purposes
    // Since there is only one field we leave it here and avoid to create decorator objects
    private transient boolean checked = false;

    private String id;
    private String name;
    private float volume = 1;
    private float harmonics = 0;
    private Lfo tremolo = new Lfo(0, 0);
    private Lfo vibrato = new Lfo(0, 0);
    private Asr volumeAsr = new Asr(1, 0, 0, 0);
    private Asr pitchAsr = new Asr(0, 0, 0, 0);
    private Asr harmonicsAsr = new Asr(0, 0, 0, 0);
    private Equalizer equalizer = new Equalizer();

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

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getHarmonics() {
        return harmonics;
    }

    public void setHarmonics(float harmonics) {
        this.harmonics = harmonics;
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

    public static class Lfo {

        private float rate;
        private int depth;

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

    public static class Asr {

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

        public void setInitialValue(int initialValue) {
            this.initialValue = initialValue;
        }

        public float getFinalValue() {
            return finalValue;
        }

        public void setFinalValue(int finalValue) {
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

    public static class Equalizer {

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
