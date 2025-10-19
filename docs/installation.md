
# Installation

HpoAnnotQC requires at least Java 11.

To build HPO Annotation Q/C, clone the GitHub repository at
https://github.com/monarch-initiative/hpoannotqc, and build HPO Workbench using maven. 

``` shell
git clone https://github.com/monarch-initiative/hpoannotqc.git
cd hpoannotqc
mvn clean package
```

This will create an executable jar file.  

``` shell
java -jar target/HpoAnnotQc.jar
Usage: java -jar HpoAnnotQc.jar [-hV] [COMMAND]
Hpo Annotation Quality Control.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  download, D         download files
  big-file, B         Create phenotype.hpoa file
  bigfile, G          Generated phenotype.hpoa file
  qc, Q               Q/C phenotype.hpoa file
  supplemental-files  Create g2p, p2g, g2d files
```

