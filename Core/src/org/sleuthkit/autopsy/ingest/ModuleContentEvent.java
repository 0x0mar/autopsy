/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
 */
package org.sleuthkit.autopsy.ingest;

import javax.swing.event.ChangeEvent;
import org.sleuthkit.datamodel.Content;

/**
 * Event data that are fired off by ingest modules when they changed or added
 * new content.
 */
public class ModuleContentEvent extends ChangeEvent {

    private String moduleName = "";

    /**
     * Create a new event passing content that has changed
     *
     * @param content
     */
    public ModuleContentEvent(Content content) {
        super(content);
    }

    /**
     * Create a new event passing content that has changed
     *
     * @param content
     */
    public ModuleContentEvent(String moduleName, Content content) {
        super(content);
        this.moduleName = moduleName;
    }

    /**
     * Gets the module name that changed the content and fired the event.
     *
     * @return The module name as a string. May be empty.
     */
    public String getModuleName() {
        return moduleName;
    }
}
