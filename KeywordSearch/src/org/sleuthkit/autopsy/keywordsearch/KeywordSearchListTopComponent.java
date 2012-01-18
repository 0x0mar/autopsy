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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.sleuthkit.autopsy.keywordsearch.KeywordSearchTabsTopComponent.TABS;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.sleuthkit.autopsy.keywordsearch//KeywordSearchList//EN",
autostore = false)
@TopComponent.Description(preferredID = "KeywordSearchListTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.sleuthkit.autopsy.keywordsearch.KeywordSearchListTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_KeywordSearchListAction",
preferredID = "KeywordSearchListTopComponent")
public final class KeywordSearchListTopComponent extends TopComponent implements KeywordSearchTopComponentInterface {

    private static Logger logger = Logger.getLogger(KeywordSearchListTopComponent.class.getName());
    private KeywordTableModel tableModel;
    private String currentKeywordList;

    public KeywordSearchListTopComponent() {
        tableModel = new KeywordTableModel();
        initComponents();
        customizeComponents();
        setName(NbBundle.getMessage(KeywordSearchListTopComponent.class, "CTL_KeywordSearchListTopComponent"));
        setToolTipText(NbBundle.getMessage(KeywordSearchListTopComponent.class, "HINT_KeywordSearchListTopComponent"));

    }

    private void customizeComponents() {
        chRegex.setToolTipText("Keyword is a regular expression");
        addWordButton.setToolTipText(("Add a new word to the keyword search list"));
        addWordField.setToolTipText("Enter a new word or regex to search");

        loadListButton.setToolTipText("Load a new keyword list from file or delete an existing list");
        importButton.setToolTipText("Import list(s) of keywords from an external file.");
        saveListButton.setToolTipText("Save the current keyword list to a file");
        searchButton.setToolTipText("Execute the keyword list search using the current list");
        deleteWordButton.setToolTipText("Delete selected keyword(s) from the list");
        deleteAllWordsButton.setToolTipText("Delete all keywords from the list (clear it)");

        keywordTable.setAutoscrolls(true);
        keywordTable.setTableHeader(null);
        keywordTable.setShowHorizontalLines(false);
        keywordTable.setShowVerticalLines(false);

        keywordTable.getParent().setBackground(keywordTable.getBackground());

        //customize column witdhs
        keywordTable.setSize(260, 200);
        final int width = keywordTable.getSize().width;
        TableColumn column = null;
        for (int i = 0; i < 2; i++) {
            column = keywordTable.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setPreferredWidth(((int) (width * 0.2)));
                //column.setCellRenderer(new CellTooltipRenderer());
            } else {
                column.setCellRenderer(new CellTooltipRenderer());
                column.setPreferredWidth(((int) (width * 0.75)));
            }
        }
        keywordTable.setCellSelectionEnabled(false);

        //loadDefaultKeywords();

