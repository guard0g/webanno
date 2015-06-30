# Introduction #

In this chapter all tutorials are displayed.


# Details #

This a tutorial which leads through the annotation function. It will show how to create an annotation  project, how to upload documents, how to create personalized layers and tagsets, how to annotate the documents with the given layers and how to export projects.





Setting the project

If you have any problems with these steps, see the following instructions with screenshots http://code.google.com/p/webanno/wiki/Projects.



To create a new project,  click on “Projects“, then on “Create Projects“. Then fill in the fields in the newly appeared frame. Here the project name is “FreeAnnoTutorial“. The setting of an annotation project is made by default. After doing so, click on “Save“.



Upload the document

Click on the tab page “Documents“, choose the “Plain text“ format, click on “Choose file“ and choose the uploaded “infile.txt“, then click “Import document“. Now the file should be visible in the Documents frame.



Layers administration

By clicking on the tab page “Layers“ the first time, only the default layers offered by WebAnno will be displayed – Coreference, Dependency, Lemma, Named Entity and POS.

This tutorial will demonstrate how to create personalized layers by reproducing annotations that were made in other annotation projects. The detailled explanation of the offered features will be presented in the Morphological Layer only. If any particular feature is special for any of the following layers, it will be explained in more detail there.



Free Layer Creation

First click on “Create Layer“ in the right bottom of the “Layers“ frame, like shown in the following screenshot:

[choseLayers.JPG] → make a red field around button



Morphological Layer

Setting the layer properties

The following settings need to be made to create a layer, which will make morphological annotation of prefixes, stems and suffixes possible:

[tut1.JPG]



In the following the settings will be explained in detail. The fields “Layer name“ and “Description“ are self-explanatory. The field beneath, “Enabled“ is chosen by default, which means that the layer will be presented to the users.

In the technical properties, the type “span“ is selected, as a range of characters will be chosen for the annotation. The layer is not attached to another layer.

None of the offered behaviours is chosen.

The first one would choose only tokens as a span. The second would allow several annotations of one type made parallely. The third field is self-explanatory. The last field would allow annotations over several tokens.

Be careful not to press “Save layer“ before having set the right properties, as they cannot be changed afterwards.



After having created the layer, the tagset has to be constructed. For this, go to the tab set “Tagset“. Then, click on “Create tagset“ as signalled by 1) in the following image. Then fill in the fields like shown above and click on “Save tagset“ as shown by 2) in the image.

[tut2.JPG]

After saving the tagset, it will be displayed in the “Tagsets“ frame. To create the tags, select the tagset in in the frame by clicking on it.

Two frames will appear, the filled out example of “Prefix“ is shown below. “Stem“ and “Suffix“ are created in the same way. After clicking on “Save tag“ it will appear in the in the “Tags“ frame.

[tut3.JPG].

To create new tags, click “Create Tag“ in the “Tagsets“ frame. Add the tags “Prefix“,“Stem“ and “Suffix“ to the tagset.

Then go back to the tab set “Layers“, select the layer “Morphology“ and click on “New feature“ in the  Feature overview.

[tut4.JPG]

The following frame will appear, and shall be filled in in the shown way for the morphological annotation:

[tut5.JPG]

The internal object type that is chosen is “String“ ASK WHY. The enabled button makes sure, that the  users are able to make this annotation, the show button whether already made annotations with this feature will be shown to the user. Choose the Morphology tagset that you just created in TagSet. Again, before saving the feature, make sure you chose the right type and tagset, and this cannot be changed afterwards.





Predicate relation annotation

