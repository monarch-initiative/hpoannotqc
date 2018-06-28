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
is currently private available here: https://github.com/monarch-initiative/hpo-annotation-data.


### Download
The application will download several required files to a (new) subdirectory called ``data``.
```
$ java -jar target/HpoAnnotQc.jar download
```

### Creation of the 'big file'

The following command converts the
new files in v2files into a new phenotype_annotation2.tab file.
```aidl
$ java -jar target/HpoAnnotQc.jar big-file -s /path/hpo-annotation-data/rare-diseases/annotated
```
This command will output the ``phenotype.hpoa`` file as well as a log file
 named ``hpoannotQC.log.date``. The ``phenotype.hpoa`` file can be used as input for phenol.
