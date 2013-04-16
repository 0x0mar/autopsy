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
package org.sleuthkit.autopsy.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.sleuthkit.autopsy.coreutils.Logger;
import static org.sleuthkit.autopsy.datamodel.DeletedContent.DeletedContentFilter.DELETED_FILES_FILTER;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.ContentVisitor;
import org.sleuthkit.datamodel.Directory;
import org.sleuthkit.datamodel.File;
import org.sleuthkit.datamodel.FsContent;
import org.sleuthkit.datamodel.LayoutFile;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * deleted content view nodes
 */
public class DeletedContent implements AutopsyVisitableItem {

    private SleuthkitCase skCase;

    public enum DeletedContentFilter implements AutopsyVisitableItem {

        DELETED_FILES_FILTER(0, "DELETED_FILES_FILTER", "Deleted Files"),
        ORPHAN_FILES_FILTER(1, "ORPHAN_FILES_FILTER", "Orphan Files"),
        UNALLOC_CONTENT_FILTER(2, "UNALLOC_CONTENT_FILTER", "Unallocated Content"),
        CARVED_CONTENT_FILTER(3, "Carved_CONTENT_FILTER", "Carved Content"),
        UNUSED_FILES_FILTER(4, "UNUSED_FILES_FILTER", "Unused Files"),
        UNUSED_BLOCKS_FILTER(5, "UNUSED_BLOCKS_FILTER", "Unused Blocks");
    
        private int id;
        private String name;
        private String displayName;

        private DeletedContentFilter(int id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;

        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public <T> T accept(AutopsyItemVisitor<T> v) {
            return v.visit(this);
        }
    }

    public DeletedContent(SleuthkitCase skCase) {
        this.skCase = skCase;
    }

    @Override
    public <T> T accept(AutopsyItemVisitor<T> v) {
        return v.visit(this);
    }

    public SleuthkitCase getSleuthkitCase() {
        return this.skCase;
    }
}

class DeletedContentsNode extends DisplayableItemNode {

    private static final String NAME = "Deleted Content";
    private SleuthkitCase skCase;

    DeletedContentsNode(SleuthkitCase skCase) {
        super(Children.create(new DeletedContentsChildren(skCase), true), Lookups.singleton(NAME));
        super.setName(NAME);
        super.setDisplayName(NAME);
        this.skCase = skCase;
        this.setIconBaseWithExtension("org/sleuthkit/autopsy/images/file-icon-deleted.png");
    }

    @Override
    public TYPE getDisplayableItemNodeType() {
        return TYPE.META;
    }

    @Override
    public <T> T accept(DisplayableItemNodeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        ss.put(new NodeProperty("Name",
                "Name",
                "no description",
                NAME));
        return s;
    }
}

class DeletedContentsChildren extends ChildFactory<DeletedContent.DeletedContentFilter> {

    private SleuthkitCase skCase;

    public DeletedContentsChildren(SleuthkitCase skCase) {
        this.skCase = skCase;

    }

    @Override
    protected boolean createKeys(List<DeletedContent.DeletedContentFilter> list) {
        list.addAll(Arrays.asList(DeletedContent.DeletedContentFilter.values()));
        return true;
    }

    @Override
    protected Node createNodeForKey(DeletedContent.DeletedContentFilter key) {
        return new DeletedContentNode(skCase, key);
    }
}

class DeletedContentNode extends DisplayableItemNode {

    private SleuthkitCase skCase;
    private DeletedContent.DeletedContentFilter filter;
    private final static Logger logger = Logger.getLogger(DeletedContentNode.class.getName());

    DeletedContentNode(SleuthkitCase skCase, DeletedContent.DeletedContentFilter filter) {
        super(Children.create(new DeletedContentChildren(filter, skCase), true), Lookups.singleton(filter.getDisplayName()));
        super.setName(filter.getName());
        super.setDisplayName(filter.getDisplayName());
        this.skCase = skCase;
        this.filter = filter;

        String tooltip = filter.getDisplayName();
        this.setShortDescription(tooltip);
        this.setIconBaseWithExtension("org/sleuthkit/autopsy/images/file-icon-deleted.png");
    }

