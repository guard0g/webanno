From Downloads https://code.google.com/p/webanno/downloads/list, get the sample projects and import them into a running WebAnno instance. These are sample projects exported from a working webanno instance. sample-projects.tar.bz2 contains 9 compressed projects (all in zip format). First untar sample-projects.tar.bz2 and get those zip projects. After starting webanno, login as admin, go to projects settings page and chose one zip file at atime in the **Import Project** frame below the **Projects** list box and import them.

# README #

These are sample collections of WebAnno projects including annotated documents, as anno1 and anno2 users.
  * demo-anno-chunk: This Project contains chunk/POS annotated documents.
  * demo-anno-coref: This project contains documents annotated with corefernce chains and Lemma annotations (one in English and one in German).
  * demo-anno-de: This project contains German documents annotated with Named Entity, POS and Dependency parsing annotations.
  * demo-anno-en: This project contains English documents annotated with Named Entity,Lemma, POS and Dependency parsing (With large documents, 5100+ sentences).
  * demo-anno-moredoc: This project contains large number of documents (140+) annotated with NE.
  * demo-anno-sv: This Project contain a document annotated with POS and dependency parsing for Swedish.
  * demo-anno-unicode: This project contains documents annotated with POS and dependency parsing which are UNICODE encoded (for Amharic and korean).
  * demo-corr-en: This project contains automatically annotated documents for NE, Lemma, POS and dependency parsing where users can correct/modify annotations.
  * demo-crowd: This project contains documents that will be farmed out to the CrowdFlower courdsourcing platform.

  1. Users anno1 and anno2 should be added using the Manage Users WebAnno page.
  1. se the Annotation page to try to annotate documents with the selected annotation layers. Refer the annotation guideline for details(https://code.google.com/p/webanno/wiki/UserGuide). You can also click the help button in annotation, correction and curation page to refer to annotation guideline.
  1. Use the correction page to open correction projects where automatically annotated documents opened in the lower half of the page and annotation view on the upper half of the page.To copy annotations from the automated document, click on the annotation. You can also add new annotation in the same way you add in the annotation page.
  1. Use the monitoring page to manually close/open annotation documents in progress. Annotation documents in progress displays as X/Y where X is the last sentence annotated and Y is the total number of sentences. Annotation documents closed show a tick symbol(√)
  1. Use the curation page to visualize annotation disagreements for those users whose annotation document marked as closed (√)