In this layer, a possibility to rebuilt the annotations that were made in PropBank (for guidelines of PropBank, see http://verbs.colorado.edu/~mpalmer/projects/ace/EPB-annotation-guidelines.pdf, is shown. Summarizing, in this corpus, predicates and their arguments are annotated according to their role towards the argument.

As this layer is supposed to be able to annotate relations of token spans, its creation is not as trivial as the preceding layer. For relation annotations, first the corresponding span annotation layer, in this case annotating the verb and its possible arguments, has to be created.

To do so, go to the “Layers” tab set on the Projects page and click on “Create Layer”, then fill in the fields as shown in the image below.

[tut6.JPG]

In this layer, the annotation should be bound to a token, which is done with tick next to “Lock to token offsets” in “Bahaviour”.

To create the tagset, go to Tagset and first click on “Create Tagset”, then fill in the fields as shown in the image below in the “Tagset details” frame.

[tut7.JPG]

After doing so, create the tags as they are shown above in the “Tags” frame. The descriptions of the six tags were taken from the guidelines mentioned above.

[propbank.JPG], Guidelines, p. 3

Having created the tagset, go back to “Layers” and choose the “VerbSpan” layer. Then click on “New feature” in the “Feature overview” frame and fill it like shown in the image below:

[tut8.JPG]

Now the relation layer shall be created. For this, fill the “Properties” layer like shown below after clicking on “Create Layer”.

[tut9.JPG]

Now in “Technical Properties” the “Relation” type is chosen and the layer is attached to the “VerbSpan” layer that was just created. Do not forget to save the the layer.





Morphological Feature Annotation

In this layer, morphological features of the German langue will be made possible. This layer is similar to the annotations that were used in the TIGER project (see [Crysmann et al.](http://www.ims.uni-stuttgart.de/forschung/ressourcen/korpora/TIGERCorpus/annotation/tiger_scheme-morph.pdf)).

In German, nouns and articles can be changed according to gender, number and case; verbs according to person, number, tense and mode; and adjectives and adverbs according to degree. These parts of speech will be annotated in separate layers.

The  “Noun” layer creation will be shown in detail, the other layers can be be made accordingly. A table which shows the morphological features to produce the layers is provided below.

Part of Speech

Noun

Verb

Adjectives/Adverbs

Changed Category

Gender: Feminine, Masculine,

Neuter

Person: 1st, 2nd, 3rd

Degree: Regular, Comparative, Superlative

Changed Category

Number: Singular, Plural

Number: Singular, Plural



Changed Category

Case: Nominative, Genitive, Dative, Accusative

Tense: Future, Present , Past(there are more specific tenses in the German laguage; these are just for presentation  purposes)



Changed Category



Mode: Indicative, Imperative, Conjunctive





To create the noun layer, first click on “Create Layer” in the “Layers” tab set. Then fill in the frames as shown in the following image.

[tut10.JPG]

Then click on “Save layer” and go to Tagsets tabset. There, click on “Create tagset”, then fill in the fields as shown in the right frame in the image below.

[tut11.JPG]

Then click on “Save tagset” and choose the “Gender” tagset in the “Tagsets” frame. Then create the tags that are shown in the bottom of the image. Click on “Create tag”, enter the name, then click “Save tag”.

Now create a new tagset, in the same way as shown before, for “Number”. The result of having created “Number” and the tags “Singular” and “Plural” are shown below.

[tut12.JPG]

Now create the the tagset for “Case”. The results of doing so are shown below.

[tut14.JPG]

After doing so, go back to “Layers”, choose the “Nouns” layer and add the tagsets. For this, click on “New feature” in the “Feature overview” frame. The filled fields for “Gender” are shown below. The other tagsets, “Number” and “Case”, are added in the same way.

[tut13.JPG]



OpinionAnnotation

For this kind of annotation, again two layers will be created. One layer will annotate spans indicating the opinion target and the words referencing it. The other layer will combine those with a positive or negative polarity,

To create the first layer, first select the “Layers” and click “Create layer”, then fill in the fields like shown in the image below.

[tut15.JPG]

Multiple tokens may be annotated with one tag, as words like “very” or “much”, but also “not” may change or amplify the meaning of the categorizing words.

Now go to the tab set “Tagsets”, click on “Create tagset”and fill the fields as shown in the right upper frame in the image below. Then add the tags shown in the left lower frame in the image by choosing “OpinionComponents”, clicking on “Create tag, filling in the name and clicking “Save tag”.

[tut16.JPG]

Then go back to “Layers”, choose “OpinionComponents”, click on “New feature” and fill the fields in the newly appeared frame as shown below:

[tut17.JPG]

Now create the layer indicating the polarity of the references. Create a new layer, filling the fields as shown below:

[tut18.JPG]

Here crossing boundaries of sentences may be crossed, as the opinion target may be mentioned in other sentences than categorizing words.

Now go to the “Tagsets” and create the tagset with the tags “positive” and “negative” as shown in the image below.

[tut19.JPG]

Afterwards go back to “Layers”, click “New feature” in the right frame and fill the new frame as shown below:

[tut20.JPG]



Metainformation annotation

In this part it will be shown how to produce a layer in which several free annotations may be added to several tokens, as for example needed when annotating facts about persons.

Go to the tab set “Layers” and fill it like shown below:

[tut21.JPG]

Afterwards click on “New feature” and fill the fields as shown in the image. Notice that no tagset is added, as a layer like this allows free annotation tags.

[tut22.JPG]