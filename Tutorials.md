<h2>Table of Contents</h2>



# Annotation Project Creation #

This a tutorial which leads through the annotation function. It will show how to create an annotation  project, how to upload documents and how to create personalized layers and tagsets. How to annotate the documents with the given layers will be shown in the next chapter.



## Setting the project ##

If you have any problems with these steps, see the following instructions with screenshots [Projects](Projects.md).



To create a new project,  click on “Projects“, then on “Create Projects“. Then fill in the fields in the newly appeared frame. Here the project name is “FreeAnnoTutorial“. The setting of an annotation project is made by default. After doing so, click on “Save“.



## Upload the document ##

Click on the tab page “Documents“, choose the “Plain text“ format, click on “Choose file“ and choose the uploaded “infile.txt“, then click “Import document“. Now the file should be visible in the Documents frame.



## Layers administration ##

By clicking on the tab page “Layers“ the first time, only the default layers offered by WebAnno will be displayed – Coreference, Dependency, Lemma, Named Entity and POS.

This tutorial will demonstrate how to create personalized layers by reproducing annotations that were made in other annotation projects. The detailled explanation of the offered features will be presented in the Morphological Layer only. If any particular feature is special for any of the following layers, it will be explained in more detail there.



## **Free Layer Creation** ##

First click on “Create Layer“ in the right bottom of the “Layers“ frame, like shown in the following screenshot:

<img src='https://webanno.googlecode.com/svn/wiki/images/choseLayers.JPG' width='800' />


### Morphological Layer ###

Setting the layer properties

The following settings need to be made to create a layer, which will make morphological annotation of prefixes, stems and suffixes possible:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut1.JPG' width='300' />



In the following the settings will be explained in detail. The fields “Layer name“ and “Description“ are self-explanatory. The field beneath, “Enabled“ is chosen by default, which means that the layer will be presented to the users.

In the technical properties, the type “span“ is selected, as a range of characters will be chosen for the annotation. The layer is not attached to another layer.

None of the offered behaviours is chosen.

The first one would choose only tokens as a span. The second would allow several annotations of one type made parallely. The third field is self-explanatory. The last field would allow annotations over several tokens.

Be careful not to press “Save layer“ before having set the right properties, as they cannot be changed afterwards.



After having created the layer, the tagset has to be constructed. For this, go to the tab set “Tagset“. Then, click on “Create tagset“ as signalled by 1) in the following image. Then fill in the fields like shown above and click on “Save tagset“ as shown by 2) in the image.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut2.JPG' width='600' />

After saving the tagset, it will be displayed in the “Tagsets“ frame. To create the tags, select the tagset in in the frame by clicking on it.

Two frames will appear, the filled out example of “Prefix“ is shown below. “Stem“ and “Suffix“ are created in the same way. After clicking on “Save tag“ it will appear in the in the “Tags“ frame.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut3.JPG' width='600' />
.

To create new tags, click “Create Tag“ in the “Tagsets“ frame. Add the tags “Prefix“,“Stem“ and “Suffix“ to the tagset.

Then go back to the tab set “Layers“, select the layer “Morphology“ and click on “New feature“ in the  Feature overview.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut4.JPG' width='200' />


The following frame will appear, and shall be filled in in the shown way for the morphological annotation:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut5.JPG' width='300' />


The internal object type that is chosen is “String“ ASK WHY. The enabled button makes sure, that the  users are able to make this annotation, the show button whether already made annotations with this feature will be shown to the user. Choose the Morphology tagset that you just created in TagSet. Again, before saving the feature, make sure you chose the right type and tagset, and this cannot be changed afterwards.




<a href='Hidden comment: 
===Predicate relation layer==

