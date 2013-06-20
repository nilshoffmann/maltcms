package cross.datastructures.tools;

import cross.Factory;
import cross.cache.CacheType;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IFileFragment;
import cross.io.MockDatasource;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.ArrayChar;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class FragmentToolsTest {

	@Rule
	public LogMethodName logMethodName = new LogMethodName();
	@Rule
	public SetupLogging logging = new SetupLogging();
	@Rule
	public TemporaryFolder tf = new TemporaryFolder();

	/**
	 * Explicitly set the available data sources. Disable caching.
	 */
	@Before
	public void setUp() {
		Factory.getInstance().getDataSourceFactory().setDataSources(Arrays.asList(MockDatasource.class.getCanonicalName()));
		Fragments.setDefaultFragmentCacheType(CacheType.NONE);
	}

	@Test
	public void testCreateSourceFilesArray() {
		try {
			File tempFile = File.createTempFile(getRandomFileName(100), ".cdf");
			int length = tempFile.getAbsoluteFile().toURI().toString().length();
			IFileFragment f0 = new FileFragment();
			IFileFragment f1 = new FileFragment();
			f1.addSourceFile(f0);
			ArrayChar.D2 sourceFiles = FragmentTools.createSourceFilesArray(f1, Arrays.asList(f0));
			log.info("length: {}", length);
			int expectedLength = ((length / 128)) * 128;
			int actualLength = sourceFiles.getShape()[1];
			log.info("expected length: {}, actual length: {}", expectedLength, actualLength);
			Assert.assertEquals(expectedLength, actualLength);
		} catch (IOException ex) {
			Logger.getLogger(FragmentToolsTest.class.getName()).log(Level.SEVERE, null, ex);
			Assert.fail();
		}

	}

	private String getRandomFileName(int length) {
		StringBuilder sb = new StringBuilder();
		//97=a, 122=z
		Random r = new Random();
		for (int i = 0; i < length; i++) {
			int value = 97 + r.nextInt(122 - 97);
			sb.append((char) value);
		}
		return sb.toString();
	}
}