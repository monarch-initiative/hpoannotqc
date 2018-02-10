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

The following two steps convert the old file formats to new V2 small file formats and put
the new files into a directory called ``v2files``; the second step converts the
new files in v2files into a new phenotype_annotation2.tab file.
```aidl
$ java -jar target/HpoAnnotQc.jar convert -h /path/to/hp.obo -a /path/hpo-annotation-data/rare-diseases/annotated
$ java -jar target/HpoAnnotQc.jar big-file
```
The runs will fill log files named ``hpoannotQC.log.date``. The files show several things
including lines that were changed in the course of the Q/C procedures.
