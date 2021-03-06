/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011-2014 Basis Technology Corp.
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

/**
 * This visitor goes over the AutopsyVisitableItems, which are currently the
 * nodes in the tree that are structural and not nodes that are from
 * Sleuthkit-based data model objects.
 */
interface AutopsyItemVisitor<T> {

    T visit(DataSources i);

    T visit(Views v);

    T visit(FileTypeExtensionFilters sf);

    T visit(FileTypeExtensionFilters.RootFilter fsf);

    T visit(FileTypeExtensionFilters.DocumentFilter df);

    T visit(FileTypeExtensionFilters.ExecutableFilter ef);

    T visit(RecentFiles rf);

    T visit(RecentFiles.RecentFilesFilter rff);

    T visit(DeletedContent dc);

    T visit(DeletedContent.DeletedContentFilter dcf);

    T visit(FileSize fs);

    T visit(FileSize.FileSizeFilter fsf);

    T visit(ExtractedContent ec);

    T visit(KeywordHits kh);

    T visit(HashsetHits hh);

    T visit(EmailExtracted ee);

    T visit(InterestingHits ih);

    T visit(Results r);

    T visit(Tags tagsNodeKey);

    T visit(Reports reportsItem);

    static abstract public class Default<T> implements AutopsyItemVisitor<T> {

        protected abstract T defaultVisit(AutopsyVisitableItem ec);

        @Override
        public T visit(ExtractedContent ec) {
            return defaultVisit(ec);
        }

        @Override
        public T visit(FileTypeExtensionFilters sf) {
            return defaultVisit(sf);
        }

        @Override
        public T visit(FileTypeExtensionFilters.RootFilter fsf) {
            return defaultVisit(fsf);
        }

        @Override
        public T visit(FileTypeExtensionFilters.DocumentFilter df) {
            return defaultVisit(df);
        }

        @Override
        public T visit(FileTypeExtensionFilters.ExecutableFilter ef) {
            return defaultVisit(ef);
        }

        @Override
        public T visit(DeletedContent dc) {
            return defaultVisit(dc);
        }

        @Override
        public T visit(DeletedContent.DeletedContentFilter dcf) {
            return defaultVisit(dcf);
        }

        @Override
        public T visit(FileSize fs) {
            return defaultVisit(fs);
        }

        @Override
        public T visit(FileSize.FileSizeFilter fsf) {
            return defaultVisit(fsf);
        }

        @Override
        public T visit(RecentFiles rf) {
            return defaultVisit(rf);
        }

        @Override
        public T visit(RecentFiles.RecentFilesFilter rff) {
            return defaultVisit(rff);
        }

        @Override
        public T visit(KeywordHits kh) {
            return defaultVisit(kh);
        }

        @Override
        public T visit(HashsetHits hh) {
            return defaultVisit(hh);
        }

        @Override
        public T visit(InterestingHits ih) {
            return defaultVisit(ih);
        }

        @Override
        public T visit(EmailExtracted ee) {
            return defaultVisit(ee);
        }

        @Override
        public T visit(Tags tagsNodeKey) {
            return defaultVisit(tagsNodeKey);
        }

        @Override
        public T visit(DataSources i) {
            return defaultVisit(i);
        }

        @Override
        public T visit(Views v) {
            return defaultVisit(v);
        }

        @Override
        public T visit(Results r) {
            return defaultVisit(r);
        }

        @Override
        public T visit(Reports reportsItem) {
            return defaultVisit(reportsItem);
        }
    }
}
