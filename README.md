# hpoannotqc
HPO Annotation QC



## Building the application
hpoannotqc depends on phenol; currently, the latest version of phenol needs to be installed locally, but soon this will all be in maven central.

## Running the application
We need to pass the application the location of the hp.obo file as well
as the location of the directory with the HPO annotation small files. This repository
is currently private available here: https://github.com/monarch-initiative/hpo-annotation-data.


### Download
The application will download several required files to a (new) subdirectory called ``data``.
```
$ java -jar target/HpoAnnotQc.jar download
```

### Creation of phenotype.hpoa

The following command converts the HPO Annotation files ("small files) into the phenotype_annotation.tab file.
```aidl
$ java -jar target/HpoAnnotQc.jar big-file -a /path/hpo-annotation-data/rare-diseases/annotated
```
This command will output the ``phenotype.hpoa`` file as well as a log file
 named ``hpoannotQC.log.date``. The ``phenotype.hpoa`` file can be used as input for phenol.
