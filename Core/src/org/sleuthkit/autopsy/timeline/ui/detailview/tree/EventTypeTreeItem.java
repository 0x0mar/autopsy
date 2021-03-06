/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-14 Basis Technology Corp.
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
package org.sleuthkit.autopsy.timeline.ui.detailview.tree;

import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import org.sleuthkit.autopsy.timeline.datamodel.EventBundle;

class EventTypeTreeItem extends NavTreeItem {

    /**
     * maps a description to the child item of this item with that description
     */
    private final Map<String, EventDescriptionTreeItem> childMap = new ConcurrentHashMap<>();

    private final Comparator<TreeItem<EventBundle<?>>> comparator = TreeComparator.Description;

    EventTypeTreeItem(EventBundle<?> g) {
        setValue(g);
    }

    @Override
    public long getCount() {
        return getValue().getCount();
    }

    public void insert(Deque<EventBundle<?>> path) {
        EventBundle<?> head = path.removeFirst();
        EventDescriptionTreeItem treeItem = childMap.get(head.getDescription());
        if (treeItem == null) {
            treeItem = new EventDescriptionTreeItem(head);
            treeItem.setExpanded(true);
            childMap.put(head.getDescription(), treeItem);
            getChildren().add(treeItem);
            FXCollections.sort(getChildren(), comparator);
        }

        if (path.isEmpty() == false) {
            treeItem.insert(path);
        }
    }

    @Override
    public NavTreeItem findTreeItemForEvent(EventBundle<?> t) {
        if (t.getEventType().getBaseType() == getValue().getEventType().getBaseType()) {

            for (EventDescriptionTreeItem child : childMap.values()) {
                final NavTreeItem findTreeItemForEvent = child.findTreeItemForEvent(t);
                if (findTreeItemForEvent != null) {
                    return findTreeItemForEvent;
                }
            }
        }
        return null;
    }

    @Override
    public void resort(Comparator<TreeItem<EventBundle<?>>> comp) {
        FXCollections.sort(getChildren(), comp);
    }
}
