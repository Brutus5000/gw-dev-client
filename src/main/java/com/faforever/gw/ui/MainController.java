package com.faforever.gw.ui;

import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.PlanetAction;
import com.faforever.gw.model.UniverseState;
import com.faforever.gw.model.entitity.*;
import com.faforever.gw.model.event.*;
import com.faforever.gw.services.GwClient;
import com.faforever.gw.ui.universe.ConnectionLine;
import com.faforever.gw.ui.universe.SolarSystemController;
import com.faforever.gw.ui.universe.UniverseItemAdapter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MainController {
    private final UiService uiService;
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
    private Button leaveAssaultButton;
    @FXML
    private Button debugHostGameButton;
    @FXML
    private TableView<Battle> battleTableView;
    @FXML
    private TableView<Reinforcement> reinforcementsTableView;
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
    @FXML
    private Pane universeEditorPane;
    @FXML
    private ComboBox<Faction> universeEditorFactionComboBox;

    @FXML
    private Label aeonPlanetCountLabel;
    @FXML
    private Label cybranPlanetCountLabel;
    @FXML
    private Label uefPlanetCountLabel;
    @FXML
    private Label seraphimPlanetCountLabel;

    private List<SolarSystem> selectedSolarSystems = new ArrayList<>();
    private Map<SolarSystem, SolarSystemController> controllerMap = new HashMap<>();
    private List<ConnectionLine> connectionLines = new ArrayList<>();
    private ObservableList<Battle> battleData = FXCollections.observableArrayList();
    private ObservableList<Reinforcement> reinforcementsData = FXCollections.observableArrayList();

    private final UniverseState universeState;

    private Map<String, String> userAccessTokenMap = new TreeMap<>();

    @Inject
    public MainController(UiService uiService, GwClient gwClient, UniverseState universeState) {
        this.uiService = uiService;
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


                    SolarSystemController solarSystemController = uiService.loadFxml("/solarSystem.fxml");
                    universeEditorPane.getChildren().add(solarSystemController.getRoot());
                    solarSystemController.setSolarSystem(solarSystem);
                    solarSystemController.setSelectionChangedListener(isSelected -> {
                        if (isSelected) {
                            selectedSolarSystems.add(solarSystem);
                        } else {
                            selectedSolarSystems.remove(solarSystem);
                        }
                    });

                    controllerMap.put(solarSystem, solarSystemController);
                }
        );

        aeonPlanetCountLabel.setText(Long.toString(
                universeState.getPlanets().stream()
                        .filter(planet -> planet.getCurrentOwner() == Faction.AEON)
                        .count()));
        cybranPlanetCountLabel.setText(Long.toString(
                universeState.getPlanets().stream()
                        .filter(planet -> planet.getCurrentOwner() == Faction.CYBRAN)
                        .count()));
        uefPlanetCountLabel.setText(Long.toString(
                universeState.getPlanets().stream()
                        .filter(planet -> planet.getCurrentOwner() == Faction.UEF)
                        .count()));
        seraphimPlanetCountLabel.setText(Long.toString(
                universeState.getPlanets().stream()
                        .filter(planet -> planet.getCurrentOwner() == Faction.SERAPHIM)
                        .count()));

        for (SolarSystem from : universeState.getSolarSystems()) {
            for (SolarSystem to : from.getConnectedSystems()) {
                if (connectionLines.stream().filter(connectionLine -> connectionLine.connects(from, to)).count() > 0)
                    continue;

                ConnectionLine connectionLine = new ConnectionLine(controllerMap.get(from), controllerMap.get(to));
                universeEditorPane.getChildren().add(connectionLine);
                connectionLine.toBack();
                connectionLines.add(connectionLine);
            }
        }
    }

    @FXML
    public void initialize() {
        for (Faction f : Faction.values()) {
            requestCharacterFactionComboBox.getItems().add(f);
        }

        universeEditorFactionComboBox.getItems().addAll(Faction.values());

        userAccessTokenMap.put("-1- UEF Alpha", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiAxLCAidXNlcl9uYW1lIjogIlVFRiBBbHBoYSJ9.u9cylQuOx-th89cUcCbeaLvegBXHSkL3_kWxZBvXTkc");
        userAccessTokenMap.put("-2- UEF Bravo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiAyLCAidXNlcl9uYW1lIjogIlVFRiBCcmF2byJ9.zTYD_vBUnG_u6QLwfNhz6BaIr_rBDF_jqCiAfAJcS1o");
        userAccessTokenMap.put("-3- Cybran Charlie", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiAzLCAidXNlcl9uYW1lIjogIkN5YnJhbiBDaGFybGllIn0.ElIwdA6vYOFs8KQ3SVEMU4q_o9vGQjDK0zxeUDPeKHI");
        userAccessTokenMap.put("-4- Cybran Delta", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA0LCAidXNlcl9uYW1lIjogIkN5YnJhbiBEZWx0YSJ9.5LwaskFvNLwRvIUIfvc0s2WUHP_Q1NlaUjY4hGN0Lv4");
        userAccessTokenMap.put("-5- Aeon Echo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA1LCAidXNlcl9uYW1lIjogIkFlb24gRWNobyJ9.Kv1en5p2bWb6zE2ag6PWp4u1WxR6F8HPZSweDG23p60");
        userAccessTokenMap.put("-X- Unregistered User", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzIjo0MTAyMzU4NDAwLCAiYXV0aG9yaXRpZXMiOiBbXSwgInVzZXJfaWQiOiA2LCAidXNlcl9uYW1lIjogIlVucmVnaXN0ZXJlZCB1c2VyIn0.N1LcYHHRu_bWxC_MH2BzmADC4AMfwdmuXOQJfLt0VFQ");

        userAccessTokenMap.keySet().forEach(s -> userComboBox.getItems().add(s));
        userComboBox.setValue("-1- UEF Alpha");

        onClientStateChanged(ClientState.DISCONNECTED);

        setupBattleTableView();
        setupReinforcementsTableView();
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

    private void setupReinforcementsTableView() {
//        TableColumn<Reinforcement, String> typeColumn = new TableColumn<>("type");
//        typeColumn.setMinWidth(200);
//        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().getName()));
//        reinforcementsTableView.getColumns().add(typeColumn);

        TableColumn<Reinforcement, String> priceColumn = new TableColumn<>("price");
        priceColumn.setMaxWidth(50);
        priceColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPrice())));
        reinforcementsTableView.getColumns().add(priceColumn);

        TableColumn<Reinforcement, String> delayColumn = new TableColumn<>("delay");
        delayColumn.setMaxWidth(50);
        delayColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getDelay())));
        reinforcementsTableView.getColumns().add(delayColumn);



        TableColumn<Reinforcement, String> unitFaUidColumn = new TableColumn<>("u_faUid");
        unitFaUidColumn.setMinWidth(100);
        unitFaUidColumn.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().getUnit()).map(Unit::getFaUid).orElse("")));
        reinforcementsTableView.getColumns().add(unitFaUidColumn);

        TableColumn<Reinforcement, String> unitNameColumn = new TableColumn<>("u_name");
        unitNameColumn.setMinWidth(100);
        unitNameColumn.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().getUnit()).map(Unit::getName).orElse("")));
        reinforcementsTableView.getColumns().add(unitNameColumn);

        TableColumn<Reinforcement, String> unitFactionColumn = new TableColumn<>("u_faction");
        unitFactionColumn.setMinWidth(50);
        unitFactionColumn.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().getUnit()).map(Unit::getFaction).map(Faction::getName).orElse("")));
        reinforcementsTableView.getColumns().add(unitFactionColumn);

        TableColumn<Reinforcement, String> unitTechLevelColumn = new TableColumn<>("u_t");
        unitTechLevelColumn.setMaxWidth(30);
        unitTechLevelColumn.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().getUnit()).map(Unit::getTechLevel).map(TechLevel::getName).orElse("")));
        reinforcementsTableView.getColumns().add(unitTechLevelColumn);


        TableColumn<Reinforcement, String> itemNameColumn = new TableColumn<>("i_name");
        itemNameColumn.setMinWidth(80);
        itemNameColumn.setCellValueFactory(c -> new SimpleStringProperty(Optional.ofNullable(c.getValue().getItem()).map(PassiveItem::getName).orElse("")));
        reinforcementsTableView.getColumns().add(itemNameColumn);


        TableColumn<Reinforcement, String> typeColumn = new TableColumn<>("type");
        typeColumn.setMinWidth(100);
        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().getName()));
        reinforcementsTableView.getColumns().add(typeColumn);

        TableColumn<Reinforcement, String> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(50);
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        reinforcementsTableView.getColumns().add(idColumn);

        reinforcementsTableView.setItems(reinforcementsData);
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
            String name = planet.getCurrentOwner() == null ? "unassigned" : planet.getCurrentOwner().getName();
            return String.format("%s %s", name, planet.getId());
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
            debugHostGameButton.setDisable(newState == ClientState.DISCONNECTED);

            switch (newState) {
                case DISCONNECTED:
                    characterTextField.setText("");
                    currentBattleTextField.setText("");
                    initiateAssaultComboBox.getItems().clear();
                    joinAssaultComboBox.getItems().clear();
                    battleData.clear();
                    universeEditorPane.getChildren().clear();
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
        Platform.runLater(() ->
            reinforcementsData.addAll(universeState.getReinforcements())
        );
        Platform.runLater(this::refreshData);
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
    private void onSolarSystemsLinked(SolarSystemsLinkedEvent event) {
        Platform.runLater(() -> {
            SolarSystem from = event.getFrom();
            SolarSystem to = event.getTo();

            ConnectionLine connectionLine = new ConnectionLine(controllerMap.get(from), controllerMap.get(to));
            connectionLines.add(connectionLine);
            universeEditorPane.getChildren().add(connectionLine);
            connectionLine.toBack();
            log.info("Solar systems link from {} to {} added to scene graph", from, to);
        });
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

    @EventListener
    private void onSolarSystemsUnlinked(SolarSystemsUnlinkedEvent event) {
        Platform.runLater(() -> {
            SolarSystem from = event.getFrom();
            SolarSystem to = event.getTo();

            connectionLines.stream()
                    .filter(connectionLine -> connectionLine.connects(from, to))
                    .collect(Collectors.toList())
                    .forEach(connectionLine -> {
                        connectionLine.unlink();
                        universeEditorPane.getChildren().remove(connectionLine);
                        connectionLines.remove(connectionLine);
                    });

            log.info("Solar systems link from {} to {} removed from scene graph", from, to);
        });
    }

    @EventListener
    private void onPlanetOwnerChanged(PlanetOwnerChangedEvent event) {
        Platform.runLater(() -> {
            SolarSystemController solarSystemController = controllerMap.get(event.getPlanet().getSolarSystem());
            solarSystemController.invalidate();
        });
    }

    @SneakyThrows
    public void onLinkClicked() {
        log.debug("Link clicked");
        log.debug("-> selected SolarSystems: {}", selectedSolarSystems);

        List<SolarSystem> remaining = new ArrayList<>(selectedSolarSystems);

        for (SolarSystem from : selectedSolarSystems) {
            for (SolarSystem to : selectedSolarSystems) {
                if (from == to)
                    continue;

                if (!remaining.contains(to))
                    continue;

                log.debug("Requesting solar system link from {} to {}", from, to);
                gwClient.adminLinkSolarSystems(from, to);
            }

            remaining.remove(from);
        }
    }

    @SneakyThrows
    public void onUnlinkClicked() {
        log.debug("Unlink clicked");
        log.debug("-> selected SolarSystems: {}", selectedSolarSystems);

        List<SolarSystem> remaining = new ArrayList<>(selectedSolarSystems);

        for (SolarSystem from : selectedSolarSystems) {
            for (SolarSystem to : selectedSolarSystems) {
                if (from == to)
                    continue;

                if (!remaining.contains(to))
                    continue;

                log.debug("Requesting removal solar system link from {} to {}", from, to);
                gwClient.adminUnlinkSolarSystems(from, to);
            }

            remaining.remove(from);
        }
    }

    public void onDeselectAllClicked() {
        log.debug("Deselect all clicked");
        new ArrayList<>(selectedSolarSystems)
                .forEach(solarSystem -> controllerMap.get(solarSystem).setSelected(false));
    }

    public void onSetFactionClicked() {
        Faction selectedFaction = universeEditorFactionComboBox.getSelectionModel().getSelectedItem();
        log.debug("SetFaction clicked for faction {}", selectedFaction);
        log.debug("-> selected SolarSystems: {}", selectedSolarSystems);

        if (selectedFaction == null) {
            log.error("No faction selected");
            onErrorEvent(new ErrorEvent("ui", "Please select a faction to set"));
            return;
        }
        selectedSolarSystems.stream()
                .flatMap(solarSystem -> solarSystem.getPlanets().stream())
                .forEach(planet -> gwClient.setFaction(planet, selectedFaction));
    }

    public void onDebugHostGame() {
        gwClient.sendDebug("dummyHostGame");
    }
}
