# Installation guide to develop WebAnno in Eclipse #

This is a guide to setting up a developer environment in Eclipse for WebAnno using Max OS X. The procedure should be similar for other operation systems.

First, you need to follow some steps of the user [installation guide](InstallationGuide.md). You need to configure your MySQL-server for WebAnno. After that, jump right to the chapter WebAnno and follow all steps besides the first one until the end of the document.

We recommend you start from a Eclipse Classic distribution.

# Eclipse plugins #

  * **Version Control:** Use Subclipse 1.8.x, see: `http://subclipse.tigris.org/update_1.8.x` <br />_Please do not use Subversive. If you did not start with an Eclipse Classic, you may end up with Subversive and Subclipse installed, which can easily confuse you as well as Eclipse._

  * **Maven Integration:** m2e , is included in Eclipse Classic. Use **Help->Install New Software, select "--All available sites--" and choose Collaboration -> m2e - Maven Integration for Eclipse**

  * **Subclipse/Maven Integration:** Update site: `http://subclipse.tigris.org/m2eclipse/1.0/`

  * **Apache UIMA tools:** Update site: `http://www.apache.org/dist/uima/eclipse-update-site/`

  * You should check that Text file encoding is UTF-8  in "Preferences -> General -> Workspace" of your eclipse install.

# WebAnno and tomcat installation #

Checkout out the svn repository `https://webanno.googlecode.com/svn/trunk` and **checkout the project as Maven project**: ![http://webanno.googlecode.com/svn/wiki/images/checkout_as_maven_project.png](http://webanno.googlecode.com/svn/wiki/images/checkout_as_maven_project.png)

Download Apache Tomcat from `http://tomcat.apache.org/` (we're using version 7). Then, you need to add the Tomcat server to your runtime configuration. Go to preferences and go to **Servers -> Runtime environments**:
![http://webanno.googlecode.com/svn/wiki/images/AddApacheTomcat.png](http://webanno.googlecode.com/svn/wiki/images/AddApacheTomcat.png)

When prompted for an installation path, specify the folder where you extracted (or installed) Apache Tomcat v7 into.

Change the runtime configuartion for the project. On the left side of the dialog, you should now be able to select Apache Tomcat. Change its VM arguments and include the definition `-Dwebanno.home="/srv/webanno"` to specifiy the home directory for WebAnno:

![http://webanno.googlecode.com/svn/wiki/images/ChangeRunConfiguration.png](http://webanno.googlecode.com/svn/wiki/images/ChangeRunConfiguration.png)

Head to the servers pane. If you cannot locate it in your eclipse window, add it by going to **Window -> Show View -> Other...** and select **Servers**. Right click on **Tomcat v7 localhost** and click on **Add and remove...**:

![http://webanno.googlecode.com/svn/wiki/images/AddAndRemoveServer.png](http://webanno.googlecode.com/svn/wiki/images/AddAndRemoveServer.png)

You should end up with:

![http://webanno.googlecode.com/svn/wiki/images/AddAndRemoveServerFinal.png](http://webanno.googlecode.com/svn/wiki/images/AddAndRemoveServerFinal.png)

WebAnno should now be configured to start with tomcat.

# Troubleshooting #

If you run into problems with the last step (Add and remove...) and get the error 'There are no resources that can be added or removed from the server', checkout if you have installed _m2eclipse-wtp_:

![http://webanno.googlecode.com/svn/wiki/images/Problems%20with%20no%20resource%20available.png](http://webanno.googlecode.com/svn/wiki/images/Problems%20with%20no%20resource%20available.png)

and go to the project settings and check if these project facets are activated for the project. If you have the _m2eclipse-wtp_ installed, it should be sufficient to right-click on the project and do a  **Maven -> Update project** to reconfigure the project and have m2e update these settings:

![http://webanno.googlecode.com/svn/wiki/images/ProjectsFacets.png](http://webanno.googlecode.com/svn/wiki/images/ProjectsFacets.png)