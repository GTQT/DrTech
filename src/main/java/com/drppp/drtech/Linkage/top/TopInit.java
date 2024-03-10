package com.drppp.drtech.Linkage.top;

import com.drppp.drtech.Linkage.top.provider.NuclearInfoProvider;
import com.drppp.drtech.Linkage.top.provider.YotTankProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;



public class TopInit {
    public static void init() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new NuclearInfoProvider());
        oneProbe.registerProvider(new YotTankProvider());

    }
}
