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
package org.eclipse.equinox.internal.p2.ui2.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui2.*;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.ProvisioningListener;
import org.eclipse.equinox.internal.provisional.p2.core.repository.IRepository;
import org.eclipse.equinox.internal.provisional.p2.core.repository.RepositoryEvent;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.ui2.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.actions.PropertyDialogAction;
import org.eclipse.equinox.internal.provisional.p2.ui2.dialogs.AddRepositoryDialog;
import org.eclipse.equinox.internal.provisional.p2.ui2.dialogs.AvailableIUGroup;
import org.eclipse.equinox.internal.provisional.p2.ui2.operations.ProvisioningOperation;
import org.eclipse.equinox.internal.provisional.p2.ui2.operations.RepositoryOperation;
import org.eclipse.equinox.internal.provisional.p2.ui2.policy.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.viewers.StructuredViewerProvisioningListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.statushandlers.StatusManager;

public class AvailableIUsPage extends WizardPage implements ISelectableIUsPage {
	private static final String DIALOG_SETTINGS_SECTION = "AvailableIUsPage"; //$NON-NLS-1$
	private static final String AVAILABLE_VIEW_TYPE = "AvailableViewType"; //$NON-NLS-1$
	private static final String SHOW_LATEST_VERSIONS_ONLY = "ShowLatestVersionsOnly"; //$NON-NLS-1$
	private static final String HIDE_INSTALLED_IUS = "HideInstalledContent"; //$NON-NLS-1$
	private static final String LINKACTION = "linkAction"; //$NON-NLS-1$
	private static final int DEFAULT_WIDTH = 300;
	private static final String ALL = ProvUIMessages.AvailableIUsPage_AllSites;
	private static final int INDEX_ALL = 0;
	private static final int DEC_MARGIN_WIDTH = 2;

	String profileId;
	Policy policy;
	Object[] initialSelections;
	QueryableMetadataRepositoryManager manager;
	IUViewQueryContext queryContext;
	AvailableIUGroup availableIUGroup;
	Composite availableIUButtonBar;
	Combo repoCombo;
	Link propLink, repoLink, installLink;
	Button useCategoriesCheckbox, hideInstalledCheckbox, showLatestVersionsCheckbox;
	Text detailsArea;
	StructuredViewerProvisioningListener profileListener;
	ProvisioningListener repoListener;
	Display display;
	ControlDecoration repoDec;
	Image info, warning, error;
	int batchCount = 0;
	URI[] comboRepos;

