#ifndef HMI_FILTERS_H
#define HMI_FILTERS_H

#include "SampleGenerator.h"
#include <math.h>

class Shelf: public SampleGenerator{
public:

    float getNextSample() override{
        return getFilteredSample(0);
    }

    float getFilteredSample(float currentSample){
        y = b0 * currentSample + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        x2 = x1;
        x1 = currentSample;
        y2 = y1;
        y1 = y;
        return y;
    }

    void reset(){
        x1 = x2 = y1 = y2 = 0.0f;
    }

    void configure(float center_freq, float Q, float gainDB) {
        reset();
        Q = (Q == 0) ? 1e-9 : Q;
        mQ = Q;
        mGainDB = gainDB;
        reconfigure(center_freq);
    }

    void reconfigure(float cf){
        center_freq = cf;
        // only used for peaking and shelving filters
        gain_abs = pow(10, mGainDB / 40);
        float omega = 2 * M_PI * center_freq / mSampleRate;
        float sn = sinf(omega);
        float cs = cosf(omega);
        //float alpha = sn / (2 * mQ);
        float beta = sqrt(gain_abs + gain_abs);

        b0 = gain_abs * ((gain_abs + 1) - (gain_abs - 1) * cs + beta * sn);
        b1 = 2 * gain_abs * ((gain_abs - 1) - (gain_abs + 1) * cs);
        b2 = gain_abs * ((gain_abs + 1) - (gain_abs - 1) * cs - beta * sn);
        a0 = (gain_abs + 1) + (gain_abs - 1) * cs + beta * sn;
        a1 = -2 * ((gain_abs - 1) + (gain_abs + 1) * cs);
        a2 = (gain_abs + 1) + (gain_abs - 1) * cs - beta * sn;

        b0 /= a0;
        b1 /= a0;
        b2 /= a0;
        a1 /= a0;
        a2 /= a0;
    }

protected:


    float a0, a1, a2, b0, b1, b2;
    float x1, x2, y, y1, y2;
    float gain_abs;
    float center_freq, mQ, mGainDB;
};

#endif //HMI_FILTERS_H
