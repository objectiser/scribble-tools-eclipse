/*
 * Copyright 2009 www.scribble.org
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
package org.scribble.protocol.designer.logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.scribble.common.logging.Journal;

public class EclipseScribbleLogger implements Journal {

	public EclipseScribbleLogger(IFile file) {
		m_file = file;
	}

	public void debug(String issue, java.util.Map<String,Object> props) {
		// TODO Auto-generated method stub
		
	}

	public void error(String issue, java.util.Map<String,Object> props) {
		reportIssue(issue, ReportEntry.ERROR_TYPE, props);
		m_errorOccurred = true;
	}

	public boolean hasErrorOccurred() {
		return(m_errorOccurred);
	}
	
	public void info(String issue, java.util.Map<String,Object> props) {
		reportIssue(issue, ReportEntry.INFORMATION_TYPE, props);
	}

	public void trace(String issue, java.util.Map<String,Object> props) {
		// TODO Auto-generated method stub
		
	}

	public void warning(String issue, java.util.Map<String,Object> props) {
		reportIssue(issue, ReportEntry.WARNING_TYPE, props);
	}
	
	protected void reportIssue(String issue, int issueType, java.util.Map<String,Object> props) {
		
		if (m_file != null) {
	
			synchronized(m_entries) {
				m_entries.add(new ReportEntry(issue, issueType, props));
			}
			
			if (m_finished) {
				// Publish immediately
				finished();
			}
		}
	}			
	
	public void finished() {
		org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				
				if (m_file != null && m_file.exists()) {
					
					// Clear current markers
					try {
						synchronized(m_entries) {
							
							if (m_finished == false) {
								m_file.deleteMarkers(ScribbleMarker.SCRIBBLE_PROBLEM, true,
										IFile.DEPTH_INFINITE);
								m_finished = true;
							}
						
							// Update the markers
							for (int i=0; i < m_entries.size(); i++) {
								ReportEntry re=(ReportEntry)m_entries.get(i);
								
								if (m_reported.contains(re) == false) {
									createMarker(re.getIssue(), re.getType(),
										re.getProperties());
									
									m_reported.add(re);
								}
							}
							
							m_entries.clear();
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	protected void createMarker(String mesg, int type,
					java.util.Map<String,Object> props) {
					
		// Create marker for message
		try {
			IMarker marker=m_file.createMarker(ScribbleMarker.SCRIBBLE_PROBLEM);
			
			// Initialize the attributes on the marker
			marker.setAttribute(IMarker.MESSAGE, mesg);
			
			if (type == ReportEntry.ERROR_TYPE) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} else if (type == ReportEntry.WARNING_TYPE) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} else if (type == ReportEntry.INFORMATION_TYPE) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			}
			
			derivePosition(m_file, marker, props);

		} catch(Exception e) {
			
			// TODO: report error
			e.printStackTrace();
		}
	}
	
	protected void derivePosition(IFile file, IMarker marker, java.util.Map<String,Object> props)
							throws Exception {
		int endMarkerAfter=-1;
		String contents=null;
		
		if (props.containsKey(START_POSITION)) {
			marker.setAttribute(IMarker.CHAR_START, (Integer)props.get(START_POSITION));
			
			if (props.containsKey(END_POSITION)) {
				marker.setAttribute(IMarker.CHAR_END, (Integer)props.get(END_POSITION));
			} else {
				endMarkerAfter = (Integer)props.get(START_POSITION);
			}
		} else if (props.containsKey(START_LINE)) {
			int pos=-1;
			
			contents = getContents(file);
			
			if (contents != null) {
				pos = 0;
				
				int curline=1;
				int line=(Integer)props.get(START_LINE);
				
				while (curline < line) {
					// Find next end of line
					int nextPos=contents.indexOf('\n', pos);
					
					curline++;
					pos = nextPos+1;
				}
				
				if (props.containsKey(START_COLUMN)) {
					pos += (Integer)props.get(START_COLUMN);
				}
			}
			
			marker.setAttribute(IMarker.CHAR_START, pos);

			if (props.containsKey(END_LINE)) {
				if (contents != null) {
					pos = 0;
					
					int curline=1;
					int line=(Integer)props.get(END_LINE);
					
					while (curline < line) {
						// Find next end of line
						int nextPos=contents.indexOf('\n', pos);
						
						curline++;
						pos = nextPos+1;
					}
					
					if (props.containsKey(END_COLUMN)) {
						pos += (Integer)props.get(END_COLUMN);
					}
					
					marker.setAttribute(IMarker.CHAR_END, pos);
				}
			} else {
				endMarkerAfter = pos;
			}
		}
		
		if (endMarkerAfter != -1) {
			if (contents == null) {
				contents = getContents(file);
			}
			
			// Find next whitespace after this position
			int nextPos=endMarkerAfter;
			
			while (nextPos < contents.length() && !Character.isWhitespace(contents.charAt(nextPos))) {
				nextPos++;
			}
			
			if (nextPos != -1) {
				marker.setAttribute(IMarker.CHAR_END, nextPos);
			}
		}
	}
	
	protected String getContents(IFile file) throws Exception {
		java.io.InputStream is=file.getContents();
		byte[] b=new byte[is.available()];
		is.read(b);
		is.close();
		
		return(new String(b));
	}

	private IFile m_file=null;
	private boolean m_finished=false;
	private boolean m_errorOccurred=false;
	private java.util.Vector<ReportEntry> m_entries=new java.util.Vector<ReportEntry>();
	private java.util.Vector<ReportEntry> m_reported=new java.util.Vector<ReportEntry>();
	
	/**
	 * This is a simple data container class to hold the
	 * information reported during validation.
	 *
	 */
	public class ReportEntry {
		
		public static final int ERROR_TYPE=0;
		public static final int WARNING_TYPE=1;
		public static final int INFORMATION_TYPE=2;
		
		private String m_issue=null;
		private int m_type=0;
		private java.util.Map<String, Object> m_properties=null;

		public ReportEntry(String issue, int type,
					java.util.Map<String, Object> props) {
			m_issue = issue;
			m_type = type;
			m_properties = props;
		}
		
		public String getIssue() {
			return(m_issue);
		}
		
		public int getType() {
			return(m_type);
		}
		
		public java.util.Map<String,Object> getProperties() {
			return(m_properties);
		}
	}
}
