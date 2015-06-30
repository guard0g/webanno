# Introduction #

For quick test, you can download the war file ( 	de.tudarmstadt.ukp.clarin.webanno.webapp-0.2.0.war) and the sample Zip file (webanno-stable.zip)


# Details #
For qucik test, you can download the war file [de.tudarmstadt.ukp.clarin.webanno.webapp-0.2.0.war] and the sample Zip file [webanno-stable.zip] from Downloads page which contains a simple instance of the application, both the database dumb and the source and annotation documents.
# How to install #
  1. Download the war and zip files from Downloads page
  1. Put the war file inside your web servers webapps directory, as indicated in the InstallationGuide
  1. Unzip the sample data and
    * copy the content of  webanno-sample folder in the /srv/webanno/ as indicated in the InstallationGuide
    * create a database as indicated in the InstallationGuide
```
mysql> CREATE DATABASE webanno; 
```
    * Then copy the content of the database dump from webanno-stable.sql to webanno as follows
```
mysql -u root -p webanno < webanno-stable.sql
```