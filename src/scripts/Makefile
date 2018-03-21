#
# Makefile to generate a release of the phenote.hpoa file
# based on Seb's makefile for releases
#

NOW := $(shell date +"%Y-%m-%d %H:%M")

## The Jar file is generated from HpoAnnotQc
JARFILE=HpoAnnotQc.jar

# the programs being called
GENERATE_ANNOTATIONFILE=$ java -Xmx1G -jar ../../target/HpoAnnotQc.jar big-file



##### 
# note that the ordering is important here!! 
# first update the annotation file! than the rest!!!
.PHONY: all
all: load-external generate-annotation-file

# the folders where external data is stored locally
DATA_FOLDER?=data

# load external data
.PHONY: load-external
load-external:
	mkdir -p $(DATA_FOLDER)
	# load orphanet data
	wget -N -P $(DATA_FOLDER) http://www.orphadata.org/data/xml/en_product4_HPO.xml
	# loads the HPO
	wget -N -P $(DATA_FOLDER) http://purl.obolibrary.org/obo/hp.obo


# takes the manual-annotations (stored at berkeley) and the orpha-data files and generates a big integrated file of HPO-annotated diseases

## Path to /rare-diseases-annotated (ADJUST)
SMALL_FILE_DIR=../../../../GIT/hpo-annotation-data/rare-diseases/annotated/
## Path to outfile
OUTFILE_PATH=./phenotype.hpoa



.PHONY: generate-annotation-file
generate-annotation-file:
	$(GENERATE_ANNOTATIONFILE) --orphadata "$(DATA_FOLDER)/en_product4_HPO.xml"  --hpo "$(DATA_FOLDER)/hp.obo" --small-files ${SMALL_FILE_DIR} --out ${OUTFILE_PATH}
	cat ${OUTFILE_PATH} | awk -F'\t' '{print $$14}'  | sort | uniq -c | sort -nr > curator-statistics.tab
	echo "$(NOW)" > data_version.txt

.PHONY: clean
clean:
	rm -Rf $(EXTERNAL_ORPHANET)
	rm -Rf $(BERKELEY_ANNOT)
	rm -Rf $(BERKELEY_ANNOT_REFORMAT)
