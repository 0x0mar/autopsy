/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
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
package org.sleuthkit.autopsy.keywordsearch;

import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.windows.TopComponent;

public class KeywordSearchSimpleTopComponent extends TopComponent implements KeywordSearchTopComponentInterface {

    private Logger logger = Logger.getLogger(KeywordSearchSimpleTopComponent.class.getName());

    /** Creates new form KeywordSearchSimpleTopComponent */
    public KeywordSearchSimpleTopComponent() {
        initComponents();
        customizeComponents();
        setName("Simple");
        searchButton.setEnabled(false);

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
    }

    private void customizeComponents() {
        searchButton.setToolTipText("Execute a keyword search using the query specified.");
        chRegex.setToolTipText("Select if keyword is a regular expression");
        queryTextField.setToolTipText("<html>For non-regex search enter one or more keywords separated by white-space.<br />"
                + "For a regular expression search, enter a valid regular expression.<br />"
                + "Examples (in double-quotes): \"\\d\\d\\d-\\d\\d\\d\" \\d{8,10} \"phone\" \"ftp|sftp|ssh|http|https|www\".<br />"
                + "Note: a word can be also searched using a regex search.<br />Regex containing whitespace [ \\s] matches are currently not supported.</html>");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchButton = new javax.swing.JButton();
        filesIndexedNameLabel = new javax.swing.JLabel();
        filesIndexedValLabel = new javax.swing.JLabel();
        chRegex = new javax.swing.JCheckBox();
        queryTextField = new javax.swing.JTextField();

        setAutoscrolls(true);

        searchButton.setText(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchSimpleTopComponent.searchButton.text")); // NOI18N

        filesIndexedNameLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchSimpleTopComponent.filesIndexedNameLabel.text")); // NOI18N

        filesIndexedValLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchSimpleTopComponent.filesIndexedValLabel.text")); // NOI18N

        chRegex.setText(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchSimpleTopComponent.chRegex.text")); // NOI18N
        chRegex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chRegexActionPerformed(evt);
            }
        });

        queryTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        queryTextField.setText(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchSimpleTopComponent.queryTextField.text")); // NOI18N
        queryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chRegex)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filesIndexedNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filesIndexedValLabel))
                    .addComponent(searchButton)
                    .addComponent(queryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(queryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chRegex)
                .addGap(18, 18, 18)
                .addComponent(searchButton)
                .addGap(49, 49, 49)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filesIndexedNameLabel)
                    .addComponent(filesIndexedValLabel))
                .addContainerGap(87, Short.MAX_VALUE))
        );

        filesIndexedNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchTopComponent.filesIndexedNameLabel.AccessibleContext.accessibleName")); // NOI18N
        filesIndexedValLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(KeywordSearchSimpleTopComponent.class, "KeywordSearchTopComponent.filesIndexedValLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chRegexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chRegexActionPerformed

    }//GEN-LAST:event_chRegexActionPerformed

    private void queryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryTextFieldActionPerformed

    }//GEN-LAST:event_queryTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chRegex;
    private javax.swing.JLabel filesIndexedNameLabel;
    private javax.swing.JLabel filesIndexedValLabel;
    private javax.swing.JTextField queryTextField;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void componentOpened() {
        // clear old search
        queryTextField.setText("");
    }

    @Override
    public boolean isMultiwordQuery() {
        return false;
    }

    @Override
    public void addSearchButtonListener(ActionListener l) {
        searchButton.addActionListener(l);
        queryTextField.addActionListener(l);
    }

    @Override
    public String getQueryText() {
        return queryTextField.getText().trim();
    }

    @Override
    public Map<String, Boolean> getQueryList() {
        return null;
    }

    @Override
    public boolean isLuceneQuerySelected() {
        return !chRegex.isSelected();
    }

    @Override
    public boolean isRegexQuerySelected() {
        return chRegex.isSelected();
    }

    /**
     * Overwrite when you want to change default persistence type. Default
     * persistence type is PERSISTENCE_ALWAYS
     * 
     * @return TopComponent.PERSISTENCE_NEVER
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void setFilesIndexed(int filesIndexed) {
        filesIndexedValLabel.setText(Integer.toString(filesIndexed));
        if (filesIndexed == 0) {
            searchButton.setEnabled(false);
        } else {
            searchButton.setEnabled(true);
        }
    }
}
