package org.monarchinitiative.hpoannotqc.smallfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.phenol.formats.hpo.HpoFrequency;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoTermRelation;
import org.monarchinitiative.phenol.graph.data.Edge;
import org.monarchinitiative.phenol.ontology.data.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.monarchinitiative.hpoannotqc.smallfile.DateUtil.convertToCanonicalDateFormat;
import static org.monarchinitiative.hpoannotqc.smallfile.DiseaseDatabase.DECIPHER;
import static org.monarchinitiative.hpoannotqc.smallfile.DiseaseDatabase.OMIM;
import static org.monarchinitiative.hpoannotqc.smallfile.DiseaseDatabase.ORPHANET;
import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.*;


/**
 * Created by peter on 1/20/2018.
 * This class is intended to take data from a single line of an "old" small file entry. Its main purpose os to map and
 * transform the data to the new field formats so that we can transform it into a {@link V2SmallFileEntry} object.
 * The transformations performed are
 * <ol>
 *     <li>Annotations to alt_id's are replaced wuth the current primary ud </li>
 *     <li>The data format is unifed to YYYY-MM-DD</li>
 *     <li>The fields geneId, geneName, genotype, and genesymbol are removed</li>
 *     <li>A new modifier field is added. If possible, the free text in the Description field is used to put something
 *     into the modifier field</li>
 *     <li>The fields entityName,qualityId ,qualityName , addlEntityName, and addlEntityId are removed</li>
 *     <li>The fields abnormalId and abnomralname are removed.</li>
 *     <li>The field orthologs is removed</li>
 *     <li>The current old file format has a single field for frequency. We try to make three separate fields from this
 *     TODO discuss with Seb</li>
 *     <li>The fields evidenceID, evidenceName, and evidence are reduced to one field "evidence" that is allow to have one
 *     of four codes, IEA, ICE, TAS, PCS only.</li>
 * </ol>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class OldSmallFileEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private DiseaseDatabase database = null;
    private String diseaseID = null;
    private String diseaseName = null;
    /** gene ID. We will delete this field for the new version*/
    private String geneID = null;
    /** Gene symbol. We will delete this field for the new version */
    private String geneName = null;
    /** We will delete the genotype field in the new version*/
    private String genotype = null;
    /** We will delete the gene symbol in the new version*/
    private String genesymbol = null;
    /** THe HPO id */
    private TermId phenotypeId = null;
    /** THe HPO label */
    private String phenotypeName = null;
    /** THis should be an HPO Id */
    private TermId ageOfOnsetId = null;
    /** Corresponding Name of HPO age of onset term. */
    private String ageOfOnsetName = null;

    private String evidenceID = null;

    private String evidenceName = null;

    private String frequencyString = null;

    private TermId frequencyId = null;
    // Frequency Ids
    private static final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    private static final TermId FrequencyRoot = new ImmutableTermId(HP_PREFIX, "0040279");
    private static final TermId FREQUENT = HpoFrequency.FREQUENT.toTermId();
    private static final TermId VERY_FREQUENT = HpoFrequency.VERY_FREQUENT.toTermId();
    private static final TermId OBLIGATE = new ImmutableTermId(HP_PREFIX, "0040280");
    private static final TermId OCCASIONAL = HpoFrequency.OCCASIONAL.toTermId();
    private static final TermId EXCLUDED = HpoFrequency.EXCLUDED.toTermId();
    private static final TermId VERY_RARE = HpoFrequency.VERY_RARE.toTermId();

    /// Modifier term that is used for Recurrent
    private static final TermId EPISODIC = new ImmutableTermId(HP_PREFIX,"0025303");
    /** Assign IEA if we cannot find an evidence code in the original data */
    private static final String DEFAULT_EVIDENCE_CODE="IEA";
    /** Assign this assignedBy string if we do not have more information. */
    private static final String DEFAULT_HPO_ASSIGNED_BY="HPO:iea";
    /** Return the empty string instead of NULL if we have nothing. Otherwise, we may wind up writing the string "null". */
    private static final String EMPTY_STRING="";


    /**
     * If present, a limitation to MALE or FEMALE.
     */
    private String sexID = null;
    /** Redundant with {@link #sexID}. */
    private String sexName = null;
    /** A previous verion of HPOA did not have the {@link #sexID} field but this shoudl also be MALE or FEMALE. */
    private String sex = null;

    private final static String MALE_CODE = "Male";
    private final static String FEMALE_CODE = "Female";
    /** If present, "NOT" */
    private String negationID = null;
    /** Redundant with {@link #negationID}. */
    private String negationName = null;
    /** Free text, b ut may contain things we can turn into modifiers. */
    private String description = null;
    /** This was not present in the old small file but will be created here if possible from the Description field. */
    private Set<TermId> modifierset = new HashSet<>();
    /** The source of the assertion, often a string such as PMID:123 or OMIM:100123 */
    private String pub = null;
    /** The biocurator */
    private String assignedBy = null;
    /* The date the annotation was first created. */
    private String dateCreated = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String entityId = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String entityName = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String qualityId = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String qualityName = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String addlEntityName = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String addlEntityId = null;
    /**
     * Some entries have just evidence rather than evidenceId and evidenceName. We do the best we can to get one evidence code but
     * looking at all three fields one after the other, with evidenceId being prefered, then evidenceName, then evidence
     */
    private String evidence = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used.*/
     private String abnormalId = null;
    /** Added here for completeness. But we will be discarding this field in the v2 because it was hardly ever used. */
    private String abnormalName = null;
    private String othologs = null;

    private static HpoOntology ontology = null;
    private static Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology = null;
    private static Ontology<HpoTerm, HpoTermRelation> frequencySubontology = null;
    private static Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology = null;
    /** key -- all lower-case label of a modifer term. Value: corresponding TermId .*/
    private static Map<String, TermId> modifier2TermId = new HashMap<>();

    private Set<SmallFileQCCode> QCissues;

    public OldSmallFileEntry() {
        QCissues = new HashSet<>();
    }
    /** This is called once by client code before we start parsing. Not pretty design but it works fine for this one-off app. */
    public static void setOntology(HpoOntology ont) {
        ontology = ont;
        TermId inheritId = new ImmutableTermId(HP_PREFIX,"0000005");
        TermId frequencyId = new ImmutableTermId(HP_PREFIX,"0040279");
        inheritanceSubontology = ontology.subOntology(inheritId);
        frequencySubontology = ontology.subOntology(frequencyId);
        abnormalPhenoSubOntology = ontology.getPhenotypicAbnormalitySubOntology();
        findModifierTerms();
    }

    /**
     * Creates a map for all terms in the Clinical modifier subhierarchy (which
     * starts from HP:0012823). The keys are lower-case versions of the Labels,
     * and the values are the corresponding TermIds. See {@link #modifier2TermId}.
     */
    private static void findModifierTerms() {
        TermId modifier = new ImmutableTermId(HP_PREFIX, "0012823");
        Stack<TermId> stack = new Stack<>();
        Set<TermId> descendents = new HashSet<>();
        stack.push(modifier);
        while (!stack.empty()) {
            TermId parent = stack.pop();
            descendents.add(parent);
            Set<TermId> kids = getChildren(parent);
            kids.forEach(k ->   stack.push(k));
        }
        for (TermId tid : descendents) {
            String label = ontology.getTermMap().get(tid).getName().toLowerCase();
            modifier2TermId.put(label, tid);
        }
    }

    private static Set<TermId> getChildren(TermId parent) {
        Set<TermId> kids = new HashSet<>();
        Iterator it = ontology.getGraph().inEdgeIterator(parent);
        while (it.hasNext()) {
            Edge<TermId> sourceEdge = (Edge<TermId>) it.next();
            TermId source = sourceEdge.getSource();
            kids.add(source);
        }
        return kids;
    }


    public void addDiseaseId(String id) {
        if (id.startsWith("OMIM")) {
            this.database = OMIM;
            this.diseaseID = id;
        } else if (id.startsWith("ORPHA")) {
            this.database = ORPHANET;
            this.diseaseID = id;
        } else if (id.startsWith("DECIPHER")) {
            database = DECIPHER;
            this.diseaseID = id;
        } else {
            LOGGER.error("Did not recognize disease database for " + id);
            System.exit(1);
        }
    }

    public void addDiseaseName(String n) {
        this.diseaseName = n;
        if (diseaseName.length() < 1) {
            LOGGER.trace("Error zero length name ");
            System.exit(1);
        }
    }

    /**
     * Note that a few entries had an OMIM id in the gene_id column; this was the gene entry that went along
     * with the phenotype entry. For instance,
     * <pre>
     *     OMIM:615134	MELANOMA, CUTANEOUS MALIGNANT, SUSCEPTIBILITY TO, 9; CMM9	OMIM:187270	TELOMERASE REVERSE TRANSCRIPTASE; TERT
     * </pre>
     * This is not an error in the parsing (I checked this by hand), but was a biocuration practice that we simply
     * abandoned years ago because it was not useful. We are discarding the gene id/name/symbol information anyway.
     * Therefore, we write this to the log for completeness sake, but do not need to worry.
     * @param id
     */
    public void addGeneId(String id) {
        if (id == null || id.isEmpty()) return;
        LOGGER.trace("Adding gene id: " + id);
        if (id.startsWith("OMIM")) { LOGGER.error("We found an OMIM Id in the gene column: " + id);}
        this.QCissues.add(GOT_GENE_DATA);
        geneID = id;
    }
    /** Record that we are adding gene data because we will be discarding it. */
    public void setGeneName(String name){
        if (name==null || name.isEmpty()) return;
        if (name.startsWith("OMIM")) {LOGGER.error("We found an OMIM name in the gene column: " + name); }
        this.QCissues.add(GOT_GENE_DATA);
        geneName = name;
    }

    public void setGenotype(String gt) {
        if (gt==null || gt.isEmpty()) return;
        this.QCissues.add(GOT_GENE_DATA);
        genotype = gt;
    }

    public void setGenesymbol(String gs) {
        if (gs==null || gs.isEmpty()) return;
        if (gs.startsWith("OMIM")) { LOGGER.error("We found an OMIM name in the gene symbol: " + gs);}
        this.QCissues.add(GOT_GENE_DATA);
        genesymbol = gs;
    }
    /** Cherck the validating of the String id and crfeate the corresponding TermIKd in {@link #phenotypeId}. */
    public void setPhenotypeId(String id) throws HPOException {
        this.phenotypeId = createHpoTermIdFromString(id);
        TermId primaryId = ontology.getTermMap().get(phenotypeId).getId();
        if (! phenotypeId.equals(primaryId)) {
            phenotypeId=primaryId; // replace alt_id with current primary id.
            this.QCissues.add(UPDATING_ALT_ID);
        }
    }
    /** Sets the name of the HPO term. */
    public void setPhenotypeName(String name) {
        phenotypeName = name;
    }
    /** Sets the age of onset id (HPO term) and checks it is a valid term. */
    public void setAgeOfOnsetId(String id) throws HPOException {
        if (id == null || id.length() == 0) {
            return;
        }// no age of onset
        if (!id.startsWith("HP:")) {
            LOGGER.error("Bad phenotype id prefix: " + id);
            System.exit(1);
        }
        if (!(id.length() == 10)) {
            LOGGER.error("Bad length for id:  " + id);
            System.exit(1);
        }
        if (!isValidInheritanceTerm(id)) {
            LOGGER.error("Not a valid inheritance term....terminating program");
            System.exit(1);
        }
        ageOfOnsetId = createHpoTermIdFromString(id);
    }

    public void setAgeOfOnsetName(String name) {
        if (name == null || name.length() == 0) return; // no age of onset (not required)
        this.ageOfOnsetName = name;
    }

    private boolean isValidInheritanceTerm(String id) throws HPOException {
        TermId tid = createHpoTermIdFromString(id);
        return true;
    }

    /** @return TermId e if this is a well-formed HPO term (starts with HP: and has ten characters).
     * and is listed in the onrtology */
    private TermId createHpoTermIdFromString(String id) throws HPOException  {
        if (!id.startsWith("HP:")) {
            throw new HPOException("Invalid HPO prefix for term id \"" + id + "\"");
        }
        id = id.substring(3);
        TermId tid = new ImmutableTermId(HP_PREFIX, id);
        if (tid == null) {
            throw new HPOException("Could not create TermId object from id: \""+ id+"\"");
        }
        if (ontology == null) {
            throw new HPOException("Ontology is null and thus we could not create TermId for " + id);
        }
        if (!ontology.getTermMap().containsKey(tid)) {
            throw new HPOException("Term " + tid.getIdWithPrefix() + " was not found in the HPO ontology");
        }
        return tid;
    }

    private boolean evidenceCodeWellFormed(String evi) {
        if (evi==null || evi.isEmpty()) return false;
        if ((!evi.equals("IEA")) && (!evi.equals("PCS")) &&
                (!evi.equals("TAS") && (!evi.equals("ICE")))) {
            return false;
        } else {
            return true;
        }
    }

    public void setEvidenceId(String id) throws HPOException {
        this.evidenceID = id;

    }

    public void setEvidenceName(String name) throws HPOException {
        this.evidenceName = name;
        evidenceCodeWellFormed(evidenceName);
    }

    /** This returns true if our Q/C procedure has identified any issues or things we need to change except for
     * updating the date format, which is done silently.
     * @return true if there is at least one reportable Q/C issue
     */
    public boolean hasQCissues() {
        if (QCissues.size()==0) return false;
        else if (QCissues.size()==1 && QCissues.contains(UPDATED_DATE_FORMAT)) return false;
        else return true;
    }

    /**
     * This method is called after all of the data have been entered. We return a List of error codes so that
     * we can list up what we had to do to convert the filesd and do targeted manual checking.
     */
    public Set<SmallFileQCCode> doQCcheck() throws HPOException{
        // check the evidence codes. At least one of the three fields
        // has to have one of the correct codes, in order for the V2small file  entry to be ok
        boolean evidenceOK=false;
        if (evidenceID!=null) {
            if (evidenceCodeWellFormed(evidenceID) ) { evidenceOK=true; }
        }
        if (!evidenceOK && evidenceName!=null) {
            if (evidenceCodeWellFormed(evidenceName)) {evidenceOK=true; }
        }
        if (!evidenceOK && evidence!=null) {
            if (evidenceCodeWellFormed(evidence)) {evidenceOK=true; }
        }
        if (!evidenceOK) {
            QCissues.add(DID_NOT_FIND_EVIDENCE_CODE);
            this.evidenceID=DEFAULT_EVIDENCE_CODE;
        }
        if (assignedBy==null || assignedBy.isEmpty()) {
            QCissues.add(ASSIGNED_BY_EMPTY);
            this.assignedBy=DEFAULT_HPO_ASSIGNED_BY;
        }
        // check whether the primary label needs to be updated.
        if (! this.phenotypeName.equals(ontology.getTermMap().get(this.phenotypeId).getName())) {
            this.QCissues.add(UPDATING_HPO_LABEL);
            this.phenotypeName=ontology.getTermMap().get(this.phenotypeId).getName();
        }
        if (this.pub==null && this.diseaseID != null )  {
            this.pub = diseaseID;
            this.QCissues.add(REPLACED_EMPTY_PUBLICATION_STRING);
        } else if (pub.equals("OMIM")&& this.diseaseID != null && diseaseID.startsWith("OMIM") )  {
            this.pub = diseaseID;
            this.QCissues.add(CORRECTED_PUBLICATION_WITH_DATABASE_BUT_NO_ID);
        }
        // we need to call some additional functions that the V2SmallFileEntry constructor will
        // call in order to get a complete tally of the Q/C issues.
        String dummy=getThreeWayFrequencyString();
        dummy=getDateCreated();
        dummy=getSex();
        dummy=getModifierString();


        return QCissues;
    }



