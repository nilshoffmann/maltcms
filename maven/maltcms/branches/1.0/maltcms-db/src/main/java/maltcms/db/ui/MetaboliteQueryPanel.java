package maltcms.db.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MetaboliteQueryPanel extends JPanel implements KeyListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3658654429470080006L;

	private JTextField jtf = null;
	
	private JButton sb = null;
	private JButton spb = null;
	
	private MetaboliteViewModel mvm = null;
	
	public MetaboliteQueryPanel(MetaboliteViewModel mvm) {
		this.mvm = mvm;
		add(getJTextField());
		add(getSubmitButton());
		add(getSearchPreferencesButton());
	}
	
	private JTextField getJTextField() {
		if(this.jtf == null) {
			this.jtf = new JTextField("Please enter query string");
			this.jtf.addKeyListener(this);
		}
		return jtf;
	}
	
	private JButton getSubmitButton() {
		if(this.sb == null) {
			this.sb = new JButton(new MetaboliteQueryAction("Query",null,getJTextField(),mvm));
		}
		return this.sb;
	}
	
	private JButton getSearchPreferencesButton(){
		if(this.spb == null){
			
			ActionListener al = new ActionListener() { 
			@Override
				public void actionPerformed(ActionEvent e) {
						MetaboliteSearchPreferences.getInstance(mvm).setVisible(true);
			}};
			this.spb = new JButton("Search Preferences");
			this.spb.addActionListener(al);
		}
		return this.spb;
	}

	/* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
	    // TODO Auto-generated method stub
	    
    }

	/* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
    	Object o = e.getSource();
	    if(o == this.jtf) {
	    	//System.out.println("Received KeyEvent from JTextField");
	    	int kc = e.getKeyCode();
	    	//System.out.println("Key code: "+kc);
	    	if(kc==KeyEvent.VK_ENTER) {
	    		Runnable r = new Runnable() {
				
					@Override
					public void run() {
						System.out.println("Launching Query");
			    		AbstractAction aa = new MetaboliteQueryAction("Query",null,getJTextField(),mvm);
			    		aa.actionPerformed(new ActionEvent(getJTextField(),(int)System.nanoTime(),(String)aa.getValue(Action.NAME)));
				
					}
				};	    		
	    		SwingUtilities.invokeLater(r);
	    	}else{
	    		System.out.println("Received event from key: "+KeyEvent.getKeyText(kc));
	    	}
	    }
	    
    }

	/* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
    	
	    
    }
}
