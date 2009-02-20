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
package org.eclipse.equinox.internal.provisional.p2.ui2.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui2.DefaultMetadataURLValidator;
import org.eclipse.equinox.internal.p2.ui2.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui2.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui2.dialogs.RepositoryManipulatorDropTarget;
import org.eclipse.equinox.internal.p2.ui2.model.ElementUtils;
import org.eclipse.equinox.internal.p2.ui2.model.MetadataRepositoryElement;
import org.eclipse.equinox.internal.p2.ui2.viewers.MetadataRepositoryElementComparator;
import org.eclipse.equinox.internal.p2.ui2.viewers.RepositoryDetailsLabelProvider;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.repository.RepositoryEvent;
import org.eclipse.equinox.internal.provisional.p2.ui2.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.model.MetadataRepositories;
import org.eclipse.equinox.internal.provisional.p2.ui2.operations.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.policy.*;
import org.eclipse.equinox.internal.provisional.p2.ui2.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Page that allows users to update, add, remove, import, and
 * export repositories.  This page can be hosted inside a preference
 * dialog or inside its own dialog.  When hosting this page inside
 * a non-preference dialog, some of the dialog methods will likely 
 * have to call page methods.  The following snippet shows how to host
 * this page inside a TitleAreaDialog.
 * <pre>
 *		TitleAreaDialog dialog = new TitleAreaDialog(shell) {
 *
 *			RepositoryManipulationPage page;
 *
 *			protected Control createDialogArea(Composite parent) {
 *				page = new RepositoryManipulationPage(policy);
 *				page.createControl(parent);
 *				this.setTitle("Software Sites");
 *				this.setMessage("The enabled sites will be searched for software.  Disabled sites are ignored.);
 *				return page.getControl();
 *			}
 *
 *			protected void okPressed() {
 *				if (page.performOk())
*					super.okPressed();
 *			}
 *
 *			protected void cancelPressed() {
 *				if (page.performCancel())
 *					super.cancelPressed();
 *			}
 *		};
 *		dialog.open();
 * </pre>
 * 
 * @since 3.5
 */
public class RepositoryManipulationPage extends PreferencePage implements IWorkbenchPreferencePage {
	private final static int WIDTH_IN_DLUS = 480;
	private final static int HEIGHT_IN_DLUS = 240;
	final static String DEFAULT_FILTER_TEXT = ProvUIMessages.RepositoryManipulationPage_DefaultFilterString;
	private final static int FILTER_DELAY = 200;

	StructuredViewerProvisioningListener listener;
	TableViewer repositoryViewer;
	Table table;
	Policy policy;
	Display display;
	boolean changed = false;
	MetadataRepositoryElementComparator comparator;
	RepositoryDetailsLabelProvider labelProvider;
	RepositoryManipulator manipulator;
	RepositoryManipulator localCacheRepoManipulator;
	CachedMetadataRepositories input;
	Text pattern, details;
	PatternFilter filter;
	WorkbenchJob filterJob;
	Button addButton, removeButton, refreshButton, disableButton, exportButton;

	class CachedMetadataRepositories extends MetadataRepositories {
		Hashtable cachedElements;

		CachedMetadataRepositories() {
			super(policy);
			setIncludeDisabledRepositories(true);
		}

		public int getQueryType() {
			return QueryProvider.METADATA_REPOS;
		}

		public Object[] getChildren(Object o) {
			if (cachedElements == null) {
				Object[] children = super.getChildren(o);
				cachedElements = new Hashtable(children.length);
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof MetadataRepositoryElement)
						cachedElements.put(((MetadataRepositoryElement) children[i]).getLocation().toString(), children[i]);
				}
			}
			return cachedElements.values().toArray();
		}

	}

	class MetadataRepositoryPatternFilter extends PatternFilter {
		public boolean isElementVisible(Viewer viewer, Object element) {
			if (element instanceof MetadataRepositoryElement) {
				return wordMatches(labelProvider.getColumnText(element, RepositoryDetailsLabelProvider.COL_NAME) + labelProvider.getColumnText(element, RepositoryDetailsLabelProvider.COL_LOCATION));
			}
			return false;
		}
	}

	/**
	 * This method must be called before the contents are created.
	 * @param policy
	 */
	public void setPolicy(Policy policy) {
		this.policy = policy;
		manipulator = policy.getRepositoryManipulator();
	}

	protected Control createContents(Composite parent) {
		display = parent.getDisplay();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(), IProvHelpContextIds.REPOSITORY_MANIPULATION_DIALOG);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);

		// Filter box
		pattern = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = DEFAULT_FILTER_TEXT;
			}
		});
		pattern.setText(DEFAULT_FILTER_TEXT);
		pattern.selectAll();
		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (table.getItemCount() > 0) {
						table.setFocus();
					} else if (e.character == SWT.CR) {
						return;
					}
				}
			}
		});

		pattern.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				display.asyncExec(new Runnable() {
					public void run() {
						if (!pattern.isDisposed()) {
							if (DEFAULT_FILTER_TEXT.equals(pattern.getText().trim())) {
								pattern.selectAll();
							}
						}
					}
				});
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		pattern.setLayoutData(gd);

		// spacer to fill other column
		new Label(composite, SWT.NONE);

		// Table of available repositories
		repositoryViewer = new TableViewer(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		table = repositoryViewer.getTable();

		// Key listener for delete
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeRepositories();
				}
			}
		});
		setTableColumns();
		repositoryViewer.setComparer(new ProvElementComparer());
		comparator = new MetadataRepositoryElementComparator(RepositoryDetailsLabelProvider.COL_NAME);
		repositoryViewer.setComparator(comparator);
		filter = new MetadataRepositoryPatternFilter();
		repositoryViewer.setFilters(new ViewerFilter[] {filter});
		// We don't need a deferred content provider because we are caching local results before
		// actually querying
		repositoryViewer.setContentProvider(new ProvElementContentProvider());
		labelProvider = new RepositoryDetailsLabelProvider();
		repositoryViewer.setLabelProvider(labelProvider);

		repositoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validateButtons();
				setDetails();
			}
		});

		// Input last
		repositoryViewer.setInput(getInput());

		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] {URLTransfer.getInstance(), FileTransfer.getInstance()});
		target.addDropListener(new RepositoryManipulatorDropTarget(getRepositoryManipulator(), table));

		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.widthHint = convertHorizontalDLUsToPixels(WIDTH_IN_DLUS);
		data.heightHint = convertVerticalDLUsToPixels(HEIGHT_IN_DLUS);
		table.setLayoutData(data);

		// Vertical buttons
		Composite verticalButtonBar = createVerticalButtonBar(composite);
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.verticalAlignment = SWT.TOP;
		data.verticalIndent = 0;
		verticalButtonBar.setLayoutData(data);
		listener = getViewerProvisioningListener();

		// Details area
		details = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = convertHeightInCharsToPixels(ILayoutConstants.DEFAULT_SITEDETAILS_HEIGHT);

		details.setLayoutData(data);

		ProvUI.addProvisioningListener(listener);
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				ProvUI.removeProvisioningListener(listener);
			}
		});
		Dialog.applyDialogFont(composite);
		validateButtons();
		return composite;
	}

	private Button createVerticalButton(Composite parent, String label, boolean defaultButton) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);

		GridData data = setVerticalButtonLayoutData(button);
		data.horizontalAlignment = GridData.FILL;

		button.setToolTipText(label);
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		return button;
	}

	private GridData setVerticalButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
		return data;
	}

	private void setTableColumns() {
		table.setHeaderVisible(true);
		String[] columnHeaders = {ProvUIMessages.RepositoryManipulationPage_NameColumnTitle, ProvUIMessages.RepositoryManipulationPage_LocationColumnTitle, ProvUIMessages.RepositoryManipulationPage_EnabledColumnTitle};
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(columnHeaders[i]);
			if (i == RepositoryDetailsLabelProvider.COL_ENABLEMENT) {
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_SMALL_COLUMN_WIDTH));
				tc.setAlignment(SWT.CENTER);
			} else if (i == RepositoryDetailsLabelProvider.COL_NAME) {
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_COLUMN_WIDTH) * 2 / 3);
			} else {
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_COLUMN_WIDTH));
			}
			tc.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					columnSelected((TableColumn) e.widget);
				}

				public void widgetSelected(SelectionEvent e) {
					columnSelected((TableColumn) e.widget);
				}

			});
			// First column only
			if (i == 0) {
				table.setSortColumn(tc);
				table.setSortDirection(SWT.UP);
			}
		}
	}

	private Composite createVerticalButtonBar(Composite parent) {
		// Create composite.
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(composite);

		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);

		createVerticalButtons(composite);
		return composite;
	}

	private void createVerticalButtons(Composite parent) {
		addButton = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_Add, false);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				addRepository();
			}
		});

		removeButton = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_Remove, false);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				removeRepositories();
			}
		});

		refreshButton = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_RefreshConnection, false);
		refreshButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refreshRepository();
			}
		});

		disableButton = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_DisableButton, false);
		disableButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				toggleRepositoryEnablement();
			}
		});

		Button button = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_Import, false);
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				importRepositories();
			}
		});

		exportButton = createVerticalButton(parent, ProvUIMessages.RepositoryManipulationPage_Export, false);
		exportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				exportRepositories();
			}
		});
	}

	CachedMetadataRepositories getInput() {
		if (input == null)
			input = new CachedMetadataRepositories();
		return input;
	}

	public boolean performOk() {
		if (changed)
			ElementUtils.updateRepositoryUsingElements(getElements(), getShell());
		return super.performOk();
	}

	private StructuredViewerProvisioningListener getViewerProvisioningListener() {
		return new StructuredViewerProvisioningListener(repositoryViewer, StructuredViewerProvisioningListener.PROV_EVENT_METADATA_REPOSITORY) {
			protected void repositoryDiscovered(RepositoryEvent e) {
				asyncRefresh();
			}

			protected void repositoryChanged(RepositoryEvent e) {
				asyncRefresh();
			}
		};
	}

	MetadataRepositoryElement[] getElements() {
		return (MetadataRepositoryElement[]) getInput().cachedElements.values().toArray(new MetadataRepositoryElement[getInput().cachedElements.size()]);
	}

	MetadataRepositoryElement[] getSelectedElements() {
		Object[] items = ((IStructuredSelection) repositoryViewer.getSelection()).toArray();
		ArrayList list = new ArrayList(items.length);
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof MetadataRepositoryElement)
				list.add(items[i]);
		}
		return (MetadataRepositoryElement[]) list.toArray(new MetadataRepositoryElement[list.size()]);
	}

	void validateButtons() {
		MetadataRepositoryElement[] elements = getSelectedElements();
		exportButton.setEnabled(elements.length > 0);
		removeButton.setEnabled(elements.length > 0);
		refreshButton.setEnabled(elements.length == 1);
		if (elements.length == 1) {
			if (elements[0].isEnabled())
				disableButton.setText(ProvUIMessages.RepositoryManipulationPage_DisableButton);
			else
				disableButton.setText(ProvUIMessages.RepositoryManipulationPage_EnableButton);
			disableButton.setEnabled(true);
		} else {
			disableButton.setText(ProvUIMessages.RepositoryManipulationPage_EnableButton);
			disableButton.setEnabled(false);
		}
	}

	void addRepository() {
		AddRepositoryDialog dialog = new AddRepositoryDialog(getShell(), policy.getQueryContext().getMetadataRepositoryFlags()) {
			protected ProvisioningOperation getOperation(URI repositoryLocation) {
				return RepositoryManipulationPage.this.getRepoAddOperation(repositoryLocation);
			}

			protected RepositoryLocationValidator getRepositoryLocationValidator() {
				return RepositoryManipulationPage.this.getRepositoryLocationValidator();
			}

		};
		dialog.setTitle(manipulator.getAddOperationLabel());
		dialog.open();
	}

	void refreshRepository() {
		final MetadataRepositoryElement[] selected = getSelectedElements();
		final ProvisionException[] fail = new ProvisionException[1];
		if (selected.length != 1)
			return;
		final URI location = selected[0].getLocation();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					SubMonitor mon = SubMonitor.convert(monitor, NLS.bind(ProvUIMessages.RepositoryManipulationPage_ContactingSiteMessage, location), 100);
					try {
						ProvUI.clearRepositoryNotFound(location);
						ProvisioningUtil.refreshArtifactRepositories(new URI[] {location}, mon.newChild(50));
						ProvisioningUtil.refreshMetadataRepositories(new URI[] {location}, mon.newChild(50));
					} catch (ProvisionException e) {
						// Need to report after dialog is closed or the error dialog will disappear when progress
						// disappears
						fail[0] = e;
					}
				}
			});
		} catch (InvocationTargetException e) {
			// nothing to report
		} catch (InterruptedException e) {
			// nothing to report
		}
		if (fail[0] != null) {
			if (fail[0].getStatus().getCode() == ProvisionException.REPOSITORY_NOT_FOUND) {
				ProvUI.reportNotFoundStatus(location, fail[0].getStatus(), StatusManager.SHOW);
			} else
				ProvUI.handleException(fail[0], null, StatusManager.SHOW);
		}
		repositoryViewer.update(selected[0], null);
		setDetails();
	}

	void toggleRepositoryEnablement() {
		MetadataRepositoryElement[] selected = getSelectedElements();
		if (selected.length == 1) {
			selected[0].setEnabled(!selected[0].isEnabled());
			if (comparator.getSortKey() == RepositoryDetailsLabelProvider.COL_ENABLEMENT)
				repositoryViewer.refresh(true);
			else
				repositoryViewer.update(selected[0], null);
			changed = true;
		}
		validateButtons();
	}

	void importRepositories() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				MetadataRepositoryElement[] imported = UpdateManagerCompatibility.importSites(getShell());
				if (imported.length > 0) {
					Hashtable repos = getInput().cachedElements;
					changed = true;
					for (int i = 0; i < imported.length; i++)
						repos.put(imported[i].getLocation().toString(), imported[i]);
					asyncRefresh();
				}
			}
		});
	}

	void exportRepositories() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				MetadataRepositoryElement[] elements = getSelectedElements();
				if (elements.length == 0)
					elements = getElements();
				UpdateManagerCompatibility.exportSites(getShell(), elements);
			}
		});
	}

	void columnSelected(TableColumn tc) {
		TableColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++) {
			if (cols[i] == tc) {
				if (i != comparator.getSortKey()) {
					comparator.setSortKey(i);
					table.setSortColumn(tc);
					comparator.sortAscending();
					table.setSortDirection(SWT.UP);
				} else {
					if (comparator.isAscending()) {
						table.setSortDirection(SWT.DOWN);
						comparator.sortDescending();
					} else {
						table.setSortDirection(SWT.UP);
						comparator.sortAscending();
					}
				}
				repositoryViewer.refresh();
				break;
			}
		}
	}

	void asyncRefresh() {
		display.asyncExec(new Runnable() {
			public void run() {
				repositoryViewer.refresh();
			}
		});
	}

	void applyFilter() {
		String text = pattern.getText();
		if (text == DEFAULT_FILTER_TEXT)
			text = ""; //$NON-NLS-1$
		if (text.length() == 0)
			filter.setPattern(null);
		else
			filter.setPattern(text);
		if (filterJob != null)
			filterJob.cancel();
		filterJob = new WorkbenchJob("filter job") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (!repositoryViewer.getTable().isDisposed())
					repositoryViewer.refresh();
				return Status.OK_STATUS;
			}

		};
		filterJob.setSystem(true);
		filterJob.schedule(FILTER_DELAY);
	}

	void setDetails() {
		MetadataRepositoryElement[] selections = getSelectedElements();
		if (selections.length == 1) {
			details.setText(selections[0].getDescription());
		} else {
			details.setText(""); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
		if (policy == null)
			setPolicy(Policy.getDefault());
	}

	void removeRepositories() {
		MetadataRepositoryElement[] selections = getSelectedElements();
		if (selections.length > 0) {
			String message = ProvUIMessages.RepositoryManipulationPage_RemoveConfirmMessage;
			if (selections.length == 1)
				message = NLS.bind(ProvUIMessages.RepositoryManipulationPage_RemoveConfirmSingleMessage, selections[0].getLocation().toString());
			if (MessageDialog.openQuestion(getShell(), ProvUIMessages.RepositoryManipulationPage_RemoveConfirmTitle, message)) {

				changed = true;
				for (int i = 0; i < selections.length; i++) {
					getInput().cachedElements.remove(selections[i].getLocation().toString());
				}
				asyncRefresh();
			}
		}
	}

	// return a repo manipulator that only operates on the local cache
	RepositoryManipulator getRepositoryManipulator() {
		if (localCacheRepoManipulator == null)
			localCacheRepoManipulator = new RepositoryManipulator() {

				public RepositoryOperation getAddOperation(URI repoLocation) {
					return RepositoryManipulationPage.this.getRepoAddOperation(repoLocation);
				}

				public String getAddOperationLabel() {
					return ProvUIMessages.RepositoryManipulationPage_Add;
				}

				public URI[] getKnownRepositories() {
					return RepositoryManipulationPage.this.getKnownRepositories();
				}

				public String getManipulatorButtonLabel() {
					return ""; //$NON-NLS-1$
				}

				public String getManipulatorLinkLabel() {
					return ""; //$NON-NLS-1$
				}

				public RepositoryOperation getRemoveOperation(URI[] repoLocations) {
					return new RepositoryOperation("Cached remove repo operation", repoLocations) { //$NON-NLS-1$
						protected IStatus doBatchedExecute(IProgressMonitor monitor) throws ProvisionException {
							removeRepositories();
							return Status.OK_STATUS;
						}
					};
				}

				public String getRemoveOperationLabel() {
					return ProvUIMessages.RepositoryManipulationPage_Remove;
				}

				public RepositoryLocationValidator getRepositoryLocationValidator(Shell shell) {
					return RepositoryManipulationPage.this.getRepositoryLocationValidator();
				}

				public boolean manipulateRepositories(Shell shell) {
					// we are the manipulator
					return true;
				}

			};
		return localCacheRepoManipulator;
	}

	URI[] getKnownRepositories() {
		MetadataRepositoryElement[] elements = getElements();
		URI[] locations = new URI[elements.length];
		for (int i = 0; i < elements.length; i++)
			locations[i] = elements[i].getLocation();
		return locations;
	}

	RepositoryLocationValidator getRepositoryLocationValidator() {
		DefaultMetadataURLValidator validator = new DefaultMetadataURLValidator() {
			protected URI[] getKnownLocations() {
				return getKnownRepositories();
			}
		};
		return validator;

	}

	RepositoryOperation getRepoAddOperation(URI location) {
		return new RepositoryOperation("Cached add repo operation", new URI[] {location}) { //$NON-NLS-1$
			protected IStatus doExecute(IProgressMonitor monitor) {
				for (int i = 0; i < locations.length; i++) {
					Hashtable elements = getInput().cachedElements;
					elements.put(locations[i].toString(), new MetadataRepositoryElement(getInput(), locations[i], true));

				}
				changed = true;
				asyncRefresh();
				return Status.OK_STATUS;
			}

			protected IStatus doBatchedExecute(IProgressMonitor monitor) throws ProvisionException {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
}
