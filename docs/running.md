# Running

## Create Bigfile

Use the following command to create the ``phenotype.hpoa``file.


```bash
java -jar HpoAnnotQc.jar bigfile -a ../../GIT/hpo-annotation-data/rare-diseases/annotated/
```

If this is successful, you will see some logging lines that end with something like this:
```bash
14:29:11.462 [main] INFO org.monarchinitiative.hpoannotqc.cmd.BigFileGenerateCommand -- Total output lines was 273679
```

Note that we should update the term labels in the small files prior to running this command. If we forget to do this, we may see error messages like this:

```bash
OMIM-618912.tab - Sorbitol dehydrogenase deficiency with peripheral neuropathy (OMIM:618912): OBSOLETE_TERM_LABEL: Term label "Proximal lower limb muscle weakness" does not match primary label "Proximal muscle weakness in lower limbs".
org.monarchinitiative.phenol.base.PhenolRuntimeException: Found errors in HPO project small file ingest
````

Use the PhenoteFX app to update labels! (Edit menu: "Update all outdated labels")


## Create accessory files
This command intends to create the g2p, p2g, g2d files that are available at the HPO website.
By default it looks for the downloaded files (e.g., hp.json) in the current folder. You may need to specify the data folder as follows.

```bash
 java -jar target/HpoAnnotQc.jar supplemental-files -d data
 ```