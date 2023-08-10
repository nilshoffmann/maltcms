## Developing with Maltcms

### Prerequisites for Developing with Maltcms

- [Git](https://git-scm.com) (for source code access)
- [Java JDK 17](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven 3](https://maven.apache.org/download.html)
- Any IDE supporting maven 3 integration and subversion support
    - [NetBeans 8.+](https://netbeans.org/)
    - [Eclipse 4.+](https://eclipse.org/)
    (requires additional m2e plugin and 
     git plugins)
    - [IntelliJ Idea 10.5](https://www.jetbrains.com/idea/)


### Building Maltcms from Source 

The sourcecode of Maltcms is under control of the distributed version control system [Git](http://git-scm.com/).
In order to access the latest source, you need to clone the repository to your local system:

    
    git clone git@github.com:nilshoffmann/maltcms.git
    

Other software for working with Git should work similarly. 

After checking out, you will find the root maven module maltcms below maltcms-code. 
Run

    
    ./mvnw install
    

from the command line to build the complete project, to run the corresponding unit tests, and to install 
the generated artifacts into your local maven repository.

### Using Maltcms as a Library

The modules of Maltcms are published as maven artifacts, hosted 
by Maven Central.

In order to use the Maltcms artifacts in your code, include them in the dependencies section of your pom (a):

    <dependencies>
        <dependency>
            <groupId>io.github.nilshoffmann</groupId>
            <artifactId>maltcms-distribution</artifactId>
            <version>VERSION</version>
            <type>pom</type>
        </dependency>
    </dependencies>

or in the dependency management section of your pom if you want to specify the version explicitly in a multi-module project parent pom (b):

    <dependencyManagement>
        <dependency>
            <groupId>io.github.nilshoffmann</groupId>
            <artifactId>maltcms-distribution</artifactId>
            <version>VERSION</version>
        </dependency>
        </dependencies>
    </dependencyManagement>

Replace "VERSION" with the release version you want to use.
With the latter approach, you still need to define the dependencies in your module's pom like in variant (a), but can omit the ``<version>...</version>`` tags.

#### Profiles

Maltcms defines two separate profiles in order to keep unnecessary build goals out of the default build cycle. You can activate these profiles explicitly on the command 
line using ``./mvnw -P src,javadoc``. This will activate the generation and attachment of source and javadoc jar artifacts. The third profile is automatically activated during 
the build in order to generate complete sourcecode for those classes that use the [Lombok](https://projectlombok.org) annotations library.

### Further Reading
The following pages give a more detailed overview of different aspects when developing 
with Maltcms.

- [Maven](./maven.md)
- [Custom Workflow Elements](./customWorkflowElements.md)
