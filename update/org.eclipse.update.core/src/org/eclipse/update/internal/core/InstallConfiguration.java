package org.eclipse.update.internal.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ISite;

public class InstallConfiguration implements IInstallConfiguration {
	
	private boolean isCurrent;
	private ArrayList installSites = new ArrayList();
	private ArrayList linkedSites = new ArrayList();
	
	/*
	 * default constructor. Create
	 */ 
	public InstallConfiguration() {
		this.isCurrent = true;
	}

	/*
	 * @see IInstallConfiguration#getFeatures()
	 */
	public IFeature[] getFeatures() {
		return new IFeature[0];
	}

	/*
	 * @see IInstallConfiguration#getInstallSites()
	 */
	public ISite[] getInstallSites() {
		return new ISite[0];
	}

	/*
	 * @see IInstallConfiguration#addInstallSite(ISite)
	 */
	public void addInstallSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#removeInstallSite(ISite)
	 */
	public void removeInstallSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#getLinkedSites()
	 */
	public ISite[] getLinkedSites() {
		return new ISite[0];
	}

	/*
	 * @see IInstallConfiguration#addLinkedSite(ISite)
	 */
	public void addLinkedSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#removeLinkedSite(ISite)
	 */
	public void removeLinkedSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#isCurrent()
	 */
	public boolean isCurrent() {
		return isCurrent;
	}

}

