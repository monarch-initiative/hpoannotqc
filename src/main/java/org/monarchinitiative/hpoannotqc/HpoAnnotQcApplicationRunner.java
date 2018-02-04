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
        } else if (!appArgs.containsOption("hpo")) {
            usage("[ERROR] need to pass the --hpo <path> option");
        } else if (!appArgs.containsOption("annotations")) {
            usage("[ERROR] need to pass the --annotations <path> option");
        } else {
            hpoAnnotQc.run();
        }

    }





    private static void usage(String msg) {
        System.out.println();
        System.out.println(msg);
        System.out.println();
        System.out.println("Usage: java -jar HpoAnnotQc.jar --hpo=<path> --annotations=<path>");
        System.out.println("where the paths are to the hp.obo file and to the " +
                "hpo-annotation-data/rare-diseases/annotated directories");
        System.out.println();
        System.exit(1);
    }
}
