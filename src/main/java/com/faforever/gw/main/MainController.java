package com.faforever.gw.main;

import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.GwClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Component
@Slf4j
public class MainController {
    private GwClient gwClient;

    @FXML
    private ComboBox userComboBox;
    @FXML
    private TextField hostTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField statusTextField;
    @FXML
    private TextField characterTextField;
    @FXML
    private TextField currentBattleTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private ComboBox initiateAssaultComboBox;
    @FXML
    private Button initiateAssaultButton;
    @FXML
    private ComboBox joinAssaultComboBox;
    @FXML
    private Button joinAssaultButton;
    @FXML
    private ComboBox leaveAssaultComboBox;
    @FXML
    private Button leaveAssaultButton;

    private Map<String, String> userAccessTokenMap = new TreeMap<>();

    @Inject
    public MainController(GwClient gwClient) {
        this.gwClient = gwClient;
    }

    @FXML
    public void initialize() {
        userAccessTokenMap.put("-1- UEF Alpha", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMSwgInVzZXJfbmFtZSI6ICJVRUYgQWxwaGEiLCAiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sICJleHAiOiA0MTAyNDQ0NzQwfQ.qlA-HIEU9zQ7OA_eAqfYAG5MZmhe7TBqV9zVnJgV2wY");
        userAccessTokenMap.put("-2- UEF Bravo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMiwgInVzZXJfbmFtZSI6ICJVRUYgQnJhdm8iLCAiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sICJleHAiOiA0MTAyNDQ0NzQwfQ.ZHwO6jvcHPd0fhBFSaJTQpt-S8Zmwa6unPW0qHkzLKw");
        userAccessTokenMap.put("-3- Cybran Charlie", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMywgInVzZXJfbmFtZSI6ICJDeWJyYW4gQ2hhcmxpZSIsICJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwgImV4cCI6IDQxMDI0NDQ3NDB9.qPE-UkG8tSdH4fMzD6RWkGHSYoH24SluvsPcfN9GX4A");
        userAccessTokenMap.put("-4- Cybran Delta", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA0LCAidXNlcl9uYW1lIjogIkN5YnJhbiBEZWx0YSJ9.5LwaskFvNLwRvIUIfvc0s2WUHP_Q1NlaUjY4hGN0Lv4");
        userAccessTokenMap.put("-5- Aeon Echo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA1LCAidXNlcl9uYW1lIjogIkFlb24gRWNobyJ9.Kv1en5p2bWb6zE2ag6PWp4u1WxR6F8HPZSweDG23p60");

        userAccessTokenMap.keySet().forEach(s -> userComboBox.getItems().add(s));
        userComboBox.setValue("-1- UEF Alpha");

        onClientStateChanged(ClientState.DISCONNECTED);
    }

    public void onConnectClicked() {
//        gwClient.connect(String.format("ws://echo.websocket.org",
        gwClient.connect(hostTextField.getText(),
                portTextField.getText(),
                userAccessTokenMap.get(userComboBox.getValue()));

        val planets = gwClient.getPlanets();
        planets.forEach(planet -> initiateAssaultComboBox.getItems().add(planet.getId()));
    }

    public void onDisconnectClicked() {
        gwClient.disconnect();
    }

    @EventListener
    private void onClientStateChanged(ClientState newState) {
        log.info("Client state changed to {}", newState);
        statusTextField.setText(newState.toString());

        userComboBox.setDisable(newState != ClientState.DISCONNECTED);
        connectButton.setDisable(newState != ClientState.DISCONNECTED);
        disconnectButton.setDisable(newState == ClientState.DISCONNECTED);
        initiateAssaultButton.setDisable(newState != ClientState.FREE_FOR_BATTLE);
        joinAssaultButton.setDisable(newState != ClientState.FREE_FOR_BATTLE);
        leaveAssaultButton.setDisable(newState != ClientState.IN_ASSAULT);

        switch (newState) {
            case DISCONNECTED:
                characterTextField.setText("");
                currentBattleTextField.setText("");
                break;
            case CONNECTED:
                characterTextField.setText("");
                currentBattleTextField.setText("");
                break;
            case FREE_FOR_BATTLE:
                characterTextField.setText(gwClient.getMyCharacter().toString());
                currentBattleTextField.setText("");
                break;
            case IN_ASSAULT:
                characterTextField.setText(gwClient.getMyCharacter().toString());
                currentBattleTextField.setText(gwClient.getCurrentBattle().toString());
                break;
            default:
                break;
        }
    }

    @SneakyThrows
    public void onInitiateAssaultButtonClicked() {
        if (initiateAssaultComboBox.getSelectionModel().isEmpty())
            return;

        gwClient.initiateAssault(UUID.fromString(initiateAssaultComboBox.getSelectionModel().getSelectedItem().toString()));
    }

    @SneakyThrows
    public void onJoinAssaultButtonClicked() {
        if (joinAssaultComboBox.getSelectionModel().isEmpty())
            return;

        gwClient.joinAssault(UUID.fromString(joinAssaultComboBox.getSelectionModel().getSelectedItem().toString()));
    }

    @SneakyThrows
    public void onLeaveAssaultButtonClicked() {
        gwClient.leaveAssault();

    }
}
