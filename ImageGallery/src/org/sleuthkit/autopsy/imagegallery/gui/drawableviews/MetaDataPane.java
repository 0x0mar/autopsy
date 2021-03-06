/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-15 Basis Technology Corp.
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
package org.sleuthkit.autopsy.imagegallery.gui.drawableviews;

import com.google.common.eventbus.Subscribe;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.util.Optional;
import org.sleuthkit.autopsy.coreutils.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.sleuthkit.autopsy.casemodule.events.ContentTagAddedEvent;
import org.sleuthkit.autopsy.casemodule.events.ContentTagDeletedEvent;
import org.sleuthkit.autopsy.imagegallery.FXMLConstructor;
import org.sleuthkit.autopsy.imagegallery.ImageGalleryController;
import org.sleuthkit.autopsy.imagegallery.datamodel.Category;
import org.sleuthkit.autopsy.imagegallery.datamodel.CategoryManager;
import org.sleuthkit.autopsy.imagegallery.datamodel.DrawableAttribute;
import org.sleuthkit.autopsy.imagegallery.datamodel.DrawableFile;
import org.sleuthkit.datamodel.TagName;

/**
 * Shows details of the selected file.
 */
public class MetaDataPane extends DrawableUIBase {

    private static final Logger LOGGER = Logger.getLogger(MetaDataPane.class.getName());

    private static final KeyCodeCombination COPY_KEY_COMBINATION = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

    @FXML
    private TableColumn<Pair<DrawableAttribute<?>, Collection<?>>, DrawableAttribute<?>> attributeColumn;

    @FXML
    private TableView<Pair<DrawableAttribute<?>, Collection<?>>> tableView;

    @FXML
    private TableColumn<Pair<DrawableAttribute<?>, Collection<?>>, String> valueColumn;

    private final MenuItem copyMenuItem = new MenuItem("Copy");
    private final ContextMenu contextMenu = new ContextMenu(copyMenuItem);

    public MetaDataPane(ImageGalleryController controller) {
        super(controller);
        FXMLConstructor.construct(this, "MetaDataPane.fxml");
    }

    @FXML
    void initialize() {
        assert attributeColumn != null : "fx:id=\"attributeColumn\" was not injected: check your FXML file 'MetaDataPane.fxml'.";
        assert imageView != null : "fx:id=\"imageView\" was not injected: check your FXML file 'MetaDataPane.fxml'.";
        assert tableView != null : "fx:id=\"tableView\" was not injected: check your FXML file 'MetaDataPane.fxml'.";
        assert valueColumn != null : "fx:id=\"valueColumn\" was not injected: check your FXML file 'MetaDataPane.fxml'.";
        getController().getTagsManager().registerListener(this);
        getController().getCategoryManager().registerListener(this);

        //listen for selection change
        getController().getSelectionModel().lastSelectedProperty().addListener((observable, oldFileID, newFileID) -> {
            setFile(newFileID);
        });

        copyMenuItem.setAccelerator(COPY_KEY_COMBINATION);
        copyMenuItem.setOnAction(actionEvent -> {
            copyValueToClipBoard();
        });

        tableView.setContextMenu(contextMenu);
        tableView.setOnKeyPressed((KeyEvent event) -> {
            if (COPY_KEY_COMBINATION.match(event)) {
                contextMenu.hide();
                copyMenuItem.fire();
                event.consume();
            }
        });

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("Select a file to show its details here."));
        tableView.getColumns().setAll(Arrays.asList(attributeColumn, valueColumn));

        attributeColumn.setPrefWidth(USE_COMPUTED_SIZE);
        attributeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getKey()));
        attributeColumn.setCellFactory(param -> new TableCell<Pair<DrawableAttribute<?>, Collection<?>>, DrawableAttribute<?>>() {
            @Override
            protected void updateItem(DrawableAttribute<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (isNull(item) || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                    setGraphic(new ImageView(item.getIcon()));
                }
            }
        });

        valueColumn.setPrefWidth(USE_COMPUTED_SIZE);
        valueColumn.setCellValueFactory(p -> new SimpleStringProperty(getValueDisplayString(p.getValue())));
        valueColumn.setCellFactory(p -> new TableCell<Pair<DrawableAttribute<?>, Collection<?>>, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (isNull(item) || empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item);
                    text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                    setGraphic(text);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    static private String getValueDisplayString(Pair<DrawableAttribute<?>, Collection<?>> p) {
        if (p.getKey() == DrawableAttribute.TAGS) {
            return ((Collection<TagName>) p.getValue()).stream()
                    .map(TagName::getDisplayName)
                    .filter(Category::isNotCategoryName)
                    .collect(Collectors.joining(" ; "));
        } else {
            return p.getValue().stream()
                    .map(value -> Objects.toString(value, ""))
                    .collect(Collectors.joining(" ; "));
        }
    }

    @Override
    protected synchronized void setFileHelper(Long newFileID) {
        setFileIDOpt(Optional.ofNullable(newFileID));
        if (newFileID == null) {
            Platform.runLater(() -> {
                imageView.setImage(null);
                imageBorder.setCenter(null);
                tableView.getItems().clear();
                getCategoryBorderRegion().setBorder(null);
            });
        } else {
            disposeContent();
            updateUI();
            updateContent();
        }
    }

    @Override
    CachedLoaderTask<Image, DrawableFile<?>> getNewImageLoadTask(DrawableFile<?> file) {
        return new ThumbnailLoaderTask(file);
    }

    public void updateUI() {
        getFile().ifPresent(file -> {
            final List<Pair<DrawableAttribute<?>, Collection<?>>> attributesList = file.getAttributesList();
            Platform.runLater(() -> {
                tableView.getItems().clear();
                tableView.getItems().setAll(attributesList);
            });

            updateCategory();
        });
    }

    @Override
    public Region getCategoryBorderRegion() {
        return imageBorder;
    }

    /** {@inheritDoc } */
    @Subscribe
    @Override
    public void handleCategoryChanged(CategoryManager.CategoryChangeEvent evt) {
        getFileID().ifPresent(fileID -> {
            if (evt.getFileIDs().contains(fileID)) {
                updateUI();
            }
        });
    }

    @Subscribe
    @Override
    public void handleTagAdded(ContentTagAddedEvent evt) {
        getFileID().ifPresent((fileID) -> {
            if (Objects.equals(evt.getAddedTag().getContent().getId(), fileID)) {
                updateUI();
            }
        });
    }

    @Override
    public void handleTagDeleted(ContentTagDeletedEvent evt) {
        getFileID().ifPresent((fileID) -> {
            if (Objects.equals(evt.getDeletedTagInfo().getContentID(), fileID)) {
                updateUI();
            }
        });
    }

    private void copyValueToClipBoard() {
        Pair<DrawableAttribute<?>, Collection<?>> selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (nonNull(selectedItem)) {
            Clipboard.getSystemClipboard().setContent(Collections.singletonMap(DataFormat.PLAIN_TEXT,
                    getValueDisplayString(selectedItem)));
        }
    }
}
