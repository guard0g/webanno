We assume you have MySQL server installed and running.
After installation, MySQL server can be started by running the "mysqld" command, located in the binary directory of mysql, e.g.:

```
$ cd /usr/local/mysql-5.1.71-osx10.6-x86_64/bin/
$ ./mysqld &
```

Log into mysql as the root user in a terminal (if no password is set, omit -p):
```
$ mysql -u root -p
```

Create a database (here: webanno as the db name) that WebAnno can use (encoding UTF-8).
```
mysql> CREATE DATABASE `webanno` /*!40100 DEFAULT CHARACTER SET utf8 */;
```


Create a user (here: "webanno") that WebAnno can use. Make sure to add full permissions on the created database. If MySql and WebAnno run on different servers, replace 'localhost' with the server's address.

```
grant all on webanno.* to 'webanno'@'localhost' identified by "t0t4llYSecreT";
```

Create WebAnno home directory, e.g. `/Users/JohnDoe/Library/WebAnno`

Download Tomcat and extract somewhere

Edit `bin/catalina.sh` and add the following line after the comment section at the beginning of the file

```
 export JAVA_OPTS="-Djava.awt.headless=true -Xmx750m -XX:+UseConcMarkSweepGC -Dwebanno.home=/Users/JohnDoe/Library/WebAnno"
```

Create a simple text file `/Users/JohnDoe/Library/WebAnno/settings.properties`(change the database user, password, and URL to match your system)

```
database.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
database.driver=com.mysql.jdbc.Driver
database.url=jdbc:mysql://localhost:3306/webanno
database.username=webanno
database.password=t0t4llYSecreT
```


<a href='Hidden comment:  THIS IS NO LONGER REQUIRED - WebAnno comes with a default file
Create another simple text file /Users/JohnDoe/Library/WebAnno/formats.properties

```
text.label=Plain text
text.reader=de.tudarmstadt.ukp.dkpro.core.io.text.TextReader
text.writer=de.tudarmstadt.ukp.dkpro.core.io.text.TextWriter

tcf.label=Weblicht TCF Format
tcf.reader=de.tudarmstadt.ukp.clarin.webanno.tcf.TcfReader
tcf.writer=de.tudarmstadt.ukp.clarin.webanno.tcf.TcfWriter

xmi.label=XMI format
xmi.reader=de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader
xmi.writer=de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter

tsv.label=TSV Format
tsv.reader=de.tudarmstadt.ukp.clarin.webanno.tsv.WebannoTsvReader
tsv.writer=de.tudarmstadt.ukp.clarin.webanno.tsv.WebannoTsvWriter
```
'></a>


Fetch the WAR and place it into the webapps folder of your tomcat. Rename it to "webanno.war"

Start Tomcat using `bin/startup.sh` - to stop tomcat, there is a `bin/shutdown.sh` script, e.g.

```
$ cd /Applications/apache-tomcat-7.0.42/bin
$ sh startup.sh
```

That should be it: `http://localhost:8080/webanno`


You can export the project from the **Projects Settings** page once logged in (and if you have admin privilege).
  * Click **Projects** in the **Welcome to WebAnno** Page
  * Select a project you are going to export in the **Projects Settings** page
  * Go to the **Export/Import** tab
  * Click the **Export the whole project into the File System** button.

Please make sure that you have turnned of **"Automatically open safe file types"** in Safari to avoid unpacking the exported project on download.

You can import exported project from the **Projects Settings** page.
  * Below the lists of projects, choose the exported webanno project (ZIP file)
  * Click **Import Project** button

We recommend using WebKit-based browsers, such as Safari or Chrome.

You can start with the SampleProjects to explore some of the functionalities.