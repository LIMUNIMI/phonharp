package com.unimi.lim.hmi.entity;

public class Timbre {

    // TODO for UI purpose, refactor?
    private boolean checked = false;

    private String id;
    private String name;
    private float volume;
    private int harmonics;
    private Lfo tremolo;
    private Lfo vibrato;
    private Envelop volumeEnv;
    private Envelop pitchEnv;
    private Envelop harmonicsEnv;
    private Equalizer equalizer;

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

    public int getHarmonics() {
        return harmonics;
    }

    public void setHarmonics(int harmonics) {
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

    public Envelop getVolumeEnv() {
        return volumeEnv;
    }

    public void setVolumeEnv(Envelop volumeEnv) {
        this.volumeEnv = volumeEnv;
    }

    public Envelop getPitchEnv() {
        return pitchEnv;
    }

    public void setPitchEnv(Envelop pitchEnv) {
        this.pitchEnv = pitchEnv;
    }

    public Envelop getHarmonicsEnv() {
        return harmonicsEnv;
    }

    public void setHarmonicsEnv(Envelop harmonicsEnv) {
        this.harmonicsEnv = harmonicsEnv;
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public void setEqualizer(Equalizer equalizer) {
        this.equalizer = equalizer;
    }

    @Override
    public String toString() {
        return "Timbre{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Lfo {

        private float rate;
        private int depth;

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

    public static class Envelop {

        private int startValue;
        private int endValue;
        private float attackTime;
        private float releaseTime;

        public int getStartValue() {
            return startValue;
        }

        public void setStartValue(int startValue) {
            this.startValue = startValue;
        }

        public int getEndValue() {
            return endValue;
        }

        public void setEndValue(int endValue) {
            this.endValue = endValue;
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
