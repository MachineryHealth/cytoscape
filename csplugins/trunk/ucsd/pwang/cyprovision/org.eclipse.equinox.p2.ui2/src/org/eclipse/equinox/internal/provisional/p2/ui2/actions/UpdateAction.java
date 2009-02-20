/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.provisional.p2.ui2.actions;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui2.PlanAnalyzer;
import org.eclipse.equinox.internal.p2.ui2.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui2.model.AvailableUpdateElement;
import org.eclipse.equinox.internal.p2.ui2.model.IUElementListRoot;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.ui2.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.dialogs.UpdateWizard;
import org.eclipse.equinox.internal.provisional.p2.ui2.model.Updates;
import org.eclipse.equinox.internal.provisional.p2.ui2.operations.PlannerResolutionOperation;
import org.eclipse.equinox.internal.provisional.p2.ui2.policy.Policy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class UpdateAction extends ExistingIUInProfileAction {

	IUElementListRoot root; // root that will be used to seed the wizard
	HashMap latestReplacements;
	boolean resolveIsVisible = true;
	QueryableMetadataRepositoryManager manager;

	public UpdateAction(Policy policy, ISelectionProvider selectionProvider, String profileId, boolean resolveIsVisible) {
		super(ProvUI.UPDATE_COMMAND_LABEL, policy, selectionProvider, profileId);
		setToolTipText(ProvUI.UPDATE_COMMAND_TOOLTIP);
		this.resolveIsVisible = resolveIsVisible;
	}

	public void setRepositoryManager(QueryableMetadataRepositoryManager manager) {
		this.manager = manager;
	}

	protected int performAction(IInstallableUnit[] ius, String targetProfileId, PlannerResolutionOperation resolution) {
		// Caches should have been created while formulating the plan
		Assert.isNotNull(latestReplacements);
		Assert.isNotNull(root);
		Assert.isNotNull(resolution);

		UpdateWizard wizard = new UpdateWizard(getPolicy(), targetProfileId, root, latestReplacements.values().toArray(), resolution, manager);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IProvHelpContextIds.UPDATE_WIZARD);

		return dialog.open();
	}

	protected ProfileChangeRequest getProfileChangeRequest(IInstallableUnit[] ius, String targetProfileId, MultiStatus status, IProgressMonitor monitor) {
		// Here we create a profile change request by finding the latest version available for any replacement.
		ArrayList toBeUpdated = new ArrayList();
		latestReplacements = new HashMap();
		ArrayList allReplacements = new ArrayList();
		SubMonitor sub = SubMonitor.convert(monitor, ProvUIMessages.ProfileChangeRequestBuildingRequest, ius.length);
		for (int i = 0; i < ius.length; i++) {
			ElementQueryDescriptor descriptor = getQueryProvider().getQueryDescriptor(new Updates(targetProfileId, new IInstallableUnit[] {ius[i]}));
			Iterator iter = descriptor.queryable.query(descriptor.query, descriptor.collector, sub).iterator();
			if (iter.hasNext())
				toBeUpdated.add(ius[i]);
			ArrayList currentReplacements = new ArrayList();
			root = new IUElementListRoot();
			while (iter.hasNext()) {
				IInstallableUnit iu = (IInstallableUnit) ProvUI.getAdapter(iter.next(), IInstallableUnit.class);
				if (iu != null) {
					AvailableUpdateElement element = new AvailableUpdateElement(root, iu, ius[i], targetProfileId, true);
					currentReplacements.add(element);
					allReplacements.add(element);
				}
			}
			root.setChildren(allReplacements.toArray());
			for (int j = 0; j < currentReplacements.size(); j++) {
				AvailableUpdateElement replacementElement = (AvailableUpdateElement) currentReplacements.get(j);
				AvailableUpdateElement latestElement = (AvailableUpdateElement) latestReplacements.get(replacementElement.getIU().getId());
				IInstallableUnit latestIU = latestElement == null ? null : latestElement.getIU();
				if (latestIU == null || replacementElement.getIU().getVersion().compareTo(latestIU.getVersion()) > 0)
					latestReplacements.put(replacementElement.getIU().getId(), replacementElement);
			}
			sub.worked(1);
		}
		if (toBeUpdated.size() <= 0) {
			status.add(PlanAnalyzer.getStatus(IStatusCodes.NOTHING_TO_UPDATE, null));
			sub.done();
			return null;
		}

		ProfileChangeRequest request = ProfileChangeRequest.createByProfileId(targetProfileId);
		Iterator iter = toBeUpdated.iterator();
		while (iter.hasNext())
			request.removeInstallableUnits(new IInstallableUnit[] {(IInstallableUnit) iter.next()});
		iter = latestReplacements.values().iterator();
		while (iter.hasNext())
			request.addInstallableUnits(new IInstallableUnit[] {((AvailableUpdateElement) iter.next()).getIU()});
		sub.done();
		return request;
	}

	protected String getTaskName() {
		return ProvUIMessages.UpdateIUProgress;
	}

	protected boolean isResolveUserVisible() {
		return resolveIsVisible;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.ui.actions.AlterExistingProfileIUAction#getLockConstant()
	 */
	protected int getLockConstant() {
		return IInstallableUnit.LOCK_UPDATE;
	}

	protected IStatus getNoProfileOrSelectionStatus(String id, IInstallableUnit[] ius) {
		if (ius.length == 0)
			return PlanAnalyzer.getStatus(IStatusCodes.NOTHING_TO_UPDATE, null);
		return super.getNoProfileOrSelectionStatus(id, ius);
	}
}
