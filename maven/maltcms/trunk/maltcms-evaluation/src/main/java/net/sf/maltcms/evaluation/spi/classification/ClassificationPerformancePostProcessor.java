/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.evaluation.spi.classification;

import com.db4o.ObjectContainer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.PeakRTFeatureVectorComparator;
import net.sf.maltcms.evaluation.api.tasks.IPostProcessor;
import net.sf.maltcms.evaluation.api.tasks.ITask;
import net.sf.maltcms.evaluation.spi.EntityGroupBuilder;
import net.sf.maltcms.evaluation.spi.Evaluation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author nilshoffmann
 */
public class ClassificationPerformancePostProcessor implements IPostProcessor {

    private File groundTruthFile;
    private double delta;
    private ObjectContainer oc;
    private String toolFileName = "multiple-alignment.csv";

    @Override
    public void process(ITask task) {
        File outputDir = task.getOutputDirectory();
        EntityGroupBuilder egb = new EntityGroupBuilder();
        List<EntityGroup> gt = egb.buildCSVPeakAssociationGroups(groundTruthFile);
        Collection<File> toolResults = FileUtils.listFiles(outputDir, FileFilterUtils.nameFileFilter(toolFileName), TrueFileFilter.INSTANCE);


        System.out.println(
                "Evaluating " + toolResults.size() + " tool results with delta: " + delta);
        ClassificationPerformanceTest<PeakRTFeatureVector> cpt = new ClassificationPerformanceTest<PeakRTFeatureVector>(
                gt, new PeakRTFeatureVectorComparator(delta));
//        List<String> usercategories = new ArrayList<String>(getToolCategories(toolFiles));
        List<String> categories = new ArrayList<String>(Arrays.asList("FullName",
                "Sensitivity", "Specificity", "Precision", "FPR", "Accuracy",
                "F1", "GTEntities", "ToolEntities", "GTExclusive",
                "ToolExclusive", "Common", "TP", "TN", "FP", "FN"));
//        categories.addAll(usercategories);
        File basedir = outputDir;
        if (toolResults.isEmpty()) {
            System.err.println("Could not retrieve " + toolFileName + " for task " + task);
            return;
        }
//        basedir.mkdirs();
        if (toolResults.size() > 1) {
            throw new IllegalArgumentException("One task should generate at most one result file!");
        }
        List<EntityGroup> egs = egb.buildCSVPeakAssociationGroups(toolResults.iterator().next());
        String toolname = task.getTaskId().toString();
        System.out.println("Adding groups for toolName: " + toolname);
//        System.out.println("Creating evaluationResults.csv");
        PerformanceMetrics pm = cpt.performTest(toolname, egs);
//        pm.
//        
//        try {
////            PrintStream dos = new PrintStream(new BufferedOutputStream(
////                    new FileOutputStream(new File(basedir,
////                    "evaluationResults.csv"))));
////            appendToEvaluationResults(categories, dos);
//            try {
//                for (int i = 0; i < toolFiles.size(); i++) {
//                    String toolName = StringTools.removeFileExt(new File(toolFiles.get(i)).getName());
//                    List<EntityGroup> egs = createEntityGroupsForFile(egb,
//                            new File(toolFiles.get(i)));
//                    File fout = new File(basedir, toolName + ".txt");
//                    if (fout.exists()) {
//                        System.err.println("Warning! File exists: " + fout.getAbsolutePath());
//                    }
//                    writeToolResultFile(fout, toolName, cpt, egs, dos);
//                }
//            } catch (Exception e) {
//                System.out.println("Exception: " + e.getLocalizedMessage());
//                System.exit(1);
//            }
//            System.out.println("Done creating tool results!");
//            dos.flush();
//            dos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.exit(1);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        Evaluation re = new Evaluation(outputDir, groundTruthFile, toolResults.toArray(new File[toolResults.size()]));
//
//        re.eval(delta);
    }
}
