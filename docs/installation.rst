
Installing and running HPO Annotation Q/C
=========================================

To build HPO Annotation Q/C, clone the GitHub repository at
https://github.com/monarch-initiative/hpoannotqc, and build HPO Workbench using maven. ::


    $ git clone https://github.com/monarch-initiative/hpoannotqc.git
    $ cd hpoannotqc
    $ mvn clean package

This will create an executable jar file.  ::

    $ java -jar target/HpoAnnotQc.jar


