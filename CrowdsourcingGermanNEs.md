This tutorial shows how you can use the experimental CrowdSource feature to crowdsource German named entity recognition (NER). This is only a demo for the Crowdsourcing feature in WebAnno. If another task than that of German NER shall be completed, code changes have to be made.

_We also offer a step-by-step tutorial, guiding you through the process of creating a new project and getting a document in it be annotated by the crowd. The instruction for this tutorial are written in italics beneath the corresponding chapters._

# Crowdsourcing NEs #

The task of NER has been split up into two tasks in this demo crowdsourcing task. This way it is less complex to present the problem to the crowd and the quality of the annotation is assured. The first task is that of Named Entity Identification, which means that the users only have to recognize the NEs. In the second task the entities, that have been recognized are classified into the following classes: LOC (location), PER (person), ORG (organisation), OTH (all other name classes) and their derivations and parts of them in phrases containing NEs.
Please note, that nested NEs are not handled by this crowdsourcing task and may lead to problems in association with the Gold standart, which does contain nested NEs.


# Prerequisites #

WebAnno now has experimental support to crowdsource German named entities with http://crowdflower.com.

You need to get an API key, which you can generate on the "Your Account" page in your Crowdflower account.
For this, you have to login with your crededtials in [Login Window](https://id.crowdflower.com/session/new?redirect_url=https%3A%2F%2Fcrowdflower.com%2Faccount&app=make). After logging proceed to your account by clicking on the down arrow next to your login name and selecting "Account".

<img src='https://webanno.googlecode.com/svn/wiki/images/your_account.jpg' width='200' />

Another prerequisite is that the account that is currently logged in into your WebAnno instance has admin capabilities.

## Creating a crowdsource project ##

Before a new Crowdsource task can be created, one has to add the user "crowd\_user" to the project one wants to use for Crowdsourcing.

<a href='Hidden comment: 
Make link to how create project and add users
'></a>
Furthermore, the document which is to be used as 'gold document' has to be marked as curated.
<a href='Hidden comment: 
Make link to how to mark documents as curated
'></a>

_For this tutorial, download the following file._
http://code.google.com/p/webanno/downloads/detail?name=webanno-project3152338732199485518export.zip&can=2&q=#makechanges
<a href='Hidden comment: 
Put project file here
'></a>

_It contains three documents, two of which will be used for the gold data and one of which has to be annotated by the crowd. After downloading, load the file in WebAnno, by choosing **Projects** on the overview screen. On the **Projects** page, click on "Choose file" in "Import project" frame. It is below the "Project" frame on the left. After choosing the file, click on import project. Now you are able to set the project "CrowdTut\_ProjectR2" up. To add the "crowd\_user", click on the "Users" tab._

<img src='https://webanno.googlecode.com/svn/wiki/images/users_tab.JPG' width='400' />

_Then click on "Add Users", select the "crowd\_user" and click on "Add" in the lower right corner._

_Now you should be able to see the following after selecting the project on the **Monitoring** page. To go to the **Monitoring** page, return to the overview screen by clicking on "Home" in the left upper corner. To select the project, click on "CrowdTut\_ProjectR2" in the "Projects" frame on the left._

<img src='https://webanno.googlecode.com/svn/wiki/images/project_before.JPG' width='400' />

# Creating a new Crowdsource task #
After having added the "crowd\_user" to the project you want to use for your Crowdsource task, you should be able to select this project, when you access the 'CrowdSource' panel.
After selecting the project, click on 'New task' to create a new task. Specify a name and the API key that you want to use with this Crowdtask and hit save. You have to enter both parameters (name and API) before saving, otherwise the task cannot be created.

You can add documents you uploaded in your chosen project in order to have them annotated by the crowd in the corresponding frames.
<a href='Hidden comment: 
Make link to add documents to project
'></a>

_The next step in the step-by-step tutorial is explained above. To access the 'CrowdSource' panel, return to the overview screen(by clicking on "Home") and select it._

## Selecting documents ##

Gold documents are curated documents (only available to select if you have curated and marked them as such). They should be chosen so that the contained sentences and NEs reliably help to distinguish bad and good annotators. This is done automatically on the crowd platform.

Gold documents are documents which have been annotated perfectly according to the guidelines and are therefore considered as the correct result or norm.

With 'add gold', you can add one ore more curated documents that should be used as gold data for your task. Removal of documents and number of sentences that will be used for the judgement of the annotation can be done with the same approach as with unannotated documents.

If you press add, you can add one or more documents that should be annotated. With the button remove you may also delete documents you have chosen before. Next to the "Remove" and "Add" button the sum of all sentences of the chosen documents is displayed. In the input window you may specify the number of sentences you want to have annotated in the crowdsourcing.

<img src='https://webanno.googlecode.com/svn/wiki/images/choose_docs.JPG' width='400' />

_For the tutorial, please follow the instruction above. The file, which will be annotated by the crowd is "NER\_deu\_block0K2-ai.tcf". Actually, it is the only one that will be offered to you to be added. In this task, we will have all 200 sentences annotated. As gold data, please select the two following two files (which will also be the only two offered to you): "NER\_deu\_block0K2-ag.tcf" and "NER\_deu\_block0K2-ah.tcf"._

# Creating tasks #

## Uploading task1 ##
Hit 'Upload task1 (span detection)' to create a new job on Crowdflower. After some time - it can take a while if you have many sentences, leave your browser window open - a new link should appear under 'Link for task1 job'.
Click on the link and log into Crowdflower.

## Setting up a task ##
One important step is to convert uploaded gold, otherwise Crowdflower won't use your gold data. Click on 'data' in the left menu, then 'Manage data' and then 'Convert uploaded gold'.

<img src='https://webanno.googlecode.com/svn/wiki/images/convert_uploaded_test.jpg' width='800' />

A popup should appear that informs you that your gold data is updated in the background. After a while, click on 'Monitor' in the menu to confirm that the right amount of gold data has been created.


Before choosing the choosing the channels, you may also specify the country, skills and behavior settings, e.g. the maximum number of judgements per contributor.
You need to choose channels in which you crowd job is going to appear. Click on 'Contributors' and then 'Channels'. You can enable as many channels as you like by pulling the trigger to "on" in the corresponding line.

<img src='https://webanno.googlecode.com/svn/wiki/images/channel.jpg' width='800' />


However, using the right channels is crucial to get good quality. Some channels are also notorious for a bigger number of cheaters.
We've had good results with: Crowdguru, EntropiaPartners (Second Life), Embee, Coinworker, Mturk (however Mturk has a very limited availability of German annotators).

Only EntropiaPartners (Second Life) and Mturk remain available to 'Basic Users' since October 2013. Unfortunately, esp. Crowdguru is a good channel for German annotation, since it's the only German-only one and has strict sign up tests in place to ensure quality.

Before finally ordering your job, you can specify the number of sentences and payment per sheet, as well as the task expiration time in "Job settings".

<img src='https://webanno.googlecode.com/svn/wiki/images/job_settings.jpg' width='800' />

Quizmode can be enabled for your task, too. TODO: explain quiz mode



## Ordering your job ##

You should now be ready to order your job. You can do that by clicking on "Launch". The Job settings and the total job costs will be displayed to you after that.

<img src='https://webanno.googlecode.com/svn/wiki/images/checkout.jpg' width='800' />

To order the job, click on "Proceed to Checkout" and then on "Launch Job Now!"

After you've done that, monitor the progress of your job. You should see first judgments after some minutes. If something goes wrong, you will see a very high percentage of untrusted judgments. You can abort the job and get a refund in this case. (You need to reupload the job then.)

It often happens that your job gets stuck at 99.x%. This is usually a last block of your data that gets paired with an unusual tricky gold question. After some time, some one might get the answer right, however people are now generally discouraged to work on your task, since the time it takes to read the instructions not worth the effort for the little potential reward remaining.

You can use the internal interface to finish the job on your own, or you skip directly to the next section with an incomplete job. WebAnno will see that some judgments are missing and only works on sentences where the span detection is 'finalized'.

## Uploading  task2 ##

Never change the associated documents after you've started crowdsourcing! If you're happy with the results of the span detection you can start a new job on Crowdflower.com which identifies the types of these spans. Only spans which have been identified by the crowd in the previous step are used to build new data for this new crowdsourcing job.

When you click the button 'Upload task 2 (type recognition)' the first time you might get an error message that the data isn't ready on Crowdflower. This is normal and expected for bigger uploads: Crowdflower needs more time to prepare your data. Wait 1-2 Minutes and rehit the button, it should work then. If not, try to update the status of the first task and try to upload task two again.

The remaining procedure is the same as previously described.

# Retrieving data #

Once you are happy with the progress you can import your data into WebAnno. You can also import a job which isn't finished (e.g. one that is stuck at 99%) and reimport later should it finish.
To import the annotated data, click on "Retrieve T2 judgements", which is on the bottom of the "Crowd Job Details" frame.

<img src='https://webanno.googlecode.com/svn/wiki/images/extract_anno.JPG' width='400' />

To see the annotation on the Curation Page, you have to mark the document (not the gold document) as annotated for the "crowd\_user" in the Monitoring Page.
<a href='Hidden comment: 
Make link to how to mark documents as curated
'></a>
