package com.drppp.drtech.intergations.top;

import com.drppp.drtech.intergations.top.provider.TopProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TopInit {
    public static void init() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new TopProvider());

    }
}
