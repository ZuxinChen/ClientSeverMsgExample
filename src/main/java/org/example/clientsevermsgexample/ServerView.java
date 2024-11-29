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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServerView implements Initializable {


    @FXML
    private VBox vbox_messages;

    @FXML
    private Button button_send;

    @FXML
    private TextField tf_message;

    private final List<Socket> clientSockets = new ArrayList<>();

    private Socket clientSocket;

    String username = "Server";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        button_send.setOnAction(event -> writeMessageSelf());
        new Thread(this::runServer).start();
    }

    private void runServer(){
        try {
            ServerSocket serverSocket = new ServerSocket(111);
            updateServer("Server is running and waiting for a client...");

            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    updateServer("Client connected!");
                    clientSockets.add(clientSocket);
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    updateServer("Error accepting client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            updateServer("Error run a server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())){

            String message;
            while ((message = dis.readUTF()) != null) {
                sentToClients(message,clientSocket);
                returnMessage(message);
            }
        } catch (IOException e) {
            updateServer("Error reading message: " + e.getMessage());

        }finally {
            clientSockets.remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                updateServer("Failed to close client socket: " + e.getMessage());
            }
        }
    }

    private void sentToClients(String message,Socket selfClientSocket){
        for (Socket socket : clientSockets) {
            if(socket != selfClientSocket) {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(message);
                } catch (IOException e) {
                    updateServer("Error sending message to every clients: " + e.getMessage());
                    clientSockets.remove(socket);
                }
            }
        }
    }

    private void writeMessageSelf() {
        String message = tf_message.getText();
        for (Socket socket : new ArrayList<>(clientSockets)) {
            try {
                if(!message.isEmpty()) {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(username+": "+message);

                }
            } catch (IOException e) {
                updateServer("Error: Server could not send message to client.");
                clientSockets.remove(clientSocket);
            }
        }

        sendMessage(message);
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



    private void returnMessage(String message){
        javafx.application.Platform.runLater(() -> {
            if (!message.isEmpty()) {
                Label sendMessage = new Label(message);
                HBox hBox = new HBox(8, sendMessage);
                hBox.setAlignment(Pos.TOP_LEFT);
                vbox_messages.getChildren().add(hBox);
                tf_message.clear();
            }
        });
    }
}