## Developing with Maltcms

### Prerequisites for Developing with Maltcms

- [Git](http://git-scm.com) (for source code access)
- [Java JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven 3](http://maven.apache.org/download.html)
- Any IDE supporting maven 2/3 integration and subversion support
    - [NetBeans 7.+](http://netbeans.org/)
    - [Eclipse 3.+ (Indigo) or 4.+](http://eclipse.org/)
    (requires additional m2e plugin and 
     git plugins)
    - [IntelliJ Idea 10.5](http://www.jetbrains.com/idea/)


### Building Maltcms from Source 

The sourcecode of Maltcms is under control of the distributed version control system [Git](http://git-scm.com/).
In order to access the latest source, you need to clone the repository to your local system:

    
    git clone git://git.code.sf.net/p/maltcms/code maltcms-code
    

Other software for working with Git should work similarly. 

After checking out, you will find the root maven module maltcms below maltcms-code. 
Run

    
    mvn install
    

from the command line to build the complete project, to run the corresponding unit tests, and to install 
the generated artifacts into your local maven repository.

### Using Maltcms as a Library

The modules of Maltcms are published as maven artifacts, hosted 
by a custom [Artifactory](http://www.jfrog.com/home/v_artifactory_opensource_overview) instance
at [Maltcms Artifactory](http://maltcms.de/artifactory).

To add the Maltcms Artifactory to your JAVA project, please follow [these instructions for your 
specific build system](http://maltcms.de/artifactory/webapp/mavensettings.html).

For Maven projects, you should add the following repository definition below the _repositories_ tag 
in your _pom.xml_

    <repositories>
        ...
        <repository>
            <id>maltcms-artifactory-release</id>
            <layout>default</layout>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>daily</updatePolicy>
            </snapshots>
            <url>http://maltcms.de/artifactory/repo</url>
        </repository>
    </repositories>

Please note that you can only retrieve artifacts from this repository without authentication that have 
already been deployed. You can not use the artifactory as a proxy to other repositories.

In order to use the Maltcms artifacts in your code, include them in the dependencies section of your pom (a):

    <dependencies>
        <dependency>
            <groupId>net.sf.maltcms</groupId>
            <artifactId>maltcms-distribution</artifactId>
            <version>1.3.1</version>
            <type>pom</type>
        </dependency>
    </dependencies>

or in the dependency management section of your pom if you want to specify the version explicitly in a multi-module project parent pom (b):

    <dependencyManagement>
        <dependency>
            <groupId>net.sf.maltcms</groupId>
            <artifactId>cross-main</artifactId>
            <version>1.3.1</version>
        </dependency>
        </dependencies>
    </dependencyManagement>

With the latter approach, you still need to define the dependencies in your module's pom like in variant (a), but can omit the ``<version>...</version>`` tags.

#### Profiles

Maltcms defines two separate profiles in order to keep unnecessary build goals out of the default build cycle. You can activate these profiles explicitly on the command 
line using ``mvn -P src,javadoc``. This will activate the generation and attachment of source and javadoc jar artifacts. The third profile is automatically activated during 
the build in order to generate complete sourcecode for those classes that use the [Lombok](http://projectlombok.org) annotations library.

<h3>Further Reading</h3>
The following pages give a more detailed overview of different aspects when developing 
with Maltcms.

- [Maven](./maven.html)
- [Data Access](./dataAccess.html)
- [Parallel Processing](./parallelProcessing.html)
- [Custom Workflow Elements](./customWorkflowElements.html)
