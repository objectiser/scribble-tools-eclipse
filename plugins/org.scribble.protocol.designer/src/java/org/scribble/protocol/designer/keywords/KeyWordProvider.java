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
 * This interface represents a keyword provider associated with a
 * particular notation. The keyword provider is responsible for returning
 * information about the valid keywords associated with a particular
 * Scribble notation.
 */
public interface KeyWordProvider {

	/**
	 * This method returns the list of keywords.
	 * 
	 * @return The list of keywords
	 */
	public String[] getKeyWords();
	
	/**
	 * This method determines whether the supplied value is
	 * a keyword.
	 * 
	 * @return Whether the supplied word is a keyword
	 */
	public boolean isKeyWord(String keyword);
	
}
