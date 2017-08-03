package com.faforever.gw.ui;

import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.PlanetAction;
import com.faforever.gw.model.UniverseState;
import com.faforever.gw.model.entitity.Battle;
import com.faforever.gw.model.entitity.Faction;
import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.event.BattleChangedEvent;
import com.faforever.gw.model.event.CharacterNameProposalEvent;
import com.faforever.gw.model.event.ErrorEvent;
import com.faforever.gw.model.event.UniverseLoadedEvent;
import com.faforever.gw.services.GwClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
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
    @FXML
    private TreeTableView<UniverseItemAdapter> universeTreeTableView;
    @FXML
    private ComboBox requestCharacterFactionComboBox;
    @FXML
    private Button requestCharacterSendButton;
    @FXML
    private TextField selectNameRequestIdTextField;
    @FXML
    private ComboBox selectNameProposalComboBox;
    @FXML
    private Button selectNameProposalButton;


    private ObservableList<Battle> battleData = FXCollections.observableArrayList();

    private final UniverseState universeState;

    private Map<String, String> userAccessTokenMap = new TreeMap<>();

    @Inject
    public MainController(GwClient gwClient, UniverseState universeState) {
        this.gwClient = gwClient;
        this.universeState = universeState;
    }

    public void onConnectClicked() {
//        gwClient.connect(String.format("ws://echo.websocket.org",
        gwClient.connect(userAccessTokenMap.get(userComboBox.getValue()));
    }

    private void refreshData() {
        val planets = universeState.getPlanets();

        planets.forEach(planet -> initiateAssaultComboBox.getItems().add(new PlanetWrapper(planet)));

        universeState.getActiveBattleDict().values()
                .forEach(battle -> {
                    joinAssaultComboBox.getItems().add(battle.getId());
                    battleData.add(battle);
                });

        TreeItem root = universeTreeTableView.getRoot();
        universeState.getSolarSystems().forEach(
                solarSystem -> {
                    TreeItem<UniverseItemAdapter> solarSystemTreeItem = new TreeItem<>(new UniverseItemAdapter(solarSystem));
                    root.getChildren().add(solarSystemTreeItem);
                    solarSystem.getPlanets().forEach(
                            planet -> solarSystemTreeItem.getChildren().add(new TreeItem<>(new UniverseItemAdapter(planet)))
                    );
                }
        );
    }

    @FXML
    public void initialize() {
        for (Faction f : Faction.values()) {
            requestCharacterFactionComboBox.getItems().add(f);
        }

        userAccessTokenMap.put("-1- UEF Alpha", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMSwgInVzZXJfbmFtZSI6ICJVRUYgQWxwaGEiLCAiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sICJleHAiOiA0MTAyNDQ0NzQwfQ.qlA-HIEU9zQ7OA_eAqfYAG5MZmhe7TBqV9zVnJgV2wY");
        userAccessTokenMap.put("-2- UEF Bravo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMiwgInVzZXJfbmFtZSI6ICJVRUYgQnJhdm8iLCAiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sICJleHAiOiA0MTAyNDQ0NzQwfQ.ZHwO6jvcHPd0fhBFSaJTQpt-S8Zmwa6unPW0qHkzLKw");
        userAccessTokenMap.put("-3- Cybran Charlie", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjogMywgInVzZXJfbmFtZSI6ICJDeWJyYW4gQ2hhcmxpZSIsICJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwgImV4cCI6IDQxMDI0NDQ3NDB9.qPE-UkG8tSdH4fMzD6RWkGHSYoH24SluvsPcfN9GX4A");
        userAccessTokenMap.put("-4- Cybran Delta", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA0LCAidXNlcl9uYW1lIjogIkN5YnJhbiBEZWx0YSJ9.5LwaskFvNLwRvIUIfvc0s2WUHP_Q1NlaUjY4hGN0Lv4");
        userAccessTokenMap.put("-5- Aeon Echo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA1LCAidXNlcl9uYW1lIjogIkFlb24gRWNobyJ9.Kv1en5p2bWb6zE2ag6PWp4u1WxR6F8HPZSweDG23p60");
        userAccessTokenMap.put("-X- Unregistered User", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA5OSwgInVzZXJfbmFtZSI6ICJVbnJlZ2lzdGVyZWQgdXNlciJ9.skNv5W3lgqq_OETwAeGDrlSDaKxq-Lqt2jrIspUI9Ik");

        userAccessTokenMap.keySet().forEach(s -> userComboBox.getItems().add(s));
        userComboBox.setValue("-1- UEF Alpha");

        onClientStateChanged(ClientState.DISCONNECTED);

        setupBattleTableView();
        setupUniverseTreeTableView();
    }

    private void setupBattleTableView() {
        TableColumn column_id = new TableColumn("Location");
        battleTableView.getColumns().add(column_id);
        column_id.setCellValueFactory(new PropertyValueFactory<Battle, String>("id"));
        column_id.setMinWidth(250);

//        TableColumn column_planet = new TableColumn("Planet");
//        battleTableView.getColumns().add(column_planet);
//        column_planet.setCellValueFactory(new PropertyValueFactory<Battle, String>("planet"));

        TableColumn column_attacker = new TableColumn("Attacker");
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
        column_action.setCellFactory(param -> new BattleActionButtonCell());

        battleTableView.setItems(battleData);
    }

    private void setupUniverseTreeTableView() {
        TreeTableColumn locationColumn = new TreeTableColumn("Location");
        locationColumn.setCellValueFactory(new TreeItemPropertyValueFactory<UniverseItemAdapter, String>("location"));
        locationColumn.setMinWidth(170);
        universeTreeTableView.getColumns().add(locationColumn);

        TreeTableColumn idColumn = new TreeTableColumn("ID");
        idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<UniverseItemAdapter, String>("id"));
        idColumn.setMinWidth(200);
        universeTreeTableView.getColumns().add(idColumn);

        TreeTableColumn ownerColumn = new TreeTableColumn("Owner");
        ownerColumn.setCellValueFactory(new TreeItemPropertyValueFactory<UniverseItemAdapter, String>("owner"));
        ownerColumn.setMinWidth(50);
        universeTreeTableView.getColumns().add(ownerColumn);


        TreeTableColumn actionColumn = new TreeTableColumn("Action");
        actionColumn.setCellFactory(param -> new UniverseActionButtonCell());
        actionColumn.setMinWidth(100);
        universeTreeTableView.getColumns().add(actionColumn);

        TreeItem<UniverseItemAdapter> rootTreeItem = new TreeItem<>(new UniverseItemAdapter(new Planet()));
        universeTreeTableView.setRoot(rootTreeItem);
        universeTreeTableView.setShowRoot(false);
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

    public void onDisconnectClicked() {
        gwClient.disconnect();

        battleData.clear();
        universeTreeTableView.getRoot().getChildren().clear();
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

    @EventListener
    public void onUniverseLoaded(UniverseLoadedEvent e) {
        refreshData();
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

    @SneakyThrows
    public void onRequestCharacterSendButtonClicked() {
        gwClient.requestCharacter((Faction) requestCharacterFactionComboBox.getValue());
    }

    private void removeBattle(Battle battle) {
        Platform.runLater(() -> {
            joinAssaultComboBox.getItems().remove(battle.getId());
            battleData.remove(battle);
            battleTableView.refresh();
            universeTreeTableView.refresh();
        });
    }

    @EventListener
    private void onErrorEvent(ErrorEvent event) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error from server");
            alert.setHeaderText(event.getMessage());
            alert.setContentText(event.getCode());
            alert.showAndWait();
        });
    }

    @EventListener
    private void onBattleChanged(BattleChangedEvent event) {
        Battle battle = event.getBattle();
        switch (battle.getStatus()) {
            case INITIATED:
                if (event.isNewBattle()) {
                    battleData.add(battle);
                }
            case RUNNING:
                Platform.runLater(() -> {
                    battleTableView.refresh();
                    universeTreeTableView.refresh();
                });
                break;
            case CANCELED:
            case FINISHED:
                removeBattle(battle);
        }
        ;
    }

    @SneakyThrows
    private boolean isCurrentBattle(String battleId) {
        if (gwClient.getCurrentBattle() == null)
            return false;

        return battleId.equals(gwClient.getCurrentBattle().toString());
    }

    private class BattleActionButtonCell extends TableCell<Battle, Boolean> {
        final Button cellButton = new Button("join");
//
//        @EventListener
//        public

        public BattleActionButtonCell() {
            cellButton.setOnAction(event -> {
                try {
                    Battle battle = (Battle) BattleActionButtonCell.this.getTableRow().getItem();
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

            Battle battle = (Battle) BattleActionButtonCell.this.getTableRow().getItem();
            if (!empty && battle != null) {
                setGraphic(cellButton);

                if (isCurrentBattle(battle.getId())) {
                    cellButton.setText("leave");

                    cellButton.setOnAction(event -> {
                        try {
                            gwClient.leaveAssault();
                        } catch (Exception e) {
                            log.error("Something went wrong on leaving assault", e);
                        }
                    });
                } else {
                    cellButton.setText("join");

                    cellButton.setOnAction(event -> {
                        try {
                            gwClient.joinAssault(battle.getId());
                        } catch (Exception e) {
                            log.error("Something went wrong on joining assault", e);
                        }
                    });
                }
            }
        }
    }

    private class UniverseActionButtonCell extends TreeTableCell<UniverseItemAdapter, UniverseItemAdapter> {
        final Button cellButton = new Button();

        public UniverseActionButtonCell() {
            cellButton.setOnAction(event -> {
                UniverseItemAdapter item = getTreeTableRow().getItem();

                if (item == null || item.isSolarSystem()) {
                    return;
                }

                PlanetAction action = gwClient.getPossibleActionFor(item.getPlanet());

                switch (action) {
                    case START_ASSAULT:
                        try {
                            gwClient.initiateAssault(UUID.fromString(item.getPlanet().getId()));
                        } catch (IOException e) {
                            log.error("Initiating assault failed", e);
                        }
                        break;
                    case JOIN_OFFENSE:
                    case JOIN_DEFENSE:
                        try {
                            Battle battle = universeState.getActiveBattleForPlanet(item.getPlanet().getId()).get();
                            gwClient.joinAssault(battle.getId());
                        } catch (IOException e) {
                            log.error("Joining battle failed", e);
                        }
                        break;
                    case LEAVE:
                        try {
                            gwClient.leaveAssault();
                        } catch (Exception e) {
                            log.error("Leaving battle failed", e);
                        }
                        break;
                    case NONE:
                        throw new IllegalStateException(MessageFormat.format("You can't interact with {0}.", item.getPlanet()));
                }
            });
        }

        @Override
        @SneakyThrows
        protected void updateItem(UniverseItemAdapter t, boolean empty) {
            super.updateItem(t, empty);

            UniverseItemAdapter item = this.getTreeTableRow().getItem();

            setGraphic(null);

            if (item == null || item.isSolarSystem()) {
                return;
            }

            PlanetAction action = gwClient.getPossibleActionFor(item.getPlanet());
            setGraphic(cellButton);
            cellButton.setVisible(true);

            switch (action) {
                case START_ASSAULT:
                    cellButton.setText("start assault");
                    break;
                case JOIN_OFFENSE:
                    cellButton.setText("join assault");
                    break;
                case JOIN_DEFENSE:
                    cellButton.setText("defend");
                    break;
                case LEAVE:
                    cellButton.setText("leave");
                    break;
                case NONE:
                    cellButton.setVisible(false);
                    break;
            }
        }
    }

    @EventListener
    public void onCharacterNameProposal(CharacterNameProposalEvent event) {
        Platform.runLater(() -> {
            selectNameRequestIdTextField.setText(event.getRequestId().toString());
            selectNameProposalComboBox.getItems().clear();

            for (String name : event.getProposedNamesList()) {
                selectNameProposalComboBox.getItems().add(name);
            }
        });
    }

    public void onSelectNameProposalButtonClicked() {
        try {
            gwClient.selectName(UUID.fromString(selectNameRequestIdTextField.getText()), (String) selectNameProposalComboBox.getValue());
        } catch (IOException e) {
            log.error("Something went wrong on selecting character name", e);
        }
    }
}

