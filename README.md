# AbDesigner

AbDesigner is a tool for analyzing the amino acid sequence of a given protein to identify optimal immunizing peptides for production of antibodies. It displays the information needed to choose an immunizing peptide, so the user can see the trade-offs between immunogenicity, specificity, animal species targets, and post-translational modifications:

- Immunogenicity Score (based on Kyte-Doolittle hydropathy and Chou-Fasman beta-turn propensity)
- Uniqueness Score (how unique the peptide's epitopes are compared with other proteins of the same species)
- Conservation Score (how well the peptide's epitopes are conserved in human, rat and mouse)
- Known protein features from Swiss-Prot, such as post-translational modifications

**Input:** an official gene symbol, Swiss-Prot accession number, or Swiss-Prot entry name from human, rat, mouse, fly (*D. melanogaster*), worm (*C. elegans*), yeast (*S. cerevisiae*) or *Arabidopsis thaliana*; or a protein sequence in FASTA format for other species.

**Use it online:** https://esbl.nhlbi.nih.gov/AbDesigner/

AbDesigner was developed at the [Epithelial Systems Biology Laboratory](https://esbl.nhlbi.nih.gov/), National Heart, Lung, and Blood Institute (NHLBI), NIH, Bethesda, Maryland, USA.

## Disclaimer

Recommended peptides are not guaranteed to successfully produce antibodies. Users should consider all factors when planning an antibody production project, including the conjugation method, immunization protocol, and antibody purification procedure.

## Citation

If you use AbDesigner in published work, please cite:

Pisitkun T, Hoffert JD, Saeed F, Knepper MA. NHLBI-AbDesigner: an online tool for design of peptide-directed antibodies. *Am J Physiol Cell Physiol.* 2012;302(1):C154-C164. [PubMed 21956165](https://pubmed.ncbi.nlm.nih.gov/21956165/)

## Version 2.0

Version 2.0 keeps the same scoring methods and database as the original version. The main changes are:

- The heat map is now drawn with JavaScript (the Java Applet is no longer needed).
- Runs on Apache Tomcat 10 (`jakarta.servlet`).
- Large proteins (for example RYR2, DMD, PKD1) are computed in the background, so the page no longer times out.

Full list of changes: see [release v2.0](../../releases/tag/v2.0).

## Install

1. Download `AbDesigner.war` from [release v2.0](../../releases/tag/v2.0).
2. Copy it into the `webapps/` folder of Apache Tomcat 10.
3. Open `http://<your-server>:8080/AbDesigner/`.

## Source code

- `src/java/gov/nih/nhlbi/abdesigner/` — Java code (scoring, database access, servlet)
- `web/` — web pages (JSP, HTML, JavaScript, CSS)
- `nbproject/`, `build.xml` — NetBeans / Ant project files

The protein database files (`web/WEB-INF/database/*.ab` and `*.ran`) are too large for this repository and are not included here. They are inside `AbDesigner.war`. To build from source, extract them from the `.war` into `web/WEB-INF/database/` first.

## Contact

Mark A. Knepper, MD, PhD — knepperm@nhlbi.nih.gov