    @Override
    public <T> T accept(DisplayableItemNodeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        ss.put(new NodeProperty("Filter Type",
                "Filter Type",
                "no description",
                filter.getDisplayName()));

        return s;
    }

    @Override
    public TYPE getDisplayableItemNodeType() {
        return TYPE.META;
    }

    @Override
    public boolean isLeafTypeNode() {
        return true;
    }
}

class DeletedContentChildren extends ChildFactory<AbstractFile> {

    private SleuthkitCase skCase;
    private DeletedContent.DeletedContentFilter filter;
    private final Logger logger = Logger.getLogger(DeletedContentChildren.class.getName());

    DeletedContentChildren(DeletedContent.DeletedContentFilter filter, SleuthkitCase skCase) {
        this.skCase = skCase;
        this.filter = filter;
    }

    @Override
    protected boolean createKeys(List<AbstractFile> list) {
        list.addAll(runFsQuery());
        return true;
    }

    private String makeQuery() {
        String query = null;
        switch (filter) {       
            case DELETED_FILES_FILTER:
                query = "dir_flags = " + TskData.TSK_FS_NAME_FLAG_ENUM.UNALLOC.getValue()
                        + " AND type != " + TskData.TSK_DB_FILES_TYPE_ENUM.UNALLOC_BLOCKS.getFileType();
                break;
            case UNUSED_FILES_FILTER:
                query = "meta_flags = " + TskData.TSK_FS_META_FLAG_ENUM.UNUSED.getValue();
                break;
            case ORPHAN_FILES_FILTER:
                query = "meta_flags = " + TskData.TSK_FS_META_FLAG_ENUM.ORPHAN.getValue();
                break;
            case UNALLOC_CONTENT_FILTER:
                query = "type = " + TskData.TSK_DB_FILES_TYPE_ENUM.UNALLOC_BLOCKS.getFileType()
                        + " OR meta_flags = " + TskData.TSK_FS_META_FLAG_ENUM.UNALLOC.getValue();
                break;
            case UNUSED_BLOCKS_FILTER:
                query = "type = " + TskData.TSK_DB_FILES_TYPE_ENUM.UNUSED_BLOCKS.getFileType();
                break;
            case CARVED_CONTENT_FILTER:
                query = "type = " + TskData.TSK_DB_FILES_TYPE_ENUM.CARVED.getFileType();
                break;
            default:
                logger.log(Level.SEVERE, "Unsupported filter type to get deleted content: " + filter);

        }

        return query;
    }

    private List<AbstractFile> runFsQuery() {
        List<AbstractFile> ret = new ArrayList<AbstractFile>();

        String query = makeQuery();
        try {
            ret = skCase.findAllFilesWhere(query);
        } catch (TskCoreException e) {
            logger.log(Level.SEVERE, "Error getting files for the deleted content view using: " + query, e);
        }

        return ret;

    }

    @Override
    protected Node createNodeForKey(AbstractFile key) {
        return key.accept(new ContentVisitor.Default<AbstractNode>() {
            public FileNode visit(AbstractFile f) {
                return new FileNode(f, false);
            }

            public FileNode visit(FsContent f) {
                return new FileNode(f, false);
            }

            @Override
            public FileNode visit(LayoutFile f) {
                return new FileNode(f, false);
            }

            @Override
            public FileNode visit(File f) {
                return new FileNode(f, false);
            }

            @Override
            public FileNode visit(Directory f) {
                return new FileNode(f, false);
            }

            @Override
            protected AbstractNode defaultVisit(Content di) {
                throw new UnsupportedOperationException("Not supported for this type of Displayable Item: " + di.toString());
            }
        });
    }
}