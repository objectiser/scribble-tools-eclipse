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

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.scribble.protocol.designer.editor.outliner.DefaultModelOutliner;
import org.scribble.protocol.model.ModelProperties;

/**
 * A content outline page which always represents the content of the
 * connected editor in 10 segments.
 */
public class ScribbleContentOutlinePage extends ContentOutlinePage {

	protected class LabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			Image ret=null;

			if (m_outliner != null) {
				ret = m_outliner.getImage(m_reference, element);
			}

			return(ret);
		}

		public String getText(Object element) {
			String ret=null;

			if (m_outliner != null) {
				ret = m_outliner.getLabel(m_reference, element);
			}

			return(ret);
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
		
	}

	/**
	 * Divides the editor's document into ten segments and provides elements for them.
	 */
	protected class ContentProvider implements ITreeContentProvider {

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		/*
		 * @see IContentProvider#dispose
		 */
		public void dispose() {
		}

		/*
		 * @see IContentProvider#isDeleted(Object)
		 */
		public boolean isDeleted(Object element) {
			return false;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object element) {
			Object[] ret=null;
			
			if (m_outliner != null) {
				java.util.List<Object> list=m_outliner.getChildren(m_reference,
									element);
				
				if (list != null) {
					ret = list.toArray();
				}
			} else {
				ret = new Object[0];
			}

			return(ret);
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			boolean ret=false;
			
			if (m_outliner != null) {
				ret = m_outliner.hasChildren(m_reference, element);
			}
			
			return(ret);
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			return(m_model);
		}

		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object element) {
			return(getElements(element));
		}
	}

	protected Object fInput;
	protected IDocumentProvider fDocumentProvider;
	protected ITextEditor fTextEditor;

	/**
	 * Creates a content outline page using the given provider and the given editor.
	 * 
	 * @param provider the document provider
	 * @param editor the editor
	 */
	public ScribbleContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
		fDocumentProvider= provider;
		fTextEditor= editor;
	}
	
	/* (non-Javadoc)
	 * Method declared on ContentOutlinePage
	 */
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);

		if (m_model != null) {
			viewer.setInput(m_model);
		} else if (fInput != null) {
			viewer.setInput(fInput);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on ContentOutlinePage
	 */
	public void selectionChanged(SelectionChangedEvent event) {

		super.selectionChanged(event);

		ISelection selection= event.getSelection();
		if (selection.isEmpty()) {
			fTextEditor.resetHighlightRange();
		} else {
			
			if (((IStructuredSelection) selection).getFirstElement() instanceof 
					org.scribble.protocol.model.ModelObject) {
				org.scribble.protocol.model.ModelObject mobj=
						(org.scribble.protocol.model.ModelObject)
							((IStructuredSelection) selection).getFirstElement();
				
				if (mobj.getProperties().containsKey(ModelProperties.START_LOCATION) &&
						mobj.getProperties().containsKey(ModelProperties.END_LOCATION)) {
					int start=(Integer)mobj.getProperties().get(ModelProperties.START_LOCATION);
					int length=(Integer)mobj.getProperties().get(ModelProperties.END_LOCATION);

					try {
						fTextEditor.setHighlightRange(start, length, true);
					} catch (IllegalArgumentException x) {
						fTextEditor.resetHighlightRange();
					}
				} else {
					fTextEditor.resetHighlightRange();					
				}
			} else {
				fTextEditor.resetHighlightRange();				
			}
		}
	}
	
	/**
	 * Sets the input of the outline page
	 * 
	 * @param input the input of this outline page
	 */
	public void setInput(Object input) {
		fInput= input;
		update();
	}
	
	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer= getTreeViewer();

		m_outliner = null;
		m_reference = null;
		m_model = null;
		
		if (fInput instanceof org.eclipse.ui.IFileEditorInput) {
			//org.eclipse.ui.IFileEditorInput fi=
			//		(org.eclipse.ui.IFileEditorInput)fInput;
			
			// TODO: Need to parse model - will need to determine
			// which notation, and select the appropriate parser
			
			m_outliner = new DefaultModelOutliner();			
		}
		
		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				
				control.setRedraw(false);

				if (m_model != null) {
					viewer.setInput(m_model);
				} else {
					
					viewer.setInput(fInput);
				}
				
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	private org.scribble.protocol.model.ProtocolModel m_model=null;
	private org.scribble.protocol.model.ProtocolReference m_reference=null;
	private org.scribble.protocol.designer.editor.outliner.ModelOutliner m_outliner=null;
}
