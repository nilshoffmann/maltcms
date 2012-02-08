/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.evaluation.spi.alignment;

import net.sf.maltcms.evaluation.api.alignment.AlignmentColumn;
import net.sf.maltcms.evaluation.api.Category;
import net.sf.maltcms.evaluation.api.alignment.AlignmentColumnComparator;
import net.sf.maltcms.evaluation.api.alignment.MultipleAlignment;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class ColumnWiseComparisonTest {

    private MultipleAlignment referenceAlignment;
    private MultipleAlignment toolAlignment;
    private Category[] categories;

    public ColumnWiseComparisonTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        categories = new Category[]{new Category("A"), new Category("B"), new Category("C"), new Category("D")};
        AlignmentColumn ra = new AlignmentColumn(Double.NaN, 2, 3, Double.NaN, 6, Double.NaN);
        AlignmentColumn rb = new AlignmentColumn(1, 2, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        AlignmentColumn rc = new AlignmentColumn(1, 2, 3, 4, Double.NaN, 7);
        AlignmentColumn rd = new AlignmentColumn(Double.NaN, 2, Double.NaN, 4, 6, 7);
        referenceAlignment = new MultipleAlignment();
        referenceAlignment.put(categories[0], ra);
        referenceAlignment.put(categories[1], rb);
        referenceAlignment.put(categories[2], rc);
        referenceAlignment.put(categories[3], rd);

//        AlignmentColumn ta = new AlignmentColumn();
        AlignmentColumn ta = new AlignmentColumn(Double.NaN, 2, 3, Double.NaN, 5, 6, Double.NaN);
        AlignmentColumn tb = new AlignmentColumn(1, 2, Double.NaN, Double.NaN, 5, Double.NaN, Double.NaN);
        AlignmentColumn tc = new AlignmentColumn(1, 2, 3, 4, Double.NaN, Double.NaN, 7);
        AlignmentColumn td = new AlignmentColumn(Double.NaN, 2, Double.NaN, 4, 5, 6, 7);
        toolAlignment = new MultipleAlignment();
        toolAlignment.put(categories[0], ra);
        toolAlignment.put(categories[1], rb);
        toolAlignment.put(categories[2], rc);
        toolAlignment.put(categories[3], rd);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
        AlignmentColumnComparator acc = new AlignmentColumnComparator(0.0d);
        int[] evaluationResults = AlignmentColumnComparator.createResultsArray();
        for (Category category : categories) {
            int[] result = acc.compare(referenceAlignment.get(category), toolAlignment.get(category));
            for (int i = 0; i < result.length; i++) {
                evaluationResults[i] += result[i];
            }
        }
        for (int i = 0; i < evaluationResults.length; i++) {
            System.out.println(AlignmentColumnComparator.names[i]+" = "+evaluationResults[i]);
        }
        
        float prec = evaluationResults[AlignmentColumnComparator.TP]/(float)(evaluationResults[AlignmentColumnComparator.TP]+evaluationResults[AlignmentColumnComparator.FP]);
        System.out.println("Precision: "+(prec));
        float recall = evaluationResults[AlignmentColumnComparator.TP]/(float)(evaluationResults[AlignmentColumnComparator.TP]+evaluationResults[AlignmentColumnComparator.FN]);
        System.out.println("Recall: "+(recall));
        
    }
}
