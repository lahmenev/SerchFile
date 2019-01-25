package com.testTask.controller;

import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.tree.DefaultMutableTreeNode;

public class MainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button findButton;

    @FXML
    private TextField formatInput;

    @FXML
    private TextField textInput;

    @FXML
    private AnchorPane anchorid;

    @FXML
    private TreeView<Object> treeView;

    @FXML
    private TextArea fileText;

    @FXML
    private TextArea fileText2;

    @FXML
    private Tab fileTab;

    @FXML
    void initialize() {
        formatInput.setText(".log");

        findButton.setOnAction(event -> {
            findFile();
        });
    }

    private List<TreeItem<Object>> files;
    private List<TreeItem<Object>> dirs;
    private TreeItem<Object> leafs;
    private boolean flagText1 = false;

    public void findFile() {
        final DefaultMutableTreeNode top = new DefaultMutableTreeNode("Проект");
        //Открытие окна выбора папки
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выбор директории");
        Stage stage = (Stage) anchorid.getScene().getWindow();
        File file = directoryChooser.showDialog(stage);
        //Создание дерева проекта

        TreeItem<Object> tree = new TreeItem<>(file.getPath());

        dirs = new ArrayList<>();
        files = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(file.getPath()))) {
            for(Path path: directoryStream) {
                if (Files.isDirectory(path)) {
                    TreeItem<Object> subDirectory = new TreeItem<>(path);
                    getSubLeafs(path, subDirectory);
                    dirs.add(subDirectory);
                } else {
                    if (path.toString().endsWith(formatInput.getText())) {
                        byte[] array = Files.readAllBytes(path);

                        String wikiString = new String(array, "windows-1251");
                        System.out.println(wikiString);
                        if (wikiString.contains(textInput.getText())) {
                            files.add(getLeafs(path));
                            System.out.println(path.toString());

                            //Вывод текста найденного файла
                            writeText(path.toString());
                            flagText1 = true;

                            // Открытие файла в новой вкладке
                            treeView.getSelectionModel()
                                    .selectedItemProperty()
                                    .addListener((observable, oldValue, newValue) ->
                                            writeText(path.toString()));
                        }
                    }
                }
            }
            tree.getChildren().addAll(dirs);
            tree.getChildren().addAll(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tree.setExpanded(true);
        treeView.setRoot(tree);
        treeView.setShowRoot(true);
    }

    private void getSubLeafs(Path subPath, TreeItem<Object> parent) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(subPath.toString()))) {
            for(Path subDir: directoryStream) {
                if (!Files.isDirectory(subDir)) {
                    String subTree = subDir.toString();
                    TreeItem<Object> subLeafs = new TreeItem<>(subTree.substring(1 + subTree.lastIndexOf(File.separator)));
                    parent.getChildren().add(subLeafs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TreeItem<Object> getLeafs(Path subPath) {
        String strPath = subPath.toString();
        leafs = new TreeItem<>(strPath.substring(1 + strPath.lastIndexOf(File.separator)));
        return leafs;
    }

    private void writeText(String path) {
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "Cp1251"));
            while ((line = br.readLine()) != null) {

                if (flagText1 == false)
                    fileText.setText(line);
                else if (flagText1 == true) {
                    fileTab.setDisable(false);
                    fileText2.setText(line);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

