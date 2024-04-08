# hpoannotqc
HPO Annotation QC

This application was developed to do Q/C of the HPO annotation (HPOA) files, the so-called
small files. It converts the small files to the "big file" (phenotype.hpoa).

## Running the application
We need to pass the application the location of the hp.obo file as well
as the location of the directory with the HPO annotation small files. This repository
is currently available here in a private repository: https://github.com/monarch-initiative/hpo-annotation-data.


### Download
The application will download several required files to a (new) subdirectory called ``data``.
```
$ java -jar target/HpoAnnotQc.jar download [--overwrite]
```

### Creation of phenotype.hpoa

The following command converts the HPO Annotation files ("small files) into the phenotype_annotation.tab file.
```aidl
$ java -jar target/HpoAnnotQc.jar big-file -a /path/hpo-annotation-data/rare-diseases/annotated
```
This command will output the ``phenotype.hpoa`` file as well as a log file
 named ``hpoannotQC.log.date``. The ``phenotype.hpoa`` file can be used as input for phenol.
