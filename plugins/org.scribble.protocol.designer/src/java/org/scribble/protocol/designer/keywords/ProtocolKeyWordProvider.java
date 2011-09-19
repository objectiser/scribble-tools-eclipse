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
 * The protocol notation keyword provider.
 */
public class ProtocolKeyWordProvider extends DefaultKeyWordProvider {
    
    /**
     * The default constructor.
     */
    public ProtocolKeyWordProvider() {
        super(PROTOCOL_KEYWORDS);
    }
    
    private static final String[] PROTOCOL_KEYWORDS={
        "import",
        "protocol",
        "role",
        "introduces",
        "at",
        "from",
        "to",
        "unordered",
        "parallel",
        "and",
        "choice",
        "or",
        "repeat",
        "run",
        "inline",
        "do",
        "interrupt",
        "end"
    };
}
