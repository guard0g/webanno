### q1 ###
**Nach Durchsicht der Infos auf der WebAnno-Website hätten wir bereits zwei Fragen, zu denen wir uns über eine Antwort sehr freuen würden:**
<font color='red'>
<ul><li><b>Wenn bei der PoS-Annotation via WebAnno die Annotatoren Segmentierungsfehler in der zugrundeliegenden Textbasis entdecken, besteht dsann die Möglichkleit, diese Fehler direkt über WebAnno zu korrigieren (d.h. Segmentierungen in der Textbasis zu ändern)? M.a.W.: Ist die Tokenebene nach dem Einspielen des Textes in die Datenbank fixiert oder nicht?</b> </font>
TRANSLATION: Is there a way to chanke tokenization or sentence segmentation directly in WebAnno?</li></ul>

Unfortunately, this is not supported in the current version and has to be done outside of WebAnno.

### q2 ###
<font color='red'>
<ul><li><b>Können in WebAnno auch Metadaten mitverwaltet bzw. gespeichert werden Im Falle unserer Daten verwenden wir u.a. IDs für einzelne Postings und deren Autoren.</b> </font></li></ul>

TRANSLATION: Is there a way to add metadata, such as IDs for postings or their authors, and to show them during annotation?

There is no built-in mechanism. However, you can add the IDs and authorship information as span annotations and import them. Regarding IDs, it is possible to maintain IDs in additional layers in the TCF file format. They will be retained and available in the exported TCF file after annotation, but not shown.
### q3 ###
<font color='red'>
<ul><li><b>I like to see kappa measure per documents, how can I do that.</b> </font>
Kappa is computed for every user and every documents they have finished. If you like per document kappa, 1) copy your project (export it from project settings) 2) mark all documents except the one you like its kappa in state of "in progress, (the play button)), and select the layer</li></ul>

<font color='red'>
<ul><li><b>I have started curation after some users have finished. If new users finished annotation, the curation result is not as I expected.</b> </font>
The best thing to do is to wait untill all users finish annotation before starting curations. If new annotation documents finished, press the re-merge button but you will loose the previous curated results!</li></ul>

### q4 ###
<font color='red'>
<ul><li><b>dear WebAnno-Group, I would like to ask you one question according to WebAnno:<br>
I am using the standalone version of the WebAnno-Tool (2.0.0-beta-5).<br>
Issue:<br>
I have several annotations of the same file in tsv-format and I would<br>
like to see the differences among them and curate them. These<br>
annotations are maden for the same base txt-file, with the same<br>
Layer-set, but in different projects.</li></ul></b>

<b>Question:</b>
Is there any possibility to make a new project which would entail all<br>
these annotations, so that I can compare and curate them?<br>
I do not see any proposed way in WebAnno for this approach, but I was<br>
very glad, if you could tell me a way to realize it.**</font>**

If you already make those annotations in different projects, there is no
easy way getting the annotations in one project and curate them.
In such scenario, usually do the following.
  1. Create **n** users, at least one user per number of files you have in different projects
  1. Export the project as zip file. Get the filename.ser from the zip file and rename it as **userN**.ser. Rename the file as **userN**.ser
  1. In the current project you like to use, login with all users and open the file (this will create the binary file for each of users but without annotations)
  1. Go to your webanno directory, the one you set it during standalone version startup and navigate the to document directory. It should be webannoDIR/repository/project/projectID/document/documentID/annotation.
  1. Copy now all the binday files you have exported and renamed by user name in step 2 above under the folder **..../annotation**/
  1. Now, login using each user and mark the file as finished.
  1. Go to curation and open the file. you should see all annotations of user with disagreements

We will see if we can automate this process in the future webanno versions