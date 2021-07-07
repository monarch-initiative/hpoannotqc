from collections import defaultdict


id2name = defaultdict(set)
entryset = set()

class Entry:
    def __init__(self, omim, hpo, pmid, sex) -> None:
        self._omim = omim
        self._hpo = hpo
        self._pmid = pmid
        self._sex = sex

    def __eq__(self, other):
        if isinstance(other, Entry):
            return ((self._omim == other._omim) and (self._hpo == other._hpo) and (self._pmid == other._pmid) and (self._sex == other._sex))
        else:
            return False
   
    def __hash__(self):
        return hash((self._omim, self._hpo, self._pmid, self._sex))


def shownames():
    ## Check for entries that have been assigned more than one name
    i = 0
    for k, v in id2name.items():
        if len(v) > 1:
            print ("{}) {} has multiple names ({})".format(i, k, "--".join(v)))
            i += 1

j = 0
with open('phenotype.hpoa') as f:
    for line in f:
        if not line.startswith('OMIM'):
            continue
        fields = line.rstrip().split('\t')
        omim_id = fields[0]
        label = fields[1]
        id2name[omim_id].add(label) 
        hpo = fields[3]
        pmid = fields[4]
        sex = fields[8]
        e = Entry(omim=omim_id, hpo=hpo, pmid=pmid, sex=sex)
        if e in entryset:
            j += 1
            print("{}) Duplicate -- {} {}".format(j, e._omim, e._hpo))
        else:
            entryset.add(e)



## shownames()

## check for entries that are duplicated
## Note hpoa must be run with the --merge false for this to make sense



