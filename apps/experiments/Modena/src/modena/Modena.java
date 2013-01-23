/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package modena;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItemBuilder;
import javafx.scene.control.ScrollPaneBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class Modena extends Application {
    static {
        System.getProperties().put("javafx.pseudoClassOverrideEnabled", "true");
    }
    private static final String testAppCssUrl = Modena.class.getResource("TestApp.css").toExternalForm();
    private static final String MODENA_STYLESHEET_CONTENT;
    private static final String CASPIAN_STYLESHEET_CONTENT;
    static {
        // these are not supported ways to find the platform themes and may 
        // change release to release. Just used here for testing.
        MODENA_STYLESHEET_CONTENT = loadUrl(com.sun.javafx.scene.control.skin.ButtonSkin.class.getResource("modena/modena.css"));
        CASPIAN_STYLESHEET_CONTENT = loadUrl(com.sun.javafx.scene.control.skin.ButtonSkin.class.getResource("caspian/caspian.css"));
    }
    
    private BorderPane root;
    private SamplePage samplePage;
    private Node mosaic;
    private Node heightTest;
    private Stage mainStage;
    private Color backgroundColor;
    private Color baseColor;
    private Color accentColor;
    private String fontName = null;
    private int fontSize = 13;
    private String styleSheetContent = "";
    private ToggleButton modenaButton,retinaButton,rtlButton;
    private TabPane contentTabs;
    
    private static Modena instance;

    public static Modena getInstance() {
        return instance;
    }
    
    public Map<String, Node> getContent() {
        return samplePage.getContent();
    }
    
    @Override public void start(Stage stage) throws Exception {
        mainStage = stage;
        // set user agent stylesheet
        updateUserAgentStyleSheet(true);
        // build UI
        rebuildUI(true,false,0);
        // show UI
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(testAppCssUrl);
        stage.setScene(scene);
        stage.setTitle("Modena");
        stage.show(); // see SamplePage.java:110 comment on how test fails without having stage shown
        instance = this;
    }
    
    private void updateUserAgentStyleSheet() {
        updateUserAgentStyleSheet(modenaButton.isSelected());
    }
    
    private void updateUserAgentStyleSheet(boolean modena) {
        styleSheetContent = modena ? MODENA_STYLESHEET_CONTENT : CASPIAN_STYLESHEET_CONTENT;
        styleSheetContent += "\n.root {\n";
        System.out.println("baseColor = "+baseColor);
        System.out.println("accentColor = " + accentColor);
        System.out.println("backgroundColor = " + backgroundColor);
        if (baseColor != null && baseColor != Color.TRANSPARENT) {
            final String color = String.format((Locale) null, "#%02x%02x%02x", 
                    Math.round(baseColor.getRed() * 255), 
                    Math.round(baseColor.getGreen() * 255), 
                    Math.round(baseColor.getBlue() * 255));
            styleSheetContent += "    -fx-base:"+color+";\n";
        }
        if (backgroundColor != null && backgroundColor != Color.TRANSPARENT) {
            final String color = String.format((Locale) null, "#%02x%02x%02x", 
                    Math.round(backgroundColor.getRed() * 255), 
                    Math.round(backgroundColor.getGreen() * 255), 
                    Math.round(backgroundColor.getBlue() * 255));
            styleSheetContent += "    -fx-background:"+color+";\n";
        }
        if (accentColor != null && accentColor != Color.TRANSPARENT) {
            final String color = String.format((Locale) null, "#%02x%02x%02x", 
                    Math.round(accentColor.getRed() * 255), 
                    Math.round(accentColor.getGreen() * 255), 
                    Math.round(accentColor.getBlue() * 255));
            styleSheetContent += "    -fx-accent:"+color+";\n";
        }
        if (fontName != null) {
            styleSheetContent += "    -fx-font:"+fontSize+"px \""+fontName+"\";\n";
        }
        styleSheetContent += "}\n";
        
        // set white background for caspian
        if (!modena) {
            styleSheetContent += ".needs-background {\n-fx-background-color: white;\n}";
        }
            
        // load theme
        setUserAgentStylesheet("internal:stylesheet"+Math.random()+".css");
        
        if (root != null) root.requestLayout();
    }
    
    private void rebuildUI(boolean modena, boolean retina, int selectedTab) {
        try {
            if (root == null) {
                root = new BorderPane();
            } else {
                // clear out old UI
                root.setTop(null);
                root.setCenter(null);
            }
            // Create Content Area
            contentTabs = new TabPane();
            contentTabs.getTabs().addAll(
                TabBuilder.create().text("All Controls").content(
                    ScrollPaneBuilder.create().content(
                        samplePage = new SamplePage()
                    ).build()
                ).build(),
                TabBuilder.create().text("UI Mosaic").content(
                    ScrollPaneBuilder.create().content(
                        mosaic = (Node)FXMLLoader.load(Modena.class.getResource("ui-mosaic.fxml"))
                    ).build()
                ).build(),
                TabBuilder.create().text("Alignment Test").content(
                    ScrollPaneBuilder.create().content(
                        heightTest = (Node)FXMLLoader.load(Modena.class.getResource("SameHeightTest.fxml"))
                    ).build()
                ).build()
            );
            contentTabs.getSelectionModel().select(selectedTab);
            // height test set selection for 
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    for (Node n: heightTest.lookupAll(".choice-box")) {
                        ((ChoiceBox)n).getSelectionModel().selectFirst();
                    }
                    for (Node n: heightTest.lookupAll(".combo-box")) {
                        ((ComboBox)n).getSelectionModel().selectFirst();
                    }
                }
            });
            // Create Toolbar
            retinaButton = ToggleButtonBuilder.create()
                .text("@2x")
                .selected(retina)
                .onAction(new EventHandler<ActionEvent>(){
                    @Override public void handle(ActionEvent event) {
                        ToggleButton btn = (ToggleButton)event.getSource();
                        if (btn.isSelected()) {
                            contentTabs.getTransforms().setAll(new Scale(2,2));
                        } else {
                            contentTabs.getTransforms().setAll(new Scale(1,1));
                        }
                        contentTabs.requestLayout();
                    }
                })
                .build();
            ToggleGroup themesToggleGroup = new ToggleGroup();
            ToolBar toolBar = new ToolBar(
                HBoxBuilder.create()
                    .children(
                        modenaButton = ToggleButtonBuilder.create()
                            .text("Modena")
                            .toggleGroup(themesToggleGroup)
                            .selected(modena)
                            .onAction(new EventHandler<ActionEvent>(){
                                @Override public void handle(ActionEvent event) { 
                                    updateUserAgentStyleSheet();
                                }
                            })
                            .styleClass("left-pill")
                            .build(),
                        ToggleButtonBuilder.create()
                            .text("Caspian")
                            .toggleGroup(themesToggleGroup)
                            .selected(!modena)
                            .onAction(new EventHandler<ActionEvent>(){
                                @Override public void handle(ActionEvent event) { 
                                    updateUserAgentStyleSheet();
                                }
                            })
                            .styleClass("right-pill")
                            .build()
                    )
                    .build(),
                ButtonBuilder.create()
                    .graphic(new ImageView(new Image(Modena.class.getResource("reload_12x14.png").toString())))
                    .onAction(new EventHandler<ActionEvent>() {
                            @Override public void handle(ActionEvent event) {
                                rebuildUI(modenaButton.isSelected(), retinaButton.isSelected(), 
                                        contentTabs.getSelectionModel().getSelectedIndex());
                            }
                        })
                    .build(),
                rtlButton = ToggleButtonBuilder.create()
                    .text("RTL")
                    .onAction(new EventHandler<ActionEvent>() {
                            @Override public void handle(ActionEvent event) {
                                root.setNodeOrientation(rtlButton.isSelected() ? 
                                        NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
                            }
                        })
                    .build(),
                new Separator(),
                retinaButton,
                createFontMenu(),
                new Label("Base:"),
                createBaseColorPicker(),
                new Label("Background:"),
                createBackgroundColorPicker(),
                new Label("Accent:"),
                createAccentColorPicker(),
                new Separator(),
                ButtonBuilder.create().text("Save...").onAction(saveBtnHandler).build()
            );
            toolBar.setId("TestAppToolbar");
            // Create content group used for scaleing @2x
            final Pane contentGroup = new Pane() {
                @Override protected void layoutChildren() {
                    double scale = contentTabs.getTransforms().isEmpty() ? 1 : ((Scale)contentTabs.getTransforms().get(0)).getX();
                    contentTabs.resizeRelocate(0,0,getWidth()/scale, getHeight()/scale);
                }
            };
            contentGroup.getChildren().add(contentTabs);
            // populate root
            root.setTop(toolBar);
            root.setCenter(contentGroup);
            // move foucus out of the way
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    modenaButton.requestFocus();
                }
            });
            
            samplePage.getStyleClass().add("needs-background");
            mosaic.getStyleClass().add("needs-background");
            heightTest.getStyleClass().add("needs-background");
            // apply retina scale
            if (retina) {
                contentTabs.getTransforms().setAll(new Scale(2,2));
            }
        } catch (IOException ex) {
            Logger.getLogger(Modena.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private MenuButton createFontMenu() {
        MenuButton mb = new MenuButton("Font Sizes");
        ToggleGroup tg = new ToggleGroup();
        mb.getItems().addAll(
            RadioMenuItemBuilder.create().text("System Default").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = null;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 13px System;").toggleGroup(tg).selected(true).build(),
            RadioMenuItemBuilder.create().text("Mac (13px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Lucida Grande";
                    fontSize = 13;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 13px \"Lucida Grande\";").toggleGroup(tg).build(),
            RadioMenuItemBuilder.create().text("Windows 100% (12px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Segoe UI";
                    fontSize = 12;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 12px \"Segoe UI\";").toggleGroup(tg).build(),
            RadioMenuItemBuilder.create().text("Windows 125% (15px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Segoe UI";
                    fontSize = 15;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 15px \"Segoe UI\";").toggleGroup(tg).build(),
            RadioMenuItemBuilder.create().text("Windows 150% (18px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Segoe UI";
                    fontSize = 18;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 18px \"Segoe UI\";").toggleGroup(tg).build(),
            RadioMenuItemBuilder.create().text("Embedded Touch (22px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Arial";
                    fontSize = 22;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 22px \"Arial\";").toggleGroup(tg).build(),
            RadioMenuItemBuilder.create().text("Embedded Small (9px)").onAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent event) {
                    fontName = "Arial";
                    fontSize = 9;
                    updateUserAgentStyleSheet();
                }
            }).style("-fx-font: 9px \"Arial\";").toggleGroup(tg).build()
        );
        return mb;
    }
    
    private ColorPicker createBaseColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color c) {
                if (c == null) {
                    baseColor = null;
                } else {
                    baseColor = c;
                }
                updateUserAgentStyleSheet();
            }
        });
        return colorPicker;
    }
    
    private ColorPicker createBackgroundColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color c) {
                if (c == null) {
                    backgroundColor = null;
                } else {
                    backgroundColor = c;
                }
                updateUserAgentStyleSheet();
            }
        });
        return colorPicker;
    }
    
    private ColorPicker createAccentColorPicker() {
        ColorPicker colorPicker = new ColorPicker(Color.web("#0096C9"));
        colorPicker.getCustomColors().addAll(
                Color.TRANSPARENT,
                Color.web("#0096C9"),
                Color.web("#4fb6d6"),
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#888"),
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.PURPLE,
                Color.MAGENTA,
                Color.BLACK
        );
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color c) {
                if (c == null) {
                    accentColor = null;
                } else {
                    accentColor = c;
                }
                updateUserAgentStyleSheet();
            }
        });
        return colorPicker;
    }
    
    private EventHandler<ActionEvent> saveBtnHandler = new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent event) {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = fc.showSaveDialog(mainStage);
            if (file != null) {
                try {
                    samplePage.getStyleClass().add("root");
                    WritableImage img = samplePage.snapshot(new SnapshotParameters(), null);
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", file);
                } catch (IOException ex) {
                    Logger.getLogger(Modena.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };
    
    public static void main(String[] args) {
        launch(args);
    }
    
    /** Utility method to load a URL into a string */
    private static String loadUrl(URL url) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException ex) {
            Logger.getLogger(Modena.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    // =========================================================================
    // URL Handler to create magic "internal:stylesheet.css" url for our css string buffer
    {
        URL.setURLStreamHandlerFactory(new StringURLStreamHandlerFactory());
    }

    /**
     * Simple URLConnection that always returns the content of the cssBuffer
     */
    private class StringURLConnection extends URLConnection {
        public StringURLConnection(URL url){
            super(url);
        }
        
        @Override public void connect() throws IOException {}

        @Override public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(styleSheetContent.getBytes("UTF-8"));
        }
    }
    
    private class StringURLStreamHandlerFactory implements URLStreamHandlerFactory {
        URLStreamHandler streamHandler = new URLStreamHandler(){
            @Override protected URLConnection openConnection(URL url) throws IOException {
                if (url.toString().toLowerCase().endsWith(".css")) {
                    return new StringURLConnection(url);
                }
                throw new FileNotFoundException();
            }
        };
        @Override public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("internal".equals(protocol)) {
                return streamHandler;
            }
            return null;
        }
    }
}