        KeywordSearchListsXML.getCurrent().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(KeywordSearchListsXML.ListsEvt.LIST_DELETED.toString())) {
                    //still keep keywords from deleted list in widgetm just disassociate the name
                    currentKeywordList = null;
                    curListValLabel.setText("-");
                    if (Integer.valueOf((Integer) evt.getNewValue()) == 0) {
                        loadListButton.setEnabled(false);
                    }
                } else if (evt.getPropertyName().equals(KeywordSearchListsXML.ListsEvt.LIST_ADDED.toString())) {
                    if (Integer.valueOf((Integer) evt.getOldValue()) == 0) {
                        loadListButton.setEnabled(true);
                    }
                }
            }
        });

        if (KeywordSearchListsXML.getCurrent().getNumberLists() == 0) {
            loadListButton.setEnabled(false);
        }
    }

    private void loadDefaultKeywords() {
        //some hardcoded keywords for testing

        //phone number
        tableModel.addKeyword("\\d\\d\\d[\\.-]\\d\\d\\d[\\.-]\\d\\d\\d\\d");
        tableModel.addKeyword("\\d{8,10}");
        tableModel.addKeyword("phone|fax");
        //IP address
        tableModel.addKeyword("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])");
        //email
        tableModel.addKeyword("[e\\-]{0,2}mail");
        tableModel.addKeyword("[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
        //URL
        tableModel.addKeyword("ftp|sftp|ssh|http|https|www");
        //escaped literal word \d\d\d
        tableModel.addKeyword("\\Q\\d\\d\\d\\E");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchButton = new javax.swing.JButton();
        filesIndexedNameLabel = new javax.swing.JLabel();
        filesIndexedValLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        addWordField = new javax.swing.JTextField();
        addWordButton = new javax.swing.JButton();
        loadListButton = new javax.swing.JButton();
        deleteWordButton = new javax.swing.JButton();
        deleteAllWordsButton = new javax.swing.JButton();
        saveListButton = new javax.swing.JButton();
        chRegex = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        keywordTable = new javax.swing.JTable();
        curListNameLabel = new javax.swing.JLabel();
        curListValLabel = new javax.swing.JLabel();
        importButton = new javax.swing.JButton();
        curListLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.searchButton.text")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filesIndexedNameLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.filesIndexedNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(filesIndexedValLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.filesIndexedValLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.titleLabel.text")); // NOI18N

        addWordField.setText(org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.addWordField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addWordButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.addWordButton.text")); // NOI18N
        addWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(loadListButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.loadListButton.text")); // NOI18N
        loadListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadListButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteWordButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.deleteWordButton.text")); // NOI18N
        deleteWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteAllWordsButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.deleteAllWordsButton.text")); // NOI18N
        deleteAllWordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllWordsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveListButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.saveListButton.text")); // NOI18N
        saveListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveListButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chRegex, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.chRegex.text")); // NOI18N
        chRegex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chRegexActionPerformed(evt);
            }
        });

        keywordTable.setModel(tableModel);
        keywordTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        keywordTable.setShowHorizontalLines(false);
        keywordTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(keywordTable);

        org.openide.awt.Mnemonics.setLocalizedText(curListNameLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.curListNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(curListValLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.curListValLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(importButton, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.importButton.text")); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(curListLabel, org.openide.util.NbBundle.getMessage(KeywordSearchListTopComponent.class, "KeywordSearchListTopComponent.curListLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(curListLabel)
                .addContainerGap(263, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(curListNameLabel)
                .addGap(18, 18, 18)
                .addComponent(curListValLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addGap(163, 163, 163))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(deleteWordButton)
                .addGap(18, 18, 18)
                .addComponent(deleteAllWordsButton)
                .addGap(18, 18, 18)
                .addComponent(saveListButton)
                .addContainerGap(52, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filesIndexedNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filesIndexedValLabel)
                .addContainerGap(243, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchButton)
                .addContainerGap(254, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chRegex)
                .addContainerGap(206, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(titleLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(addWordField, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addWordButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(loadListButton)
                        .addGap(18, 18, 18)
                        .addComponent(importButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(47, Short.MAX_VALUE))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadListButton)
                    .addComponent(importButton))
                .addGap(8, 8, 8)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curListLabel)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addWordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addWordButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chRegex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteAllWordsButton)
                    .addComponent(deleteWordButton)
                    .addComponent(saveListButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(searchButton)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(curListNameLabel)
                    .addComponent(curListValLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filesIndexedNameLabel)
                    .addComponent(filesIndexedValLabel))
                .addGap(71, 71, 71))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    }//GEN-LAST:event_searchButtonActionPerformed

    private void addWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordButtonActionPerformed

        String newWord = addWordField.getText().trim();
        String newWordEscaped = Pattern.quote(newWord);

        if (newWord.equals("")) {
            return;
        } else if (keywordExists(newWord) || keywordExists(newWordEscaped)) {
            KeywordSearchUtil.displayDialog("New Keyword Entry", "Keyword already exists in the list.", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.INFO);
            return;
        }

        String toAdd = null;
        if (! chRegex.isSelected()) {
            toAdd = newWordEscaped;
        } else {
            toAdd = newWord;
        }

        //check if valid
        boolean valid = true;
        try {
            Pattern.compile(toAdd);
        } catch (PatternSyntaxException ex1) {
            valid = false;
        } catch (IllegalArgumentException ex2) {
            valid = false;
        }
        if (!valid) {
            KeywordSearchUtil.displayDialog("New Keyword Entry", "Invalid keyword pattern.  Use words or a correct regex pattern.", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.ERROR);
            return;
        }

        //add & reset checkbox
        chRegex.setSelected(false);
        tableModel.addKeyword(toAdd);
        addWordField.setText("");

    }//GEN-LAST:event_addWordButtonActionPerformed

    private void saveListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveListButtonActionPerformed
        final String FEATURE_NAME = "Save Keyword List";
        KeywordSearchListsXML writer = KeywordSearchListsXML.getCurrent();

        String listName = (String) JOptionPane.showInputDialog(
                null,
                "New keyword list name:",
                FEATURE_NAME,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                currentKeywordList != null ? currentKeywordList : "");
        if (listName == null || listName.equals("")) {
            return;
        }

        List<String> keywords = tableModel.getAllKeywords();
        boolean shouldAdd = false;
        if (writer.listExists(listName)) {
            boolean replace = KeywordSearchUtil.displayConfirmDialog(FEATURE_NAME, "Keyword List <" + listName + "> already exists, do you want to replace it?",
                    KeywordSearchUtil.DIALOG_MESSAGE_TYPE.WARN);
            if (replace) {
                shouldAdd = true;
            }

        } else {
            shouldAdd = true;
        }

        if (shouldAdd) {
            writer.addList(listName, keywords);
        }

        currentKeywordList = listName;
        curListValLabel.setText(listName);
        KeywordSearchUtil.displayDialog(FEATURE_NAME, "Keyword List <" + listName + "> saved", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.INFO);


    }//GEN-LAST:event_saveListButtonActionPerformed

    private void chRegexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chRegexActionPerformed
    }//GEN-LAST:event_chRegexActionPerformed

    private void deleteWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteWordButtonActionPerformed
        tableModel.deleteSelected();
    }//GEN-LAST:event_deleteWordButtonActionPerformed

    private void deleteAllWordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllWordsButtonActionPerformed
        tableModel.deleteAll();
    }//GEN-LAST:event_deleteAllWordsButtonActionPerformed

    private void loadListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadListButtonActionPerformed

        final String FEATURE_NAME = "Load Keyword List";

        KeywordSearchListsXML loader = KeywordSearchListsXML.getCurrent();

        final String listName = showLoadDeleteListDialog(FEATURE_NAME, loader.getListNames().toArray(), currentKeywordList, true);

        if (listName == null || listName.equals("")) {
            return;
        }
        currentKeywordList = listName;
        tableModel.resync(currentKeywordList);
        curListValLabel.setText(listName);

        KeywordSearchUtil.displayDialog(FEATURE_NAME, "Keyword List <" + listName + "> loaded", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.INFO);


    }//GEN-LAST:event_loadListButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        //delegate to lists component
        JTabbedPane tabs = (JTabbedPane) this.getParent();
        KeywordSearchListImportExportTopComponent lists = (KeywordSearchListImportExportTopComponent) tabs.getComponentAt(TABS.Lists.ordinal());
        if (lists != null) {
            lists.importButtonAction(evt);
        }
        
    }//GEN-LAST:event_importButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addWordButton;
    private javax.swing.JTextField addWordField;
    private javax.swing.JCheckBox chRegex;
    private javax.swing.JLabel curListLabel;
    private javax.swing.JLabel curListNameLabel;
    private javax.swing.JLabel curListValLabel;
    private javax.swing.JButton deleteAllWordsButton;
    private javax.swing.JButton deleteWordButton;
    private javax.swing.JLabel filesIndexedNameLabel;
    private javax.swing.JLabel filesIndexedValLabel;
    private javax.swing.JButton importButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable keywordTable;
    private javax.swing.JButton loadListButton;
    private javax.swing.JButton saveListButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    private JComboBox loadListCombo;

    private JComboBox findDialogComponent(Component component) {
        if (component instanceof JComboBox) {
            loadListCombo = (JComboBox) component;
        } else if (component instanceof JPanel) {
            for (Component c : ((JPanel) component).getComponents()) {
                findDialogComponent(c);
            }
        } else if (component instanceof JOptionPane) {
            for (Component c : ((JOptionPane) component).getComponents()) {
                findDialogComponent(c);
            }

        }
        return loadListCombo;
    }

    private String showLoadDeleteListDialog(final String title, Object[] choices, Object initialChoice, boolean deleteOption) {
        if (deleteOption) {
            //custom JOptionPane with right click to delete list
            //TODO custom component might be better, than customizing a prefab component
            final JOptionPane loadPane = new JOptionPane("Keyword list to load (right-click to delete):", JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION, null,
                    null, null);

            loadPane.setWantsInput(true);
            loadPane.setSelectionValues(choices);
            loadPane.setInitialSelectionValue(initialChoice);

            final JDialog loadDialog = loadPane.createDialog(null, title);
            final JPopupMenu rightClickMenu = new JPopupMenu();

            final MouseListener rightClickListener = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        rightClickMenu.show(loadPane, e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    rightClickMenu.setVisible(false);
                }
            };
            JMenuItem delItem = new JMenuItem("Delete List");

            delItem.addActionListener(new ActionListener() {

                JComboBox combo;

                @Override
                public void actionPerformed(ActionEvent e) {

                    String selList = null;
                    //there is no JOptionPane API to get current from combobox before OK is pressed
                    //workaround traversing the widgets
                    combo = findDialogComponent(loadPane);

                    if (combo != null) {
                        selList = (String) combo.getSelectedItem();
                    }

                    if (selList != null && selList != JOptionPane.UNINITIALIZED_VALUE) {
                        KeywordSearchListsXML loader = KeywordSearchListsXML.getCurrent();
                        boolean deleted = loader.deleteList(selList);
                        if (deleted) {
                            Object[] choices = loader.getListNames().toArray();
                            loadPane.setSelectionValues(choices);
                            if (choices.length > 0) {
                                loadPane.setInitialSelectionValue(choices[0]);
                            }
                            loadPane.selectInitialValue();
                            combo = findDialogComponent(loadPane);
                            combo.addMouseListener(rightClickListener);
                            KeywordSearchUtil.displayDialog(title, "Keyword List <" + selList + "> deleted", KeywordSearchUtil.DIALOG_MESSAGE_TYPE.INFO);
                        }
                    }
                    rightClickMenu.setVisible(false);
                }
            });

            rightClickMenu.add(delItem);

            JComboBox combo = findDialogComponent(loadPane);
            combo.addMouseListener(rightClickListener);

            loadPane.selectInitialValue();
            loadDialog.setVisible(true);
            loadDialog.dispose();
            String retString = (String) loadPane.getInputValue();
            if (retString == JOptionPane.UNINITIALIZED_VALUE) //no choice was made
            {
                retString = null;
            }

            return retString;
        } else {
            return (String) JOptionPane.showInputDialog(
                    null,
                    "Keyword list to load:",
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    choices,
                    initialChoice);

        }
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
    }

    @Override
    public boolean isMultiwordQuery() {
        return true;
    }

    @Override
    public void addSearchButtonListener(ActionListener l) {
        searchButton.addActionListener(l);
    }

    @Override
    public String getQueryText() {
        return null;
    }

    @Override
    public Map<String, Boolean> getQueryList() {
        List<String> selected = getSelectedKeywords();
        //filter out blank just in case
        Map<String, Boolean> ret = new LinkedHashMap<String, Boolean>();
        for (String s : selected) {
            if (!s.trim().equals("")) {
                //use false for isLiteral because we are currently escaping
                //the keyword earlier as it is stored
                //might need to change and pass isLiteral 
                //if the query object needs to treat it specially
                ret.put(s, false);
            }
        }
        return ret;
    }

    @Override
    public boolean isLuceneQuerySelected() {
        return false;
    }

    @Override
    public boolean isRegexQuerySelected() {
        return true;
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

    public List<String> getAllKeywords() {
        return tableModel.getAllKeywords();
    }

    public List<String> getSelectedKeywords() {
        return tableModel.getSelectedKeywords();
    }

    private boolean keywordExists(String keyword) {

        return tableModel.keywordExists(keyword);
    }

    static class KeywordTableModel extends AbstractTableModel {
        //data

        private Set<TableEntry> keywordData = new TreeSet<TableEntry>();

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return keywordData.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object ret = null;
            TableEntry entry = null;
            //iterate until row
            Iterator<TableEntry> it = keywordData.iterator();
            for (int i = 0; i <= rowIndex; ++i) {
                entry = it.next();
            }
            switch (columnIndex) {
                case 0:
                    ret = (Object) entry.keyword;
                    break;
                case 1:
                    ret = (Object) entry.isActive;
                    break;
                default:
                    logger.log(Level.SEVERE, "Invalid table column index: " + columnIndex);
                    break;
            }
            return ret;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 ? true : false;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                TableEntry entry = null;
                //iterate until row
                Iterator<TableEntry> it = keywordData.iterator();
                for (int i = 0; i <= rowIndex; ++i) {
                    entry = it.next();
                }
                entry.isActive = (Boolean) aValue;
            }
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        List<String> getAllKeywords() {
            List<String> ret = new ArrayList<String>();
            for (TableEntry e : keywordData) {
                ret.add(e.keyword);
            }
            return ret;
        }

        List<String> getSelectedKeywords() {
            List<String> ret = new ArrayList<String>();
            for (TableEntry e : keywordData) {
                if (e.isActive && !e.keyword.equals("")) {
                    ret.add(e.keyword);
                }
            }
            return ret;
        }

        boolean keywordExists(String keyword) {
            List<String> all = getAllKeywords();
            return all.contains(keyword);
        }

        void addKeyword(String keyword) {
            if (!keywordExists(keyword)) {
                keywordData.add(new TableEntry(keyword));
            }
            fireTableDataChanged();
        }

        void addKeywords(List<String> keywords) {
            for (String keyword : keywords) {
                if (!keywordExists(keyword)) {
                    keywordData.add(new TableEntry(keyword));
                }
            }
            fireTableDataChanged();
        }

        void resync(String listName) {
            KeywordSearchListsXML loader = KeywordSearchListsXML.getCurrent();
            KeywordSearchList list = loader.getList(listName);
            List<String> keywords = list.getKeywords();

            deleteAll();
            addKeywords(keywords);
        }

        void deleteAll() {
            keywordData.clear();
            fireTableDataChanged();
        }

        void deleteSelected() {
            List<TableEntry> toDel = new ArrayList<TableEntry>();

            for (TableEntry e : keywordData) {
                if (e.isActive && !e.keyword.equals("")) {
                    toDel.add(e);
                }
            }
            for (TableEntry del : toDel) {
                keywordData.remove(del);
            }
            fireTableDataChanged();

        }

        class TableEntry implements Comparable {

            String keyword;
            Boolean isActive;

            TableEntry(String keyword, Boolean isActive) {
                this.keyword = keyword;
                this.isActive = isActive;
            }

            TableEntry(String keyword) {
                this.keyword = keyword;
                this.isActive = false;
            }

            @Override
            public int compareTo(Object o) {
                return this.keyword.compareTo(((TableEntry) o).keyword);
            }
        }
    }

    /**
     * tooltips that show entire query string
     */
    private static class CellTooltipRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            if (column == 0) {
                String val = (String) table.getModel().getValueAt(row, column);
                setToolTipText(val);
                setText(val);
            }

            return this;
        }
    }
}
