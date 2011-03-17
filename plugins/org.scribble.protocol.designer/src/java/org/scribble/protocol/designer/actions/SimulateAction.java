/*
 * Copyright 2009-10 www.scribble.org
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
package org.scribble.protocol.designer.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.scribble.command.simulate.SimulateCommand;
import org.scribble.common.logging.CachedJournal;
import org.scribble.protocol.designer.DesignerServices;
import org.scribble.protocol.designer.osgi.Activator;
import org.scribble.protocol.model.ProtocolModel;

public class SimulateAction implements IObjectActionDelegate {

	private ISelection m_selection=null;
    private IWorkbenchPart m_targetPart=null;

	public void run(IAction arg0) {
		if (m_selection instanceof StructuredSelection) {
			StructuredSelection sel=(StructuredSelection)m_selection;
			
			IResource res=(IResource)sel.getFirstElement();
			
			// Obtain the protocol from the resource
			if (res instanceof IFile) {

				ProtocolModel model=null;
				
				try {
					CachedJournal journal=new CachedJournal();
					
					java.io.InputStream is=((IFile)res).getContents();
			
					model = DesignerServices.getParserManager().parse(((IFile)res).getFileExtension(),
											is, journal, null);
			
					is.close();
					
					if (model == null || journal.hasErrors()) {
						error("Cannot simulate '"+((IFile)res).getName()+"' due to errors", null);
					} else if (model.isLocated() == false) {
						error("Cannot simulate '"+((IFile)res).getName()+"' as not a local model", null);
					} else {
						simulate(model, (IFile)res);
					}
				} catch(Exception e) {
					error("Failed to parse file '"+((IFile)res).getName()+"'", e);
				}
			}
		}
	}
    
    protected void error(String mesg, Throwable exc) {
		Activator.logError(mesg, exc);

		MessageBox mbox=new MessageBox(m_targetPart.getSite().getShell(),
				SWT.ICON_ERROR|SWT.OK);
		
		if (mesg == null) {
			mesg = "Null pointer exception has occurred";
		}

		mbox.setMessage(mesg);
		mbox.open();

    }
    
    protected void simulate(ProtocolModel pm, IFile protocolFile) {
		FileDialog dialog=new FileDialog(m_targetPart.getSite().getShell());
		
		dialog.setFilterPath(protocolFile.getParent().getLocation().toOSString());
		
		String filename=dialog.open();
		
		if (filename != null) {
			
			if (filename.endsWith(".events")) {
				File f=new File(filename);
				
				if (f.exists()) {
					SimulateCommand simulate=new SimulateCommand();
					
					CachedJournal journal=new CachedJournal();
					
					simulate.setJournal(journal);
					simulate.setProtocolExportManager(DesignerServices.getProtocolExportManager());
					simulate.setProtocolMonitor(DesignerServices.getProtocolMonitor());
					
					// Not required when directly invoking simulate
					//simulate.setProtocolParser(DesignerServices.getProtocolParser());
					
					simulate.simulate(pm, f);
					
					StringBuffer buf=new StringBuffer();
					
					for (CachedJournal.IssueDetails issue : journal.getIssues()) {
						buf.append(issue.getIssueType());
						buf.append(": ");
						buf.append(issue.getMessage());
						buf.append("\r\n");
					}
					
					MessageBox mbox=new MessageBox(m_targetPart.getSite().getShell(),
							journal.hasErrors()?SWT.ICON_ERROR:
								journal.hasWarnings()?SWT.ICON_WARNING:
									SWT.ICON_INFORMATION|SWT.OK);
					
					mbox.setMessage(buf.toString());
					mbox.open();
				} else {
					error("File '"+filename+"' does not exist", null);
				}
			} else {
				error("Must select an .events file", null);
			}
		}
    }

	public void selectionChanged(IAction arg0, ISelection selection) {
		m_selection = selection;
	}

	public void setActivePart(IAction arg0, IWorkbenchPart targetPart) {
		m_targetPart = targetPart;
	}

}