/*
if (frequencyMod.equalsIgnoreCase("typical") || frequencyMod.equalsIgnoreCase("common") || frequencyMod.equalsIgnoreCase("variable")
					|| frequencyMod.equalsIgnoreCase("frequent") || frequencyMod.equalsIgnoreCase("FREQUENT(79-30%)")) {
				frequencyMod = OntologyConstants.frequency_Frequent;
 */

    public void setFrequencyString(String freq) {
        if (freq == null || freq.length() == 0) return; // not required!
        this.frequencyString = freq.trim();
        if (frequencyString.length() == 0) return; //it ewas just a whitespace
        if (frequencyString.startsWith("HP:")) {
            LOGGER.error("NEVER HAPPENS, FREQUENCY WITH TERM");
            System.exit(1);
        } else if (Character.isDigit(frequencyString.charAt(0))) {
            LOGGER.error("Adding numeric frequency data: \"" + freq + "\"");
            this.frequencyString=freq.trim();
        } else if (frequencyString.equalsIgnoreCase("very rare")) {
            this.frequencyId = VERY_RARE;
        } else if (frequencyString.equalsIgnoreCase("rare")) {
            this.frequencyId = OCCASIONAL;
        } else if (frequencyString.equalsIgnoreCase("frequent")  ) {
            this.frequencyId = FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("occasional")) {
            this.frequencyId = OCCASIONAL;
        } else if (frequencyString.equalsIgnoreCase("variable")) {
            this.frequencyId = FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("typical")) {
            this.frequencyId = FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("very frequent")) {
            this.frequencyId = VERY_FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("common")) {
            this.frequencyId = FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("hallmark")) {
            this.frequencyId = VERY_FREQUENT;
        } else if (frequencyString.equalsIgnoreCase("obligate")) {
            this.frequencyId = OBLIGATE;
        } else {
            LOGGER.error("BAD FREQ ID \"" + freq + "\"");
            System.exit(1); // should never happen, but we want to know about it right away --
            // therefore dying is the only option.
        }
    }


    public void setSexID(String id) throws HPOException {
        if (id == null || id.length() == 0) return;//oik, not required
        if (id.equalsIgnoreCase("MALE"))
            sexID = MALE_CODE;
        else if (id.equals("M"))
            sexID = MALE_CODE;
        else if (id.equalsIgnoreCase("FEMALE"))
            sexID = FEMALE_CODE;
        else if (id.equals("F"))
            sexID = FEMALE_CODE;
        else
            throw new HPOException("Did not recognize Sex ID: \"" + id + "\"");
    }

    public void setSexName(String name) throws HPOException {
        if (name == null || name.length() == 0) return;//oik, not required
        if (name.equalsIgnoreCase("MALE"))
            sexID = MALE_CODE;
        else if (name.equalsIgnoreCase("FEMALE"))
            sexID = FEMALE_CODE;
        else
            throw new HPOException("Did not recognize Sex Name: " + name);
    }

    public void setNegationID(String id) throws HPOException {
        if (id == null || id.length() == 0) return;
        if (id.equalsIgnoreCase("NOT")) {
            negationID = "NOT";
        } else throw new HPOException("Malformed negation ID: \"" + id + "\"");
    }

    public void setNegationName(String name) throws HPOException {
        if (name == null || name.length() == 0) return;
        if (name.equalsIgnoreCase("NOT")) {
            negationID = "NOT";
        } else throw new HPOException("Malformed negation Name: \"" + name + "\"");
    }
    /** This is an obsolete field type that will be discarded in the V2 version. Parse it here for completeness sake. */
    public void setOrthologs(String orth) {
        if (orth==null || orth.isEmpty()) return;
        this.othologs=orth;
    }


    //(In 1/4 PATIENTS)
    //1: OMIM-CS:RADIOLOGY > GENERALIZED OSTEOSCLEROSIS
    /**
     * In some case, the Description field will contain a modifier such as {@code Mild}. In  other cases,
     * Seb's pipeline will have added something like {@code MODIFIER:episodic}.
     * Put everything we cannot match like this back into the free text description field ({@link #description}).
     * @param d The description from the original "old" small file.
     */
    public void setDescription(String d) {
        List<String> descriptionList = new ArrayList<>();

        // capture items suich as (In 1/4 PATIENTS)
        final String REGEX = "IN (\\d+)/(\\d+) PATIENTS";
        Pattern pattern = Pattern.compile(REGEX);
         // multiple items. Probably from Sebastian's text mining pipeline
            String A[] = d.split(";");
            for (String a : A) {
                Matcher matcher = pattern.matcher(a);
                if (a.contains("OMIM-CS")) {
                    this.evidenceID="TAS";
                }
                if (a.startsWith("MODIFIER:")) {
                    String candidateModifier = a.substring(9).toLowerCase();
                    if (candidateModifier.contains("recurrent")) {
                        LOGGER.trace("Adding Modifier term \"Episodic\" for recurrent");
                        modifierset.add(EPISODIC);
                        this.QCissues.add(CREATED_MODIFER);
                    } else if (modifier2TermId.containsKey(candidateModifier)) {
                        modifierset.add(modifier2TermId.get(candidateModifier));
                    } else {
                        LOGGER.error("Could not identify modifer for " + candidateModifier + ", in description "+d+", terminating program....");
                        System.exit(1); // if this happens we need to add the item to HPO or otherwise check the code!
                    }
                } else if (a.contains("(RARE)")) {
                    if (this.frequencyId==null && this.frequencyString==null) {
                        this.frequencyId=VERY_RARE;
                        this.frequencyString="Very rare";
                    }
                    descriptionList.add(a);
                } else if (a.contains("(IN SOME PATIENTS)")) {
                    if (this.frequencyId==null && this.frequencyString==null) {
                        this.frequencyId=OCCASIONAL;
                        this.frequencyString="Occasional";
                    }
                    descriptionList.add(a);
                } else if (modifier2TermId.containsKey(a.toLowerCase())) {  // exact match (except for capitalization).
                    TermId tid = modifier2TermId.get(a.toLowerCase());
                    this.QCissues.add(CREATED_MODIFER);
                    modifierset.add(tid);
                } else if (matcher.find()){
                  String m=matcher.group(1);
                  String n=matcher.group(2);
                 // System.err.println(a + " m="+m+" n="+n);
                  this.frequencyString=String.format("%s/%s",m,n );
                  QCissues.add(CONVERTED_N_OF_M);
                } else {
                    descriptionList.add(a);
                }
            }

        description = descriptionList.stream().collect(Collectors.joining(";"));
    }

    /** Set the publication id. Note that we enforce that the prefix is written in all upper case, i.e.,
     * pmid:123 is changed to PMID:123. We also enforce that the string is not null and that there is
     * a prefix:id structure. This code does not try to correct anything except a lower case prefix such
     * as pmid. We also change "MIM" to "OMIM"
     * @param p
     */
    public void setPub(String p) throws HPOException {
        if (p==null || p.isEmpty()) {
            return; // we will try to fix this in the doQCcheck function/
        }
        p=p.trim(); // remove extraneous white space
        int index=p.indexOf(":");
        if (index <= 0) {
            if (p.startsWith("OMIM")) {
                // somebody forgot the ":"
                p=p.replaceAll("OMIM","OMIM:");
                QCissues.add(ADDED_FORGOTTEN_COLON);
            } else if (p.startsWith("MIM")) {
                p=p.replaceAll("MIM","OMIM:");
                QCissues.add(ADDED_FORGOTTEN_COLON);
            }
            pub = p;
            return;
        }
        if (p.startsWith("http")) { // accept URLs.
            pub=p;
            return;
        }
        String prefix = p.substring(0,index);
        String ucPrefix = prefix.toUpperCase();
        if (!prefix.equals(ucPrefix)) {
            this.QCissues.add(PUBLICATION_PREFIX_IN_LOWER_CASE);
            prefix = ucPrefix;
        }
        if (prefix.equals("MIM")) {
            this.QCissues.add(CHANGED_MIM_TO_OMIM);
            prefix = "OMIM";
        } else if (prefix.equals("PUBMED")) {
            this.QCissues.add(CHANGED_PUBMED_TO_PMID);
            prefix = "PMID";
        }
        this.pub = prefix + p.substring(index);
    }

    public void setAssignedBy(String ab) {
        if (ab==null || ab.isEmpty()) {
            this.assignedBy=DEFAULT_HPO_ASSIGNED_BY;
            QCissues.add(ASSIGNED_BY_EMPTY);
        }
        if (ab.equals("HPO")) {
            this.assignedBy=DEFAULT_HPO_ASSIGNED_BY;
            QCissues.add(ASSIGNED_BY_ONLY_HPO);
        } else {
            this.assignedBy = ab;
        }
    }

    /** Set the date and convert its format to yyy-mm-dd. */
    public void setDateCreated(String dc) {
        this.dateCreated = convertToCanonicalDateFormat(dc);
        if (!dc.equals(this.dateCreated)) {
            QCissues.add(UPDATED_DATE_FORMAT);
        }
    }

    public void setAddlEntityName(String n) {
        addlEntityName = n;
    }

    public void setAddlEntityId(String id) {
        addlEntityId = id;
    }

    public void setEntityId(String id) {
        entityId = id;
    }

    public void setEntityName(String name) {
        if (name ==null || name.isEmpty()) return;
        QCissues.add(GOT_EQ_ITEM);
        entityName = name;
    }

    public void setQualityId(String id) {
        if (id ==null || id.isEmpty()) return;
        QCissues.add(GOT_EQ_ITEM);
        qualityId = id;
    }

    public void setQualityName(String name) {
        if (name ==null || name.isEmpty()) return;
        QCissues.add(GOT_EQ_ITEM);
        qualityName = name;
    }

    public void setEvidence(String e) {
        evidence = e;
    }

    public void setAbnormalId(String id) {
        if (id ==null || id.isEmpty()) return;
        QCissues.add(GOT_EQ_ITEM);
        abnormalId = id;
    }

    public void setAbnormalName(String name) {
        if (name ==null || name.isEmpty()) return;
        QCissues.add(GOT_EQ_ITEM);
        abnormalName = name;
    }

    public void setSex(String s) throws HPOException {
        if (s == null || s.isEmpty()) return;
        if (s.equalsIgnoreCase("MALE")) this.sex = MALE_CODE;
        else if (s.equalsIgnoreCase("FEMALE")) this.sex = FEMALE_CODE;
        else throw new HPOException("Did not recognize sex code \"" + s + "\"");
    }

    public DiseaseDatabase getDatabase() {
        return database;
    }

    public String getDiseaseID() {
        return diseaseID;
    }

    public String getDiseaseName() {
        return diseaseName;
    }



    public TermId getPhenotypeId() {
        return phenotypeId;
    }

    public String getPhenotypeName() {
        return phenotypeName;
    }

    public TermId getAgeOfOnsetId() {
        return ageOfOnsetId;
    }

    public String getAgeOfOnsetName() {
        return ageOfOnsetName;
    }

    public String getEvidenceID() {
        return evidenceID;
    }

    public String getEvidenceName() {
        return evidenceName;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getFrequencyString() {
        return frequencyString;
    }

    public TermId getFrequencyId() {
        return frequencyId;
    }

    public String getSex() {
        if (sexID != null) return sexID;
        else if (sexName != null) return sexName;
        else if (sex != null) return sex;
        else return "";
    }

    public String getNegation() {
        if (negationID != null) return negationID;
        else if (negationName != null) return negationName;
        else return "";
    }

    public Set<TermId> getModifierSet() {
        return modifierset;
    }

    public String getModifierString() {
        if (modifierset == null || modifierset.isEmpty()) return "";
        else return modifierset.stream().map(TermId::getIdWithPrefix).collect(Collectors.joining(";"));
    }

    public String getDescription() {
        return description;
    }

    public String getPub() {
        return pub;
    }

    public String getAssignedBy() {
        return assignedBy.trim();
    }

    /**
     * Returns the date created, and transforms the date format to YYYY-MM-DD, e.g., 2009-03-23.
     */
    public String getDateCreated() {
        if (dateCreated==null || dateCreated.isEmpty()) {
            QCissues.add(NO_DATE_CREATED);
            // replace with today's date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            Date date = new Date();
            return dateFormat.format(date);
        }
        return convertToCanonicalDateFormat(dateCreated);
    }


    /**
     * COnvert a string such as 9 of 16 to 9/16
     * @param freq A string that has been founf to have the word "of" ion it
     * @return
     * @throws  HPOException if  format does not match 9 of 16
     */
    private String convertNofMString(String freq) throws HPOException {
        String s = freq.replaceAll(" ","");
        int i = s.indexOf("of");
        if (i<0) {
            throw new HPOException("Could not find word \"of\" in N-of-M expression "+freq);
        }
        Integer N,M;
        try {
            N = Integer.parseInt(s.substring(0,i));
        } catch (NumberFormatException e) {
            throw new HPOException(String.format("Could not parse first int in N-of-M expression (could not parse \"%s\")"+freq,s.substring(0,i)));
        }
        int j=i+2; // should be starting place of second number
        if (j>=s.length()) {
            throw new HPOException(String.format("Could not find second int in N-of-M expression for %s (could not parse \"%s\")",freq,s.substring(j)));
        }
        try {
            M = Integer.parseInt(s.substring(j));
        } catch (NumberFormatException e) {
            throw new HPOException(String.format("Could not parse second int in N-of-M expression (could not parse \"%s\")"+freq,s.substring(j)));
        }
        QCissues.add(CONVERTED_N_OF_M);
        return String.format("%d/%d",N,M );

    }


    /** There are 3 correct formats for frequency. This function returns
     * a String representing either a frequency such as 45%, 6/13, or an HPO
     * term id from the frequency subontology. Note that the emtpy string is also
     * valid. All other data will cause an error. Note that by assumption (and this will be
     * true given the formats of the old small files), not more than one type of
     * frequency data can occur in one old small file entry. */
    public String getThreeWayFrequencyString() throws HPOException {
        // it is ok not to have frequency data
        if (frequencyId == null && frequencyString == null) {
            return EMPTY_STRING;
        } else if (frequencyString.isEmpty()) {
            return EMPTY_STRING;
        }
        if (frequencyString.contains("of")) {
            return convertNofMString(frequencyString);
        } else if (frequencyString.matches("\\d+/\\d+")) {
            return frequencyString; // standard n/m
        } else if (frequencyString.matches("\\d{1,2}-\\d{1,2}\\s?\\%")){
            QCissues.add(FREQUENCY_WITH_DASH);
            return frequencyString.replaceAll(" ","");
        } else if (frequencyString.matches("\\d{1,2}\\%-\\d{1,2}\\s?\\%")){
            // remove middle percent sign
            QCissues.add(FREQUENCY_WITH_DASH);
            String f = frequencyString.replaceAll("%","").trim();
            return f+"%";
        } else  if (frequencyString.matches("\\d{1,3}\\%")) {
            return frequencyString; // remove whitepsace
        }else  if (frequencyString.matches("\\d{1,3}\\s+\\%")) {
            QCissues.add(REMOVED_FREQUENCY_WHITESPACE);
            return frequencyString.replaceAll(" ","");
        }  else if (frequencyString.matches("\\d{1,3}\\.?\\d+?\\%")) {
            return frequencyString;
        } else if (frequencyString.matches("\\d{1,3}\\.?\\d+?\\s+\\%")) {
            QCissues.add(REMOVED_FREQUENCY_WHITESPACE);
        return frequencyString.replaceAll(" ","");
    }
        else if (frequencyId != null) {
            if (frequencySubontology.getTermMap().containsKey(frequencyId)) {
                return frequencyId.getIdWithPrefix();
            } else {
                String err = String.format("Attempt to use term %s [%s] as a frequency term",
                        ontology.getTermMap().get(frequencyId).getName(),
                        ontology.getTermMap().get(frequencyId).getId().getIdWithPrefix());
                System.err.println(err);
                throw new HPOException(err);
            }
        } else {
            System.err.println(String.format("Unrecognized frequency string: \"%s\"",frequencyString ));
            // if we get here, frequencyId was null and frequencyString was not null but was not recognized.
            throw new HPOException(String.format("Unrecognized frequency string: \"%s\"",frequencyString ));
        }
    }




}