/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderSandbox;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Checkout a remote folder into a sandbox that is contained within remote folder handles and
 * the file contents cache.
 */
public class CheckoutToRemoteFolderOperation extends CheckoutOperation {

	RemoteFolderSandbox sandbox;
	
	/**
	 * This class overrides the "Created" handler in order to configure the remote file
	 * to recieve and cache the contents
	 */
	public class CreatedResponseHandler extends UpdatedHandler {
		public CreatedResponseHandler() {
			super(UpdatedHandler.HANDLE_CREATED);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.core.client.UpdatedHandler#receiveTargetFile(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.ICVSFile, java.lang.String, java.util.Date, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected void receiveTargetFile(
			Session session,
			ICVSFile mFile,
			String entryLine,
			Date modTime,
			boolean binary,
			boolean readOnly,
			IProgressMonitor monitor)
			throws CVSException {
			
			if (mFile instanceof RemoteFile) {
			    try {
					((RemoteFile)mFile).aboutToReceiveContents(entryLine.getBytes());
					super.receiveTargetFile(
						session,
						mFile,
						entryLine,
						modTime,
						binary,
						readOnly,
						monitor);
			    } finally {
			        ((RemoteFile)mFile).doneReceivingContents();
			    }
			} else {
				super.receiveTargetFile(
						session,
						mFile,
						entryLine,
						modTime,
						binary,
						readOnly,
						monitor);
			}
		}
	}
	
	public class SandboxCheckout extends Checkout {
		
			/* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.core.client.Command#commandFinished(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption[], org.eclipse.team.internal.ccvs.core.client.Command.LocalOption[], org.eclipse.team.internal.ccvs.core.ICVSResource[], org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IStatus)
		 */
		protected IStatus commandFinished(
			Session session,
			GlobalOption[] globalOptions,
			LocalOption[] localOptions,
			ICVSResource[] resources,
			IProgressMonitor monitor,
			IStatus status)
			throws CVSException {
			
			// Don't do anything (i.e. don't prune)
			return status;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.core.client.Command#doExecute(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption[], org.eclipse.team.internal.ccvs.core.client.Command.LocalOption[], java.lang.String[], org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener, org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus doExecute(
			Session session,
			GlobalOption[] globalOptions,
			LocalOption[] localOptions,
			String[] arguments,
			ICommandOutputListener listener,
			IProgressMonitor monitor)
			throws CVSException {
			
			ResponseHandler newCreated = new CreatedResponseHandler();
			ResponseHandler oldCreated = session.getResponseHandler(newCreated.getResponseID());
			session.registerResponseHandler(newCreated);
			try {
				return super.doExecute(
						session,
						globalOptions,
						localOptions,
						arguments,
						listener,
						monitor);
			} finally {
				session.registerResponseHandler(oldCreated);
			}
		}

}
	public static ICVSRemoteFolder  checkoutRemoteFolder(IWorkbenchPart part, ICVSRemoteFolder folder, IProgressMonitor monitor) throws CVSException, InvocationTargetException, InterruptedException {
		CheckoutToRemoteFolderOperation op = new CheckoutToRemoteFolderOperation(part, folder);
		op.run(monitor);
		return op.getResultingFolder();
	}
	public CheckoutToRemoteFolderOperation(IWorkbenchPart part, ICVSRemoteFolder remoteFolder) {
		super(part, new ICVSRemoteFolder[] { remoteFolder });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutOperation#checkout(org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus checkout(
		ICVSRemoteFolder folder,
		IProgressMonitor monitor)
		throws CVSException {
		
		IPath sandboxPath = new Path(folder.getRepositoryRelativePath()).removeLastSegments(1);
		String pathString;
		if (sandboxPath.isEmpty()) {
			pathString = ICVSRemoteFolder.REPOSITORY_ROOT_FOLDER_NAME;
		} else {
			pathString = sandboxPath.toString();
		}
		sandbox = new RemoteFolderSandbox(null, folder.getRepository(), pathString, folder.getTag());
		return checkout(folder, sandbox, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("CheckoutToRemoteFolderOperation.0", getRemoteFolders()[0].getName()); //$NON-NLS-1$
	}
	
	protected IStatus checkout(final ICVSRemoteFolder resource, final ICVSFolder sandbox, IProgressMonitor pm) throws CVSException {
		// Get the location and the workspace root
		ICVSRepositoryLocation repository = resource.getRepository();
		// Open a connection session to the repository
		final Session session = new Session(repository, sandbox);
		pm.beginTask(null, 100);
		Policy.checkCanceled(pm);
		session.open(Policy.subMonitorFor(pm, 5), false /* read-only */);
		try {
			// Build the local options
			List localOptions = new ArrayList();
			// Add the options related to the CVSTag
			CVSTag tag = resource.getTag();
			if (tag == null) {
				// A null tag in a remote resource indicates HEAD
				tag = CVSTag.DEFAULT;
			}
			localOptions.add(Update.makeTagOption(tag));
			localOptions.add(Checkout.makeDirectoryNameOption(resource.getName()));
			
			// Perform the checkout
			IStatus status = new SandboxCheckout().execute(session,
					Command.NO_GLOBAL_OPTIONS,
					(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
					new String[]{resource.getRepositoryRelativePath()},
					null,
					Policy.subMonitorFor(pm, 90));
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				// Any created projects will exist but will not be mapped to CVS
				return status;
			}
			return OK;
		} catch (CVSException e) {
			// An exception occurred either during the module-expansion or checkout
			// Since we were able to make a connection, return the status so the
			// checkout of any other modules can proceed
			return e.getStatus();
		} finally {
			session.close();
			pm.done();
		}
	}
	
	public ICVSRemoteFolder getResultingFolder() throws CVSException {
		return (ICVSRemoteFolder)sandbox.getFolder(getRemoteFolders()[0].getName());
	}
}