	public AvailableIUsPage(Policy policy, String profileId, QueryableMetadataRepositoryManager manager) {
		super("AvailableSoftwarePage"); //$NON-NLS-1$
		this.policy = policy;
		this.profileId = profileId;
		this.manager = manager;
		makeQueryContext();
		setTitle(ProvUIMessages.AvailableIUsPage_Title);
		setDescription(ProvUIMessages.AvailableIUsPage_Description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		this.display = parent.getDisplay();

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertHorizontalDLUsToPixels(DEFAULT_WIDTH);
		composite.setLayoutData(gd);
		setDropTarget(composite);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;

		composite.setLayout(layout);
		// Repo manipulation 
		createRepoArea(composite);

		// Now the available group 
		availableIUGroup = new AvailableIUGroup(policy, composite, JFaceResources.getDialogFont(), manager, queryContext, ProvUI.getIUColumnConfig());

		// Selection listeners must be registered on both the normal selection
		// events and the check mark events.  Must be done after buttons 
		// are created so that the buttons can register and receive their selection notifications before us.
		availableIUGroup.getStructuredViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateDetails();
				checkPropertyLink();
			}
		});

		availableIUGroup.getCheckboxTreeViewer().addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validateNextButton();
			}
		});

		addProvisioningListeners();

		availableIUGroup.setUseBoldFontForFilteredItems(queryContext.getViewType() != IUViewQueryContext.AVAILABLE_VIEW_FLAT);
		setDropTarget(availableIUGroup.getStructuredViewer().getControl());

		// Details area
		Group detailsComposite = new Group(composite, SWT.NONE);
		detailsComposite.setText(ProvUIMessages.ProfileModificationWizardPage_DetailsLabel);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		detailsComposite.setLayout(layout);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		detailsComposite.setLayoutData(gd);

		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.verticalIndent = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		gd.heightHint = convertHeightInCharsToPixels(ILayoutConstants.DEFAULT_DESCRIPTION_HEIGHT);
		gd.widthHint = convertHorizontalDLUsToPixels(DEFAULT_WIDTH);

		detailsArea = new Text(detailsComposite, SWT.WRAP | SWT.READ_ONLY);
		detailsArea.setLayoutData(gd);

		gd = new GridData(SWT.END, SWT.FILL, true, false);
		gd.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		propLink = createLink(detailsComposite, new PropertyDialogAction(new SameShellProvider(parent.getShell()), availableIUGroup.getStructuredViewer()), ProvUIMessages.AvailableIUsPage_GotoProperties);
		propLink.setLayoutData(gd);

		// Controls for filtering/presentation/site selection
		Composite controlsComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		controlsComposite.setLayout(layout);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		controlsComposite.setLayoutData(gd);

		createViewControlsArea(controlsComposite);

		initializeWidgetState();
		setControl(composite);
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				removeProvisioningListeners();
			}

		});
	}

	private void createViewControlsArea(Composite parent) {
		showLatestVersionsCheckbox = new Button(parent, SWT.CHECK);
		showLatestVersionsCheckbox.setText(ProvUIMessages.AvailableIUsPage_ShowLatestVersions);
		showLatestVersionsCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}

			public void widgetSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}
		});

		hideInstalledCheckbox = new Button(parent, SWT.CHECK);
		hideInstalledCheckbox.setText(ProvUIMessages.AvailableIUsPage_HideInstalledItems);
		hideInstalledCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}

			public void widgetSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}
		});

		useCategoriesCheckbox = new Button(parent, SWT.CHECK);
		useCategoriesCheckbox.setText(ProvUIMessages.AvailableIUsPage_GroupByCategory);
		useCategoriesCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}

			public void widgetSelected(SelectionEvent e) {
				updateQueryContext();
				availableIUGroup.updateAvailableViewState();
			}
		});

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		installLink = createLink(parent, new Action() {
			public void runWithEvent(Event event) {
				ProvUI.openInstallationDialog(event);
			}
		}, ProvUIMessages.AvailableIUsPage_GotoInstallInfo);
		installLink.setLayoutData(gd);
	}

	private void createRepoArea(Composite parent) {
		// Site controls are only available if a repository manipulator
		// is specified.
		final RepositoryManipulator repoMan = policy.getRepositoryManipulator();
		if (repoMan != null) {
			// Get the possible field error indicators
			info = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage();
			warning = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
			error = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
			// Combo that filters sites
			Composite comboComposite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginTop = 0;
			layout.marginBottom = IDialogConstants.VERTICAL_SPACING;
			layout.numColumns = 3;
			comboComposite.setLayout(layout);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			comboComposite.setLayoutData(gd);

			Label label = new Label(comboComposite, SWT.NONE);
			label.setText(ProvUIMessages.AvailableIUsPage_RepoFilterLabel);

			repoCombo = new Combo(comboComposite, SWT.DROP_DOWN);
			repoCombo.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					repoComboSelectionChanged();
				}

				public void widgetSelected(SelectionEvent e) {
					repoComboSelectionChanged();
				}

			});
			repoCombo.addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.CR)
						addRepository(false);
				}
			});

			// We don't ever want this to be interpreted as a default
			// button event
			repoCombo.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_RETURN) {
						e.doit = false;
					}
				}
			});
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			// breathing room for info dec
			gd.horizontalIndent = DEC_MARGIN_WIDTH * 2;
			repoCombo.setLayoutData(gd);
			repoCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					URI location = null;
					IStatus status = null;
					try {
						String text = repoCombo.getText();
						// only validate text that doesn't match existing text in combo
						if (getComboIndex(text) < 0) {
							location = URIUtil.fromString(repoCombo.getText());
							RepositoryLocationValidator validator = repoMan.getRepositoryLocationValidator(getShell());
							status = validator.validateRepositoryLocation(location, false, new NullProgressMonitor());
						}
					} catch (URISyntaxException e) {
						status = RepositoryLocationValidator.getInvalidLocationStatus(repoCombo.getText());
					}
					setRepoComboDecoration(status);
				}
			});

			repoDec = new ControlDecoration(repoCombo, SWT.LEFT | SWT.TOP);
			repoDec.setMarginWidth(DEC_MARGIN_WIDTH);

			DropTarget target = new DropTarget(repoCombo, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
			target.setTransfer(new Transfer[] {URLTransfer.getInstance(), FileTransfer.getInstance()});
			target.addDropListener(new URLDropAdapter(true) {
				/* (non-Javadoc)
				 * @see org.eclipse.equinox.internal.provisional.p2.ui.dialogs.URLDropAdapter#handleURLString(java.lang.String, org.eclipse.swt.dnd.DropTargetEvent)
				 */
				protected void handleDrop(String urlText, DropTargetEvent event) {
					repoCombo.setText(urlText);
					event.detail = DND.DROP_LINK;
					addRepository(false);
				}
			});

			Button button = new Button(comboComposite, SWT.PUSH);
			button.setText(ProvUIMessages.AvailableIUsPage_AddButton);
			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					addRepository(true);
				}

				public void widgetSelected(SelectionEvent e) {
					addRepository(true);
				}
			});

			// Link to repository manipulator
			repoLink = createLink(comboComposite, new Action() {
				public void runWithEvent(Event event) {
					policy.getRepositoryManipulator().manipulateRepositories(getShell());
				}
			}, policy.getRepositoryManipulator().getManipulatorLinkLabel());
			gd = new GridData(SWT.END, SWT.FILL, true, false);
			gd.horizontalSpan = 3;
			repoLink.setLayoutData(gd);
		}
	}

	void checkPropertyLink() {
		propLink.setVisible(availableIUGroup.getSelectedIUElements().length == 1);
	}

	void validateNextButton() {
		setPageComplete(availableIUGroup.getCheckedLeafIUs().length > 0);
	}

	void updateQueryContext() {
		queryContext.setShowLatestVersionsOnly(showLatestVersionsCheckbox.getSelection());
		if (hideInstalledCheckbox.getSelection())
			queryContext.hideAlreadyInstalled(profileId);
		else
			queryContext.showAlreadyInstalled();
		if (useCategoriesCheckbox.getSelection())
			queryContext.setViewType(IUViewQueryContext.AVAILABLE_VIEW_BY_CATEGORY);
		else
			queryContext.setViewType(IUViewQueryContext.AVAILABLE_VIEW_FLAT);
	}

	private Link createLink(Composite parent, IAction action, String text) {
		Link link = new Link(parent, SWT.PUSH);
		link.setText(text);

		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IAction linkAction = getLinkAction(event.widget);
				if (linkAction != null) {
					linkAction.runWithEvent(event);
				}
			}
		});
		link.setToolTipText(action.getToolTipText());
		link.setData(LINKACTION, action);
		return link;
	}

	IAction getLinkAction(Widget widget) {
		Object data = widget.getData(LINKACTION);
		if (data == null || !(data instanceof IAction)) {
			return null;
		}
		return (IAction) data;
	}

	private void setDropTarget(Control control) {
		DropTarget target = new DropTarget(control, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] {URLTransfer.getInstance(), FileTransfer.getInstance()});
		target.addDropListener(new RepositoryManipulatorDropTarget(policy.getRepositoryManipulator(), control));
	}

	private void initializeWidgetState() {
		// Set widgets according to query context
		hideInstalledCheckbox.setSelection(queryContext.getHideAlreadyInstalled());
		showLatestVersionsCheckbox.setSelection(queryContext.getShowLatestVersionsOnly());
		useCategoriesCheckbox.setSelection(queryContext.getViewType() == IUViewQueryContext.AVAILABLE_VIEW_BY_CATEGORY);
		availableIUGroup.updateTreeColumns();
		if (initialSelections != null)
			availableIUGroup.setChecked(initialSelections);

		Control focusControl = null;
		focusControl = availableIUGroup.getDefaultFocusControl();
		if (focusControl != null)
			focusControl.setFocus();
		updateDetails();
		checkPropertyLink();
		validateNextButton();
		fillRepoCombo(ALL);
		setRepoComboDecoration(null);
	}

	public boolean performFinish() {
		savePageSettings();
		return true;
	}

	private void makeQueryContext() {
		// Make a local query context that is based on the default.
		IUViewQueryContext defaultQueryContext = policy.getQueryContext();
		queryContext = new IUViewQueryContext(defaultQueryContext.getViewType());
		queryContext.setArtifactRepositoryFlags(defaultQueryContext.getArtifactRepositoryFlags());
		queryContext.setMetadataRepositoryFlags(defaultQueryContext.getMetadataRepositoryFlags());
		if (defaultQueryContext.getHideAlreadyInstalled()) {
			queryContext.hideAlreadyInstalled(profileId);
		}
		queryContext.setShowLatestVersionsOnly(defaultQueryContext.getShowLatestVersionsOnly());
		queryContext.setVisibleAvailableIUProperty(defaultQueryContext.getVisibleAvailableIUProperty());
		queryContext.setVisibleInstalledIUProperty(defaultQueryContext.getVisibleInstalledIUProperty());
		// Now check for saved away dialog settings
		IDialogSettings settings = ProvUIActivator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section != null) {
			// View by...
			try {
				if (section.get(AVAILABLE_VIEW_TYPE) != null)
					queryContext.setViewType(section.getInt(AVAILABLE_VIEW_TYPE));
			} catch (NumberFormatException e) {
				// Ignore if there actually was a value that didn't parse.  
			}

			// Show latest versions
			if (section.get(SHOW_LATEST_VERSIONS_ONLY) != null)
				queryContext.setShowLatestVersionsOnly(section.getBoolean(SHOW_LATEST_VERSIONS_ONLY));

			// Hide installed content
			boolean hideContent = section.getBoolean(HIDE_INSTALLED_IUS);
			if (hideContent)
				queryContext.hideAlreadyInstalled(profileId);
			else
				queryContext.showAlreadyInstalled();
		}
	}

	private void savePageSettings() {
		if (getShell().isDisposed())
			return;
		IDialogSettings settings = ProvUIActivator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		section.put(AVAILABLE_VIEW_TYPE, queryContext.getViewType());
		section.put(SHOW_LATEST_VERSIONS_ONLY, showLatestVersionsCheckbox.getSelection());
		section.put(HIDE_INSTALLED_IUS, hideInstalledCheckbox.getSelection());
	}

	void updateDetails() {
		IInstallableUnit[] selected = getSelectedIUs();
		if (selected.length == 1) {
			StringBuffer result = new StringBuffer();
			String description = IUPropertyUtils.getIUProperty(selected[0], IInstallableUnit.PROP_DESCRIPTION);
			if (description != null) {
				result.append(description);
			} else {
				String name = IUPropertyUtils.getIUProperty(selected[0], IInstallableUnit.PROP_NAME);
				if (name != null)
					result.append(name);
				else
					result.append(selected[0].getId());
				result.append(" "); //$NON-NLS-1$
				result.append(selected[0].getVersion().toString());
			}

			detailsArea.setText(result.toString());
			return;
		}
		detailsArea.setText(""); //$NON-NLS-1$
	}

	public IInstallableUnit[] getSelectedIUs() {
		return availableIUGroup.getSelectedIUs();
	}

	/*
	 * This method is provided only for automated testing.
	 */
	public AvailableIUGroup testGetAvailableIUGroup() {
		return availableIUGroup;
	}

	public IInstallableUnit[] getCheckedIUs() {
		return availableIUGroup.getCheckedLeafIUs();
	}

	/*
	 * Overridden so that we don't call getNextPage().
	 * We use getNextPage() to start resolving the install so
	 * we only want to do that when the next button is pressed.
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.p2.ui.dialogs.ISelectableIUsPage#getCheckedIUElements()
	 */
	public Object[] getCheckedIUElements() {
		return availableIUGroup.getCheckedLeafIUs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.p2.ui.dialogs.ISelectableIUsPage#getSelectedIUElements()
	 */
	public Object[] getSelectedIUElements() {
		return availableIUGroup.getSelectedIUElements();
	}

	/**
	 * Set the initial selections to be used in this page.  This method has no effect
	 * once the page has been created.
	 * 
	 * @param elements
	 */
	public void setInitialSelections(Object[] elements) {
		initialSelections = elements;
	}

	/*
	 *  Add a repository using the text in the combo or launch a dialog if the text
	 *  represents an already known repo.  For any add operation spawned by this
	 *  method, we do not want to notify the UI with a special listener.  This is to
	 *  prevent a multiple update flash because we intend to reset the available IU
	 *  filter as soon as the new repo is added.
	 */
	void addRepository(boolean alwaysPrompt) {
		final RepositoryManipulator repoMan = policy.getRepositoryManipulator();
		if (repoMan == null)
			return;
		final String selectedRepo = repoCombo.getText();
		int selectionIndex = getComboIndex(selectedRepo);
		final boolean isNewText = selectionIndex < 0;
		// If we are adding something already in the combo, just
		// select that item.
		if (!alwaysPrompt && !isNewText && selectionIndex != repoCombo.getSelectionIndex()) {
			repoCombo.select(selectionIndex);
		} else if (alwaysPrompt) {
			AddRepositoryDialog dialog = new AddRepositoryDialog(getShell(), policy.getQueryContext().getMetadataRepositoryFlags()) {
				protected ProvisioningOperation getOperation(URI repositoryLocation) {
					RepositoryOperation op = repoMan.getAddOperation(repositoryLocation);
					op.setNotify(false);
					return op;
				}

				protected String getInitialLocationText() {
					if (isNewText)
						return selectedRepo;
					return super.getInitialLocationText();
				}

			};
			dialog.setTitle(repoMan.getAddOperationLabel());
			dialog.open();
		} else if (isNewText) {
			try {
				getContainer().run(false, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						URI location = null;
						IStatus status;
						try {
							location = URIUtil.fromString(selectedRepo);
							RepositoryLocationValidator validator = repoMan.getRepositoryLocationValidator(getShell());
							status = validator.validateRepositoryLocation(location, false, monitor);
						} catch (URISyntaxException e) {
							status = RepositoryLocationValidator.getInvalidLocationStatus(selectedRepo);
						}
						if (status.isOK() && location != null) {
							try {
								RepositoryOperation op = repoMan.getAddOperation(location);
								op.setNotify(false);
								op.execute(monitor);
							} catch (ProvisionException e) {
								// TODO Auto-generated catch block
								ProvUI.handleException(e, null, StatusManager.SHOW);
							}
						}
						setRepoComboDecoration(status);
					}
				});
			} catch (InvocationTargetException e) {
				// ignore
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	void fillRepoCombo(final String selection) {
		if (repoCombo == null || policy.getRepositoryManipulator() == null)
			return;
		comboRepos = policy.getRepositoryManipulator().getKnownRepositories();
		final String[] items = new String[comboRepos.length + 1];
		items[INDEX_ALL] = ALL;
		for (int i = 0; i < comboRepos.length; i++)
			items[i + 1] = comboRepos[i].toString();
		display.asyncExec(new Runnable() {
			public void run() {
				String repoToSelect = selection == null ? repoCombo.getItem(repoCombo.getSelectionIndex()) : selection;
				repoCombo.setItems(items);
				boolean selected = false;
				for (int i = 0; i < items.length; i++)
					if (items[i].equals(repoToSelect)) {
						selected = true;
						repoCombo.select(i);
						break;
					}
				if (!selected)
					repoCombo.select(INDEX_ALL);
				repoComboSelectionChanged();
			}
		});

	}

	int getComboIndex(String repoText) {
		int index = -1;
		if (repoText.length() > 0) {
			String[] items = repoCombo.getItems();
			for (int i = 0; i < items.length; i++)
				if (repoText.equals(items[i])) {
					index = i;
					break;
				}
		}
		return index;
	}

	void repoComboSelectionChanged() {
		int selection = repoCombo.getSelectionIndex();
		if (comboRepos == null || selection > comboRepos.length)
			selection = INDEX_ALL;

		if (selection == INDEX_ALL) {
			availableIUGroup.setRepositoryFilter(null);
		} else if (selection > 0) {
			availableIUGroup.setRepositoryFilter(comboRepos[selection - 1]);
		}
	}

	void addProvisioningListeners() {
		// We might need to adjust the content of this viewer according to installation
		// changes.  We want to be very selective about refreshing.
		profileListener = new StructuredViewerProvisioningListener(availableIUGroup.getStructuredViewer(), StructuredViewerProvisioningListener.PROV_EVENT_PROFILE) {
			protected void profileAdded(String id) {
				// do nothing
			}

			protected void profileRemoved(String id) {
				// do nothing
			}

			protected void profileChanged(String id) {
				if (id.equals(profileId)) {
					display.asyncExec(new Runnable() {
						public void run() {
							if (isClosing())
								return;
							refreshAll();
						}
					});
				}
			}
		};
		ProvUI.addProvisioningListener(profileListener);
		repoListener = new ProvisioningListener() {

			public void notify(EventObject o) {
				if (o instanceof BatchChangeBeginningEvent) {
					batchCount++;
				} else if (o instanceof BatchChangeCompleteEvent) {
					batchCount--;
					if (batchCount <= 0)
						if (policy.getRepositoryManipulator() != null)
							fillRepoCombo(null);
						else
							display.asyncExec(new Runnable() {
								public void run() {
									availableIUGroup.updateAvailableViewState();
								}
							});
				} else if (batchCount > 0) {
					// We are in the middle of a batch operation
					return;
				} else if (o instanceof RepositoryEvent) {
					final RepositoryEvent event = (RepositoryEvent) o;
					// Do not refresh unless this is the type of repo that we are interested in
					if (event.getRepositoryType() == IRepository.TYPE_METADATA) {
						if (event.getKind() == RepositoryEvent.ADDED && event.isRepositoryEnabled()) {
							fillRepoCombo(event.getRepositoryLocation().toString());
						} else if (event.getKind() == RepositoryEvent.REMOVED) {
							fillRepoCombo(null);
						} else if (event.getKind() == RepositoryEvent.ENABLEMENT) {
							if (event.isRepositoryEnabled())
								fillRepoCombo(event.getRepositoryLocation().toString());
							else
								fillRepoCombo(null);
						}
					}
				}
			}
		};
		IProvisioningEventBus bus = ProvUIActivator.getDefault().getProvisioningEventBus();
		if (bus != null)
			bus.addListener(repoListener);
	}

	void removeProvisioningListeners() {
		if (profileListener != null) {
			ProvUI.removeProvisioningListener(profileListener);
			profileListener = null;
		}
		if (repoListener != null) {
			IProvisioningEventBus bus = ProvUIActivator.getDefault().getProvisioningEventBus();
			if (bus != null)
				bus.removeListener(repoListener);
			repoListener = null;
		}
	}

	void setRepoComboDecoration(IStatus status) {
		if (status == null || status.isOK() || status.getSeverity() == IStatus.CANCEL) {
			repoDec.setShowOnlyOnFocus(true);
			repoDec.setDescriptionText(ProvUIMessages.AvailableIUsPage_RepoFilterInstructions);
			repoDec.setImage(info);
			// We may have been previously showing an error or warning
			// hover.  We will need to dismiss it, but if there is no text
			// typed, don't do this, so that the user gets the info cue
			if (repoCombo.getText().length() > 0)
				repoDec.showHoverText(null);
			return;
		}
		Image image;
		if (status.getSeverity() == IStatus.WARNING)
			image = warning;
		else if (status.getSeverity() == IStatus.ERROR)
			image = error;
		else
			image = info;
		repoDec.setImage(image);
		repoDec.setDescriptionText(status.getMessage());
		repoDec.setShowOnlyOnFocus(false);
		repoDec.showHoverText(status.getMessage());
	}
}
