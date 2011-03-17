/*
 * Copyright 2009 www.scribble.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.scribble.protocol.designer.editor;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.core.resources.*;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

import org.eclipse.core.runtime.Path;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.scribble.protocol.designer.DesignerServices;
import org.scribble.protocol.model.ProtocolReference;

/**
 * This class provides the wizard responsible for creating
 * new Protocol Global definitions.
 */
public class NewProtocolWizard extends Wizard implements INewWizard {

	/**
     * This method initializes the wizard.
     * 
     * @param workbench The workbench
     * @param selection The selected resource
     */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_workbench = workbench;
		m_selection = selection;
        setWindowTitle("New Scribble Wizard");
	}
	
	/**
	 * This method is invoked when the new CDL object model
	 * should be created.
	 */
	public boolean performFinish() {
		try {
			// Remember the file.
			//
			final IFile modelFile = getModelFile();

			// Do the work within an operation.
			//
			WorkspaceModifyOperation operation =
				new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor progressMonitor) {
						try {
							
							// Check that project has scribble nature
							/*
							IProject proj=modelFile.getProject();
							if (proj.hasNature("org.scribble.nature") == false) {
								IProjectDescription description = proj.getDescription();
								String[] natures = description.getNatureIds();
								String[] newNatures = new String[natures.length + 1];
								System.arraycopy(natures, 0, newNatures, 0, natures.length);
								newNatures[natures.length] = "org.scribble.nature";
								description.setNatureIds(newNatures);
								proj.setDescription(description, null);
							}
							*/
							
							byte[] b=new byte[0];
							
							// Identify the model reference from the resource
							org.eclipse.core.runtime.IPath path=modelFile.getFullPath();
							org.eclipse.core.runtime.IPath fqnPath=path.removeFirstSegments(1);
							
							String[] segments=fqnPath.segments();
							
							String local=segments[segments.length-1];
							
							String namespace="";
							
							for (int i=0; i < segments.length-1; i++) {
								if (i > 0) {
									namespace += ".";
								}
								namespace += segments[i];
							}
							
							//String type=null;
							String located=null;
							
							if (local != null) {
								int nindex=local.lastIndexOf('.');
								if (nindex != -1) {
									//type = local.substring(nindex+1);
									local = local.substring(0, nindex);

									int pindex=local.lastIndexOf(ProtocolReference.LOCATED_REFERENCE_SEPARATOR);
									if (pindex != -1) {
										located = local.substring(pindex+1);
										local = local.substring(0, pindex);
									}
								}
							}
							
							String name=namespace;
							
							if (name.length() > 0) {
								name += '.';
							}
							
							name += local;
							
							if (located != null) {
								name += ProtocolReference.LOCATED_REFERENCE_SEPARATOR + located;
							}
							
							ProtocolReference ref=new ProtocolReference(name);
							
							String initDesc=getInitialDescription(ref);
							
							if (initDesc != null) {
								b = initDesc.getBytes();
							} else {
								b = "".getBytes();
							}
							
							java.io.ByteArrayInputStream bis=new java.io.ByteArrayInputStream(b);
							
							modelFile.create(bis, true, progressMonitor);
							
							bis.close();
							
						} catch (Exception e) {
							org.scribble.protocol.designer.osgi.Activator.logError(e.getMessage(), e);
						} finally {
							progressMonitor.done();
						}
					}
				};

			getContainer().run(false, false, operation);

			// Select the new file resource in the current view.
			//
			IWorkbenchWindow workbenchWindow =
			    m_workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(modelFile);
				getShell().getDisplay().asyncExec
					(new Runnable() {
						 public void run() {
							 ((ISetSelectionTarget)activePart).selectReveal(targetSelection);
						 }
					 });
			}

			// Open an editor on the new file.
			//
			try {
				org.eclipse.ui.IEditorDescriptor ed=
					m_workbench.getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString());
				
				if (ed != null) {
					page.openEditor(new FileEditorInput(modelFile),
										ed.getId());
				}
			}
			catch (PartInitException exception) {
				MessageDialog.openError(workbenchWindow.getShell(),
						"Open Error", exception.getMessage());
				return false;
			}

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			org.scribble.protocol.designer.osgi.Activator.logError(e.getMessage(), e);
			return false;
		}
	}
	
	protected String getInitialDescription(ProtocolReference ref) {
		String ret="";
		
		// TODO: Add default description
		
		return(ret);
	}

    /**
     * Get the file from the page.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public IFile getModelFile() {
        return m_newFileCreationPage.getModelFile();
    }

    /**
     * The framework calls this to create the contents of the wizard.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void addPages() {
    	
        m_newFileCreationPage = new ScribbleNewFileCreationPage("Whatever", m_selection);
        m_newFileCreationPage.setTitle("Protocol");
        m_newFileCreationPage.setDescription("Create a new Protocol");

        addPage(m_newFileCreationPage);
        
        initFileCreationPage();
    }
    
    protected void initFileCreationPage() {
        String defaultModelBaseFilename = "My";
        
        String defaultModelFilenameExtension = DesignerServices.PROTOCOL_FILE_EXTENSION;
        String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension;

        // Create a page, set the title, and the initial model file name.
        //
        m_newFileCreationPage.setFileName(modelFilename);

        // Try and get the resource selection to determine a current directory for the file dialog.
        //
        if (m_selection != null && !m_selection.isEmpty()) {
            // Get the resource...
            //
            Object selectedElement = m_selection.iterator().next();
            if (selectedElement instanceof IResource) {
                // Get the resource parent, if its a file.
                //
                IResource selectedResource = (IResource)selectedElement;
                if (selectedResource.getType() == IResource.FILE) {
                    selectedResource = selectedResource.getParent();
                }

                // This gives us a directory...
                //
                if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
                    // Set this for the container.
                    //
                    m_newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());

                    // Make up a unique new name here.
                    //
                    for (int i = 1; ((IContainer)selectedResource).findMember(modelFilename) != null; ++i) {
                        modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension;
                    }
                    m_newFileCreationPage.setFileName(modelFilename);
                }
            }
        }
    }

    private IWorkbench m_workbench=null;
	private IStructuredSelection m_selection=null;
	private ScribbleNewFileCreationPage m_newFileCreationPage=null;

	/**
     * This is the one page of the wizard.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public class ScribbleNewFileCreationPage extends WizardNewFileCreationPage {
        /**
         * Remember the model file.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected IFile modelFile;
    
        /**
         * Pass in the selection.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public ScribbleNewFileCreationPage(String pageId, IStructuredSelection selection) {
            super(pageId, selection);
        }
    
        /**
         * The framework calls this to see if the file is correct.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected boolean validatePage() {
            if (super.validatePage()) {
                String requiredExt = DesignerServices.PROTOCOL_FILE_EXTENSION;
            	
                String enteredExt = new Path(getFileName()).getFileExtension();
                if (enteredExt == null || !enteredExt.equals(requiredExt)) {
                    setErrorMessage("The filename must end in: "+requiredExt);
                    return false;
                }
                else {
                    return true;
                }
            }
            else {
                return false;
            }
        }
    
        /**
         * Store the dialog field settings upon completion.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public boolean performFinish() {
            modelFile = getModelFile();
            return true;
        }
    
        /**
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public IFile getModelFile() {
            return
                modelFile == null ?
                    ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName())) :
                    modelFile;
        }
    }

}
