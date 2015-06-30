This guide summarizes the functionality of WebAnno from the user's perspective. It is constantly updated to match the latest verson of WebAnno.


Currently, this guide describes the functionality of WebAnno Version 2.0.0-beta-5. Some of the described functionalities are under development and will therefore be accessible soon.

<h2>Table of Contents</h2>



# Introduction #
This document serves as a quick user’s guide for the operation of the web-based annotation platform WebAnno, and is targeted towards users of the platform, including project managers, curators and annotators.

It is assumed that WebAnno has been installed successfully. For issues regarding the installation, please see http://code.google.com/p/webanno/wiki/InstallationGuide.

All materials, including this guide, is available via the project site: https://code.google.com/p/webanno/

# Compatibility #

WebAnno is compatible with google chrome or safari browsers, sepecifically for annotation and visualization. The remaining component of Webanno, such as project settings and monitoring page should run in all brosers.

# Description of GUI functionality #

## Log-in screen ##

Upon navigating to the WebAnno page, the login screen opens. Please enter your credentials to proceed.

<img src='https://webanno.googlecode.com/svn/wiki/images/login.jpg' width='400' />


## Overview screen ##

After login, you will be presented with the WebAnno overview screen. This screen can be reached at any time from within the GUI by clicking on the **Home** link in the left upper corner.


Here, you can navigate to one of the currently eight options:

  * **[Annotation](https://code.google.com/p/webanno/wiki/Annotation)** - The page to perform annotations
  * **[Curation](https://code.google.com/p/webanno/wiki/Curation)** - Compare and merge annotations from multiple users (only for _curators_)
  * **[Correction](https://code.google.com/p/webanno/wiki/Correction)** - Correcting automatic annotation (under development)
  * **[Automation](https://code.google.com/p/webanno/wiki/Automation)** - Creating automatically annotated data
  * **[Projects](https://code.google.com/p/webanno/wiki/Projects)** - Set up or change annotation projects (only for _administrators_)
  * **[Monitoring](https://code.google.com/p/webanno/wiki/Monitoring)** - Allows you to see the projects, their progress and change documentstatus (only for _administrators_ and _curators)
  * **[Manage users](https://code.google.com/p/webanno/wiki/Manage_users)** Allows you to manage the rights of users
  * **CrowdSource** - See https://code.google.com/p/webanno/wiki/CrowdsourcingGermanNEs
Please click on the functionality you need. The individual functionalities will be explained in further chapters._

<img src='https://webanno.googlecode.com/svn/wiki/images/overview_screen.jpg' width='100' />


# Workflow #

The following image shows an examplary workflow of an annotation project with WebAnno.

<img src='https://webanno.googlecode.com/svn/wiki/images/progress_workflow.JPG' width='500' />

First, the projects needs to be set up. In more detail, this means that users are to be added, guidelines need to provided, documents have to be uploaded, tagsets need to be defined and uploaded, etc. The process of setting up and administrating a project are explicidly described in **Projects**.
After the setup of a project, the users which were assigned with the task of annotation annotate the documents according to the guidelines. The task of annotation is further explained in **Annotation**. The work of the annotators is managed and controlled by Monitoring. Here, the person in charge has to assign the workload. For example, in order to prevent redundant annotation, documents which are already annotated by several other annotators and need not be annotated by another person, can be blocked for others. The person in charge is also able to follow the progress of individual annotators. All these tasks are demonstrated in **Monitoring** in more detail. The person in charge should not only controll the quantity, but also the quality of annotation by looking closer into the annotations of individual annotators. This can be done by logging in with the credentials of the annotators.
After at least two annotors finished the annotation of the same document by clicking on "Done", the curator can start his work. The curator compares the annotations and corrects them if needed. This task is further explained in **Curation**.
The document merged by the curator can be exported as soon as the curator clicked on "Done" for the document. The extraction of curated documents is also explained in **Projects**.

# Tutorials #
For tuturials on the different functionalities of WebAnno, please see [Tutorials](https://code.google.com/p/webanno/wiki/Tutorials).
Soon also video-tutorials will be available.





# Known Issues and Error reporting #

If the tool is kept open in the browser, but not used for a long period of time, you will have to log in again. For this, press the reload button of your browser.

If the tool does not react for more than 1 minute, please also reload and re-login.

We are collecting error reports to improve the tool. For this, the error must be reproducible: If you find a way how to produce the error, please open an issue and describe it.