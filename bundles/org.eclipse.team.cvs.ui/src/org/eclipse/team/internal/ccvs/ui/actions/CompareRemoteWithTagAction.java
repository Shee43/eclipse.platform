/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteCompareOperation;

/**
 * Compare to versions of a remote resource.
 */
public class CompareRemoteWithTagAction extends CVSAction {

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		ICVSRemoteResource[] resources = getSelectedRemoteResources();
		if (resources.length == 0) return;
		
		// Obtain the tag to compare against
		final ICVSRemoteResource resource = resources[0];
		final CVSTag[] tag = new CVSTag[] { null};
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				ICVSFolder folder;
				if (resource instanceof ICVSRemoteFolder) {
					folder = (ICVSFolder)resource;
				} else {
					folder = resource.getParent();
				}
				tag[0] = TagSelectionDialog.getTagToCompareWith(getShell(), new ICVSFolder[] {folder});
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		if (tag[0] == null) return;
		
		// Run the compare operation in the background
		try {
			new RemoteCompareOperation(null, resource, tag[0])
				.run();
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		ICVSRemoteResource[] resources = getSelectedRemoteResources();
		// Only support single select for now.
		// Need to avoid overlap if multi-select is supported
		return resources.length == 1;
	}

}
