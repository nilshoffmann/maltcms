<h2>Maven</h2>
If you plan to use Maltcms within your own projects, you can do so in many different ways. For 
Ant-based projects, download the Maltcms binary distribution and add all jar files within the 
lib/ folder to your compilation classpath. If you use Maven, Gradle, or Ivy for project and dependency
management, the following instructions are rather similar. 

<h3>Setting up the Maven project</h3>

Create a new Maven project using your IDE of choice. The packaging type should be jar, if you plan to create 
an extension of Maltcms. 

Call 

	mvn install

to compile your project and to create the jar artifact (below target/ folder).
If you want to make your plugin available to Maltcms, add it to the extension classpath:

	maltcms.sh -e file:///path/to/your.jar ...

Maltcms uses a custom URL ClassLoader to load the contents of your jar. If you use external dependencies other than 
those used by Maltcms, you need to specify them on the extension classpath as well.

<h3>Maltcms Artifactory</h3>

<h3>Build profiles</h3>
Build profiles in maven are activated on the commandline with `-P profile1,profile2`. Maltcms currently supports a small number of profiles. In order to build src jars for each artifact, the `src` profile needs to be activated. If you want to build the javadoc jar files for each artifact, activate the `javadoc` profile. Further profiles exist to only create certain project artifacts, like the OSGI and NetBeans module distributions, if they are really needed. These profiles can be activated with `osgi` and `nbm`, respectively.

