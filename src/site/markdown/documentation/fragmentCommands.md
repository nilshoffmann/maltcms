# Available Fragment Commands     

To get a list of available commands, you can query maltcms directly:

    >maltcms.sh -l cross.commands.fragments.AFragmentCommand 

To find out about the parameters supported by an individual command:

    >maltcms.sh -s maltcms.commands.fragments.peakfinding.TICPeakFinder

If you want to generate a basic configuration file for a number of commands:


    >maltcms.sh -b maltcms.commands.fragments.peakfinding.TICPeakFinder


You can provide multiple commands by separating them with a ','. The created
pipeline will be in the 'maltcmsOutput/pipelines/' directory below the current
working directory. The created files are just a starting point and will most probably 
not work immediately. For hints on completing the configuration files, please 
consult the provided pipelines below 'cfg/pipelines/' and 'cfg/pipelines/xml'.

If you have further questions, please feel free to contact [the author](http://maltcms.sourceforge.net/info/contact.html).
Currently, the following fragment commands are available for execution 
within the pipeline:

[List of Fragment Commands](./commands/index.html)
	
