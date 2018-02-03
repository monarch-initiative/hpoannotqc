package org.monarchinitiative.hpoannotqc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class HpoAnnotQcApplicationRunner implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger(HpoAnnotQcApplicationRunner.class);


    private final HpoAnnotQc hpoAnnotQc;

    public HpoAnnotQcApplicationRunner(HpoAnnotQc hpoAnnQc) {
        this.hpoAnnotQc=hpoAnnQc;
    }


    @Override
    public void run(ApplicationArguments appArgs) {

        if (appArgs.containsOption("help") || appArgs.containsOption("h")) {
            System.out.println("HELP! Help write this help!");
        } else {
            hpoAnnotQc.run();
        }

    }
}
