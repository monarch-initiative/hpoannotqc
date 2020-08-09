
Installing and running HPO Annotation Q/C
=========================================

This version of HpoAnnotQc uses phenol-1.3.2-SNAPSHOT, which needs to be installed locally with ``mvn install``.
Following this, to build HPO Annotation Q/C, clone the GitHub repository at
https://github.com/monarch-initiative/hpoannotqc, and build HPO Workbench using maven. ::


    $ git clone https://github.com/monarch-initiative/hpoannotqc.git
    $ cd hpoannotqc
    $ mvn clean package

This will create an executable jar file.  ::

    $ java -jar target/HpoAnnotQc.jar
        Usage: java -jar HpoAnnotQc.jar [-hV] [COMMAND]
        Variant-motif visualization tool.
        -h, --help      Show this help message and exit.
        -V, --version   Print version information and exit.
       Commands:
        download, D  download files
        big-file, B  Create phenotype.hpoa file
        gene2phen    Create genes to phenotypes file



We will update this as soon as phenol-1.3.2 is released in maven central.