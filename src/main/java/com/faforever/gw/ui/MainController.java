package com.faforever.gw.ui;

import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.GwClient;
import com.faforever.gw.model.entitity.Battle;
import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.event.BattleUpdateWaitingProgressEvent;
import com.faforever.gw.model.event.NewBattleEvent;
import com.faforever.gw.model.event.PlanetConqueredEvent;
import com.faforever.gw.model.event.PlanetDefendedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.inject.Inject;

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
    private ComboBox<PlanetWrapper> initiateAssaultComboBox;
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
    @FXML
    private TableView<Battle> battleTableView;
    private ObservableList<Battle> battleData = FXCollections.observableArrayList();

    private Map<String, String> userAccessTokenMap = new TreeMap<>();

    @Inject
    public MainController(GwClient gwClient) {
        this.gwClient = gwClient;
    }

    static class PlanetWrapper {
        public final Planet planet;
        PlanetWrapper(Planet planet) {
            this.planet = planet;
        }

        public String toString() {
            return String.format("%s %s", planet.getCurrentOwner().getName(), planet.getId());
        }
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


        TableColumn column_id = new TableColumn("ID");
        battleTableView.getColumns().add(column_id);
        column_id.setCellValueFactory(new PropertyValueFactory<Battle, String>("id"));
        column_id.setMinWidth(250);

//        TableColumn column_planet = new TableColumn("Planet");
//        battleTableView.getColumns().add(column_planet);
//        column_planet.setCellValueFactory(new PropertyValueFactory<Battle, String>("planet"));

        TableColumn column_attacker = new TableColumn("Atacker");
        battleTableView.getColumns().add(column_attacker);
        column_attacker.setCellValueFactory(new PropertyValueFactory<Battle, String>("attackingFaction"));

        TableColumn column_defender = new TableColumn("Defender");
        battleTableView.getColumns().add(column_defender);
        column_defender.setCellValueFactory(new PropertyValueFactory<Battle, String>("defendingFaction"));

        TableColumn<Battle, String> column_waitingProgress = new TableColumn<>("Starting");
        battleTableView.getColumns().add(column_waitingProgress);
//        column_waitingProgress.setCellValueFactory(new PropertyValueFactory<Battle, String>("waitingProgress"));
        column_waitingProgress.setCellValueFactory(
                battle -> {
                    SimpleStringProperty property = new SimpleStringProperty();
                    property.setValue(String.format("%.0f%%", battle.getValue().getWaitingProgress() * 100.0));
                    return property;
                });

        TableColumn column_action = new TableColumn("Action");
        battleTableView.getColumns().add(column_action);
        column_action.setCellFactory(param -> new ActionButtonCell());


        battleTableView.setItems(battleData);
    }

    public void onConnectClicked() {
//        gwClient.connect(String.format("ws://echo.websocket.org",
        gwClient.connect(hostTextField.getText(),
                portTextField.getText(),
                userAccessTokenMap.get(userComboBox.getValue()));

        val planets = gwClient.getPlanets();
        planets.forEach(planet -> initiateAssaultComboBox.getItems().add(new PlanetWrapper(planet)));

        val battles = gwClient.getInitiatedBattles();
        battles.forEach(battle -> {
            joinAssaultComboBox.getItems().add(battle.getId());
            battleData.add(battle);
        });
    }

    public void onDisconnectClicked() {
        gwClient.disconnect();
    }

    @EventListener
    private void onClientStateChanged(ClientState newState) {
        Platform.runLater(() -> {
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
                    initiateAssaultComboBox.getItems().clear();
                    joinAssaultComboBox.getItems().clear();
                    battleData.clear();
                    break;
                case CONNECTED:
                    characterTextField.setText("");
                    currentBattleTextField.setText("");
                    break;
                case FREE_FOR_BATTLE:
                    characterTextField.setText(gwClient.getMyCharacter().toString());
                    currentBattleTextField.setText("");
                    battleTableView.refresh();
                    break;
                case IN_ASSAULT:
                    characterTextField.setText(gwClient.getMyCharacter().toString());
                    if (gwClient.getCurrentBattle() != null)
                        currentBattleTextField.setText(gwClient.getCurrentBattle().toString());
                    battleTableView.refresh();
                    break;
                default:
                    break;
            }
        });
    }

    @SneakyThrows
    public void onInitiateAssaultButtonClicked() {
        if (initiateAssaultComboBox.getSelectionModel().isEmpty())
            return;

        gwClient.initiateAssault(UUID.fromString(initiateAssaultComboBox.getSelectionModel().getSelectedItem().planet.getId()));
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

    private void removeBattle(String battleId) {
        Platform.runLater(() -> {
            joinAssaultComboBox.getItems().remove(battleId);
            battleData.stream()
                    .filter(battle -> battle.getId().equals(battleId.toString()))
                    .findFirst().ifPresent(battle -> battleData.remove(battle));
        });
    }

    @EventListener
    private void onPlanetConquered(PlanetConqueredEvent event) {
        Platform.runLater(() -> removeBattle(event.getBattle().getId()));
    }

    @EventListener
    private void onPlanetDefended(PlanetDefendedEvent event) {
        Platform.runLater(() -> removeBattle(event.getBattle().getId()));
    }

    @EventListener
    private void onNewBattle(NewBattleEvent event) {
        Platform.runLater(() -> {
            battleData.add(event.getBattle());
            if (event.getBattle().getId().equals(gwClient.getCurrentBattle())) {
                log.debug("Ignore PlanetUnderAssaultMessage: it's our own battle");
            } else {
                log.debug("Received PlanetUnderAssaultMessage - add to joinable battles");
                joinAssaultComboBox.getItems().add(event.getBattle().getId());
            }
        });
    }

    @EventListener
    private void onBattleWaitingProgressUpdate(BattleUpdateWaitingProgressEvent event) {
        Platform.runLater(() -> {
            battleData.stream()
                    .filter(battle -> battle.getId().equals(event.getBattleId().toString()))
                    .forEach(battle -> battle.setWaitingProgress(event.getWaitingProgress()));

            battleTableView.refresh();
        });
    }

    @SneakyThrows
    private boolean isCurrentBattle(String battleId) {
        if (gwClient.getCurrentBattle() == null)
            return false;

        return battleId.equals(gwClient.getCurrentBattle().toString());
    }

    private class ActionButtonCell extends TableCell<Battle, Boolean> {
        final Button cellButton = new Button("join");
//
//        @EventListener
//        public

        public ActionButtonCell() {
            cellButton.setOnAction(event -> {
                try {
                    Battle battle = (Battle) ActionButtonCell.this.getTableRow().getItem();
                    gwClient.joinAssault(UUID.fromString(battle.getId()));
                } catch (Exception e) {
                    log.error("Error on generating UUID from battleId string", e);
                }
            });
        }

        //Display button if the row is not empty
        @Override
        @SneakyThrows
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);

            Battle battle = (Battle) ActionButtonCell.this.getTableRow().getItem();
            if (!empty && battle != null) {
                setGraphic(cellButton);

                if (isCurrentBattle(battle.getId())) {
                    cellButton.setText("leave");

                    cellButton.setOnAction(event -> {
                        try {
                            gwClient.leaveAssault();
                        } catch (Exception e) {
                        }
                    });
                } else {
                    cellButton.setText("join");

                    cellButton.setOnAction(event -> {
                        try {
                            gwClient.joinAssault(battle.getId());
                        } catch (Exception e) {
                        }
                    });
                }
            }
        }
    }
}

