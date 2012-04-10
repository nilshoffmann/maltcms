/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id: CwtEicPeakFinder.java 426 2012-02-09 19:38:11Z nilshoffmann $
 */
package maltcms.commands.fragments.peakfinding;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.cwtEicPeakFinder.CwtEicPeakFinderCallable;
import maltcms.tools.ArrayTools;
import net.sf.maltcms.execution.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CwtEicPeakFinder extends AFragmentCommand {

    @Override
    public String getDescription() {
        return "Finds EIC peaks using  Continuous Wavelet Transform.";
    }
    
    //FIXME this is wrong!
    public int convertToScans(double duration) {
        return 1;
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        System.out.println("Received " + t.size() + " input files");
        // MasterServerFactory msf = new MasterServerFactory();
        // Impaxs ms = msf.getMasterServerImplementations().get(0);
        // ms.startMasterServer(this.rmiServerConfigFile);
        // JobMonitor jm = new JobMonitor(ms,t.size());
        // ms.addJobEventListener(jm);
        // ExecutorService es = Executors.newSingleThreadExecutor();
        ICompletionService<File> ics = createCompletionService(File.class);
        int cnt = 0;
        for (IFileFragment f : t) {
            IVariableFragment biv = f.getChild("binned_intensity_values");
            IVariableFragment scanIndex = f.getChild("binned_scan_index");
            biv.setIndex(scanIndex);
            List<Array> eics = ArrayTools.tilt(biv.getIndexedArray());
            for(Array eic:eics) {
                CwtEicPeakFinderCallable cwt = new CwtEicPeakFinderCallable();
                cwt.setInput(new File(f.getAbsolutePath()));
                cwt.setMinScale(5);
            }
            
            
            System.out.println("Opening file: " + f.getAbsolutePath());
            // create ConfigurableRunnable config
//            File runtimeConfig = createRuntimeConfiguration(cnt, f, cwt);

            // create job config
//            File jobConfig = createJobConfiguration(runtimeConfig, cnt);
            // try {
            // Job j = new Job(jobConfig.getAbsolutePath());
//            cwt.configure(runtimeConfig);
            log.info("Running cwt peak finder");
//            File featureFile = cwt.call();
//            getWorkflow().append(new DefaultWorkflowResult(featureFile, this,
//                    WorkflowSlot.FILEIO, f));
            // jm.addJob(j.getId());
            // ms.submitJob(j);
            // } catch (MalformedURLException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (ClassNotFoundException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (InstantiationException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IllegalAccessException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            cnt++;
        }
        // es.submit(jm);
        // es.shutdown();
        // try {
        // es.awaitTermination(20, TimeUnit.MINUTES);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        return t;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
