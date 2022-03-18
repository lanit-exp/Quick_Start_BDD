package ru.lanit.at.utils.web.properties;

import org.aeonbits.owner.Config;


@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "classpath:config/configuration.properties",
        "system:properties",
        "system:env"
})
public interface Configurations extends Config {


    @Key("stand")
    @DefaultValue("GOOGLE")
    String getStand();


    @Key("screen_after_step")
    @DefaultValue("false")
    boolean screenAfterStep();

    @Key("remoteUrl")
    @DefaultValue("")
    String getRemoteURL();

    @Key("enableVNC")
    @DefaultValue("false")
    boolean getEnableVNC();

    @Key("enableVideo")
    @DefaultValue("false")
    boolean getEnableVideo();

    @Key("enableLog")
    @DefaultValue("false")
    boolean getEnableLog();

}
