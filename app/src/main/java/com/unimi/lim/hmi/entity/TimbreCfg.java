package com.unimi.lim.hmi.entity;

public class TimbreCfg {

    // TODO for UI purpose, refactor?
    private boolean checked = false;

    private String id;
    private String name;
    private float volume;
    private int harmonics;
    private LfoCfg tremolo;
    private LfoCfg vibrato;
    private EnvelopCfg volumeEnv;
    private EnvelopCfg pitchEnv;
    private EnvelopCfg harmonicsEnv;
    private EqualizerCfg equalizer;

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
