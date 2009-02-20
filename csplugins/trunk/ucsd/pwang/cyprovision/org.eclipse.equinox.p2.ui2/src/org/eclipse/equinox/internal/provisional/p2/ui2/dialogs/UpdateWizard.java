/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genuitec, LLC - added license support
 *******************************************************************************/
package org.eclipse.equinox.internal.provisional.p2.ui2.dialogs;

import java.util.ArrayList;
import org.eclipse.equinox.internal.p2.ui2.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui2.dialogs.*;
import org.eclipse.equinox.internal.p2.ui2.model.AvailableUpdateElement;
import org.eclipse.equinox.internal.p2.ui2.model.IUElementListRoot;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.ui2.ProvUIImages;
import org.eclipse.equinox.internal.provisional.p2.ui2.QueryableMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.ui2.operations.PlannerResolutionOperation;
import org.eclipse.equinox.internal.provisional.p2.ui2.policy.Policy;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.4
 */
public class UpdateWizard extends WizardWithLicenses {
	IInstallableUnit[] iusToReplace;
	QueryableMetadataRepositoryManager manager;

	public UpdateWizard(Policy policy, String profileId, IUElementListRoot root, Object[] initialSelections, PlannerResolutionOperation initialResolution, QueryableMetadataRepositoryManager manager) {
		super(policy, profileId, root, initialSelections, initialResolution);
		setWindowTitle(ProvUIMessages.UpdateAction_UpdatesAvailableTitle);
		setDefaultPageImageDescriptor(ProvUIImages.getImageDescriptor(ProvUIImages.WIZARD_BANNER_UPDATE));
		this.manager = manager;
	}

	protected ISelectableIUsPage createMainPage(IUElementListRoot input, Object[] selections) {
		SelectableIUsPage page = new SelectableIUsPage(policy, input, selections, profileId);
		page.setTitle(ProvUIMessages.UpdateAction_UpdatesAvailableTitle);
		page.setDescription(ProvUIMessages.UpdateAction_UpdatesAvailableMessage);
		return page;
	}

	protected ResolutionWizardPage createResolutionPage(IUElementListRoot root, PlannerResolutionOperation initialResolution) {
		return new UpdateWizardPage(policy, root, profileId, initialResolution);
	}

	protected IUElementListRoot makeResolutionElementRoot(Object[] selectedElements) {
		IUElementListRoot elementRoot = new IUElementListRoot();
		ArrayList list = new ArrayList(selectedElements.length);
		for (int i = 0; i < selectedElements.length; i++) {
			if (selectedElements[i] instanceof AvailableUpdateElement) {
				AvailableUpdateElement element = (AvailableUpdateElement) selectedElements[i];
				AvailableUpdateElement newElement = new AvailableUpdateElement(elementRoot, element.getIU(), element.getIUToBeUpdated(), profileId, policy.getQueryContext().getShowProvisioningPlanChildren());
				list.add(newElement);
			}
		}
		elementRoot.setChildren(list.toArray());
		return elementRoot;
	}

	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		if (manager != null)
			// async exec since we are in the middle of opening
			pageContainer.getDisplay().asyncExec(new Runnable() {
				public void run() {
					manager.reportAccumulatedStatus();
				}
			});
	}
}
