package maltcms.db.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JTextField;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.metabolite.IAggregatePredicateFactory;
import maltcms.db.predicates.metabolite.MAggregatePredicate;
import maltcms.db.predicates.metabolite.MAggregatePredicateFactory;

import com.db4o.query.Predicate;

/**
 * @author -rhilker-
 *
 */
public class FilterButtonActionListener implements ActionListener {

	private IAggregatePredicateFactory af = null;
	private Predicate<IMetabolite> ap = null;
	private MetaboliteViewModel mvm = null;
	private boolean error = false;
	private HashMap<Integer, JTextField> textFields = null;
	private MetaboliteSearchPreferences msp = null;

	
	public FilterButtonActionListener (MetaboliteViewModel mvm, boolean error, HashMap<Integer, JTextField> textFields, MetaboliteSearchPreferences msp){
	
		this.mvm = mvm;
		this.error = error;
		this.textFields = textFields;
		this.msp = msp;

		this.af = new MAggregatePredicateFactory(new MAggregatePredicate());
		
	}
		@Override
			public void actionPerformed(ActionEvent e) {

			int length = textFields.size();
			String[] filterA = new String[length];
			for (int i=0; i<length; i++){
				filterA[i] = getFilter(textFields.get(i));
			}	
			
			//check if there is a wrong input to a filter textfield
			if (error == false){
				String filter = "";
				for (int i=0; i<length; i++){
					filter = filter+filterA[i];
				}
				System.out.println(filter.toString());
				submitFilter(filter);
				closeWindow();
			} else {
				error = false;
			}
	}

/**
 * reads out a textfield & checks the obtained values
 * @param jtf
 * @return filter
 */
private String getFilter(JTextField jtf){
	int intValue = 0;
	double doubleValue = 0.0;
	String input = jtf.getText();
	String methodName = jtf.getName();
	System.out.println("NAMEEE: "+methodName); //Mit MassWeight stimmt noch was net, teste alle Namen!
	
	if(input.length() != 0){
		String methodReturnType = mvm.getMethodForGetterName(methodName).getGenericReturnType().toString();
		
		//strings don't need to be checked!
		//check for correct integer input
		if(methodReturnType.contains("integer") == true){
			try {
				  intValue = Integer.parseInt(input);
				  input = methodName+"="+input+" ";
				}
				catch(NumberFormatException e) {
				  jtf.setText("Enter number like: 3");
				  error = true;					  
				}
		} else 
			//check for correct double range input 
		if (methodReturnType.contains("double") == true){
			if (input.contains(",")){
				
				String[] check = input.split(",");
				if (check.length==2){
					try {
						doubleValue = Double.parseDouble(check[0]);
						doubleValue = Double.parseDouble(check[1]);
						input = methodName+"=["+input+"] ";
					}
					catch(NumberFormatException e) {
						jtf.setText("Enter range like: 5,6");
						error = true;
					}
				}
			} else {
				try {
						doubleValue = Double.parseDouble(input);
						input = methodName+"="+input+" ";
				}
				catch(NumberFormatException e) {
						jtf.setText("Enter number like: 4.0");
						error = true;
				}
			} 
		} else input=methodName+"="+input+" ";
	}
	return input;
}

	/**
	 * Submit filter method for the extra Search Preferences Frame
	 * 
	 * @param filter
	 */
	public void submitFilter(String filter) {
		// SwingWorker<Void,Integer> sw = new SwingWorker<Void,Integer>() {

		// @Override
		// protected Void doInBackground() throws Exception {
		ap = af.digestCommandLine(filter.split(" "));
		System.out.println("Parsed command line: " + filter);
		if (mvm != null && ap != null) {
			System.out.println("Executing query");
			mvm.query(ap);
		}
	}
	
	
	/**
	 * Closes the preferences screen after submitting a correct filter
	 */
	private void closeWindow(){
			this.msp.setVisible(false);
			//this.msp = null;
	}

}
