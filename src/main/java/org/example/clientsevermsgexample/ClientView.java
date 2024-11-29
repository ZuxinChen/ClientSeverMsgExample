package org.example.clientsevermsgexample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientView implements Initializable {

    @FXML
    private VBox vbox_messages;

    @FXML
    private Button button_send;

    @FXML
    private TextField tf_message;

    @FXML
    private TextField userName;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    String username = "user";
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userName.textProperty().addListener((observable, oldValue, newValue)-> username = newValue);
        button_send.setOnAction(event -> writeMessage());
        connectServer();
    }
    @FXML
    private void connectServer(){
        try {
            socket = new Socket("localhost", 111);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            updateServer("connect to server");
            new Thread(this::readMessage).start();

        } catch (IOException e) {
            updateServer("Error connecting: " + e.getMessage() + "\n");
        }
    }

    private void readMessage(){
        while (true) {
            try {
                String response = dis.readUTF();
                returnMessage(response);
            } catch (IOException e) {
                updateServer("Error reading message: " + e.getMessage());
                break;
            }
        }
    }

    private void writeMessage(){
        String message = tf_message.getText();
        if (socket == null || socket.isClosed()){
            updateServer("Socket is closed. Cannot send message.");
        }
        try {
            dos.writeUTF(username + ": "+ message);
            sendMessage(message);
        } catch (IOException e) {
            updateServer("Error writing message: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {

        javafx.application.Platform.runLater(() -> {
            if (!message.isEmpty()) {
                Label sendMessage = new Label(message);
                Label user = new Label(":" + username);
                HBox hBox = new HBox(8, sendMessage, user);
                hBox.setAlignment(Pos.TOP_RIGHT);
                vbox_messages.getChildren().add(hBox);
                tf_message.clear();
            }
        });
    }

    private void updateServer(String message) {
        javafx.application.Platform.runLater(() -> {
            Label updateMessage = new Label(message + "\n");
            HBox hBox = new HBox(updateMessage);
            hBox.setAlignment(Pos.CENTER);
            vbox_messages.getChildren().add(hBox);
        });
    }

    private void returnMessage(String message) {
        javafx.application.Platform.runLater(() -> {
            Label sendMessage = new Label(message);
            HBox hBox = new HBox(8, sendMessage);
            hBox.setAlignment(Pos.TOP_LEFT);
            vbox_messages.getChildren().add(hBox);
            tf_message.clear();
        });
    }
}