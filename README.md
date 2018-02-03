# hpoannotqc
HPO Annotation QC



## Building the application
hpoannotqc depends on the forked version of opntolib called OLPG available at
https://github.com/monarch-initiative/OLPG
Install this locally on the machine you are building hpoannotqc on.
```aidl
$ mvn install
```

## Running the application
By default SpringApplication will convert any command line option arguments 
(starting with ‘–‘, e.g. –server.port=9090) to a property and add it to the 
Spring Environment. Command line properties always take precedence over other 
property sources. 
We need to pass the application the location of the hp.obo file as well
as the location of the directory with the HPO annotation small files. This repository
is currently private available here: https://github.com/monarch-initiative/hpo-annotation-data

Run this application therefore as
```aidl
$ mvn clean install
$ java -jar target/HpoAnnotQc.jar -hpo=/path/to/hp.obo -annotations=/path/hpo-annotation-data/rare-diseases/annotated

```

