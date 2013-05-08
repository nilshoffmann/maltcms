/*
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.vocabulary;

import cross.exception.ConstraintViolationException;
import cross.exception.MappingNotAvailableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@ServiceProvider(service = ICvResolver.class)
@Data
public class CvResolver implements ICvResolver {

	private Map<String, IControlledVocabularyProvider> providers = new ConcurrentHashMap<String, IControlledVocabularyProvider>();

	public CvResolver() {
		for (IControlledVocabularyProvider provider : Lookup.getDefault().lookupAll(IControlledVocabularyProvider.class)) {
			String namespace = provider.getNamespace();
			if (namespace == null || namespace.isEmpty()) {
				throw new ConstraintViolationException("Namespace must not be null!");
			}
			providers.put(provider.getNamespace(), provider);
		}
	}

	@Override
	public String translate(String variable) throws MappingNotAvailableException {
		String ns = getNamespacePrefix(variable);
		if (providers.containsKey(ns)) {
			return providers.get(ns).translate(variable);
		}
		throw new MappingNotAvailableException("No provider known for namespace " + ns + " and variable " + variable);
	}

	protected String getNamespacePrefix(String variable) {
		if (variable.contains(".")) {
			String[] s = variable.split("\\.");
			log.info("Splits of variable: {}", Arrays.toString(s));
			if (s.length < 2) {
				throw new ConstraintViolationException("Variable has no valid namespace declaration: " + variable);
			}
			return s[0];
		}else if(variable.contains(":")) {
			String[] s = variable.split(":");
			log.info("Splits of variable: {}", Arrays.toString(s));
			if (s.length < 2) {
				throw new ConstraintViolationException("Variable has no valid namespace declaration: " + variable);
			}
			return s[0];
		}
		throw new IllegalArgumentException("Can not process namespace of "+variable+"! Unknown delimiter!");
	}

	@Override
	public Collection<? extends IControlledVocabularyProvider> getCvProviders() {
		return providers.values();
	}

	@Override
	public void add(IControlledVocabularyProvider provider) {
		providers.put(provider.getNamespace(), provider);
	}

	@Override
	public void remove(IControlledVocabularyProvider provider) {
		providers.remove(provider.getNamespace());
	}
}
