# Metabolite Database

**Please note that the underlying database engine db4o has been discontinued. The following code may not work at this point!**

For a quick introduction into building and running Maltcms, see [Getting Started](../gettingStarted.md).

## Howto run MetaboliteBrowser

From the directory containing this file call

    java -Xmx256M -cp maltcms.jar net.sf.maltcms.apps.MetaboliteBrowser

or alternatively call


    maltcms.sh -exec net.sf.maltcms.apps.MetaboliteBrowser

A popup dialog will ask you for a database location, username
and password. The database location can be either a local file
(e.g. C:\path\to\my\database.db4o) or a url pointing to DB4oServer 
instance (e.g. file:///localhost:1234, if 1234 is the port on 	
which the DB4oServer is listening).
	
## Howto run DB4oServer

From the directory containing this file call

    java -Xmx256M -cp maltcms.jar maltcms.db.connection.DB4oServer DATABASEFILE PORTNUMBER

or alternatively call

    maltcms.sh -exec maltcms.db.connection.DB4oServer DATABASEFILE PORTNUMBER

## Howto build a new Database from MSP files

From the directory containing this file call

    java -Xmx256M -cp maltcms.jar maltcms.db.MSPFormatMetaboliteParser nameOfDBFileToCreate.db4o [nameOfFirstMSPFileToParse,nameOfSecondMSPFileToParse,...]

or alternatively call

    maltcms.sh -exec gmdb.MetaboliteParser nameOfDBFileToCreate.db4o [nameOfFirstMSPFileToParse,nameOfSecondMSPFileToParse,...] 

This will parse the supplied MSP files and convert them to IMetabolite instances, which are then 
saved in the database file 'nameOfDBFileToCreate'. You can then fire up MetaboliteBrowser
to browse the Metabolites. We recommend to append the suffix ".db4o" to
the database files in order to distinguish them more easily from
ordinary files.

In case of errors, please check that the supplied msp file contains only numbers in US-English locale, e.g. with a ',' as a decimal separator. 
	
## For Developers

The Datatype used for storage and retrieval must implement the interface IMetabolite
from maltcms-datastructures-api.jar. Detection of available members, via
the appropriate Getter/Setter-Methods is based on the Interface definition. Of course,
concrete Implementations can extend the existing interface, but then, the automatic detection
needs to be extended as well.

A query on a database containing ~6000 Metabolites [Golm Metabolite Database](https://gmd.mpimp-golm.mpg.de/) returns in less than 5 seconds on a fairly recent MacBookPro with a 2.66 GhZ Intel Core2 Duo CPU. 

