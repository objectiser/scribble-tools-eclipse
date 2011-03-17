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
package org.scribble.protocol.designer.keywords;

/**
 * This class provides a default implementation of the keyword provider. Derived
 * classes can supply the list of keywords appropriate for the notation they
 * represent.
 */
public class DefaultKeyWordProvider implements KeyWordProvider {

	/**
	 * This is the constructor for the default keyword provider. This
	 * construct is supplied the list of keywords it represents.
	 * 
	 * @param keywords The list of keywords
	 */
	public DefaultKeyWordProvider(String[] keywords) {
		m_keywords = keywords;
	}
	
	/**
	 * This method returns the list of keywords.
	 * 
	 * @return The list of keywords
	 */
	public String[] getKeyWords() {
		return(m_keywords);
	}

	/**
	 * This method determines whether the supplied value is
	 * a keyword.
	 * 
	 * @return Whether the supplied word is a keyword
	 */
	public boolean isKeyWord(String keyword) {
		boolean ret=false;
		
		for (int i=0; ret == false && i < m_keywords.length; i++) {
			if (keyword.equals(m_keywords[i])) {
				ret = true;
			}
		}
		
		return(ret);
	}
	
	private String[] m_keywords=null;
}
