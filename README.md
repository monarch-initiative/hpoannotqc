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
We need to pass the application the location of the hp.obo file as well
as the location of the directory with the HPO annotation small files. This repository
is currently private available here: https://github.com/monarch-initiative/hpo-annotation-data

Run this application therefore as
```aidl
$ mvn clean install
$ java -jar target/HpoAnnotQc.jar -h /path/to/hp.obo -a /path/hpo-annotation-data/rare-diseases/annotated

```

