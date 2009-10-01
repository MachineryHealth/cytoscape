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
package org.eclipse.equinox.internal.p2.ui;

import java.net.URI;
import org.eclipse.equinox.internal.p2.ui.model.*;
import org.eclipse.equinox.internal.p2.ui.query.*;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.engine.IUProfilePropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IUPropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.LatestIUVersionQuery;
import org.eclipse.equinox.internal.provisional.p2.query.*;
import org.eclipse.equinox.internal.provisional.p2.ui.*;
import org.eclipse.equinox.internal.provisional.p2.ui.model.MetadataRepositories;
import org.eclipse.equinox.internal.provisional.p2.ui.model.Updates;
import org.eclipse.equinox.internal.provisional.p2.ui.operations.ProvisioningUtil;
import org.eclipse.equinox.internal.provisional.p2.ui.policy.*;
import org.eclipse.osgi.util.NLS;
//import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Provides a default set of queries to drive the provisioning UI.
 * 
 * @since 3.5
 */

public class DefaultQueryProvider extends QueryProvider {

	private Policy policy;

	private Query allQuery = new MatchQuery() {
		public boolean isMatch(Object candidate) {
			return true;
		}
	};

	public DefaultQueryProvider(Policy policy) {
		this.policy = policy;
	}

	public ElementQueryDescriptor getQueryDescriptor(final QueriedElement element) {
		// Initialize queryable, queryContext, and queryType from the element.
		// In some cases we override this.
		IQueryable queryable = element.getQueryable();
		int queryType = element.getQueryType();
		IUViewQueryContext context = element.getQueryContext();
		if (context == null) {
			context = policy.getQueryContext();
		}
		switch (queryType) {
			case QueryProvider.ARTIFACT_REPOS :
				queryable = new QueryableArtifactRepositoryManager(context.getArtifactRepositoryFlags());
				return new ElementQueryDescriptor(queryable, null, new Collector() {
					public boolean accept(Object object) {
						if (object instanceof URI)
							return super.accept(new ArtifactRepositoryElement(element, (URI) object));
						return true;
					}
				});

			case QueryProvider.AVAILABLE_IUS :
				// Things get more complicated if the user wants to filter out installed items. 
				// This involves setting up a secondary query for installed content that the various
				// collectors will use to reject content.  We can't use a compound query because the
				// queryables are different (profile for installed content, repo for available content)
				AvailableIUCollector availableIUCollector;
				boolean showLatest = context.getShowLatestVersionsOnly();
				boolean hideInstalled = context.getHideAlreadyInstalled();
				IProfile targetProfile = null;
				String profileId = context.getInstalledProfileId();
				if (profileId != null) {
					try {
						targetProfile = ProvisioningUtil.getProfile(profileId);
					} catch (ProvisionException e) {
						// just bail out, we won't try to query the installed
					}
				}

				Query topLevelQuery = new IUPropertyQuery(context.getVisibleAvailableIUProperty(), Boolean.TRUE.toString());
				Query categoryQuery = new IUPropertyQuery(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.toString(true));

				// Showing child IU's of a group of repositories, or of a single repository
				if (element instanceof MetadataRepositories || element instanceof MetadataRepositoryElement) {
					if (context.getViewType() == IUViewQueryContext.AVAILABLE_VIEW_FLAT || !context.getUseCategories()) {
						AvailableIUCollector collector;
						if (showLatest)
							collector = new LatestIUVersionElementCollector(queryable, element, false, context.getShowAvailableChildren());
						else
							collector = new AvailableIUCollector(queryable, element, false, context.getShowAvailableChildren());
						if (targetProfile != null)
							collector.markInstalledIUs(targetProfile, hideInstalled);
						return new ElementQueryDescriptor(queryable, topLevelQuery, collector);
					}
					// Installed content not a concern for collecting categories
					return new ElementQueryDescriptor(queryable, categoryQuery, new CategoryElementCollector(queryable, element));
				}

				// If it's a category or some other IUElement to drill down in, we get the requirements and show all requirements
				// that are also visible in the available list.  
				if (element instanceof CategoryElement || (element instanceof IIUElement && ((IIUElement) element).shouldShowChildren())) {
					// children of a category should drill down according to the context.  If we aren't in a category, we are already drilling down and
					// continue to do so.
					boolean drillDown = element instanceof CategoryElement ? context.getShowAvailableChildren() : true;
					Query meetsAnyRequirementQuery = new AnyRequiredCapabilityQuery(((IIUElement) element).getRequirements());
					if (showLatest)
						availableIUCollector = new LatestIUVersionElementCollector(queryable, element, true, drillDown);
					else
						availableIUCollector = new AvailableIUCollector(queryable, element, true, drillDown);
					if (targetProfile != null)
						availableIUCollector.markInstalledIUs(targetProfile, hideInstalled);
					// if it's a category, the metadata was specifically set up so that the requirements are the IU's that should
					// be visible in the category.
					if (element instanceof CategoryElement)
						return new ElementQueryDescriptor(queryable, meetsAnyRequirementQuery, availableIUCollector);
					// If it's not a category, these are generic requirements and should be filtered by the visibility property (topLevelQuery)
					return new ElementQueryDescriptor(queryable, CompoundQuery.createCompoundQuery(new Query[] {topLevelQuery, meetsAnyRequirementQuery}, true), availableIUCollector);
				}
				return null;

			case QueryProvider.AVAILABLE_UPDATES :
				IProfile profile;
				IInstallableUnit[] toUpdate = null;
				if (element instanceof Updates) {
					try {
						profile = ProvisioningUtil.getProfile(((Updates) element).getProfileId());
					} catch (ProvisionException e) {
						//ProvUI.handleException(e, NLS.bind(ProvUIMessages.DefaultQueryProvider_ErrorRetrievingProfile, ((Updates) element).getProfileId()), StatusManager.LOG);
						return null;
					}
					toUpdate = ((Updates) element).getIUs();
				} else {
					//profile = (IProfile) ProvUI.getAdapter(element, IProfile.class);
				}
				//if (profile == null)
				//	return null;
				if (toUpdate == null) {
					//Collector collector = profile.query(new IUProfilePropertyQuery(profile, context.getVisibleInstalledIUProperty(), Boolean.toString(true)), new Collector(), null);
					//toUpdate = (IInstallableUnit[]) collector.toArray(IInstallableUnit.class);
				}
				QueryableUpdates updateQueryable = new QueryableUpdates(toUpdate);
				return new ElementQueryDescriptor(updateQueryable, context.getShowLatestVersionsOnly() ? new LatestIUVersionQuery() : allQuery, new Collector());

			case QueryProvider.INSTALLED_IUS :
				// Querying of IU's.  We are drilling down into the requirements.
				if (element instanceof IIUElement && context.getShowInstallChildren()) {
					Query meetsAnyRequirementQuery = new AnyRequiredCapabilityQuery(((IIUElement) element).getRequirements());
					Query visibleAsAvailableQuery = new IUPropertyQuery(context.getVisibleAvailableIUProperty(), Boolean.TRUE.toString());
					return new ElementQueryDescriptor(queryable, CompoundQuery.createCompoundQuery(new Query[] {visibleAsAvailableQuery, meetsAnyRequirementQuery}, true), new InstalledIUCollector(queryable, element));
				}
				//profile = (IProfile) ProvUI.getAdapter(element, IProfile.class);
				//if (profile == null)
				//	return null;
				return null;//new ElementQueryDescriptor(profile, new IUProfilePropertyQuery(profile, context.getVisibleInstalledIUProperty(), Boolean.toString(true)), new InstalledIUCollector(profile, element));

			case QueryProvider.METADATA_REPOS :
				if (element instanceof MetadataRepositories) {
					if (queryable == null) {
						queryable = new QueryableMetadataRepositoryManager(policy, ((MetadataRepositories) element).getIncludeDisabledRepositories());
						element.setQueryable(queryable);
					}
					return new ElementQueryDescriptor(element.getQueryable(), null, new MetadataRepositoryElementCollector(element.getQueryable(), element));
				}
				return null;

			case QueryProvider.PROFILES :
				queryable = new QueryableProfileRegistry();
				return new ElementQueryDescriptor(queryable, new MatchQuery() {
					public boolean isMatch(Object candidate) {
						return false;//ProvUI.getAdapter(candidate, IProfile.class) != null;
					}
				}, new ProfileElementCollector(null, element));

			default :
				return null;
		}
	}
}
