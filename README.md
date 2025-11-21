# HPO Annotation Quality Control (hpoannotqc)

A Java application for quality control, validation, and transformation of Human Phenotype Ontology (HPO) annotation files.

## Overview

This application performs quality control on HPO rare disease annotation files (small files) and transforms them into integrated phenotype annotation files (big files). It validates HPO annotations against the HPO ontology, checks for obsolete terms, validates biocuration information, and generates comprehensive output files for downstream analysis.

**Version:** 2.0.0

## Features

- **Quality Control**: Validates HPO annotation entries for correctness
- **Format Conversion**: Converts small annotation files to the unified phenotype.hpoa format
- **Obsolete Term Detection**: Identifies and auto-updates obsolete HPO terms
- **Validation**: Checks disease IDs, phenotypes, evidence codes, publications, frequencies, and more
- **File Generation**: Creates supplemental files (genes_to_phenotype.txt, phenotype_to_genes.txt, genes_to_disease.txt)
- **Comprehensive Logging**: Generates detailed QC reports and logs

## Requirements

- **Java 11** or higher
- **Maven 3.6+** (for building)
- Internet connection (for downloading HPO data)

## Building

Build the application using Maven:

```bash
./mvnw clean package
```

This creates an executable JAR: `target/HpoAnnotQC-2.0.0.jar`

## Running Tests

The project includes comprehensive test coverage (230+ tests):

```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Commands

The application provides four main commands:

### 1. Download Command

Downloads required data files (HPO ontology, Orphanet XML data):

```bash
java -jar target/HpoAnnotQC-2.0.0.jar download [--overwrite]
```

**Options:**
- `--overwrite`: Force re-download of existing files

**Output:**
- Creates a `data/` directory with downloaded resources
- Downloads `hp.json` (HPO ontology)
- Downloads Orphanet XML files

### 2. Big File Command

Converts HPO annotation small files into the integrated `phenotype.hpoa` file:

```bash
java -jar target/HpoAnnotQC-2.0.0.jar big-file \
    -a /path/to/hpo-annotation-data/rare-diseases/annotated \
    [-o /path/to/omit-list.txt]
```

**Options:**
- `-a, --annotdir <path>`: Directory containing HPO annotation small files (required)
- `-o, --omit-list <path>`: Optional list of disease IDs to omit from processing

**Output:**
- `phenotype.hpoa`: Unified HPO annotation file
- `hpoannotQC.log.<date>`: Detailed processing log
- Console output showing processing statistics

**Features:**
- Auto-updates obsolete HPO terms in Orphanet annotations
- Validates all annotation entries
- Merges OMIM, ORPHA, DECIPHER, and other disease database annotations

### 3. Small File QC Command

Performs quality control on individual small annotation files:

```bash
java -jar target/HpoAnnotQC-2.0.0.jar small-file-qc \
    -a /path/to/hpo-annotation-data/rare-diseases/annotated \
    [-o /path/to/omit-list.txt]
```

**Options:**
- `-a, --annotdir <path>`: Directory containing HPO annotation small files (required)
- `-o, --omit-list <path>`: Optional list of disease IDs to omit from validation

**Output:**
- Console report of QC results
- Identifies files with validation errors
- Reports obsolete terms and suggested updates

**Validates:**
- Disease database prefixes (OMIM, ORPHA, DECIPHER, MONDO)
- HPO term IDs and labels
- Age of onset terms
- Frequency modifiers (ratios, percentages, HPO terms)
- Sex specifications
- Evidence codes (TAS, IEA, PCS)
- Publication/citation formats
- Biocuration entries

### 4. Supplemental Files Command

Generates supplemental gene-phenotype and gene-disease mapping files:

```bash
java -jar target/HpoAnnotQC-2.0.0.jar supplemental-files
```

**Output:**
- `genes_to_phenotype.txt`: Gene to phenotype associations
- `phenotype_to_genes.txt`: Phenotype to gene associations
- `genes_to_disease.txt`: Gene to disease associations

## Data Sources

The application works with HPO annotation data from:
- **HPO Annotation Data Repository**: https://github.com/monarch-initiative/hpo-annotation-data
- **Human Phenotype Ontology**: https://hpo.jax.org/
- **Orphanet**: Rare disease data in XML format

## File Formats

### Small Files (Input)

Tab-delimited files with 14 columns:
```
#diseaseID  diseaseName  phenotypeID  phenotypeName  onsetID  onsetName  frequency  sex  negation  modifier  description  publication  evidence  biocuration
```

### Big File (Output - phenotype.hpoa)

Tab-delimited file with 12 columns:
```
#diseaseID  diseaseName  qualifier  HPO_ID  reference  evidence  onset  frequency  sex  modifier  aspect  biocuration
```

## Project Structure

```
src/
├── main/java/org/monarchinitiative/hpoannotqc/
│   ├── annotations/          # Annotation models and validation
│   ├── cmd/                  # Command implementations
│   ├── exception/            # Custom exceptions
│   └── Main.java            # Application entry point
└── test/java/                # Comprehensive test suite (230+ tests)
```

## Dependencies

- **Phenol 2.1.4**: HPO ontology library
- **Picocli 4.7.5**: Command-line interface
- **Logback 1.5.3**: Logging
- **JUnit 5.10.2**: Testing framework

## Development

### Test Coverage

The project maintains high test coverage:
- 230+ unit tests
- 100% coverage for core annotation classes:
  - `BiocurationChecker`
  - `DiseaseDatabase`
  - `HpoAnnotationFileParser`
  - `HpoAnnotationFileWriter`
  - `HpoAnnotationFileValidator`
  - `AnnotationFileDiscovery`
  - And more...

### Running Locally

```bash
# Build
mvn clean package

# Download data
java -jar target/HpoAnnotQC-2.0.0.jar download

# Run QC on local annotations
java -jar target/HpoAnnotQC-2.0.0.jar small-file-qc \
    -a /path/to/annotations

# Generate big file
java -jar target/HpoAnnotQC-2.0.0.jar big-file \
    -a /path/to/annotations
```

## Logging

The application uses SLF4J with Logback:
- Console output: INFO level
- Log files: DEBUG level with detailed validation information
- Log file naming: `hpoannotQC.log.<timestamp>`

## Contributing

When adding new validation logic:
1. Update the appropriate validator class in `annotations/`
2. Add comprehensive unit tests
3. Run `mvn test` to ensure all tests pass
4. Run `mvn jacoco:report` to verify coverage

## Authors

- **Peter Robinson** (peter.robinson@jax.org)
- **Michael Gargano** (michael.gargano@jax.org)

## License

See LICENSE file for details.

## Links

- [Human Phenotype Ontology](https://hpo.jax.org/)
- [HPO Annotation Data](https://github.com/monarch-initiative/hpo-annotation-data)
- [Phenol Library](https://github.com/monarch-initiative/phenol)
