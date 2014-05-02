package com.babyrhythm;

import java.io.File;

public class Config {
    
    public int port;
    public File fsRoot;

    public static Config parseDefault() {
        Config cfg = new Config();
        cfg.port=2424;
        cfg.fsRoot = Utils.tildeExpand("~/babyrhythm");
        if (!cfg.fsRoot.exists())
            if( !cfg.fsRoot.mkdir())
                System.err.println("Failed to create dir "+cfg.fsRoot.getAbsolutePath());
        
        return cfg;
    }

}
