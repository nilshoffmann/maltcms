## Workflow Configuration
Before continuing, please check that [Maltcms is correctly installed and working](../gettingStarted.md)!
You may also like to [create your own custom pipeline](../documentation/fragmentCommands.md).

We will use the pipeline definition at `cfg/pipelines/xml/bipace.xml` for the following examples.

### Using the default pipeline
If you want to use the default pipeline, which does not allow for restartability, make sure the the 
bean with `id="commandPipeline"` has `class="cross.datastructures.pipeline.CommandPipeline"` set.

The corresponding part of `bipace.xml` then looks like this:

    <!-- a command pipeline consists of a list of 
    commands to be executed -->
    <bean id="commandPipeline" class="cross.datastructures.pipeline.CommandPipeline">
        <property name="checkCommandDependencies" value="true"/>
        <property name="commands">
            <list>
                <ref bean="csvAnchorReader" />
                <ref bean="defaultVarLoader" />
                <ref bean="massFilter" />
                <ref bean="denseArrayProducer" />
                <ref bean="peakCliqueAlignment" />
            </list>
        </property>    
    </bean>

### Using a result aware pipeline
The new result aware command pipeline has a number of benefits over the standard pipeline.
It 

- calculates a joint checksum of all input files,
- calculates a joint checksum of all files produced by each fragment command,
- calculates a joint hashcode/checksum of all parameters of each fragment command.

Therefore, the result aware command pipeline automatically detects changed parameters and 
recalculates only those parts of the pipeline that are affected by them, and all commands that 
follow downstream are updated as well. 

<p class="alert alert-warn">
<b>Caveat:</b><br/>
The updated results are placed in the same output location as the original results,
thereby overwriting/updating existing results.
</p>

The corresponding part of `bipace.xml` then looks like this:

    <!-- a command pipeline consists of a list of 
    commands to be executed -->
    <bean id="commandPipeline" class="cross.datastructures.pipeline.ResultAwareCommandPipeline">
        <property name="checkCommandDependencies" value="true"/>
        <property name="commands">
            <list>
                <ref bean="csvAnchorReader" />
                <ref bean="defaultVarLoader" />
                <ref bean="massFilter" />
                <ref bean="denseArrayProducer" />
                <ref bean="peakCliqueAlignment" />
            </list>
        </property>    
    </bean>

