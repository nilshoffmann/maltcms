/**
 * 
 */
package maltcms.runtime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.netbeans.api.wizard.WizardResultReceiver;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * Allows direct launching of a Maltcms-Configuration on Sun's Grid Engine.
 *
 */
public class SGELauncher implements Thread.UncaughtExceptionHandler, WizardResultReceiver, Runnable{
	
	enum State{INIT,MONITORING,FINISHED,FAILURE};
	
	private State state ;
	
	private File cfgFile;
	
	private String command;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SessionFactory factory = SessionFactory.getFactory();
		Session session = factory.getSession();
		
		try {
			session.init("");
			JobTemplate jt = session.createJobTemplate();
			jt.setRemoteCommand("sleeper.sh");
			jt.setArgs(Collections.singletonList("5").toArray(new String[]{}));
			String id = session.runJob(jt);
			System.out.println("Your job has been submitted with id " + id);
			session.deleteJobTemplate(jt);
			//int state = session.getJobProgramStatus(id);
			session.exit();
		} catch (DrmaaException e) {
			System.out.println("Error: " + e.getMessage());
		}


	}

	private ScheduledExecutorService stp;
	private Session s;
	private JobTemplate jt;
	private String id;
	private SGEPoller sgp;

	public void setCommand(String s) {
		this.command = s;
	}
	
	/* (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see org.netbeans.api.wizard.WizardResultReceiver#cancelled(java.util.Map)
     */
    @Override
    public void cancelled(Map arg0) {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see org.netbeans.api.wizard.WizardResultReceiver#finished(java.lang.Object)
     */
    @Override
    public void finished(Object arg0) {
	    if(arg0 instanceof PropertiesConfiguration) {
	    	String basedir = (((PropertiesConfiguration)arg0).getString("output.basedir",""));
	    	File f = new File(basedir,"wizard.properties");//jfc.getSelectedFile();
                try {
	                ConfigurationUtils.dump((Configuration)arg0, new PrintWriter(new FileWriter(f)));
                } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
	    	this.cfgFile = (File)arg0;
	    }
	    
    }
    
    private Session initSession() throws DrmaaException{
    	SessionFactory factory = SessionFactory.getFactory();
		Session session = factory.getSession();
		Date date = new Date();

	    SimpleDateFormat sdf = new SimpleDateFormat(
		    "MM-dd-yyyy_HH-mm-ss", Locale.US);
		
		session.init("MALTCMS-"+System.getProperty("user.name")+"-"+sdf.format(date));
		return session;
		
    }
    
    private JobTemplate initJobTemplate(Session s, File cfgFile) throws DrmaaException{
    	JobTemplate jt = s.createJobTemplate();
    	
		jt.setRemoteCommand(this.command+" -c "+cfgFile.getAbsolutePath());
		//jt.setArgs(Collections.singletonList("5").toArray(new String[]{}));
		return jt;
    }

	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
    	int lastState = Session.UNDETERMINED;
    	while(state!=State.FINISHED || state!=State.FAILURE) {
    		
    		switch(state){
	    		case INIT:
		    	{
		    		try {
	                    s = initSession();
	                    jt = initJobTemplate(s,cfgFile);
				    	id = s.runJob(jt);
				    	sgp = new SGEPoller(s,id);
				    	this.stp.scheduleAtFixedRate(sgp, 0, 5, TimeUnit.SECONDS);
						System.out.println("Your job has been submitted with id " + id);
                    } catch (DrmaaException e) {
	                    this.state = State.FAILURE;
                    }
				    
		    		break;
		    	}
		    	case MONITORING:
		    	{
		    		try {
		    			int jobstate = sgp.get().intValue();
		    			if(lastState!=jobstate) {
		    				System.out.print("State: " +jobstate+": ");
		    			}
	                    if(jobstate==Session.DONE) {	
	                    	System.out.print("DONE\n\r");
	                    	state = State.FINISHED;
	                    }else if(jobstate==Session.FAILED) {
	                    	state = State.FAILURE;
	                    	System.out.print("FAILED\n\r");
	                    }else if(jobstate==Session.QUEUED_ACTIVE) {
	                    	System.out.print("QUEUED_ACTIVE\n\r");
	                    }else if(jobstate==Session.RUNNING) {
	                    	System.out.print("RUNNING\n\r");
	                    }else if(jobstate==Session.HOLD) {
	                    	System.out.print("HOLD\n\r");
	                    }else if(jobstate==Session.SUSPEND) {
	                    	System.out.print("SUSPEND\n\r");
	                    }else if(jobstate==Session.UNDETERMINED) {
	                    	System.out.print("UNDETERMINED\n\r");
	                    }else{
	                    	System.out.print("UNKNOWN\n\r");
	                    }
	                    lastState = jobstate;
                    } catch (InterruptedException e) {
	                    System.err.println("Interrupted while waiting for result");
                    } catch (ExecutionException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
                    }
		    		break;
		    	}
    		}
    		try {
	            Thread.sleep(200);
            } catch (InterruptedException e) {
	            System.err.println("Thread interrupted while sleeping: \n\r"+e.getLocalizedMessage());
            }
    	}
	    switch(state){
	    	case FINISHED:
	    	{
	    		System.out.println("Job finished successfully!");
	    		break;
	    	}
	    	case FAILURE:
	    	{
	    		System.out.println("Job failed, consult output!");
	    		break;
	    	}
	    }
	    try {
	        s.deleteJobTemplate(jt);
	        s.exit();	
        } catch (DrmaaException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }	
		
    }

}
