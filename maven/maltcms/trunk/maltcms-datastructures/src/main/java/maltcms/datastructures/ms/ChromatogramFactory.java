package maltcms.datastructures.ms;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

import cross.IConfigurable;
import cross.Logging;
import cross.ObjectFactory;
import cross.datastructures.fragments.IFileFragment;

public class ChromatogramFactory implements IConfigurable {

	private final Logger log = Logging.getLogger(ObjectFactory.class);

	private Configuration cfg = new PropertiesConfiguration();

	@Override
	public void configure(final Configuration cfg) {
		this.cfg = new PropertiesConfiguration();
		ConfigurationUtils.copy(cfg, this.cfg);
	}

	public IChromatogram1D createChromatogram1D(IFileFragment f) {
		Chromatogram1D c = new Chromatogram1D(f);
		c.configure(this.cfg);
		return c;
	}

	public ProfileChromatogram1D createProfileChromatogram1D(IFileFragment f) {
		ProfileChromatogram1D c = new ProfileChromatogram1D(f);
		c.configure(this.cfg);
		return c;
	}

	public IChromatogram2D createChromatogram2D(IFileFragment f) {
		Chromatogram2D c = new Chromatogram2D(f);
		c.configure(this.cfg);
		return c;
	}
}
