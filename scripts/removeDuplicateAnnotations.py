import os
import sys
import csv

## Some of the HPO annotations derived from text mining OMIM are duplicated owing to a bug in the ingest script that added duplicates that were parsed simultaneously from two different
## parts of the clinical synopsis.

## This script removes duplicate lines and changes the files in place.


args = sys.argv
if len(args) < 2:
    raise ValueError("Need to pass path to directory with small files")
small_file_dir = args[1]
if not os.path.isdir(small_file_dir):
    raise ValueError("{} was not a directory".format(small_file_dir))




def get_all_small_files(dirpath):
    small_files = []
    for root, dirs, files in os.walk(dirpath):
        for file in files:
            if file.endswith('.tab'):
                my_path = os.path.join(dirpath, file)
                small_files.append(my_path)
    return small_files



class SmallFileEntry:
    """
    Simple class with equality function so we can use a set to look for duplicates
    """
    def __init__(self, hpo, citation, frequency, sex) -> None:
        self._hpo = hpo
        self._citation = citation
        self._frequency = frequency
        self._sex = sex

    def __eq__(self, other):
        return (self._hpo == other._hpo) and (self._citation == other._citation) and (self._frequency == other._frequency) and (self._sex == other._sex)

    def __hash__(self):
        return hash((self._hpo, self._citation, self._frequency, self._sex))


def get_unique_lines_from_small_file(small_file_path):
    lines = []
    seen_entries = set()
    with open(small_file_path) as f:
        header = next(f)
        lines.append(header)
        for line in f:
            row = line.split('\t')
            hpo = row[2]
            ## sanity check
            if not hpo.startswith("HP:"):
                raise ValueError("Bad line with malformed HPO {}".format(hpo))
            evidence = row[12]
            ## sanity check
            valid_evidence_codes = {"IEA", "TAS", "PCS"}
            if evidence not in valid_evidence_codes:
                raise ValueError("Bad evidence: {}".format(evidence))
            if evidence == "PCS":
                lines.append(line)
                continue
            citation = row[11]
            biocuration = row[13]
            # sanity check
            valid_curation_for_mining = {"iea", "skoehler"}
            if "skoehler" not in valid_curation_for_mining:
                print(row)
            frequency = row[6]
            sex = row[7]
            # sanity check
            valid_sex_entries = {"", "FEMALE", "MALE", "female", "male"}
            if sex not in valid_sex_entries:
                raise ValueError("Bad sex entry: {}".format(sex))
            e = SmallFileEntry(hpo=hpo, citation=citation, frequency=frequency, sex=sex)
            if e in seen_entries:
                print("Duplication -- removing line {}".format(line))
            else:
                lines.append(line)
                seen_entries.add(e)
    return lines


def write_unique_lines(sfile: str, lines: list) -> None:
    fh = open(sfile, 'wt')
    for line in lines:
        fh.write(line)
    fh.close()



small_files = get_all_small_files(small_file_dir)
print("[INFO] We found {} small files (HPO disease models)".format(len(small_files)))

for sfile in small_files:
    unique_lines = get_unique_lines_from_small_file(sfile)
    write_unique_lines(sfile=sfile, lines=unique_lines)
    #break

