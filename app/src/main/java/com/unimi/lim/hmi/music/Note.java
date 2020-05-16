package com.unimi.lim.hmi.music;

/**
 * Note frequencies from octave 0 to octave 6
 */
public enum Note {
    C0(16.35),
    Cd0(17.32),
    D0(18.35),
    Dd0(19.45),
    E0(20.60),
    F0(21.83),
    Fd0(23.12),
    G0(24.50),
    Gd0(25.96),
    A0(27.50),
    Ad0(29.14),
    B0(30.87),
    C1(32.70),
    Cd1(34.65),
    D1(36.71),
    Dd1(38.89),
    E1(41.20),
    F1(43.65),
    Fd1(46.25),
    G1(49.00),
    Gd1(51.91),
    A1(55.00),
    Ad1(58.27),
    B1(61.74),
    C2(65.41),
    Cd2(69.30),
    D2(73.42),
    Dd2(77.78),
    E2(82.41),
    F2(87.31),
    Fd2(92.50),
    G2(98.00),
    Gd2(103.83),
    A2(110.00),
    Ad2(116.54),
    B2(123.47),
    C3(130.81),
    Cd3(138.59),
    D3(146.83),
    Dd3(155.56),
    E3(164.81),
    F3(174.61),
    Fd3(185.00),
    G3(196.00),
    Gd3(207.65),
    A3(220.00),
    Ad3(233.08),
    B3(246.94),
    C4(261.63),
    Cd4(277.18),
    D4(293.66),
    Dd4(311.13),
    E4(329.63),
    F4(349.23),
    Fd4(369.99),
    G4(392.00),
    Gd4(415.30),
    A4(440.00),
    Ad4(466.16),
    B4(493.88),
    C5(523.25),
    Cd5(554.37),
    D5(587.33),
    Dd5(622.25),
    E5(659.25),
    F5(698.46),
    Fd5(739.99),
    G5(783.99),
    Gd5(830.61),
    A5(880.00),
    Ad5(932.33),
    B5(987.77),
    C6(1046.50),
    Cd6(1108.73),
    D6(1174.66),
    Dd6(1244.51),
    E6(1318.51),
    F6(1396.91),
    Fd6(1479.98),
    G6(1567.98),
    Gd6(1661.22),
    A6(1760.00),
    Ad6(1864.66),
    B6(1975.53),
    ;

    private final double frequency;

    Note(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }
}
