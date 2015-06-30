

# Login does not work upon setup #

### Problem ###

If you use old war files such as **webanno-0.3.0-rc-4.war**, you will encounter the **log-in failed** message with any username/password combination.
The exception at catalina.out resembles the following
```
INFO: Starting Coyote HTTP/1.1 on http-18080
Oct 2, 2013 3:47:06 PM org.apache.catalina.startup.Catalina start
INFO: Server startup in 4068 ms
2013-10-02 15:47:52  INFO [http-18080-1] (PropertiesFactory) - Loading properties files from file:/opt/webanno/webapps/webanno/WEB-INF/classes/de/tudarmstadt/ukp/clarin/webanno/webapp/page/login/LoginPage.properties with loader org.apache.wicket.resource.IsoPropertiesFilePropertiesLoader@54135640
2013-10-02 15:48:01  WARN [http-18080-2] (SpringAuthenticatedWebSession) - User 'admin' failed to login. Reason: No entity found for query
2013-10-02 15:48:01 ERROR [http-18080-2] (LoginForm) - Login failed
2013-10-02 15:48:01 ERROR [http-18080-2] (ApplicationPageBase$1) - anonymousUser: Login failed
2013-10-02 15:49:39  WARN [http-18080-1] (SpringAuthenticatedWebSession) - User 'admin' failed to login. Reason: No entity found for query
2013-10-02 15:49:39 ERROR [http-18080-1] (LoginForm) - Login failed
2013-10-02 15:49:39 ERROR [http-18080-1] (ApplicationPageBase$1) - anonymousUser: Login failed
```

### Solution ###

When following the installation instruction, use the latest war file by compiling webanno from source code or ask the developer for the latest war file. Otherwise, follow the [upgradeprocedure](upgradeprocedure.md)


# I lost my database #

### Problem ###

You accidentally lost/deleted your database but you still have the WebAnno home directory on the file system.

### Solution ###

You can partially restored the database using the data on the file system, but it will take quite some work. We'll assume that the full database was lost.

**Set up database schema**
  * Re-create a fresh, empty database without any tables.
  * Start WebAnno once to have it initialize the database schema
  * Open the login screen to create the default _admin_ user

**Restore projects**
  * you find one folder per project in the **repository** folder of your WebAnno home. There are also the project logs files, e.g. **project-58.log**
  * The first line in a project log looks like this: `2013-11-09 23:10:22,453 [admin]  Created  Project [lala] with ID [75]`
  * With this information you can manually add a line to the **project** table in the database
| id | description | name | reverseDependencyDirection | mode |
|:---|:------------|:-----|:---------------------------|:------|
| 75 |             | lala | 0                          | annotation |
  * You need to do this for every project which still has a folder. There may be additional log files for project that have already been deleted. Those you can just ignore.

**Restore source documents**
  * In each project folder, there is a folder called **document** which contains one subfolder for each source document, named after the source document id.
  * Scan the project log of the project for lines like this: `Imported file [text1.tcf] with ID [8513] to project [75]`
  * With this information, you can manually add lines to the **source\_document** table in the database
| id | name | project | format | state |
|:---|:-----|:--------|:-------|:------|
| 8513 | text1.tcf | 75      | tcf    | NEW   |
  * Repeat this for every document which still has a folder. There may be additionally _Imported file_ lines in the log for documents that have already been deleted.

**Restore tag set**
  * Scan the project log of the project for lines like this: `Added tagset  [STTS] with ID [404]`
  * Scan the project log of the project for lines like this: `Added tag [$(] with ID [12366] to tagset [STTS]`
  * The IDs of the tag sets and tags are not relevant. But by these lines, you know the names of the tag sets and the names of the tags in each tag set, which you can use to manually add back these tag sets and tags via the project settings web interface.

**Restore permissions**
  * Add all missing users back via the user management web interface
  * Scan the project log of the project for lines like this: `New permission created on project [lala] for user [admin] with permission [ADMIN]]`
  * Use this information to add users back to the projects via the project management web interface

**Restore project assignments**
  * Go to the monitoring web interface and re-assign documents to users as you desire. This information is currently not recoverable from the project log
  * Mind that also the annotation state is lost, all documents will appear as **new** for every user. However, since the annotations themselves are saved on the file system, not in the database, they are still there.

# safari automatically extract WebAnno Project #
### Problem ###
When exporting the whole Webnno project, my browser automatically extract it. This hinders Webanno to properly import the exported project if the extracted files are re-zipped

### solution ###
make sure that you have turnned of "Automatically open safe file types" in Safari to avoid unpacking the exported project on download.