In this layer, a possibility to rebuilt the annotations that were made in PropBank (for guidelines of PropBank, see [http://verbs.colorado.edu/~mpalmer/projects/ace/EPB-annotation-guidelines.pdf Bonial et al. 2012], is shown. Summarizing, in this corpus, predicates and their arguments are annotated according to their role towards the argument.

As this layer is supposed to be able to annotate relations of token spans, its creation is not as trivial as the preceding layer. For relation annotations, first the corresponding span annotation layer, in this case annotating the verb and its possible arguments, has to be created.

To do so, go to the “Layers” tab set on the Projects page and click on “Create Layer”, then fill in the fields as shown in the image below.

<img src="https://webanno.googlecode.com/svn/wiki/images/tut6.JPG" width="300"/>


In this layer, the annotation should be bound to a token, which is done with tick next to “Lock to token offsets” in “Bahaviour”.

To create the tagset, go to Tagset and first click on “Create Tagset”, then fill in the fields as shown in the image below in the “Tagset details” frame.

<img src="https://webanno.googlecode.com/svn/wiki/images/tut7.JPG" width="800"/>


After doing so, create the tags as they are shown above in the “Tags” frame. The descriptions of the six tags were taken from the guidelines mentioned above.

<img src="https://webanno.googlecode.com/svn/wiki/images/propbank.JPG" width="600"/>[http://verbs.colorado.edu/~mpalmer/projects/ace/EPB-annotation-guidelines.pdf Bonial et al. 2012, p.3]

Having created the tagset, go back to “Layers” and choose the “VerbSpan” layer. Then click on “New feature” in the “Feature overview” frame and fill it like shown in the image below:

<img src="https://webanno.googlecode.com/svn/wiki/images/tut8.JPG" width="300"/>

Now the relation layer shall be created. For this, fill the “Properties” layer like shown below after clicking on “Create Layer”.

<img src="https://webanno.googlecode.com/svn/wiki/images/tut9.JPG" width="300"/>

Now in “Technical Properties” the “Relation” type is chosen and the layer is attached to the “VerbSpan” layer that was just created. Do not forget to save the the layer.

'></a>



### Morphological Feature Layer ###

In this layer, morphological features of the German langue will be made possible. This layer is similar to the annotations that were used in the TIGER project (see [Crysmann et al.](http://www.ims.uni-stuttgart.de/forschung/ressourcen/korpora/TIGERCorpus/annotation/tiger_scheme-morph.pdf)).

In German, nouns and articles can be changed according to gender, number and case; verbs according to person, number, tense and mode; and adjectives and adverbs according to degree. These parts of speech will be annotated in separate layers.

The  “Noun” layer creation will be shown in detail, the other layers can be be made accordingly. A table which shows the morphological features to produce the layers is provided below.

<img src='https://webanno.googlecode.com/svn/wiki/images/table.JPG' width='600' />

To create the noun layer, first click on “Create Layer” in the “Layers” tab set. Then fill in the frames as shown in the following image.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut10.JPG' width='300' />

Then click on “Save layer” and go to Tagsets tabset. There, click on “Create tagset”, then fill in the fields as shown in the right frame in the image below.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut11.JPG' width='600' />

Then click on “Save tagset” and choose the “Gender” tagset in the “Tagsets” frame. Then create the tags that are shown in the bottom of the image. Click on “Create tag”, enter the name, then click “Save tag”.

Now create a new tagset, in the same way as shown before, for “Number”. The result of having created “Number” and the tags “Singular” and “Plural” are shown below.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut12.JPG' width='800' />

Now create the the tagset for “Case”. The results of doing so are shown below.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut14.JPG' width='800' />

After doing so, go back to “Layers”, choose the “Nouns” layer and add the tagsets. For this, click on “New feature” in the “Feature overview” frame. The filled fields for “Gender” are shown below. The other tagsets, “Number” and “Case”, are added in the same way.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut13.JPG' width='200' />



### Opinion Layer ###

For this kind of annotation, again two layers will be created. One layer will annotate spans indicating the opinion target and the words referencing it. The other layer will combine those with a positive or negative polarity,

To create the first layer, first select the “Layers” and click “Create layer”, then fill in the fields like shown in the image below.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut15.JPG' width='300' />

Multiple tokens may be annotated with one tag, as words like “very” or “much”, but also “not” may change or amplify the meaning of the categorizing words.

Now go to the tab set “Tagsets”, click on “Create tagset”and fill the fields as shown in the right upper frame in the image below. Then add the tags shown in the left lower frame in the image by choosing “OpinionComponents”, clicking on “Create tag, filling in the name and clicking “Save tag”.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut16.JPG' width='800' />

Then go back to “Layers”, choose “OpinionComponents”, click on “New feature” and fill the fields in the newly appeared frame as shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut17.JPG' width='300' />

Now create the layer indicating the polarity of the references. Create a new layer, filling the fields as shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut18.JPG' width='300' />

Here crossing boundaries of sentences may be crossed, as the opinion target may be mentioned in other sentences than categorizing words.

Now go to the “Tagsets” and create the tagset with the tags “positive” and “negative” as shown in the image below.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut19.JPG' width='800' />

Afterwards go back to “Layers”, click “New feature” in the right frame and fill the new frame as shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut20.JPG' width='300' />



### Metainformation Layer ###

In this part it will be shown how to produce a layer in which several free annotations may be added to several tokens, as for example needed when annotating facts about persons.

Go to the tab set “Layers” and fill it like shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut21.JPG' width='300' />

Afterwards click on “New feature” and fill the fields as shown in the image. Notice that no tagset is added, as a layer like this allows free annotation tags.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut22.JPG' width='300' />

# Annotation #
In this chapter the different annotation layers will be shown.

## Uploading Project ##
If you have not created the Annotation Project as it was described in the previous chapter, you may download the resulting project from here.

To import the project, go to the "Projects" page, upload the file after clicking on the upload button and click "Import project". Now "FreeAnnoTutorial" should be visible in the "Projects" frame.

## Annotating ##
Go to the "Annotation" page, which can be reached from the Home page. A dialogue will be opened. Choose the project "FreeAnnoTutorial" in the left frame and "infile.txt" on the right frame, as shown in the image below. The first two lines are the source.

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno1.JPG' width='400' />

After clicking on "Open", the following view will be visible:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno2.JPG' width='800' />

If you see less sentences than shown in the image, you may change this by clicking on "Settings" and changing the number in "Number of sentences". You may also choose only those layers, that you want to annotate here.

## Span Annotation ##

For span annotation, we will show how to annotate POS and Morphology. Then you may annotate any span annotation layer you like. For clarity, the annotations shown below show only the annotation level that is explained. In the end of this tutorial you will be shown an example of how all annotations may be made.

For POS annotation, first mark a span that you want to annotate by marking it with the left mouse tab (Notice, that as this layer is locked to a token, the whole token will be annotated, even if only a part of it was marked). In the following examplary image the first word, "Wir" was marked. The frame shown below will open. In the field "Layers", choose "POS" and "PPER" as a POSvalue. You may also type or start typing the values in, which will make it quicker. The annotation tag set is STTS [Schiller et al., 1999](http://www.sfs.uni-tuebingen.de/resources/stts-1999.pdf)

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno3.JPG' width='300' />

After choosing the described annotation, click "Annotate". Now the sentence should look like shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno4.JPG' width='500' />

The fully annotated sentence should look like this:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno5.JPG' width='500' />

For the example of morphological annotation, we will take the second word, _haben_, as the first is a complete stem. Now take care that you mark only what you want to annotate, as this time not the whole token will be chosen. As this is an irregular verb, we will mark "ha" as the stem. After choosing it with the left mouse button, choose the "Morphology" layer and "stem" as the morphology feature and click "Annotate". Afterwards mark "en" as suffix" in the same way. The annotation should look like this:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno6.JPG' width='500' />

The fully morphologically annotated sentence looks like this:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno7.JPG' width='500' />

## Relation annotation ##
In order to show how relation annotation works, examples of <a href='Hidden comment: the layer similar to PropBank and '></a>the opinion layer will be provided.

<a href='Hidden comment: 
The PropBank similar layer annotates verbs and its arguments.
Before annotating the relation itself, the components need to be annotated. First mark the verb - "haben" - in the 3rd sentence. Then choose the "VerbSpan" in "Layer" and "Verb" as the component in "Features".
Afterwards mark "Wir" as "Arg0" (agent), "seit einigen Monaten" as "ArgM" (modifier) and "diese Kaffeemaschine" as "Arg2" (instrument)and "in Gebrauch" as "Arg3" (attribute). The result of annotating the sentence looks like this:

<img src="https://webanno.googlecode.com/svn/wiki/images/tut_anno8.JPG" width="500"/>

Now all arguments should be related to the verb. To combine an argument to the verb,
'></a>

To create an Opinion layer annotation, first the opinion target and the opinion references have to be annotated as spans. Then they are combined.
The creation of an OpinionComponent span annotation is like the span annotation layers that were described above. Mark "Kaffeemaschine", choose the layer "OpinionComponent" and "Opinion target" as a feature and click "Annotate". Now mark "schnell" (in the 4th sentence) as "Target reference" in the same layer. Now click on the "Target reference" annotation (not the word "schnell") and pull the upcoming arrow to the "Opinion target" annotation (not the word "Kaffeemaschine"). A new frame will appear. In this, the annotation tag of the relation may be chosen. In our case, the "OpinionPolarity" is "positive". The resulting filled frame is shown below:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno9.JPG' width='500' />


After clicking on "Ok" the annotation should look like this:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno10.JPG' width='800' />

Chain annotation
The chain annotation is similar to the relation annotation. The difference is that it combines more than two components. The exemplary layer chosen here is "Coreference".
For this, mark "Kaffeemaschine" (3rd sentence), "Sie" (4th sentence) and "sie" (also 4th sentence) as "nam" in the "Coreference" layer. Then click on Kaffeemaschine" and pull the upcoming arrow till "Sie". Choose "anaphoric" as annotation. Do the same between "Sie" and "sie". The following image shows the resulting annotation:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno11.JPG' width='800' />

When going over with the mouse over one component of the chain, the preceeding and the following component and their links will be highlighted.

In the image above, additionally to the described layers, some of the sentence were annotated on the Noun, Named Entity and MetaInformation layer. An examplary image is provided above:

<img src='https://webanno.googlecode.com/svn/wiki/images/tut_anno12.JPG' width='800' />


