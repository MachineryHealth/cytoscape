/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.provisional.p2.ui2;

import java.security.cert.Certificate;
import org.eclipse.equinox.internal.p2.ui2.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui2.dialogs.TrustCertificateDialog;
import org.eclipse.equinox.internal.p2.ui2.dialogs.UserValidationDialog;
import org.eclipse.equinox.internal.p2.ui2.viewers.CertificateLabelProvider;
import org.eclipse.equinox.internal.provisional.p2.core.IServiceUI;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * The default GUI-based implementation of {@link IServiceUI}.
 */
public class ValidationDialogServiceUI implements IServiceUI {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.core.IServiceUI#getUsernamePassword(java.lang.String)
	 */
	public AuthenticationInfo getUsernamePassword(final String location) {

		final AuthenticationInfo[] result = new AuthenticationInfo[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				Shell shell = ProvUI.getDefaultParentShell();
				String[] buttonLabels = new String[] {ProvUIMessages.ServiceUI_OK, ProvUIMessages.ServiceUI_Cancel};
				String message = NLS.bind(ProvUIMessages.ServiceUI_LoginDetails, location);
				UserValidationDialog dialog = new UserValidationDialog(shell, ProvUIMessages.ServiceUI_LoginRequired, null, message, buttonLabels);
				if (dialog.open() == Window.OK) {
					result[0] = dialog.getResult();
				}
			}

		});
		return result[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.core.IServiceUI#showCertificates(java.lang.Object)
	 */
	public Certificate[] showCertificates(final Certificate[][] certificates) {
		final Object[] result = new Object[1];
		final TreeNode[] input = createTreeNodes(certificates);
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell shell = ProvUI.getDefaultParentShell();
				ILabelProvider labelProvider = new CertificateLabelProvider();
				TreeNodeContentProvider contentProvider = new TreeNodeContentProvider();
				TrustCertificateDialog trustCertificateDialog = new TrustCertificateDialog(shell, input, labelProvider, contentProvider);
				trustCertificateDialog.open();
				Certificate[] values = new Certificate[trustCertificateDialog.getResult() == null ? 0 : trustCertificateDialog.getResult().length];
				for (int i = 0; i < values.length; i++) {
					values[i] = (Certificate) ((TreeNode) trustCertificateDialog.getResult()[i]).getValue();
				}
				result[0] = values;
			}
		});
		return (Certificate[]) result[0];
	}

	private TreeNode[] createTreeNodes(Certificate[][] certificates) {
		TreeNode[] children = new TreeNode[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			TreeNode head = new TreeNode(certificates[i][0]);
			TreeNode parent = head;
			children[i] = head;
			for (int j = 0; j < certificates[i].length; j++) {
				TreeNode node = new TreeNode(certificates[i][j]);
				node.setParent(parent);
				parent.setChildren(new TreeNode[] {node});
				parent = node;
			}
		}
		return children;
	}
}
