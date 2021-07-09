from collections import defaultdict
from datetime import date, time, datetime
import sys, os, re

id2name = defaultdict(set)
entryset = set()



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



class Name:
    def __init__(self, label, biocuration) -> None:
        self._label = label
        i = biocuration.rindex('[')
        if i < 0:
            raise ValueError("Could not find [")
        j = biocuration.index(']', i)
        if j < i:
            raise ValueError("Could not find ]")
        datestr = biocuration[i+1:j]
        self._date = datetime.fromisoformat(datestr)

    def __eq__(self, other):
            return (self._label == other._label) and (self._date == other._date)

    def __hash__(self):
        return hash((self._label, self._date))

    def __repr__(self) -> str:
        return "{} ({})".format(self._label, str(self._date))

        




def get_all_lines_from_small_file(small_file_path):
    lines = []
    names = set()
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
            biocuration = row[13]
            name = Name(label=row[1], biocuration=biocuration)
            names.add(name)
            lines.append(line)
    return lines, names


def write_unique_lines(sfile: str, lines: list) -> None:
    fh = open(sfile, 'wt')
    for line in lines:
        fh.write(line)
    fh.close()


def get_label_at_latest_date(dateset:set):
    lst = list(dateset)
    lst = sorted(lst, key=lambda k: k._date, reverse=True)
    L = len(lst)
    return lst[0]._label

def checkIfRomanNumeral(numeral):
    """Controls that the userinput only contains valid roman numerals"""
    numeral = numeral.upper()
    validRomanNumerals = ["M", "D", "C", "L", "X", "V", "I", "(", ")"]
    for letters in numeral:
        if letters not in validRomanNumerals:
            return False
    return True


def split_and_de_shout(name):
    name = name.split(';')[0] # take the part before the first semicolon
    # Some names start with #600543 etc. Remove this
    z = re.match('^\#\d{6}', name)
    if z:
        if not name.startswith('#'):
            raise ValueError("Sanity check 1")
        if name[7] != ' ':
            # expect a whitespace after the OMIM id
            raise ValueError("Sanity check 2")
        name = name[8:]
    fields = name.split()
    LcFields = []
    LcFields.append(fields[0].capitalize())
    if len(fields)>1:
        for i in range(1,len(fields)-1):
            LcFields.append(fields[i].lower().strip())
        lastfield = fields[len(fields)-1].strip()
        if checkIfRomanNumeral(lastfield):
            LcFields.append(lastfield)
        else:
            LcFields.append(lastfield.lower().strip())
    return " ".join(LcFields).strip()
    
    

small_files = get_all_small_files(small_file_dir)
print("[INFO] We found {} small files (HPO disease models)".format(len(small_files)))

for sfile in small_files:
    lines, names = get_all_lines_from_small_file(sfile)
    if len(names)>1:
        print("'".join([str(s) for s in names]))
        latest_label = get_label_at_latest_date(names)
        print("LTEST = " + latest_label)
        latest_label = split_and_de_shout(latest_label)
        print(latest_label)
        print()
    #write_unique_lines(sfile=sfile, lines=unique_lines)
    #break


