package org.example;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class PrimaryController {
    @FXML
    private TextField pathId;
    @FXML
    private ProgressBar barId;

    private Task task = null;

    @FXML
    protected void btnPathClick(ActionEvent event) {
        Node node = (Node) event.getSource();
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (!pathId.getText().isEmpty()) {
            directoryChooser.setInitialDirectory(new File(pathId.getText()));
        }
        File dir = directoryChooser.showDialog(node.getScene().getWindow());
        if (dir != null) {
            pathId.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    protected void btnStartClick(ActionEvent event) {
        String path = pathId.getText();
        if (path.isEmpty()) {
            return;
        }

        task = new FixTask(path);
        barId.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void btnCancelClick(ActionEvent event) {
        if (task != null) {
            task.cancel();
        }
    }
}
