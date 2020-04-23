package com.unimi.lim.hmi.entity;

public class TimbreCfg {

    // TODO for UI purpose, refactor?
    private boolean checked = false;

    private String id;
    private String name;
    private float volume = 1;
    private float harmonics = 0;
    private LfoCfg tremolo = new LfoCfg(0, 0);
    private LfoCfg vibrato = new LfoCfg(0, 0);
    private EnvelopCfg volumeEnv = new EnvelopCfg(1, 0, 0, 0);
    private EnvelopCfg pitchEnv = new EnvelopCfg(0, 0, 0, 0);
    private EnvelopCfg harmonicsEnv = new EnvelopCfg(0, 0, 0, 0);
    private EqualizerCfg equalizer = new EqualizerCfg();

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

    public LfoCfg getTremolo() {
        return tremolo;
    }

    public void setTremolo(LfoCfg tremolo) {
        this.tremolo = tremolo;
    }

    public LfoCfg getVibrato() {
        return vibrato;
    }

    public void setVibrato(LfoCfg vibrato) {
        this.vibrato = vibrato;
    }

    public EnvelopCfg getVolumeEnv() {
        return volumeEnv;
    }

    public void setVolumeEnv(EnvelopCfg volumeEnv) {
        this.volumeEnv = volumeEnv;
    }

    public EnvelopCfg getPitchEnv() {
        return pitchEnv;
    }

    public void setPitchEnv(EnvelopCfg pitchEnv) {
        this.pitchEnv = pitchEnv;
    }

    public EnvelopCfg getHarmonicsEnv() {
        return harmonicsEnv;
    }

    public void setHarmonicsEnv(EnvelopCfg harmonicsEnv) {
        this.harmonicsEnv = harmonicsEnv;
    }

    public EqualizerCfg getEqualizer() {
        return equalizer;
    }

    public void setEqualizer(EqualizerCfg equalizer) {
        this.equalizer = equalizer;
    }

    public static class LfoCfg {

        private float rate;
        private int depth;

        public LfoCfg() {
        }

        public LfoCfg(float rate, int depth) {
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

    public static class EnvelopCfg {

        private float initialValue;
        private float finalValue;
        private float attackTime;
        private float releaseTime;

        public EnvelopCfg() {

        }

        public EnvelopCfg(float initialValue, float finalValue, float attackTime, float releaseTime) {
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

    public static class EqualizerCfg {

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
