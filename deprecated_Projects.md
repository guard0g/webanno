## Projects ##
This is the place to specify/edit annotation projects.
You can either select one of the existing projects for editing, or click “Create Project” to add a project.

**Although correction and automation projects function similarly, the management differs after the creation of the document. For further description, look at the corresponding chapters ([Automation](https://code.google.com/p/webanno/wiki/Automation), [Correction](https://code.google.com/p/webanno/wiki/Correction)).**

## Table of Contents ##


### Create a New Project ###

Only superadmins are allowed to create projects.
Click on “Create Project” to create a new project.

<img src='https://webanno.googlecode.com/svn/wiki/images/project1.jpg' width='200' />

After doing so, a new pane is displayed, where you can name and describe your new project. It is also important to chose the kind of project you want to create. You have the choice between annotation, automation and correction.
Please do not forget to save.

<img src='https://webanno.googlecode.com/svn/wiki/images/project2.jpg' width='400' />

After saving the details of the new project, it can be treated like any other already existing one.  Also, you are displayed a new pane with many options to organize it.

### Edit and Organize a Project ###

The pane with the options to organize and edit a project, as described above, can also be reached by clicking on the desired project in the left frame.

<a href='Hidden comment: 
project3.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project3.jpg' width='600' />

By clicking on the tabs, you can now set up the chosen project.

#### User administration ####

After clicking on “Users”, you are displayed a new pane in which you can add new users by clicking on the button “Add User”.  After doing so, you get a list of users in the system which can be added to the project. By making a tick in front of the log in you can chose a new user.

<a href='Hidden comment: 
project4.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project4.jpg' width='200' />

Please do not forget to save after choosing all members of the project. Close the pane by clicking on “Cancel”. The rights of users created like this are that of an annotator. If you want to expand the user's status, you can do so by clicking on the user and then on “Change permission”. The following frame will pop up.


<a href='Hidden comment: 
project5.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project5.jpg' width='600' />

After ticking the wished permissions, click update.
To remove a user, click on the login and then “Remove User”.

#### Document administration ####
To add or delete documents, you have to click on the tab “Documents” in the project pane. Two frames will be displayed. In the first frame you can import new documents.

<a href='Hidden comment: 
project6.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project6.jpg' width='600' />

Choose a document by clicking on “Dateien auswählen”. Please mind the format, which you have to choose above.  Then click on “Import document”.
The imported documents can be seen in the frame below.
To delete a document from the project, you have to click on it and then click on “Delete” in the right lower corner.

#### Layer administration ####
This feature is currently under development and looks different in the currently available version. The new version will be released soon.

To administer the layers, click on "Layer". A frame showing all existing layers in this project will appear. The colouring of the layers signal the following:

green: default annotation layer, enabled

blue: personal annotation layer, enabled

red: disabled annotation layer

By default, only the default layer will be displayed as enabled.
To disable a layer, which means that it will not be shown to the users of the project, first select the layer by clicking on it. The following frames will open.

<img src='https://webanno.googlecode.com/svn/wiki/images/layer1.JPG' width='600' />

In the first frame, there is field "Enabled", which can be ticked or unticked. By unticking the field it can be made invisible to the users. Layers cannot simply be deleted.

##### New Layer Creation #####

To create a new layer, select "Create Layer" in the "Layers" frame. Then, the following frame will be displayed.

<img src='https://webanno.googlecode.com/svn/wiki/images/layer2.JPG' width='200' />

The only obligatory field in this frame is the "Layer name". Above, the user can give a description to the new layer. The tick after enabled signals whether the layer is shown to the users of the project, as it was already explained above.

In the frame "Technical Properties", the user may select the type of annation that will be made with this layer: span, relation or chain.
Span annotations cover an optional range of characters - tokens, words or sentences.

Relation and chain annotations are used for annotating links between two spans.

For relation and chain annotations the type of the spans which are to be connected can be chosen in the field "Attach to layer". Here only non-default layers are displayed. To create a relation or chain annotation, first the span annotation needs to be created.

In the last frame, "Behaviours", other properties of the annotation may be selected. The first field, "Lock to token offsets", gives the possiblity to bind the annotation to a token.
The next field, "Allow stacking", gives the possibility to make several annotations over one range.
The third field, "Allow crossing sentence boundary" gives the possibility to annotate a range covering more than one sentence.
After clicking on "Save layer" only changes in the first frame ("Properties") may be made. After the layer has been saved, it will be displayed in the Layer frame.

To be able to make annotations with this layer, features need to be added. This can be done by choosing the layer and clicking on "New feature" in the "Feature overview" frame. Then the following frame will be displayed:

<img src='https://webanno.googlecode.com/svn/wiki/images/layer3.JPG' width='200' />

In this frame details of the features may be chosen.
In the first field, the user may choose between different object types the tags of the feature are going to have (string, integer, float or boolean).
A name and a description may also be given to the feature.
The field "Enabled" shows whether the feature can be chosen by users.
The field "Show" shows whether the feature is shown to the user.
A tagset may be chosen in the last field. Only non-default tagsets may be chosen. Tagsets may be created in the "Tagsets" frame above. The next chapter explains how to create new tagsets in detail.


**Please take care that when working with non-custom layers, they have to be ex- and imported, if you want to use the resulting files in e.g. correction projects.**


#### Tagset administration ####

To administer the tagsets, click on the tab “Tagsets” in the project pane.

<a href='Hidden comment: 
project7.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project7.jpg' width='200' />

To administer one of the existing tagsets, select it by a click. Then, the tagset characteristics are displayed.

<a href='Hidden comment: 
project8.jpg here
'></a>

<img src='https://webanno.googlecode.com/svn/wiki/images/project8.jpg' width='800' />

In the Frame “Tagset details”, you can change them,  export a tagset, save the changes you made on it or delete it by clicking on “Delete tagSet”.
To change an individual tag, you select one in the list displayed in the frame “Tags”. You can then change its description or name or delete it by clicking “Delete tag” in “Tag details”.  Please do not forget to save your changes by clicking on “Save tag”.
To add a new tag, you have to click on “Create tag” in “Tag details”. Then you add the name   and the description, which is optional. Again, do not forget to click “Save tag” or else the new tag will not be created.

To create an own tagset, click on "Create tagset" and fill in the fields that will be displayed in the new frame. Only the first field is obligatory. Adding new tags works the same way as described for already existing tagsets. If you want to have a free annotation, as it could be used for lemma or meta information annotation, do not add any tags.

<img src='https://webanno.googlecode.com/svn/wiki/images/new_tagset.JPG' width='400' />

To export a tagset, choose the format of the export at the bottom of the frame and click "Export tagset".

#### Guidelines administration ####

To add or delete guidelines, which will be accessible by users in the project, you have to select the tab “Guidelines”. Two new frames will be displayed.
To upload guidelines, click on “Dateien auswählen” in the first frame – “Add guideline document”, select a file from your local disc and then click “Import guidelines”.


<img src='https://webanno.googlecode.com/svn/wiki/images/project9.jpg' width='800' />

Uploaded guidelines are displayed in the second frame – “Guideline documents”.
To delete a guideline document, click on it and then on “Delete” in the right lower corner of the frame.

### Delete Project ###

<img src='https://webanno.googlecode.com/svn/wiki/images/project10.jpg' width='600' />

To delete a project, click on it in the frame “Details”. Then you are displayed the details of the project. Click on “Delete”.

### Export Documents ###

<img src='https://webanno.googlecode.com/svn/wiki/images/export_project.JPG' width='600' />

WebAnno offers two modes of exporting projects:

  * **Export the whole project** for the purpose of creating a backup, of migrating it to a new WebAnno version, of migrating to a different WebAnno instance, or simply in order to reimport it as a duplicate copy.
  * **Export curated documents** for the purpose of getting an easy access to the final annotation results. If you do not have any curated documents in your project, this export option is not offered.

**Bug note: do not leave the export page after initiating an export before the progress bar is complete or your WebAnno instance can become locked until it is restarted!**

When exporting a whole project, the structure of the exported ZIP file is a follows:

  * **<project ID>.json** - project metadata file
  * **annotation**
    * **<source document name>**
      * **<user ID>.XXX** - file representing the annotations for this user. The file format corresponds to the format of the source document.
  * **annotation\_ser**
    * **<source document name>**
      * **<user ID>.ser** - serialized CAS file representing the annotations for this user
  * **curation**
    * **<source document name>**
      * **CURATION\_USER.tsv** - TSV file representing the state of curation
  * **curation\_ser**
    * **<source document name>**
      * **CURATION\_USER.ser** - serialized UIMA CAS representing the state of curation
  * **log**
    * **<project ID>.log** - project log file
  * **source** - folder containing the original source files

Currently, WebAnno does not allow the user to choose a specific format for bulk-exporting annotations. However, [this mailing list post](https://groups.google.com/forum/#!msg/webanno-user/X3ShaFPXQT0/PnBzpPdXrIgJ) describes how [DKPro Core](https://code.google.com/p/dkpro-core-asl/) can be used to transform the UIMA CAS formats into alternative formats.