/*
 * MegaMek -
 * Copyright (C) 2000,2001,2002,2003,2004,2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */

package megamek.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import megamek.MegaMek;
import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.BattleArmor;
import megamek.common.Bay;
import megamek.common.BipedMech;
import megamek.common.Board;
import megamek.common.Building;
import megamek.common.CommonConstants;
import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.CriticalSlot;
import megamek.common.Dropship;
import megamek.common.Engine;
import megamek.common.Entity;
import megamek.common.EntitySelector;
import megamek.common.EntityWeightClass;
import megamek.common.EquipmentMode;
import megamek.common.EquipmentType;
import megamek.common.FighterSquadron;
import megamek.common.Flare;
import megamek.common.FuelTank;
import megamek.common.Game;
import megamek.common.GameTurn;
import megamek.common.GunEmplacement;
import megamek.common.HexTarget;
import megamek.common.HitData;
import megamek.common.IArmorState;
import megamek.common.IBoard;
import megamek.common.IEntityMovementMode;
import megamek.common.IEntityMovementType;
import megamek.common.IEntityRemovalConditions;
import megamek.common.IGame;
import megamek.common.IHex;
import megamek.common.ILocationExposureStatus;
import megamek.common.INarcPod;
import megamek.common.IOffBoardDirections;
import megamek.common.ITerrain;
import megamek.common.Infantry;
import megamek.common.InfernoTracker;
import megamek.common.Jumpship;
import megamek.common.LargeSupportTank;
import megamek.common.LocationFullException;
import megamek.common.LosEffects;
import megamek.common.MapSettings;
import megamek.common.Mech;
import megamek.common.MechWarrior;
import megamek.common.Minefield;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.MovePath;
import megamek.common.MoveStep;
import megamek.common.PhysicalResult;
import megamek.common.Pilot;
import megamek.common.PilotingRollData;
import megamek.common.PlanetaryConditions;
import megamek.common.Player;
import megamek.common.Protomech;
import megamek.common.QuadMech;
import megamek.common.Report;
import megamek.common.SmallCraft;
import megamek.common.SpaceStation;
import megamek.common.SpecialHexDisplay;
import megamek.common.SupportTank;
import megamek.common.SupportVTOL;
import megamek.common.Tank;
import megamek.common.TargetRoll;
import megamek.common.Targetable;
import megamek.common.Team;
import megamek.common.TeleMissile;
import megamek.common.Terrain;
import megamek.common.Terrains;
import megamek.common.ToHitData;
import megamek.common.TurnOrdered;
import megamek.common.TurnVectors;
import megamek.common.UnitLocation;
import megamek.common.VTOL;
import megamek.common.Warship;
import megamek.common.WeaponComparator;
import megamek.common.WeaponType;
import megamek.common.IGame.Phase;
import megamek.common.actions.AbstractAttackAction;
import megamek.common.actions.ArtilleryAttackAction;
import megamek.common.actions.AttackAction;
import megamek.common.actions.BAVibroClawAttackAction;
import megamek.common.actions.BreakGrappleAttackAction;
import megamek.common.actions.BrushOffAttackAction;
import megamek.common.actions.ChargeAttackAction;
import megamek.common.actions.ClearMinefieldAction;
import megamek.common.actions.ClubAttackAction;
import megamek.common.actions.DfaAttackAction;
import megamek.common.actions.DodgeAction;
import megamek.common.actions.EntityAction;
import megamek.common.actions.FindClubAction;
import megamek.common.actions.FlipArmsAction;
import megamek.common.actions.GrappleAttackAction;
import megamek.common.actions.JumpJetAttackAction;
import megamek.common.actions.KickAttackAction;
import megamek.common.actions.LayExplosivesAttackAction;
import megamek.common.actions.LayMinefieldAction;
import megamek.common.actions.ProtomechPhysicalAttackAction;
import megamek.common.actions.PunchAttackAction;
import megamek.common.actions.PushAttackAction;
import megamek.common.actions.RamAttackAction;
import megamek.common.actions.RepairWeaponMalfunctionAction;
import megamek.common.actions.SearchlightAttackAction;
import megamek.common.actions.SpotAction;
import megamek.common.actions.TeleMissileAttackAction;
import megamek.common.actions.ThrashAttackAction;
import megamek.common.actions.TorsoTwistAction;
import megamek.common.actions.TriggerAPPodAction;
import megamek.common.actions.TriggerBPodAction;
import megamek.common.actions.TripAttackAction;
import megamek.common.actions.UnjamAction;
import megamek.common.actions.UnjamTurretAction;
import megamek.common.actions.UnloadStrandedAction;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.containers.PlayerIDandList;
import megamek.common.net.ConnectionFactory;
import megamek.common.net.ConnectionListenerAdapter;
import megamek.common.net.DisconnectedEvent;
import megamek.common.net.IConnection;
import megamek.common.net.Packet;
import megamek.common.net.PacketReceivedEvent;
import megamek.common.options.IBasicOption;
import megamek.common.options.IOption;
import megamek.common.preference.PreferenceManager;
import megamek.common.util.BoardUtilities;
import megamek.common.util.StringUtil;
import megamek.common.verifier.EntityVerifier;
import megamek.common.verifier.TestEntity;
import megamek.common.verifier.TestMech;
import megamek.common.verifier.TestTank;
import megamek.common.weapons.AttackHandler;
import megamek.common.weapons.TAGHandler;
import megamek.common.weapons.Weapon;
import megamek.common.weapons.WeaponHandler;
import megamek.server.commands.AddBotCommand;
import megamek.server.commands.CheckBVCommand;
import megamek.server.commands.DefeatCommand;
import megamek.server.commands.ExportListCommand;
import megamek.server.commands.FixElevationCommand;
import megamek.server.commands.HelpCommand;
import megamek.server.commands.KickCommand;
import megamek.server.commands.ListSavesCommand;
import megamek.server.commands.LocalSaveGameCommand;
import megamek.server.commands.LoadGameCommand;
import megamek.server.commands.LocalLoadGameCommand;
import megamek.server.commands.NukeCommand;
import megamek.server.commands.ResetCommand;
import megamek.server.commands.RollCommand;
import megamek.server.commands.RulerCommand;
import megamek.server.commands.SaveGameCommand;
import megamek.server.commands.SeeAllCommand;
import megamek.server.commands.ServerCommand;
import megamek.server.commands.ShowEntityCommand;
import megamek.server.commands.ShowTileCommand;
import megamek.server.commands.ShowValidTargetsCommand;
import megamek.server.commands.SkipCommand;
import megamek.server.commands.TeamCommand;
import megamek.server.commands.VictoryCommand;
import megamek.server.commands.WhoCommand;
import megamek.server.victory.Victory;

/**
 * @author Ben Mazur
 */
public class Server implements Runnable {

    /**
     * The DamageType enumeration is used for the damageEntity function.
     */
    public enum DamageType {
        NONE, FRAGMENTATION, FLECHETTE, ACID, INCENDIARY, IGNORE_PASSENGER, ANTI_TSM
    }

    // public final static String LEGAL_CHARS =
    // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_.-";
    public final static String DEFAULT_BOARD = MapSettings.BOARD_SURPRISE;

    private final static String VERIFIER_CONFIG_FILENAME = "data/mechfiles/UnitVerifierOptions.xml";

    // server setup
    private String password;

    private ServerSocket serverSocket;

    private String motd;

    // game info
    Vector<IConnection> connections = new Vector<IConnection>(4);

    Vector<IConnection> connectionsPending = new Vector<IConnection>(4);

    Hashtable<Integer, IConnection> connectionIds = new Hashtable<Integer, IConnection>();

    private int connectionCounter;

    IGame game = new Game();

    private Vector<Report> vPhaseReport = new Vector<Report>();

    private MapSettings mapSettings = new MapSettings();

    // commands
    private Hashtable<String, ServerCommand> commandsHash = new Hashtable<String, ServerCommand>();

    // listens for and connects players
    private Thread connector;

    // Track buildings that are affected by an entity's movement.
    private Hashtable<Building, Boolean> affectedBldgs = new Hashtable<Building, Boolean>();

    // Track Physical Action results, HACK to deal with opposing pushes
    // canceling each other
    private Vector<PhysicalResult> physicalResults = new Vector<PhysicalResult>();

    private Vector<DynamicTerrainProcessor> terrainProcessors = new Vector<DynamicTerrainProcessor>();

    private Timer timer = new Timer();

    /*
     * Tracks entities which have been destroyed recently. Allows refactoring of
     * the damage and kill logic from Server, where it is now, to the Entity
     * subclasses eventually. This has not been implemented yet -- I am just
     * starting to build the groundwork into Server. It isn't in the execution
     * path and shouldn't cause any bugs
     */
    // Note from another coder - I have commented out your groundwork
    // for now because it is using HashSet, which isn't available in
    // Java 1.1 unless you import the collections classes. Since the
    // Server class isn't using any other collecitons classes, there
    // might be a reason we're avoiding them here...if not, feel free
    // to add the import.
    // private HashSet knownDeadEntities = new HashSet();
    private static EntityVerifier entityVerifier;

    private ArrayList<int[]> scheduledNukes = new ArrayList<int[]>();

    private static Server serverInstance = null;

    private ConnectionListenerAdapter connectionListener = new ConnectionListenerAdapter() {

        /**
         * Called when it is sensed that a connection has terminated.
         */
        public void disconnected(DisconnectedEvent e) {
            IConnection conn = e.getConnection();

            // write something in the log
            System.out.println("s: connection " + conn.getId() + " disconnected");

            connections.removeElement(conn);
            connectionsPending.removeElement(conn);
            connectionIds.remove(new Integer(conn.getId()));

            // if there's a player for this connection, remove it too
            Player player = getPlayer(conn.getId());
            if (null != player) {
                Server.this.disconnected(player);
            }

        }

        public void packetReceived(PacketReceivedEvent e) {
            Server.this.handle(e.getConnection().getId(), e.getPacket());
        }

    };

    /**
     * Construct a new GameHost and begin listening for incoming clients.
     * 
     * @param password
     *            the <code>String</code> that is set as a password
     * @param port
     *            the <code>int</code> value that specifies the port that is
     *            used
     */
    public Server(String password, int port) throws IOException {
        serverInstance = this;
        this.password = password.length() > 0 ? password : null;
        // initialize server socket
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(50);

        motd = createMotd();

        game.getOptions().initialize();
        game.getOptions().loadOptions();

        changePhase(IGame.Phase.PHASE_LOUNGE);

        // display server start text
        System.out.println("s: starting a new server...");

        try {
            String host = InetAddress.getLocalHost().getHostName();
            System.out.print("s: hostname = '");
            System.out.print(host);
            System.out.print("' port = ");
            System.out.println(serverSocket.getLocalPort());
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (int i = 0; i < addresses.length; i++) {
                System.out.println("s: hosting on address = " + addresses[i].getHostAddress());
            }
        } catch (UnknownHostException e) {
            // oh well.
        }

        System.out.println("s: password = " + this.password);

        // register commands
        registerCommand(new DefeatCommand(this));
        registerCommand(new ExportListCommand(this));
        registerCommand(new FixElevationCommand(this));
        registerCommand(new HelpCommand(this));
        registerCommand(new KickCommand(this));
        registerCommand(new ListSavesCommand(this));
        registerCommand(new LocalSaveGameCommand(this));
        registerCommand(new LocalLoadGameCommand(this));
        registerCommand(new ResetCommand(this));
        registerCommand(new RollCommand(this));
        registerCommand(new SaveGameCommand(this));
        registerCommand(new LoadGameCommand(this));
        registerCommand(new SeeAllCommand(this));
        registerCommand(new SkipCommand(this));
        registerCommand(new VictoryCommand(this));
        registerCommand(new WhoCommand(this));
        registerCommand(new TeamCommand(this));
        registerCommand(new ShowTileCommand(this));
        registerCommand(new ShowEntityCommand(this));
        registerCommand(new RulerCommand(this));
        registerCommand(new ShowValidTargetsCommand(this));
        registerCommand(new AddBotCommand(this));
        registerCommand(new CheckBVCommand(this));
        registerCommand(new NukeCommand(this));

        // register terrain processors
        terrainProcessors.add(new FireProcessor(this));
        terrainProcessors.add(new SmokeProcessor(this));
        terrainProcessors.add(new GeyserProcessor(this));
        terrainProcessors.add(new ElevatorProcessor(this));
        terrainProcessors.add(new ScreenProcessor(this));
        terrainProcessors.add(new WeatherProcessor(this));
        terrainProcessors.add(new QuicksandProcessor(this));

        // Fully initialised, now accept connections
        connector = new Thread(this, "Connection Listener");
        connector.start();
    }

    /**
     * Sets the game for this server. Restores any transient fields, and sets
     * all players as ghosts. This should only be called during server
     * initialization before any players have connected.
     */
    public void setGame(IGame g) {
        game = g;

        // reattach the transient fields and ghost the players
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity ent = e.nextElement();
            ent.setGame(game);
        }
        game.setOutOfGameEntitiesVector(game.getOutOfGameEntitiesVector());
        for (Enumeration<Player> e = game.getPlayers(); e.hasMoreElements();) {
            Player p = e.nextElement();
            p.setGame(game);
            p.setGhost(true);
        }

    }

    /**
     * Resets all the connections. The only use of this right now is when games are loaded after
     * clients have connected
     */
    public void resetConnections() {
        for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            send(conn.getId(), new Packet(Packet.COMMAND_RESET_CONNECTION));
        }
    }
    
    /** Returns the current game object */
    public IGame getGame() {
        return game;
    }

    /**
     * Make a default message o' the day containing the version string, and if
     * it was found, the build timestamp
     */
    private String createMotd() {
        StringBuffer buf = new StringBuffer();
        buf.append("Welcome to MegaMek.  Server is running version ");
        buf.append(MegaMek.VERSION);
        buf.append(", build date ");
        if (MegaMek.TIMESTAMP > 0L) {
            buf.append(new Date(MegaMek.TIMESTAMP).toString());
        } else {
            buf.append("unknown");
        }
        buf.append('.');

        return buf.toString();
    }

    /**
     * @return true if the server has a password
     */
    public boolean isPassworded() {
        return password != null;
    }

    /**
     * @return true if the password matches
     */
    public boolean isPassword(Object guess) {
        return password.equals(guess);
    }

    /**
     * Registers a new command in the server command table
     */
    private void registerCommand(ServerCommand command) {
        commandsHash.put(command.getName(), command);
    }

    /**
     * Returns the command associated with the specified name
     */
    public ServerCommand getCommand(String name) {
        return commandsHash.get(name);
    }

    /**
     * Shuts down the server.
     */
    public void die() {
        timer.cancel();

        // kill thread accepting new connections
        connector = null;

        // close socket
        try {
            serverSocket.close();
        } catch (IOException ex) {
        }

        // kill pending connnections
        for (Enumeration<IConnection> i = connectionsPending.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            conn.close();
        }
        connectionsPending.removeAllElements();

        // Send "kill" commands to all connections
        // N.B. I may be starting a race here.
        for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            send(conn.getId(), new Packet(Packet.COMMAND_CLOSE_CONNECTION));
        }

        // kill active connnections
        for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            conn.close();
        }
        connections.removeAllElements();
        connectionIds.clear();
        System.out.flush();
    }

    /**
     * Returns an enumeration of all the command names
     */
    public Enumeration<String> getAllCommandNames() {
        return commandsHash.keys();
    }

    /**
     * Sent when a client attempts to connect.
     */
    void greeting(int cn) {
        // send server greeting -- client should reply with client info.
        sendToPending(cn, new Packet(Packet.COMMAND_SERVER_GREETING));
    }

    /**
     * Returns a free connection id.
     */
    public int getFreeConnectionId() {
        while (getPendingConnection(connectionCounter) != null || getConnection(connectionCounter) != null || getPlayer(connectionCounter) != null) {
            connectionCounter++;
        }
        return connectionCounter;
    }

    /**
     * Returns a free entity id. Perhaps this should be in Game instead.
     */
    public int getFreeEntityId() {
        return game.getNextEntityId();
    }

    /**
     * Allow the player to set whatever parameters he is able to
     */
    private void receivePlayerInfo(Packet packet, int connId) {
        Player player = (Player) packet.getObject(0);
        Player connPlayer = game.getPlayer(connId);
        if (null != connPlayer) {
            connPlayer.setColorIndex(player.getColorIndex());
            connPlayer.setStartingPos(player.getStartingPos());
            connPlayer.setTeam(player.getTeam());
            connPlayer.setCamoCategory(player.getCamoCategory());
            connPlayer.setCamoFileName(player.getCamoFileName());
            connPlayer.setNbrMFConventional(player.getNbrMFConventional());
            connPlayer.setNbrMFCommand(player.getNbrMFCommand());
            connPlayer.setNbrMFVibra(player.getNbrMFVibra());
            connPlayer.setNbrMFActive(player.getNbrMFActive());
            connPlayer.setNbrMFInferno(player.getNbrMFInferno());
            connPlayer.setConstantInitBonus(player.getConstantInitBonus());
        }
    }

    /**
     * Correct a duplicate playername
     * 
     * @param oldName
     *            the <code>String</code> old playername, that is a duplicate
     * @return the <code>String</code> new playername
     */
    private String correctDupeName(String oldName) {
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            Player player = i.nextElement();
            if (player.getName().equals(oldName)) {
                // We need to correct it.
                String newName = oldName;
                int dupNum = 2;
                try {
                    dupNum = Integer.parseInt(oldName.substring(oldName.lastIndexOf(".") + 1));
                    dupNum++;
                    newName = oldName.substring(0, oldName.lastIndexOf("."));
                } catch (Exception e) {
                    // If this fails, we don't care much.
                    // Just assume it's the first time for this name.
                    dupNum = 2;
                }
                newName = newName.concat(".").concat(Integer.toString(dupNum));
                return correctDupeName(newName);
            }
        }
        return oldName;
    }

    /**
     * Recieves a player name, sent from a pending connection, and connects that
     * connection.
     */
    private void receivePlayerName(Packet packet, int connId) {
        final IConnection conn = getPendingConnection(connId);
        String name = (String) packet.getObject(0);
        boolean returning = false;

        // this had better be from a pending connection
        if (conn == null) {
            System.out.println("server: got a client name from a non-pending" + " connection");
            return;
        }

        // check if they're connecting with the same name as a ghost player
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            Player player = i.nextElement();
            if (player.getName().equals(name)) {
                if (player.isGhost()) {
                    returning = true;
                    player.setGhost(false);
                    // switch id
                    connId = player.getId();
                    conn.setId(connId);
                }
            }
        }

        if (!returning) {
            // Check to avoid duplicate names...
            name = correctDupeName(name);
            send(connId, new Packet(Packet.COMMAND_SERVER_CORRECT_NAME, name));
        }

        // right, switch the connection into the "active" bin
        connectionsPending.removeElement(conn);
        connections.addElement(conn);
        connectionIds.put(new Integer(conn.getId()), conn);

        // add and validate the player info
        if (!returning) {
            game.addPlayer(connId, new Player(connId, name));
            validatePlayerInfo(connId);
        }

        // if it is not the lounge phase, this player becomes an observer
        Player player = getPlayer(connId);
        if (game.getPhase() != IGame.Phase.PHASE_LOUNGE && null != player && game.getEntitiesOwnedBy(player) < 1) {
            player.setObserver(true);
        }

        // send the player the motd
        sendServerChat(connId, motd);

        // send info that the player has connected
        send(createPlayerConnectPacket(connId));

        // tell them their local playerId
        send(connId, new Packet(Packet.COMMAND_LOCAL_PN, new Integer(connId)));

        // send current game info
        sendCurrentInfo(connId);

        try {
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (int i = 0; i < addresses.length; i++) {
                sendServerChat(connId, "Machine IP is " + addresses[i].getHostAddress());
            }
        } catch (UnknownHostException e) {
            // oh well.
        }

        // Send the port we're listening on. Only useful for the player
        // on the server machine to check.
        sendServerChat(connId, "Listening on port " + serverSocket.getLocalPort());

        // Get the player *again*, because they may have disconnected.
        player = getPlayer(connId);
        if (null != player) {
            StringBuffer buff = new StringBuffer();
            buff.append(player.getName()).append(" connected from ").append(getClient(connId).getInetAddress());
            String who = buff.toString();
            System.out.print("s: player #");
            System.out.print(connId);
            System.out.print(", ");
            System.out.println(who);

            sendServerChat(who);

        } // Found the player

    }

    /**
     * Sends a player the info they need to look at the current phase. This is
     * triggered when a player first connects to the server.
     */
    private void sendCurrentInfo(int connId) {
        // why are these two outside the player != null check below?
        transmitAllPlayerConnects(connId);
        send(connId, createGameSettingsPacket());
        send(connId, createPlanetaryConditionsPacket());

        Player player = game.getPlayer(connId);
        if (null != player) {
            send(connId, new Packet(Packet.COMMAND_SENDING_MINEFIELDS, player.getMinefields()));

            switch (game.getPhase()) {
            case PHASE_LOUNGE:
                send(connId, createMapSettingsPacket());
                // Send Entities *after* the Lounge Phase Change
                send(connId, new Packet(Packet.COMMAND_PHASE_CHANGE, game.getPhase()));
                if (doBlind()) {
                    send(connId, createFilteredFullEntitiesPacket(player));
                } else {
                    send(connId, createFullEntitiesPacket());
                }
                break;
            default:
                send(connId, new Packet(Packet.COMMAND_ROUND_UPDATE, new Integer(game.getRoundCount())));
                // send(connId, createReportPacket(player));
                send(connId, createAllReportsPacket(player));

                // Send entities *before* other phase changes.
                if (doBlind()) {
                    send(connId, createFilteredFullEntitiesPacket(player));
                } else {
                    send(connId, createFullEntitiesPacket());
                }
                player.setDone(game.getEntitiesOwnedBy(player) <= 0);
                send(connId, createBoardPacket());
                send(connId, new Packet(Packet.COMMAND_PHASE_CHANGE, game.getPhase()));
                break;
            }
            if (game.getPhase() == IGame.Phase.PHASE_FIRING || game.getPhase() == IGame.Phase.PHASE_TARGETING || game.getPhase() == IGame.Phase.PHASE_OFFBOARD || game.getPhase() == IGame.Phase.PHASE_PHYSICAL) {
                // can't go above, need board to have been sent
                send(connId, createAttackPacket(game.getActionsVector(), 0));
                send(connId, createAttackPacket(game.getChargesVector(), 1));
                send(connId, createAttackPacket(game.getRamsVector(), 1));
                send(connId, createAttackPacket(game.getTeleMissileAttacksVector(), 1));
                send(connId, createAttackPacket(game.getLayMinefieldActionsVector(), 2));
            }
            if (game.phaseHasTurns(game.getPhase())) {
                send(connId, createTurnVectorPacket());
                send(connId, createTurnIndexPacket());
            }

            send(connId, createArtilleryPacket(player));
            send(connId, createFlarePacket());
            send(connId, createSpecialHexDisplayPacket(connId));

        } // Found the player.

    }

    /**
     * Resend entities to the player called by seeall command
     */
    public void sendEntities(int connId) {
        if (doBlind()) {
            send(connId, createFilteredEntitiesPacket(getPlayer(connId)));
        } else {
            send(connId, createEntitiesPacket());
        }
    }

    /**
     * Validates the player info.
     */
    public void validatePlayerInfo(int playerId) {
        final Player player = getPlayer(playerId);

        // maybe this isn't actually useful
        // // replace characters we don't like with "X"
        // StringBuffer nameBuff = new StringBuffer(player.getName());
        // for (int i = 0; i < nameBuff.length(); i++) {
        // int chr = nameBuff.charAt(i);
        // if (LEGAL_CHARS.indexOf(chr) == -1) {
        // nameBuff.setCharAt(i, 'X');
        // }
        // }
        // player.setName(nameBuff.toString());

        // TODO: check for duplicate or reserved names

        // make sure colorIndex is unique
        boolean[] colorUsed = new boolean[Player.colorNames.length];
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player otherPlayer = i.nextElement();
            if (otherPlayer.getId() != playerId) {
                colorUsed[otherPlayer.getColorIndex()] = true;
            }
        }
        if (null != player && colorUsed[player.getColorIndex()]) {
            // find a replacement color;
            for (int i = 0; i < colorUsed.length; i++) {
                if (!colorUsed[i]) {
                    player.setColorIndex(i);
                    break;
                }
            }
        }

    }

    /**
     * Called when it's been determined that an actual player disconnected.
     * Notifies the other players and does the appropriate housekeeping.
     */
    void disconnected(Player player) {
        IGame.Phase phase = game.getPhase();

        // in the lounge, just remove all entities for that player
        if (phase == IGame.Phase.PHASE_LOUNGE) {
            removeAllEntitesOwnedBy(player);
        }

        // if a player has active entities, he becomes a ghost
        // except the VICTORY_PHASE when the dosconnected
        // player is most likely the Bot disconnected after receiving
        // the COMMAND_END_OF_GAME command
        // see the Bug 1225949.
        // TODO Perhaps there is a better solution to handle the Bot disconnect
        if (game.getEntitiesOwnedBy(player) > 0 && phase != IGame.Phase.PHASE_VICTORY) {
            player.setGhost(true);
            player.setDone(true);
            send(createPlayerUpdatePacket(player.getId()));
        } else {
            game.removePlayer(player.getId());
            send(new Packet(Packet.COMMAND_PLAYER_REMOVE, new Integer(player.getId())));
        }

        // make sure the game advances
        if (game.phaseHasTurns(game.getPhase()) && null != game.getTurn()) {
            if (game.getTurn().isValid(player.getId(), game)) {
                sendGhostSkipMessage(player);
            }
        } else {
            checkReady();
        }

        // notify other players
        sendServerChat(player.getName() + " disconnected.");

        // log it
        System.out.println("s: removed player " + player.getName());

        // Reset the game after Elvis has left the building.
        if (0 == game.getNoOfPlayers()) {
            resetGame();
        }
    }

    /**
     * Checks each player to see if he has no entities, and if true, sets the
     * observer flag for that player. An exception is that there are no
     * observers during the lounge phase.
     */
    public void checkForObservers() {
        for (Enumeration<Player> e = game.getPlayers(); e.hasMoreElements();) {
            Player p = e.nextElement();
            p.setObserver(game.getEntitiesOwnedBy(p) < 1 && game.getPhase() != IGame.Phase.PHASE_LOUNGE);
        }
    }

    /**
     * Reset the game back to the lounge. TODO: couldn't this be a hazard if
     * there are other things executing at the same time?
     */
    public void resetGame() {
        // remove all entities
        game.reset();
        send(createEntitiesPacket());
        send(new Packet(Packet.COMMAND_SENDING_MINEFIELDS, new Vector<Object>()));

        // remove ghosts
        ArrayList<Player> ghosts = new ArrayList<Player>();
        for (Enumeration<Player> players = game.getPlayers(); players.hasMoreElements();) {
            Player p = players.nextElement();
            if (p.isGhost()) {
                ghosts.add(p);
            } else {
                // non-ghosts set their starting positions to any
                p.setStartingPos(0);
                send(createPlayerUpdatePacket(p.getId()));
            }
        }
        for (Player p : ghosts) {
            game.removePlayer(p.getId());
            send(new Packet(Packet.COMMAND_PLAYER_REMOVE, new Integer(p.getId())));
        }

        // reset all players
        resetPlayersDone();
        transmitAllPlayerDones();

        // Write end of game to stdout so controlling scripts can rotate logs.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        System.out.print(format.format(new Date()));
        System.out.println(" END OF GAME");

        changePhase(IGame.Phase.PHASE_LOUNGE);
    }

    /**
     * automatically save the game
     */
    public void autoSave() {
        String fileName = "autosave";
        if (PreferenceManager.getClientPreferences().stampFilenames()) {
            fileName = StringUtil.addDateTimeStamp(fileName);
        }
        saveGame(fileName, game.getOptions().booleanOption("autosave_msg"));
    }

    /**
     * save the game and send it to the sepecified connection
     * 
     * @param connId
     *            The <code>int</code> connection id to send to
     * @param sFile
     *            The <code>String</code> filename to use
     */
    public void sendSaveGame(int connId, String sFile) {
        saveGame(sFile, false);
        String sFinalFile = sFile;
        if (!sFinalFile.endsWith(".sav")) {
            sFinalFile = sFile + ".sav";
        }
        String localFile = "savegames" + File.separator + sFinalFile;
        File f = new File(localFile);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            send(connId, new Packet(Packet.COMMAND_SEND_SAVEGAME, new Object[] { sFinalFile, ois.readObject() }));
            sendChat(connId, "***Server", "Savegame has been sent to you.");
            ois.close();
        } catch (Exception e) {
            System.err.println("Unable to load file: " + f);
            e.printStackTrace();
        }
    }

    /**
     * save the game
     * 
     * @param sFile
     *            The <code>String</code> filename to use
     * @param sendChat
     *            A <code>boolean</code> value wether or not to announce the
     *            saving to the server chat.
     */
    public void saveGame(String sFile, boolean sendChat) {
        String sFinalFile = sFile;
        if (!sFinalFile.endsWith(".sav")) {
            sFinalFile = sFile + ".sav";
        }
        try {
            File sDir = new File("savegames");
            if (!sDir.exists()) {
                sDir.mkdir();
            }
            sFinalFile = sDir + File.separator + sFinalFile;
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sFinalFile));
            oos.writeObject(game);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            System.err.println("Unable to save file: " + sFinalFile);
            e.printStackTrace();
        }

        if (sendChat)
            sendChat("MegaMek", "Game saved to " + sFinalFile);
    }

    /**
     * save the game
     * 
     * @param sFile
     *            The <code>String</code> filename to use
     */
    public void saveGame(String sFile) {
        saveGame(sFile, true);
    }
    
    /**
     * send a packet to the connection tells it load a locally saved game
     * 
     * @param connId
     *            The <code>int</code> connection id to send to
     * @param sFile
     *            The <code>String</code> filename to use
     */
    public void sendLoadGame(int connId, String sFile) {
        String sFinalFile = sFile;
        if (!sFinalFile.endsWith(".sav")) {
            sFinalFile = sFile + ".sav";
        }
        send(connId, new Packet(Packet.COMMAND_LOAD_SAVEGAME, new Object[] { sFinalFile}));
    }

    /**
     * load the game
     * 
     * @param f
     *            The <code>File</code> to load
     * @return A <code>boolean</code> value wether or not the loading was
     *         successfull
     */
    public boolean loadGame(File f) {
        System.out.println("s: loading saved game file '" + f + '\'');
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            game = (IGame) ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.err.println("Unable to load file: " + f);
            e.printStackTrace();
            return false;
        }

        // a bit redundant, but there's some initialization code there
        setGame(game);

        return true;
    }

    /**
     * Shortcut to game.getPlayer(id)
     */
    public Player getPlayer(int id) {
        return game.getPlayer(id);
    }

    /**
     * Removes all entities owned by a player. Should only be called when it
     * won't cause trouble (the lounge, for instance, or between phases.)
     * 
     * @param player
     *            whose entites are to be removed
     */
    private void removeAllEntitesOwnedBy(Player player) {
        Vector<Entity> toRemove = new Vector<Entity>();

        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            final Entity entity = e.nextElement();

            if (entity.getOwner().equals(player)) {
                toRemove.addElement(entity);
            }
        }

        for (Entity entity : toRemove) {
            int id = entity.getId();
            game.removeEntity(id, IEntityRemovalConditions.REMOVE_NEVER_JOINED);
            send(createRemoveEntityPacket(id, IEntityRemovalConditions.REMOVE_NEVER_JOINED));
        }
    }

    /**
     * a shorter name for getConnection()
     */
    private IConnection getClient(int connId) {
        return getConnection(connId);
    }

    /**
     * Returns a connection, indexed by id
     */
    public Enumeration<IConnection> getConnections() {
        return connections.elements();
    }

    /**
     * Returns a connection, indexed by id
     */
    public IConnection getConnection(int connId) {
        return connectionIds.get(new Integer(connId));
    }

    /**
     * Returns a pending connection
     */
    IConnection getPendingConnection(int connId) {
        for (Enumeration<IConnection> i = connectionsPending.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();

            if (conn.getId() == connId) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Called at the beginning of each game round to reset values on this entity
     * that are reset every round
     */
    private void resetEntityRound() {
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity entity = e.nextElement();

            entity.newRound(game.getRoundCount());
        }
    }

    /**
     * Called at the beginning of each phase. Sets and resets any entity
     * parameters that need to be reset.
     */
    private void resetEntityPhase(IGame.Phase phase) {
        // first, mark doomed entities as destroyed and flag them
        Vector<Entity> toRemove = new Vector<Entity>(0, 10);
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            final Entity entity = e.nextElement();

            if (entity.crew.isDoomed()) {
                entity.crew.setDoomed(false);
                entity.crew.setDead(true);
                if (entity instanceof Tank) {
                    entity.setCarcass(true);
                    ((Tank) entity).immobilize();
                } else {
                    entity.setDestroyed(true);
                }
            }

            if (entity.isDoomed()) {
                entity.setDestroyed(true);

                // Is this unit swarming somebody? Better let go before
                // it's too late.
                final int swarmedId = entity.getSwarmTargetId();
                if (Entity.NONE != swarmedId) {
                    final Entity swarmed = game.getEntity(swarmedId);
                    swarmed.setSwarmAttackerId(Entity.NONE);
                    entity.setSwarmTargetId(Entity.NONE);
                    Report r = new Report(5165);
                    r.subject = swarmedId;
                    r.addDesc(swarmed);
                    addReport(r);
                    entityUpdate(swarmedId);
                }
            }

            if (entity.isDestroyed()) {
                toRemove.addElement(entity);
            }
        }

        // actually remove all flagged entities
        for (Entity entity : toRemove) {
            int condition = IEntityRemovalConditions.REMOVE_SALVAGEABLE;
            if (!entity.isSalvage()) {
                condition = IEntityRemovalConditions.REMOVE_DEVASTATED;
            }

            entityUpdate(entity.getId());
            game.removeEntity(entity.getId(), condition);
            send(createRemoveEntityPacket(entity.getId(), condition));
        }

        // do some housekeeping on all the remaining
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            final Entity entity = e.nextElement();

            entity.applyDamage();

            entity.reloadEmptyWeapons();

            // reset damage this phase
            // tele-missiles need a record of damage last phase
            entity.damageThisRound += entity.damageThisPhase;
            entity.damageThisPhase = 0;
            entity.engineHitsThisRound = 0;
            entity.rolledForEngineExplosion = false;
            entity.dodging = false;

            // reset done to false

            if (phase == IGame.Phase.PHASE_DEPLOYMENT) {
                entity.setDone(!entity.shouldDeploy(game.getRoundCount()));
            } else {
                entity.setDone(false);
            }
            
            //if this is the deployment phase, then apply bombs and santa annas to
            //the appropriate aero entities
            //FIXME: there must be a better place to do this
            if( phase == IGame.Phase.PHASE_DEPLOYMENT ) {
                if(entity instanceof Aero) {
                    if(game.getOptions().booleanOption("at2_nukes") && 
                            (entity instanceof Dropship || entity instanceof Jumpship)) {
                        entity.applySantaAnna();
                    }
                    if(!(entity instanceof SmallCraft || entity instanceof Jumpship)) {
                        //a.applyBombs();
                    }
                }
            }

            // reset spotlights
            entity.setIlluminated(false);
            entity.setUsedSearchlight(false);
            entity.setCarefulStand(false);
            
            if ( entity instanceof MechWarrior ){
                ((MechWarrior)entity).setLanded(true);
            }
        }
    }

    /**
     * are we currently in a reporting phase
     * 
     * @return <code>true</code> if we are or <code>false</code> if not.
     */
    private boolean isReportingPhase() {

        if (game.getPhase() == IGame.Phase.PHASE_FIRING_REPORT || game.getPhase() == IGame.Phase.PHASE_INITIATIVE_REPORT || game.getPhase() == IGame.Phase.PHASE_MOVEMENT_REPORT || game.getPhase() == IGame.Phase.PHASE_OFFBOARD_REPORT || game.getPhase() == IGame.Phase.PHASE_PHYSICAL_REPORT)
            return true;

        return false;
    }

    /**
     * Called at the beginning of certain phases to make every player not ready.
     */
    private void resetPlayersDone() {
        if (isReportingPhase())
            return;
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();
            player.setDone(false);
        }
        transmitAllPlayerDones();
    }

    /**
     * Called at the beginning of certain phases to make every active player not
     * ready.
     */
    private void resetActivePlayersDone() {
        if (isReportingPhase())
            return;
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();

            player.setDone(game.getEntitiesOwnedBy(player) <= 0);

        }
        transmitAllPlayerDones();
    }

    /**
     * Writes the victory report
     */
    private void prepareVictoryReport() {
        Report r;

        // remove carcasses to the graveyard
        Vector<Entity> toRemove = new Vector<Entity>();
        for (Entity e : game.getEntitiesVector()) {
            if (e.isCarcass() && !e.isDestroyed())
                toRemove.add(e);
        }
        for (Entity e : toRemove) {
            destroyEntity(e, "crew death", false, true);
            game.removeEntity(e.getId(), IEntityRemovalConditions.REMOVE_SALVAGEABLE);
            e.setDestroyed(true);
        }

        addReport(new Report(7000, Report.PUBLIC));

        // Declare the victor
        r = new Report(1210);
        r.type = Report.PUBLIC;
        if (game.getVictoryTeam() == Player.TEAM_NONE) {
            Player player = getPlayer(game.getVictoryPlayerId());
            if (null == player) {
                r.messageId = 7005;
            } else {
                r.messageId = 7010;
                r.add(player.getName());
            }
        } else {
            // Team victory
            r.messageId = 7015;
            r.add(game.getVictoryTeam());
        }
        addReport(r);

        // Show player BVs
        Enumeration<Player> players = game.getPlayers();
        while (players.hasMoreElements()) {
            Player player = players.nextElement();
            r = new Report();
            r.type = Report.PUBLIC;
            r.messageId = 7016;
            r.add(player.getName());
            r.add(player.getBV());
            r.add(player.getInitialBV());
            addReport(r);
        }

        // List the survivors
        Enumeration<Entity> survivors = game.getEntities();
        if (survivors.hasMoreElements()) {
            addReport(new Report(7020, Report.PUBLIC));
            while (survivors.hasMoreElements()) {
                Entity entity = survivors.nextElement();

                if (!entity.isDeployed())
                    continue;

                addReport(entity.victoryReport());
            }
        }
        // List units that never deployed
        Enumeration<Entity> undeployed = game.getEntities();
        if (undeployed.hasMoreElements()) {
            boolean wroteHeader = false;

            while (undeployed.hasMoreElements()) {
                Entity entity = undeployed.nextElement();

                if (entity.isDeployed())
                    continue;

                if (!wroteHeader) {
                    addReport(new Report(7075, Report.PUBLIC));
                    wroteHeader = true;
                }

                addReport(entity.victoryReport());
            }
        }
        // List units that retreated
        Enumeration<Entity> retreat = game.getRetreatedEntities();
        if (retreat.hasMoreElements()) {
            addReport(new Report(7080, Report.PUBLIC));
            while (retreat.hasMoreElements()) {
                Entity entity = retreat.nextElement();
                addReport(entity.victoryReport());
            }
        }
        // List destroyed units
        Enumeration<Entity> graveyard = game.getGraveyardEntities();
        if (graveyard.hasMoreElements()) {
            addReport(new Report(7085, Report.PUBLIC));
            while (graveyard.hasMoreElements()) {
                Entity entity = graveyard.nextElement();
                addReport(entity.victoryReport());
            }
        }
        // List devastated units (not salvagable)
        Enumeration<Entity> devastated = game.getDevastatedEntities();
        if (devastated.hasMoreElements()) {
            addReport(new Report(7090, Report.PUBLIC));

            while (devastated.hasMoreElements()) {
                Entity entity = devastated.nextElement();
                addReport(entity.victoryReport());
            }
        }
        // Let player know about entitystatus.txt file
        addReport(new Report(7095, Report.PUBLIC));
    }

    /**
     * Generates a detailed report for campaign use
     */
    private String getDetailedVictoryReport() {
        StringBuffer sb = new StringBuffer();

        Vector<Entity> vAllUnits = new Vector<Entity>();
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            vAllUnits.addElement(i.nextElement());
        }

        for (Enumeration<Entity> i = game.getRetreatedEntities(); i.hasMoreElements();) {
            vAllUnits.addElement(i.nextElement());
        }

        for (Enumeration<Entity> i = game.getGraveyardEntities(); i.hasMoreElements();) {
            vAllUnits.addElement(i.nextElement());
        }

        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {

            // Record the player.
            Player p = i.nextElement();
            sb.append("++++++++++ ").append(p.getName()).append(" ++++++++++");
            sb.append(CommonConstants.NL);

            // Record the player's alive, retreated, or salvageable units.
            for (int x = 0; x < vAllUnits.size(); x++) {
                Entity e = vAllUnits.elementAt(x);
                if (e.getOwner() == p) {
                    sb.append(UnitStatusFormatter.format(e));
                }
            }

            // Record the player's devastated units.
            Enumeration<Entity> devastated = game.getDevastatedEntities();
            if (devastated.hasMoreElements()) {
                sb.append("=============================================================");
                sb.append(CommonConstants.NL);
                sb.append("The following utterly destroyed units are not available for salvage:");
                sb.append(CommonConstants.NL);
                while (devastated.hasMoreElements()) {
                    Entity e = devastated.nextElement();
                    if (e.getOwner() == p) {
                        sb.append(e.getShortName()).append(", Pilot: ").append(e.getCrew().getName()).append(" (").append(e.getCrew().getGunnery()).append('/').append(e.getCrew().getPiloting()).append(')');
                        sb.append(CommonConstants.NL);
                    }
                } // Handle the next unsalvageable unit for the player
                sb.append("=============================================================");
                sb.append(CommonConstants.NL);
            }

        } // Handle the next player

        return sb.toString();
    }

    /**
     * Forces victory for the specified player, or his/her team at the end of
     * the round.
     */
    public void forceVictory(Player victor) {
        game.setForceVictory(true);
        if (victor.getTeam() == Player.TEAM_NONE) {
            game.setVictoryPlayerId(victor.getId());
            game.setVictoryTeam(Player.TEAM_NONE);
        } else {
            game.setVictoryPlayerId(Player.PLAYER_NONE);
            game.setVictoryTeam(victor.getTeam());
        }

        Vector<Player> players = game.getPlayersVector();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.elementAt(i);
            player.setAdmitsDefeat(false);
        }
    }

    /** Cancels the force victory */
    public void cancelVictory() {
        game.setForceVictory(false);
        game.setVictoryPlayerId(Player.PLAYER_NONE);
        game.setVictoryTeam(Player.TEAM_NONE);
    }

    /**
     * Called when a player declares that he is "done." Checks to see if all
     * players are done, and if so, moves on to the next phase.
     */
    private void checkReady() {
        // check if all active players are done
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();
            if (!player.isGhost() && !player.isObserver() && !player.isDone()) {
                return;
            }
        }

        // Tactical Genius pilot special ability (lvl 3)
        if (game.getNoOfInitiativeRerollRequests() > 0) {
            resetActivePlayersDone();
            game.rollInitAndResolveTies();

            determineTurnOrder(IGame.Phase.PHASE_INITIATIVE);
            clearReports();
            writeInitiativeReport(true);
            sendReport(true);
            return; // don't end the phase yet, players need to see new report
        }

        // need at least one entity in the game for the lounge phase to end
        if (!game.phaseHasTurns(game.getPhase()) && (game.getPhase() != IGame.Phase.PHASE_LOUNGE || game.getNoOfEntities() > 0)) {
            endCurrentPhase();
        }
    }

    /**
     * Called when the current player has done his current turn and the turn
     * counter needs to be advanced. Also enforces the "protos_move_multi" and
     * the "protos_move_multi" option. If the player has just moved
     * infantry/protos with a "normal" turn, adds up to
     * Game.INF_AND_PROTOS_MOVE_MULTI - 1 more infantry/proto-specific turns
     * after the current turn.
     */
    private void endCurrentTurn(Entity entityUsed) {

        // Enforce "inf_move_multi" and "protos_move_multi" options.
        // The "isNormalTurn" flag is checking to see if any non-Infantry
        // or non-Protomech units can move during the current turn.
        boolean turnsChanged = false;
        boolean outOfOrder = false;
        GameTurn turn = game.getTurn();
        if(game.isPhaseSimultaneous() && !turn.isValid(entityUsed.getOwnerId(), game)) {
            //turn played out of order
            outOfOrder = true;
            entityUsed.setDone(false);
            GameTurn removed = game.removeFirstTurnFor(entityUsed);
            entityUsed.setDone(true);
            turnsChanged = true;
            if(removed != null) turn = removed;
        }
        final int playerId = null == entityUsed ? Player.PLAYER_NONE : entityUsed.getOwnerId();
        boolean infMoved = entityUsed instanceof Infantry;
        boolean infMoveMulti = game.getOptions().booleanOption("inf_move_multi") && (game.getPhase() == IGame.Phase.PHASE_MOVEMENT || game.getPhase() == IGame.Phase.PHASE_INITIATIVE);
        boolean protosMoved = entityUsed instanceof Protomech;
        boolean protosMoveMulti = game.getOptions().booleanOption("protos_move_multi");
        boolean tanksMoved = entityUsed instanceof Tank;
        boolean tanksMoveMulti = game.getOptions().booleanOption("vehicle_lance_movement") && (game.getPhase() == IGame.Phase.PHASE_MOVEMENT || game.getPhase() == IGame.Phase.PHASE_INITIATIVE);

        // If infantry or protos move multi see if any
        // other unit types can move in the current turn.
        int multiMask = 0;
        if (infMoveMulti) {
            multiMask += GameTurn.CLASS_INFANTRY;
        }
        if (protosMoveMulti) {
            multiMask += GameTurn.CLASS_PROTOMECH;
        }

        // If a proto declared fire and protos don't move
        // multi, ignore whether infantry move or not.
        else if (protosMoved && game.getPhase() == IGame.Phase.PHASE_FIRING) {
            multiMask = 0;
        }

        if (tanksMoveMulti) {
            multiMask += GameTurn.CLASS_TANK;
        }

        // Is this a general move turn?
        boolean isGeneralMoveTurn = !(turn instanceof GameTurn.SpecificEntityTurn) && !(turn instanceof GameTurn.UnitNumberTurn) && !(turn instanceof GameTurn.UnloadStrandedTurn) && (!(turn instanceof GameTurn.EntityClassTurn) || turn instanceof GameTurn.EntityClassTurn && ((GameTurn.EntityClassTurn) turn).isValidClass(~multiMask));

        // Unless overridden by the "protos_move_multi" option, all Protomechs
        // in a unit declare fire, and they don't mix with infantry.
        if (protosMoved && !protosMoveMulti && isGeneralMoveTurn && entityUsed != null) {

            // What's the unit number and ID of the entity used?
            final char movingUnit = entityUsed.getUnitNumber();
            final int movingId = entityUsed.getId();

            // How many other Protomechs are in the unit that can fire?
            int protoTurns = game.getSelectedEntityCount(new EntitySelector() {
                private final int ownerId = playerId;

                private final int entityId = movingId;

                private final char unitNum = movingUnit;

                public boolean accept(Entity entity) {
                    if (entity instanceof Protomech && entity.isSelectableThisTurn() && ownerId == entity.getOwnerId() && entityId != entity.getId() && unitNum == entity.getUnitNumber())
                        return true;
                    return false;
                }
            });

            // Add the correct number of turns for the Protomech unit number.
            for (int i = 0; i < protoTurns; i++) {
                GameTurn newTurn = new GameTurn.UnitNumberTurn(playerId, movingUnit);
                game.insertNextTurn(newTurn);
                turnsChanged = true;
            }
        }

        // Otherwise, we may need to add turns for the "*_move_multi" options.
        else if ((infMoved && infMoveMulti || protosMoved && protosMoveMulti) && isGeneralMoveTurn) {
            int remaining = 0;

            // Calculate the number of EntityClassTurns need to be added.
            if (infMoveMulti) {
                remaining += game.getInfantryLeft(playerId);
            }
            if (protosMoveMulti) {
                remaining += game.getProtomechsLeft(playerId);
            }
            int moreInfAndProtoTurns = Math.min(game.getOptions().intOption("inf_proto_move_multi") - 1, remaining);

            // Add the correct number of turns for the right unit classes.
            for (int i = 0; i < moreInfAndProtoTurns; i++) {
                GameTurn newTurn = new GameTurn.EntityClassTurn(playerId, multiMask);
                game.insertNextTurn(newTurn);
                turnsChanged = true;
            }
        }

        if (tanksMoved && tanksMoveMulti && isGeneralMoveTurn) {
            int remaining = game.getVehiclesLeft(playerId);

            int moreVeeTurns = Math.min(game.getOptions().intOption("vehicle_lance_movement_number") - 1, remaining);

            // Add the correct number of turns for the right unit classes.
            for (int i = 0; i < moreVeeTurns; i++) {
                GameTurn newTurn = new GameTurn.EntityClassTurn(playerId, multiMask);
                game.insertNextTurn(newTurn);
                turnsChanged = true;
            }
        }

        // brief everybody on the turn update, if they changed
        if (turnsChanged) {
            send(createTurnVectorPacket());
        }

        // move along
        if(outOfOrder) {
            send(createTurnIndexPacket());
        } else {
            changeToNextTurn();
        }
    }

    /**
     * Changes the current phase, does some bookkeeping and then tells the
     * players.
     * 
     * @param phase
     *            the <code>int</code> id of the phase to change to
     */
    private void changePhase(IGame.Phase phase) {
        game.setLastPhase(game.getPhase());
        game.setPhase(phase);

        // prepare for the phase
        prepareForPhase(phase);

        if (isPhasePlayable(phase)) {
            // tell the players about the new phase
            send(new Packet(Packet.COMMAND_PHASE_CHANGE, phase));

            // post phase change stuff
            executePhase(phase);
        } else {
            endCurrentPhase();
        }
    }

    /**
     * Prepares for, presumably, the next phase. This typically involves
     * resetting the states of entities in the game and making sure the client
     * has the information it needs for the new phase.
     * 
     * @param phase
     *            the <code>int</code> id of the phase to prepare for
     */
    private void prepareForPhase(IGame.Phase phase) {
        switch (phase) {
        case PHASE_LOUNGE:
            clearReports();
            mapSettings.setBoardsAvailableVector(scanForBoards(mapSettings.getBoardWidth(), mapSettings.getBoardHeight()));
            mapSettings.setNullBoards(DEFAULT_BOARD);
            send(createMapSettingsPacket());
            break;
        case PHASE_INITIATIVE:
            // remove the last traces of last round
            game.resetActions();
            game.resetTagInfo();
            clearReports();
            resetEntityRound();
            resetEntityPhase(phase);
            checkForObservers();
            
            // roll 'em
            resetActivePlayersDone();
            rollInitiative();

            if (!game.shouldDeployThisRound())
                incrementAndSendGameRound();

            // setIneligible(phase);
            determineTurnOrder(phase);
            writeInitiativeReport(false);
            
            //checks for environmental survival
            checkForConditionDeath();
            if (game.getBoard().inAtmosphere()) {
                checkForAtmosphereDeath();
            }
            if (game.getBoard().inSpace()) {
                checkForSpaceDeath();
            }
            
            System.out.println("Round " + game.getRoundCount() + " memory usage: " + MegaMek.getMemoryUsed());
            break;
        case PHASE_DEPLOY_MINEFIELDS:
            checkForObservers();
            resetActivePlayersDone();
            setIneligible(phase);

            Enumeration<Player> e = game.getPlayers();
            Vector<GameTurn> turns = new Vector<GameTurn>();
            while (e.hasMoreElements()) {
                Player p = e.nextElement();
                if (p.hasMinefields() && game.getBoard().onGround()) {
                    GameTurn gt = new GameTurn(p.getId());
                    turns.addElement(gt);
                }
            }
            game.setTurnVector(turns);
            game.resetTurnIndex();

            // send turns to all players
            send(createTurnVectorPacket());
            break;
        case PHASE_SET_ARTYAUTOHITHEXES:
            // place off board entities actually off-board
            Enumeration<Entity> entities = game.getEntities();
            while (entities.hasMoreElements()) {
                Entity en = entities.nextElement();
                en.deployOffBoard();
            }
            checkForObservers();
            resetActivePlayersDone();
            setIneligible(phase);

            Enumeration<Player> players = game.getPlayers();
            Vector<GameTurn> turn = new Vector<GameTurn>();

            // Walk through the players of the game, and add
            // a turn for all players with artillery weapons.
            while (players.hasMoreElements()) {

                // Get the next player.
                final Player p = players.nextElement();

                // Does the player have any artillery-equipped units?
                EntitySelector playerArtySelector = new EntitySelector() {
                    private Player owner = p;

                    public boolean accept(Entity entity) {
                        if (owner.equals(entity.getOwner()) && entity.isEligibleForTargetingPhase())
                            return true;
                        return false;
                    }
                };
                if (game.getSelectedEntities(playerArtySelector).hasMoreElements()) {

                    // Yes, the player has arty-equipped units.
                    GameTurn gt = new GameTurn(p.getId());
                    turn.addElement(gt);
                }
            }
            game.setTurnVector(turn);
            game.resetTurnIndex();

            // send turns to all players
            send(createTurnVectorPacket());
            break;
        case PHASE_MOVEMENT:
        case PHASE_DEPLOYMENT:
        case PHASE_FIRING:
        case PHASE_PHYSICAL:
        case PHASE_TARGETING:
        case PHASE_OFFBOARD:
            resetEntityPhase(phase);
            checkForObservers();
            setIneligible(phase);
            determineTurnOrder(phase);
            resetActivePlayersDone();
            // send(createEntitiesPacket());
            entityAllUpdate();
            clearReports();
            doTryUnstuck();
            break;
        case PHASE_END:
            resetEntityPhase(phase);
            clearReports();
            resolveHeat();
            if(game.getPlanetaryConditions().isSandBlowing() 
                    && game.getPlanetaryConditions().getWindStrength() > PlanetaryConditions.WI_LIGHT_GALE) {
                addReport(resolveBlowingSandDamage());
            }
            addReport(resolveControlRolls());
            // write End Phase header
            addReport(new Report(5005, Report.PUBLIC));
            checkForSuffocation();
            game.getPlanetaryConditions().determineWind();
            send(createPlanetaryConditionsPacket());
            /*
            checkForConditionDeath();
            if (game.getBoard().inAtmosphere()) {
                checkForAtmosphereDeath();
            }
            if (game.getBoard().inSpace()) {
                checkForSpaceDeath();
            }
            */
            for (DynamicTerrainProcessor tp : terrainProcessors) {
                tp.doEndPhaseChanges(vPhaseReport);
            }
            addReport(game.ageFlares());
            send(createFlarePacket());
            resolveExtremeTempInfantryDeath();
            resolveAmmoDumps();
            resolveCrewWakeUp();
            resolveMechWarriorPickUp();
            resolveVeeINarcPodRemoval();
            resolveFortify();
            checkForObservers();
            entityAllUpdate();
            break;
        case PHASE_INITIATIVE_REPORT:
            autoSave();
            // Show player BVs
            Enumeration<Player> players2 = game.getPlayers();
            while (players2.hasMoreElements()) {
                Player player = players2.nextElement();
                Report r = new Report();
                r.type = Report.PUBLIC;
                if (doBlind()) {
                    r.type = Report.PLAYER;
                    r.player = player.getId();
                }
                r.messageId = 7016;
                r.add(player.getName());
                r.add(player.getBV());
                r.add(player.getInitialBV());
                addReport(r);
            }
        case PHASE_TARGETING_REPORT:
        case PHASE_MOVEMENT_REPORT:
        case PHASE_OFFBOARD_REPORT:
        case PHASE_FIRING_REPORT:
        case PHASE_PHYSICAL_REPORT:
        case PHASE_END_REPORT:
            resetActivePlayersDone();
            sendReport();
            if (game.getOptions().booleanOption("paranoid_autosave"))
                autoSave();
            break;
        case PHASE_VICTORY:
            resetPlayersDone();
            clearReports();
            prepareVictoryReport();
            game.addReports(vPhaseReport);
            send(createFullEntitiesPacket());
            send(createReportPacket(null));
            send(createEndOfGamePacket());
            break;
        }
    }

    /**
     * Should we play this phase or skip it?
     */
    private boolean isPhasePlayable(IGame.Phase phase) {
        switch (phase) {
        case PHASE_INITIATIVE:
        case PHASE_END:
            return false;
        case PHASE_SET_ARTYAUTOHITHEXES:
        case PHASE_DEPLOY_MINEFIELDS:
        case PHASE_DEPLOYMENT:
        case PHASE_MOVEMENT:
        case PHASE_FIRING:
        case PHASE_PHYSICAL:
        case PHASE_TARGETING:
            return game.hasMoreTurns();
        case PHASE_OFFBOARD:
            return isOffboardPlayable();
        default:
            return true;
        }
    }

    /**
     * Skip offboard phase, if there is no homing / semiguided ammo in play
     */
    private boolean isOffboardPlayable() {
        if (!game.hasMoreTurns())
            return false;
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity entity = e.nextElement();
            for (Mounted m : entity.getAmmo()) {
                AmmoType atype = (AmmoType) m.getType();
                if ((atype.getAmmoType() == AmmoType.T_LRM || atype.getAmmoType() == AmmoType.T_MML) && atype.getMunitionType() == AmmoType.M_SEMIGUIDED) {
                    return true;
                }
                if ((atype.getAmmoType() == AmmoType.T_ARROW_IV || atype.getAmmoType() == AmmoType.T_LONG_TOM || atype.getAmmoType() == AmmoType.T_SNIPER) && atype.getMunitionType() == AmmoType.M_HOMING) {
                    return true;
                }
            }
        }
        // loop through all current attacks
        // if there are any that use homing ammo, we are playable
        // we need to do this because we might have a homing arty shot in flight
        // when the unit that mounted that ammo is no longer on the field
        for (Enumeration<AttackHandler> attacks = game.getAttacks(); attacks.hasMoreElements();) {
            AttackHandler ah = attacks.nextElement();
            Mounted ammo = ah.getWaa().getEntity(game).getEquipment(ah.getWaa().getAmmoId());
            if (ammo != null) {
                AmmoType atype = (AmmoType) ammo.getType();
                if (atype.getMunitionType() == AmmoType.M_HOMING) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Do anything we seed to start the new phase, such as give a turn to the
     * first player to play.
     */
    private void executePhase(IGame.Phase phase) {
        switch (phase) {
        case PHASE_EXCHANGE:
            resetPlayersDone();
            calculatePlayerBVs();
            // Build teams vector
            game.setupTeams();
            applyBoardSettings();
            game.getPlanetaryConditions().determineWind();
            send(createPlanetaryConditionsPacket());
            game.setupRoundDeployment();
            game.setVictoryContext(new HashMap<String, Object>());
            game.createVictoryConditions();
            // If we add transporters for any Magnetic Clamp
            // equiped squads, then update the clients' entities.
            if (game.checkForMagneticClamp()) {
                entityAllUpdate();
            }
            // transmit the board to everybody
            send(createBoardPacket());
            break;
        case PHASE_MOVEMENT:
            // write Movement Phase header to report
            addReport(new Report(2000, Report.PUBLIC));
        case PHASE_SET_ARTYAUTOHITHEXES:
        case PHASE_DEPLOY_MINEFIELDS:
        case PHASE_DEPLOYMENT:
        case PHASE_FIRING:
        case PHASE_PHYSICAL:
        case PHASE_TARGETING:
        case PHASE_OFFBOARD:
            changeToNextTurn();
            if (game.getOptions().booleanOption("paranoid_autosave"))
                autoSave();
            break;
        }
    }

    /**
     * Calculates all players initial BV, should only be called at start of game
     */
    public void calculatePlayerBVs() {
        for (Enumeration<Player> players = game.getPlayers(); players.hasMoreElements();)
            players.nextElement().setInitialBV();
    }

    /**
     * Ends this phase and moves on to the next.
     */
    private void endCurrentPhase() {
        switch (game.getPhase()) {
        case PHASE_LOUNGE:
            changePhase(IGame.Phase.PHASE_EXCHANGE);
            break;
        case PHASE_EXCHANGE:
            changePhase(IGame.Phase.PHASE_SET_ARTYAUTOHITHEXES);
            break;
        case PHASE_STARTING_SCENARIO:
            changePhase(IGame.Phase.PHASE_SET_ARTYAUTOHITHEXES);
            break;
        case PHASE_SET_ARTYAUTOHITHEXES:
            sendSpecialHexDisplayPackets();
            Enumeration<Player> e = game.getPlayers();
            boolean mines = false;
            while (e.hasMoreElements() && !mines) {
                Player p = e.nextElement();
                if (p.hasMinefields()) {
                    mines = true;
                }
            }
            if (mines) {
                changePhase(IGame.Phase.PHASE_DEPLOY_MINEFIELDS);
            } else {
                changePhase(IGame.Phase.PHASE_INITIATIVE);
            }
            break;
        case PHASE_DEPLOY_MINEFIELDS:
            changePhase(IGame.Phase.PHASE_INITIATIVE);
            break;
        case PHASE_DEPLOYMENT:
            game.clearDeploymentThisRound();
            game.checkForCompleteDeployment();
            Enumeration<Player> pls = game.getPlayers();
            while (pls.hasMoreElements()) {
                Player p = pls.nextElement();
                p.adjustStartingPosForReinforcements();
            }

            if (game.getRoundCount() < 1) {
                changePhase(IGame.Phase.PHASE_INITIATIVE);
            } else {
                changePhase(IGame.Phase.PHASE_TARGETING);
            }
            break;
        case PHASE_INITIATIVE:
            game.addReports(vPhaseReport);
            changePhase(IGame.Phase.PHASE_INITIATIVE_REPORT);
            break;
        case PHASE_INITIATIVE_REPORT:
            // boolean doDeploy = game.shouldDeployThisRound() &&
            // (game.getLastPhase() != IGame.Phase.PHASE_DEPLOYMENT);
            if (game.shouldDeployThisRound()) {
                changePhase(IGame.Phase.PHASE_DEPLOYMENT);
            } else {
                changePhase(IGame.Phase.PHASE_TARGETING);
            }
            break;
        case PHASE_MOVEMENT:
            doAllAssaultDrops();
            addMovementHeat();
            applyBuildingDamage();
            checkForPSRFromDamage();
            addReport(resolvePilotingRolls()); // Skids cause damage in
                                                // movement phase
            checkForFlamingDamage();
            checkForTeleMissileAttacks();
            // check phase report
            if (vPhaseReport.size() > 1) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_MOVEMENT_REPORT);
            } else {
                // just the header, so we'll add the <nothing> label
                addReport(new Report(1205, Report.PUBLIC));
                game.addReports(vPhaseReport);
                sendReport();
                changePhase(IGame.Phase.PHASE_OFFBOARD);
            }
            break;
        case PHASE_MOVEMENT_REPORT:
            changePhase(IGame.Phase.PHASE_OFFBOARD);
            break;
        case PHASE_FIRING:
            resolveAllButWeaponAttacks();
            reportGhostTargetRolls();
            resolveOnlyWeaponAttacks();
            assignAMS();
            handleAttacks();
            resolveScheduledNukes();
            applyBuildingDamage();
            checkForPSRFromDamage();
            addReport(resolvePilotingRolls());
            // check phase report
            if (vPhaseReport.size() > 1) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_FIRING_REPORT);
            } else {
                // just the header, so we'll add the <nothing> label
                addReport(new Report(1205, Report.PUBLIC));
                sendReport();
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_PHYSICAL);
            }
            break;
        case PHASE_FIRING_REPORT:
            changePhase(IGame.Phase.PHASE_PHYSICAL);
            break;
        case PHASE_PHYSICAL:
            resolvePhysicalAttacks();
            applyBuildingDamage();
            checkForPSRFromDamage();
            addReport(resolvePilotingRolls());
            resolveSinkVees();
            // check phase report
            if (vPhaseReport.size() > 1) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_PHYSICAL_REPORT);
            } else {
                // just the header, so we'll add the <nothing> label
                addReport(new Report(1205, Report.PUBLIC));
                game.addReports(vPhaseReport);
                sendReport();
                changePhase(IGame.Phase.PHASE_END);
            }
            break;
        case PHASE_PHYSICAL_REPORT:
            changePhase(IGame.Phase.PHASE_END);
            break;
        case PHASE_TARGETING:
            vPhaseReport.addElement(new Report(1035, Report.PUBLIC));
            resolveAllButWeaponAttacks();
            resolveOnlyWeaponAttacks();
            handleAttacks();
            // check reports
            if (vPhaseReport.size() > 1) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_TARGETING_REPORT);
            } else {
                // just the header, so we'll add the <nothing> label
                vPhaseReport.addElement(new Report(1205, Report.PUBLIC));
                game.addReports(vPhaseReport);
                sendReport();
                changePhase(IGame.Phase.PHASE_MOVEMENT);
            }

            sendSpecialHexDisplayPackets();

            break;
        case PHASE_OFFBOARD:
            // write Offboard Attack Phase header
            addReport(new Report(1100, Report.PUBLIC));
            resolveAllButWeaponAttacks(); // torso twist or flip arms
            // possible
            resolveOnlyWeaponAttacks(); // should only be TAG at this point
            handleAttacks();
            for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
                Player player = i.nextElement();
                int connId = player.getId();
                send(connId, createArtilleryPacket(player));
            }
            applyBuildingDamage();
            checkForPSRFromDamage();
            addReport(resolvePilotingRolls());

            sendSpecialHexDisplayPackets();

            // check reports
            if (vPhaseReport.size() > 1) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_OFFBOARD_REPORT);
            } else {
                // just the header, so we'll add the <nothing> label
                addReport(new Report(1205, Report.PUBLIC));
                game.addReports(vPhaseReport);
                sendReport();
                changePhase(IGame.Phase.PHASE_FIRING);
            }
            break;
        case PHASE_OFFBOARD_REPORT:
            sendSpecialHexDisplayPackets();
            changePhase(IGame.Phase.PHASE_FIRING);
            break;
        case PHASE_TARGETING_REPORT:
            changePhase(IGame.Phase.PHASE_MOVEMENT);
            break;
        case PHASE_END:
            // remove any entities that died in the heat/end phase before
            // check
            // for victory
            resetEntityPhase(IGame.Phase.PHASE_END);
            boolean victory = victory(); // note this may add reports
            // check phase report
            // HACK: hardcoded message ID check
            if (vPhaseReport.size() > 3 || vPhaseReport.elementAt(1).messageId != 1205) {
                game.addReports(vPhaseReport);
                changePhase(IGame.Phase.PHASE_END_REPORT);
            } else {
                // just the heat and end headers, so we'll add
                // the <nothing> label
                addReport(new Report(1205, Report.PUBLIC));
                game.addReports(vPhaseReport);
                sendReport();
                if (victory) {
                    changePhase(IGame.Phase.PHASE_VICTORY);
                } else {
                    changePhase(IGame.Phase.PHASE_INITIATIVE);
                }
            }
            break;
        case PHASE_END_REPORT:
            if (victory()) {
                changePhase(IGame.Phase.PHASE_VICTORY);
            } else {
                changePhase(IGame.Phase.PHASE_INITIATIVE);
            }
            break;
        case PHASE_VICTORY:
            resetGame();
            break;
        }
    }

    /**
     * 
     */
    private void sendSpecialHexDisplayPackets() {
        if (connections == null) {
            return;
        }
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i) != null) {
                connections.get(i).send(createSpecialHexDisplayPacket(i));
            }
        }
    }

    /**
     * Increment's the server's game round and send it to all the clients
     */
    private void incrementAndSendGameRound() {
        game.incrementRoundCount();
        send(new Packet(Packet.COMMAND_ROUND_UPDATE, new Integer(game.getRoundCount())));
    }

    /**
     * Tries to change to the next turn. If there are no more turns, ends the
     * current phase. If the player whose turn it is next is not connected, we
     * allow the other players to skip that player.
     */
    private void changeToNextTurn() {
        // if there aren't any more turns, end the phase
        if (!game.hasMoreTurns()) {
            endCurrentPhase();
            return;
        }

        // okay, well next turn then!
        GameTurn nextTurn = game.changeToNextTurn();

        Player player = getPlayer(nextTurn.getPlayerNum());

        if (player != null && game.getEntitiesOwnedBy(player) == 0) {
            endCurrentTurn(null);
            return;
        }

        send(createTurnIndexPacket());

        if (null != player && player.isGhost()) {
            sendGhostSkipMessage(player);
        } else if (null == game.getFirstEntity() && null != player && (game.getPhase() != IGame.Phase.PHASE_DEPLOY_MINEFIELDS && game.getPhase() != IGame.Phase.PHASE_SET_ARTYAUTOHITHEXES)) {
            sendTurnErrorSkipMessage(player);
        }
    }

    /**
     * Sends out a notification message indicating that a ghost player may be
     * skipped.
     * 
     * @param ghost -
     *            the <code>Player</code> who is ghosted. This value must not
     *            be <code>null</code>.
     */
    private void sendGhostSkipMessage(Player ghost) {
        StringBuffer message = new StringBuffer();
        message.append("Player '").append(ghost.getName()).append("' is disconnected.  You may skip his/her current turn with the /skip command.");
        sendServerChat(message.toString());
    }

    /**
     * Sends out a notification message indicating that the current turn is an
     * error and should be skipped.
     * 
     * @param skip -
     *            the <code>Player</code> who is to be skipped. This value
     *            must not be <code>null</code>.
     */
    private void sendTurnErrorSkipMessage(Player skip) {
        StringBuffer message = new StringBuffer();
        message.append("Player '").append(skip.getName()).append("' has no units to move.  You should skip his/her/your current turn with the /skip command. You may want to report this error.  See the MegaMek homepage (http://megamek.sf.net/) for details.");
        sendServerChat(message.toString());
    }

    /**
     * Skips the current turn. This only makes sense in phases that have turns.
     * Operates by finding an entity to move and then doing nothing with it.
     */
    public void skipCurrentTurn() {
        // find an entity to skip...
        Entity toSkip = game.getFirstEntity();

        switch (game.getPhase()) {
        case PHASE_DEPLOYMENT:
            // allow skipping during deployment,
            // we need that when someone removes a unit.
            endCurrentTurn(null);
            break;
        case PHASE_MOVEMENT:
            if (toSkip != null) {
                processMovement(toSkip, new MovePath(game, toSkip));
            }
            endCurrentTurn(toSkip);
            break;
        case PHASE_FIRING:
        case PHASE_PHYSICAL:
        case PHASE_TARGETING:
        case PHASE_OFFBOARD:
            if (toSkip != null) {
                processAttack(toSkip, new Vector<EntityAction>(0));
            }
            endCurrentTurn(toSkip);
            break;
        default:
        }
    }

    /**
     * Returns true if the current turn may be skipped. Ghost players' turns are
     * skippable, and a turn should be skipped if there's nothing to move.
     */
    public boolean isTurnSkippable() {
        GameTurn turn = game.getTurn();
        if (null == turn)
            return false;
        Player player = getPlayer(turn.getPlayerNum());
        return null == player || player.isGhost() || game.getFirstEntity() == null;
    }

    /**
     * Returns true if victory conditions have been met. Victory conditions are
     * when there is only one player left with mechs or only one team. will also
     * add some reports to reporting
     */
    public boolean victory() {
        Victory.Result vr = game.getVictory().victory(game, game.getVictoryContext());
        for (Report r : vr.getReports()) {
            addReport(r);
        }

        if (vr.victory()) {
            boolean draw = vr.isDraw();
            int wonPlayer = vr.getWinningPlayer();
            int wonTeam = vr.getWinningTeam();

            if (wonPlayer != Player.PLAYER_NONE) {
                Report r = new Report(7200, Report.PUBLIC);
                r.add(game.getPlayer(wonPlayer).getName());
                addReport(r);
            }
            if (wonTeam != Player.TEAM_NONE) {
                Report r = new Report(7200, Report.PUBLIC);
                r.add("Team " + wonTeam);
                addReport(r);
            }
            if (draw) {
                // multiple-won draw
                game.setVictoryPlayerId(Player.PLAYER_NONE);
                game.setVictoryTeam(Player.TEAM_NONE);
            } else {
                // nobody-won draw or
                // single player won or
                // single team won
                game.setVictoryPlayerId(wonPlayer);
                game.setVictoryTeam(wonTeam);
            }
        } else {
            game.setVictoryPlayerId(Player.PLAYER_NONE);
            game.setVictoryTeam(Player.TEAM_NONE);
            if (game.isForceVictory())
                cancelVictory();
        }
        return vr.victory();
    }// end victory

    private boolean isPlayerForcedVictory(){
        // check game options
        if (!game.getOptions().booleanOption("skip_forced_victory")) {
                return false;
        }

        if ( !game.isForceVictory() ){
            return false;
        }

        for ( Player player : game.getPlayersVector() ){
            
            if ( player.getId() == game.getVictoryPlayerId()
                    || (player.getTeam() == game.getVictoryTeam() && game.getVictoryTeam() != Player.TEAM_NONE) ){
                continue;
            }
                
            if ( !player.admitsDefeat() ){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Applies board settings. This loads and combines all the boards that were
     * specified into one mega-board and sets that board as current.
     */
    public void applyBoardSettings() {
        mapSettings.replaceBoardWithRandom(MapSettings.BOARD_RANDOM);
        mapSettings.replaceBoardWithRandom(MapSettings.BOARD_SURPRISE);
        IBoard[] sheetBoards = new IBoard[mapSettings.getMapWidth() * mapSettings.getMapHeight()];
        for (int i = 0; i < mapSettings.getMapWidth() * mapSettings.getMapHeight(); i++) {
            sheetBoards[i] = new Board();
            String name = mapSettings.getBoardsSelectedVector().get(i);
            boolean isRotated = false;
            if (name.startsWith(Board.BOARD_REQUEST_ROTATION)) {
                // only rotate boards with an even width
                if (mapSettings.getBoardWidth() % 2 == 0)
                    isRotated = true;
                name = name.substring(Board.BOARD_REQUEST_ROTATION.length());
            }
            if (name.startsWith(MapSettings.BOARD_GENERATED) || mapSettings.getMedium() == MapSettings.MEDIUM_SPACE) {
                sheetBoards[i] = BoardUtilities.generateRandom(mapSettings);
            } else {
                sheetBoards[i].load(name + ".board");
                BoardUtilities.flip(sheetBoards[i], isRotated, isRotated);
            }
        }
        IBoard newBoard = BoardUtilities.combine(mapSettings.getBoardWidth(), mapSettings.getBoardHeight(), mapSettings.getMapWidth(), mapSettings.getMapHeight(), sheetBoards, mapSettings.getMedium());
        if (game.getOptions().getOption("bridgeCF").intValue() > 0) {
            newBoard.setBridgeCF(game.getOptions().getOption("bridgeCF").intValue());
        }
        BoardUtilities.addWeatherConditions(newBoard, game.getPlanetaryConditions().getWeather(), game.getPlanetaryConditions().getWindStrength());
        game.setBoard(newBoard);
    }

    /**
     * Rolls initiative for all the players.
     */
    private void rollInitiative() {
        if (game.getOptions().booleanOption("individual_initiative")) {
            TurnOrdered.rollInitiative(game.getEntitiesVector());
        } else {
            // Roll for initative on the teams.
            TurnOrdered.rollInitiative(game.getTeamsVector());
        }

        transmitAllPlayerUpdates();
    }

    /**
     * Determines the turn oder for a given phase (with individual init)
     * 
     * @param phase
     *            the <code>int</code> id of the phase
     */
    private void determineTurnOrderIUI(IGame.Phase phase) {
        for (Enumeration<Entity> loop = game.getEntities(); loop.hasMoreElements();) {
            final Entity entity = loop.nextElement();
            entity.resetOtherTurns();
            if (entity.isSelectableThisTurn()) {
                entity.incrementOtherTurns();
            }
        }
        // Now, generate the global order of all teams' turns.
        TurnVectors team_order = TurnOrdered.generateTurnOrder(game.getEntitiesVector(), game);

        // See if there are any loaded units stranded on immobile transports.
        Enumeration<Entity> strandedUnits = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (Server.this.game.isEntityStranded(entity))
                    return true;
                return false;
            }
        });

        // Now, we collect everything into a single vector.
        Vector<GameTurn> turns;

        if (strandedUnits.hasMoreElements() && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
            // Add a game turn to unload stranded units, if this
            // is the movement phase.
            turns = new Vector<GameTurn>(team_order.getTotalTurns() + team_order.getEvenTurns() + 1);
            turns.addElement(new GameTurn.UnloadStrandedTurn(strandedUnits));
        } else {
            // No stranded units.
            turns = new Vector<GameTurn>(team_order.getTotalTurns() + team_order.getEvenTurns());
        }

        // add the turns (this is easy)
        while (team_order.hasMoreElements()) {
            Entity e = (Entity) team_order.nextElement();
            if (e.isSelectableThisTurn())
                turns.addElement(new GameTurn.SpecificEntityTurn(e.getOwnerId(), e.getId()));
        }

        // set fields in game
        game.setTurnVector(turns);
        game.resetTurnIndex();

        // send turns to all players
        send(createTurnVectorPacket());
    }

    /**
     * Determines the turn oder for a given phase
     * 
     * @param phase
     *            the <code>int</code> id of the phase
     */
    private void determineTurnOrder(IGame.Phase phase) {

        if (game.getOptions().booleanOption("individual_initiative")) {
            determineTurnOrderIUI(phase);
            return;
        }
        // and/or deploy even according to game options.
        boolean infMoveEven = game.getOptions().booleanOption("inf_move_even") && (game.getPhase() == IGame.Phase.PHASE_INITIATIVE || game.getPhase() == IGame.Phase.PHASE_MOVEMENT) || game.getOptions().booleanOption("inf_deploy_even") && game.getPhase() == IGame.Phase.PHASE_DEPLOYMENT;
        boolean infMoveMulti = game.getOptions().booleanOption("inf_move_multi") && (game.getPhase() == IGame.Phase.PHASE_INITIATIVE || game.getPhase() == IGame.Phase.PHASE_MOVEMENT);
        boolean protosMoveEven = game.getOptions().booleanOption("protos_move_even") && (game.getPhase() == IGame.Phase.PHASE_INITIATIVE || game.getPhase() == IGame.Phase.PHASE_MOVEMENT) || game.getOptions().booleanOption("protos_deploy_even") && game.getPhase() == IGame.Phase.PHASE_DEPLOYMENT;
        boolean protosMoveMulti = game.getOptions().booleanOption("protos_move_multi");
        boolean protosMoveByPoint = !protosMoveMulti;
        boolean tankMoveByLance = game.getOptions().booleanOption("vehicle_lance_movement") && (game.getPhase() == IGame.Phase.PHASE_INITIATIVE || game.getPhase() == IGame.Phase.PHASE_MOVEMENT);

        int evenMask = 0;
        if (infMoveEven)
            evenMask += GameTurn.CLASS_INFANTRY;
        if (protosMoveEven)
            evenMask += GameTurn.CLASS_PROTOMECH;
        // Reset all of the Players' turn category counts
        for (Enumeration<Player> loop = game.getPlayers(); loop.hasMoreElements();) {
            final Player player = loop.nextElement();
            player.resetEvenTurns();
            player.resetMultiTurns();
            player.resetOtherTurns();
            player.resetSpaceStationTurns();
            player.resetJumpshipTurns();
            player.resetWarshipTurns();
            player.resetDropshipTurns();
            player.resetSmallCraftTurns();

            // Add turns for protomechs weapons declaration.
            if (protosMoveByPoint) {

                // How many Protomechs does the player have?
                Enumeration<Entity> playerProtos = game.getSelectedEntities(new EntitySelector() {
                    private final int ownerId = player.getId();

                    public boolean accept(Entity entity) {
                        if (entity instanceof Protomech && ownerId == entity.getOwnerId() && entity.isSelectableThisTurn())
                            return true;
                        return false;
                    }
                });
                HashSet<Integer> points = new HashSet<Integer>();
                int numPlayerProtos = 0;
                for (; playerProtos.hasMoreElements();) {
                    Entity proto = playerProtos.nextElement();
                    numPlayerProtos++;
                    points.add(new Integer(proto.getUnitNumber()));
                }
                int numProtoUnits = (int) Math.ceil(numPlayerProtos / 5.0);
                if (!protosMoveEven)
                    numProtoUnits = points.size();
                for (int unit = 0; unit < numProtoUnits; unit++) {
                    if (protosMoveEven)
                        player.incrementEvenTurns();
                    else
                        player.incrementOtherTurns();
                }

            } // End handle-proto-firing-turns

        } // Handle the next player

        // Go through all entities, and update the turn categories of the
        // entity's player. The teams get their totals from their players.
        // N.B. protomechs declare weapons fire based on their point.
        for (Enumeration<Entity> loop = game.getEntities(); loop.hasMoreElements();) {
            final Entity entity = loop.nextElement();
            if (entity.isSelectableThisTurn()) {
                final Player player = entity.getOwner();
                if (entity instanceof Infantry
                        // only non-swarming infantry get a movement turn
                        &&!(game.getPhase() == IGame.Phase.PHASE_MOVEMENT && entity.getSwarmTargetId() != Entity.NONE)) {
                    if (infMoveEven)
                        player.incrementEvenTurns();
                    else if (infMoveMulti)
                        player.incrementMultiTurns();
                    else
                        player.incrementOtherTurns();
                } else if (entity instanceof Protomech) {
                    if (!protosMoveByPoint) {
                        if (protosMoveEven)
                            player.incrementEvenTurns();
                        else if (protosMoveMulti)
                            player.incrementMultiTurns();
                        else
                            player.incrementOtherTurns();
                    }
                } else if (entity instanceof Tank && tankMoveByLance) {
                    player.incrementMultiTurns();
                } else if (entity instanceof SpaceStation && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                    player.incrementSpaceStationTurns();
                } else if (entity instanceof Warship && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                    player.incrementWarshipTurns();
                } else if (entity instanceof Jumpship && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                    player.incrementJumpshipTurns();
                } else if (entity instanceof Dropship && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                    player.incrementDropshipTurns();
                } else if (entity instanceof SmallCraft && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                    player.incrementSmallCraftTurns();
                } else
                    player.incrementOtherTurns();
            }
        }

        // Generate the turn order for the Players *within*
        // each Team. Map the teams to their turn orders.
        // Count the number of teams moving this turn.
        int nTeams = game.getNoOfTeams();
        Hashtable<Team, TurnVectors> allTeamTurns = new Hashtable<Team, TurnVectors>(nTeams);
        Hashtable<Team, int[]> evenTrackers = new Hashtable<Team, int[]>(nTeams);
        int numTeamsMoving = 0;
        for (Enumeration<Team> loop = game.getTeams(); loop.hasMoreElements();) {
            final Team team = loop.nextElement();
            allTeamTurns.put(team, team.determineTeamOrder(game));

            // Track both the number of times we've checked the team for
            // "leftover" turns, and the number of "leftover" turns placed.
            int[] evenTracker = new int[2];
            evenTracker[0] = 0;
            evenTracker[1] = 0;
            evenTrackers.put(team, evenTracker);

            // Count this team if it has any "normal" moves.
            if (team.getNormalTurns(game) > 0)
                numTeamsMoving++;
        }

        // Now, generate the global order of all teams' turns.
        TurnVectors team_order = TurnOrdered.generateTurnOrder(game.getTeamsVector(), game);

        // See if there are any loaded units stranded on immobile transports.
        Enumeration<Entity> strandedUnits = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (Server.this.game.isEntityStranded(entity))
                    return true;
                return false;
            }
        });

        // Now, we collect everything into a single vector.
        Vector<GameTurn> turns;

        if (strandedUnits.hasMoreElements() && game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
            // Add a game turn to unload stranded units, if this
            // is the movement phase.
            turns = new Vector<GameTurn>(team_order.getTotalTurns() + team_order.getEvenTurns() + 1);
            turns.addElement(new GameTurn.UnloadStrandedTurn(strandedUnits));
        } else {
            // No stranded units.
            turns = new Vector<GameTurn>(team_order.getTotalTurns() + team_order.getEvenTurns());
        }

        // Walk through the global order, assigning turns
        // for individual players to the single vector.
        // Keep track of how many turns we've added to the vector.
        Team prevTeam = null;
        int min = team_order.getMin();
        for (int numTurn = 0; team_order.hasMoreElements(); numTurn++) {
            Team team = (Team) team_order.nextElement();
            TurnVectors withinTeamTurns = allTeamTurns.get(team);

            int[] evenTracker = evenTrackers.get(team);
            float teamEvenTurns = team.getEvenTurns();

            // Calculate the number of "even" turns to add for this team.
            int numEven = 0;
            if (1 == numTeamsMoving) {
                // The only team moving should move all "even" units.
                numEven += teamEvenTurns;
            } else if (prevTeam == null) {
                // Increment the number of times we've checked for "leftovers".
                evenTracker[0]++;

                // The first team to move just adds the "baseline" turns.
                numEven += teamEvenTurns / min;
            } else if (!team.equals(prevTeam)) {
                // Increment the number of times we've checked for "leftovers".
                evenTracker[0]++;

                // This wierd equation attempts to spread the "leftover"
                // turns accross the turn's moves in a "fair" manner.
                // It's based on the number of times we've checked for
                // "leftovers" the number of "leftovers" we started with,
                // the number of times we've added a turn for a "leftover",
                // and the total number of times we're going to check.
                numEven += Math.ceil(evenTracker[0] * (teamEvenTurns % min) / min - 0.5) - evenTracker[1];

                // Update the number of turns actually added for "leftovers".
                evenTracker[1] += numEven;

                // Add the "baseline" number of turns.
                numEven += teamEvenTurns / min;
            }

            // Record this team for the next move.
            prevTeam = team;

            if (withinTeamTurns.hasMoreSpaceStationElements()) {
                Player player = (Player) withinTeamTurns.nextSpaceStationElement();
                GameTurn turn = null;
                turn = new GameTurn.EntityClassTurn(player.getId(), GameTurn.CLASS_SPACE_STATION);
                turns.addElement(turn);
            } else if (withinTeamTurns.hasMoreJumpshipElements()) {
                Player player = (Player) withinTeamTurns.nextJumpshipElement();
                GameTurn turn = null;
                turn = new GameTurn.EntityClassTurn(player.getId(), GameTurn.CLASS_JUMPSHIP);
                turns.addElement(turn);
            } else if (withinTeamTurns.hasMoreWarshipElements()) {
                Player player = (Player) withinTeamTurns.nextWarshipElement();
                GameTurn turn = null;
                turn = new GameTurn.EntityClassTurn(player.getId(), GameTurn.CLASS_WARSHIP);
                turns.addElement(turn);
            } else if (withinTeamTurns.hasMoreDropshipElements()) {
                Player player = (Player) withinTeamTurns.nextDropshipElement();
                GameTurn turn = null;
                turn = new GameTurn.EntityClassTurn(player.getId(), GameTurn.CLASS_DROPSHIP);
                turns.addElement(turn);
            } else if (withinTeamTurns.hasMoreSmallCraftElements()) {
                Player player = (Player) withinTeamTurns.nextSmallCraftElement();
                GameTurn turn = null;
                turn = new GameTurn.EntityClassTurn(player.getId(), GameTurn.CLASS_SMALL_CRAFT);
                turns.addElement(turn);
            }
            // This may be a "placeholder" for a team without "normal" turns.
            else if (withinTeamTurns.hasMoreElements()) {

                // Not a placeholder... get the player who moves next.
                Player player = (Player) withinTeamTurns.nextElement();

                // If we've added all "normal" turns, allocate turns
                // for the infantry and/or protomechs moving even.
                GameTurn turn = null;
                if (numTurn >= team_order.getTotalTurns()) {
                    turn = new GameTurn.EntityClassTurn(player.getId(), evenMask);
                }

                // If either Infantry or Protomechs move even, only allow
                // the other classes to move during the "normal" turn.
                else if (infMoveEven || protosMoveEven) {
                    turn = new GameTurn.EntityClassTurn(player.getId(), ~evenMask);
                }

                // Otherwise, let *anybody* move.
                else {
                    turn = new GameTurn(player.getId());
                }
                turns.addElement(turn);

            } // End team-has-"normal"-turns

            // Add the calculated number of "even" turns.
            // Allow the player at least one "normal" turn before the
            // "even" turns to help with loading infantry in deployment.
            while (numEven > 0 && withinTeamTurns.hasMoreEvenElements()) {
                Player evenPlayer = (Player) withinTeamTurns.nextEvenElement();
                turns.addElement(new GameTurn.EntityClassTurn(evenPlayer.getId(), evenMask));
                numEven--;
            }
        }

        // set fields in game
        game.setTurnVector(turns);
        game.resetTurnIndex();

        // send turns to all players
        send(createTurnVectorPacket());

    }

    /**
     * Write the initiative results to the report
     */
    private void writeInitiativeReport(boolean abbreviatedReport) {
        // write to report
        Report r;
        boolean deployment = false;
        if (!abbreviatedReport) {
            r = new Report(1210);
            r.type = Report.PUBLIC;
            if (game.getLastPhase() == IGame.Phase.PHASE_DEPLOYMENT || game.isDeploymentComplete() || !game.shouldDeployThisRound()) {
                r.messageId = 1000;
                r.add(game.getRoundCount());
            } else {
                deployment = true;
                if (game.getRoundCount() == 0) {
                    r.messageId = 1005;
                } else {
                    r.messageId = 1010;
                    r.add(game.getRoundCount());
                }
            }
            addReport(r);

            // write seperator
            addReport(new Report(1200, Report.PUBLIC));
        } else {
            addReport(new Report(1210, Report.PUBLIC)); // newline
        }

        if (game.getOptions().booleanOption("individual_initiative")) {
            r = new Report(1040, Report.PUBLIC);
            addReport(r);
            for (Enumeration<GameTurn> e = game.getTurns(); e.hasMoreElements();) {
                GameTurn t = e.nextElement();
                if (t instanceof GameTurn.SpecificEntityTurn) {
                    Entity entity = game.getEntity(((GameTurn.SpecificEntityTurn) t).getEntityNum());
                    r = new Report(1045);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(entity.getInitiative().toString());
                    addReport(r);
                } else {
                    Player player = getPlayer(t.getPlayerNum());
                    if (null != player) {
                        r = new Report(1050, Report.PUBLIC);
                        r.add(player.getName());
                        addReport(r);
                    }
                }
            }
        } else {
            for (Enumeration<Team> i = game.getTeams(); i.hasMoreElements();) {
                final Team team = i.nextElement();

                // If there is only one player, list them as the 'team', and
                // use the team iniative
                if (team.getSize() == 1) {
                    final Player player = team.getPlayers().nextElement();
                    r = new Report(1015, Report.PUBLIC);
                    r.add(player.getName());
                    r.add(team.getInitiative().toString());
                    addReport(r);
                } else {
                    // Multiple players. List the team, then break it down.
                    r = new Report(1015, Report.PUBLIC);
                    r.add(Player.teamNames[team.getId()]);
                    r.add(team.getInitiative().toString());
                    addReport(r);
                    for (Enumeration<Player> j = team.getPlayers(); j.hasMoreElements();) {
                        final Player player = j.nextElement();
                        r = new Report(1015, Report.PUBLIC);
                        r.indent();
                        r.add(player.getName());
                        r.add(player.getInitiative().toString());
                        addReport(r);
                    }
                }
            }

            if (!doBlind()) {

                // The turn order is different in movement phase
                // if a player has any "even" moving units.
                r = new Report(1020, Report.PUBLIC);

                boolean hasEven = false;
                for (Enumeration<GameTurn> i = game.getTurns(); i.hasMoreElements();) {
                    GameTurn turn = i.nextElement();
                    Player player = getPlayer(turn.getPlayerNum());
                    if (null != player) {
                        r.add(player.getName());
                        if (player.getEvenTurns() > 0)
                            hasEven = true;
                    }
                }
                r.newlines = 2;
                addReport(r);
                if (hasEven) {
                    r = new Report(1021, Report.PUBLIC);
                    if ((game.getOptions().booleanOption("inf_deploy_even") || game.getOptions().booleanOption("protos_deploy_even")) && !(game.getLastPhase() == IGame.Phase.PHASE_END_REPORT))
                        r.choose(true);
                    else
                        r.choose(false);
                    r.indent();
                    r.newlines = 2;
                    addReport(r);
                }
            }

        }
        if (!abbreviatedReport) {
            // Wind direction and strength
            Report rWindDir = new Report(1025, Report.PUBLIC);
            rWindDir.add(game.getPlanetaryConditions().getWindDirName());
            rWindDir.newlines = 0;
            Report rWindStr = new Report(1030, Report.PUBLIC);
            rWindStr.add(game.getPlanetaryConditions().getWindCurrentName());
            rWindStr.newlines = 0;
            Report rWeather = new Report(1031, Report.PUBLIC);
            rWeather.add(game.getPlanetaryConditions().getWeatherCurrentName());
            rWeather.newlines = 0;
            Report rLight = new Report(1032, Report.PUBLIC);
            rLight.add(game.getPlanetaryConditions().getLightCurrentName());  
            addReport(rWindDir);
            addReport(rWindStr);
            addReport(rWeather);
            addReport(rLight);

            if (deployment)
                addNewLines();
        }
    }

    /**
     * Marks ineligible entities as not ready for this phase
     */
    private void setIneligible(IGame.Phase phase) {
        Vector<Entity> assistants = new Vector<Entity>();
        boolean assistable = false;
        
        if ( isPlayerForcedVictory() ){
            assistants.addAll(game.getEntitiesVector());
        }else {
            for (Entity entity : game.getEntitiesVector()) {
                if (entity.isEligibleFor(phase)) {
                    assistable = true;
                } else {
                    assistants.addElement(entity);
                }
            }
        }
        for (Entity assistant : assistants) {
            if (!assistable || !assistant.canAssist(phase)) {
                assistant.setDone(true);
            }
        }
    }

    /**
     * Have the loader load the indicated unit. The unit being loaded loses its
     * turn.
     * 
     * @param loader -
     *            the <code>Entity</code> that is loading the unit.
     * @param unit -
     *            the <code>Entity</code> being loaded.
     */
    private void loadUnit(Entity loader, Entity unit) {

        if (!unit.isDone()) {
            // Remove the *last* friendly turn (removing the *first* penalizes
            // the opponent too much, and re-calculating moves is too hard).
            game.removeTurnFor(unit);
            send(createTurnVectorPacket());
        }

        // Load the unit.
        loader.load(unit);

        // The loaded unit is being carried by the loader.
        unit.setTransportId(loader.getId());

        // Remove the loaded unit from the screen.
        unit.setPosition(null);

        // Update the loaded unit.
        entityUpdate(unit.getId());
    }

    /**
     * Have the unloader unload the indicated unit. The unit being unloaded does
     * *not* gain a turn.
     * 
     * @param unloader -
     *            the <code>Entity</code> that is unloading the unit.
     * @param unloaded -
     *            the <code>Targetable</code> unit being unloaded.
     * @param pos -
     *            the <code>Coords</code> for the unloaded unit.
     * @param facing -
     *            the <code>int</code> facing for the unloaded unit.
     * @param elevation -
     *            the <code>int</code> elevation at which to unload, if both
     *            loader and loaded units use VTOL movement.
     * @return <code>true</code> if the unit was successfully unloaded,
     *         <code>false</code> if the unit isn't carried in unloader.
     */
    private boolean unloadUnit(Entity unloader, Targetable unloaded, Coords pos, int facing, int elevation) {

        // We can only unload Entities.
        Entity unit = null;
        if (unloaded instanceof Entity) {
            unit = (Entity) unloaded;
        } else {
            return false;
        }

        // Unload the unit.
        if (!unloader.unload(unit)) {
            return false;
        }

        // The unloaded unit is no longer being carried.
        unit.setTransportId(Entity.NONE);

        // Place the unloaded unit onto the screen.
        unit.setPosition(pos);

        // Units unloaded onto the screen are deployed.
        if (pos != null) {
            unit.setDeployed(true);
        }

        // Point the unloaded unit in the given direction.
        unit.setFacing(facing);
        unit.setSecondaryFacing(facing);

        IHex hex = game.getBoard().getHex(pos);
        boolean isBridge = hex.containsTerrain(Terrains.PAVEMENT);

        if (unloader.getMovementMode() == IEntityMovementMode.VTOL) {
            if (unit.getMovementMode() == IEntityMovementMode.VTOL) {
                // Flying units onload to the same elevation as the flying
                // transport
                unit.setElevation(elevation);
            } else if (game.getBoard().getBuildingAt(pos) != null) {
                // non-flying unit onloaded from a flying onto a building
                // -> sit on the roff
                unit.setElevation(hex.terrainLevel(Terrains.BLDG_ELEV));
            } else {
                while (elevation >= -hex.depth()) {
                    if (unit.isElevationValid(elevation, hex)) {
                        unit.setElevation(elevation);
                        break;
                    }
                    elevation--;
                    unit.moved = IEntityMovementType.MOVE_JUMP;
                }
                if (!unit.isElevationValid(elevation, hex)) {
                    return false;
                }
            }
        } else if (game.getBoard().getBuildingAt(pos) != null) {
            // non flying unit unloading units into a building
            // -> sit in the building at the same elevation
            unit.setElevation(elevation);
        } else if (hex.terrainLevel(Terrains.WATER) > 0) {
            if (unit.getMovementMode() == IEntityMovementMode.HOVER || unit.getMovementMode() == IEntityMovementMode.WIGE || unit.getMovementMode() == IEntityMovementMode.HYDROFOIL || unit.getMovementMode() == IEntityMovementMode.NAVAL || unit.getMovementMode() == IEntityMovementMode.SUBMARINE || unit.getMovementMode() == IEntityMovementMode.INF_UMU || hex.containsTerrain(Terrains.ICE) || isBridge) {
                // units that can float stay on the surface, or we go on the
                // bridge
                // this means elevation 0, because elevation is relative to the
                // surface
                unit.setElevation(0);
            }
        } else {
            // default to the floor of the hex.
            // unit elevation is relative to the surface
            unit.setElevation(hex.floor() - hex.surface());
        }
        addReport(doSetLocationsExposure(unit, hex, false, unit.getElevation()));

        // Update the unloaded unit.
        entityUpdate(unit.getId());

        // Unloaded successfully.
        return true;
    }

    private boolean launchUnit(Entity unloader, Targetable unloaded, Coords pos, int facing, int velocity, int elevation, int[] moveVec, int bonus) {

        Entity unit = null;
        if (unloaded instanceof Entity) {
            unit = (Entity) unloaded;
        } else {
            return false;
        }

        // must be an ASF or Small Craft
        Aero a = new Aero();
        if (unit instanceof Aero && !(unit instanceof Dropship) && !(unit instanceof Jumpship)) {
            a = (Aero) unit;
        } else {
            return false;
        }

        Report r;

        // Unload the unit.
        if (!unloader.unload(unit)) {
            return false;
        }

        // The unloaded unit is no longer being carried.
        unit.setTransportId(Entity.NONE);

        // Place the unloaded unit onto the screen.
        unit.setPosition(pos);

        // Units unloaded onto the screen are deployed.
        if (pos != null) {
            unit.setDeployed(true);
        }

        // Point the unloaded unit in the given direction.
        unit.setFacing(facing);
        unit.setSecondaryFacing(facing);

        // the velocity of the unloadd unit is the same as the loader
        a.setCurrentVelocity(velocity);
        a.setNextVelocity(velocity);

        // if using advanced movement then set vectors
        a.setVectors(moveVec);

        unit.setElevation(elevation);

        // it seems that the done button is still being set and I can't figure
        // out where
        unit.setDone(false);

        // if the bonus was greater than zero then too many fighters were
        // launched and they
        // must all make control rolls
        if (bonus > 0) {
            PilotingRollData psr = unit.getBasePilotingRoll();
            psr.addModifier(bonus, "safe launch rate exceeded");
            int ctrlroll = Compute.d6(2);
            r = new Report(9375);
            r.subject = unit.getId();
            r.add(unit.getDisplayName());
            r.add(psr.getValue());
            r.add(ctrlroll);
            r.newlines = 0;
            r.indent(1);
            if (ctrlroll < psr.getValue()) {
                r.choose(false);
                addReport(r);
                // damage the unit
                int damage = 10 * (psr.getValue() - ctrlroll);
                HitData hit = a.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                addReport(damageEntity(unit, hit, damage));
                // did we destroy the unit?
                if (unit.isDoomed()) {
                    // Clean out the entity.
                    unit.setDestroyed(true);
                    game.moveToGraveyard(unit.getId());
                    send(createRemoveEntityPacket(unit.getId()));
                }
            } else {
                // avoided damage
                r.choose(true);
                addReport(r);
            }
        }

        // launching from an OOC vessel causes damage
        // same thing if faster than 2 velocity in atmosphere
        if ((((Aero) unloader).isOutControlTotal() && !unit.isDoomed()) || (((Aero) unloader).getCurrentVelocity() > 2 && game.getBoard().inAtmosphere())) {
            int damroll = Compute.d6(2);
            int damage = damroll * 10;
            r = new Report(9385);
            r.subject = unit.getId();
            r.add(unit.getDisplayName());
            r.add(damage);
            addReport(r);
            HitData hit = a.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
            addReport(damageEntity(unit, hit, damage));
            // did we destroy the unit?
            if (unit.isDoomed()) {
                // Clean out the entity.
                unit.setDestroyed(true);
                game.moveToGraveyard(unit.getId());
                send(createRemoveEntityPacket(unit.getId()));
            }
        }

        // Update the unloaded unit.
        entityUpdate(unit.getId());

        return true;

    }

    /**
     * Record that the given building has been affected by the current entity's
     * movement. At the end of the entity's movement, notify the clients about
     * the updates.
     * 
     * @param bldg -
     *            the <code>Building</code> that has been affected.
     * @param collapse -
     *            a <code>boolean</code> value that specifies that the
     *            building collapsed (when <code>true</code>).
     */
    private void addAffectedBldg(Building bldg, boolean collapse) {

        // If the building collapsed, then the clients have already
        // been notified, so remove it from the notification list.
        if (collapse) {
            System.err.print("Removing building from a list of " + affectedBldgs.size() + '\n');// killme
            affectedBldgs.remove(bldg);
            System.err.print("... now list of " + affectedBldgs.size() + '\n');// killme
        } else { // Otherwise, make sure that this building is tracked.
            affectedBldgs.put(bldg, Boolean.FALSE);
        }
    }

    /**
     * Walk through the building hexes that were affected by the recent entity's
     * movement. Notify the clients about the updates to all affected entities
     * and uncollapsed buildings. The affected hexes is then cleared for the
     * next entity's movement.
     */
    private void applyAffectedBldgs() {

        // Build a list of Building updates.
        Vector<Building> bldgUpdates = new Vector<Building>();

        // Only send a single turn update.
        boolean bTurnsChanged = false;

        // Walk the set of buildings.
        Enumeration<Building> bldgs = affectedBldgs.keys();
        while (bldgs.hasMoreElements()) {
            final Building bldg = bldgs.nextElement();

            // Walk through the building's coordinates.
            Enumeration<Coords> bldgCoords = bldg.getCoords();
            while (bldgCoords.hasMoreElements()) {
                final Coords coords = bldgCoords.nextElement();

                // Walk through the entities at these coordinates.
                Enumeration<Entity> entities = game.getEntities(coords);
                while (entities.hasMoreElements()) {
                    final Entity entity = entities.nextElement();

                    // Is the entity infantry?
                    if (entity instanceof Infantry) {

                        // Is the infantry dead?
                        if (entity.isDoomed() || entity.isDestroyed()) {

                            // Has the entity taken a turn?
                            if (!entity.isDone()) {

                                // Dead entities don't take turns.
                                game.removeTurnFor(entity);
                                bTurnsChanged = true;

                            } // End entity-still-to-move

                            // Clean out the dead entity.
                            entity.setDestroyed(true);
                            game.moveToGraveyard(entity.getId());
                            send(createRemoveEntityPacket(entity.getId()));
                        }

                        // Infantry that aren't dead are damaged.
                        else {
                            entityUpdate(entity.getId());
                        }

                    } // End entity-is-infantry

                } // Check the next entity.

            } // Handle the next hex in this building.

            // Add this building to the report.
            bldgUpdates.addElement(bldg);

        } // Handle the next affected building.

        // Did we update the turns?
        if (bTurnsChanged) {
            send(createTurnVectorPacket());
        }

        // Are there any building updates?
        if (!bldgUpdates.isEmpty()) {

            // Send the building updates to the clients.
            sendChangedCFBuildings(bldgUpdates);

            // Clear the list of affected buildings.
            affectedBldgs.clear();
        }

        // And we're done.

    } // End private void applyAffectedBldgs()

    /**
     * Receives an entity movement packet, and if valid, executes it and ends
     * the current turn.
     */
    private void receiveMovement(Packet packet, int connId) {
        Entity entity = game.getEntity(packet.getIntValue(0));
        MovePath md = (MovePath) packet.getObject(1);

        // is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_MOVEMENT) {
            System.err.println("error: server got movement packet in wrong phase");
            return;
        }

        // can this player/entity act right now?
        if (!game.getTurn().isValid(connId, entity, game)) {
            System.err.println("error: server got invalid movement packet");
            return;
        }

        // looks like mostly everything's okay
        processMovement(entity, md);

        // check the LOS of any telemissiles owned by this entity
        for (int missileId : entity.getTMTracker().getMissiles()) {
            Entity tm = game.getEntity(missileId);
            if (null != tm && !tm.isDestroyed() && tm instanceof TeleMissile) {
                if (LosEffects.calculateLos(game, entity.getId(), tm).canSee()) {
                    ((TeleMissile) tm).setOutContact(false);
                } else {
                    ((TeleMissile) tm).setOutContact(true);
                }
                entityUpdate(tm.getId());
            }
        }

        // Notify the clients about any building updates.
        applyAffectedBldgs();

        // Update visibility indications if using double blind.
        if (doBlind()) {
            updateVisibilityIndicator();
        }

        // This entity's turn is over.
        // N.B. if the entity fell, a *new* turn has already been added.
        endCurrentTurn(entity);
    }

    /**
     * makes a unit skid or sideslip on the board
     * 
     * @param entity
     *            the unit which should skid
     * @param start
     *            the coordinates of the hex the unit was in prior to skidding
     * @param elevation
     *            the elevation of the unit
     * @param direction
     *            the direction of the skid
     * @param distance
     *            the number of hexes skidded
     * @param step
     *            the MoveStep which caused the skid
     * @return true if the entity was removed from play
     */
    private boolean processSkid(Entity entity, Coords start, int elevation, int direction, int distance, MoveStep step) {
        Coords nextPos = start;
        Coords curPos = nextPos;
        IHex curHex = game.getBoard().getHex(start);
        Report r;
        int skidDistance = 0; // actual distance moved
        ArrayList<Entity> avoidedChargeUnits = new ArrayList<Entity>();
        while (!entity.isDoomed() && distance > 0) {
            nextPos = curPos.translated(direction);
            // Is the next hex off the board?
            if (!game.getBoard().contains(nextPos)) {

                // Can the entity skid off the map?
                if (game.getOptions().booleanOption("push_off_board")) {
                    // Yup. One dead entity.
                    game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                    send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                    r = new Report(2030, Report.PUBLIC);
                    r.addDesc(entity);
                    addReport(r);

                    for (Entity e : entity.getLoadedUnits()) {
                        game.removeEntity(e.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                        send(createRemoveEntityPacket(e.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                    }
                    Entity swarmer = game.getEntity(entity.getSwarmAttackerId());
                    if (swarmer != null) {
                        if (!swarmer.isDone()) {
                            swarmer.setDone(true);
                            game.removeTurnFor(swarmer);
                            send(createTurnVectorPacket());
                        }
                        game.removeEntity(swarmer.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                        send(createRemoveEntityPacket(swarmer.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                    }
                    // The entity's movement is completed.
                    return true;

                }
                // Nope. Update the report.
                r = new Report(2035);
                r.subject = entity.getId();
                r.indent();
                addReport(r);
                // Stay in the current hex and stop skidding.
                break;
            }

            IHex nextHex = game.getBoard().getHex(nextPos);
            distance -= nextHex.movementCost(entity.getMovementMode()) + 1;
            // By default, the unit is going to fall to the floor of the next
            // hex
            int curAltitude = entity.getElevation() + curHex.getElevation();
            int nextAltitude = nextHex.floor();

            // but VTOL keep altitude
            if (entity.getMovementMode() == IEntityMovementMode.VTOL) {
                nextAltitude = Math.max(nextAltitude, curAltitude);
            } else {
                // Is there a building to "catch" the unit?
                if (nextHex.containsTerrain(Terrains.BLDG_ELEV)) {
                    // unit will land on the roof, if at a higher level,
                    // otherwise it will skid through the wall onto the same
                    // floor.
                    nextAltitude = Math.min(curAltitude, nextHex.getElevation() + nextHex.terrainLevel(Terrains.BLDG_ELEV));
                }
                // Is there a bridge to "catch" the unit?
                if (nextHex.containsTerrain(Terrains.BRIDGE)) {
                    // unit will land on the bridge, if at a higher level,
                    // and the bridge exits towards the current hex,
                    // otherwise the bridge has no effect
                    int exitDir = (direction + 3) % 6;
                    exitDir = 1 << exitDir;
                    if ((nextHex.getTerrain(Terrains.BRIDGE).getExits() & exitDir) == exitDir) {
                        nextAltitude = Math.min(curAltitude, Math.max(nextAltitude, nextHex.getElevation() + nextHex.terrainLevel(Terrains.BRIDGE_ELEV)));
                    }
                }
                if (nextAltitude <= nextHex.surface() && curAltitude >= curHex.surface()) {
                    // Hovercraft can "skid" over water.
                    // all units can skid over ice.
                    if (entity instanceof Tank && (entity.getMovementMode() == IEntityMovementMode.HOVER || entity.getMovementMode() == IEntityMovementMode.WIGE)) {
                        if (nextHex.containsTerrain(Terrains.WATER)) {
                            nextAltitude = nextHex.surface();
                        }
                    } else {
                        if (nextHex.containsTerrain(Terrains.ICE)) {
                            nextAltitude = nextHex.surface();
                        }
                    }
                }
            }

            // The elevation the skidding unit will occupy in next hex
            int nextElevation = nextAltitude - nextHex.surface();

            boolean crashedIntoTerrain = curAltitude < nextAltitude;
            if (entity.getMovementMode() == IEntityMovementMode.VTOL) {
                if (nextElevation == 0 || (nextElevation == 1 && (nextHex.containsTerrain(Terrains.WOODS) || nextHex.containsTerrain(Terrains.JUNGLE)))) {
                    crashedIntoTerrain = true;
                }
            }

            if (nextHex.containsTerrain(Terrains.BLDG_ELEV)) {
                Building bldg = game.getBoard().getBuildingAt(nextPos);

                if (bldg.getType() == Building.WALL)
                    crashedIntoTerrain = true;
            }

            // however WIGE can gain 1 level to avoid crashing into the terrain
            if (entity.getMovementMode() == IEntityMovementMode.WIGE) {
                if (nextElevation == 0 && !(nextHex.containsTerrain(Terrains.WOODS) || nextHex.containsTerrain(Terrains.JUNGLE))) {
                    nextElevation = 1;
                    crashedIntoTerrain = false;
                } else if (nextElevation == 1 && (nextHex.containsTerrain(Terrains.WOODS) || nextHex.containsTerrain(Terrains.JUNGLE))) {
                    nextElevation = 2;
                    crashedIntoTerrain = false;
                }
            }

            if (crashedIntoTerrain) {

                if (nextHex.containsTerrain(Terrains.BLDG_ELEV)) {
                    Building bldg = game.getBoard().getBuildingAt(nextPos);

                    // If you crash into a wall you want to stop in the hex
                    // before the wall not in the wall
                    // Like a building.
                    if (bldg.getType() == Building.WALL) {
                        r = new Report(2047);
                    } else
                        r = new Report(2045);

                } else
                    r = new Report(2045);

                r.subject = entity.getId();
                r.indent();
                r.add(nextPos.getBoardNum(), true);
                addReport(r);

                if (entity.getMovementMode() == IEntityMovementMode.WIGE || entity.getMovementMode() == IEntityMovementMode.VTOL) {
                    int hitSide = step.getFacing() - direction + 6;
                    hitSide %= 6;
                    int table = 0;
                    switch (hitSide) {// quite hackish...I think it ought to
                    // work, though.
                    case 0:// can this happen?
                        table = ToHitData.SIDE_FRONT;
                        break;
                    case 1:
                    case 2:
                        table = ToHitData.SIDE_LEFT;
                        break;
                    case 3:
                        table = ToHitData.SIDE_REAR;
                        break;
                    case 4:
                    case 5:
                        table = ToHitData.SIDE_RIGHT;
                        break;
                    }
                    elevation = nextElevation;
                    addReport(crashVTOLorWiGE((VTOL) entity, true, distance, curPos, elevation, table));

                    if (nextHex.containsTerrain(Terrains.WATER) && !nextHex.containsTerrain(Terrains.ICE) || nextHex.containsTerrain(Terrains.WOODS) || nextHex.containsTerrain(Terrains.JUNGLE)) {
                        addReport(destroyEntity(entity, "could not land in crash site"));
                    } else if (elevation < nextHex.terrainLevel(Terrains.BLDG_ELEV)) {
                        Building bldg = game.getBoard().getBuildingAt(nextPos);

                        // If you crash into a wall you want to stop in the hex
                        // before the wall not in the wall
                        // Like a building.
                        if (bldg.getType() == Building.WALL) {
                            addReport(destroyEntity(entity, "crashed into a wall"));
                            break;
                        }

                        addReport(destroyEntity(entity, "crashed into building"));
                    } else {
                        entity.setPosition(nextPos);
                        entity.setElevation(0);
                        addReport(doEntityDisplacementMinefieldCheck(entity, curPos, nextPos, nextElevation));
                    }
                    curPos = nextPos;
                    break;

                }
                // skidding into higher terrain does weight/20
                // damage in 5pt clusters to front.
                int damage = ((int) entity.getWeight() + 19) / 20;
                while (damage > 0) {
                    addReport(damageEntity(entity, entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Math.min(5, damage)));
                    damage -= 5;
                }
                // Stay in the current hex and stop skidding.
                break;
            }

            // Have skidding units suffer falls (off a cliff).
            else if (curAltitude > nextAltitude + entity.getMaxElevationChange()) {
                // WIGE can avoid this too, if they have 2MP to spend
                if (entity.getMovementMode() == IEntityMovementMode.WIGE && entity.getRunMP() - 2 >= entity.mpUsed) {
                    entity.mpUsed += 2;
                    nextAltitude = curAltitude;
                } else {
                    addReport(doEntityFallsInto(entity, curPos, nextPos, entity.getBasePilotingRoll(step.getMovementType())));
                    addReport(doEntityDisplacementMinefieldCheck(entity, curPos, nextPos, nextElevation));
                    // Stay in the current hex and stop skidding.
                    break;
                }
            }

            // Get any building in the hex.
            Building bldg = null;
            if (nextElevation < nextHex.terrainLevel(Terrains.BLDG_ELEV)) {
                // We will only run into the building if its at a higher level,
                // otherwise we skid over the roof
                bldg = game.getBoard().getBuildingAt(nextPos);
            }
            boolean bldgSuffered = false;
            boolean stopTheSkid = false;
            // Does the next hex contain an entities?
            // ASSUMPTION: hurt EVERYONE in the hex.
            Enumeration<Entity> targets = game.getEntities(nextPos);
            if (targets.hasMoreElements()) {
                boolean skidChargeHit = false;
                while (targets.hasMoreElements()) {
                    Entity target = targets.nextElement();

                    if (target.getElevation() > nextElevation + entity.getHeight() || target.absHeight() < nextElevation) {
                        // target is not in the way
                        continue;
                    }

                    // Can the target avoid the skid?
                    if (!target.isDone()) {
                        if (target instanceof Infantry) {
                            r = new Report(2420);
                            r.subject = target.getId();
                            r.addDesc(target);
                            addReport(r);
                            continue;
                        } else if (target instanceof Protomech) {
                            if (target != Compute.stackingViolation(game, entity, nextPos, null)) {
                                r = new Report(2420);
                                r.subject = target.getId();
                                r.addDesc(target);
                                addReport(r);
                                continue;
                            }
                        } else {
                            PilotingRollData psr = target.getBasePilotingRoll();
                            psr.addModifier(0, "avoiding collision");
                            int roll = Compute.d6(2);
                            r = new Report(2425);
                            r.subject = target.getId();
                            r.addDesc(target);
                            r.add(psr.getValue());
                            r.add(psr.getDesc());
                            r.add(roll);
                            addReport(r);
                            if (roll >= psr.getValue()) {
                                game.removeTurnFor(target);
                                avoidedChargeUnits.add(target);
                                continue;
                                // TODO: the charge should really be suspended
                                // and resumed after the target moved.
                            }
                        }
                    }

                    // Mechs and vehicles get charged,
                    // but need to make a to-hit roll
                    if (target instanceof Mech || target instanceof Tank) {
                        ChargeAttackAction caa = new ChargeAttackAction(entity.getId(), target.getTargetType(), target.getTargetId(), target.getPosition());
                        ToHitData toHit = caa.toHit(game, true);

                        // roll
                        int roll = Compute.d6(2);
                        // Update report.
                        r = new Report(2050);
                        r.subject = entity.getId();
                        r.indent();
                        r.add(target.getShortName(), true);
                        r.add(nextPos.getBoardNum(), true);
                        r.newlines = 0;
                        addReport(r);
                        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
                            roll = -12;
                            r = new Report(2055);
                            r.subject = entity.getId();
                            r.add(toHit.getDesc());
                            r.newlines = 0;
                            addReport(r);
                        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
                            r = new Report(2060);
                            r.subject = entity.getId();
                            r.add(toHit.getDesc());
                            r.newlines = 0;
                            addReport(r);
                            roll = Integer.MAX_VALUE;
                        } else {
                            // report the roll
                            r = new Report(2065);
                            r.subject = entity.getId();
                            r.add(toHit.getValue());
                            r.add(roll);
                            r.newlines = 0;
                            addReport(r);
                        }

                        // Resolve a charge against the target.
                        // ASSUMPTION: buildings block damage for
                        // *EACH* entity charged.
                        if (roll < toHit.getValue()) {
                            r = new Report(2070);
                            r.subject = entity.getId();
                            addReport(r);
                        } else {
                            // Resolve the charge.
                            resolveChargeDamage(entity, target, toHit, direction);
                            // HACK: set the entity's location
                            // to the original hex again, for the other targets
                            if (targets.hasMoreElements()) {
                                entity.setPosition(curPos);
                            }
                            bldgSuffered = true;
                            skidChargeHit = true;
                            // The skid ends here if the target lives.
                            if (!target.isDoomed() && !target.isDestroyed() && !game.isOutOfGame(target)) {
                                stopTheSkid = true;
                            }
                        }

                        // if we don't do this here,
                        // we can have a mech without a leg
                        // standing on the field and moving
                        // as if it still had his leg after
                        // getting skid-charged.
                        if (!target.isDone()) {
                            addReport(resolvePilotingRolls(target));
                            game.resetPSRs(target);
                            target.applyDamage();
                            addNewLines();
                        }

                    }

                    // Resolve "move-through" damage on infantry.
                    // Infantry inside of a building don't get a
                    // move-through, but suffer "bleed through"
                    // from the building.
                    else if (target instanceof Infantry && bldg != null) {
                        // Update report.
                        r = new Report(2075);
                        r.subject = entity.getId();
                        r.indent();
                        r.add(target.getShortName(), true);
                        r.add(nextPos.getBoardNum(), true);
                        r.newlines = 0;
                        addReport(r);

                        // Infantry don't have different
                        // tables for punches and kicks
                        HitData hit = target.rollHitLocation(ToHitData.HIT_NORMAL, Compute.targetSideTable(entity, target));
                        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                        // Damage equals tonnage, divided by 5.
                        // ASSUMPTION: damage is applied in one hit.
                        addReport(damageEntity(target, hit, Math.round(entity.getWeight() / 5)));
                        addNewLines();
                    }

                    // Has the target been destroyed?
                    if (target.isDoomed()) {

                        // Has the target taken a turn?
                        if (!target.isDone()) {

                            // Dead entities don't take turns.
                            game.removeTurnFor(target);
                            send(createTurnVectorPacket());

                        } // End target-still-to-move

                        // Clean out the entity.
                        target.setDestroyed(true);
                        game.moveToGraveyard(target.getId());
                        send(createRemoveEntityPacket(target.getId()));
                    }

                    // Update the target's position,
                    // unless it is off the game map.
                    if (!game.isOutOfGame(target)) {
                        entityUpdate(target.getId());
                    }

                } // Check the next entity in the hex.

                if (skidChargeHit) {
                    // HACK: set the entities position to that
                    // hex's coords, because we had to move the entity
                    // back earlier for the other targets
                    entity.setPosition(nextPos);
                }

                for (Entity e : avoidedChargeUnits) {
                    GameTurn newTurn = new GameTurn.SpecificEntityTurn(e.getOwner().getId(), e.getId());
                    game.insertNextTurn(newTurn);
                    send(createTurnVectorPacket());
                }
            }

            // Handle the building in the hex.
            if (bldg != null) {

                // Report that the entity has entered the bldg.
                r = new Report(2080);
                r.subject = entity.getId();
                r.indent();
                r.add(bldg.getName());
                r.add(nextPos.getBoardNum(), true);
                addReport(r);

                // If the building hasn't already suffered
                // damage, then apply charge damage to the
                // building and displace the entity inside.
                // ASSUMPTION: you don't charge the building
                // if Tanks or Mechs were charged.
                int chargeDamage = ChargeAttackAction.getDamageFor(entity, game.getOptions().booleanOption("tacops_charge_damage"), entity.delta_distance);
                if (!bldgSuffered) {
                    Vector<Report> reports = damageBuilding(bldg, chargeDamage, nextPos);
                    for (Report report : reports) {
                        report.subject = entity.getId();
                    }
                    addReport(reports);

                    // Apply damage to the attacker.
                    int toAttacker = ChargeAttackAction.getDamageTakenBy(entity, bldg, nextPos);
                    HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(nextPos));
                    hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                    addReport(damageEntity(entity, hit, toAttacker));
                    addNewLines();

                    entity.setPosition(nextPos);
                    entity.setElevation(nextElevation);
                    addReport(doEntityDisplacementMinefieldCheck(entity, curPos, nextPos, nextElevation));
                    curPos = nextPos;
                } // End buildings-suffer-too

                // Any infantry in the building take damage
                // equal to the building being charged.
                // ASSUMPTION: infantry take no damage from the
                // building absorbing damage from
                // Tanks and Mechs being charged.
                damageInfantryIn(bldg, chargeDamage, nextPos);

                // If a building still stands, then end the skid,
                // and add it to the list of affected buildings.
                if (bldg.getCurrentCF(nextPos) > 0) {
                    stopTheSkid = true;
                    addAffectedBldg(bldg, false);
                } else {
                    // otherwise it collapses immediately on our head
                    checkForCollapse(bldg, game.getPositionMap(), nextPos);
                }

            } // End handle-building.

            // Do we stay in the current hex and stop skidding?
            if (stopTheSkid) {
                break;
            }

            // Update entity position and elevation
            entity.setPosition(nextPos);
            entity.setElevation(nextElevation);
            addReport(doEntityDisplacementMinefieldCheck(entity, curPos, nextPos, nextElevation));
            skidDistance++;

            // Check for collapse of any building the entity might be on
            Building roof = game.getBoard().getBuildingAt(nextPos);
            if (roof != null) {
                if (checkForCollapse(roof, game.getPositionMap(), nextPos))
                    break; // stop skidding if the building collapsed
            }

            // Can the skiding entity enter the next hex from this?
            // N.B. can skid along roads.
            if ((entity.isHexProhibited(curHex) || entity.isHexProhibited(nextHex)) && !Compute.canMoveOnPavement(game, curPos, nextPos, step.getParentUpToThisStep())) {
                // Update report.
                r = new Report(2040);
                r.subject = entity.getId();
                r.indent();
                r.add(nextPos.getBoardNum(), true);
                addReport(r);

                // If the prohibited terrain is water, entity is destroyed
                if (nextHex.terrainLevel(Terrains.WATER) > 0 && entity instanceof Tank && entity.getMovementMode() != IEntityMovementMode.HOVER && entity.getMovementMode() != IEntityMovementMode.WIGE) {
                    addReport(destroyEntity(entity, "skidded into a watery grave", false, true));
                }

                // otherwise, damage is weight/5 in 5pt clusters
                int damage = ((int) entity.getWeight() + 4) / 5;
                while (damage > 0) {
                    addReport(damageEntity(entity, entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Math.min(5, damage)));
                    damage -= 5;
                }
                // and unit is immobile
                if (entity instanceof Tank) {
                    ((Tank) entity).immobilize();
                }

                // Stay in the current hex and stop skidding.
                break;
            }

            if (nextHex.terrainLevel(Terrains.WATER) > 0 && entity.getMovementMode() != IEntityMovementMode.HOVER && entity.getMovementMode() != IEntityMovementMode.WIGE) {
                // water ends the skid
                break;
            }

            // check for breaking magma crust
            if (nextHex.terrainLevel(Terrains.MAGMA) == 1 && nextElevation == 0) {
                int roll = Compute.d6(1);
                r = new Report(2395);
                r.addDesc(entity);
                r.add(roll);
                r.subject = entity.getId();
                addReport(r);
                if (roll == 6) {
                    nextHex.removeTerrain(Terrains.MAGMA);
                    nextHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.MAGMA, 2));
                    sendChangedHex(curPos);
                    for (Enumeration<Entity> e = game.getEntities(curPos); e.hasMoreElements();) {
                        Entity en = e.nextElement();
                        if (en != entity)
                            doMagmaDamage(en, false);
                    }
                }
            }

            // check for entering liquid magma
            if (nextHex.terrainLevel(Terrains.MAGMA) == 2 && nextElevation == 0) {
                doMagmaDamage(entity, false);
            }

            // is the next hex a swamp?
            PilotingRollData rollTarget = entity.checkBogDown(step, nextHex, curPos, nextPos, step.getElevation(), Compute.canMoveOnPavement(game, curPos, nextPos, step.getParentUpToThisStep()));
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                //Taharqa: According to TacOps, you automatically stick if you are skidding, (pg. 63)
                //if (0 < doSkillCheckWhileMoving(entity, curPos, nextPos, rollTarget, false)) {
                    entity.setStuck(true);
                    r = new Report(2081);
                    r.subject = entity.getId();
                    r.add(entity.getDisplayName(), true);
                    addReport(r);
                    //check for quicksand
                    addReport (checkQuickSand(nextPos));
                    // check for accidental stacking violation
                    Entity violation = Compute.stackingViolation(game, entity.getId(), curPos);
                    if (violation != null) {
                        // target gets displaced, because of low elevation
                        Coords targetDest = Compute.getValidDisplacement(game, entity.getId(), curPos, direction);
                        addReport(doEntityDisplacement(violation, curPos, targetDest, new PilotingRollData(violation.getId(), 0, "domino effect")));
                        // Update the violating entity's postion on the client.
                        entityUpdate(violation.getId());
                    }
                    // stay here and stop skidding, see bug 1115608
                    break;
               // }
            }

            // Update the position and keep skidding.
            curPos = nextPos;
            curHex = nextHex;
            r = new Report(2085);
            r.subject = entity.getId();
            r.indent();
            r.add(curPos.getBoardNum(), true);
            addReport(r);

        } // Handle the next skid hex.

        // If the skidding entity violates stacking,
        // displace targets until it doesn't.
        curPos = entity.getPosition();
        Entity target = Compute.stackingViolation(game, entity.getId(), curPos);
        while (target != null) {
            nextPos = Compute.getValidDisplacement(game, target.getId(), target.getPosition(), direction);
            // ASSUMPTION
            // There should always be *somewhere* that
            // the target can go... last skid hex if
            // nothing else is available.
            if (null == nextPos) {
                // But I don't trust the assumption fully.
                // Report the error and try to continue.
                System.err.println("The skid of " + entity.getShortName() + " should displace " + target.getShortName() + " in hex " + curPos.getBoardNum() + " but there is nowhere to go.");
                break;
            }
            // indent displacement
            r = new Report(1210, Report.PUBLIC);
            r.indent();
            r.newlines = 0;
            addReport(r);
            addReport(doEntityDisplacement(target, curPos, nextPos, null));
            addReport(doEntityDisplacementMinefieldCheck(entity, curPos, nextPos, entity.getElevation()));
            target = Compute.stackingViolation(game, entity.getId(), curPos);
        }

        // Mechs suffer damage for every hex skidded.
        if (entity instanceof Mech) {
            // Calculate one half falling damage times skid length.
            int damage = skidDistance * (int) Math.ceil(Math.round(entity.getWeight() / 10.0) / 2.0);

            // report skid damage
            r = new Report(2090);
            r.subject = entity.getId();
            r.indent();
            r.addDesc(entity);
            r.add(damage);
            addReport(r);

            // standard damage loop
            // All skid damage is to the front.
            while (damage > 0) {
                int cluster = Math.min(5, damage);
                HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                addReport(damageEntity(entity, hit, cluster));
                damage -= cluster;
            }
            addNewLines();
        }

        // Clean up the entity if it has been destroyed.
        if (entity.isDoomed()) {
            entity.setDestroyed(true);
            game.moveToGraveyard(entity.getId());
            send(createRemoveEntityPacket(entity.getId()));

            // The entity's movement is completed.
            return true;
        }

        // Let the player know the ordeal is over.
        r = new Report(2095);
        r.subject = entity.getId();
        r.indent();
        addReport(r);

        return false;
    }

    /**
     * processes a potential collision
     * 
     * @param entity
     * @param target
     * @param src
     * @return
     */
    private boolean processCollision(Entity entity, Entity target, Coords src) {
        Report r;

        r = new Report(9035);
        r.subject = entity.getId();
        r.add(entity.getDisplayName());
        r.add(target.getDisplayName());
        addReport(r);
        int partialroll = Compute.d6(1);
        boolean partial = (partialroll == 6);
        // has the target moved yet this round?
        Aero ta = (Aero) target;
        if (target.mpUsed < target.getRunMPwithoutMASC() && !ta.isOutControlTotal() && !target.isImmobile()) {
            // give them a control roll to avoid the collision
            // TODO: I should make this voluntary really
            PilotingRollData psr = target.getBasePilotingRoll();
            psr.addModifier(0, "avoiding collision");
            int ctrlroll = Compute.d6(2);
            r = new Report(9045);
            r.subject = target.getId();
            r.add(target.getDisplayName());
            r.add(ctrlroll);
            r.newlines = 0;
            r.indent(2);
            if (ctrlroll < psr.getValue()) {
                r.choose(false);
                addReport(r);
            } else {
                // avoided collision
                r.choose(true);
                addReport(r);
                // two possibilities:
                // 1) the target already moved, but had MP left - check for
                // control roll conditions
                // 2) the target had not yet moved, move them in straight line
                if (!target.isDone()) {
                    int vel = ta.getCurrentVelocity();
                    MovePath md = new MovePath(game, target);
                    while (vel > 0) {
                        md.addStep(MovePath.STEP_FORWARDS);
                        vel--;
                    }
                    game.removeTurnFor(target);
                    send(createTurnVectorPacket());
                    processMovement(target, md);
                    // for some reason it is not clearing out turn
                } else {
                    // what needs to get checked?
                    // this move puts them at over-thrust
                    target.moved = IEntityMovementType.MOVE_OVER_THRUST;
                    // they may have exceeded SI, only add if they hadn't
                    // exceeded it before
                    if (target.mpUsed <= ta.getSI()) {
                        PilotingRollData rollTarget = ta.checkThrustSITotal(target.getRunMPwithoutMASC(), ta.moved);
                        if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                            game.addControlRoll(new PilotingRollData(ta.getId(), 0, "Thrust spent during turn exceeds SI"));
                        }
                    }
                    target.mpUsed = target.getRunMPwithoutMASC();
                }
                return false;
            }
        } else {
            // can't avoid collision - write report
            r = new Report(9040);
            r.subject = entity.getId();
            r.add(entity.getDisplayName());
            r.indent(2);
            addReport(r);
        }

        // if we are still here, then collide
        ToHitData toHit = new ToHitData(TargetRoll.AUTOMATIC_SUCCESS, "Its a collision");
        toHit.setSideTable(target.sideTable(src));
        resolveRamDamage((Aero) entity, (Aero) target, toHit, partial, false);

        // Has the target been destroyed?
        if (target.isDoomed()) {
            // Has the target taken a turn?
            if (!target.isDone()) {
                // Dead entities don't take turns.
                game.removeTurnFor(target);
                send(createTurnVectorPacket());
            } // End target-still-to-move
            // Clean out the entity.
            target.setDestroyed(true);
            game.moveToGraveyard(target.getId());
            send(createRemoveEntityPacket(target.getId()));
        }
        // Update the target's position,
        // unless it is off the game map.
        if (!game.isOutOfGame(target)) {
            entityUpdate(target.getId());
        }

        return true;

    }

    private Vector<Report> processCrash(Entity entity, int vel) {
        Vector<Report> vReport = new Vector<Report>();
        Report r;

        if (vel < 1) {
            vel = 1;
        }
        int crash_damage = Compute.d6(2) * 10 * vel;
        r = new Report(9392, Report.PUBLIC);
        r.indent();
        r.addDesc(entity);
        r.add(crash_damage);
        r.newlines = 0;
        vReport.add(r);
        while (crash_damage > 0) {
            HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
            if (entity.getMovementMode() == IEntityMovementMode.SPHEROID) {
                hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_REAR);
            }

            if (crash_damage > 10) {
                vReport.addAll(damageEntity(entity, hit, 10));
            } else {
                vReport.addAll(damageEntity(entity, hit, crash_damage));
            }
            crash_damage -= 10;
        }

        // if the entity survived they are useless anyway because we have no
        // ground map yet so remove them
        if (!entity.isDoomed() && !entity.isDestroyed()) {
            r = new Report(9393, Report.PUBLIC);
            r.indent();
            r.addDesc(entity);
            vReport.add(r);
            entity.setDoomed(true);
        }
        return vReport;
    }

    /**
     * Steps through an entity movement packet, executing it.
     */
    private void processMovement(Entity entity, MovePath md) {
        Report r;
        boolean sideslipped = false; // for VTOL sideslipping

        // check for fleeing
        if (md.contains(MovePath.STEP_FLEE)) {
            // Unit has fled the battlefield.
            r = new Report(2005, Report.PUBLIC);
            r.addDesc(entity);
            addReport(r);
            Coords pos = entity.getPosition();
            int fleeDirection;
            if (pos.x == 0) {
                fleeDirection = IOffBoardDirections.WEST;
            } else if (pos.y == 0) {
                fleeDirection = IOffBoardDirections.SOUTH;
            } else if (pos.x == game.getBoard().getWidth()) {
                fleeDirection = IOffBoardDirections.EAST;
            } else {
                fleeDirection = IOffBoardDirections.NORTH;
            }

            // Is the unit carrying passengers?
            final Vector<Entity> passengers = entity.getLoadedUnits();
            if (!passengers.isEmpty()) {
                for (Entity passenger : passengers) {
                    // Unit has fled the battlefield.
                    r = new Report(2010, Report.PUBLIC);
                    r.indent();
                    r.addDesc(passenger);
                    addReport(r);
                    passenger.setRetreatedDirection(fleeDirection);
                    game.removeEntity(passenger.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT);
                    send(createRemoveEntityPacket(passenger.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT));
                }
            }

            // Handle any picked up MechWarriors
            for (Integer mechWarriorId : entity.getPickedUpMechWarriors()) {
                Entity mw = game.getEntity(mechWarriorId.intValue());

                // Is the MechWarrior an enemy?
                int condition = IEntityRemovalConditions.REMOVE_IN_RETREAT;
                r = new Report(2010);
                if (mw.isCaptured()) {
                    r = new Report(2015);
                    condition = IEntityRemovalConditions.REMOVE_CAPTURED;
                } else {
                    mw.setRetreatedDirection(fleeDirection);
                }
                game.removeEntity(mw.getId(), condition);
                send(createRemoveEntityPacket(mw.getId(), condition));
                r.addDesc(mw);
                r.indent();
                addReport(r);
            }
            // Is the unit being swarmed?
            final int swarmerId = entity.getSwarmAttackerId();
            if (Entity.NONE != swarmerId) {
                final Entity swarmer = game.getEntity(swarmerId);

                // Has the swarmer taken a turn?
                if (!swarmer.isDone()) {

                    // Dead entities don't take turns.
                    game.removeTurnFor(swarmer);
                    send(createTurnVectorPacket());

                } // End swarmer-still-to-move

                // Unit has fled the battlefield.
                swarmer.setSwarmTargetId(Entity.NONE);
                entity.setSwarmAttackerId(Entity.NONE);
                r = new Report(2015, Report.PUBLIC);
                r.indent();
                r.addDesc(swarmer);
                addReport(r);
                game.removeEntity(swarmerId, IEntityRemovalConditions.REMOVE_CAPTURED);
                send(createRemoveEntityPacket(swarmerId, IEntityRemovalConditions.REMOVE_CAPTURED));
            }
            entity.setRetreatedDirection(fleeDirection);
            game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT);
            send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT));
            return;
        }
        
        if (md.contains(MovePath.STEP_EJECT)) {
            if (entity instanceof Mech) {
                r = new Report(2020);
                r.subject = entity.getId();
                r.add(entity.getCrew().getName());
                r.addDesc(entity);
                addReport(r);
            } else if (entity instanceof Tank && !entity.isCarcass()) {
                r = new Report(2025);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
            }
            addReport(ejectEntity(entity, false));

            return;
        }

        if ( md.contains(MovePath.STEP_CAREFUL_STAND) ) {
            entity.setCarefulStand(true);
        }

        // okay, proceed with movement calculations
        Coords lastPos = entity.getPosition();
        Coords curPos = entity.getPosition();
        int curFacing = entity.getFacing();
        int curVTOLElevation = entity.getElevation();
        int startElevation = entity.getElevation();
        int curElevation = entity.getElevation();
        int lastElevation = entity.getElevation();
        // if the entity already used some MPs,
        // it previously tried to get up and fell,
        // and then got another turn. set moveType
        // and overallMoveType accordingly
        // (these are all cleared by Entity.newRound)
        int distance = entity.delta_distance;
        int mpUsed = entity.mpUsed;
        int moveType = entity.moved;
        int overallMoveType = entity.moved;
        boolean firstStep;
        boolean wasProne;
        boolean fellDuringMovement = false;
        boolean turnOver;
        boolean wigeStartedLanded = false;
        int prevFacing = curFacing;
        IHex prevHex = null;
        final boolean isInfantry = entity instanceof Infantry;
        AttackAction charge = null;
        RamAttackAction ram = null;
        PilotingRollData rollTarget;
        // cache this here, otherwise changing MP in the turn causes
        // errorneous gravity PSRs
        int cachedGravityLimit = -1;
        int thrustUsed = 0;
        int j = 0;
        boolean didMove = false;
        boolean recovered = false;
        Entity loader = null;

        // get a list of coordinates that the unit passed through this turn
        // so that I can later recover potential bombing targets
        // it may already have some values
        Vector<Coords> passedThrough = entity.getPassedThrough();// new
                                                                    // Vector<Coords>();
        passedThrough.add(curPos);

        entity.setPriorPosition(curPos);

        // Compile the move
        md.compile(game, entity);

        // if advanced movement is being used then set the new vectors based on
        // movepath
        entity.setVectors(md.getFinalVectors());

        overallMoveType = md.getLastStepMovementType();

        // check for starting in liquid magma
        if (game.getBoard().getHex(entity.getPosition()).terrainLevel(Terrains.MAGMA) == 2 && entity.getElevation() == 0) {
            doMagmaDamage(entity, false);
        }

        // set acceleration used to default
        if (entity instanceof Aero) {
            Aero a = (Aero) entity;
            a.setAccLast(false);
        }

        // iterate through steps
        firstStep = true;
        turnOver = false;
        /* Bug 754610: Revert fix for bug 702735. */
        MoveStep prevStep = null;

        Vector<UnitLocation> movePath = new Vector<UnitLocation>();

        for (final Enumeration<MoveStep> i = md.getSteps(); i.hasMoreElements();) {
            final MoveStep step = i.nextElement();
            wasProne = entity.isProne();
            boolean isPavementStep = step.isPavementStep();
            entity.inReverse = step.isThisStepBackwards();
            boolean entityFellWhileAttemptingToStand = false;

            // stop for illegal movement
            if (step.getMovementType() == IEntityMovementType.MOVE_ILLEGAL) {
                break;
            }

            // stop if the entity already killed itself
            if (entity.isDestroyed() || entity.isDoomed()) {
                break;
            }

            if (firstStep && entity.getMovementMode() == IEntityMovementMode.WIGE && entity.getElevation() == 0) {
                wigeStartedLanded = true;
            }

            // check for MASC failure on first step
            if (firstStep && entity instanceof Mech) {
                HashMap<Integer, CriticalSlot> crits = new HashMap<Integer, CriticalSlot>();
                Vector<Report> vReport = new Vector<Report>();
                if (((Mech) entity).checkForMASCFailure(md, vReport, crits)) {
                    addReport(vReport);
                    for (Integer loc : crits.keySet()) {
                        CriticalSlot cs = crits.get(loc);
                        addReport(applyCriticalHit(entity, loc, cs, true));
                    }
                    // do any PSR immediately
                    addReport(resolvePilotingRolls(entity));
                    game.resetPSRs(entity);
                    // let the player replot their move as MP might be changed
                    md.clear();
                    fellDuringMovement = true; // so they get a new turn
                } else
                    addReport(vReport);
            }

            // did the entity move?
            didMove = step.getDistance() > distance;

            // check fore aero stuff
            if (entity instanceof Aero) {
                Aero a = (Aero) entity;
                j++;

                // TODO: change the way this check is made
                if (!didMove && md.length() != j) {
                    thrustUsed += step.getMp();
                } else {
                    // if this was the last move and distance was zero, then add
                    // thrust
                    if (!didMove && md.length() == j) {
                        thrustUsed += step.getMp();
                    }
                    // then we moved to a new hex or the last step so check
                    // conditions
                    // structural damage
                    rollTarget = a.checkThrustSI(thrustUsed, overallMoveType);
                    if (rollTarget.getValue() != TargetRoll.CHECK_FALSE && !(entity instanceof FighterSquadron) && !game.useVectorMove()) {
                        if (!doSkillCheckInSpace(entity, rollTarget)) {
                            a.setSI(a.getSI() - 1);
                            // check for destruction
                            if (a.getSI() == 0) {
                                destroyEntity(entity, "Structural Integrity Collapse", false);
                            }
                        }
                    }

                    // check for pilot damage
                    int hits = entity.getCrew().getHits();
                    int health = 6 - hits;

                    if (thrustUsed > (2 * health) && !game.useVectorMove() && !(entity instanceof TeleMissile)) {
                        int targetroll = 2 + (thrustUsed - 2 * health) + 2 * hits;
                        resistGForce(entity, targetroll);
                    }

                    thrustUsed = 0;
                }

                rollTarget = a.checkRolls(step, overallMoveType);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    game.addControlRoll(new PilotingRollData(a.getId(), 0, "excess roll"));
                }

                rollTarget = a.checkManeuver(step, overallMoveType);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    if (!doSkillCheckManeuver(entity, rollTarget)) {
                        a.setFailedManeuver(true);
                        int forward = Math.max(step.getVelocityLeft() / 2, 1);
                        if (forward < step.getVelocityLeft()) {
                            fellDuringMovement = true;
                        }
                        while (forward > 0) {
                            curPos = curPos.translated(step.getFacing());
                            forward--;
                            distance++;
                            // make sure it didn't fly off the map
                            if (!game.getBoard().contains(curPos)) {
                                r = new Report(9370, Report.PUBLIC);
                                r.indent();
                                r.addDesc(entity);
                                addReport(r);
                                game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                                send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                                forward = 0;
                                fellDuringMovement = false;
                                return;
                                // make sure it didn't crash
                            } else if (game.getBoard().getHex(curPos).ceiling() >= step.getElevation()) {
                                addReport(processCrash(entity, step.getVelocity()));
                                forward = 0;
                                fellDuringMovement = false;
                            }
                        }
                        break;
                    }
                }

                // if out of control, check for possible collision
                if (didMove && a.isOutControlTotal()) {
                    Enumeration<Entity> targets = game.getEntities(step.getPosition());
                    if (targets.hasMoreElements()) {
                        // Somebody here so check to see if there is a collision
                        int checkroll = Compute.d6(2);
                        // TODO: change this to 11 for Large Craft
                        int targetroll = 11;
                        if (a instanceof Dropship || entity instanceof Jumpship)
                            targetroll = 10;
                        if (checkroll >= targetroll) {
                            // this gets complicated, I need to check for each
                            // unit type
                            // by order of movement subphase
                            Vector<Integer> potentialSpaceStation;
                            Vector<Integer> potentialWarship;
                            Vector<Integer> potentialJumpship;
                            Vector<Integer> potentialDropship;
                            Vector<Integer> potentialSmallCraft;
                            Vector<Integer> potentialASF;
                            potentialSpaceStation = new Vector<Integer>();
                            potentialWarship = new Vector<Integer>();
                            potentialJumpship = new Vector<Integer>();
                            potentialDropship = new Vector<Integer>();
                            potentialSmallCraft = new Vector<Integer>();
                            potentialASF = new Vector<Integer>();
                            while (targets.hasMoreElements()) {
                                int id = targets.nextElement().getId();
                                Entity ce = game.getEntity(id);
                                if (ce instanceof SpaceStation) {
                                    potentialSpaceStation.addElement(id);
                                } else if (ce instanceof Warship) {
                                    potentialWarship.addElement(id);
                                } else if (ce instanceof Jumpship) {
                                    potentialJumpship.addElement(id);
                                } else if (ce instanceof Dropship) {
                                    potentialDropship.addElement(id);
                                } else if (ce instanceof SmallCraft) {
                                    potentialSmallCraft.addElement(id);
                                } else {
                                    potentialASF.addElement(id);
                                }
                            }

                            // ok now go through and see if these have anybody
                            // in them
                            if (potentialSpaceStation.size() > 0) {
                                int chosen = Compute.randomInt(potentialSpaceStation.size());
                                Entity target = game.getEntity(potentialSpaceStation.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            } else if (potentialWarship.size() > 0) {
                                int chosen = Compute.randomInt(potentialWarship.size());
                                Entity target = game.getEntity(potentialWarship.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            } else if (potentialJumpship.size() > 0) {
                                int chosen = Compute.randomInt(potentialJumpship.size());
                                Entity target = game.getEntity(potentialJumpship.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            } else if (potentialDropship.size() > 0) {
                                int chosen = Compute.randomInt(potentialDropship.size());
                                Entity target = game.getEntity(potentialDropship.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            } else if (potentialSmallCraft.size() > 0) {
                                int chosen = Compute.randomInt(potentialSmallCraft.size());
                                Entity target = game.getEntity(potentialSmallCraft.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            } else if (potentialASF.size() > 0) {
                                int chosen = Compute.randomInt(potentialASF.size());
                                Entity target = game.getEntity(potentialASF.elementAt(chosen));
                                Coords dest = target.getPosition();
                                if (processCollision(entity, target, lastPos)) {
                                    curPos = dest;
                                    break;
                                }
                            }

                        }
                    }
                }

                // if in the atmosphere, check for a potential crash

                if (game.getBoard().inAtmosphere() && game.getBoard().getHex(step.getPosition()).ceiling() >= step.getElevation()) {
                    addReport(processCrash(entity, md.getFinalVelocity()));
                    // don't do the rest
                    break;
                }

                // handle fighter launching
                if (step.getType() == MovePath.STEP_LAUNCH) {
                    Vector<Integer[]> launched = step.getLaunched();
                    int nLaunched = launched.size();
                    r = new Report(9380);
                    r.add(entity.getDisplayName());
                    r.subject = entity.getId();
                    r.newlines = 0;
                    r.add(nLaunched);
                    addReport(r);
                    for (int launch = 0; launch < nLaunched; launch++) {
                        int launchId = launched.elementAt(launch)[0];
                        int bonus = launched.elementAt(launch)[1];
                        int doorDamage = launched.elementAt(launch)[2];
                        Bay currentBay = entity.getLoadedBay(launchId);
                        // is there door damage
                        if (doorDamage > 0) {
                            r = new Report(9390);
                            r.subject = entity.getId();
                            r.indent(1);
                            r.newlines = 0;
                            r.add(currentBay.getType());
                            addReport(r);
                            currentBay.destroyDoorNext();
                        }
                        Targetable unloaded = game.getTarget(Targetable.TYPE_ENTITY, launchId);
                        if (!launchUnit(entity, unloaded, curPos, curFacing, step.getVelocity(), step.getElevation(), step.getVectors(), bonus)) {
                            System.err.println("Error! Server was told to unload " + unloaded.getDisplayName() + " from " + entity.getDisplayName() + " into " + curPos.getBoardNum());
                        }
                    }
                    // now apply any damage to bay doors
                    entity.resetBayDoors();
                }

                if (step.getType() == MovePath.STEP_OFF) {
                    r = new Report(9370, Report.PUBLIC);
                    r.indent();
                    r.addDesc(entity);
                    addReport(r);
                    game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                    send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                    return;
                }

            }

            // check piloting skill for getting up
            rollTarget = entity.checkGetUp(step);
            
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                entity.heatBuildup += 1;
                entity.setProne(false);
                //entity.setHullDown(false);
                wasProne = false;
                game.resetPSRs(entity);
                entityFellWhileAttemptingToStand = !doSkillCheckInPlace(entity, rollTarget);
            }
            // did the entity just fall?
            if (entityFellWhileAttemptingToStand) {
                moveType = step.getMovementType();
                curFacing = entity.getFacing();
                curPos = entity.getPosition();
                mpUsed = step.getMpUsed();
                fellDuringMovement = true;
                if ( !entity.isCarefulStand() )
                    break;
            }  else {
                entity.setHullDown(false);
            }

            if (step.getType() == MovePath.STEP_UNJAM_RAC) {
                entity.setUnjammingRAC(true);
                game.addAction(new UnjamAction(entity.getId()));

                // for Aeros this will end movement prematurely
                // if we break
                if (!(entity instanceof Aero))
                    break;
            }

            if (step.getType() == MovePath.STEP_LAY_MINE) {
                LayMinefieldAction lma = new LayMinefieldAction(entity.getId(), step.getMineToLay());
                game.addLayMinefieldAction(lma);
                entity.setLayingMines(true);
                break;
            }
            
            if (step.getType() == MovePath.STEP_CLEAR_MINEFIELD) {
                ClearMinefieldAction cma = new ClearMinefieldAction(entity.getId(), step.getMinefield());
                entity.setClearingMinefield(true);
                game.addAction(cma);
                break;
            }

            if (step.getType() == MovePath.STEP_SEARCHLIGHT && entity.hasSpotlight()) {
                final boolean SearchOn = !entity.isUsingSpotlight();
                entity.setSpotlightState(SearchOn);
                sendServerChat(entity.getDisplayName() + " switched searchlight " + (SearchOn ? "on" : "off") + '.');
            }

            // set most step parameters
            moveType = step.getMovementType();
            distance = step.getDistance();
            mpUsed = step.getMpUsed();

            if (cachedGravityLimit < 0)
                cachedGravityLimit = IEntityMovementType.MOVE_JUMP == moveType ? entity.getJumpMP(false) : entity.getRunMP(false, false);
            // check for charge
            if (step.getType() == MovePath.STEP_CHARGE) {
                if (entity.canCharge()) {
                    checkExtremeGravityMovement(entity, step, curPos, cachedGravityLimit);
                    Targetable target = step.getTarget(game);
                    ChargeAttackAction caa = new ChargeAttackAction(entity.getId(), target.getTargetType(), target.getTargetId(), target.getPosition());
                    entity.setDisplacementAttack(caa);
                    game.addCharge(caa);
                    charge = caa;
                } else {
                    sendServerChat("Illegal charge!! I don't think " + entity.getDisplayName() + " should be allowed to charge," + " but the client of " + entity.getOwner().getName() + " disagrees.");
                    sendServerChat("Please make sure " + entity.getOwner().getName() + " is running MegaMek " + MegaMek.VERSION + ", or if that is already the case, submit a bug report at http://megamek.sf.net/");
                    return;
                }
                break;
            }

            // check for dfa
            if (step.getType() == MovePath.STEP_DFA) {
                if (entity.canDFA()) {
                    checkExtremeGravityMovement(entity, step, curPos, cachedGravityLimit);
                    Targetable target = step.getTarget(game);
                    DfaAttackAction daa = new DfaAttackAction(entity.getId(), target.getTargetType(), target.getTargetId(), target.getPosition());
                    entity.setDisplacementAttack(daa);
                    game.addCharge(daa);
                    charge = daa;
                } else {
                    sendServerChat("Illegal DFA!! I don't think " + entity.getDisplayName() + " should be allowed to DFA," + " but the client of " + entity.getOwner().getName() + " disagrees.");
                    sendServerChat("Please make sure " + entity.getOwner().getName() + " is running MegaMek " + MegaMek.VERSION + ", or if that is already the case, submit a bug report at http://megamek.sf.net/");
                    return;
                }
                break;
            }

            // check for ram
            if (step.getType() == MovePath.STEP_RAM) {
                if (entity.canRam()) {
                    Targetable target = step.getTarget(game);
                    RamAttackAction raa = new RamAttackAction(entity.getId(), target.getTargetType(), target.getTargetId(), target.getPosition());
                    entity.setRamming(true);
                    game.addRam(raa);
                    ram = raa;
                } else {
                    sendServerChat("Illegal ram!! I don't think " + entity.getDisplayName() + " should be allowed to charge," + " but the client of " + entity.getOwner().getName() + " disagrees.");
                    sendServerChat("Please make sure " + entity.getOwner().getName() + " is running MegaMek " + MegaMek.VERSION + ", or if that is already the case, submit a bug report at http://megamek.sf.net/");
                    return;
                }
                break;
            }

            if (step.getType() == MovePath.STEP_ACC || step.getType() == MovePath.STEP_ACCN) {
                if (entity instanceof Aero) {
                    Aero a = (Aero) entity;
                    if (step.getType() == MovePath.STEP_ACCN) {
                        a.setAccLast(true);
                        a.setNextVelocity(a.getNextVelocity() + 1);
                    } else {
                        a.setAccDecNow(true);
                        a.setCurrentVelocity(a.getCurrentVelocity() + 1);
                        a.setNextVelocity(a.getNextVelocity() + 1);
                    }
                }
            }

            if (step.getType() == MovePath.STEP_DEC || step.getType() == MovePath.STEP_DECN) {
                if (entity instanceof Aero) {
                    Aero a = (Aero) entity;
                    if (step.getType() == MovePath.STEP_DECN) {
                        a.setAccLast(true);
                        a.setNextVelocity(a.getNextVelocity() - 1);
                    } else {
                        a.setAccDecNow(true);
                        a.setCurrentVelocity(a.getCurrentVelocity() - 1);
                        a.setNextVelocity(a.getNextVelocity() - 1);
                    }
                }
            }

            if (step.getType() == MovePath.STEP_EVADE) {
                entity.setEvading(true);
                
            }

            if (step.getType() == MovePath.STEP_STALL) {
                // TODO: check VSTOL status
                r = new Report(9391);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.newlines = 0;
                addReport(r);
                game.addControlRoll(new PilotingRollData(entity.getId(), 0, "stalled out"));
                // check for crash
                if (game.getBoard().getHex(step.getPosition()).ceiling() >= step.getElevation()) {
                    addReport(processCrash(entity, 0));
                    // don't do the rest
                    break;
                }
            }

            if (step.getType() == MovePath.STEP_ROLL) {
                if (entity instanceof Aero) {
                    Aero a = (Aero) entity;
                    if (a.isRolled()) {
                        a.setRolled(false);
                    } else {
                        a.setRolled(true);
                    }

                }
            }

            // check for dig in or fortify
            if (entity instanceof Infantry) {
                Infantry inf = (Infantry) entity;
                if (step.getType() == MovePath.STEP_DIG_IN) {
                    inf.setDugIn(Infantry.DUG_IN_WORKING);
                    continue;
                } else if (step.getType() == MovePath.STEP_FORTIFY) {
                    if (!entity.hasWorkingMisc(MiscType.F_TOOLS, MiscType.S_VIBROSHOVEL)) {
                        sendServerChat(entity.getDisplayName() + " failed to fortify because it is missing suitable equipment");
                    }
                    inf.setDugIn(Infantry.DUG_IN_FORTIFYING1);
                    continue;
                } else if (step.getType() != MovePath.STEP_TURN_LEFT && step.getType() != MovePath.STEP_TURN_RIGHT) {
                    // other movement clears dug in status
                    inf.setDugIn(Infantry.DUG_IN_NONE);
                }
            }

            // set last step parameters
            curPos = step.getPosition();
            if (moveType != IEntityMovementType.MOVE_JUMP || entity.getJumpType() != Mech.JUMP_BOOSTER)
                curFacing = step.getFacing();
            // check if a building PSR will be needed later, before setting the
            // new elevation
            int buildingMove = entity.checkMovementInBuilding(step, prevStep, curPos, lastPos);
            curVTOLElevation = step.getElevation();
            curElevation = step.getElevation();
            // set elevation in case of collapses
            entity.setElevation(step.getElevation());

            IHex curHex = game.getBoard().getHex(curPos);

            // check for automatic unstick
            if (entity.canUnstickByJumping() && entity.isStuck() && moveType == IEntityMovementType.MOVE_JUMP) {
                entity.setStuck(false);
                entity.setCanUnstickByJumping(false);
            }

            // Check for skid.
            rollTarget = entity.checkSkid(moveType, prevHex, overallMoveType, prevStep, prevFacing, curFacing, lastPos, curPos, isInfantry, distance);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                // Have an entity-meaningful PSR message.
                boolean psrFailed = true;
                if (entity instanceof Mech) {
                    psrFailed = (0 < doSkillCheckWhileMoving(entity, lastPos, lastPos, rollTarget, true));
                } else {
                    psrFailed = (0 < doSkillCheckWhileMoving(entity, lastPos, lastPos, rollTarget, false));
                }
                // Does the entity skid?
                if (psrFailed) {

                    if (entity instanceof Tank) {
                        addReport(vehicleMotiveDamage((Tank) entity, 0));
                    }

                    curPos = lastPos;
                    int skidDistance = (distance + 1) / 2;
                    int skidDirection = prevFacing;

                    // All charge damage is based upon
                    // the pre-skid move distance.
                    entity.delta_distance = distance - 1;

                    // Attacks against a skidding target have additional +2.
                    moveType = IEntityMovementType.MOVE_SKID;

                    // What is the first hex in the skid?
                    if (step.isThisStepBackwards()) {
                        skidDirection = (skidDirection + 3) % 6;
                    }

                    if (processSkid(entity, curPos, step.getElevation(), skidDirection, skidDistance, step))
                        return;

                    // set entity parameters
                    curFacing = entity.getFacing();
                    curPos = entity.getPosition();
                    entity.setSecondaryFacing(curFacing);

                    // skid consumes all movement
                    if (md.hasActiveMASC()) {
                        mpUsed = entity.getRunMP();
                    } else {
                        mpUsed = entity.getRunMPwithoutMASC();
                    }

                    entity.moved = moveType;
                    fellDuringMovement = true;
                    turnOver = true;
                    distance = entity.delta_distance;
                    break;

                } // End failed-skid-psr

            } // End need-skid-psr

            // check sideslip
            if (entity instanceof VTOL || entity.getMovementMode() == IEntityMovementMode.HOVER || entity.getMovementMode() == IEntityMovementMode.WIGE) {
                rollTarget = entity.checkSideSlip(moveType, prevHex, overallMoveType, prevStep, prevFacing, curFacing, lastPos, curPos, distance);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    int MoF = doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, false);
                    if (MoF > 0) {
                        // maximum distance is hexes moved / 2
                        int sideslipDistance = Math.min(MoF, distance - 1);
                        if (sideslipDistance > 0) {
                            int skidDirection = prevFacing;
                            // report sideslip
                            sideslipped = true;
                            r = new Report(2100);
                            r.subject = entity.getId();
                            r.addDesc(entity);
                            r.add(sideslipDistance);
                            addReport(r);

                            if (processSkid(entity, lastPos, step.getElevation(), skidDirection, sideslipDistance, step))
                                return;

                            if (!entity.isDestroyed() && !entity.isDoomed()) {
                                fellDuringMovement = true; // No, but it should
                                // work...
                            }

                            if (entity.getElevation() == 0 && (entity.getMovementMode() == IEntityMovementMode.VTOL || entity.getMovementMode() == IEntityMovementMode.WIGE)) {
                                turnOver = true;
                            }
                            // set entity parameters
                            curFacing = entity.getFacing();
                            curPos = entity.getPosition();
                            entity.setSecondaryFacing(curFacing);
                            break;
                        }
                    }
                }
            }

            // check if we've moved into rubble
            rollTarget = entity.checkRubbleMove(step, curHex, lastPos, curPos);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, true);
            }
            
            //check if we are using reckless movement
            rollTarget = entity.checkRecklessMove(step, curHex, lastPos, curPos, lastElevation);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                if(entity instanceof Mech) {
                    doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, true);
                } else if (entity instanceof Tank) {
                    if(0 < doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, false)) {
                        //assume VTOLs in flight are always in clear terrain
                        if(0 == curHex.terrainsPresent() || step.getElevation() > 0) {
                            r = new Report(2206);
                            r.addDesc(entity);
                            r.subject = entity.getId();
                            addReport(r);
                            mpUsed = step.getMpUsed() + 1;
                            fellDuringMovement = true;
                            break;
                        } else {
                            r = new Report(2207);
                            r.addDesc(entity);
                            r.subject = entity.getId();
                            addReport(r);
                            //until we get a rules clarification assume that the entity is both giver and taker
                            //for charge damage
                            HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                            addReport(damageEntity(entity, hit, ChargeAttackAction.getDamageTakenBy(entity, entity)));
                            turnOver = true;
                            break;
                        }
                    }
                }
            }

            // check for breaking magma crust
            if (curHex.terrainLevel(Terrains.MAGMA) == 1 && step.getElevation() == 0 && step.getMovementType() != IEntityMovementType.MOVE_JUMP) {
                int roll = Compute.d6(1);
                r = new Report(2395);
                r.addDesc(entity);
                r.add(roll);
                r.subject = entity.getId();
                addReport(r);
                if (roll == 6) {
                    curHex.removeTerrain(Terrains.MAGMA);
                    curHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.MAGMA, 2));
                    sendChangedHex(curPos);
                    for (Enumeration<Entity> e = game.getEntities(curPos); e.hasMoreElements();) {
                        Entity en = e.nextElement();
                        if (en != entity)
                            doMagmaDamage(en, false);
                    }
                }
            }

            // check for entering liquid magma
            if (curHex.terrainLevel(Terrains.MAGMA) == 2 && step.getElevation() == 0 && step.getMovementType() != IEntityMovementType.MOVE_JUMP) {
                doMagmaDamage(entity, false);
            }

            // check if we've moved into a swamp
            rollTarget = entity.checkBogDown(step, curHex, lastPos, curPos, lastElevation, isPavementStep);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                if (0 < doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, false)) {
                    entity.setStuck(true);
                    entity.setCanUnstickByJumping(true);
                    r = new Report(2081);
                    r.add(entity.getDisplayName());
                    r.subject = entity.getId();
                    addReport(r);
                    //check for quicksand
                    addReport(checkQuickSand(curPos));
                    // check for accidental stacking violation
                    Entity violation = Compute.stackingViolation(game, entity.getId(), curPos);
                    if (violation != null) {
                        // target gets displaced, because of low elevation
                        int direction = lastPos.direction(curPos);
                        Coords targetDest = Compute.getValidDisplacement(game, entity.getId(), curPos, direction);
                        addReport(doEntityDisplacement(violation, curPos, targetDest, new PilotingRollData(violation.getId(), 0, "domino effect")));
                        // Update the violating entity's postion on the client.
                        entityUpdate(violation.getId());
                    }
                    break;
                }
            }

            // check to see if we are a mech and we've moved OUT of fire
            IHex lastHex = game.getBoard().getHex(lastPos);
            if (entity instanceof Mech) {
                if (!lastPos.equals(curPos) && prevStep != null && ((lastHex.containsTerrain(Terrains.FIRE) && prevStep.getElevation() <= 1) || (lastHex.containsTerrain(Terrains.MAGMA) && prevStep.getElevation() == 0)) && (step.getMovementType() != IEntityMovementType.MOVE_JUMP
                // Bug #828741 -- jumping bypasses fire, but not on the
                        // first step
                        // getMpUsed -- total MP used to this step
                        // getMp -- MP used in this step
                        // the difference will always be 0 on the "first step"
                        // of a jump,
                        // and >0 on a step in the midst of a jump
                        || 0 == step.getMpUsed() - step.getMp())) {
                    int heat = 0;
                    if (lastHex.containsTerrain(Terrains.FIRE))
                        heat += 2;
                    if (lastHex.terrainLevel(Terrains.MAGMA) == 1) {
                        heat += 2;
                    } else if (lastHex.terrainLevel(Terrains.MAGMA) == 2) {
                        heat += 5;
                    }
                    entity.heatFromExternal += heat;
                    r = new Report(2115);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(heat);
                    addReport(r);
                }
            }

            // check to see if we are not a mech and we've moved INTO fire
            if (!(entity instanceof Mech)) {
                if (game.getBoard().getHex(curPos).containsTerrain(Terrains.FIRE) && !lastPos.equals(curPos) && step.getMovementType() != IEntityMovementType.MOVE_JUMP && step.getElevation() <= 1) {
                    doFlamingDamage(entity);
                }
            }
            // check for extreme gravity movement
            if (!i.hasMoreElements() && !firstStep) {
                checkExtremeGravityMovement(entity, step, curPos, cachedGravityLimit);
            }
            // check for minefields. have to check both new hex and new elevation 
            // VTOLs may land and submarines may rise or lower into a minefield
            if (!lastPos.equals(curPos) ||  lastElevation != curElevation) {
                boolean boom = false;
                boolean isOnGround = !i.hasMoreElements();
                isOnGround |= step.getMovementType() != IEntityMovementType.MOVE_JUMP;
                isOnGround &= step.getElevation() < 1;
                if(isOnGround)
                    boom = checkVibrabombs(entity, curPos, false, lastPos, curPos, vPhaseReport);
                if (game.containsMinefield(curPos)) {                   
                    // set the new position temporarily, because
                    // infantry otherwise would get double damage
                    // when moving from clear into mined woods
                    entity.setPosition(curPos);
                    if(enterMinefield(entity, curPos, step.getElevation(), isOnGround, vPhaseReport)) {
                        //resolve any piloting rolls from damage unless unit was jumping
                        if(step.getMovementType() != IEntityMovementType.MOVE_JUMP) {
                            addReport(resolvePilotingRolls(entity));
                            game.resetPSRs(entity); 
                        }
                        boom = true;
                    } 
                    if(wasProne || !entity.isProne()) {
                        entity.setPosition(lastPos);
                    }
                }
                //did anything go boom?
                if(boom) {
                    //set fell during movement so that entity will get another chance to move with any motive damage 
                    //taken account of (functions the same as MASC failure)
                    //only do this if they had more steps (and they were not jumping
                    if(i.hasMoreElements() && step.getMovementType() != IEntityMovementType.MOVE_JUMP) {
                        md.clear();
                        fellDuringMovement = true;
                    }
                    //reset mines if anything detonated
                    resetMines();
                }
            }

            // infantry discovers minefields if they end their move
            // in a minefield.
            if (!lastPos.equals(curPos) && !i.hasMoreElements() && isInfantry) {
                if (game.containsMinefield(curPos)) {
                    Player owner = entity.getOwner();
                    for (Minefield mf : game.getMinefields(curPos)) {
                        if (!owner.containsMinefield(mf)) {
                            r = new Report(2120);
                            r.subject = entity.getId();
                            r.add(entity.getShortName(), true);
                            addReport(r);
                            revealMinefield(game.getTeamForPlayer(owner), mf);
                        }
                    }
                }
            }

            // check if we've moved into water
            rollTarget = entity.checkWaterMove(step, curHex, lastPos, curPos, isPavementStep);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                // Swarmers need special handling.
                final int swarmerId = entity.getSwarmAttackerId();
                boolean swarmerDone = true;
                Entity swarmer = null;
                if (Entity.NONE != swarmerId) {
                    swarmer = game.getEntity(swarmerId);
                    swarmerDone = swarmer.isDone();
                }

                // Now do the skill check.
                doSkillCheckWhileMoving(entity, lastPos, curPos, rollTarget, true);

                // Swarming infantry platoons may drown.
                if (curHex.terrainLevel(Terrains.WATER) > 1) {
                    drownSwarmer(entity, curPos);
                }

                // Do we need to remove a game turn for the swarmer
                if (!swarmerDone && swarmer != null && (swarmer.isDoomed() || swarmer.isDestroyed())) {
                    // We have to diddle with the swarmer's
                    // status to get its turn removed.
                    swarmer.setDone(false);
                    swarmer.setUnloaded(false);

                    // Dead entities don't take turns.
                    game.removeTurnFor(swarmer);
                    send(createTurnVectorPacket());

                    // Return the original status.
                    swarmer.setDone(true);
                    swarmer.setUnloaded(true);
                }

                // check for inferno wash-off
                checkForWashedInfernos(entity, curPos);
            }

            // In water, may or may not be a new hex, neccessary to
            // check during movement, for breach damage, and always
            // set dry if appropriate
            // TODO: possibly make the locations local and set later
            addReport(doSetLocationsExposure(entity, curHex, step.getMovementType() == IEntityMovementType.MOVE_JUMP, step.getElevation()));

            // check for breaking ice by breaking through from below
            if (lastElevation < 0 && step.getElevation() == 0 
                    && lastHex.containsTerrain(Terrains.ICE) && lastHex.containsTerrain(Terrains.WATER) 
                    && step.getMovementType() != IEntityMovementType.MOVE_JUMP && !lastPos.equals(curPos)) {
                //need to temporarily reset entity's position so it doesn't fall in the ice
                entity.setPosition(curPos);
                r = new Report(2410);
                r.addDesc(entity);
                addReport(r);
                addReport(resolveIceBroken(lastPos));
                //ok now set back
                entity.setPosition(lastPos);
            }
            // check for breaking ice by stepping on it
            if (curHex.containsTerrain(Terrains.ICE) && curHex.containsTerrain(Terrains.WATER) 
                    && step.getMovementType() != IEntityMovementType.MOVE_JUMP 
                    && !lastPos.equals(curPos)
                    && !(entity instanceof Infantry)) {
                if (step.getElevation() == 0) {
                    int roll = Compute.d6(1);
                    r = new Report(2118);
                    r.addDesc(entity);
                    r.add(roll);
                    r.subject = entity.getId();
                    addReport(r);
                    if (roll == 6) {
                        entity.setPosition(curPos);
                        addReport(resolveIceBroken(curPos));
                        curPos = entity.getPosition();
                    }
                }
                // or intersecting it
                else if (step.getElevation() + entity.height() == 0) {
                    r = new Report(2410);
                    r.addDesc(entity);
                    addReport(r);
                    addReport(resolveIceBroken(curPos));
                }
            }

            // Handle loading units.
            if (step.getType() == MovePath.STEP_LOAD) {

                // Find the unit being loaded.
                Entity loaded = null;
                Enumeration<Entity> entities = game.getEntities(curPos);
                while (entities.hasMoreElements()) {

                    // Is the other unit friendly and not the current entity?
                    loaded = entities.nextElement();
                    if (!entity.isEnemyOf(loaded) && !entity.equals(loaded)) {

                        // The moving unit should be able to load the other
                        // unit and the other should be able to have a turn.
                        if (!entity.canLoad(loaded) || !loaded.isLoadableThisTurn()) {
                            // Something is fishy in Denmark.
                            System.err.println(entity.getShortName() + " can not load " + loaded.getShortName());
                            loaded = null;
                        } else {
                            // Have the deployed unit load the indicated unit.
                            loadUnit(entity, loaded);

                            // Stop looking.
                            break;
                        }

                    } else {
                        // Nope. Discard it.
                        loaded = null;
                    }

                } // Handle the next entity in this hex.

                // We were supposed to find someone to load.
                if (loaded == null) {
                    System.err.println("Could not find unit for " + entity.getShortName() + " to load in " + curPos);
                }

            } // End STEP_LOAD

            // handle fighter recovery
            if (step.getType() == MovePath.STEP_RECOVER) {

                loader = game.getEntity(step.getRecoveryUnit());

                PilotingRollData psr = entity.getBasePilotingRoll(overallMoveType);
                if (loader.mpUsed > 0) {
                    psr.addModifier(5, "carrier used thrust");
                }
                int ctrlroll = Compute.d6(2);
                r = new Report(9381);
                r.subject = entity.getId();
                r.add(entity.getDisplayName());
                r.add(loader.getDisplayName());
                r.add(psr.getValue());
                r.add(ctrlroll);
                r.newlines = 0;
                r.indent(1);
                if (ctrlroll < psr.getValue()) {
                    r.choose(false);
                    addReport(r);
                    // damage unit
                    Aero a = (Aero) entity;
                    HitData hit = a.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    addReport(damageEntity(entity, hit, 2 * (psr.getValue() - ctrlroll)));
                } else {
                    r.choose(true);
                    addReport(r);
                    recovered = true;
                }
                // check for door damage
                if (ctrlroll == 2) {
                    loader.damageDoorRecovery(entity);
                    r = new Report(9384);
                    r.subject = entity.getId();
                    r.indent(0);
                    r.add(loader.getDisplayName());
                    addReport(r);
                }
            }

            // Handle unloading units.
            if (step.getType() == MovePath.STEP_UNLOAD) {
                Targetable unloaded = step.getTarget(game);
                if (!unloadUnit(entity, unloaded, curPos, curFacing, step.getElevation())) {
                    System.err.println("Error! Server was told to unload " + unloaded.getDisplayName() + " from " + entity.getDisplayName() + " into " + curPos.getBoardNum());
                }
            }

            if ((step.getType() == MovePath.STEP_BACKWARDS || step.getType() == MovePath.STEP_LATERAL_LEFT_BACKWARDS || step.getType() == MovePath.STEP_LATERAL_RIGHT_BACKWARDS) && game.getBoard().getHex(lastPos).getElevation() != curHex.getElevation() && !(entity instanceof VTOL)) {

                PilotingRollData psr = entity.getBasePilotingRoll(overallMoveType);
                int roll = Compute.d6(2);
                if (entity instanceof Tank)
                    r = new Report(2435);
                else
                    r = new Report(2430);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(psr.getValue());
                r.add(roll);
                addReport(r);

                if (roll < psr.getValue()) {
                    if (entity instanceof Mech) {
                        if (curHex.getElevation() < game.getBoard().getHex(lastPos).getElevation()) {
                            entity.setElevation(Integer.MIN_VALUE);
                            addReport(doEntityFallsInto(entity, lastPos, curPos, entity.getBasePilotingRoll(overallMoveType), false));
                        } else {
                            // Tac Ops p.22
                            // the change in levels is not taken into
                            // consideration for determining the levels fallen.
                            entity.setElevation(Integer.MIN_VALUE);
                            addReport(doEntityFallsInto(entity, curPos, lastPos, entity.getBasePilotingRoll(overallMoveType), false));
                        }
                    } else if (entity instanceof Tank) {
                        curPos = lastPos;
                    }
                }
            }

            // Handle non-infantry moving into a building.
            if (buildingMove > 0) {

                // Get the building being exited.
                Building bldgExited = null;
                if ((buildingMove & 1) == 1)
                    bldgExited = game.getBoard().getBuildingAt(lastPos);

                // Get the building being entered.
                Building bldgEntered = null;
                if ((buildingMove & 2) == 2)
                    bldgEntered = game.getBoard().getBuildingAt(curPos);

                // Get the building being stepped on.
                Building bldgStepped = null;
                if ((buildingMove & 4) == 4)
                    bldgStepped = game.getBoard().getBuildingAt(curPos);

                boolean collapsed = false;
                // are we passing through a building wall?
                if (bldgEntered != null || bldgExited != null) {
                    // If we're not leaving a building, just handle the
                    // "entered".
                    if (bldgExited == null) {
                        collapsed = passBuildingWall(entity, bldgEntered, lastPos, curPos, distance, "entering", step.isThisStepBackwards(), step.getParent().getLastStepMovementType(), true);
                        addAffectedBldg(bldgEntered, collapsed);
                    }

                    // If we're moving within the same building, just handle
                    // the "within".
                    else if (bldgExited.equals(bldgEntered)) {
                        collapsed = passBuildingWall(entity, bldgEntered, lastPos, curPos, distance, "moving in", step.isThisStepBackwards(), step.getParent().getLastStepMovementType(), true);
                        addAffectedBldg(bldgEntered, collapsed);
                    }

                    // If we have different buildings, roll for each.
                    else if (bldgEntered != null) {
                        if (entity instanceof Protomech) {
                            // protos entering a building cause 1 damage
                            Vector<Report> vBuildingReport = damageBuilding(bldgEntered, 1, curPos);
                            for (Report report : vBuildingReport)
                                report.subject = entity.getId();
                            addReport(vBuildingReport);
                        }
                        collapsed = passBuildingWall(entity, bldgExited, lastPos, curPos, distance, "exiting", step.isThisStepBackwards(), step.getParent().getLastStepMovementType(), false);
                        addAffectedBldg(bldgExited, collapsed);
                        collapsed = passBuildingWall(entity, bldgEntered, lastPos, curPos, distance, "entering", step.isThisStepBackwards(), step.getParent().getLastStepMovementType(), true);
                        addAffectedBldg(bldgEntered, collapsed);
                    }

                    // Otherwise, just handle the "exited".
                    else {
                        collapsed = passBuildingWall(entity, bldgExited, lastPos, curPos, distance, "exiting", step.isThisStepBackwards(), step.getParent().getLastStepMovementType(), false);
                        addAffectedBldg(bldgExited, collapsed);
                    }
                }

                // stepping on roof, no PSR just check for over weight
                if (bldgStepped != null) {
                    collapsed = checkBuildingCollapseWhileMoving(bldgStepped, entity, curPos);
                    addAffectedBldg(bldgStepped, collapsed);
                }

                // Clean up the entity if it has been destroyed.
                if (entity.isDoomed()) {
                    entity.setDestroyed(true);
                    game.moveToGraveyard(entity.getId());
                    send(createRemoveEntityPacket(entity.getId()));

                    // The entity's movement is completed.
                    return;
                }

                // TODO: what if a building collapses into rubble?
            }

            // did the entity just fall?
            if (!wasProne && entity.isProne()) {
                curFacing = entity.getFacing();
                curPos = entity.getPosition();
                mpUsed = step.getMpUsed();
                fellDuringMovement = true;
                break;
            }

            // dropping prone intentionally?
            if (step.getType() == MovePath.STEP_GO_PRONE) {
                mpUsed = step.getMpUsed();
                rollTarget = entity.checkDislodgeSwarmers(step);
                if (rollTarget.getValue() == TargetRoll.CHECK_FALSE) {
                    // Not being swarmed
                    entity.setProne(true);
                    // check to see if we washed off infernos
                    checkForWashedInfernos(entity, curPos);
                } else {
                    // Being swarmed
                    entity.setPosition(curPos);
                    if (doDislodgeSwarmerSkillCheck(entity, rollTarget, curPos)) {
                        // Entity falls
                        curFacing = entity.getFacing();
                        curPos = entity.getPosition();
                        fellDuringMovement = true;
                        break;
                    }
                    // roll failed, go prone but don't dislodge swarmers
                    entity.setProne(true);
                    // check to see if we washed off infernos
                    checkForWashedInfernos(entity, curPos);
                    break;
                }
            }

            // going hull down
            if (step.getType() == MovePath.STEP_HULL_DOWN) {
                mpUsed = step.getMpUsed();
                entity.setHullDown(true);
            }

            // Track this step's location.
            movePath.addElement(new UnitLocation(entity.getId(), curPos, curFacing, step.getElevation()));

            // if the lastpos is not the same as the current position
            // then add the current position to the list of places passed
            // through
            if (!curPos.equals(lastPos)) {
                passedThrough.add(curPos);
                entity.setPriorPosition(lastPos);
            }

            // update lastPos, prevStep, prevFacing & prevHex
            lastPos = new Coords(curPos);
            lastElevation = curElevation;
            prevStep = step;
            /*
             * Bug 754610: Revert fix for bug 702735. if (prevHex != null &&
             * !curHex.equals(prevHex)) {
             */
            if (!curHex.equals(prevHex)) {
                prevFacing = curFacing;
            }
            prevHex = curHex;

            firstStep = false;
        }

        // set entity parameters
        entity.setPosition(curPos);
        entity.setFacing(curFacing);
        entity.setSecondaryFacing(curFacing);
        entity.delta_distance = distance;
        entity.moved = moveType;
        entity.mpUsed = mpUsed;
        if (!sideslipped && !fellDuringMovement) {
            entity.setElevation(curVTOLElevation);
        }
        entity.setClimbMode(md.getFinalClimbMode());

        // add a list of places passed through
        entity.setPassedThrough(passedThrough);

        // if we ran with destroyed hip or gyro, we need a psr
        rollTarget = entity.checkRunningWithDamage(overallMoveType);
        if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
            doSkillCheckInPlace(entity, rollTarget);
        }

        if (entity instanceof Aero) {

            Aero a = (Aero) entity;
            int thrust = md.getMpUsed();

            // set straight movement
            if (null != md.getLastStep()) {
                a.setStraightMoves(md.getLastStep().getNStraight());
            } else {
                a.setStraightMoves(0);
            }

            // consume fuel
            if ((entity instanceof Aero && game.getOptions().booleanOption("fuel_consumption")) || entity instanceof TeleMissile) {
                int fuelUsed = ((Aero) entity).getFuelUsed(thrust);
                a.setFuel(Math.max(a.getFuel() - fuelUsed, 0));
            }

            // check to see if thrust exceeded SI

            rollTarget = a.checkThrustSITotal(thrust, overallMoveType);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                game.addControlRoll(new PilotingRollData(a.getId(), 0, "Thrust spent during turn exceeds SI"));
            }

            if (game.getBoard().inAtmosphere()) {
                rollTarget = a.checkVelocityDouble(md.getFinalVelocity(), overallMoveType);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    game.addControlRoll(new PilotingRollData(a.getId(), 0, "Velocity greater than 2x safe thrust"));
                }

                rollTarget = a.checkDown(md.getFinalNDown(), overallMoveType);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    game.addControlRoll(new PilotingRollData(a.getId(), md.getFinalNDown(), "descended more than two altitudes"));
                }

                // check for hovering
                rollTarget = a.checkHover(md);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    game.addControlRoll(new PilotingRollData(a.getId(), 0, "hovering"));
                }

                // if this is a spheroid then check to see if it loses elevation
                // TODO: I am currently awaiting a rules decision on the forums
                // regarding
                // exactly when this elevation loss occurs. For the moment lets
                // assume it occurs
                // if there was no elevation change during the turn and no hover
                // step
                if ((a.isSpheroid() || game.getPlanetaryConditions().isVacuum()) && startElevation == curElevation && !md.contains(MovePath.STEP_HOVER)) {
                    a.setElevation(a.getElevation() - 1);
                    // check for crash
                    if (game.getBoard().getHex(a.getPosition()).ceiling() >= a.getElevation()) {
                        addReport(processCrash(entity, md.getFinalVelocity()));
                    }
                }

            }
        }

        // If the entity is being swarmed, erratic movement may dislodge the
        // fleas.
        final int swarmerId = entity.getSwarmAttackerId();
        if (Entity.NONE != swarmerId && md.contains(MovePath.STEP_SHAKE_OFF_SWARMERS)) {
            final Entity swarmer = game.getEntity(swarmerId);
            final PilotingRollData roll = entity.getBasePilotingRoll(overallMoveType);

            entity.addPilotingModifierForTerrain(roll);

            // Add a +4 modifier.
            if (md.getLastStepMovementType() == IEntityMovementType.MOVE_VTOL_RUN)
                roll.addModifier(2, "dislodge swarming infantry with VTOL movement");
            else
                roll.addModifier(4, "dislodge swarming infantry");

            // If the swarmer has Assault claws, give a 1 modifier.
            // We can stop looking when we find our first match.
            for (Mounted mount : swarmer.getMisc()) {
                EquipmentType equip = mount.getType();
                if (BattleArmor.ASSAULT_CLAW.equals(equip.getInternalName())) {
                    roll.addModifier(1, "swarmer has assault claws");
                    break;
                }
            }

            // okay, print the info
            r = new Report(2125);
            r.subject = entity.getId();
            r.addDesc(entity);
            addReport(r);

            // roll
            final int diceRoll = Compute.d6(2);
            r = new Report(2130);
            r.subject = entity.getId();
            r.add(roll.getValueAsString());
            r.add(roll.getDesc());
            r.add(diceRoll);
            if (diceRoll < roll.getValue()) {
                r.choose(false);
                addReport(r);
            } else {
                // Dislodged swarmers don't get turns.
                game.removeTurnFor(swarmer);
                send(createTurnVectorPacket());

                // Update the report and the swarmer's status.
                r.choose(true);
                addReport(r);
                entity.setSwarmAttackerId(Entity.NONE);
                swarmer.setSwarmTargetId(Entity.NONE);

                IHex curHex = game.getBoard().getHex(curPos);

                // Did the infantry fall into water?
                if (curHex.terrainLevel(Terrains.WATER) > 0) {
                    // Swarming infantry die.
                    swarmer.setPosition(curPos);
                    r = new Report(2135);
                    r.subject = entity.getId();
                    r.indent();
                    r.addDesc(swarmer);
                    addReport(r);
                    addReport(destroyEntity(swarmer, "a watery grave", false));
                } else {
                    // Swarming infantry take a 3d6 point hit.
                    // ASSUMPTION : damage should not be doubled.
                    r = new Report(2140);
                    r.subject = entity.getId();
                    r.indent();
                    r.addDesc(swarmer);
                    r.add("3d6");
                    addReport(r);
                    addReport(damageEntity(swarmer, swarmer.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Compute.d6(3)));
                    addNewLines();
                    swarmer.setPosition(curPos);
                }
                entityUpdate(swarmerId);
            } // End successful-PSR

        } // End try-to-dislodge-swarmers

        // but the danger isn't over yet! landing from a jump can be risky!
        if (overallMoveType == IEntityMovementType.MOVE_JUMP && !entity.isMakingDfa()) {
            final IHex curHex = game.getBoard().getHex(curPos);
            // check for damaged criticals
            rollTarget = entity.checkLandingWithDamage(overallMoveType);
            if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                doSkillCheckInPlace(entity, rollTarget);
            }
            // jumped into water?
            int waterLevel = curHex.terrainLevel(Terrains.WATER);
            if (curHex.containsTerrain(Terrains.ICE) && waterLevel > 0) {
                if(!(entity instanceof Infantry)) {
                    waterLevel = 0;
                    // check for breaking ice
                    int roll = Compute.d6(1);
                    r = new Report(2122);
                    r.add(entity.getDisplayName(), true);
                    r.add(roll);
                    r.subject = entity.getId();
                    addReport(r);
                    if (roll >= 4) {
                        // oops!
                        entity.setPosition(curPos);
                        addReport(resolveIceBroken(curPos));
                        curPos = entity.getPosition();
                    } else {
                        //TacOps: immediate PSR with +4 for terrain. If you fall then may break the ice after all
                        rollTarget = entity.checkLandingOnIce(overallMoveType, curHex);
                        if(!doSkillCheckInPlace(entity, rollTarget)) {
                            //apply damage now, or it will show up as a possible breach, if ice is broken
                            entity.applyDamage();
                            roll = Compute.d6(1);
                            r = new Report(2118);
                            r.addDesc(entity);
                            r.add(roll);
                            r.subject = entity.getId();
                            addReport(r);
                            if(roll == 6) {
                                entity.setPosition(curPos);
                                addReport(resolveIceBroken(curPos));
                                curPos = entity.getPosition();
                            }
                        }
                    }
                }
            } else if (!(prevStep.climbMode() && curHex.containsTerrain(Terrains.BRIDGE))) {
                rollTarget = entity.checkWaterMove(waterLevel, overallMoveType);
                if (rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
                    doSkillCheckInPlace(entity, rollTarget);
                }
                if (waterLevel > 1) {
                    // Any swarming infantry will be destroyed.
                    drownSwarmer(entity, curPos);
                }
            }

            // check for building collapse
            Building bldg = game.getBoard().getBuildingAt(curPos);
            if (bldg != null) {
                checkForCollapse(bldg, game.getPositionMap(), curPos);
            }

            // check for breaking magma crust
            if (curHex.terrainLevel(Terrains.MAGMA) == 1) {
                int roll = Compute.d6(1);
                r = new Report(2395);
                r.addDesc(entity);
                r.add(roll);
                r.subject = entity.getId();
                addReport(r);
                if (roll == 6) {
                    curHex.removeTerrain(Terrains.MAGMA);
                    curHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.MAGMA, 2));
                    sendChangedHex(curPos);
                    for (Enumeration<Entity> e = game.getEntities(curPos); e.hasMoreElements();) {
                        Entity en = e.nextElement();
                        if (en != entity)
                            doMagmaDamage(en, false);
                    }
                }
            }

            // check for entering liquid magma
            if (curHex.terrainLevel(Terrains.MAGMA) == 2) {
                doMagmaDamage(entity, false);
            }

            // jumped into swamp? maybe stuck!
            if (curHex.getBogDownModifier(entity.getMovementMode(), entity instanceof LargeSupportTank) != TargetRoll.AUTOMATIC_SUCCESS) {
                if (entity instanceof Mech) {
                    entity.setStuck(true);
                    r = new Report(2121);
                    r.add(entity.getDisplayName(), true);
                    r.subject = entity.getId();
                    addReport(r);
                    //check for quicksand
                    addReport(checkQuickSand(curPos));
                } else {
                    PilotingRollData roll = new PilotingRollData(entity.getId(), 5, "entering boggy terrain");             
                    roll.append(new PilotingRollData(entity.getId(), curHex.getBogDownModifier(entity.getMovementMode(), entity instanceof LargeSupportTank), "avoid bogging down"));
                    if (0 < doSkillCheckWhileMoving(entity, curPos, curPos, roll, false)) {
                        entity.setStuck(true);
                        r = new Report(2081);
                        r.add(entity.getDisplayName());
                        r.subject = entity.getId();
                        addReport(r);
                        //check for quicksand
                        addReport(checkQuickSand(curPos));
                    }
                }
            }

            // If the entity is being swarmed, jumping may dislodge the fleas.
            if (Entity.NONE != swarmerId) {
                final Entity swarmer = game.getEntity(swarmerId);
                final PilotingRollData roll = entity.getBasePilotingRoll(overallMoveType);

                entity.addPilotingModifierForTerrain(roll);

                // Add a +4 modifier.
                roll.addModifier(4, "dislodge swarming infantry");

                // If the swarmer has Assault claws, give a 1 modifier.
                // We can stop looking when we find our first match.
                for (Mounted mount : swarmer.getMisc()) {
                    EquipmentType equip = mount.getType();
                    if (BattleArmor.ASSAULT_CLAW.equals(equip.getInternalName())) {
                        roll.addModifier(1, "swarmer has assault claws");
                        break;
                    }
                }

                // okay, print the info
                r = new Report(2125);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);

                // roll
                final int diceRoll = Compute.d6(2);
                r = new Report(2130);
                r.subject = entity.getId();
                r.add(roll.getValueAsString());
                r.add(roll.getDesc());
                r.add(diceRoll);
                if (diceRoll < roll.getValue()) {
                    r.choose(false);
                    addReport(r);
                } else {
                    // Dislodged swarmers don't get turns.
                    game.removeTurnFor(swarmer);
                    send(createTurnVectorPacket());

                    // Update the report and the swarmer's status.
                    r.choose(true);
                    addReport(r);
                    entity.setSwarmAttackerId(Entity.NONE);
                    swarmer.setSwarmTargetId(Entity.NONE);

                    // Did the infantry fall into water?
                    if (curHex.terrainLevel(Terrains.WATER) > 0) {
                        // Swarming infantry die.
                        swarmer.setPosition(curPos);
                        r = new Report(2135);
                        r.subject = entity.getId();
                        r.indent();
                        r.addDesc(swarmer);
                        addReport(r);
                        addReport(destroyEntity(swarmer, "a watery grave", false));
                    } else {
                        // Swarming infantry take a 3d6 point hit.
                        // ASSUMPTION : damage should not be doubled.
                        r = new Report(2140);
                        r.subject = entity.getId();
                        r.indent();
                        r.addDesc(swarmer);
                        r.add("3d6");
                        addReport(r);
                        addReport(damageEntity(swarmer, swarmer.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Compute.d6(3)));
                        addNewLines();
                        swarmer.setPosition(curPos);
                    }
                    entityUpdate(swarmerId);
                } // End successful-PSR

            } // End try-to-dislodge-swarmers

            // one more check for inferno wash-off
            checkForWashedInfernos(entity, curPos);

        } // End entity-is-jumping
        // update entity's locations' exposure
        doSetLocationsExposure(entity, game.getBoard().getHex(curPos), false, entity.getElevation());

        // Check the falls_end_movement option to see if it should be able to
        // move on.
        if (!(game.getOptions().booleanOption("falls_end_movement") && entity instanceof Mech) && fellDuringMovement && !turnOver && entity.mpUsed < entity.getRunMP() && entity.isSelectableThisTurn() && !entity.isDoomed()) {
            entity.applyDamage();
            entity.setDone(false);
            GameTurn newTurn = new GameTurn.SpecificEntityTurn(entity.getOwner().getId(), entity.getId());
            game.insertNextTurn(newTurn);
            // brief everybody on the turn update
            send(createTurnVectorPacket());
            // let everyone know about what just happened
            send(entity.getOwner().getId(), createSpecialReportPacket());
        } else {
            if (entity.getMovementMode() == IEntityMovementMode.WIGE && entity.getElevation() > 0) {
                if (!wigeStartedLanded && entity.delta_distance < 5) {
                    // try to land safely
                    r = new Report(2123);
                    r.addDesc(entity);
                    r.subject = entity.getId();
                    vPhaseReport.add(r);
                    // when no clear or pavement, crash
                    IHex hex = game.getBoard().getHex(curPos);
                    if (!hex.hasPavement() && hex.terrainsPresent() > 0) {
                        // crash
                        r = new Report(2124);
                        r.addDesc(entity);
                        r.subject = entity.getId();
                        vPhaseReport.add(r);
                        vPhaseReport.addAll(crashVTOLorWiGE((Tank) entity));
                    } else {
                        entity.setElevation(0);
                    }
                } else {
                    // we didn't land, so we go to elevation 1
                    // it might have been higher than one due to the extra MPs
                    // it can spend to stay higher during movement, but should
                    // end up at one
                    entity.setElevation(1);
                }
            }
            entity.setDone(true);
        }

        // If the entity is being swarmed, update the attacker's position.
        if (Entity.NONE != swarmerId) {
            final Entity swarmer = game.getEntity(swarmerId);
            swarmer.setPosition(curPos);
            // If the hex is on fire, and the swarming infantry is
            // *not* Battle Armor, it drops off.
            if (!(swarmer instanceof BattleArmor) && game.getBoard().getHex(curPos).containsTerrain(Terrains.FIRE)) {
                swarmer.setSwarmTargetId(Entity.NONE);
                entity.setSwarmAttackerId(Entity.NONE);
                r = new Report(2145);
                r.subject = entity.getId();
                r.indent();
                r.add(swarmer.getShortName(), true);
                addReport(r);
            }
            entityUpdate(swarmerId);
        }

        // Update the entitiy's position,
        // unless it is off the game map.
        if (!game.isOutOfGame(entity)) {
            entityUpdate(entity.getId(), movePath);
            if (entity.isDoomed()) {
                send(createRemoveEntityPacket(entity.getId(), entity.getRemovalCondition()));
            }
        }

        // recovered units should now be recovered and dealt with
        if (entity instanceof Aero && recovered && loader != null) {
            // Load the unit.
            loader.recover(entity);

            // start the recovery timer
            entity.setRecoveryTurn(5);

            // The loaded unit is being carried by the loader.
            entity.setTransportId(loader.getId());

            // Remove the loaded unit from the screen.
            entity.setPosition(null);

            // Update the loaded unit.
            entityUpdate(entity.getId());
        }

        // even if load was unsuccessful, I may need to update the loader
        if (null != loader) {
            entityUpdate(loader.getId());
        }

        // if using double blind, update the player on new units he might see
        if (doBlind()) {
            send(entity.getOwner().getId(), createFilteredEntitiesPacket(entity.getOwner()));
        }

        // if we generated a charge attack, report it now
        if (charge != null) {
            send(createAttackPacket(charge, 1));
        }

        // if we generated a ram attack, report it now
        if (ram != null) {
            send(createAttackPacket(ram, 1));
        }
    }

    /**
     * Delivers a thunder-aug shot to the targetted hex area. Thunder-Augs are 7
     * hexes, though, so...
     */
    public void deliverThunderAugMinefield(Coords coords, int playerId, int damage, int entityId) {
        Coords mfCoord = null;
        //divide damage in half
        damage = damage / 2 + damage % 2;
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));       
        for (int dir = 0; dir < 7; dir++) {
            switch (dir) {
            case 6:
                // The targeted hex.
                mfCoord = new Coords(coords);
                break;
            default:
                // The hex in the dir direction from the targeted hex.
                mfCoord = coords.translated(dir);
                break;
            }

            // Only if this is on the board...
            if (game.getBoard().contains(mfCoord)) {
                Minefield minefield = null;
                Enumeration<Minefield> minefields = game.getMinefields(mfCoord).elements();
                // Check if there already are Thunder minefields in the hex.
                while (minefields.hasMoreElements()) {
                    Minefield mf = minefields.nextElement();
                    if (mf.getType() == Minefield.TYPE_CONVENTIONAL) {
                        minefield = mf;
                        break;
                    }
                }

                // Did we find a Thunder minefield in the hex?
                // N.B. damage Thunder minefields equals the number of
                // missiles, divided by two, rounded up.
                if (minefield == null) {
                    // Nope. Create a new Thunder minefield
                    minefield = Minefield.createMinefield(mfCoord, playerId, Minefield.TYPE_CONVENTIONAL, damage);
                    game.addMinefield(minefield);
                    checkForRevealMinefield(minefield, game.getEntity(entityId));
                } else if (minefield.getDensity() < Minefield.MAX_DAMAGE) {
                    // Yup. Replace the old one.
                    removeMinefield(minefield);
                    int newDamage = damage / 2 + damage % 2;
                    newDamage += minefield.getDensity();

                    // Damage from Thunder minefields are capped.
                    if (newDamage > Minefield.MAX_DAMAGE) {
                        newDamage = Minefield.MAX_DAMAGE;
                    }
                    minefield.setDensity(newDamage);
                    game.addMinefield(minefield);
                    checkForRevealMinefield(minefield, game.getEntity(entityId));
                }
            } // End coords-on-board

        } // Handle the next coords

    }

    /**
     * Adds a Thunder minefield to the hex.
     * 
     * @param coords
     * @param playerId
     * @param damage
     */
    public void deliverThunderMinefield(Coords coords, int playerId, int damage, int entityId) {
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));  
        Minefield minefield = null;
        Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
        // Check if there already are Thunder minefields in the hex.
        while (minefields.hasMoreElements()) {
            Minefield mf = minefields.nextElement();
            if (mf.getType() == Minefield.TYPE_CONVENTIONAL) {
                minefield = mf;
                break;
            }
        }

        // Create a new Thunder minefield
        if (minefield == null) {
            minefield = Minefield.createMinefield(coords, playerId, Minefield.TYPE_CONVENTIONAL, damage);
            // Add to the old one
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        } else if (minefield.getDensity() < Minefield.MAX_DAMAGE) {
            removeMinefield(minefield);
            int oldDamage = minefield.getDensity();
            damage += oldDamage;
            damage = damage > Minefield.MAX_DAMAGE ? Minefield.MAX_DAMAGE : damage;
            minefield.setDensity(damage);
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        }
    }

    /**
     * Adds a Thunder Inferno minefield to the hex.
     * 
     * @param coords
     * @param playerId
     * @param damage
     */
    public void deliverThunderInfernoMinefield(Coords coords, int playerId, int damage, int entityId) {
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));      
        Minefield minefield = null;
        Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
        // Check if there already are Thunder minefields in the hex.
        while (minefields.hasMoreElements()) {
            Minefield mf = minefields.nextElement();
            if (mf.getType() == Minefield.TYPE_INFERNO) {
                minefield = mf;
                break;
            }
        }

        // Create a new Thunder Inferno minefield
        if (minefield == null) {
            minefield = Minefield.createMinefield(coords, playerId, Minefield.TYPE_INFERNO, damage);
            // Add to the old one
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        } else if (minefield.getDensity() < Minefield.MAX_DAMAGE) {
            removeMinefield(minefield);
            int oldDamage = minefield.getDensity();
            damage += oldDamage;
            damage = damage > Minefield.MAX_DAMAGE ? Minefield.MAX_DAMAGE : damage;
            minefield.setDensity(damage);
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        }
    }

    /**
     * Delivers a Arrow IV FASCAM shot to the targetted hex area.
     */
    public void deliverFASCAMMinefield(Coords coords, int playerId, int damage, int entityId) {
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));  
        // Only if this is on the board...
        if (game.getBoard().contains(coords)) {
            Minefield minefield = null;
            Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
            // Check if there already are Thunder minefields in the hex.
            while (minefields.hasMoreElements()) {
                Minefield mf = minefields.nextElement();
                if (mf.getType() == Minefield.TYPE_CONVENTIONAL) {
                    minefield = mf;
                    break;
                }
            }
            // Did we find a Thunder minefield in the hex?
            // N.B. damage of FASCAM minefields is 30
            if (minefield == null)
                minefield = Minefield.createMinefield(coords, playerId, Minefield.TYPE_CONVENTIONAL, 30);
            removeMinefield(minefield);
            minefield.setDensity(30);
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        } // End coords-on-board
    }

    /**
     * Adds a Thunder-Active minefield to the hex.
     */
    public void deliverThunderActiveMinefield(Coords coords, int playerId, int damage, int entityId) {
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));  
        Minefield minefield = null;
        Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
        // Check if there already are Thunder minefields in the hex.
        while (minefields.hasMoreElements()) {
            Minefield mf = minefields.nextElement();
            if (mf.getType() == Minefield.TYPE_ACTIVE) {
                minefield = mf;
                break;
            }
        }

        // Create a new Thunder-Active minefield
        if (minefield == null) {
            minefield = Minefield.createMinefield(coords, playerId, Minefield.TYPE_ACTIVE, damage);
            // Add to the old one
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        } else if (minefield.getDensity() < Minefield.MAX_DAMAGE) {
            removeMinefield(minefield);
            int oldDamage = minefield.getDensity();
            damage += oldDamage;
            damage = damage > Minefield.MAX_DAMAGE ? Minefield.MAX_DAMAGE : damage;
            minefield.setDensity(damage);
            game.addMinefield(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        }
    }

    /**
     * Adds a Thunder-Vibrabomb minefield to the hex.
     */
    public void deliverThunderVibraMinefield(Coords coords, int playerId, int damage, int sensitivity, int entityId) {
        //round up damage to next highest increment of five
        damage = (int)(5 * Math.ceil(damage / 5.0));  
        Minefield minefield = null;
        Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
        // Check if there already are Thunder minefields in the hex.
        while (minefields.hasMoreElements()) {
            Minefield mf = minefields.nextElement();
            if (mf.getType() == Minefield.TYPE_VIBRABOMB) {
                minefield = mf;
                break;
            }
        }

        // Create a new Thunder-Vibra minefield
        if (minefield == null) {
            minefield = Minefield.createMinefield(coords, playerId, Minefield.TYPE_VIBRABOMB, damage, sensitivity);
            // Add to the old one
            game.addVibrabomb(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        } else if (minefield.getDensity() < Minefield.MAX_DAMAGE) {
            removeMinefield(minefield);
            int oldDamage = minefield.getDensity();
            damage += oldDamage;
            damage = damage > Minefield.MAX_DAMAGE ? Minefield.MAX_DAMAGE : damage;
            minefield.setDensity(damage);
            game.addVibrabomb(minefield);
            checkForRevealMinefield(minefield, game.getEntity(entityId));
        }
    }

    /**
     * Creates an artillery flare of the given radius above the target
     */
    public void deliverArtilleryFlare(Coords coords, int radius) {
        Flare flare = new Flare(coords, 5, radius, Flare.F_DRIFTING);
        game.addFlare(flare);
    }

    /**
     * deliver artillery smoke
     * 
     * @param coords
     *            the <code>Coords</code> where to deliver
     */
    public void deliverArtillerySmoke(Coords coords, Vector<Report> vPhaseReport) {
        IHex h = game.getBoard().getHex(coords);
        // Unless there is a heavy smoke in the hex already, add one.
        if (h.terrainLevel(Terrains.SMOKE) < 2) {
            Report r = new Report(5185, Report.PUBLIC);
            r.indent(2);
            r.add(coords.getBoardNum());
            vPhaseReport.add(r);
            createSmoke(coords);
            sendChangedHex(coords);
        }
    }

    /**
     * deliver artillery inferno
     * 
     * @param coords
     *            the <code>Coords</code> where to deliver
     * @param ae 
     *           the attacking <code>entity<code>
     * @param subjectId
     *            the <code>int</code> id of the target
     */
    public void deliverArtilleryInferno(Coords coords, Entity ae, int subjectId, Vector<Report> vPhaseReport) {
        IHex h = game.getBoard().getHex(coords);
        Report r;
        // Unless there is a fire in the hex already, start one.
        if (!h.containsTerrain(Terrains.FIRE)) {
            ignite(coords, true, vPhaseReport);
        }
        //possibly melt ice and snow
        if(h.containsTerrain(Terrains.ICE) || h.containsTerrain(Terrains.SNOW)) {
            vPhaseReport.addAll(meltIceAndSnow(coords, subjectId));
        }
        for (Enumeration<Entity> impactHexHits = game.getEntities(coords); impactHexHits.hasMoreElements();) {
            Entity entity = impactHexHits.nextElement();
            //TacOps, p. 356 - treat as if hit by 5 inferno missiles
            r = new Report(6695);
            r.indent(3);
            r.add(entity.getDisplayName());
            r.newlines = 0;
            vPhaseReport.add(r);
            if(entity instanceof Tank) {
                Report.addNewline(vPhaseReport);
            }
            Vector<Report> vDamageReport = deliverInfernoMissiles(ae, entity, 5);
            Report.indentAll(vDamageReport, 2);
            vPhaseReport.addAll(vDamageReport);
        }
        for (int dir = 0; dir <= 5; dir++) {
            Coords tempcoords = coords.translated(dir);
            if (!game.getBoard().contains(tempcoords)) {
                continue;
            }
            if (coords.equals(tempcoords)) {
                continue;
            }
            h = game.getBoard().getHex(tempcoords);
            // Unless there is a fire in the hex already, start one.
            if (!h.containsTerrain(Terrains.FIRE)) {
                ignite(tempcoords, true, vPhaseReport);
            }
            //possibly melt ice and snow
            if(h.containsTerrain(Terrains.ICE) || h.containsTerrain(Terrains.SNOW)) {
                vPhaseReport.addAll(meltIceAndSnow(tempcoords, subjectId));
            }
            for (Enumeration<Entity> splashHexHits = game.getEntities(tempcoords); splashHexHits.hasMoreElements();) {
                Entity entity = splashHexHits.nextElement();
                r = new Report(6695);
                r.indent(3);
                r.add(entity.getDisplayName());
                r.newlines = 0;
                vPhaseReport.add(r);
                if(entity instanceof Tank) {
                    Report.addNewline(vPhaseReport);
                }
                Vector<Report> vDamageReport = deliverInfernoMissiles(ae, entity, 5);
                Report.indentAll(vDamageReport, 2);
                vPhaseReport.addAll(vDamageReport);
            }
        }
    }

    public void deliverScreen(Coords coords, Vector<Report> vPhaseReport) {
        IHex h = game.getBoard().getHex(coords);
        Report r;
        Report.addNewline(vPhaseReport);
        r = new Report(9070, Report.PUBLIC);
        r.indent(2);
        r.add(coords.getBoardNum());
        vPhaseReport.add(r);
        // use level to count the number of screens (since level does not matter
        // in space)
        int nscreens = h.terrainLevel(Terrains.SCREEN);
        if (nscreens > 0) {
            h.removeTerrain(Terrains.SCREEN);
            h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.SCREEN, nscreens + 1));
        } else {
            h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.SCREEN, 1));
        }
        sendChangedHex(coords);
    }

    /*
     * deploys a new tele-missile entity onto the map
     */
    public void deployTeleMissile(Entity ae, AmmoType atype, int wId, int capMisMod, Vector<Report> vPhaseReport) {
        Report r = new Report(9080);
        r.subject = ae.getId();
        r.addDesc(ae);
        r.indent(2);
        r.newlines = 0;
        r.add(atype.getName());
        vPhaseReport.add(r);
        TeleMissile tele = new TeleMissile(ae, atype.getDamagePerShot(), atype.getTonnage(ae), atype.getAmmoType(), capMisMod);
        tele.setDeployed(true);
        tele.setId(getFreeEntityId());
        if (ae instanceof Aero) {
            Aero a = (Aero) ae;
            tele.setCurrentVelocity(a.getCurrentVelocity());
            tele.setNextVelocity(a.getNextVelocity());
            tele.setVectors(a.getVectors());
            tele.setFacing(a.getFacing());
        }
        // set velocity and heading the same as parent entity
        game.addEntity(tele.getId(), tele);
        send(createAddEntityPacket(tele.getId()));
        // make him not get a move this turn
        tele.setDone(true);
        // place on board
        tele.setPosition(ae.getPosition());
        // Update the entity
        entityUpdate(tele.getId());
        // check to see if the launching of this missile removes control of any
        // prior missiles
        if (ae.getTMTracker().containsLauncher(wId)) {
            Entity priorMissile = game.getEntity(ae.getTMTracker().getMissile(wId));
            if (null != priorMissile && priorMissile instanceof TeleMissile) {
                ((TeleMissile) priorMissile).setOutContact(true);
                // remove this from the tracker for good measure
                ae.getTMTracker().removeMissile(wId);
            }
        }
        // track this missile on the entity
        ae.getTMTracker().addMissile(wId, tele.getId());
    }

    /**
     * deliver inferno missiles
     * 
     * @param ae
     *            the <code>Entity</code> that fired the missiles
     * @param t
     *            the <code>Targetable</code> that is the target
     * @param missiles
     *            the <code>int</code> amount of missiles
     */
    public Vector<Report> deliverInfernoMissiles(Entity ae, Targetable t, int missiles) {
        IHex hex = game.getBoard().getHex(t.getPosition());
        Report r;
        Vector<Report> vPhaseReport = new Vector<Report>();
        switch (t.getTargetType()) {
        case Targetable.TYPE_HEX_ARTILLERY:
            // used for BA inferno explosion
            for (Enumeration<Entity> entities = game.getEntities(t.getPosition()); entities.hasMoreElements();) {
                Entity e = entities.nextElement();
                if (e.getElevation() > hex.terrainLevel(Terrains.BLDG_ELEV)) {
                    r = new Report(6685);
                    r.subject = e.getId();
                    r.addDesc(e);
                    vPhaseReport.add(r);
                    vPhaseReport.addAll(deliverInfernoMissiles(ae, e, missiles));
                } else {
                    int roll = Compute.d6();
                    r = new Report(3570);
                    r.subject = e.getId();
                    r.addDesc(e);
                    r.add(roll);
                    vPhaseReport.add(r);
                    if (roll >= 5) {
                        vPhaseReport.addAll(deliverInfernoMissiles(ae, e, missiles));
                    }
                }
            }
            if (game.getBoard().getBuildingAt(t.getPosition()) != null) {
                Vector<Report> vBuildingReport = damageBuilding(game.getBoard().getBuildingAt(t.getPosition()), 2 * missiles, t.getPosition());
                for (Report report : vBuildingReport) {
                    report.subject = ae.getId();
                }
                vPhaseReport.addAll(vBuildingReport);
            }
            // fall through
        case Targetable.TYPE_HEX_CLEAR:
        case Targetable.TYPE_HEX_IGNITE:
            vPhaseReport.addAll(tryClearHex(t.getPosition(), missiles * 4, ae.getId()));
            tryIgniteHex(t.getPosition(), ae.getId(), false, true, new TargetRoll(0, "inferno"), -1, vPhaseReport);
            break;
        case Targetable.TYPE_BLDG_IGNITE:
        case Targetable.TYPE_BUILDING:
            for (Enumeration<Entity> entities = game.getEntities(t.getPosition()); entities.hasMoreElements();) {
                Entity e = entities.nextElement();
                if (e.getElevation() > hex.terrainLevel(Terrains.BLDG_ELEV))
                    continue;
                int roll = Compute.d6();
                r = new Report(3570);
                r.subject = e.getId();
                r.addDesc(e);
                r.add(roll);
                vPhaseReport.add(r);
                if (roll >= 5) {
                    vPhaseReport.addAll(deliverInfernoMissiles(ae, e, missiles));
                }
            }
            Vector<Report> vBuildingReport = damageBuilding(game.getBoard().getBuildingAt(t.getPosition()), 2 * missiles, t.getPosition());
            for (Report report : vBuildingReport) {
                report.subject = ae.getId();
            }
            vPhaseReport.addAll(vBuildingReport);
            break;
        case Targetable.TYPE_ENTITY:
            Entity te = (Entity) t;
            if (te instanceof Mech) {
                // Bug #1585497: Check for partial cover
                int m = missiles;
                for (int i = 0; i < m; i++) {
                    int roll = Compute.d6(2);
                    LosEffects le = LosEffects.calculateLos(game, ae.getId(), t);
                    if (te.removePartialCoverHits(roll, le.getTargetCover(), Compute.targetSideTable(ae, t))) {
                        missiles--;
                    }
                }
                if (missiles != m) {
                    r = new Report(3403);
                    r.add(m - missiles);
                    r.indent(2);
                    r.subject = te.getId();
                    vPhaseReport.add(r);
                }
                r = new Report(3400);
                r.add(2 * missiles);
                r.subject = te.getId();
                r.indent(2);
                r.choose(true);
                vPhaseReport.add(r);
                te.heatFromExternal += 2 * missiles;
            } else if (te instanceof Tank) {
                int direction = Compute.targetSideTable(ae, te);
                while (missiles-- > 0) {
                    HitData hit = te.rollHitLocation(ToHitData.HIT_NORMAL, direction);
                    vPhaseReport.addAll(criticalEntity(te, hit.getLocation(), -2));
                }
            } else if (te instanceof Protomech) {
                te.heatFromExternal += missiles;
                while (te.heatFromExternal >= 3) {
                    te.heatFromExternal -= 3;
                    HitData hit = te.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    if (hit.getLocation() == Protomech.LOC_NMISS) {
                        r = new Report(6035);
                        r.subject = te.getId();
                        r.newlines = 0;
                        vPhaseReport.add(r);
                    } else {
                        r = new Report(6690);
                        r.subject = te.getId();
                        r.indent(2);
                        r.add(te.getLocationName(hit));
                        vPhaseReport.add(r);
                        te.destroyLocation(hit.getLocation());
                        // Handle Protomech pilot damage
                        // due to location destruction
                        int hits = Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()] - ((Protomech) te).getPilotDamageTaken(hit.getLocation());
                        if (hits > 0) {
                            vPhaseReport.addAll(damageCrew(te, hits));
                            ((Protomech) te).setPilotDamageTaken(hit.getLocation(), Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()]);
                        }
                        if (te.getTransferLocation(hit).getLocation() == Entity.LOC_DESTROYED) {
                            vPhaseReport.addAll(destroyEntity(te, "flaming inferno death", false, true));
                            Report.addNewline(vPhaseReport);
                        }
                    }
                }
            } else if (te instanceof BattleArmor) {
                for (Mounted equip : te.getMisc()) {
                    if (equip.getType().hasFlag(MiscType.F_FIRE_RESISTANT)) {
                        r = new Report(3395);
                        r.indent(2);
                        r.subject = te.getId();
                        r.addDesc(te);
                        vPhaseReport.add(r);
                        return vPhaseReport;
                    }
                }
                te.heatFromExternal += missiles;
                while (te.heatFromExternal >= 3) {
                    te.heatFromExternal -= 3;
                    HitData hit = te.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    hit.setEffect(HitData.EFFECT_CRITICAL);
                    vPhaseReport.addAll(damageEntity(te, hit, 1));
                    Report.addNewline(vPhaseReport);
                }
            } else if (te instanceof Infantry) {
                HitData hit = new HitData(Infantry.LOC_INFANTRY);
                if (te.getInternal(hit) > 3 * missiles) {
                    // internal structure absorbs all damage
                    te.setInternal(te.getInternal(hit) - 3 * missiles, hit);
                    r = new Report(6065);
                    r.addDesc(te);
                    r.add(3 * missiles);
                    r.indent(2);
                    r.add(te.getLocationAbbr(hit));
                    r.newlines = 0;
                    r.subject = te.getId();
                    vPhaseReport.add(r);
                    r = new Report(6095);
                    r.add(te.getInternal(hit));
                    r.subject = te.getId();
                    r.indent(2);
                    vPhaseReport.add(r);
                } else {
                    vPhaseReport.addAll(destroyEntity(te, "damage", false));
                    Report.addNewline(vPhaseReport);
                }
            } else {
                // gun emplacements
                int direction = Compute.targetSideTable(ae, te);
                while (missiles-- > 0) {
                    HitData hit = te.rollHitLocation(ToHitData.HIT_NORMAL, direction);
                    vPhaseReport.addAll(damageEntity(te, hit, 2));
                }
            }
        }
        return vPhaseReport;
    }

    /**
     * Check for any detonations when an entity enters a minefield, except a vibrabomb.
     * 
     * @param entity - the <code>entity</code> who entered the minefield
     * @param c - the <code>Coords</code> of the minefield
     * @param curElev - an <code>int</code> for the elevation of the entity entering the minefield (used for underwater sea mines)
     * @param isOnGround - <code>true</code> if the entity is not in the middle of a jump
     * @param vMineReport - the report vector that reports will be added to
     * @return - <code>true</code> if the entity set off any mines
     */
    private boolean enterMinefield(Entity entity, Coords c, int curElev, boolean isOnGround, Vector<Report> vMineReport) {
        return enterMinefield(entity, c, curElev, isOnGround, vMineReport, -1);
    }
    
    /**
     * Check for any detonations when an entity enters a minefield, except a vibrabomb.
     * 
     * @param entity - the <code>entity</code> who entered the minefield
     * @param c - the <code>Coords</code> of the minefield
     * @param curElev - an <code>int</code> for the elevation of the entity entering the minefield (used for underwater sea mines)
     * @param isOnGround - <code>true</code> if the entity is not in the middle of a jump
     * @param vMineReport - the report vector that reports will be added to
     * @param target - the <code>int</code> target number for detonation. If this will be determined by density, it should be -1
     * @return - <code>true</code> if the entity set off any mines
     */
    private boolean enterMinefield(Entity entity, Coords c, int curElev, boolean isOnGround, Vector<Report> vMineReport, int target) {
        Report r;
        boolean trippedMine = false;
        //flying units cannot trip a mine
        if(curElev > 0) {
            return false;
        }
        
        //loop through mines in this hex
        for(Minefield mf : game.getMinefields(c)) {
            
            //vibrabombs are handled differently
            if(mf.getType() == Minefield.TYPE_VIBRABOMB) {
                continue;
            }
            
            //if we are in the water, then the sea mine will only blow up if at the right depth
            if(game.getBoard().getHex(mf.getCoords()).containsTerrain(Terrains.WATER)) {
                if(Math.abs(curElev) != mf.getDepth() 
                        && Math.abs(curElev + entity.getHeight()) != mf.getDepth()) {
                    continue;
                }
            }            
            //check whether we have an active mine
            if(mf.getType() == Minefield.TYPE_ACTIVE && isOnGround) {
                continue;
            }
            if(mf.getType() != Minefield.TYPE_ACTIVE && !isOnGround) {
                continue;
            }
            
            //set the target number
            if(target == -1) {
                target = mf.getTrigger();
                if(mf.getType() == Minefield.TYPE_ACTIVE) {
                    target = 9;
                }
                if(entity instanceof Infantry) {
                    target += 1;
                }
                if(entity.getMovementMode() == IEntityMovementMode.HOVER || entity.getMovementMode() == IEntityMovementMode.WIGE) {
                    target = 12;
                }
            }
            
            if (Compute.d6(2) < target) {
                continue;
            }
            
            //apply damage
            trippedMine = true;
            //explodedMines.add(mf);
            mf.setDetonated(true);
            if(mf.getType() == Minefield.TYPE_INFERNO) {
                //report hitting an inferno mine
                r = new Report(2155);
                r.subject = entity.getId();
                r.add(entity.getShortName(), true);
                r.add(mf.getCoords().getBoardNum(), true);
                vMineReport.add(r);
                vMineReport.addAll(deliverInfernoMissiles(entity, entity, mf.getDensity() / 2));
            } else {                   
                r = new Report(2150);
                r.subject = entity.getId();
                r.add(entity.getShortName(), true);
                r.add(mf.getCoords().getBoardNum(), true);
                vMineReport.add(r);
                int damage = mf.getDensity();
                while(damage > 0) {
                    int cur_damage = Math.min(5, damage);
                    damage = damage - cur_damage;
                    HitData hit = entity.rollHitLocation(Minefield.TO_HIT_TABLE, Minefield.TO_HIT_SIDE);
                    vMineReport.addAll(damageEntity(entity, hit, cur_damage));
                }
                if(entity instanceof Tank) {
                    vMineReport.addAll(vehicleMotiveDamage((Tank)entity, 2));
                }
            }
            
            //check the direct reduction
            mf.checkReduction(0, true);
            revealMinefield(mf);
        }
        
        return trippedMine;
    }
    
    /**
     * cycle through all mines on the board, check to see whether they should do collateral damage
     * to other mines due to detonation, resets detonation to false, and removes any mines whose density has
     * been reduced to zero.
     */
    private void resetMines() {
        
        Enumeration<Coords> mineLoc = game.getMinedCoords();
        while(mineLoc.hasMoreElements()) {
            Coords c = mineLoc.nextElement();
            Enumeration<Minefield> minefields = game.getMinefields(c).elements();
            while (minefields.hasMoreElements()) {
                Minefield minefield = minefields.nextElement();
                if(minefield.hasDetonated()) {
                    minefield.setDetonated(false);
                    Enumeration<Minefield> otherMines = game.getMinefields(c).elements();
                    while (otherMines.hasMoreElements()) {
                        Minefield otherMine = otherMines.nextElement();
                        if(otherMine.equals(minefield))
                            continue;
                        int bonus = 0;
                        if(otherMine.getDensity() > minefield.getDensity()) {
                            bonus = 1;
                        }
                        if(otherMine.getDensity() < minefield.getDensity()) {
                            bonus = -1;
                        }
                        otherMine.checkReduction(bonus, false);
                    }
                }
            }
            //cycle through a second time to see if any mines at these coords need to be removed
            ArrayList<Minefield> mfRemoved = new ArrayList<Minefield>();
            Enumeration<Minefield> mines = game.getMinefields(c).elements();
            while (mines.hasMoreElements()) {
                Minefield mine = mines.nextElement();
                if(mine.getDensity() < 5)
                    mfRemoved.add(mine);
            }
            //we have to do it this way to avoid a concurrent error problem
            for(Minefield mf : mfRemoved) {
                removeMinefield(mf);
            }
            //update the mines at these coords
            sendChangedMines(c);
        }      
    }
    
    /**
     * attempt to clear a minefield
     * @param mf - a <code>Minefield</code> to clear
     * @param en - <code>entity</code> doing the clearing
     * @param target - <code>int</code> needed to roll for a successful clearance
     * @return <code>true</code> if clearance successful
     */
    public boolean clearMinefield(Minefield mf, Entity en, int target, Vector<Report> vClearReport) {
        return clearMinefield(mf, en, target, -1, vClearReport);
    }
    
    /**
     * attempt to clear a minefield
     * We don't actually remove the minefield here, because if this is called up from within
     * a loop, that will cause problems
     * @param mf - a <code>Minefield</code> to clear
     * @param en - <code>entity</code> doing the clearing
     * @param target - <code>int</code> needed to roll for a successful clearance
     * @param botch - <code>int</code> that indicates an accidental detonation
     * @return <code>true</code> if clearance successful
     */
    public boolean clearMinefield(Minefield mf, Entity en, int target, int botch, Vector<Report> vClearReport) {
        Report r;
        int roll = Compute.d6(2);
        if(roll >= target) {
            r = new Report(2250);
            r.subject = en.getId();
            r.add(Minefield.getDisplayableName(mf.getType()));
            r.indent(1);
            vClearReport.add(r);
            return true;
        } 
        else if(roll <= botch) {
            //TODO: detonate the minefield
            r = new Report(2255);
            r.subject = en.getId();
            r.indent(1);
            r.add(Minefield.getDisplayableName(mf.getType()));
            vClearReport.add(r);
            //The detonation damages any units that were also attempting to 
            //clear mines in the same hex
            for (Enumeration<Entity> e = game.getEntities(mf.getCoords()); e.hasMoreElements();) {
                Entity victim = e.nextElement();
                Report rVictim;
                if(victim.isClearingMinefield()) {
                    rVictim = new Report(2265);
                    rVictim.subject = victim.getId();
                    rVictim.add(victim.getShortName(), true);
                    rVictim.indent(2);
                    vClearReport.add(rVictim);
                    int damage = mf.getDensity();
                    while(damage > 0) {
                        int cur_damage = Math.min(5, damage);
                        damage = damage - cur_damage;
                        HitData hit = victim.rollHitLocation(Minefield.TO_HIT_TABLE, Minefield.TO_HIT_SIDE);
                        vClearReport.addAll(damageEntity(victim, hit, cur_damage));
                    }
                }
            }
            //reduction works differently here
            if(mf.getType() == Minefield.TYPE_CONVENTIONAL) {
                mf.setDensity(Math.max(5, mf.getDensity() - 5));
            } else {
                //congratulations, you cleared the mine by blowing yourself up
                return true;
            }
        } else {
            // failure
            r = new Report(2260);
            r.subject = en.getId();
            r.indent(1);
            r.add(Minefield.getDisplayableName(mf.getType()));
            vClearReport.add(r);
        }
        return false;
    }
    
    /**
     * Clear any detonated mines at these coords
     */
    private void clearDetonatedMines(Coords c, int target) {
        
        Enumeration<Minefield> minefields = game.getMinefields(c).elements();
        ArrayList<Minefield> mfRemoved = new ArrayList<Minefield>();
        while (minefields.hasMoreElements()) {
            Minefield minefield = minefields.nextElement();
            if(minefield.hasDetonated() && Compute.d6(2) >= target) {
                mfRemoved.add(minefield);
            }
        }
        //we have to do it this way to avoid a concurrent error problem
        for(Minefield mf : mfRemoved) {
            removeMinefield(mf);
        }
    }
    
    
    /**
     * Checks to see if an entity sets off any vibrabombs.
     */
    private boolean checkVibrabombs(Entity entity, Coords coords, boolean displaced, Vector<Report> vMineReport) {
        return checkVibrabombs(entity, coords, displaced, null, null, vMineReport);
    }

    /**
     * Checks to see if an entity sets off any vibrabombs.
     */
    private boolean checkVibrabombs(Entity entity, Coords coords, boolean displaced, Coords lastPos, Coords curPos, Vector<Report> vMineReport) {
        
        boolean boom = false;
        // Only mechs can set off vibrabombs.
        if (!(entity instanceof Mech)) {
            return boom;
        }

        int mass = (int) entity.getWeight();

        Enumeration<Minefield> e = game.getVibrabombs().elements();

        while (e.hasMoreElements()) {
            Minefield mf = e.nextElement();
            
            // Bug 954272: Mines shouldn't work underwater, and BMRr says
            // Vibrabombs are mines
            if (game.getBoard().getHex(mf.getCoords()).containsTerrain(Terrains.WATER) && !game.getBoard().getHex(mf.getCoords()).containsTerrain(Terrains.PAVEMENT) && !game.getBoard().getHex(mf.getCoords()).containsTerrain(Terrains.ICE)) {
                continue;
            }

            // Mech weighing 10 tons or less can't set off the bomb
            if (mass <= mf.getSetting() - 10) {
                continue;
            }

            int effectiveDistance = (mass - mf.getSetting()) / 10;
            int actualDistance = coords.distance(mf.getCoords());

            if (actualDistance <= effectiveDistance) {
                Report r = new Report(2156);
                r.subject = entity.getId();
                r.add(entity.getShortName(), true);
                r.add(mf.getCoords().getBoardNum(), true);
                vMineReport.add(r);  
                explodeVibrabomb(mf, vMineReport, false);
            }

            // Hack; when moving, the Mech isn't in the hex during
            // the movement.
            if (!displaced && actualDistance == 0) {
                // report getting hit by vibrabomb
                Report r = new Report(2160);
                r.subject = entity.getId();
                r.add(entity.getShortName(), true);
                vMineReport.add(r);
                int damage = mf.getDensity();
                while(damage > 0) {
                    int cur_damage = Math.min(5, damage);
                    damage = damage - cur_damage;
                    HitData hit = entity.rollHitLocation(Minefield.TO_HIT_TABLE, Minefield.TO_HIT_SIDE);
                    vMineReport.addAll(damageEntity(entity, hit, cur_damage));
                }
                vMineReport.addAll(resolvePilotingRolls(entity, true, lastPos, curPos));
                // we need to apply Damage now, in case the entity lost a leg,
                // otherwise it won't get a leg missing mod if it hasn't yet
                // moved and lost a leg, see bug 1071434 for an example
                entity.applyDamage();
            }
            
            //don't check for reduction until the end or units in the same hex through
            //movement will get the reduced damage
            if(mf.hasDetonated()) {
                boom = true;
                mf.checkReduction(0, true);
            }
            
        }
        return boom;
    }

    /**
     * Remove all minefields in the specified coords from the game
     * 
     * @param coords
     *            The <code>Coords</code> from which to remove minefields
     */
    /*
    public void removeMinefieldsFrom(Coords coords) {
        Vector<Minefield> v = game.getMinefields(coords);
        while (v.elements().hasMoreElements()) {
            Minefield mf = v.elements().nextElement();
            removeMinefield(mf);
        }

    }
    */

    /**
     * Removes the minefield from the game.
     * 
     * @param mf
     *            The <code>Minefield</code> to remove
     */
    public void removeMinefield(Minefield mf) {
        if (game.containsVibrabomb(mf)) {
            game.removeVibrabomb(mf);
        }
        game.removeMinefield(mf);

        Enumeration<Player> players = game.getPlayers();
        while (players.hasMoreElements()) {
            Player player = players.nextElement();
            removeMinefield(player, mf);
        }
    }

    /**
     * Removes the minfield from a player.
     * 
     * @param player
     *            The <code>Player</code> who's minefield should be removed
     * @param mf
     *            The <code>Minefield</code> to be removed
     */
    private void removeMinefield(Player player, Minefield mf) {
        if (player.containsMinefield(mf)) {
            player.removeMinefield(mf);
            send(player.getId(), new Packet(Packet.COMMAND_REMOVE_MINEFIELD, mf));
        }
    }

    /**
     * Reveals a minefield for all players.
     * 
     * @param mf
     *            The <code>Minefield</code> to be revealed
     */
    private void revealMinefield(Minefield mf) {
        Enumeration<Team> teams = game.getTeams();
        while (teams.hasMoreElements()) {
            Team team = teams.nextElement();
            revealMinefield(team, mf);
        }
    }

    /**
     * Reveals a minefield for all players on a team.
     * 
     * @param team
     *            The <code>team</code> who's minefiled should be revealed
     * @param mf
     *            The <code>Minefield</code> to be revealed
     */
    private void revealMinefield(Team team, Minefield mf) {
        Enumeration<Player> players = team.getPlayers();
        while (players.hasMoreElements()) {
             Player player = players.nextElement();
             if (!player.containsMinefield(mf)) {
                 player.addMinefield(mf);
                 send(player.getId(), new Packet(Packet.COMMAND_REVEAL_MINEFIELD, mf));
             }
        }
    }

    /**
     * checks whether a newly set mine should be revealed to players based on
     * LOS. If so, then it reveals the mine
     */
    private void checkForRevealMinefield(Minefield mf, Entity layer) {
        Enumeration<Team> teams = game.getTeams();
        //loop through each team and determine if they can see the mine, then loop through players on team
        //and reveal the mine
        while(teams.hasMoreElements()) {
            Team team = teams.nextElement();
            boolean canSee = false;
            
            //the players own team can always see the mine
            if(team.equals(game.getTeamForPlayer(game.getPlayer(mf.getPlayerId())))) {
                canSee = true;
            } else {                
                //need to loop through all entities on this team and find the one with the best shot of seeing
                //the mine placement
                int target = Integer.MAX_VALUE;            
                Enumeration<Entity> entities = game.getEntities();
                    while(entities.hasMoreElements()) {
                        Entity en = entities.nextElement();
                        //are we on the right team?
                        if(!team.equals(game.getTeamForPlayer(en.getOwner()))) {
                            continue;
                        }
                        if(LosEffects.calculateLos(game, en.getId(), 
                                new HexTarget(mf.getCoords(), game.getBoard(), Targetable.TYPE_HEX_CLEAR)).canSee()) {
                            target = 0;
                            break;
                        }
                        LosEffects los = LosEffects.calculateLos(game, en.getId(), layer);
                        if(los.canSee()) {
                            //TODO: need to add mods
                            ToHitData current = new ToHitData(4, "base");
                            current.append(Compute.getAttackerMovementModifier(game, en.getId()));
                            current.append(Compute.getTargetMovementModifier(game, layer.getId()));
                            current.append(los.losModifiers(game));
                            if(current.getValue() < target) {
                                target = current.getValue();
                            }
                        }            
                    }
                    
                    if(Compute.d6(2) >= target) {
                        canSee = true;
                    }
            }               
                if(canSee) {
                    revealMinefield(team, mf);
            }
        }
    }
    
    
    /**
     * Explodes a vibrabomb.
     * 
     * @param mf
     *            The <code>Minefield</code> to explode
     */
    private void explodeVibrabomb(Minefield mf, Vector<Report> vBoomReport, boolean reduce) {
        Enumeration<Entity> targets = game.getEntities(mf.getCoords());
        Report r;

        while (targets.hasMoreElements()) {
            Entity entity = targets.nextElement();

            // check for the "no_premove_vibra" option
            // If it's set, and the target has not yet moved,
            // it doesn't get damaged.
            if (!entity.isDone() && game.getOptions().booleanOption("no_premove_vibra")) {
                r = new Report(2157);
                r.subject = entity.getId();
                r.add(entity.getShortName(), true);
                vBoomReport.add(r);
                continue;
            }
            // report hitting vibrabomb
            r = new Report(2160);
            r.subject = entity.getId();
            r.add(entity.getShortName(), true);
            vBoomReport.add(r);

            int damage = mf.getDensity();
            while(damage > 0) {
                int cur_damage = Math.min(5, damage);
                damage = damage - cur_damage;
                HitData hit = entity.rollHitLocation(Minefield.TO_HIT_TABLE, Minefield.TO_HIT_SIDE);
                vBoomReport.addAll(damageEntity(entity, hit, cur_damage));
            }
            Report.addNewline(vBoomReport);
           
            if(entity instanceof Tank) {
                vBoomReport.addAll(vehicleMotiveDamage((Tank)entity, 2));
            }
            vBoomReport.addAll(resolvePilotingRolls(entity, true, entity.getPosition(), entity.getPosition()));
            // we need to apply Damage now, in case the entity lost a leg,
            // otherwise it won't get a leg missing mod if it hasn't yet
            // moved and lost a leg, see bug 1071434 for an example
            game.resetPSRs(entity);
            entity.applyDamage();
            Report.addNewline(vBoomReport);
            entityUpdate(entity.getId());    
        }
        
        //check the direct reduction of mine
        if(reduce)
            mf.checkReduction(0, true);
        mf.setDetonated(true);
    }

    /**
     * drowns any units swarming the entity
     * 
     * @param entity
     *            The <code>Entity</code> that is being swarmed
     * @param pos
     *            The <code>Coords</code> the entity is at
     */
    private void drownSwarmer(Entity entity, Coords pos) {
        // Any swarming infantry will be destroyed.
        final int swarmerId = entity.getSwarmAttackerId();
        if (Entity.NONE != swarmerId) {
            final Entity swarmer = game.getEntity(swarmerId);
            // Only *platoons* drown while swarming.
            if (!(swarmer instanceof BattleArmor)) {
                swarmer.setSwarmTargetId(Entity.NONE);
                entity.setSwarmAttackerId(Entity.NONE);
                swarmer.setPosition(pos);
                Report r = new Report(2165);
                r.subject = entity.getId();
                r.indent();
                r.add(entity.getShortName(), true);
                addReport(r);
                addReport(destroyEntity(swarmer, "a watery grave", false));
                entityUpdate(swarmerId);
            }
        }
    }

    /**
     * Checks to see if we may have just washed off infernos. Call after a step
     * which may have done this.
     * 
     * @param entity
     *            The <code>Entity</code> that is being checked
     * @param coords
     *            The <code>Coords</code> the entity is at
     */
    void checkForWashedInfernos(Entity entity, Coords coords) {
        IHex hex = game.getBoard().getHex(coords);
        int waterLevel = hex.terrainLevel(Terrains.WATER);
        // Mech on fire with infernos can wash them off.
        if (!(entity instanceof Mech) || !entity.infernos.isStillBurning()) {
            return;
        }
        // Check if entering depth 2 water or prone in depth 1.
        if (waterLevel > 0 && entity.absHeight() < 0) {
            washInferno(entity, coords);
        }
    }

    /**
     * Washes off an inferno from a mech and adds it to the (water) hex.
     * 
     * @param entity
     *            The <code>Entity</code> that is taking a bath
     * @param coords
     *            The <code>Coords</code> the entity is at
     */
    void washInferno(Entity entity, Coords coords) {
        game.getBoard().addInfernoTo(coords, InfernoTracker.STANDARD_ROUND, 1);
        entity.infernos.clear();

        // Start a fire in the hex?
        IHex hex = game.getBoard().getHex(coords);
        Report r = new Report(2170);
        r.subject = entity.getId();
        r.addDesc(entity);
        if (!hex.containsTerrain(Terrains.FIRE)) {
            r.messageId = 2175;
            ignite(coords, true, null);
        }
        addReport(r);
    }

    /**
     * Add heat from the movement phase
     */
    public void addMovementHeat() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity entity = i.nextElement();

            if (entity.getMovementMode() == IEntityMovementMode.BIPED_SWIM || entity.getMovementMode() == IEntityMovementMode.QUAD_SWIM)
                return;

            // build up heat from movement
            if(entity.isEvading() && !(entity instanceof Aero)) {
                entity.heatBuildup += entity.getRunHeat() + 2;
            } else if (entity.moved == IEntityMovementType.MOVE_NONE) {
                entity.heatBuildup += entity.getStandingHeat();
            } else if (entity.moved == IEntityMovementType.MOVE_WALK || entity.moved == IEntityMovementType.MOVE_VTOL_WALK || entity.moved == IEntityMovementType.MOVE_CAREFUL_STAND) {
                entity.heatBuildup += entity.getWalkHeat();
            } else if (entity.moved == IEntityMovementType.MOVE_RUN || entity.moved == IEntityMovementType.MOVE_VTOL_RUN || entity.moved == IEntityMovementType.MOVE_SKID) {
                entity.heatBuildup += entity.getRunHeat();
            } else if (entity.moved == IEntityMovementType.MOVE_JUMP) {
                entity.heatBuildup += entity.getJumpHeat(entity.delta_distance);
            }
        }
    }

    /**
     * Set the locationsexposure of an entity
     * 
     * @param entity
     *            The <code>Entity</code> who's exposure is being set
     * @param hex
     *            The <code>IHex</code> the entity is in
     * @param isJump
     *            a <code>boolean</code> value wether the entity is jumping
     * @param elevation
     *            the elevation the entity should be at.
     */

    public Vector<Report> doSetLocationsExposure(Entity entity, IHex hex, boolean isJump, int elevation) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        if (hex.terrainLevel(Terrains.WATER) > 0 && !isJump && elevation < 0) {
            if (entity instanceof Mech && !entity.isProne() && hex.terrainLevel(Terrains.WATER) == 1) {
                for (int loop = 0; loop < entity.locations(); loop++) {
                    if (game.getPlanetaryConditions().isVacuum())
                        entity.setLocationStatus(loop, ILocationExposureStatus.VACUUM);
                    else
                        entity.setLocationStatus(loop, ILocationExposureStatus.NORMAL);
                }
                entity.setLocationStatus(Mech.LOC_RLEG, ILocationExposureStatus.WET);
                entity.setLocationStatus(Mech.LOC_LLEG, ILocationExposureStatus.WET);
                vPhaseReport.addAll(breachCheck(entity, Mech.LOC_RLEG, hex));
                vPhaseReport.addAll(breachCheck(entity, Mech.LOC_LLEG, hex));
                if (entity instanceof QuadMech) {
                    entity.setLocationStatus(Mech.LOC_RARM, ILocationExposureStatus.WET);
                    entity.setLocationStatus(Mech.LOC_LARM, ILocationExposureStatus.WET);
                    vPhaseReport.addAll(breachCheck(entity, Mech.LOC_RARM, hex));
                    vPhaseReport.addAll(breachCheck(entity, Mech.LOC_LARM, hex));
                }
            } else {
                for (int loop = 0; loop < entity.locations(); loop++) {
                    entity.setLocationStatus(loop, ILocationExposureStatus.WET);
                    vPhaseReport.addAll(breachCheck(entity, loop, hex));
                }
            }
        } else {
            for (int loop = 0; loop < entity.locations(); loop++) {
                if (game.getPlanetaryConditions().isVacuum())
                    entity.setLocationStatus(loop, ILocationExposureStatus.VACUUM);
                else
                    entity.setLocationStatus(loop, ILocationExposureStatus.NORMAL);
            }
        }
        return vPhaseReport;
    }

    /**
     * Do a roll to avoid pilot damage from g-forces
     * 
     * @param entity
     *            The <code>Entity</code> that should make the PSR
     * @param roll
     *            The <code>PilotingRollData</code> to be used for this PSR.
     * 
     * @return true if check succeeds, false otherwise.
     * 
     */
    private void resistGForce(Entity entity, int targetroll) {

        // okay, print the info
        Report r = new Report(9330);
        r.subject = entity.getId();
        r.addDesc(entity);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(9335);
        r.subject = entity.getId();
        r.add(Integer.toString(targetroll));
        r.add(diceRoll);
        if (diceRoll < targetroll) {
            r.choose(false);
            addReport(r);
            addReport(damageCrew(entity, 1));
        } else {
            r.choose(true);
            addReport(r);
        }
    }

    /**
     * Do a piloting skill check in space to avoid structural damage
     * 
     * @param entity
     *            The <code>Entity</code> that should make the PSR
     * @param roll
     *            The <code>PilotingRollData</code> to be used for this PSR.
     * 
     * @return true if check succeeds, false otherwise.
     * 
     */
    private boolean doSkillCheckInSpace(Entity entity, PilotingRollData roll) {

        if (roll.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            return true;
        }

        // okay, print the info
        Report r = new Report(9320);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(roll.getLastPlainDesc(), true);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(9325);
        r.subject = entity.getId();
        r.add(roll.getValueAsString());
        r.add(roll.getDesc());
        r.add(diceRoll);
        boolean suc;
        if (diceRoll < roll.getValue()) {
            r.choose(false);
            addReport(r);
            suc = false;
        } else {
            r.choose(true);
            addReport(r);
            suc = true;
        }

        return suc;
    }

    /**
     * Do a piloting skill check in space to do a successful maneuver Failure
     * means moving forward half velocity
     * 
     * @param entity
     *            The <code>Entity</code> that should make the PSR
     * @param roll
     *            The <code>PilotingRollData</code> to be used for this PSR.
     * 
     * @return true if check succeeds, false otherwise.
     * 
     */
    private boolean doSkillCheckManeuver(Entity entity, PilotingRollData roll) {

        if (roll.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            return true;
        }

        // okay, print the info
        Report r = new Report(9600);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(roll.getLastPlainDesc(), true);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(9601);
        r.subject = entity.getId();
        r.add(roll.getValueAsString());
        r.add(roll.getDesc());
        r.add(diceRoll);
        boolean suc;
        if (diceRoll < roll.getValue()) {
            r.choose(false);
            addReport(r);
            suc = false;
        } else {
            r.choose(true);
            addReport(r);
            suc = true;
        }

        return suc;
    }

    /**
     * Do a piloting skill check while standing still (during the movement
     * phase).
     * 
     * @param entity
     *            The <code>Entity</code> that should make the PSR
     * @param roll
     *            The <code>PilotingRollData</code> to be used for this PSR.
     * @return true if check succeeds, false otherwise.
     */
    private boolean doSkillCheckInPlace(Entity entity, PilotingRollData roll) {
        if (roll.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            return true;
        }

        if (entity.isProne()) {
            return true;
        }

        // okay, print the info
        Report r = new Report(2180);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(roll.getLastPlainDesc(), true);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(2185);
        r.subject = entity.getId();
        r.add(roll.getValueAsString());
        r.add(roll.getDesc());
        r.add(diceRoll);
        boolean suc;
        if (diceRoll < roll.getValue() ) {
            r.choose(false);
            addReport(r);
            if ( entity instanceof Mech 
                    && game.getOptions().booleanOption("tacops_falling_expanded") 
                    && entity.getCrew().getPiloting() < 6
                    && !entity.isHullDown() 
                    && entity.canGoHullDown() ){
                if ( entity.getCrew().getPiloting() > 1 && roll.getValue() - diceRoll < 2){
                    entity.setHullDown(true);
                }else if ( entity.getCrew().getPiloting() <= 1 && roll.getValue() - diceRoll < 3 ){
                    entity.setHullDown(true);
                }
            }
            if ( !entity.isHullDown() || ( entity.isHullDown() && !entity.canGoHullDown()) ) {
                addReport(doEntityFall(entity, roll));
            }
            else{
                r = new Report (2317);
                r.subject = entity.getId();
                r.add(entity.getDisplayName());
                addReport(r);
            }
                
            suc = false;
        } else {
            r.choose(true);
            addReport(r);
            suc = true;
        }

        return suc;
    }

    /**
     * Do a Piloting Skill check to dislogde swarming infantry.
     * 
     * @param entity
     *            The <code>Entity</code> that is doing the dislodging.
     * @param roll
     *            The <code>PilotingRollData</code> for this PSR.
     * @param curPos
     *            The <code>Coords</code> the entity is at.
     * @return <code>true</code> if the dislodging is successful.
     */
    private boolean doDislodgeSwarmerSkillCheck(Entity entity, PilotingRollData roll, Coords curPos) {
        // okay, print the info
        Report r = new Report(2180);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(roll.getLastPlainDesc(), true);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(2190);
        r.subject = entity.getId();
        r.add(roll.getValueAsString());
        r.add(roll.getDesc());
        r.add(diceRoll);
        if (diceRoll < roll.getValue()) {
            r.choose(false);
            addReport(r);
            return false;
        }
        // Dislodged swarmers don't get turns.
        int swarmerId = entity.getSwarmAttackerId();
        final Entity swarmer = game.getEntity(swarmerId);
        if (!swarmer.isDone()) {
            swarmer.setDone(true);
            game.removeTurnFor(swarmer);
            send(createTurnVectorPacket());
        }

        // Update the report and cause a fall.
        r.choose(true);
        addReport(r);
        entity.setPosition(curPos);
        addReport(doEntityFallsInto(entity, curPos, curPos, roll, false));
        return true;
    }

    /**
     * Do a piloting skill check while moving.
     * 
     * @param entity -
     *            the <code>Entity</code> that must roll.
     * @param src -
     *            the <code>Coords</code> the entity is moving from.
     * @param dest -
     *            the <code>Coords</code> the entity is moving to. This value
     *            can be the same as src for in-place checks.
     * @param roll -
     *            the <code>PilotingRollData</code> that is causing this
     *            check.
     * @param isFallRoll -
     *            a <code>boolean</code> flag that indicates that failure will
     *            result in a fall or not. Falls will be processed.
     * @return Margin of Failure if the pilot fails the skill check, 0 if they
     *         pass.
     */
    private int doSkillCheckWhileMoving(Entity entity, Coords src, Coords dest, PilotingRollData roll, boolean isFallRoll) {
        boolean fallsInPlace;

        // Start the info for this roll.
        Report r = new Report(1210);
        r.subject = entity.getId();
        r.addDesc(entity);

        // Will the entity fall in the source or destination hex?
        if (src.equals(dest)) {
            fallsInPlace = true;
            r.messageId = 2195;
            r.add(src.getBoardNum(), true);
        } else {
            fallsInPlace = false;
            r.messageId = 2200;
            r.add(src.getBoardNum(), true);
            r.add(dest.getBoardNum(), true);
        }

        // Finish the info.
        r.add(roll.getLastPlainDesc(), true);
        addReport(r);

        // roll
        final int diceRoll = Compute.d6(2);
        r = new Report(2185);
        r.subject = entity.getId();
        r.add(roll.getValueAsString());
        r.add(roll.getDesc());
        r.add(diceRoll);
        if (diceRoll < roll.getValue()) {
            // Does failing the PSR result in a fall.
            if (isFallRoll) {
                r.choose(false);
                addReport(r);
                addReport(doEntityFallsInto(entity, fallsInPlace ? dest : src, fallsInPlace ? src : dest, roll));
            } else {
                r.messageId = 2190;
                r.choose(false);
                addReport(r);
                entity.setPosition(fallsInPlace ? src : dest);
            }
            return roll.getValue() - diceRoll;
        }
        r.choose(true);
        addReport(r);
        return 0;
    }

    /**
     * The entity falls into the hex specified. Check for any conflicts and
     * resolve them. Deal damage to faller.
     * 
     * @param entity
     *            The <code>Entity</code> that is falling.
     * @param src
     *            The <code>Coords</code> of the source hex.
     * @param dest
     *            The <code>Coords</code> of the destination hex.
     * @param roll
     *            The <code>PilotingRollData</code> to be used for PSRs
     *            induced by the falling.
     */
    private Vector<Report> doEntityFallsInto(Entity entity, Coords src, Coords dest, PilotingRollData roll) {
        return doEntityFallsInto(entity, src, dest, roll, true);
    }

    /**
     * The entity falls into the hex specified. Check for any conflicts and
     * resolve them. Deal damage to faller.
     * 
     * @param entity
     *            The <code>Entity</code> that is falling.
     * @param src
     *            The <code>Coords</code> of the source hex.
     * @param dest
     *            The <code>Coords</code> of the destination hex.
     * @param roll
     *            The <code>PilotingRollData</code> to be used for PSRs
     *            induced by the falling.
     * @param causeAffa
     *            The <code>boolean</code> value wether this fall should be
     *            able to cause an accidental fall from above
     */
    private Vector<Report> doEntityFallsInto(Entity entity, Coords src, Coords dest, PilotingRollData roll, boolean causeAffa) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        final IHex srcHex = game.getBoard().getHex(src);
        final IHex destHex = game.getBoard().getHex(dest);
        final int srcHeightAboveFloor = entity.getElevation() + srcHex.depth();
        final int fallElevation = Math.max(0, srcHex.floor() + srcHeightAboveFloor - (destHex.containsTerrain(Terrains.ICE) ? destHex.surface() : destHex.floor()));
        int direction;
        if (src.equals(dest))
            direction = Compute.d6() - 1;
        else
            direction = src.direction(dest);
        Report r;
        // check entity in target hex
        Entity affaTarget = game.getAffaTarget(dest, entity);
        // falling mech falls
        r = new Report(2205);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(fallElevation);
        r.add(dest.getBoardNum(), true);
        vPhaseReport.add(r);

        // if hex was empty, deal damage and we're done
        if (affaTarget == null) {
            vPhaseReport.addAll(doEntityFall(entity, dest, fallElevation, roll));
            return vPhaseReport;
        }

        // hmmm... somebody there... problems.
        if (fallElevation >= 2 && causeAffa) {
            // accidental fall from above: havoc!
            r = new Report(2210);
            r.subject = entity.getId();
            r.addDesc(affaTarget);
            vPhaseReport.add(r);

            // determine to-hit number
            ToHitData toHit = new ToHitData(7, "base");
            if (affaTarget instanceof Tank) {
                toHit = new ToHitData(TargetRoll.AUTOMATIC_SUCCESS, "Target is a Tank");
            } else {
                toHit.append(Compute.getTargetMovementModifier(game, affaTarget.getId()));
                toHit.append(Compute.getTargetTerrainModifier(game, affaTarget));
            }

            if (toHit.getValue() != TargetRoll.AUTOMATIC_FAIL) {
                // collision roll
                final int diceRoll = Compute.d6(2);
                if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
                    r = new Report(2212);
                    r.add(toHit.getValue());
                    r.indent();
                } else {
                    r = new Report(2215);
                    r.subject = entity.getId();
                    r.add(toHit.getValue());
                    r.add(diceRoll);
                    r.newlines = 0;
                    r.indent();
                }
                vPhaseReport.add(r);
                if (diceRoll >= toHit.getValue()) {
                    // deal damage to target
                    int damage = Compute.getAffaDamageFor(entity);
                    r = new Report(2220);
                    r.subject = affaTarget.getId();
                    r.addDesc(affaTarget);
                    r.add(damage);
                    vPhaseReport.add(r);
                    while (damage > 0) {
                        int cluster = Math.min(5, damage);
                        HitData hit = affaTarget.rollHitLocation(ToHitData.HIT_PUNCH, ToHitData.SIDE_FRONT);
                        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                        vPhaseReport.addAll(damageEntity(affaTarget, hit, cluster));
                        damage -= cluster;
                    }

                    // attacker falls as normal, on his back
                    // only given a modifier, so flesh out into a full piloting
                    // roll
                    PilotingRollData pilotRoll = entity.getBasePilotingRoll();
                    pilotRoll.append(roll);
                    //entity.addPilotingModifierForTerrain(pilotRoll, dest);
                    vPhaseReport.addAll(doEntityFall(entity, dest, fallElevation, 3, pilotRoll));
                    vPhaseReport.addAll(doEntityDisplacementMinefieldCheck(entity, src, dest, entity.getElevation()));

                    // defender pushed away, or destroyed, if there is a
                    // stacking violation
                    Entity violation = Compute.stackingViolation(game, entity.getId(), dest);
                    if (violation != null) {
                        Coords targetDest = Compute.getValidDisplacement(game, violation.getId(), dest, direction);
                        if (targetDest != null) {
                            vPhaseReport.addAll(doEntityDisplacement(affaTarget, dest, targetDest, new PilotingRollData(violation.getId(), 2, "fallen on")));
                            // Update the violating entity's postion on the
                            // client.
                            entityUpdate(affaTarget.getId());
                        } else {
                            // ack! automatic death! Tanks
                            // suffer an ammo/power plant hit.
                            // TODO : a Mech suffers a Head Blown Off crit.
                            vPhaseReport.addAll(destroyEntity(affaTarget, "impossible displacement", violation instanceof Mech, violation instanceof Mech));
                        }
                    }
                    return vPhaseReport;
                }
            } else {
                // automatic miss
                r = new Report(2213);
                r.add(toHit.getDesc());
                vPhaseReport.add(r);
            }
            // ok, we missed, let's fall into a valid other hex and not cause an
            // AFFA while doing so
            Coords targetDest = Compute.getValidDisplacement(game, entity.getId(), dest, direction);
            if (targetDest != null) {
                vPhaseReport.addAll(doEntityFallsInto(entity, src, targetDest, new PilotingRollData(entity.getId(), TargetRoll.IMPOSSIBLE, "pushed off a cliff"), false));
                // Update the entity's postion on the client.
                entityUpdate(entity.getId());
            } else {
                // ack! automatic death! Tanks
                // suffer an ammo/power plant hit.
                // TODO : a Mech suffers a Head Blown Off crit.
                vPhaseReport.addAll(destroyEntity(entity, "impossible displacement", entity instanceof Mech, entity instanceof Mech));
            }
        } else {
            // damage as normal
            vPhaseReport.addAll(doEntityFall(entity, dest, fallElevation, roll));
            Entity violation = Compute.stackingViolation(game, entity.getId(), dest);
            if (violation != null) {
                // target gets displaced, because of low elevation
                Coords targetDest = Compute.getValidDisplacement(game, entity.getId(), dest, direction);
                vPhaseReport.addAll(doEntityDisplacement(violation, dest, targetDest, new PilotingRollData(violation.getId(), 0, "domino effect")));
                // Update the violating entity's postion on the client.
                entityUpdate(violation.getId());
            }
        }
        return vPhaseReport;
    }

    /**
     * Displace a unit in the direction specified. The unit moves in that
     * direction, and the piloting skill roll is used to determine if it falls.
     * The roll may be unnecessary as certain situations indicate an automatic
     * fall. Rolls are added to the piloting roll list.
     */
    private Vector<Report> doEntityDisplacement(Entity entity, Coords src, Coords dest, PilotingRollData roll) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        Report r;
        if (!game.getBoard().contains(dest)) {
            // set position anyway, for pushes moving through and stuff like
            // that
            entity.setPosition(dest);
            if (!entity.isDoomed()) {
                game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED);
                send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_PUSHED));
                // entity forced from the field
                r = new Report(2230);
                r.subject = entity.getId();
                r.addDesc(entity);
                vPhaseReport.add(r);
                // TODO: remove passengers and swarmers.
            }
            return vPhaseReport;
        }
        final IHex srcHex = game.getBoard().getHex(src);
        final IHex destHex = game.getBoard().getHex(dest);
        final int direction = src.direction(dest);

        // Handle null hexes.
        if (srcHex == null || destHex == null) {
            System.err.println("Can not displace " + entity.getShortName() + " from " + src + " to " + dest + '.');
            return vPhaseReport;
        }
        int fallElevation = entity.elevationOccupied(srcHex) - entity.elevationOccupied(destHex);
        if (fallElevation > 1) {
            if (roll == null)
                roll = entity.getBasePilotingRoll();
            vPhaseReport.addAll(doEntityFallsInto(entity, src, dest, roll));
            return vPhaseReport;
        }
        // unstick the entity if it was stuck in swamp
        boolean wasStuck = entity.isStuck();
        entity.setStuck(false);
        int oldElev = entity.elevationOccupied(srcHex);
        // move the entity into the new location gently
        entity.setPosition(dest);
        entity.setElevation(entity.elevationOccupied(destHex) - destHex.surface());
        Building bldg = game.getBoard().getBuildingAt(dest);
        if (bldg != null) {
            if (destHex.terrainLevel(Terrains.BLDG_ELEV) > oldElev) {
                // woops, into the building we go
                passBuildingWall(entity, game.getBoard().getBuildingAt(dest), src, dest, 1, "displaced into", Math.abs(entity.getFacing() - src.direction(dest)) == 3, entity.moved, true);
            } else {
                // woops, we step on the roof
                checkBuildingCollapseWhileMoving(bldg, entity, dest);
            }
        }
        Entity violation = Compute.stackingViolation(game, entity.getId(), dest);
        if (violation == null) {
            // move and roll normally
            r = new Report(2235);
            r.indent();
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(dest.getBoardNum(), true);
            vPhaseReport.add(r);
        } else {
            // domino effect: move & displace target
            r = new Report(2240);
            r.indent();
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(dest.getBoardNum(), true);
            r.addDesc(violation);
            vPhaseReport.add(r);
        }
        // trigger any special things for moving to the new hex
        vPhaseReport.addAll(doEntityDisplacementMinefieldCheck(entity, src, dest, entity.getElevation()));
        vPhaseReport.addAll(doSetLocationsExposure(entity, destHex, false, entity.getElevation()));
        //mechs that were stuck will automatically fall in their new hex
        if(wasStuck && entity instanceof Mech && !entity.isProne()) {
            if (roll == null)
                roll = entity.getBasePilotingRoll();
            vPhaseReport.addAll(doEntityFall(entity, dest, 0, roll));
        }
        //check bog-down conditions
        vPhaseReport.addAll(doEntityDisplacementBogDownCheck(entity, dest, entity.getElevation()));
        
        if (roll != null) {
            game.addPSR(roll);
        }
        // Update the entity's postion on the client.
        entityUpdate(entity.getId());

        if (violation != null) {
            vPhaseReport.addAll(doEntityDisplacement(violation, dest, dest.translated(direction), new PilotingRollData(violation.getId(), 0, "domino effect")));
            // Update the violating entity's postion on the client,
            // if it didn't get displaced off the board.
            if (!game.isOutOfGame(violation)) {
                entityUpdate(violation.getId());
            }
        }
        return vPhaseReport;
    }

    private Vector<Report> doEntityDisplacementMinefieldCheck(Entity entity, Coords src, Coords dest, int elev) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        boolean boom = checkVibrabombs(entity, dest, true, vPhaseReport);
        if (game.containsMinefield(dest)) {
            boom = enterMinefield(entity, dest, elev, true, vPhaseReport) || boom;
        }
        if(boom) {
            resetMines();
        }
        
        return vPhaseReport;
    }
    
    private Vector<Report> doEntityDisplacementBogDownCheck(Entity entity, Coords c, int elev) {
        Vector<Report> vReport = new Vector<Report>();
        Report r; 
        IHex destHex = game.getBoard().getHex(c);
        int bgMod = destHex.getBogDownModifier(entity.getMovementMode(), entity instanceof LargeSupportTank);
        if (bgMod != TargetRoll.AUTOMATIC_SUCCESS 
                && (entity.getMovementMode() != IEntityMovementMode.HOVER)
                && (entity.getMovementMode() != IEntityMovementMode.WIGE)
                && elev == 0) {
            PilotingRollData roll = entity.getBasePilotingRoll();
            roll.append(new PilotingRollData(entity.getId(), bgMod, "avoid bogging down"));
            int stuckroll = Compute.d6(2);
            if(stuckroll < roll.getValue()) {
                entity.setStuck(true);
                r = new Report(2081);
                r.subject = entity.getId();
                r.add(entity.getDisplayName(), true);
                vReport.add(r);
                //check for quicksand
                vReport.addAll(checkQuickSand(c));
            }
            
        } 
        return vReport;
    }

    /**
     * Receive a deployment packet. If valid, execute it and end the current
     * turn.
     */
    private void receiveDeployment(Packet packet, int connId) {
        Entity entity = game.getEntity(packet.getIntValue(0));
        Coords coords = (Coords) packet.getObject(1);
        int nFacing = packet.getIntValue(2);

        // Handle units that deploy loaded with other units.
        int loadedCount = packet.getIntValue(3);
        Vector<Entity> loadVector = new Vector<Entity>();
        for (int i = 0; i < loadedCount; i++) {
            int loadedId = packet.getIntValue(5 + i);
            loadVector.addElement(game.getEntity(loadedId));
        }

        // is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_DEPLOYMENT) {
            System.err.println("error: server got deployment packet in wrong phase");
            return;
        }

        // can this player/entity act right now?
        final boolean assaultDrop = packet.getBooleanValue(4);
        if (!game.getTurn().isValid(connId, entity, game) || !(game.getBoard().isLegalDeployment(coords, entity.getOwner()) || assaultDrop && game.getOptions().booleanOption("assault_drop") && entity.canAssaultDrop())) {
            System.err.println("error: server got invalid deployment packet");
            return;
        }

        // looks like mostly everything's okay
        processDeployment(entity, coords, nFacing, loadVector, assaultDrop);

        // Update visibility indications if using double blind.
        if (doBlind()) {
            updateVisibilityIndicator();
        }

        endCurrentTurn(entity);
    }

    /**
     * Process a deployment packet by... deploying the entity! We load any other
     * specified entities inside of it too. Also, check that the deployment is
     * valid.
     */
    private void processDeployment(Entity entity, Coords coords, int nFacing, Vector<Entity> loadVector, boolean assaultDrop) {
        for (Enumeration<Entity> i = loadVector.elements(); i.hasMoreElements();) {
            Entity loaded = i.nextElement();
            if (loaded == null || loaded.getPosition() != null || loaded.getTransportId() != Entity.NONE) {
                // Something is fishy in Denmark.
                System.err.println("error: " + entity + " can not load entity #" + loaded);
                break;
            }
            // Have the deployed unit load the indicated unit.
            loadUnit(entity, loaded);
        }

        /*
         * deal with starting velocity for advanced movement. Probably not the
         * best place to do it, but what are you going to do
         */
        if (entity instanceof Aero && game.useVectorMove()) {
            Aero a = (Aero) entity;
            if (a.getCurrentVelocityActual() > 0) {
                int[] v = { 0, 0, 0, 0, 0, 0 };
                v[nFacing] = a.getCurrentVelocityActual();
                entity.setVectors(v);
            }
        }

        entity.setPosition(coords);
        entity.setFacing(nFacing);
        entity.setSecondaryFacing(nFacing);
        IHex hex = game.getBoard().getHex(coords);
        if (assaultDrop) {
            entity.setElevation(hex.ceiling() - hex.surface() + 100); // falling
            // from
            // the
            // sky!
            entity.setAssaultDropInProgress(true);
        } else if (entity instanceof VTOL) {
            // We should let players pick, but this simplifies a lot.
            // Only do it for VTOLs, though; assume everything else is on the
            // ground.
            entity.setElevation(hex.ceiling() - hex.surface() + 1);
            while (Compute.stackingViolation(game, entity, coords, null) != null && entity.getElevation() <= 50) {
                entity.setElevation(entity.getElevation() + 1);
            }
            if (entity.getElevation() > 50) {
                throw new IllegalStateException("Entity #" + entity.getId() + " appears to be in an infinite loop trying to get a legal elevation.");
            }
        } else if (entity instanceof Aero) {
            if (game.getBoard().inAtmosphere()) {
                // all spheroid craft should have velocity of zero in atmosphere
                // regardless of
                // what was entered
                Aero a = (Aero) entity;
                if (a.isSpheroid() || game.getPlanetaryConditions().isVacuum()) {
                    a.setCurrentVelocity(0);
                    a.setNextVelocity(0);
                }
            } else if (game.getBoard().inSpace()) {
                entity.setElevation(0);
            } else {
                entity.setElevation(hex.floor() - hex.surface());
            }
        } else if (entity.getMovementMode() == IEntityMovementMode.SUBMARINE) {
            // TODO: Submarines should have a selectable height.
            // For now, pretend they're regular naval.
            entity.setElevation(0);
        } else if (entity.getMovementMode() == IEntityMovementMode.HOVER || entity.getMovementMode() == IEntityMovementMode.WIGE || entity.getMovementMode() == IEntityMovementMode.NAVAL || entity.getMovementMode() == IEntityMovementMode.HYDROFOIL) {
            // For now, assume they're on the surface.
            // entity elevation is relative to hex surface
            entity.setElevation(0);
        } else if (hex.containsTerrain(Terrains.ICE)) {
            entity.setElevation(0);
        } else if (hex.containsTerrain(Terrains.BRIDGE)) {
            entity.setElevation(hex.terrainLevel(Terrains.BRIDGE_ELEV));
        } else {
            Building bld = game.getBoard().getBuildingAt(entity.getPosition());

            if (bld != null && bld.getType() == Building.WALL) {
                entity.setElevation(hex.terrainLevel(Terrains.BLDG_ELEV));
            } else
                // For anything else, assume they're on the floor.
                // entity elevation is relative to hex surface
                entity.setElevation(hex.floor() - hex.surface());
        }
        entity.setDone(true);
        entity.setDeployed(true);
        entityUpdate(entity.getId());
    }

    /**
     * receive a packet that contains hexes that are automatically hit by
     * artillery
     * 
     * @param packet
     * @param connId
     */
    @SuppressWarnings("unchecked")
    private void receiveArtyAutoHitHexes(Packet packet, int connId) {
        PlayerIDandList<Coords> artyAutoHitHexes = (PlayerIDandList<Coords>) packet.getObject(0);

        int playerId = artyAutoHitHexes.getPlayerID();

        // is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_SET_ARTYAUTOHITHEXES) {
            System.err.println("error: server got set artyautohithexespacket in wrong phase");
            return;
        }
        game.getPlayer(playerId).setArtyAutoHitHexes(artyAutoHitHexes);

        for (Coords coord : artyAutoHitHexes) {
            game.getBoard().addSpecialHexDisplay(coord, new SpecialHexDisplay(SpecialHexDisplay.Type.ARTILLERY_AUTOHIT, SpecialHexDisplay.NO_ROUND, getPlayer(playerId).getName(), "ArtyAutoHit Hex, better text later"));
        }
        endCurrentTurn(null);
    }

    /**
     * receive a packet that contains minefields
     * 
     * @param packet
     * @param connId
     */
    @SuppressWarnings("unchecked")
    private void receiveDeployMinefields(Packet packet, int connId) {
        Vector<Minefield> minefields = (Vector<Minefield>) packet.getObject(0);

        // is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_DEPLOY_MINEFIELDS) {
            System.err.println("error: server got deploy minefields packet in wrong phase");
            return;
        }

        // looks like mostly everything's okay
        processDeployMinefields(minefields);
        endCurrentTurn(null);
    }

    /**
     * process deployment of minefields
     * 
     * @param minefields
     */
    private void processDeployMinefields(Vector<Minefield> minefields) {
        int playerId = Player.PLAYER_NONE;
        for (int i = 0; i < minefields.size(); i++) {
            Minefield mf = minefields.elementAt(i);
            playerId = mf.getPlayerId();

            game.addMinefield(mf);
            if (mf.getType() == Minefield.TYPE_VIBRABOMB) {
                game.addVibrabomb(mf);
            }
        }

        Player player = game.getPlayer(playerId);
        if (null != player) {
            int teamId = player.getTeam();

            if (teamId != Player.TEAM_NONE) {
                Enumeration<Team> teams = game.getTeams();
                while (teams.hasMoreElements()) {
                    Team team = teams.nextElement();
                    if (team.getId() == teamId) {
                        Enumeration<Player> players = team.getPlayers();
                        while (players.hasMoreElements()) {
                            Player teamPlayer = players.nextElement();
                            if (teamPlayer.getId() != player.getId()) {
                                send(teamPlayer.getId(), new Packet(Packet.COMMAND_DEPLOY_MINEFIELDS, minefields));
                            }
                            teamPlayer.addMinefields(minefields);
                        }
                        break;
                    }
                }
            } else {
                player.addMinefields(minefields);
            }
        }
    }

    /**
     * Gets a bunch of entity attacks from the packet. If valid, processess them
     * and ends the current turn.
     */
    @SuppressWarnings("unchecked")
    private void receiveAttack(Packet packet, int connId) {
        Entity entity = game.getEntity(packet.getIntValue(0));
        Vector<EntityAction> vector = (Vector<EntityAction>) packet.getObject(1);

        // is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_FIRING && game.getPhase() != IGame.Phase.PHASE_PHYSICAL && game.getPhase() != IGame.Phase.PHASE_TARGETING && game.getPhase() != IGame.Phase.PHASE_OFFBOARD) {
            System.err.println("error: server got attack packet in wrong phase");
            return;
        }

        // can this player/entity act right now?
        GameTurn turn = game.getTurn();
        if(game.isPhaseSimultaneous()) {
            turn = game.getTurnForPlayer(connId);
        }
        if (turn==null || !turn.isValid(connId, entity, game)) {
            System.err.println("error: server got invalid attack packet");
            send(connId, createTurnVectorPacket());
            send(connId, createTurnIndexPacket());
            return;
        }

        // looks like mostly everything's okay
        processAttack(entity, vector);

        // Update visibility indications if using double blind.
        if (doBlind()) {
            updateVisibilityIndicator();
        }

        endCurrentTurn(entity);
    }

    /**
     * Process a batch of entity attack (or twist) actions by adding them to the
     * proper list to be processed later.
     */
    private void processAttack(Entity entity, Vector<EntityAction> vector) {

        // Not **all** actions take up the entity's turn.
        boolean setDone = !(game.getTurn() instanceof GameTurn.TriggerAPPodTurn || game.getTurn() instanceof GameTurn.TriggerBPodTurn) ;
        for (Enumeration<EntityAction> i = vector.elements(); i.hasMoreElements();) {
            EntityAction ea = i.nextElement();

            // is this the right entity?
            if (ea.getEntityId() != entity.getId()) {
                System.err.println("error: attack packet has wrong attacker");
                continue;
            }

            // Anti-mech and pointblank attacks from
            // hiding may allow the target to respond.
            if (ea instanceof WeaponAttackAction) {
                final WeaponAttackAction waa = (WeaponAttackAction) ea;
                final String weaponName = entity.getEquipment(waa.getWeaponId()).getType().getInternalName();

                if (Infantry.SWARM_MEK.equals(weaponName) || Infantry.LEG_ATTACK.equals(weaponName)) {

                    // Does the target have any AP Pods available?
                    final Entity target = game.getEntity(waa.getTargetId());
                    for (Mounted equip : target.getMisc()) {
                        if (equip.getType().hasFlag(MiscType.F_AP_POD) && equip.canFire()) {

                            // Yup. Insert a game turn to handle AP pods.
                            // ASSUMPTION : AP pod declarations come
                            // immediately after the attack declaration.
                            game.insertNextTurn(new GameTurn.TriggerAPPodTurn(target.getOwnerId(), target.getId()));
                            send(createTurnVectorPacket());

                            // We can stop looking.
                            break;

                        } // end found-available-ap-pod

                    } // Check the next piece of equipment on the target.

                    for (Mounted weapon : target.getWeaponList()) {
                        if ( weapon.getType().hasFlag(WeaponType.F_B_POD) && weapon.canFire() ) {

                            // Yup. Insert a game turn to handle B pods.
                            // ASSUMPTION : B pod declarations come
                            // immediately after the attack declaration.
                            game.insertNextTurn(new GameTurn.TriggerBPodTurn(target.getOwnerId(), target.getId(), weaponName));
                            send(createTurnVectorPacket());

                            // We can stop looking.
                            break;

                        } // end found-available-b-pod
                    } // Check the next piece of equipment on the target.
                } // End check-for-available-ap-pod
            }

            // If attacker breaks grapple, defender may counter
            if (ea instanceof BreakGrappleAttackAction) {
                final BreakGrappleAttackAction bgaa = (BreakGrappleAttackAction) ea;
                final Entity att = (game.getEntity(bgaa.getEntityId()));
                if (att.isGrappleAttacker()) {
                    final Entity def = (game.getEntity(bgaa.getTargetId()));
                    // Remove existing break grapple by defender (if exists)
                    if (def.isDone()) {
                        game.removeActionsFor(def.getId());
                    } else {
                        game.removeTurnFor(def);
                        def.setDone(true);
                    }
                    // Add a turn to declare counterattack
                    game.insertNextTurn(new GameTurn.CounterGrappleTurn(def.getOwnerId(), def.getId()));
                    send(createTurnVectorPacket());
                }
            }
            if (ea instanceof ArtilleryAttackAction) {
                boolean firingAtNewHex = false;
                final ArtilleryAttackAction aaa = (ArtilleryAttackAction) ea;
                final Entity firingEntity = game.getEntity(aaa.getEntityId());
                for (Enumeration<AttackHandler> j = game.getAttacks(); !firingAtNewHex && j.hasMoreElements();) {
                    WeaponHandler wh = (WeaponHandler) j.nextElement();
                    if (wh.waa instanceof ArtilleryAttackAction) {
                        ArtilleryAttackAction oaaa = (ArtilleryAttackAction) wh.waa;
                        if (oaaa.getEntityId() == aaa.getEntityId() && !oaaa.getTarget(game).getPosition().equals(aaa.getTarget(game).getPosition())) {
                            firingAtNewHex = true;
                        }
                    }
                    if (firingAtNewHex) {
                        clearArtillerySpotters(firingEntity.getId(), aaa.getWeaponId());
                    }
                    Enumeration<Entity> spotters = game.getSelectedEntities(new EntitySelector() {
                        public int player = firingEntity.getOwnerId();
                        public Targetable target = aaa.getTarget(game);

                        public boolean accept(Entity entity) {
                            return ((player == entity.getOwnerId()) && !((LosEffects.calculateLos(game, entity.getId(), target)).isBlocked()) && entity.isActive());
                        }
                    });
                    Vector<Integer> spotterIds = new Vector<Integer>();
                    while (spotters.hasMoreElements()) {
                        Integer id = new Integer(spotters.nextElement().getId());
                        spotterIds.addElement(id);
                    }
                    aaa.setSpotterIds(spotterIds);
                }
            }

            // The equipment type of a club needs to be restored.
            if (ea instanceof ClubAttackAction) {
                ClubAttackAction caa = (ClubAttackAction) ea;
                Mounted club = caa.getClub();
                club.restore();
            }

            if (ea instanceof PushAttackAction) {
                // push attacks go the end of the displacement attacks
                PushAttackAction paa = (PushAttackAction) ea;
                entity.setDisplacementAttack(paa);
                game.addCharge(paa);
            } else if (ea instanceof DodgeAction) {
                entity.dodging = true;
            } else if (ea instanceof SpotAction) {
                entity.setSpotting(true);
                entity.setSpotTargetId(((SpotAction) ea).getTargetId());
            } else {
                // add to the normal attack list.
                game.addAction(ea);
            }

            // Mark any AP Pod as used in this turn.
            if (ea instanceof TriggerAPPodAction) {
                TriggerAPPodAction tapa = (TriggerAPPodAction) ea;
                Mounted pod = entity.getEquipment(tapa.getPodId());
                pod.setUsedThisRound(true);
            }
            // Mark any B Pod as used in this turn.
            if (ea instanceof TriggerBPodAction) {
                TriggerBPodAction tba = (TriggerBPodAction) ea;
                Mounted pod = entity.getEquipment(tba.getPodId());
                pod.setUsedThisRound(true);
            }
        }

        // Unless otherwise stated,
        // this entity is done for the round.
        if (setDone) {
            entity.setDone(true);
        }
        entityUpdate(entity.getId());

        Packet p = createAttackPacket(vector,0);
        if(game.isPhaseSimultaneous()) {
            // Update attack only to player who declared it & observers
            for(Player player:game.getPlayersVector()) {
                if(player.canSeeAll() || player.isObserver() || entity.getOwnerId()==player.getId()) {
                    send(player.getId(),p);
                }
            }
        } else {
            // update all players on the attacks. Don't worry about pushes being a
            // "charge" attack. It doesn't matter to the client.
            send(p);
        }
    }

    /**
     * Auto-target active AMS systems
     */
    private void assignAMS() {

        // sort all missile-based attacks by the target
        Hashtable<Entity, Vector<WeaponHandler>> htAttacks = new Hashtable<Entity, Vector<WeaponHandler>>();
        for (AttackHandler ah : game.getAttacksVector()) {
            WeaponHandler wh = (WeaponHandler) ah;
            WeaponAttackAction waa = wh.waa;
            // ignore artillery attacks, here the attacking entity
            // might no longer be in the game
            if (waa instanceof ArtilleryAttackAction)
                continue;
            Mounted weapon = game.getEntity(waa.getEntityId()).getEquipment(waa.getWeaponId());

            // Only entities can have AMS.
            if (Targetable.TYPE_ENTITY != waa.getTargetType()) {
                continue;
            }

            // AMS is only used against attacks that hit (TW p129)
            if (wh.roll < wh.toHit.getValue()) {
                continue;
            }

            // Can only use AMS versus missles.
            if (((WeaponType) weapon.getType()).getDamage() == WeaponType.DAMAGE_MISSILE) {
                Entity target = game.getEntity(waa.getTargetId());
                Vector<WeaponHandler> v = htAttacks.get(target);
                if (v == null) {
                    v = new Vector<WeaponHandler>();
                    htAttacks.put(target, v);
                }
                v.addElement(wh);
            }
        }

        // let each target assign its AMS
        for (Entity e : htAttacks.keySet()) {
            Vector<WeaponHandler> vAttacks = htAttacks.get(e);
            e.assignAMS(vAttacks);
        }
    }

    /**
     * Called during the weapons fire phase. Resolves anything other than
     * weapons fire that happens. Torso twists, for example.
     */
    private void resolveAllButWeaponAttacks() {
        if (game.getPhase() == IGame.Phase.PHASE_FIRING) {
            // Phase report header
            addReport(new Report(3000, Report.PUBLIC));
            Report r;
            for (Enumeration<LayMinefieldAction> e = game.getLayMinefieldActions(); e.hasMoreElements();) {
                LayMinefieldAction lma = e.nextElement();
                Entity ent = game.getEntity(lma.getEntityId());
                Mounted mine = ent.getEquipment(lma.getMineId());
                if (!mine.isMissing()) {
                    switch (mine.getMineType()) {
                    case Mounted.MINE_CONVENTIONAL:
                        deliverThunderMinefield(ent.getPosition(), ent.getOwnerId(), 10, ent.getId());
                        mine.setMissing(true);
                        r = new Report(3500, Report.PLAYER);
                        r.subject = ent.getId();
                        r.addDesc(ent);
                        r.type = Report.PLAYER;
                        r.add(ent.getPosition().getBoardNum());
                        addReport(r);
                        break;
                    case Mounted.MINE_VIBRABOMB:
                        deliverThunderVibraMinefield(ent.getPosition(), ent.getOwnerId(), 10, mine.getVibraSetting(), ent.getId());
                        mine.setMissing(true);
                        r = new Report(3505, Report.PLAYER);
                        r.subject = ent.getId();
                        r.addDesc(ent);
                        r.add(ent.getPosition().getBoardNum());
                        addReport(r);
                        break;
                    case Mounted.MINE_ACTIVE:
                        deliverThunderActiveMinefield(ent.getPosition(), ent.getOwnerId(), 10, ent.getId());
                        mine.setMissing(true);
                        r = new Report(3510, Report.PLAYER);
                        r.subject = ent.getId();
                        r.addDesc(ent);
                        r.add(ent.getPosition().getBoardNum());
                        addReport(r);
                        break;
                    case Mounted.MINE_INFERNO:
                        deliverThunderInfernoMinefield(ent.getPosition(), ent.getOwnerId(), 10, ent.getId());
                        mine.setMissing(true);
                        r = new Report(3515, Report.PLAYER);
                        r.subject = ent.getId();
                        r.addDesc(ent);
                        r.add(ent.getPosition().getBoardNum());
                        addReport(r);
                        break;
                    // TODO: command-detonated mines
                    // case 2:
                    }
                }
            }
            game.resetLayMinefieldActions();
        }

        Vector<EntityAction> triggerPodActions = new Vector<EntityAction>();
        // loop thru actions and handle everything we expect except attacks
        for (Enumeration<EntityAction> i = game.getActions(); i.hasMoreElements();) {
            EntityAction ea = i.nextElement();
            Entity entity = game.getEntity(ea.getEntityId());
            if (ea instanceof TorsoTwistAction) {
                TorsoTwistAction tta = (TorsoTwistAction) ea;
                if (entity.canChangeSecondaryFacing()) {
                    entity.setSecondaryFacing(tta.getFacing());
                }
            } else if (ea instanceof FlipArmsAction) {
                FlipArmsAction faa = (FlipArmsAction) ea;
                entity.setArmsFlipped(faa.getIsFlipped());
            } else if (ea instanceof FindClubAction) {
                resolveFindClub(entity);
            } else if (ea instanceof UnjamAction) {
                resolveUnjam(entity);
            } else if (ea instanceof ClearMinefieldAction) {
                resolveClearMinefield(entity, ((ClearMinefieldAction)ea).getMinefield());
            } else if (ea instanceof TriggerAPPodAction) {
                TriggerAPPodAction tapa = (TriggerAPPodAction) ea;

                // Don't trigger the same pod twice.
                if (!triggerPodActions.contains(tapa)) {
                    triggerAPPod(entity, tapa.getPodId());
                    triggerPodActions.addElement(tapa);
                } else {
                    System.err.print("AP Pod #");
                    System.err.print(tapa.getPodId());
                    System.err.print(" on ");
                    System.err.print(entity.getDisplayName());
                    System.err.println(" was already triggered this round!!");
                }
            } else if (ea instanceof TriggerBPodAction) {
                TriggerBPodAction tba = (TriggerBPodAction) ea;

                // Don't trigger the same pod twice.
                if (!triggerPodActions.contains(tba)) {
                    triggerBPod(entity, tba.getPodId(), tba.getTarget());
                    triggerPodActions.addElement(tba);
                } else {
                    System.err.print("B Pod #");
                    System.err.print(tba.getPodId());
                    System.err.print(" on ");
                    System.err.print(entity.getDisplayName());
                    System.err.println(" was already triggered this round!!");
                }
            } else if (ea instanceof SearchlightAttackAction) {
                SearchlightAttackAction saa = (SearchlightAttackAction) ea;
                addReport(saa.resolveAction(game));
            } else if (ea instanceof UnjamTurretAction) {
                if (entity instanceof Tank) {
                    ((Tank) entity).unjamTurret();
                    Report r = new Report(3033);
                    r.addDesc(entity);
                    addReport(r);
                } else {
                    System.err.println("Non-Tank tried to unjam turret");
                }
            } else if (ea instanceof RepairWeaponMalfunctionAction) {
                if (entity instanceof Tank) {
                    Mounted m = entity.getEquipment(((RepairWeaponMalfunctionAction) ea).getWeaponId());
                    m.setJammed(false);
                    ((Tank) entity).getJammedWeapons().remove(m);
                    Report r = new Report(3034);
                    r.addDesc(entity);
                    r.add(m.getName());
                    addReport(r);
                } else {
                    System.err.println("Non-Tank tried to repair weapon malfunction");
                }
            }
        }
    }
    
    private void reportGhostTargetRolls() {
        //run through an enumeration of deployed game entities. If they have ghost targets, then check the roll
        //and report it
        Report r;
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity ent = e.nextElement();
            if(ent.isDeployed() && ent.hasGhostTargets(false)) {
                r = new Report(3630);
                r.addDesc(ent);
                int target = ent.getCrew().getPiloting() + 2;
                int roll = ent.getGhostTargetRoll();
                r.add(target);
                r.add(roll);
                if(roll >= target) {
                    r.choose(true);
                } else {
                    r.choose(false);
                }
                addReport(r);
            }
        }
    }

    private void resolveClearMinefield(Entity ent, Minefield mf) {

        if(null == mf || null == ent || ent.isDoomed() || ent.isDestroyed()) {
            return;
        }
        
        Coords pos = mf.getCoords();
        int clear = Minefield.CLEAR_NUMBER_INFANTRY;
        int boom = Minefield.CLEAR_NUMBER_INFANTRY_ACCIDENT;

        // Does the entity has a minesweeper?
        /*this is no longer the way this is handled in TacOps
         * for infantry, we need to check for specialized minesweeping
         * for vees, this is totally different
                for (Mounted mounted : ent.getMisc()) {
                    if (mounted.getType().hasFlag(MiscType.F_TOOLS) && mounted.getType().hasSubType(MiscType.S_MINESWEEPER)) {
                        int sweeperType = mounted.getType().getToHitModifier();
                        clear = Minefield.CLEAR_NUMBER_SWEEPER[sweeperType];
                        boom = Minefield.CLEAR_NUMBER_SWEEPER_ACCIDENT[sweeperType];
                        break;
                    }
                }
         */
        // mine clearing roll
        Report r = new Report(2245);
        r.subject = ent.getId();
        r.add(ent.getShortName(), true);
        r.add(Minefield.getDisplayableName(mf.getType()));
        r.add(pos.getBoardNum(), true);
        addReport(r);

        if(clearMinefield(mf, ent, clear, boom, vPhaseReport)) {
            removeMinefield(mf);
        }
        //some mines might have blown up
        resetMines();
    }        

    /**
     * Called during the fire phase to resolve all (and only) weapon attacks
     */
    private void resolveOnlyWeaponAttacks() {
        // loop thru received attack actions, getting attack handlers
        for (Enumeration<EntityAction> i = game.getActions(); i.hasMoreElements();) {
            EntityAction ea = i.nextElement();
            if (ea instanceof WeaponAttackAction) {
                WeaponAttackAction waa = (WeaponAttackAction) ea;
                Entity ae = game.getEntity(waa.getEntityId());
                Mounted m = ae.getEquipment(waa.getWeaponId());
                Weapon w = (Weapon) m.getType();
                AttackHandler ah = w.fire(waa, game, this);
                if (ah != null) {
                    game.addAttack(ah);
                }
            }
        }
        // and clear the attacks Vector
        game.resetActions();
    }

    /**
     * Trigger the indicated AP Pod of the entity.
     * 
     * @param entity
     *            the <code>Entity</code> triggering the AP Pod.
     * @param podId
     *            the <code>int</code> ID of the AP Pod.
     */
    private void triggerAPPod(Entity entity, int podId) {

        // Get the mount for this pod.
        Mounted mount = entity.getEquipment(podId);

        // Confirm that this is, indeed, an AP Pod.
        if (null == mount) {
            System.err.print("Expecting to find an AP Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.println(" but found NO equipment at all!!!");
            return;
        }
        EquipmentType equip = mount.getType();
        if (!(equip instanceof MiscType) || !equip.hasFlag(MiscType.F_AP_POD)) {
            System.err.print("Expecting to find an AP Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.print(" but found ");
            System.err.print(equip.getName());
            System.err.println(" instead!!!");
            return;
        }

        // Now confirm that the entity can trigger the pod.
        // Ignore the "used this round" flag.
        boolean oldFired = mount.isUsedThisRound();
        mount.setUsedThisRound(false);
        boolean canFire = mount.canFire();
        mount.setUsedThisRound(oldFired);
        if (!canFire) {
            System.err.print("Can not trigger the AP Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.println("!!!");
            return;
        }

        Report r;

        // Mark the pod as fired and log the action.
        mount.setFired(true);
        r = new Report(3010);
        r.newlines = 0;
        r.subject = entity.getId();
        r.addDesc(entity);
        addReport(r);

        // Walk through ALL entities in the triggering entity's hex.
        Enumeration<Entity> targets = game.getEntities(entity.getPosition());
        while (targets.hasMoreElements()) {
            final Entity target = targets.nextElement();

            // Is this an unarmored infantry platoon?
            if (target instanceof Infantry && !(target instanceof BattleArmor)) {

                // Roll d6-1 for damage.
                final int damage = Math.max(1, Compute.d6() - 1);

                // Damage the platoon.
                addReport(damageEntity(target, new HitData(Infantry.LOC_INFANTRY), damage));

                // Damage from AP Pods is applied immediately.
                target.applyDamage();

            } // End target-is-unarmored

            // Nope, the target is immune.
            // Don't make a log entry for the triggering entity.
            else if (!entity.equals(target)) {
                r = new Report(3020);
                r.indent(2);
                r.subject = target.getId();
                r.addDesc(target);
                addReport(r);
            }

        } // Check the next entity in the triggering entity's hex.
    }

    /**
     * Trigger the indicated B Pod of the entity.
     * 
     * @param entity
     *            the <code>Entity</code> triggering the B Pod.
     * @param podId
     *            the <code>int</code> ID of the B Pod.
     */
    private void triggerBPod(Entity entity, int podId, Entity target) {

        // Get the mount for this pod.
        Mounted mount = entity.getEquipment(podId);

        // Confirm that this is, indeed, an Anti-BA Pod.
        if (null == mount) {
            System.err.print("Expecting to find an B Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.println(" but found NO equipment at all!!!");
            return;
        }
        EquipmentType equip = mount.getType();
        if (!(equip instanceof WeaponType) || !equip.hasFlag(WeaponType.F_B_POD)) {
            System.err.print("Expecting to find an B Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.print(" but found ");
            System.err.print(equip.getName());
            System.err.println(" instead!!!");
            return;
        }

        // Now confirm that the entity can trigger the pod.
        // Ignore the "used this round" flag.
        boolean oldFired = mount.isUsedThisRound();
        mount.setUsedThisRound(false);
        boolean canFire = mount.canFire();
        mount.setUsedThisRound(oldFired);
        if (!canFire) {
            System.err.print("Can not trigger the B Pod at ");
            System.err.print(podId);
            System.err.print(" on the unit, ");
            System.err.print(entity.getDisplayName());
            System.err.println("!!!");
            return;
        }

        Report r;

        // Mark the pod as fired and log the action.
        mount.setFired(true);
        r = new Report(3011);
        r.newlines = 0;
        r.subject = entity.getId();
        r.addDesc(entity);
        addReport(r);


        // Is this an unarmored infantry platoon?
        if (target instanceof Infantry && !(target instanceof BattleArmor)) {

            // Roll d6 for damage.
            final int damage = Compute.d6();

            // Damage the platoon.
            addReport(damageEntity(target, new HitData(Infantry.LOC_INFANTRY), damage));

            // Damage from AP Pods is applied immediately.
            target.applyDamage();

            // End target-is-unarmored
        } else if ( target instanceof BattleArmor ){
            // 20 damage in 5 point clusters
            final int damage = 5;

            // Damage the squad.
            addReport(damageEntity(target, target.rollHitLocation(0,0), damage));
            addReport(damageEntity(target, target.rollHitLocation(0,0), damage));
            addReport(damageEntity(target, target.rollHitLocation(0,0), damage));
            addReport(damageEntity(target, target.rollHitLocation(0,0), damage));

            // Damage from B Pods is applied immediately.
            target.applyDamage();
        }

        // Nope, the target is immune.
        // Don't make a log entry for the triggering entity.
        else if (!entity.equals(target)) {
            r = new Report(3020);
            r.indent(2);
            r.subject = target.getId();
            r.addDesc(target);
            addReport(r);
        }
    }

    /**
     * Resolve an Unjam Action object
     */
    private void resolveUnjam(Entity entity) {
        Report r;
        final int TN = entity.getCrew().getGunnery() + 3;
        r = new Report(3025);
        r.subject = entity.getId();
        r.addDesc(entity);
        addReport(r);
        for (Mounted mounted : entity.getTotalWeaponList()) {
            if (mounted.isJammed()) {
                WeaponType wtype = (WeaponType) mounted.getType();
                if (wtype.getAmmoType() == AmmoType.T_AC_ROTARY) {
                    int roll = Compute.d6(2);
                    r = new Report(3030);
                    r.indent();
                    r.subject = entity.getId();
                    r.add(wtype.getName());
                    r.add(TN);
                    r.add(roll);
                    if (roll >= TN) {
                        r.choose(true);
                        mounted.setJammed(false);
                    } else {
                        r.choose(false);
                    }
                    addReport(r);
                }
            }
        }
    }

    private void resolveFindClub(Entity entity) {
        EquipmentType clubType = null;

        entity.setFindingClub(true);

        // Get the entity's current hex.
        Coords coords = entity.getPosition();
        IHex curHex = game.getBoard().getHex(coords);

        Report r;

        // Is there a blown off arm in the hex?
        if (curHex.terrainLevel(Terrains.ARMS) > 0) {
            clubType = EquipmentType.get("Limb Club");
            curHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ARMS, curHex.terrainLevel(Terrains.ARMS) - 1));
            sendChangedHex(entity.getPosition());
            r = new Report(3035);
            r.subject = entity.getId();
            r.addDesc(entity);
            addReport(r);
        }
        // Is there a blown off leg in the hex?
        else if (curHex.terrainLevel(Terrains.LEGS) > 0) {
            clubType = EquipmentType.get("Limb Club");
            curHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.LEGS, curHex.terrainLevel(Terrains.LEGS) - 1));
            sendChangedHex(entity.getPosition());
            r = new Report(3040);
            r.subject = entity.getId();
            r.addDesc(entity);
            addReport(r);
        }

        // Is there the rubble of a medium, heavy,
        // or hardened building in the hex?
        else if (Building.LIGHT < curHex.terrainLevel(Terrains.RUBBLE)) {

            // Finding a club is not guaranteed. The chances are
            // based on the type of building that produced the
            // rubble.
            boolean found = false;
            int roll = Compute.d6(2);
            switch (curHex.terrainLevel(Terrains.RUBBLE)) {
            case Building.MEDIUM:
                if (roll >= 7) {
                    found = true;
                }
                break;
            case Building.HEAVY:
                if (roll >= 6) {
                    found = true;
                }
                break;
            case Building.HARDENED:
                if (roll >= 5) {
                    found = true;
                }
                break;
            case Building.WALL:
                if (roll >= 13) {
                    found = true;
                }
                break;
            default:
                //we must be in ultra
                if(roll >= 4) {
                    found = true;
                }
            }

            // Let the player know if they found a club.
            if (found) {
                clubType = EquipmentType.get("Girder Club");
                r = new Report(3045);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
            } else {
                // Sorry, no club for you.
                r = new Report(3050);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
            }
        }

        // Are there woods in the hex?
        else if (curHex.containsTerrain(Terrains.WOODS) || curHex.containsTerrain(Terrains.JUNGLE)) {
            clubType = EquipmentType.get("Tree Club");
            r = new Report(3055);
            r.subject = entity.getId();
            r.addDesc(entity);
            addReport(r);
        }

        // add the club
        try {
            if (clubType != null) {
                entity.addEquipment(clubType, Entity.LOC_NONE);
            }
        } catch (LocationFullException ex) {
            // unlikely...
            r = new Report(3060);
            r.subject = entity.getId();
            r.addDesc(entity);
            addReport(r);
        }
    }

    /**
     * Try to ignite the hex, taking into account exisiting fires and the
     * effects of Inferno rounds.
     * 
     * @param c -
     *            the <code>Coords</code> of the hex being lit.
     * @param entityId -
     *            the <code>int</code> id of the entity involved.
     * @param bInferno -
     *            <code>true</code> if the weapon igniting the hex is an
     *            Inferno round. If some other weapon or ammo is causing the
     *            roll, this should be <code>false</code>.
     * @param bHotGun -
     *            <code>true</code> if the weapon is plasma/flamer/incendiary
     *            LRM/etc
     * @param nTargetRoll -
     *            the <code>TargetRoll</code> for the ignition roll.
     * @param bReportAttempt -
     *            <code>true</code> if the attempt roll should be added to the
     *            report.
     * @param accidentTarget -
     *            <code>int</code> the target number below which a roll has to
     *            be made in order to try igniting a hex accidently. -1 for
     *            intentional
     */
    public boolean tryIgniteHex(Coords c, int entityId, boolean bHotGun, boolean bInferno, TargetRoll nTargetRoll, 
            boolean bReportAttempt, int accidentTarget, Vector<Report> vPhaseReport) {

        IHex hex = game.getBoard().getHex(c);
        Report r;

        // Ignore bad coordinates.
        if (hex == null) {
            return false;
        }

        // Ignore if fire is not enabled as a game option
        if (!game.getOptions().booleanOption("tacops_start_fire")) {
            return false;
        }

        // is the hex ignitable (how are infernos handled?)
        if (!hex.isIgnitable() && !bInferno) {
            return false;
        }

        // first for accidental ignitions, make the necessary roll
        if (accidentTarget > -1) {
            // if this hex is in snow, then accidental ignitions are not
            // possible
            if (hex.containsTerrain(Terrains.SNOW)) {
                return false;
            }
            nTargetRoll.addModifier(2, "accidental");
            int accidentRoll = Compute.d6(2);
            r = new Report(3066);
            r.subject = entityId;
            r.add(accidentTarget);
            r.add(accidentRoll);
            r.indent(2);
            if (accidentRoll > accidentTarget) {
                r.choose(false);
                vPhaseReport.add(r);
                return false;
            } else {
                r.choose(true);
                vPhaseReport.add(r);
            }
        }

        int terrainMod = hex.getIgnitionModifier();
        if (terrainMod != 0) {
            nTargetRoll.addModifier(terrainMod, "terrain");
        }

        // building modifiers
        Building bldg = game.getBoard().getBuildingAt(c);
        if (null != bldg) {
            nTargetRoll.addModifier(bldg.getType() - 1, "building");
        }

        // add in any modifiers for planetary conditions
        int weatherMod = game.getPlanetaryConditions().getIgniteModifiers();
        if (weatherMod != 0) {
            nTargetRoll.addModifier(weatherMod, "conditions");
        }

        // if there is snow on the ground and this a hotgun or inferno, it may
        // melt the snow instead
        if ((hex.containsTerrain(Terrains.SNOW) || hex.containsTerrain(Terrains.ICE)) && (bHotGun || bInferno)) {
            boolean melted = false;
            int meltCheck = Compute.d6(2);
            if (hex.terrainLevel(Terrains.SNOW) > 1 && meltCheck == 12) {
                melted = true;
            } else if (hex.containsTerrain(Terrains.ICE) && meltCheck > 9) {
                melted = true;
            } else if (hex.containsTerrain(Terrains.SNOW) && meltCheck > 7) {
                melted = true;
            }
            if (bInferno) {
                melted = true;
            }
            if (melted) {
                vPhaseReport.addAll(meltIceAndSnow(c, entityId));
                return false;
            }

        }

        // inferno always ignites
        // ERRATA not if targeting clear hexes for ignition is disabled.
        if (bInferno && !game.getOptions().booleanOption("no_ignite_clear")) {
            nTargetRoll = new TargetRoll(0, "inferno");
        }

        // no lighting fires in tornados
        if (game.getPlanetaryConditions().getWindStrength() > PlanetaryConditions.WI_STORM) {
            nTargetRoll = new TargetRoll(TargetRoll.AUTOMATIC_FAIL, "tornado");
        }

        // The hex may already be on fire.
        if (hex.containsTerrain(Terrains.FIRE)) {
            if (bReportAttempt) {
                r = new Report(3065);
                r.indent(2);
                r.subject = entityId;
                vPhaseReport.add(r);
            }
        } else if(checkIgnition(c, nTargetRoll, bInferno, entityId, vPhaseReport)){
            return true;
        }
        return false;
    }

    /**
     * Try to ignite the hex, taking into account exisiting fires and the
     * effects of Inferno rounds. This version of the method will not report the
     * attempt roll.
     * 
     * @param c -
     *            the <code>Coords</code> of the hex being lit.
     * @param entityId -
     *            the <code>int</code> id of the entity involved.
     * @param bInferno -
     *            <code>true</code> if the weapon igniting the hex is an
     *            Inferno round. If some other weapon or ammo is causing the
     *            roll, this should be <code>false</code>.
     * @param nTargetRoll -
     *            the <code>int</code> roll target for the attempt.
     */
    public boolean tryIgniteHex(Coords c, int entityId, boolean bHotGun, boolean bInferno, TargetRoll nTargetRoll, int accidentTarget, Vector<Report> vPhaseReport) {
        return tryIgniteHex(c, entityId, bHotGun, bInferno, nTargetRoll, false, accidentTarget, vPhaseReport);
    }

    public Vector<Report> tryClearHex(Coords c, int nDamage, int entityId) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        IHex h = game.getBoard().getHex(c);
        if (h == null)
            return vPhaseReport;
        ITerrain woods = h.getTerrain(Terrains.WOODS);
        ITerrain jungle = h.getTerrain(Terrains.JUNGLE);
        ITerrain ice = h.getTerrain(Terrains.ICE);
        Report r;
        if (woods != null) {
            int tf = woods.getTerrainFactor() - nDamage;
            int level = woods.getLevel();
            if (tf <= 0) {
                h.removeTerrain(Terrains.WOODS);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ROUGH, 1));
                // light converted to rough
                r = new Report(3090);
                r.subject = entityId;
                vPhaseReport.add(r);
            } else if (tf <= 50 && level > 1) {
                h.removeTerrain(Terrains.WOODS);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.WOODS, 1));
                woods = h.getTerrain(Terrains.WOODS);
                // heavy converted to light
                r = new Report(3085);
                r.subject = entityId;
                vPhaseReport.add(r);
            } else if (tf <= 90 && level > 2) {
                h.removeTerrain(Terrains.WOODS);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.WOODS, 2));
                woods = h.getTerrain(Terrains.WOODS);
                // ultra heavy converted to heavy
                r = new Report(3082);
                r.subject = entityId;
                vPhaseReport.add(r);
            }
            woods.setTerrainFactor(tf);
        }
        if (jungle != null) {
            int tf = jungle.getTerrainFactor() - nDamage;
            int level = jungle.getLevel();
            if (tf < 0) {
                h.removeTerrain(Terrains.JUNGLE);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ROUGH, 1));
                // light converted to rough
                r = new Report(3091);
                r.subject = entityId;
                vPhaseReport.add(r);
            } else if (tf <= 50 && level > 1) {
                h.removeTerrain(Terrains.JUNGLE);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.JUNGLE, 1));
                jungle = h.getTerrain(Terrains.JUNGLE);
                // heavy converted to light
                r = new Report(3086);
                r.subject = entityId;
                vPhaseReport.add(r);
            } else if (tf <= 90 && level > 2) {
                h.removeTerrain(Terrains.JUNGLE);
                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.JUNGLE, 2));
                jungle = h.getTerrain(Terrains.JUNGLE);
                // ultra heavy converted to heavy
                r = new Report(3083);
                r.subject = entityId;
                vPhaseReport.add(r);
            }
            jungle.setTerrainFactor(tf);
        }
        if (ice != null) {
            int tf = ice.getTerrainFactor() - nDamage;
            if (tf <= 0) {
                // ice melted
                r = new Report(3092);
                r.subject = entityId;
                vPhaseReport.add(r);
                vPhaseReport.addAll(resolveIceBroken(c));
            } else {
                ice.setTerrainFactor(tf);
            }
        }
        sendChangedHex(c);
        
        //any attempt to clear an heavy industrial hex may cause an exposion
        checkExplodeIndustrialZone(c, vPhaseReport);
        
        return vPhaseReport;
    }

    /**
     * Handle all physical attacks for the round
     */
    private void resolvePhysicalAttacks() {
        // Physical phase header
        addReport(new Report(4000, Report.PUBLIC));

        // add any pending charges
        for (Enumeration<AttackAction> i = game.getCharges(); i.hasMoreElements();) {
            game.addAction(i.nextElement());
        }
        game.resetCharges();

        // add any pending rams
        for (Enumeration<AttackAction> i = game.getRams(); i.hasMoreElements();) {
            game.addAction(i.nextElement());
        }
        game.resetRams();

        // add any pending Tele Missile Attacks
        for (Enumeration<AttackAction> i = game.getTeleMissileAttacks(); i.hasMoreElements();) {
            game.addAction(i.nextElement());
        }
        game.resetTeleMissileAttacks();

        // remove any duplicate attack declarations
        cleanupPhysicalAttacks();

        // loop thru received attack actions
        for (Enumeration<EntityAction> i = game.getActions(); i.hasMoreElements();) {
            Object o = i.nextElement();
            // verify that the attacker is still active
            AttackAction aa = (AttackAction) o;
            if (!game.getEntity(aa.getEntityId()).isActive() && !(o instanceof DfaAttackAction)) {
                continue;
            }
            AbstractAttackAction aaa = (AbstractAttackAction) o;
            // do searchlights immediately
            if (aaa instanceof SearchlightAttackAction) {
                SearchlightAttackAction saa = (SearchlightAttackAction) aaa;
                addReport(saa.resolveAction(game));
            } else {
                physicalResults.addElement(preTreatPhysicalAttack(aaa));
            }
        }
        int cen = Entity.NONE;
        for (Enumeration<PhysicalResult> i = physicalResults.elements(); i.hasMoreElements();) {
            PhysicalResult pr = i.nextElement();
            resolvePhysicalAttack(pr, cen);
            cen = pr.aaa.getEntityId();
        }
        physicalResults.removeAllElements();
    }

    /**
     * Cleans up the attack declarations for the physical phase by removing all
     * attacks past the first for any one mech. Also clears out attacks by dead
     * or disabled mechs.
     */
    private void cleanupPhysicalAttacks() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity entity = i.nextElement();
            removeDuplicateAttacks(entity.getId());
        }
        removeDeadAttacks();
    }

    /**
     * Removes any actions in the attack queue beyond the first by the specified
     * entity.
     */
    private void removeDuplicateAttacks(int entityId) {
        boolean attacked = false;
        Vector<EntityAction> toKeep = new Vector<EntityAction>(/* game.actionsSize() */);

        for (Enumeration<EntityAction> i = game.getActions(); i.hasMoreElements();) {
            EntityAction action = i.nextElement();
            if (action.getEntityId() != entityId) {
                toKeep.addElement(action);
            } else if (!attacked) {
                toKeep.addElement(action);
                if (!(action instanceof SearchlightAttackAction)) {
                    attacked = true;
                }
            } else {
                System.err.println("server: removing duplicate phys attack for id#" + entityId);
                System.err.println("        action was " + action.toString());
            }
        }

        // reset actions and re-add valid elements
        game.resetActions();
        for (Enumeration<EntityAction> i = toKeep.elements(); i.hasMoreElements();) {
            game.addAction(i.nextElement());
        }
    }

    /**
     * Removes all attacks by any dead entities. It does this by going through
     * all the attacks and only keeping ones from active entities. DFAs are kept
     * even if the pilot is unconscious, so that he can fail.
     */
    private void removeDeadAttacks() {
        Vector<EntityAction> toKeep = new Vector<EntityAction>(game.actionsSize());

        for (Enumeration<EntityAction> i = game.getActions(); i.hasMoreElements();) {
            EntityAction action = i.nextElement();
            Entity entity = game.getEntity(action.getEntityId());
            if (entity != null && !entity.isDestroyed() && (entity.isActive() || action instanceof DfaAttackAction)) {
                toKeep.addElement(action);
            }
        }

        // reset actions and re-add valid elements
        game.resetActions();
        for (Enumeration<EntityAction> i = toKeep.elements(); i.hasMoreElements();) {
            game.addAction(i.nextElement());
        }
    }

    /**
     * Handle a punch attack
     */
    private void resolvePunchAttack(PhysicalResult pr, int lastEntityId) {
        final PunchAttackAction paa = (PunchAttackAction) pr.aaa;
        final Entity ae = game.getEntity(paa.getEntityId());
        final Targetable target = game.getTarget(paa.getTargetType(), paa.getTargetId());
        Entity te = null;
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        final String armName = paa.getArm() == PunchAttackAction.LEFT ? "Left Arm" : "Right Arm";
        // get damage, ToHitData and roll from the PhysicalResult
        int damage = paa.getArm() == PunchAttackAction.LEFT ? pr.damage : pr.damageRight;
        final ToHitData toHit = paa.getArm() == PunchAttackAction.LEFT ? pr.toHit : pr.toHitRight;
        int roll = paa.getArm() == PunchAttackAction.LEFT ? pr.roll : pr.rollRight;
        final boolean targetInBuilding = Compute.isInBuilding(game, te);
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        Report r;
        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        if (lastEntityId != paa.getEntityId()) {
            // report who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4010);
        r.subject = ae.getId();
        r.indent();
        r.add(armName);
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4015);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4020);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            r.newlines = 0;
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // nope
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);

            // If the target is in a building, the building absorbs the damage.
            if (targetInBuilding && bldg != null) {

                // Only report if damage was done to the building.
                if (damage > 0) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                    for (Report report : buildingReport) {
                        report.subject = ae.getId();
                    }
                    addReport(buildingReport);
                }

            }
            return;
        }

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // And we're done!
            return;
        }

        HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
        r = new Report(4045);
        r.subject = ae.getId();
        r.add(toHit.getTableDesc());
        r.add(te.getLocationAbbr(hit));
        r.newlines = 0;
        addReport(r);

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        if (targetInBuilding && bldg != null) {
            int bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(target.getPosition()) / 10.0);
            int toBldg = Math.min(bldgAbsorbs, damage);
            damage -= toBldg;
            addNewLines();
            Vector<Report> buildingReport = damageBuilding(bldg, toBldg, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);
        }

        // A building may absorb the entire shot.
        if (damage == 0) {
            r = new Report(4050);
            r.subject = ae.getId();
            r.add(te.getShortName());
            r.add(te.getOwner().getName());
            r.newlines = 0;
            addReport(r);
        } else {
            if (glancing) {
                damage = (int) Math.floor(damage / 2.0);
            }
            if (directBlow) {
                damage += toHit.getMoS() / 3;
                hit.makeDirectBlow(toHit.getMoS() / 3);
            }

            if (damage >= 1 && te.hasWorkingMisc(MiscType.F_SPIKES, -1, hit.getLocation())) {
                r = new Report(4330);
                r.indent(2);
                r.newlines = 0;
                r.subject = ae.getId();
                addReport(r);
                checkBreakSpikes(te, hit.getLocation());
                damage = Math.max(1, damage - 4);
                HitData ahit;
                if (paa.getArm() == PunchAttackAction.LEFT)
                    ahit = new HitData(Mech.LOC_LARM);
                else
                    ahit = new HitData(Mech.LOC_RARM);
                addReport(damageEntity(ae, ahit, 2, false, DamageType.NONE, false, false, false));
            }
            DamageType damageType = DamageType.NONE;
            addReport(damageEntity(te, hit, damage, false, damageType, false, false, throughFront));
            if (target instanceof VTOL) {
                // destroy rotor
                addReport(applyCriticalHit(te, VTOL.LOC_ROTOR, new CriticalSlot(CriticalSlot.TYPE_SYSTEM, VTOL.CRIT_ROTOR_DESTROYED), false));
            }
            //check for extending retractable blades
            if(paa.isBladeExtended(paa.getArm())) {
                addNewLines();
                r = new Report(4455);
                r.indent(2);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
                //conventional infantry don't take crits and battle armor need to be handled differently
                if(!(target instanceof Infantry)) {
                    addNewLines();
                    addReport(criticalEntity(te, hit.getLocation(), 0, true, false));
                }
                if(target instanceof BattleArmor 
                        && hit.getLocation() < te.locations() && te.getInternal(hit.getLocation()) > 0) {
                    //TODO: we should really apply BA criticals through the critical
                    //hits methods. Right now they are applied in damageentity
                    HitData bahit = new HitData(hit.getLocation(), false, HitData.EFFECT_CRITICAL);
                    addReport(damageEntity(te,bahit,0));
                }
                //extend the blade
                //since retracting/extending is a freebie in the movement phase, lets assume that the
                //blade retracts to its original mode
                //ae.extendBlade(paa.getArm());
                //check for breaking a nail
                if(Compute.d6(2) > 9) {
                    int armLoc = (paa.getArm() == PunchAttackAction.RIGHT) ? Mech.LOC_RARM : Mech.LOC_LARM;
                    addNewLines();
                    r = new Report(4456);
                    r.indent(2);
                    r.subject = ae.getId();
                    r.newlines = 0;
                    addReport(r);
                    ae.destroyRetractableBlade(armLoc);
                }
            }
                
            
            
        }

        addNewLines();
    }

    /**
     * Handle a kick attack
     */
    private void resolveKickAttack(PhysicalResult pr, int lastEntityId) {
        KickAttackAction kaa = (KickAttackAction) pr.aaa;
        final Entity ae = game.getEntity(kaa.getEntityId());
        final Targetable target = game.getTarget(kaa.getTargetType(), kaa.getTargetId());
        Entity te = null;
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        String legName = kaa.getLeg() == KickAttackAction.LEFT || kaa.getLeg() == KickAttackAction.LEFTMULE ? "Left " : "Right ";
        if (kaa.getLeg() == KickAttackAction.LEFTMULE || kaa.getLeg() == KickAttackAction.RIGHTMULE) {
            legName.concat(" rear ");
        } else {
            legName.concat(" front ");
        }
        legName.concat("leg");
        Report r;

        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final boolean targetInBuilding = Compute.isInBuilding(game, te);
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        if (lastEntityId != ae.getId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4055);
        r.subject = ae.getId();
        r.indent();
        r.add(legName);
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4060);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            game.addPSR(new PilotingRollData(ae.getId(), 0, "missed a kick"));
            return;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4065);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            r.newlines = 0;
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            game.addPSR(new PilotingRollData(ae.getId(), 0, "missed a kick"));

            // If the target is in a building, the building absorbs the damage.
            if (targetInBuilding && bldg != null) {

                // Only report if damage was done to the building.
                if (damage > 0) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                    for (Report report : buildingReport) {
                        report.subject = ae.getId();
                    }
                    addReport(buildingReport);
                }

            }
            return;
        }

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // And we're done!
            return;
        }

        HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
        r = new Report(4045);
        r.subject = ae.getId();
        r.add(toHit.getTableDesc());
        r.add(te.getLocationAbbr(hit));
        r.newlines = 0;
        addReport(r);

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        if (targetInBuilding && bldg != null) {
            int bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(target.getPosition()) / 10.0);
            int toBldg = Math.min(bldgAbsorbs, damage);
            damage -= toBldg;
            addNewLines();
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);
        }

        // A building may absorb the entire shot.
        if (damage == 0) {
            r = new Report(4050);
            r.subject = ae.getId();
            r.add(te.getShortName());
            r.add(te.getOwner().getName());
            r.newlines = 0;
            addReport(r);
        } else {
            if (glancing) {
                damage = (int) Math.floor(damage / 2.0);
            }
            if (directBlow) {
                damage += toHit.getMoS() / 3;
                hit.makeDirectBlow(toHit.getMoS() / 3);
            }

            if (damage >= 1 && te.hasWorkingMisc(MiscType.F_SPIKES, -1, hit.getLocation())) {
                r = new Report(4330);
                r.indent(2);
                r.newlines = 0;
                r.subject = ae.getId();
                addReport(r);
                checkBreakSpikes(te, hit.getLocation());
                damage = Math.max(1, damage - 4);
                HitData ahit;
                switch (kaa.getLeg()) {
                case KickAttackAction.LEFT:
                    if (ae instanceof QuadMech)
                        ahit = new HitData(Mech.LOC_LARM);
                    else
                        ahit = new HitData(Mech.LOC_LLEG);
                    break;
                case KickAttackAction.RIGHT:
                    if (ae instanceof QuadMech)
                        ahit = new HitData(Mech.LOC_RARM);
                    else
                        ahit = new HitData(Mech.LOC_RLEG);
                    break;
                case KickAttackAction.LEFTMULE:
                    ahit = new HitData(Mech.LOC_LLEG);
                    break;
                case KickAttackAction.RIGHTMULE:
                default:
                    ahit = new HitData(Mech.LOC_RLEG);
                    break;
                }
                addReport(damageEntity(ae, ahit, 2, false, DamageType.NONE, false, false, false));
            }
            DamageType damageType = DamageType.NONE;
            addReport(damageEntity(te, hit, damage, false, damageType, false, false, throughFront));
            if (target instanceof VTOL) {
                // destroy rotor
                addReport(applyCriticalHit(te, VTOL.LOC_ROTOR, new CriticalSlot(CriticalSlot.TYPE_SYSTEM, VTOL.CRIT_ROTOR_DESTROYED), false));
            }
        }

        if (te.getMovementMode() == IEntityMovementMode.BIPED || te.getMovementMode() == IEntityMovementMode.QUAD) {
            PilotingRollData kickPRD = getKickPushPSR(te, ae, te, "was kicked");
            kickPRD.setCumulative(false); // see Bug# 811987 for more info
            game.addPSR(kickPRD);
        }

        addNewLines();
    }

    /**
     * Handle a kick attack
     */
    private void resolveJumpJetAttack(PhysicalResult pr, int lastEntityId) {
        JumpJetAttackAction kaa = (JumpJetAttackAction) pr.aaa;
        final Entity ae = game.getEntity(kaa.getEntityId());
        final Targetable target = game.getTarget(kaa.getTargetType(), kaa.getTargetId());
        Entity te = null;
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        String legName = null;
        switch (kaa.getLeg()) {
        case JumpJetAttackAction.LEFT:
            legName = "Left leg";
            break;
        case JumpJetAttackAction.RIGHT:
            legName = "Right leg";
            break;
        default:
            legName = "Both legs";
            break;
        }

        Report r;

        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final boolean targetInBuilding = Compute.isInBuilding(game, te);
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        if (lastEntityId != ae.getId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4290);
        r.subject = ae.getId();
        r.indent();
        r.add(legName);
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4075);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4080);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            r.newlines = 0;
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);

            // If the target is in a building, the building absorbs the damage.
            if (targetInBuilding && bldg != null) {

                damage += pr.damageRight;
                // Only report if damage was done to the building.
                if (damage > 0) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                    for (Report report : buildingReport) {
                        report.subject = ae.getId();
                    }
                    addReport(buildingReport);
                }

            }
            return;
        }

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            damage += pr.damageRight;
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // And we're done!
            return;
        }

        r = new Report(4040);
        r.subject = ae.getId();
        r.newlines = 0;
        addReport(r);

        for (int leg = 0; leg < 2; leg++) {
            if (leg == 1) {
                damage = pr.damageRight;
                if (damage == 0)
                    break;
            }
            HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
            hit.setGeneralDamageType(HitData.DAMAGE_ENERGY);

            // The building shields all units from a certain amount of damage.
            // The amount is based upon the building's CF at the phase's start.
            if (targetInBuilding && bldg != null) {
                int bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(target.getPosition()) / 10.0);
                int toBldg = Math.min(bldgAbsorbs, damage);
                damage -= toBldg;
                addNewLines();
                Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                for (Report report : buildingReport) {
                    report.subject = ae.getId();
                }
                addReport(buildingReport);
            }

            // A building may absorb the entire shot.
            if (damage == 0) {
                r = new Report(4050);
                r.subject = ae.getId();
                r.add(te.getShortName());
                r.add(te.getOwner().getName());
                r.newlines = 0;
                addReport(r);
            } else {
                if (glancing) {
                    damage = (int) Math.floor(damage / 2.0);
                }
                if (directBlow) {
                    damage += toHit.getMoS() / 3;
                    hit.makeDirectBlow(toHit.getMoS() / 3);
                }
                addReport(damageEntity(te, hit, damage, false, DamageType.NONE, false, false, throughFront));
            }
        }

        addNewLines();
    }

    /**
     * Handle a Protomech physicalattack
     */

    private void resolveProtoAttack(PhysicalResult pr, int lastEntityId) {
        final ProtomechPhysicalAttackAction ppaa = (ProtomechPhysicalAttackAction) pr.aaa;
        final Entity ae = game.getEntity(ppaa.getEntityId());
        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final Targetable target = game.getTarget(ppaa.getTargetType(), ppaa.getTargetId());
        Entity te = null;
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        final boolean targetInBuilding = Compute.isInBuilding(game, te);
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();
        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        Report r;

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        if (lastEntityId != ae.getId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4070);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4075);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4080);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            r.newlines = 0;
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);

            // If the target is in a building, the building absorbs the damage.
            if (targetInBuilding && bldg != null) {

                // Only report if damage was done to the building.
                if (damage > 0) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                    for (Report report : buildingReport) {
                        report.subject = ae.getId();
                    }
                    addReport(buildingReport);
                }

            }
            return;
        }

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // And we're done!
            return;
        }

        HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);

        r = new Report(4045);
        r.subject = ae.getId();
        r.add(toHit.getTableDesc());
        r.add(te.getLocationAbbr(hit));
        r.newlines = 0;
        addReport(r);

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        if (targetInBuilding && bldg != null) {
            int bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(target.getPosition()) / 10.0);
            int toBldg = Math.min(bldgAbsorbs, damage);
            damage -= toBldg;
            addNewLines();
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);
        }

        // A building may absorb the entire shot.
        if (damage == 0) {
            r = new Report(4050);
            r.subject = ae.getId();
            r.add(te.getShortName());
            r.add(te.getOwner().getName());
            r.newlines = 0;
            addReport(r);
        } else {
            if (glancing) {
                damage = (int) Math.floor(damage / 2.0);
            }
            if (directBlow) {
                damage += toHit.getMoS() / 3;
                hit.makeDirectBlow(toHit.getMoS() / 3);
            }
            addReport(damageEntity(te, hit, damage, false, DamageType.NONE, false, false, throughFront));
        }

        addNewLines();
    }

    /**
     * Handle a brush off attack
     */
    private void resolveBrushOffAttack(PhysicalResult pr, int lastEntityId) {
        final BrushOffAttackAction baa = (BrushOffAttackAction) pr.aaa;
        final Entity ae = game.getEntity(baa.getEntityId());
        // PLEASE NOTE: buildings are *never* the target
        // of a "brush off", but iNarc pods **are**.
        Targetable target = game.getTarget(baa.getTargetType(), baa.getTargetId());
        Entity te = null;
        final String armName = baa.getArm() == BrushOffAttackAction.LEFT ? "Left Arm" : "Right Arm";
        Report r;

        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = game.getEntity(baa.getTargetId());
        }

        // get damage, ToHitData and roll from the PhysicalResult
        // ASSUMPTION: buildings can't absorb *this* damage.
        int damage = baa.getArm() == BrushOffAttackAction.LEFT ? pr.damage : pr.damageRight;
        final ToHitData toHit = baa.getArm() == BrushOffAttackAction.LEFT ? pr.toHit : pr.toHitRight;
        int roll = baa.getArm() == BrushOffAttackAction.LEFT ? pr.roll : pr.rollRight;

        if (lastEntityId != baa.getEntityId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4085);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.add(armName);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4090);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // report the roll
        r = new Report(4025);
        r.subject = ae.getId();
        r.add(toHit.getValue());
        r.add(roll);
        r.newlines = 0;
        addReport(r);

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);

            // Missed Brush Off attacks cause punch damage to the attacker.
            toHit.setHitTable(ToHitData.HIT_PUNCH);
            toHit.setSideTable(ToHitData.SIDE_FRONT);
            HitData hit = ae.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            r = new Report(4095);
            r.subject = ae.getId();
            r.addDesc(ae);
            r.add(ae.getLocationAbbr(hit));
            r.newlines = 0;
            addReport(r);
            addReport(damageEntity(ae, hit, damage));
            addNewLines();
            return;
        }

        // Different target types get different handling.
        switch (target.getTargetType()) {
        case Targetable.TYPE_ENTITY:
            // Handle Entity targets.
            HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            r = new Report(4045);
            r.subject = ae.getId();
            r.add(toHit.getTableDesc());
            r.add(te.getLocationAbbr(hit));
            r.newlines = 0;
            addReport(r);
            addReport(damageEntity(te, hit, damage));
            addNewLines();

            // Dislodge the swarming infantry.
            ae.setSwarmAttackerId(Entity.NONE);
            te.setSwarmTargetId(Entity.NONE);
            r = new Report(4100);
            r.subject = ae.getId();
            r.add(te.getDisplayName());
            addReport(r);
            break;
        case Targetable.TYPE_INARC_POD:
            // Handle iNarc pod targets.
            // TODO : check the return code and handle false appropriately.
            ae.removeINarcPod((INarcPod) target);
            // // TODO : confirm that we don't need to update the attacker.
            // //killme
            // entityUpdate( ae.getId() ); // killme
            r = new Report(4105);
            r.subject = ae.getId();
            r.add(target.getDisplayName());
            addReport(r);
            break;
        // TODO : add a default: case and handle it appropriately.
        }
    }

    /**
     * Handle a thrash attack
     */
    private void resolveThrashAttack(PhysicalResult pr, int lastEntityId) {
        final ThrashAttackAction taa = (ThrashAttackAction) pr.aaa;
        final Entity ae = game.getEntity(taa.getEntityId());

        // get damage, ToHitData and roll from the PhysicalResult
        int hits = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        // PLEASE NOTE: buildings are *never* the target of a "thrash".
        final Entity te = game.getEntity(taa.getTargetId());
        Report r;

        if (lastEntityId != taa.getEntityId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4110);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4115);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // Thrash attack may hit automatically
        if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4120);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);

            // do we hit?
            if (roll < toHit.getValue()) {
                // miss
                r = new Report(4035);
                r.subject = ae.getId();
                addReport(r);
                return;
            }
            r = new Report(4125);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        }

        // Standard damage loop in 5 point clusters.
        if (glancing) {
            hits = (int) Math.floor(hits / 2.0);
        }
        if (directBlow) {
            hits += toHit.getMoS() / 3;
        }

        r = new Report(4130);
        r.subject = ae.getId();
        r.add(hits);
        r.newlines = 0;
        addReport(r);
        if (glancing) {
            r = new Report(4030);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        }
        if (directBlow) {
            r = new Report(4032);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        }

        while (hits > 0) {
            int damage = Math.min(5, hits);
            hits -= damage;
            HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            r = new Report(4135);
            r.subject = ae.getId();
            r.add(te.getLocationAbbr(hit));
            r.newlines = 0;
            addReport(r);
            addReport(damageEntity(te, hit, damage));
        }

        addNewLines();

        // Thrash attacks cause PSRs. Failed PSRs cause falling damage.
        // This fall damage applies even though the Thrashing Mek is prone.
        PilotingRollData rollData = ae.getBasePilotingRoll();
        ae.addPilotingModifierForTerrain(rollData);
        rollData.addModifier(0, "thrashing at infantry");
        r = new Report(4140);
        r.subject = ae.getId();
        r.addDesc(ae);
        addReport(r);
        final int diceRoll = Compute.d6(2);
        r = new Report(2190);
        r.subject = ae.getId();
        r.add(rollData.getValueAsString());
        r.add(rollData.getDesc());
        r.add(diceRoll);
        if (diceRoll < rollData.getValue()) {
            r.choose(false);
            addReport(r);
            addReport(doEntityFall(ae, rollData));
        } else {
            r.choose(true);
            addReport(r);
        }
    }
    
    /**
     * Handle a thrash attack
     */
    private void resolveBAVibroClawAttack(PhysicalResult pr, int lastEntityId) {
        final BAVibroClawAttackAction bvaa = (BAVibroClawAttackAction) pr.aaa;
        final Entity ae = game.getEntity(bvaa.getEntityId());

        // get damage, ToHitData and roll from the PhysicalResult
        int hits = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        // PLEASE NOTE: buildings are *never* the target of a BA vibroclaw attack.
        final Entity te = game.getEntity(bvaa.getTargetId());
        Report r;

        if (lastEntityId != bvaa.getEntityId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4146);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4147);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // we may hit automatically
        if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4120);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);

            // do we hit?
            if (roll < toHit.getValue()) {
                // miss
                r = new Report(4035);
                r.subject = ae.getId();
                addReport(r);
                return;
            }
        }

        // Standard damage loop
        if (glancing) {
            hits = (int) Math.floor(hits / 2.0);
        }
        if (directBlow) {
            hits += toHit.getMoS() / 3;
        }
        if (te instanceof Infantry && !(te instanceof BattleArmor)) {
            r = new Report(4149);
            r.subject = ae.getId();
        } else {
            r = new Report(4148);
            r.subject = ae.getId();
            r.add(hits);
            r.add(ae.getVibroClaws());
        }
        r.newlines = 0;
        addReport(r);
        if (glancing) {
            r = new Report(4030);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        }
        if (directBlow) {
            r = new Report(4032);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
        }
        while (hits > 0) {
            // BA get hit seperately by each attacking BA trooper
            int damage = Math.min(ae.getVibroClaws(), hits);
            // conv infantry get hit in one lump
            if (te instanceof Infantry && !(te instanceof BattleArmor)) {
                damage = hits;
            }
            hits -= damage;
            HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            r = new Report(4135);
            r.subject = ae.getId();
            r.add(te.getLocationAbbr(hit));
            r.newlines = 0;
            addReport(r);
            addReport(damageEntity(te, hit, damage));
        }
        addNewLines();
    }

    /**
     * Handle a club attack
     */
    private void resolveClubAttack(PhysicalResult pr, int lastEntityId) {
        final ClubAttackAction caa = (ClubAttackAction) pr.aaa;
        final Entity ae = game.getEntity(caa.getEntityId());
        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        final Targetable target = game.getTarget(caa.getTargetType(), caa.getTargetId());
        Entity te = null;
        if (target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        final boolean targetInBuilding = Compute.isInBuilding(game, te);
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        Report r;

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(target.getPosition());

        // restore club attack
        caa.getClub().restore();

        // Shield bash causes 1 point of damage to the shield
        if (((MiscType) caa.getClub().getType()).isShield())
            ((Mech) ae).shieldAbsorptionDamage(1, caa.getClub().getLocation(), false);

        if (lastEntityId != caa.getEntityId()) {
            // who is making the attacks
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4145);
        r.subject = ae.getId();
        r.indent();
        r.add(caa.getClub().getName());
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        // Flail/Wrecking Ball auto misses on a 2 and hits themself.
        if ((((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_FLAIL) 
                || ((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_WRECKING_BALL)) 
                && roll == 2) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            ToHitData newToHit = new ToHitData(TargetRoll.AUTOMATIC_SUCCESS, "hit with own flail/wrecking ball");
            pr.damage /= 2;
            newToHit.setHitTable(ToHitData.HIT_NORMAL);
            newToHit.setSideTable(ToHitData.SIDE_FRONT);
            pr.toHit = newToHit;
            pr.aaa.setTargetId(ae.getId());
            pr.aaa.setTargetType(Targetable.TYPE_ENTITY);
            pr.roll = Integer.MAX_VALUE;
            resolveClubAttack(pr, ae.getId());
            game.addPSR(new PilotingRollData(ae.getId(), 0, "missed a flail/wrecking ball attack"));
            return;
        }

        // Need to compute 2d6 damage. and add +3 heat build up.
        if (((MiscType) (caa.getClub().getType())).hasSubType(MiscType.S_BUZZSAW)) {

            damage = Compute.d6(2);
            ae.heatBuildup += 3;

            // Buzzsaw's blade will shatter on a roll of 2.
            if (roll == 2) {

                Mounted club = caa.getClub();

                for (Mounted eq : ae.getWeaponList()) {
                    if (eq.getLocation() == club.getLocation() && eq.getType() instanceof MiscType && ((MiscType) eq.getType()).hasFlag(MiscType.F_CLUB) && ((MiscType) eq.getType()).hasSubType(MiscType.S_BUZZSAW)) {
                        eq.setDestroyed(true);
                        break;
                    }
                }
                r = new Report(4037);
                r.subject = ae.getId();
                addReport(r);
                damage = 0;
                return;
            }
        }

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4075);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_MACE_THB)) {
                game.addPSR(new PilotingRollData(ae.getId(), 0, "missed a mace attack"));
            }
            if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_MACE)) {
                game.addPSR(new PilotingRollData(ae.getId(), 2, "missed a mace attack"));
            }
            return;
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4080);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            r.newlines = 0;
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_MACE_THB)) {
                game.addPSR(new PilotingRollData(ae.getId(), 0, "missed a mace attack"));
            }
            if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_MACE)) {
                game.addPSR(new PilotingRollData(ae.getId(), 2, "missed a mace attack"));
            }

            // If the target is in a building, the building absorbs the damage.
            if (targetInBuilding && bldg != null) {

                // Only report if damage was done to the building.
                if (damage > 0) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
                    for (Report report : buildingReport) {
                        report.subject = ae.getId();
                    }
                    addReport(buildingReport);
                }

            }
            return;
        }

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) 
                || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // And we're done!
            return;
        }

        HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
        hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
        r = new Report(4045);
        r.subject = ae.getId();
        r.add(toHit.getTableDesc());
        r.add(te.getLocationAbbr(hit));
        r.newlines = 0;
        addReport(r);

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        if (targetInBuilding && bldg != null) {
            int bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(target.getPosition()) / 10.0);
            int toBldg = Math.min(bldgAbsorbs, damage);
            damage -= toBldg;
            addNewLines();
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);
        }

        // A building may absorb the entire shot.
        if (damage == 0) {
            r = new Report(4050);
            r.subject = ae.getId();
            r.add(te.getShortName());
            r.add(te.getOwner().getName());
            r.newlines = 0;
            addReport(r);
        } else {
            if (glancing) {
                damage = (int) Math.floor(damage / 2.0);
            }
            if (directBlow) {
                damage += toHit.getMoS() / 3;
                hit.makeDirectBlow(toHit.getMoS() / 3);
            }
            
            /*if (damage >= 1 && te.hasWorkingMisc(MiscType.F_SPIKES, -1, hit.getLocation())) {
                r = new Report(4330);
                r.indent(2);
                r.newlines = 0;
                r.subject = ae.getId();
                addReport(r);
                checkBreakSpikes(te, hit.getLocation());
                damage = Math.max(1, damage - 4);
                int loc = caa.getClub().getLocation();
                if (loc == Entity.LOC_NONE) {
                    addReport(damageEntity(ae, new HitData(Mech.LOC_LARM), 1, false, DamageType.NONE, false, false, false));
                    addReport(damageEntity(ae, new HitData(Mech.LOC_RARM), 1, false, DamageType.NONE, false, false, false));
                } else {
                    addReport(damageEntity(ae, new HitData(loc), 2, false, DamageType.NONE, false, false, false));
                }
            }*/
            DamageType damageType = DamageType.NONE;
            addReport(damageEntity(te, hit, damage, false, damageType, false, false, throughFront));
            if (target instanceof VTOL) {
                // destroy rotor
                addReport(applyCriticalHit(te, VTOL.LOC_ROTOR, new CriticalSlot(CriticalSlot.TYPE_SYSTEM, VTOL.CRIT_ROTOR_DESTROYED), false));
            }
        }

        // On a roll of 10+ a lance hitting a mech/Vehicle can cause 1 point of
        // internal damage
        if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_LANCE) && te.getArmor(hit) > 0 && te.getArmorType() != EquipmentType.T_ARMOR_HARDENED) {
            roll = Compute.d6(2);
            // Pierce checking report
            r = new Report(4021);
            r.indent(2);
            r.subject = ae.getId();
            r.add(te.getLocationAbbr(hit));
            r.add(roll);
            r.newlines = 1;
            addReport(r);
            if (roll >= 10) {
                hit.makeGlancingBlow();
                addReport(damageEntity(te, hit, 1, false, DamageType.NONE, true, false, throughFront));
            }
        }

        if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_CHAIN_WHIP) 
                && te instanceof Mech) {
            addNewLines();
            
            int loc = hit.getLocation();
            int toHitNumber = toHit.getValue();
            
            if ( (loc == Mech.LOC_LLEG || loc == Mech.LOC_RLEG) && (!te.hasActiveShield(loc) && !te.hasPassiveShield(loc))) {
                
                roll = Compute.d6(2);
                
                if ( (((Mech)ae).hasTSM() && ae.heat >= 9) 
                        && (!((Mech)te).hasTSM() || (((Mech)te).hasTSM()) && te.heat < 9) ) {
                    toHitNumber -= 2;
                }
                
                r = new Report(4450);
                r.subject = ae.getId();
                r.add(ae.getShortName());
                r.add(te.getShortName());
                r.add(toHitNumber);
                r.add(roll);
                r.indent(2);
                r.newlines = 0;
                addReport(r);
                
                if ( roll >= toHit.getValue() ) {
                    r = new Report(2270);
                    r.subject = ae.getId();
                    r.newlines = 0;
                    addReport(r);
                    
                    game.addPSR(new PilotingRollData(te.getId(),3,"Snared by chain whip"));
                }else {
                    r = new Report(2357);
                    r.subject = ae.getId();
                    r.newlines = 0;
                    addReport(r);
                }
            } else if ( (loc == Mech.LOC_RARM || loc == Mech.LOC_LARM)
                    && (!te.hasActiveShield(loc) && !te.hasPassiveShield(loc) && !te.hasNoDefenseShield(loc)) ) {
                GrappleAttackAction gaa = new GrappleAttackAction(ae.getId(),te.getId());
                ToHitData grappleHit = GrappleAttackAction.toHit(game, ae.getId(), target);
                PhysicalResult grappleResult = new PhysicalResult();
                grappleResult.aaa = gaa;
                grappleResult.toHit = grappleHit;
                grappleResult.roll = Compute.d6(2);
                resolveGrappleAttack(grappleResult, lastEntityId, hit.getLocation() == Mech.LOC_RARM ? Entity.GRAPPLE_RIGHT : Entity.GRAPPLE_LEFT );
                
            }
        }

        addNewLines();

        if (((MiscType) caa.getClub().getType()).hasSubType(MiscType.S_TREE_CLUB)) {
            // the club breaks
            r = new Report(4150);
            r.subject = ae.getId();
            r.add(caa.getClub().getName());
            addReport(r);
            ae.removeMisc(caa.getClub().getName());
        }
    }

    /**
     * Handle a push attack
     */
    private void resolvePushAttack(PhysicalResult pr, int lastEntityId) {
        final PushAttackAction paa = (PushAttackAction) pr.aaa;
        final Entity ae = game.getEntity(paa.getEntityId());
        // PLEASE NOTE: buildings are *never* the target of a "push".
        final Entity te = game.getEntity(paa.getTargetId());
        // get roll and ToHitData from the PhysicalResult
        int roll = pr.roll;
        final ToHitData toHit = pr.toHit;
        Report r;

        // was this push resolved earlier?
        if (pr.pushBackResolved) {
            return;
        }
        // don't try this one again
        pr.pushBackResolved = true;

        if (lastEntityId != paa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4155);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4160);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // report the roll
        r = new Report(4025);
        r.subject = ae.getId();
        r.add(toHit.getValue());
        r.add(roll);
        r.newlines = 0;
        addReport(r);

        // check if our target has a push against us, too, and get it
        PhysicalResult targetPushResult = null;
        for (Enumeration<PhysicalResult> i = physicalResults.elements(); i.hasMoreElements();) {
            PhysicalResult tpr = i.nextElement();
            if (tpr.aaa.getEntityId() == te.getId() && tpr.aaa instanceof PushAttackAction && tpr.aaa.getTargetId() == ae.getId()) {
                targetPushResult = tpr;
            }
        }
        // if our target has a push against us,
        // and we are hitting, we need to resolve both now
        if (targetPushResult != null && !targetPushResult.pushBackResolved && roll >= toHit.getValue()) {
            targetPushResult.pushBackResolved = true;
            // do they hit?
            if (targetPushResult.roll >= targetPushResult.toHit.getValue()) {
                r = new Report(4165);
                r.subject = ae.getId();
                r.addDesc(te);
                r.addDesc(te);
                r.addDesc(ae);
                r.add(targetPushResult.toHit.getValue());
                r.add(targetPushResult.roll);
                r.addDesc(ae);
                addReport(r);
                PilotingRollData targetPushPRD = getKickPushPSR(te, ae, te, "was pushed");
                targetPushPRD.setCumulative(false); // see Bug# 811987 for more
                // info
                PilotingRollData pushPRD = getKickPushPSR(ae, ae, te, "was pushed");
                pushPRD.setCumulative(false); // see Bug# 811987 for more info
                game.addPSR(pushPRD);
                game.addPSR(targetPushPRD);
                return;
            }
            // report the miss
            r = new Report(4166);
            r.subject = ae.getId();
            r.addDesc(te);
            r.addDesc(ae);
            r.add(targetPushResult.toHit.getValue());
            r.add(targetPushResult.roll);
            addReport(r);
        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            return;
        }

        // we hit...
        int direction = ae.getFacing();

        Coords src = te.getPosition();
        Coords dest = src.translated(direction);

        PilotingRollData pushPRD = getKickPushPSR(te, ae, te, "was pushed");
        pushPRD.setCumulative(false); // see Bug# 811987 for more info

        if (Compute.isValidDisplacement(game, te.getId(), te.getPosition(), direction)) {
            r = new Report(4170);
            r.subject = ae.getId();
            r.newlines = 0;
            addReport(r);
            if (game.getBoard().contains(dest)) {
                r = new Report(4175);
                r.subject = ae.getId();
                r.add(dest.getBoardNum(), true);
                addReport(r);
            } else {
                // uh-oh, pushed off board
                r = new Report(4180);
                r.subject = ae.getId();
                addReport(r);
            }

            addReport(doEntityDisplacement(te, src, dest, pushPRD));

            // if push actually moved the target, attacker follows thru
            if (!te.getPosition().equals(src)) {
                ae.setPosition(src);
            }
        } else {
            // targe imovable
            r = new Report(4185);
            r.subject = ae.getId();
            addReport(r);
            game.addPSR(pushPRD);
        }

        addNewLines();
    }

    /**
     * Handle a trip attack
     */
    private void resolveTripAttack(PhysicalResult pr, int lastEntityId) {
        final TripAttackAction paa = (TripAttackAction) pr.aaa;
        final Entity ae = game.getEntity(paa.getEntityId());
        // PLEASE NOTE: buildings are *never* the target of a "trip".
        final Entity te = game.getEntity(paa.getTargetId());
        // get roll and ToHitData from the PhysicalResult
        int roll = pr.roll;
        final ToHitData toHit = pr.toHit;
        Report r;

        if (lastEntityId != paa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4280);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4285);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // report the roll
        r = new Report(4025);
        r.subject = ae.getId();
        r.add(toHit.getValue());
        r.add(roll);
        r.newlines = 0;
        addReport(r);

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            return;
        }

        // we hit...
        PilotingRollData pushPRD = getKickPushPSR(te, ae, te, "was tripped");
        pushPRD.setCumulative(false); // see Bug# 811987 for more info

        game.addPSR(pushPRD);

        r = new Report(4040);
        r.subject = ae.getId();
        addReport(r);
        addNewLines();
    }

    /**
     * Handle a grapple attack
     */
    private void resolveGrappleAttack(PhysicalResult pr, int lastEntityId) {
        resolveGrappleAttack(pr, lastEntityId, Entity.GRAPPLE_BOTH);
    }
    
    private void resolveGrappleAttack(PhysicalResult pr, int lastEntityId, int grappleSide) {
        final GrappleAttackAction paa = (GrappleAttackAction) pr.aaa;
        final Entity ae = game.getEntity(paa.getEntityId());
        // PLEASE NOTE: buildings are *never* the target of a "push".
        final Entity te = game.getEntity(paa.getTargetId());
        // get roll and ToHitData from the PhysicalResult
        int roll = pr.roll;
        final ToHitData toHit = pr.toHit;
        Report r;

        // same method as push, for counterattacks
        if (pr.pushBackResolved) {
            return;
        }

        if (te.getGrappled() != Entity.NONE || ae.getGrappled() != Entity.NONE) {
            toHit.addModifier(TargetRoll.IMPOSSIBLE, "Already Grappled");
        }

        if (lastEntityId != paa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4295);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4300);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        // report the roll
        r = new Report(4025);
        r.subject = ae.getId();
        r.add(toHit.getValue());
        r.add(roll);
        r.newlines = 0;
        addReport(r);

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            return;
        }

        // we hit...
        ae.setGrappled(te.getId(), true);
        te.setGrappled(ae.getId(), false);
        Coords pos = te.getPosition();
        ae.setPosition(pos);
        ae.setElevation(te.getElevation());
        te.setFacing((ae.getFacing() + 3) % 6);
        addReport(doSetLocationsExposure(ae, game.getBoard().getHex(pos), false, ae.getElevation()));
        ae.setGrappleSide(grappleSide);
        te.setGrappleSide(grappleSide);
        
        r = new Report(4040);
        r.subject = ae.getId();
        addReport(r);
        addNewLines();
    }

    /**
     * Handle a break grapple attack
     */
    private void resolveBreakGrappleAttack(PhysicalResult pr, int lastEntityId) {
        final BreakGrappleAttackAction paa = (BreakGrappleAttackAction) pr.aaa;
        final Entity ae = game.getEntity(paa.getEntityId());
        // PLEASE NOTE: buildings are *never* the target of a "push".
        final Entity te = game.getEntity(paa.getTargetId());
        // get roll and ToHitData from the PhysicalResult
        int roll = pr.roll;
        final ToHitData toHit = pr.toHit;
        Report r;

        if (lastEntityId != paa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        r = new Report(4305);
        r.subject = ae.getId();
        r.indent();
        r.addDesc(te);
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(4310);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            return;
        }

        if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4320);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);

            // do we hit?
            if (roll < toHit.getValue()) {
                // miss
                r = new Report(4035);
                r.subject = ae.getId();
                addReport(r);
                return;
            }

            // hit
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
        }

        // is there a counterattack?
        PhysicalResult targetGrappleResult = null;
        for (Enumeration<PhysicalResult> i = physicalResults.elements(); i.hasMoreElements();) {
            PhysicalResult tpr = i.nextElement();
            if (tpr.aaa.getEntityId() == te.getId() && tpr.aaa instanceof GrappleAttackAction && tpr.aaa.getTargetId() == ae.getId()) {
                targetGrappleResult = tpr;
                break;
            }
        }

        if (targetGrappleResult != null) {
            targetGrappleResult.pushBackResolved = true;
            // counterattack
            r = new Report(4315);
            r.subject = te.getId();
            r.newlines = 0;
            r.addDesc(te);
            addReport(r);

            // report the roll
            r = new Report(4025);
            r.subject = te.getId();
            r.add(targetGrappleResult.toHit.getValue());
            r.add(targetGrappleResult.roll);
            r.newlines = 0;
            addReport(r);

            // do we hit?
            if (roll < toHit.getValue()) {
                // miss
                r = new Report(4035);
                r.subject = ae.getId();
                addReport(r);
            } else {
                // hit
                r = new Report(4040);
                r.subject = ae.getId();
                addReport(r);

                // exchange attacker and defender
                ae.setGrappled(te.getId(), false);
                te.setGrappled(ae.getId(), true);

                return;
            }
        }

        // score the adjacent hexes
        Coords hexes[] = new Coords[6];
        int scores[] = new int[6];

        IHex curHex = game.getBoard().getHex(ae.getPosition());
        for (int i = 0; i < 6; i++) {
            hexes[i] = ae.getPosition().translated(i);
            scores[i] = 0;
            IHex hex = game.getBoard().getHex(hexes[i]);
            if (hex.containsTerrain(Terrains.MAGMA))
                scores[i] += 10;
            if (hex.containsTerrain(Terrains.WATER))
                scores[i] += hex.terrainLevel(Terrains.WATER);
            if (curHex.surface() - hex.surface() >= 2)
                scores[i] += 2 * (curHex.surface() - hex.surface());
        }

        int bestScore = 99999;
        int best = 0;
        int worstScore = -99999;
        int worst = 0;

        for (int i = 0; i < 6; i++) {
            if (bestScore > scores[i]) {
                best = i;
                bestScore = scores[i];
            }
            if (worstScore < scores[i]) {
                worst = i;
                worstScore = scores[i];
            }
        }

        // attacker doesnt fall, unless off a cliff
        if (ae.isGrappleAttacker()) {
            // move self to least dangerous hex
            PilotingRollData psr = ae.getBasePilotingRoll();
            psr.addModifier(TargetRoll.AUTOMATIC_SUCCESS, "break grapple");
            addReport(doEntityDisplacement(ae, ae.getPosition(), hexes[best], psr));
            ae.setFacing(hexes[best].direction(te.getPosition()));
        } else {
            // move enemy to most dangerous hex
            PilotingRollData psr = te.getBasePilotingRoll();
            psr.addModifier(TargetRoll.AUTOMATIC_SUCCESS, "break grapple");
            addReport(doEntityDisplacement(te, te.getPosition(), hexes[worst], psr));
            te.setFacing(hexes[worst].direction(ae.getPosition()));
        }

        // grapple is broken
        ae.setGrappled(Entity.NONE, false);
        te.setGrappled(Entity.NONE, false);

        addNewLines();
    }

    /**
     * Handle a charge attack
     */
    private void resolveChargeAttack(PhysicalResult pr, int lastEntityId) {
        final ChargeAttackAction caa = (ChargeAttackAction) pr.aaa;
        final Entity ae = game.getEntity(caa.getEntityId());
        final Targetable target = game.getTarget(caa.getTargetType(), caa.getTargetId());
        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;

        Entity te = null;
        if (target != null && target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();

        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        Report r;

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(caa.getTargetPos());

        // is the attacker dead? because that sure messes up the calculations
        if (ae == null) {
            return;
        }

        final int direction = ae.getFacing();

        // entity isn't charging any more
        ae.setDisplacementAttack(null);

        if (lastEntityId != caa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        // should we even bother?
        if (target == null || target.getTargetType() == Targetable.TYPE_ENTITY && (te.isDestroyed() || te.isDoomed() || te.crew.isDead())) {
            r = new Report(4190);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            // doEntityDisplacement(ae, ae.getPosition(), caa.getTargetPos(),
            // null);
            // Randall said that if a charge fails because of target
            // destruction,
            // the attacker stays in the hex he was in at the end of the
            // movement phase
            // See Bug 912094
            return;
        }

        // attacker fell down?
        if (ae.isProne()) {
            r = new Report(4195);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        // attacker immobile?
        if (ae.isImmobile()) {
            r = new Report(4200);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        // target fell down, only for attacking Mechs, though
        if ((te != null) && (te.isProne()) && ae instanceof Mech) {
            r = new Report(4205);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        r = new Report(4210);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        // target still in the same position?
        if (!target.getPosition().equals(caa.getTargetPos())) {
            r = new Report(4215);
            r.subject = ae.getId();
            addReport(r);
            addReport(doEntityDisplacement(ae, ae.getPosition(), caa.getTargetPos(), null));
            return;
        }

        // if the attacker's prone, fudge the roll
        if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            roll = -12;
            r = new Report(4220);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            roll = Integer.MAX_VALUE;
            r = new Report(4225);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            Coords src = ae.getPosition();
            Coords dest = Compute.getMissedChargeDisplacement(game, ae.getId(), src, direction);

            // TODO: handle movement into/out of/through a building. Do it here?

            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            // move attacker to side hex
            addReport(doEntityDisplacement(ae, src, dest, null));
        } else if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) { // Targeting
            // a
            // building.
            // The building takes the full brunt of the attack.
            r = new Report(4040);
            r.subject = ae.getId();
            addReport(r);
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

            // Apply damage to the attacker.
            int toAttacker = ChargeAttackAction.getDamageTakenBy(ae, bldg, target.getPosition());
            HitData hit = ae.rollHitLocation(ToHitData.HIT_NORMAL, ae.sideTable(target.getPosition()));
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            addReport(damageEntity(ae, hit, toAttacker, false, DamageType.NONE, false, false, throughFront));
            addNewLines();
            entityUpdate(ae.getId());

            // TODO: Does the attacker enter the building?
            // TODO: What if the building collapses?
        } else {
            // Resolve the damage.
            resolveChargeDamage(ae, te, toHit, direction, glancing, throughFront);
        }
    }

    /**
     * Handle a telemissile attack
     */
    private void resolveTeleMissileAttack(PhysicalResult pr, int lastEntityId) {
        final TeleMissileAttackAction taa = (TeleMissileAttackAction) pr.aaa;
        final Entity ae = game.getEntity(taa.getEntityId());
        if (!(ae instanceof TeleMissile)) {
            return;
        }
        TeleMissile tm = (TeleMissile) ae;
        final Targetable target = game.getTarget(taa.getTargetType(), taa.getTargetId());
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        Entity te = null;
        if (target != null && target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }

        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }

        Report r;

        if (lastEntityId != taa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        // should we even bother?
        if (target == null || target.getTargetType() == Targetable.TYPE_ENTITY && (te.isDestroyed() || te.isDoomed() || te.crew.isDead())) {
            r = new Report(4190);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        r = new Report(9031);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        // add some stuff to the to hit value
        // need to add damage done modifier
        if (ae.damageThisRound > 10)
            toHit.addModifier((int) (Math.floor(ae.damageThisRound / 10.0)), "damage taken");

        // add modifiers for the originating unit missing CIC, FCS, or sensors
        Entity ride = game.getEntity(tm.getOriginalRideId());
        if (null != ride && ride instanceof Aero) {
            Aero aride = (Aero) ride;
            int cic = aride.getCICHits();
            if (cic > 0) {
                toHit.addModifier(cic * 2, "CIC damage");
            }

            // sensor hits
            int sensors = aride.getSensorHits();
            if (sensors > 0 && sensors < 3)
                toHit.addModifier(sensors, "sensor damage");
            if (sensors > 2)
                toHit.addModifier(+5, "sensors destroyed");

            // FCS hits
            int fcs = aride.getFCSHits();
            if (fcs > 0)
                toHit.addModifier(fcs * 2, "fcs damage");
        }

        if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            roll = Integer.MAX_VALUE;
            r = new Report(4225);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else {
            // report the roll
            r = new Report(9032);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(toHit.getDesc());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
        } else {
            // Resolve the damage.
            HitData hit = te.rollHitLocation(ToHitData.HIT_NORMAL, te.sideTable(ae.getPosition(), true));
            hit.setCapital(true);
            hit.setCapMisCritMod(tm.getCritMod());
            addReport(damageEntity(te, hit, TeleMissileAttackAction.getDamageFor(ae), false, DamageType.NONE, false, false, throughFront));
            destroyEntity(ae, "successful attack");
        }

    }

    /**
     * Handle a ramming attack
     */
    private void resolveRamAttack(PhysicalResult pr, int lastEntityId) {
        final RamAttackAction raa = (RamAttackAction) pr.aaa;
        final Entity ae = game.getEntity(raa.getEntityId());
        final Targetable target = game.getTarget(raa.getTargetType(), raa.getTargetId());
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        Entity te = null;
        if (target != null && target.getTargetType() == Targetable.TYPE_ENTITY) {
            te = (Entity) target;
        }

        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }

        Report r;

        boolean glancing = Compute.d6(1) == 6;

        // entity isn't ramming any more
        ae.setRamming(false);

        if (lastEntityId != raa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        // should we even bother?
        if (target == null || target.getTargetType() == Targetable.TYPE_ENTITY && (te.isDestroyed() || te.isDoomed() || te.crew.isDead())) {
            r = new Report(4190);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        // steel yourself for attack
        int steelroll = Compute.d6(2);
        r = new Report(9020);
        r.subject = ae.getId();
        r.add(steelroll);
        if (steelroll > 11) {
            r.choose(true);
            addReport(r);
        } else {
            r.choose(false);
            addReport(r);
            return;
        }

        // attacker immobile?
        if (ae.isImmobile()) {
            r = new Report(4200);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            return;
        }

        r = new Report(9030);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            roll = Integer.MAX_VALUE;
            r = new Report(4225);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
        }

        // do we hit?
        if (roll < toHit.getValue()) {
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
        } else {
            // Resolve the damage.
            resolveRamDamage((Aero) ae, (Aero) te, toHit, glancing, throughFront);
        }

    }

    /**
     * Handle a ramming attack's damage
     */
    private void resolveRamDamage(Aero ae, Aero te, ToHitData toHit, boolean glancing, boolean throughFront) {

        int damage = RamAttackAction.getDamageFor(ae, te);
        int damageTaken = RamAttackAction.getDamageTakenBy(ae, te);
        if (glancing) {
            damage = (int) Math.floor(damage / 2.0);
            damageTaken = (int) Math.floor(damageTaken / 2.0);
        }

        // are they capital scale?
        if (te.isCapitalScale()) {
            damage = (int) Math.floor(damage / 10.0);
        }
        if (ae.isCapitalScale()) {
            damageTaken = (int) Math.floor(damageTaken / 10.0);
        }

        Report r;

        if (glancing) {
            r = new Report(9015);
            r.subject = ae.getId();
            r.indent(1);
            r.newlines = 0;
            addReport(r);
        }

        // damage to attacker
        r = new Report(4240);
        r.subject = ae.getId();
        r.add(damageTaken);
        r.newlines = 0;
        addReport(r);

        HitData hit = ae.rollHitLocation(ToHitData.HIT_NORMAL, ae.sideTable(te.getPosition(), true));
        // if the damage is greater than the initial armor then destroy the
        // entity
        if ((2 * ae.getOArmor(hit)) < damageTaken) {
            addReport(destroyEntity(ae, "by massive ramming damage", false));
        } else {
            addReport(damageEntity(ae, hit, damageTaken, false, DamageType.NONE, false, false, throughFront));
        }

        r = new Report(4230);
        r.subject = ae.getId();
        r.add(damage);
        r.add(toHit.getTableDesc());
        r.newlines = 0;
        addReport(r);

        hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
        if ((2 * te.getOArmor(hit)) < damage) {
            addReport(destroyEntity(te, "by massive ramming damage", false));
        } else {
            addReport(damageEntity(te, hit, damage, false, DamageType.NONE, false, false, throughFront));
        }
    }

    /**
     * Handle a charge's damage
     */
    private void resolveChargeDamage(Entity ae, Entity te, ToHitData toHit, int direction) {
        resolveChargeDamage(ae, te, toHit, direction, false, true);
    }

    private void resolveChargeDamage(Entity ae, Entity te, ToHitData toHit, int direction, boolean glancing, boolean throughFront) {

        // we hit...
        // Damage To Target
        int damage = ChargeAttackAction.getDamageFor(ae, te, game.getOptions().booleanOption("tacops_charge_damage"), toHit.getMoS());

        // Damage to Attacker
        int damageTaken = ChargeAttackAction.getDamageTakenBy(ae, te, game.getOptions().booleanOption("tacops_charge_damage"));
        PilotingRollData chargePSR = null;
        if (glancing) {
            // Glancing Blow rule doesn't state whether damage to attacker on
            // charge
            // or DFA is halved as well, assume yes. TODO: Check with PM
            damage = (int) Math.floor(damage / 2.0);
            damageTaken = (int) Math.floor(damageTaken / 2.0);
        }
        boolean bDirect = false;
        int directBlowCritMod = toHit.getMoS() / 3;
        if (game.getOptions().booleanOption("tacops_direct_blow") && toHit.getMoS() / 3 >= 1) {
            damage += toHit.getMoS() / 3;
            bDirect = false;
        }

        // Is the target inside a building?
        final boolean targetInBuilding = Compute.isInBuilding(game, te);

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(te.getPosition());

        // The building shields all units from a certain amount of damage.
        // The amount is based upon the building's CF at the phase's start.
        int bldgAbsorbs = 0;
        if (targetInBuilding && bldg != null) {
            bldgAbsorbs = (int) Math.ceil(bldg.getPhaseCF(te.getPosition()) / 10.0);
        }

        // If we're upright, we may fall down.
        if (!ae.isProne()) {
            chargePSR = new PilotingRollData(ae.getId(), 2, "charging");
        }

        Report r;

        // damage to attacker
        r = new Report(4240);
        r.subject = ae.getId();
        r.add(damageTaken);
        r.newlines = 0;
        addReport(r);

        // work out which locations have spikes
        int[] spikes = new int[ae.locations()];
        for (int i = 0; i < ae.locations(); i++)
            spikes[i] = 0;
        for (Mounted m : ae.getMisc()) {
            if (m.getLocation() != Entity.LOC_NONE && m.getType().hasFlag(MiscType.F_SPIKES))
                spikes[m.getLocation()] = 1;
        }
        int spikeDamage = 0;

        while (damageTaken > 0) {
            int cluster = Math.min(5, damageTaken);
            HitData hit = ae.rollHitLocation(toHit.getHitTable(), ae.sideTable(te.getPosition()));
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            if (spikes[hit.getLocation()] == 1) {
                r = new Report(4335);
                r.indent(2);
                r.newlines = 0;
                r.subject = ae.getId();
                addReport(r);
                spikes[hit.getLocation()] = 0;
                checkBreakSpikes(ae, hit.getLocation());
                spikeDamage += 2;
            }
            addReport(damageEntity(ae, hit, cluster, false, DamageType.NONE, false, false, throughFront));
            damageTaken -= cluster;
        }

        // Damage to target
        damage += spikeDamage;
        r = new Report(4230);
        r.subject = ae.getId();
        r.add(damage);
        r.add(toHit.getTableDesc());
        r.newlines = 0;
        addReport(r);

        // work out which locations have spikes
        spikes = new int[te.locations()];
        for (int i = 0; i < te.locations(); i++)
            spikes[i] = 0;
        for (Mounted m : te.getMisc()) {
            if (m.getLocation() != Entity.LOC_NONE && m.getType().hasFlag(MiscType.F_SPIKES))
                spikes[m.getLocation()] = 1;
        }
        spikeDamage = 0;

        while (damage > 0) {
            int cluster = Math.min(5, damage);
            damage -= cluster;
            if (bldgAbsorbs > 0) {
                int toBldg = Math.min(bldgAbsorbs, cluster);
                cluster -= toBldg;
                addNewLines();
                Vector<Report> buildingReport = damageBuilding(bldg, damage, te.getPosition());
                for (Report report : buildingReport) {
                    report.subject = ae.getId();
                }
                addReport(buildingReport);
            }

            // A building may absorb the entire shot.
            if (cluster == 0) {
                r = new Report(4235);
                r.subject = ae.getId();
                r.addDesc(te);
                r.newlines = 0;
                addReport(r);
            } else {
                HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                if (bDirect)
                    hit.makeDirectBlow(directBlowCritMod);
                if (spikes[hit.getLocation()] == 1) {
                    r = new Report(4330);
                    r.indent(2);
                    r.newlines = 0;
                    r.subject = ae.getId();
                    addReport(r);
                    spikes[hit.getLocation()] = 0;
                    checkBreakSpikes(te, hit.getLocation());
                    cluster = 1;
                    spikeDamage += 2;
                }
                addReport(damageEntity(te, hit, cluster, false, DamageType.NONE, false, false, throughFront));
            }
        }
        // finally apply spike damage to attacker
        if (ae instanceof Mech)
            addReport(damageEntity(ae, new HitData(Mech.LOC_CT), spikeDamage, false, DamageType.NONE, false, false, throughFront));
        else if (ae instanceof Tank)
            addReport(damageEntity(ae, new HitData(Tank.LOC_FRONT), spikeDamage, false, DamageType.NONE, false, false, throughFront));

        // move attacker and target, if possible
        Coords src = te.getPosition();
        Coords dest = src.translated(direction);

        if (Compute.isValidDisplacement(game, te.getId(), te.getPosition(), direction)) {
            addNewLines();
            addReport(doEntityDisplacement(te, src, dest, new PilotingRollData(te.getId(), 2, "was charged")));
            addReport(doEntityDisplacement(ae, ae.getPosition(), src, chargePSR));
        }

        addNewLines();

    } // End private void resolveChargeDamage( Entity, Entity, ToHitData )

    private void resolveLayExplosivesAttack(PhysicalResult pr, int lastEntityId) {
        final LayExplosivesAttackAction laa = (LayExplosivesAttackAction) pr.aaa;
        final Entity ae = game.getEntity(laa.getEntityId());
        if (ae instanceof Infantry) {
            Infantry inf = (Infantry) ae;
            if (inf.turnsLayingExplosives < 0) {
                inf.turnsLayingExplosives = 0;
                Report r = new Report(4270);
                r.subject = inf.getId();
                r.addDesc(inf);
                addReport(r);
            } else {
                Building building = game.getBoard().getBuildingAt(ae.getPosition());
                if (building != null) {
                    building.addDemolitionCharge(ae.getOwner().getId(), pr.damage);
                    Report r = new Report(4275);
                    r.subject = inf.getId();
                    r.addDesc(inf);
                    r.add(pr.damage);
                    addReport(r);
                }
                inf.turnsLayingExplosives = -1;
            }
        }
    }

    /**
     * Handle a death from above attack
     */
    private void resolveDfaAttack(PhysicalResult pr, int lastEntityId) {
        final DfaAttackAction daa = (DfaAttackAction) pr.aaa;
        final Entity ae = game.getEntity(daa.getEntityId());
        final Targetable target = game.getTarget(daa.getTargetType(), daa.getTargetId());
        // get damage, ToHitData and roll from the PhysicalResult
        int damage = pr.damage;
        final ToHitData toHit = pr.toHit;
        int roll = pr.roll;
        Entity te = null;
        if (target != null && target.getTargetType() == Targetable.TYPE_ENTITY) {
            // Lets re-write around that horrible hack that was here before.
            // So instead of asking if a specific location is wet and praying
            // that it won't cause an NPE...
            // We'll check 1) if the hex has water, and 2) if it's deep enough
            // to cover the unit in question at its current elevation.
            // It's especially important to make sure it's done this way,
            // because some units (Sylph, submarines) can be at ANY elevation
            // underwater, and VTOLs can be well above the surface.
            te = (Entity) target;
            IHex hex = game.getBoard().getHex(te.getPosition());
            if (hex.containsTerrain(Terrains.WATER)) {
                if (te.absHeight() < hex.getElevation())
                    damage = (int) Math.ceil(damage * 0.5f);
            }
        }
        boolean throughFront = true;
        if (te != null) {
            throughFront = Compute.isThroughFrontHex(game, ae.getPosition(), te);
        }
        final boolean glancing = game.getOptions().booleanOption("tacops_glancing_blows") && roll == toHit.getValue();
        // Set Margin of Success/Failure.
        toHit.setMoS(roll - Math.max(2, toHit.getValue()));
        final boolean directBlow = game.getOptions().booleanOption("tacops_direct_blow") && ((toHit.getMoS() / 3) >= 1);

        Report r;

        // Which building takes the damage?
        Building bldg = game.getBoard().getBuildingAt(daa.getTargetPos());

        // is the attacker dead? because that sure messes up the calculations
        if (ae == null) {
            return;
        }

        final int direction = ae.getFacing();

        if (lastEntityId != daa.getEntityId()) {
            // who is making the attack
            r = new Report(4005);
            r.subject = ae.getId();
            r.addDesc(ae);
            addReport(r);
        }

        // entity isn't DFAing any more
        ae.setDisplacementAttack(null);

        // should we even bother?
        if (target == null || target.getTargetType() == Targetable.TYPE_ENTITY && (te.isDestroyed() || te.isDoomed() || te.crew.isDead())) {
            r = new Report(4245);
            r.subject = ae.getId();
            r.indent();
            addReport(r);
            if (ae.isProne()) {
                // attacker prone during weapons phase
                addReport(doEntityFall(ae, daa.getTargetPos(), 2, 3, ae.getBasePilotingRoll()));

            } else {
                // same effect as successful DFA
                addReport(doEntityDisplacement(ae, ae.getPosition(), daa.getTargetPos(), new PilotingRollData(ae.getId(), 4, "executed death from above")));
            }
            return;
        }

        r = new Report(4246);
        r.subject = ae.getId();
        r.indent();
        r.add(target.getDisplayName());
        r.newlines = 0;
        addReport(r);

        // target still in the same position?
        if (!target.getPosition().equals(daa.getTargetPos())) {
            r = new Report(4215);
            r.subject = ae.getId();
            addReport(r);
            addReport(doEntityFallsInto(ae, ae.getPosition(), daa.getTargetPos(), ae.getBasePilotingRoll()));
            return;
        }

        // hack: if the attacker's prone, or incapacitated, fudge the roll
        if (ae.isProne() || !ae.isActive()) {
            roll = -12;
            r = new Report(4250);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else if (toHit.getValue() == TargetRoll.IMPOSSIBLE) {
            roll = -12;
            r = new Report(4255);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
        } else if (toHit.getValue() == TargetRoll.AUTOMATIC_SUCCESS) {
            r = new Report(4260);
            r.subject = ae.getId();
            r.add(toHit.getDesc());
            addReport(r);
            roll = Integer.MAX_VALUE;
        } else {
            // report the roll
            r = new Report(4025);
            r.subject = ae.getId();
            r.add(toHit.getValue());
            r.add(roll);
            r.newlines = 0;
            addReport(r);
            if (glancing) {
                r = new Report(4030);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }
            if (directBlow) {
                r = new Report(4032);
                r.subject = ae.getId();
                r.newlines = 0;
                addReport(r);
            }

        }

        // do we hit?
        if (roll < toHit.getValue()) {
            Coords dest = te.getPosition();
            Coords targetDest = Compute.getPreferredDisplacement(game, te.getId(), dest, direction);
            // miss
            r = new Report(4035);
            r.subject = ae.getId();
            addReport(r);
            if (targetDest != null) {
                // attacker falls into destination hex
                r = new Report(4265);
                r.subject = ae.getId();
                r.addDesc(ae);
                r.add(dest.getBoardNum(), true);
                addReport(r);
                int height = 2 + (game.getBoard().getHex(dest).containsTerrain(Terrains.BLDG_ELEV) ? game.getBoard().getHex(dest).terrainLevel(Terrains.BLDG_ELEV) : 0);
                addReport(doEntityFall(ae, dest, height, 3, ae.getBasePilotingRoll()));

                // move target to preferred hex
                addReport(doEntityDisplacement(te, dest, targetDest, null));
            } else {
                // attacker destroyed Tanks
                // suffer an ammo/power plant hit.
                // TODO : a Mech suffers a Head Blown Off crit.
                addReport(destroyEntity(ae, "impossible displacement", ae instanceof Mech, ae instanceof Mech));
            }
            return;
        }

        // we hit...
        // Can't DFA a target inside of a building.
        int damageTaken = DfaAttackAction.getDamageTakenBy(ae);

        r = new Report(4040);
        r.subject = ae.getId();
        addReport(r);

        // Targeting a building.
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {

            // The building takes the full brunt of the attack.
            Vector<Report> buildingReport = damageBuilding(bldg, damage, target.getPosition());
            for (Report report : buildingReport) {
                report.subject = ae.getId();
            }
            addReport(buildingReport);

            // Damage any infantry in the hex.
            damageInfantryIn(bldg, damage, target.getPosition());

        } else { // Target isn't building.
            if (glancing) {
                damage = (int) Math.floor(damage / 2.0);
            }
            if (directBlow) {
                damage += toHit.getMoS() / 3;
            }
            // damage target
            r = new Report(4230);
            r.subject = ae.getId();
            r.add(damage);
            r.add(toHit.getTableDesc());
            r.newlines = 0;
            addReport(r);

            // work out which locations have spikes
            int[] spikes = new int[te.locations()];
            for (int i = 0; i < te.locations(); i++)
                spikes[i] = 0;
            for (Mounted m : te.getMisc()) {
                if (m.getLocation() != Entity.LOC_NONE && m.getType().hasFlag(MiscType.F_SPIKES))
                    spikes[m.getLocation()] = 1;
            }
            int spikeDamage = 0;

            while (damage > 0) {
                int cluster = Math.min(5, damage);
                HitData hit = te.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                if (directBlow) {
                    hit.makeDirectBlow(toHit.getMoS() / 3);
                }

                if (spikes[hit.getLocation()] == 1) {
                    r = new Report(4330);
                    r.indent(2);
                    r.newlines = 0;
                    r.subject = ae.getId();
                    addReport(r);
                    cluster = 1;
                    spikes[hit.getLocation()] = 0;
                    checkBreakSpikes(te, hit.getLocation());
                    spikeDamage += 2;
                }
                addReport(damageEntity(te, hit, cluster, false, DamageType.NONE, false, false, throughFront));
                damage -= 5;
            }

            if (spikeDamage > 0) {
                if (ae instanceof QuadMech) {
                    addReport(damageEntity(ae, new HitData(Mech.LOC_LARM), (spikeDamage + 2) / 4, false, DamageType.NONE, false, false, false));
                    addReport(damageEntity(ae, new HitData(Mech.LOC_RARM), (spikeDamage + 2) / 4, false, DamageType.NONE, false, false, false));
                    if (spikeDamage > 2) {
                        addReport(damageEntity(ae, new HitData(Mech.LOC_LLEG), spikeDamage / 4, false, DamageType.NONE, false, false, false));
                        addReport(damageEntity(ae, new HitData(Mech.LOC_RLEG), spikeDamage / 4, false, DamageType.NONE, false, false, false));
                    }
                } else {
                    addReport(damageEntity(ae, new HitData(Mech.LOC_LLEG), spikeDamage / 2, false, DamageType.NONE, false, false, false));
                    addReport(damageEntity(ae, new HitData(Mech.LOC_RLEG), spikeDamage / 2, false, DamageType.NONE, false, false, false));
                }
            }
            if (target instanceof VTOL) {
                // destroy rotor
                addReport(applyCriticalHit(te, VTOL.LOC_ROTOR, new CriticalSlot(CriticalSlot.TYPE_SYSTEM, VTOL.CRIT_ROTOR_DESTROYED), false));
            }
        }

        if (glancing) {
            // Glancing Blow rule doesn't state whether damage to attacker on
            // charge
            // or DFA is halved as well, assume yes. TODO: Check with PM
            damageTaken = (int) Math.floor(damageTaken / 2.0);
        }

        // damage attacker
        r = new Report(4240);
        r.subject = ae.getId();
        r.add(damageTaken);
        r.newlines = 0;
        addReport(r);
        while (damageTaken > 0) {
            int cluster = Math.min(5, damageTaken);
            HitData hit = ae.rollHitLocation(ToHitData.HIT_KICK, ToHitData.SIDE_FRONT);
            hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
            addReport(damageEntity(ae, hit, cluster));
            damageTaken -= cluster;
        }

        addNewLines();

        // That's it for target buildings.
        // TODO: where do I put the attacker?!?
        if ((target.getTargetType() == Targetable.TYPE_BUILDING) || (target.getTargetType() == Targetable.TYPE_FUEL_TANK)) {
            return;
        }

        // Target entities are pushed away or destroyed.
        Coords dest = te.getPosition();
        Coords targetDest = Compute.getValidDisplacement(game, te.getId(), dest, direction);
        if (targetDest != null) {
            addReport(doEntityDisplacement(te, dest, targetDest, new PilotingRollData(te.getId(), 2, "hit by death from above")));
        } else {
            // ack! automatic death! Tanks
            // suffer an ammo/power plant hit.
            // TODO : a Mech suffers a Head Blown Off crit.
            addReport(destroyEntity(te, "impossible displacement", te instanceof Mech, te instanceof Mech));
        }
        // HACK: to avoid automatic falls, displace from dest to dest
        addReport(doEntityDisplacement(ae, dest, dest, new PilotingRollData(ae.getId(), 4, "executed death from above")));
    }

    /**
     * Get the Kick or Push PSR, modified by weight class
     * 
     * @param psrEntity
     *            The <code>Entity</code> that should make a PSR
     * @param attacker
     *            The attacking <code>Entity></code>
     * @param target
     *            The target <code>Entity</code>
     * @return The <code>PilotingRollData</code>
     */
    private PilotingRollData getKickPushPSR(Entity psrEntity, Entity attacker, Entity target, String reason) {
        int mod = 0;

        if (game.getOptions().booleanOption("tacops_physical_psr")) {

            switch (target.getWeightClass()) {
            case EntityWeightClass.WEIGHT_LIGHT:
                mod = 1;
                break;
            case EntityWeightClass.WEIGHT_MEDIUM:
                mod = 0;
                break;
            case EntityWeightClass.WEIGHT_HEAVY:
                mod = -1;
                break;
            case EntityWeightClass.WEIGHT_ASSAULT:
                mod = -2;
                break;
            }
        }
        StringBuffer reportStr = new StringBuffer();
        reportStr.append(reason);
        if (mod > 0)
            reportStr.append(", weight class modifier +").append(mod);
        else
            reportStr.append(", weight class modifier ").append(mod);
        return new PilotingRollData(psrEntity.getId(), mod, reportStr.toString());
    }

    /**
     * Each mech sinks the amount of heat appropriate to its current heat
     * capacity.
     */
    private void resolveHeat() {
        Report r;
        // Heat phase header
        addReport(new Report(5000, Report.PUBLIC));
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity entity = i.nextElement();
            if (null == entity.getPosition()) {
                continue;
            }
            IHex entityHex = game.getBoard().getHex(entity.getPosition());

            // put in ASF heat build-up first because there are few differences
            if (entity instanceof Aero) {

                Aero a = (Aero) entity;

                // should we even bother?
                if (entity.isDestroyed() || entity.isDoomed() || entity.crew.isDoomed() || entity.crew.isDead()) {
                    continue;
                }

                // Engine hits cause excess heat for fighters
                if (!(entity instanceof SmallCraft || entity instanceof Jumpship))
                    entity.heatBuildup += 2 * a.getEngineHits();

                // add the heat we've built up so far.
                entity.heat += entity.heatBuildup;

                // how much heat can we sink?
                int tosink = entity.getHeatCapacityWithWater();

                tosink = Math.min(tosink, entity.heat);
                entity.heat -= tosink;
                r = new Report(5035);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(entity.heatBuildup);
                r.add(tosink);
                r.add(entity.heat);
                addReport(r);
                entity.heatBuildup = 0;

                // add in the effects of heat

                if (entity instanceof Dropship || entity instanceof Jumpship) {
                    // only check for a possible control roll
                    if (entity.heat > 0) {
                        int bonus = (int) Math.ceil(entity.heat / 100.0);
                        game.addControlRoll(new PilotingRollData(entity.getId(), bonus, "used too much heat"));
                        entity.heat = 0;
                    }
                    continue;
                }

                int autoShutDownHeat = 30;
                boolean mtHeat = game.getOptions().booleanOption("tacops_heat");
                if (mtHeat)
                    autoShutDownHeat = 50;

                // heat effects: start up
                if (entity.heat < autoShutDownHeat && entity.isShutDown()) {
                    if (entity.heat < 14) {
                        // automatically starts up again
                        entity.setShutDown(false);
                        r = new Report(5045);
                        r.subject = entity.getId();
                        r.addDesc(entity);
                        addReport(r);
                    } else {
                        // roll for startup
                        int startup = 4 + (entity.heat - 14) / 4 * 2;
                        if (mtHeat) {
                            startup -= 5;
                            switch (entity.crew.getPiloting()) {
                            case 0:
                            case 1:
                                startup -= 2;
                                break;
                            case 2:
                            case 3:
                                startup -= 1;
                                break;
                            case 6:
                            case 7:
                                startup += 1;
                                break;
                            }
                        }
                        int suroll = Compute.d6(2);
                        r = new Report(5050);
                        r.subject = entity.getId();
                        r.addDesc(entity);
                        r.add(startup);
                        r.add(suroll);
                        if (suroll >= startup) {
                            // start 'er back up
                            entity.setShutDown(false);
                            r.choose(true);
                        } else {
                            r.choose(false);
                        }
                        addReport(r);
                    }
                }

                // heat effects: shutdown!
                else if (entity.heat >= 14 && !entity.isShutDown()) {
                    if (entity.heat >= autoShutDownHeat) {
                        r = new Report(5055);
                        r.subject = entity.getId();
                        r.addDesc(entity);
                        addReport(r);
                        // okay, now mark shut down
                        entity.setShutDown(true);
                    } else if (entity.heat >= 14) {
                        int shutdown = 4 + (entity.heat - 14) / 4 * 2;
                        if (mtHeat) {
                            shutdown -= 5;
                            switch (entity.crew.getPiloting()) {
                            case 0:
                            case 1:
                                shutdown -= 2;
                                break;
                            case 2:
                            case 3:
                                shutdown -= 1;
                                break;
                            case 6:
                            case 7:
                                shutdown += 1;
                                break;
                            }
                        }
                        int sdroll = Compute.d6(2);
                        r = new Report(5060);
                        r.subject = entity.getId();
                        r.addDesc(entity);
                        r.add(shutdown);
                        r.add(sdroll);
                        if (sdroll >= shutdown) {
                            // avoided
                            r.choose(true);
                            addReport(r);
                        } else {
                            // shutting down...
                            r.choose(false);
                            addReport(r);
                            // okay, now mark shut down
                            entity.setShutDown(true);
                        }
                    }
                }

                // heat effects: control effects (must make it unless already
                // random moving)
                if (entity.heat >= 5 && !a.isRandomMove()) {
                    int controlavoid = 5 + (entity.heat >= 10 ? 1 : 0) + (entity.heat >= 15 ? 1 : 0) + (entity.heat >= 20 ? 1 : 0) + (entity.heat >= 25 ? 2 : 0);
                    int controlroll = Compute.d6(2);
                    r = new Report(9210);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(controlavoid);
                    r.add(controlroll);
                    if (controlroll >= controlavoid) {
                        // in control
                        r.choose(true);
                        addReport(r);
                    } else {
                        // out of control
                        r.choose(false);
                        addReport(r);
                        // if not already out of control, this may lead to
                        // elevation decline
                        if (!a.isOutControl() && game.getBoard().inAtmosphere()) {
                            int loss = Compute.d6(1);
                            r = new Report(9366);
                            r.newlines = 0;
                            r.subject = entity.getId();
                            r.addDesc(entity);
                            r.add(loss);
                            addReport(r);
                            // check for crash
                            if ((a.getElevation() - loss) <= game.getBoard().getHex(a.getPosition()).ceiling()) {
                                a.setElevation(0);
                                addReport(processCrash(entity, a.getCurrentVelocity()));
                            } else {
                                a.setElevation(a.getElevation() - loss);
                            }
                        }
                        // force unit out of control through heat
                        a.setOutCtrlHeat(true);
                        a.setRandomMove(true);
                    }
                } //End of Entity Instance of Aero

                // heat effects: ammo explosion!
                if (entity.heat >= 19) {
                    int boom = 4 + (entity.heat >= 23 ? 2 : 0) + (entity.heat >= 28 ? 2 : 0);
                    if (mtHeat) {
                        boom += (entity.heat >= 35 ? 2 : 0) + (entity.heat >= 40 ? 2 : 0) + (entity.heat >= 45 ? 2 : 0);
                        // Last line is a crutch; 45 heat should be no roll
                        // but automatic explosion.
                    }
                    int boomroll = Compute.d6(2);
                    r = new Report(5065);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(boom);
                    r.add(boomroll);
                    if (boomroll >= boom) {
                        // mech is ok
                        r.choose(true);
                        addReport(r);
                    } else {
                        // boom!
                        r.choose(false);
                        addReport(r);
                        addReport(explodeAmmoFromHeat(entity));
                    }
                }

                // heat effects: pilot damage
                if (entity.heat >= 21) {
                    int ouch = 6 + (entity.heat >= 27 ? 3 : 0);
                    int ouchroll = Compute.d6(2);
                    r = new Report(5075);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(ouch);
                    r.add(ouchroll);
                    if (ouchroll >= ouch) {
                        // pilot is ok
                        r.choose(true);
                        addReport(r);
                    } else {
                        // pilot is hurting
                        r.choose(false);
                        addReport(r);
                        addReport(damageCrew(entity, 1));
                    }
                }

                // The pilot may have just expired.
                if ((entity.crew.isDead() || entity.crew.isDoomed()) && !entity.crew.isEjected()) {
                    r = new Report(5080);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    addReport(r);
                    addReport(destroyEntity(entity, "pilot death", true));
                }

                continue;
            }

            // heat doesn't matter for non-mechs
            if (!(entity instanceof Mech)) {
                entity.heatBuildup = 0;
                entity.heatFromExternal = 0;

                if (entity.infernos.isStillBurning()) {
                    doFlamingDamage(entity);
                }
                continue;
            }
            // Meks gain heat from inferno hits.
            if (entity.infernos.isStillBurning()) {
                int infernoHeat = entity.infernos.getHeat();
                entity.heatFromExternal += infernoHeat;
                r = new Report(5010);
                r.subject = entity.getId();
                r.add(infernoHeat);
                addReport(r);
            }

            // should we even bother?
            if (entity.isDestroyed() || entity.isDoomed() || entity.crew.isDoomed() || entity.crew.isDead()) {
                continue;
            }

            // engine hits add a lot of heat, provided the engine is on
            entity.heatBuildup += entity.getEngineCritHeat();

            // If a Mek had an active Stealth suite, add 10 heat.
            if (entity instanceof Mech && entity.isStealthActive()) {
                entity.heatBuildup += 10;
                r = new Report(5015);
                r.subject = entity.getId();
                addReport(r);
            }
            
            //void sig adds 10 heat
            if (entity instanceof Mech && entity.isVoidSigActive()) {
                entity.heatBuildup += 10;
                r = new Report(5016);
                r.subject = entity.getId();
                addReport(r);
            }
            
            //null sig adds 10 heat
            if (entity instanceof Mech && entity.isNullSigActive()) {
                entity.heatBuildup += 10;
                r = new Report(5017);
                r.subject = entity.getId();
                addReport(r);
            }
            
            //chameleon polarization field adds 6
            if (entity instanceof Mech && entity.isChameleonShieldActive()) {
                entity.heatBuildup += 6;
                r = new Report(5014);
                r.subject = entity.getId();
                addReport(r);
            }   
            
            // If a Mek is in extreme Temperatures, add or subtract one
            // heat per 10 degrees (or fraction of 10 degrees) above or
            // below 50 or -30 degrees Celsius
            if (entity instanceof Mech && game.getPlanetaryConditions().getTemperatureDifference(50, -30) != 0 && !((Mech) entity).hasLaserHeatSinks()) {
                if (game.getPlanetaryConditions().getTemperature() > 50) {
                    entity.heatFromExternal += game.getPlanetaryConditions().getTemperatureDifference(50, -30);
                    r = new Report(5020);
                    r.subject = entity.getId();
                    r.add(game.getPlanetaryConditions().getTemperatureDifference(50, -30));
                    addReport(r);
                } else {
                    entity.heatFromExternal -= game.getPlanetaryConditions().getTemperatureDifference(50, -30);
                    r = new Report(5025);
                    r.subject = entity.getId();
                    r.add(game.getPlanetaryConditions().getTemperatureDifference(50, -30));
                    addReport(r);
                }
            }

            // Add +5 Heat if the hex you're in is on fire
            // and was on fire for the full round.
            if (entityHex != null) {
                if (entityHex.containsTerrain(Terrains.FIRE) && entityHex.getFireTurn() > 0
                        && entity.getElevation() <= 1) {
                    entity.heatFromExternal += 5;
                    r = new Report(5030);
                    r.subject = entity.getId();
                    addReport(r);
                }
                int magma = entityHex.terrainLevel(Terrains.MAGMA);
                if (magma > 0 && entity.getElevation() == 0) {
                    entity.heatFromExternal += 5 * magma;
                    r = new Report(5032);
                    r.subject = entity.getId();
                    r.add(5 * magma);
                    addReport(r);
                }
            }

            // Check the mech for vibroblades if so then check to see if any
            // are active and what heat they will produce.
            if (entity.hasVibroblades()) {
                int vibroHeat = 0;

                vibroHeat = entity.getActiveVibrobladeHeat(Mech.LOC_RARM);
                vibroHeat += entity.getActiveVibrobladeHeat(Mech.LOC_LARM);

                if (vibroHeat > 0) {
                    r = new Report(5018);
                    r.subject = entity.getId();
                    r.add(vibroHeat);
                    addReport(r);
                    entity.heatBuildup += vibroHeat;
                }
            }

            int capHeat = 0;
            for (Mounted m : entity.getEquipment()) {
                if (m.hasChargedCapacitor() && !m.isUsedThisRound()) {
                    capHeat += 5;
                }
            }
            if (capHeat > 0) {
                r = new Report(5019);
                r.subject = entity.getId();
                r.add(capHeat);
                addReport(r);
                entity.heatBuildup += capHeat;
            }

            // Add heat from external sources to the heat buildup
            entity.heatBuildup += Math.min(15, entity.heatFromExternal);
            entity.heatFromExternal = 0;

            // if heatbuildup is negative due to temperature, set it to 0
            // for prettier turnreports
            if (entity.heatBuildup < 0) {
                entity.heatBuildup = 0;
            }

            // add the heat we've built up so far.
            entity.heat += entity.heatBuildup;

            // how much heat can we sink?
            int tosink = entity.getHeatCapacityWithWater();
            

            if ( entity.getCoolantFailureAmount() > 0 ){
                int failureAmount = entity.getCoolantFailureAmount(); 
                r = new Report(5520);
                r.subject = entity.getId();
                r.add(failureAmount);
                tosink -= failureAmount;
            }
            
            // should we use a coolant pod?
            int safeHeat = entity.hasInfernoAmmo() ? 9 : 13;
            int possibleSinkage = ((Mech) entity).getNumberOfSinks() - entity.getCoolantFailureAmount();
            for (Mounted m : entity.getEquipment()) {
                if (m.getType() instanceof AmmoType) {
                    AmmoType at = (AmmoType) m.getType();
                    if (at.getAmmoType() == AmmoType.T_COOLANT_POD && m.isAmmoUsable()) {
                        EquipmentMode mode = m.curMode();
                        if (mode.equals("dump")) {
                            r = new Report(5260);
                            r.subject = entity.getId();
                            addReport(r);
                            m.setShotsLeft(0);
                            tosink += possibleSinkage;
                            break;
                        }
                        if (mode.equals("safe") && entity.heat - tosink > safeHeat) {
                            r = new Report(5265);
                            r.subject = entity.getId();
                            addReport(r);
                            m.setShotsLeft(0);
                            tosink += possibleSinkage;
                            break;
                        }
                        if (mode.equals("efficient") && entity.heat - tosink >= possibleSinkage) {
                            r = new Report(5270);
                            r.subject = entity.getId();
                            addReport(r);
                            m.setShotsLeft(0);
                            tosink += possibleSinkage;
                            break;
                        }
                    }
                }
            }

            tosink = Math.min(tosink, entity.heat);
            entity.heat -= tosink;
            r = new Report(5035);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(entity.heatBuildup);
            r.add(tosink);
            r.add(entity.heat);
            addReport(r);
            entity.heatBuildup = 0;

            // Does the unit have inferno ammo?
            if (entity.hasInfernoAmmo()) {

                // Roll for possible inferno ammo explosion.
                if (entity.heat >= 10) {
                    int boom = 4 + (entity.heat >= 14 ? 2 : 0) + (entity.heat >= 19 ? 2 : 0) + (entity.heat >= 23 ? 2 : 0) + (entity.heat >= 28 ? 2 : 0);
                    int boomroll = Compute.d6(2);
                    r = new Report(5040);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(boom);
                    r.add(boomroll);

                    if (boomroll >= boom) {
                        // avoided
                        r.choose(true);
                        addReport(r);
                    } else {
                        r.choose(false);
                        addReport(r);
                        addReport(explodeInfernoAmmoFromHeat(entity));
                    }
                }
            } // End avoid-inferno-explosion
            int autoShutDownHeat;
            boolean mtHeat;

            if (game.getOptions().booleanOption("tacops_heat")) {
                autoShutDownHeat = 50;
                mtHeat = true;
            } else {
                autoShutDownHeat = 30;
                mtHeat = false;
            }
            // heat effects: start up
            if (entity.heat < autoShutDownHeat && entity.isShutDown()) {
                if (entity.heat < 14) {
                    // automatically starts up again
                    entity.setShutDown(false);
                    r = new Report(5045);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    addReport(r);
                } else {
                    // roll for startup
                    int startup = 4 + (entity.heat - 14) / 4 * 2;
                    if (mtHeat) {
                        startup -= 5;
                        switch (entity.crew.getPiloting()) {
                        case 0:
                        case 1:
                            startup -= 2;
                            break;
                        case 2:
                        case 3:
                            startup -= 1;
                            break;
                        case 6:
                        case 7:
                            startup += 1;
                        }
                        if (entity instanceof QuadMech)
                            startup -= 2;
                    }
                    int suroll = Compute.d6(2);
                    r = new Report(5050);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(startup);
                    r.add(suroll);
                    if (suroll >= startup) {
                        // start 'er back up
                        entity.setShutDown(false);
                        r.choose(true);
                    } else {
                        r.choose(false);
                    }
                    addReport(r);
                }
            }

            // heat effects: shutdown!
            // 2003-01-26 JAD - Don't shut down if you just restarted.
            else if (entity.heat >= 14 && !entity.isShutDown()) {
                if (entity.heat >= autoShutDownHeat) {
                    r = new Report(5055);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    addReport(r);
                    // add a piloting roll and resolve immediately
                    game.addPSR(new PilotingRollData(entity.getId(), 3, "reactor shutdown"));
                    addReport(resolvePilotingRolls());
                    // okay, now mark shut down
                    entity.setShutDown(true);
                } else if (entity.heat >= 14) {
                    int shutdown = 4 + (entity.heat - 14) / 4 * 2;
                    if (mtHeat) {
                        shutdown -= 5;
                        switch (entity.crew.getPiloting()) {
                        case 0:
                        case 1:
                            shutdown -= 2;
                            break;
                        case 2:
                        case 3:
                            shutdown -= 1;
                            break;
                        case 6:
                        case 7:
                            shutdown += 1;
                        }
                        if (entity instanceof QuadMech)
                            shutdown -= 2;
                    }
                    int sdroll = Compute.d6(2);
                    r = new Report(5060);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(shutdown);
                    r.add(sdroll);
                    if (sdroll >= shutdown) {
                        // avoided
                        r.choose(true);
                        addReport(r);
                    } else {
                        // shutting down...
                        r.choose(false);
                        addReport(r);
                        // add a piloting roll and resolve immediately
                        game.addPSR(new PilotingRollData(entity.getId(), 3, "reactor shutdown"));
                        addReport(resolvePilotingRolls());
                        // okay, now mark shut down
                        entity.setShutDown(true);
                    }
                }
            }

            // heat effects: ammo explosion!
            if (entity.heat >= 19) {
                int boom = 4 + (entity.heat >= 23 ? 2 : 0) + (entity.heat >= 28 ? 2 : 0);
                if (mtHeat) {
                    boom += (entity.heat >= 35 ? 2 : 0) + (entity.heat >= 40 ? 2 : 0) + (entity.heat >= 45 ? 2 : 0);
                    // Last line is a crutch; 45 heat should be no roll
                    // but automatic explosion.
                }
                if (entity instanceof Mech && ((Mech) entity).hasLaserHeatSinks())
                    boom--;
                int boomroll = Compute.d6(2);
                r = new Report(5065);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(boom);
                r.add(boomroll);
                if (boomroll >= boom) {
                    // mech is ok
                    r.choose(true);
                    addReport(r);
                } else {
                    // boom!
                    r.choose(false);
                    addReport(r);
                    addReport(explodeAmmoFromHeat(entity));
                }
            }

            // heat effects: mechwarrior damage
            // N.B. The pilot may already be dead.
            int lifeSupportCritCount = 0;
            boolean torsoMountedCockpit = ((Mech) entity).getCockpitType() == Mech.COCKPIT_TORSO_MOUNTED;
            if (entity instanceof Mech && torsoMountedCockpit) {
                lifeSupportCritCount = entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, Mech.LOC_RT);
                lifeSupportCritCount += entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, Mech.LOC_LT);
            } else {
                lifeSupportCritCount = entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, Mech.LOC_HEAD);
            }
            if (lifeSupportCritCount > 0 && (entity.heat >= 15 || torsoMountedCockpit && entity.heat >= 0) && !entity.crew.isDead() && !entity.crew.isDoomed() && !entity.crew.isEjected()) {
                int heatLimitDesc = 1;
                int damageToCrew = 0;
                if (entity.heat >= 47 && mtHeat) {
                    // mechwarrior takes 5 damage
                    heatLimitDesc = 47;
                    damageToCrew = 5;
                } else if (entity.heat >= 39 && mtHeat) {
                    // mechwarrior takes 4 damage
                    heatLimitDesc = 39;
                    damageToCrew = 4;
                } else if (entity.heat >= 32 && mtHeat) {
                    // mechwarrior takes 3 damage
                    heatLimitDesc = 32;
                    damageToCrew = 3;
                } else if (entity.heat >= 25) {
                    // mechwarrior takes 2 damage
                    heatLimitDesc = 25;
                    damageToCrew = 2;
                } else if (entity.heat >= 15) {
                    // mechwarrior takes 1 damage
                    heatLimitDesc = 15;
                    damageToCrew = 1;
                }
                if (entity.heat > 0 && entity instanceof Mech && ((Mech) entity).getCockpitType() == Mech.COCKPIT_TORSO_MOUNTED && !entity.crew.getOptions().booleanOption("pain_shunt"))
                    damageToCrew += 1;
                r = new Report(5070);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(heatLimitDesc);
                r.add(damageToCrew);
                addReport(r);
                addReport(damageCrew(entity, damageToCrew));
            } else if (mtHeat && entity.heat >= 32 && !entity.crew.isDead() && !entity.crew.isDoomed() && !entity.crew.getOptions().booleanOption("pain_shunt")) {
                // Pilot may take damage from heat if MaxTech option is set
                int heatroll = Compute.d6(2);
                int avoidNumber = -1;
                if (entity.heat >= 47) {
                    avoidNumber = 12;
                } else if (entity.heat >= 39) {
                    avoidNumber = 10;
                } else if (entity.heat >= 32) {
                    avoidNumber = 8;
                }
                r = new Report(5075);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(avoidNumber);
                r.add(heatroll);
                if (heatroll >= avoidNumber) {
                    // damage avoided
                    r.choose(true);
                    addReport(r);
                } else {
                    r.choose(false);
                    addReport(r);
                    addReport(damageCrew(entity, 1));
                }
            }

            // The pilot may have just expired.
            if ((entity.crew.isDead() || entity.crew.isDoomed()) && !entity.crew.isEjected()) {
                r = new Report(5080);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
                addReport(destroyEntity(entity, "crew death", true));
            }

            // With MaxTech Heat Scale, there may occur critical damage
            if (mtHeat) {
                if (entity.heat >= 36) {
                    int damageroll = Compute.d6(2);
                    int damageNumber = -1;
                    if (entity.heat >= 44) {
                        damageNumber = 10;
                    } else if (entity.heat >= 36) {
                        damageNumber = 8;
                    }
                    r = new Report(5085);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(damageNumber);
                    r.add(damageroll);
                    r.newlines = 0;
                    if (damageroll >= damageNumber) {
                        r.choose(true);
                        addReport(r);
                    } else {
                        r.choose(false);
                        addReport(r);
                        addReport(oneCriticalEntity(entity, Compute.randomInt(8)));
                        // add an empty report, for linebreaking
                        r = new Report(1210, Report.PUBLIC);
                        addReport(r);
                    }
                }
            }
            
            if ( game.getOptions().booleanOption("tacops_coolant_failure") 
                    && entity.getHeatCapacity() > entity.getCoolantFailureAmount() 
                    && entity.heat >= 5){
                int roll = Compute.d6(2);
                int hitNumber = 10;
                
                hitNumber -= Math.max(0, (int)Math.ceil(entity.heat/5.0)-2);
                
                r = new Report(5525);
                r.subject = entity.getId();
                r.add(entity.getShortName());
                r.add(hitNumber);
                r.add(roll);
                r.newlines = 0;
                addReport(r);
                if ( roll >= hitNumber ){
                    r = new Report(5052);
                    r.subject = entity.getId();
                    addReport(r);
                    r = new Report(5526);
                    r.subject = entity.getId();
                    r.add(entity.getShortNameRaw());
                    addReport(r);
                    entity.addCoolantFailureAmount(1);
                }else {
                    r = new Report(5041);
                    r.subject = entity.getId();
                    addReport(r);
                }
            }
        }
        
        if (vPhaseReport.size() == 1) {
            // I guess nothing happened...
            addReport(new Report(1205, Report.PUBLIC));
        }
    }

    /**
     * check to see if unarmored infantry is outside in extreme temperatures
     * (crude fix because infantry shouldn't be able to be deployed outside of
     * vehicles or buildings, but we can't do that because we don't know wether
     * the map has buildings or not or wether the player has an apc No longer
     * Used TacOps allows for infantry to easily equip Exteme Weather Gear.
     */
    @Deprecated
    private void resolveExtremeTempInfantryDeath() {
        /*
         * int heat = game.getTemperatureDifference(); if (heat > 0) { for
         * (Enumeration<Entity> i = game.getEntities(); i .hasMoreElements();) {
         * Entity entity = i.nextElement(); if (null == entity.getPosition()) {
         * continue; } IHex entityHex =
         * game.getBoard().getHex(entity.getPosition()); if (entity instanceof
         * Infantry && !(entity instanceof BattleArmor) &&
         * !entityHex.containsTerrain(Terrains.BUILDING) &&
         * entity.getTransportId() == Entity.NONE) { if
         * (game.getOptions().booleanOption( "extreme_temperature_survival")) {
         * Report r = new Report(5310); r.subject = entity.getId();
         * r.addDesc(entity); r.add(heat); int roll = Compute.d6(); r.add(roll);
         * addReport(r); if (roll <= heat) { if (entity instanceof MechWarrior) {
         * addReport(damageCrew(entity, 1)); } else {
         * addReport(damageEntity(entity, entity
         * .rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Compute
         * .d6() + heat)); } r = new Report(1210, Report.PUBLIC); addReport(r); } }
         * else { Report r = new Report(5090); r.subject = entity.getId();
         * r.addDesc(entity); addReport(r); addReport(destroyEntity(entity,
         * "heat/cold", false, false)); } } } }
         */
    }

    /**
     * Resolve Flaming Damage for the given Entity Taharqa: This is now updated
     * to TacOps rules which is much more lenient So I have change the name to
     * Flaming Damage rather than flaming death
     * 
     * @param entity
     *            The <code>Entity</code> that may experience flaming damage.
     */
    private void doFlamingDamage(Entity entity) {
        Report r;
        int boomroll = Compute.d6(2);
        // Infantry are unaffected by fire while they're still swarming.
        if (Entity.NONE != entity.getSwarmTargetId()) {
            return;
        }
        if (entity.getMovementMode() == IEntityMovementMode.VTOL && !entity.infernos.isStillBurning()) {
            // VTOLs don't check as long as they are flying higher than
            // the burning terrain. TODO: Check for rules conformity (ATPM?)
            // according to maxtech, elevation 0 or 1 should be affected,
            // this makes sense for level 2 as well

            if (entity.getElevation() > 1) {
                return;
            }
        }
        // Battle Armor squads equipped with fire protection
        // gear automatically avoid flaming damage
        // TODO: can conv. infantry mount fire-resistant armor?
        for (Mounted mount : entity.getMisc()) {
            EquipmentType equip = mount.getType();
            if (equip.hasFlag(MiscType.F_FIRE_RESISTANT)) {
                r = new Report(5095);
                r.subject = entity.getId();
                r.indent(1);
                r.addDesc(entity);
                addReport(r);
                return;
            }
        }

        // mechs shouldn't be here, but just in case
        // TODO: what to do with Aeros?
        if (entity instanceof Mech) {
            return;
        }

        // Must roll 8+ to survive...
        r = new Report(5100);
        r.subject = entity.getId();
        r.newlines = 0;
        r.addDesc(entity);
        r.add(boomroll);
        if (boomroll >= 8) {
            // phew!
            r.choose(true);
               addReport(r);
        } else {
            // eek
            r.choose(false);
            r.newlines = 1;
            addReport(r);
            // Taharqa: TacOps rules, protos and vees no longer die instantly
            // (hurray!)
            if (entity instanceof Tank) {
                int bonus = -2;
                if (entity instanceof SupportTank || entity instanceof SupportVTOL) {
                    bonus = 0;
                }
                // roll a critical hit
                Report.addNewline(vPhaseReport);
                addReport(criticalTank((Tank) entity, Tank.LOC_FRONT, bonus));
            } else if (entity instanceof Protomech) {
                // this code is taken from inferno hits
                HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                if (hit.getLocation() == Protomech.LOC_NMISS) {
                    r = new Report(6035);
                    r.subject = entity.getId();
                    r.indent(1);
                    addReport(r);
                } else {
                    r = new Report(6690);
                    r.subject = entity.getId();
                    r.indent(1);
                    r.add(entity.getLocationName(hit));
                    addReport(r);
                    entity.destroyLocation(hit.getLocation());
                    // Handle Protomech pilot damage
                    // due to location destruction
                    int hits = Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()] - ((Protomech) entity).getPilotDamageTaken(hit.getLocation());
                    if (hits > 0) {
                        addReport(damageCrew(entity, hits));
                        ((Protomech) entity).setPilotDamageTaken(hit.getLocation(), Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()]);
                    }
                    if (entity.getTransferLocation(hit).getLocation() == Entity.LOC_DESTROYED) {
                        addReport(destroyEntity(entity, "flaming death", false, true));
                        Report.addNewline(vPhaseReport);
                    }
                }
            } else {
                // sucks to be you
                addReport(destroyEntity(entity, "fire", false, false));
                Report.addNewline(vPhaseReport);
            }
        }
    }

    /**
     * Checks to see if any entity takes enough damage that requires them to
     * make a piloting roll
     */
    private void checkForPSRFromDamage() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (entity instanceof Mech) {
                // if this mech has 20+ damage, add another roll to the list.
                if (entity.damageThisPhase >= 20) {
                    if (game.getOptions().booleanOption("tacops_taking_damage")) {
                        int damMod = entity.damageThisPhase / 20;
                        int weightMod = 0;
                        StringBuffer reportStr = new StringBuffer();
                        reportStr.append(entity.damageThisPhase).append(" damage +").append(damMod);

                        if (game.getOptions().booleanOption("tacops_physical_psr")) {
                            switch (entity.getWeightClass()) {
                            case EntityWeightClass.WEIGHT_LIGHT:
                                weightMod = 1;
                                break;

                            case EntityWeightClass.WEIGHT_MEDIUM:
                                weightMod = 0;
                                break;

                            case EntityWeightClass.WEIGHT_HEAVY:
                                weightMod = -1;
                                break;

                            case EntityWeightClass.WEIGHT_ASSAULT:
                                weightMod = -2;
                                break;
                            }
                        }
                        if (weightMod > 0)
                            reportStr.append(", weight class modifier +").append(weightMod);
                        else
                            reportStr.append(", weight class modifier ").append(weightMod);

                        PilotingRollData damPRD = new PilotingRollData(entity.getId(), damMod + weightMod, reportStr.toString());
                        damPRD.setCumulative(false); // see Bug# 811987 for
                        // more info
                        game.addPSR(damPRD);
                    } else {
                        game.addPSR(new PilotingRollData(entity.getId(), 1, "20+ damage"));
                    }
                }
            }
            if (entity instanceof Aero && game.getBoard().inAtmosphere()) {
                // if this aero has any damage, add another roll to the list.
                if (entity.damageThisPhase > 0) {
                    int damMod = entity.damageThisPhase / 20;
                    StringBuffer reportStr = new StringBuffer();
                    reportStr.append(entity.damageThisPhase).append(" damage +").append(damMod);
                    PilotingRollData damPRD = new PilotingRollData(entity.getId(), damMod, reportStr.toString());
                    game.addControlRoll(damPRD);
                }
            }
        }
    }

    /**
     * Checks to see if any non-mech units are standing in fire. Called at the
     * end of the movement phase
     */
    public void checkForFlamingDamage() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (null == entity.getPosition() || entity instanceof Mech || entity.isDoomed() || entity.isDestroyed() || entity.isOffBoard()) {
                continue;
            }
            final IHex curHex = game.getBoard().getHex(entity.getPosition());
            if (curHex.containsTerrain(Terrains.FIRE) && entity.getElevation() <= 1) {
                doFlamingDamage(entity);
            }
        }
    }

    /**
     * Checks to see if any tele-missiles are in a hex with enemy units. If so,
     * then attack one.
     */
    private void checkForTeleMissileAttacks() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (entity instanceof TeleMissile) {
                // check for enemy units
                Vector<Integer> potTargets = new Vector<Integer>();
                for (Enumeration<Entity> j = game.getEntities(entity.getPosition()); j.hasMoreElements();) {
                    final Entity te = j.nextElement();
                    if (te.isEnemyOf(entity)) {
                        // then add it to a vector of potential targets
                        potTargets.add(te.getId());
                    }
                }
                if (potTargets.size() > 0) {
                    // determine randomly
                    Entity target = game.getEntity(potTargets.get(Compute.randomInt(potTargets.size())));
                    // report this and add a new TeleMissileAttackAction
                    Report r = new Report(9085);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.addDesc(target);
                    addReport(r);
                    game.addTeleMissileAttack(new TeleMissileAttackAction(entity, target));
                }
            }
        }
    }

    /**
     * Check to see if anyone dies due to being in certain planetary conditions.
     */
    private void checkForConditionDeath() {
        Report r;
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (null == entity.getPosition() || entity.isOffBoard()) {
                // If it's not on the board - aboard something else, for
                // example...
                continue;
            }
            String reason = game.getPlanetaryConditions().whyDoomed(entity);
            if (null != reason) {
                r = new Report(6015);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.add(reason);
                addReport(r);
                addReport(destroyEntity(entity, reason, true, true));
            }
        }
    }

    /**
     * Check to see if anyone dies due to being in atmosphere.
     */
    private void checkForAtmosphereDeath() {
        Report r;
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (null == entity.getPosition() || entity.isOffBoard()) {
                // If it's not on the board - aboard something else, for
                // example...
                continue;
            }
            if (entity.doomedInAtmosphere()) {
                r = new Report(6016);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
                addReport(destroyEntity(entity, "being in atmosphere where it can't survive", true, true));
            }
        }
    }

    /**
     * Check to see if anyone dies due to being in space.
     */
    private void checkForSpaceDeath() {
        Report r;
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (null == entity.getPosition() || entity.isOffBoard()) {
                // If it's not on the board - aboard something else, for
                // example...
                continue;
            }
            if (entity.doomedInSpace()) {
                r = new Report(6017);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
                addReport(destroyEntity(entity, "being in space where it can't survive", true, true));
            }
        }
    }

    /**
     * Checks to see if any entities are underwater (or in vacuum) with damaged life support.
     * Called during the end phase.
     */
    private void checkForSuffocation() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (null == entity.getPosition() || entity.isOffBoard()) {
                continue;
            }
            final IHex curHex = game.getBoard().getHex(entity.getPosition());
            if (((entity.getElevation() < 0 && (curHex.terrainLevel(Terrains.WATER) > 1 || curHex.terrainLevel(Terrains.WATER) == 1 && entity.isProne()))
                    || game.getPlanetaryConditions().isVacuum()) 
                    && entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, Mech.LOC_HEAD) > 0) {
                Report r = new Report(6020);
                r.subject = entity.getId();
                r.addDesc(entity);
                addReport(r);
                addReport(damageCrew(entity, 1));

            }
        }
    }

    /**
     * Resolves all built up piloting skill rolls. Used at end of weapons,
     * physical phases.
     */
    private Vector<Report> resolvePilotingRolls() {
        Vector<Report> vPhaseReport = new Vector<Report>();
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            vPhaseReport.addAll(resolvePilotingRolls(i.nextElement()));
        }
        game.resetPSRs();
        return vPhaseReport;
    }

    /**
     * Resolves and reports all piloting skill rolls for a single mech.
     */
    private Vector<Report> resolvePilotingRolls(Entity entity) {
        return resolvePilotingRolls(entity, false, entity.getPosition(), entity.getPosition());
    }

    private Vector<Report> resolvePilotingRolls(Entity entity, boolean moving, Coords src, Coords dest) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        // dead and undeployed and offboard units don't need to.
        if (entity.isDoomed() || entity.isDestroyed() || entity.isOffBoard() || !entity.isDeployed()) {
            return vPhaseReport;
        }
        Report r;

        // first, do extreme gravity PSR, because non-mechs do these, too
        PilotingRollData rollTarget = null;
        for (Enumeration<PilotingRollData> i = game.getExtremeGravityPSRs(); i.hasMoreElements();) {
            final PilotingRollData roll = i.nextElement();
            if (roll.getEntityId() != entity.getId()) {
                continue;
            }
            // found a roll, use it (there can be only 1 per entity)
            rollTarget = roll;
            game.resetExtremeGravityPSRs(entity);
        }
        if (rollTarget != null && rollTarget.getValue() != TargetRoll.CHECK_FALSE) {
            // okay, print the info
            r = new Report(2180);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(rollTarget.getLastPlainDesc());
            vPhaseReport.add(r);
            // roll
            final int diceRoll = Compute.d6(2);
            r = new Report(2190);
            r.subject = entity.getId();
            r.add(rollTarget.getValueAsString());
            r.add(rollTarget.getDesc());
            r.add(diceRoll);
            if (diceRoll < rollTarget.getValue()) {
                r.choose(false);
                vPhaseReport.add(r);
                // walking and running, 1 damage per MP used more than we would
                // have normally
                if (entity.moved == IEntityMovementType.MOVE_WALK || entity.moved == IEntityMovementType.MOVE_VTOL_WALK || entity.moved == IEntityMovementType.MOVE_RUN || entity.moved == IEntityMovementType.MOVE_VTOL_RUN) {
                    if (entity instanceof Mech) {
                        int j = entity.mpUsed;
                        int damage = 0;
                        while (j > entity.getRunMP(false, false)) {
                            j--;
                            damage++;
                        }
                        // Wee, direct internal damage
                        vPhaseReport.addAll(doExtremeGravityDamage(entity, damage));
                    } else if (entity instanceof Tank) {
                        // if we got a pavement bonus, take care of it
                        int k = entity.gotPavementBonus ? 1 : 0;
                        if (!entity.gotPavementBonus) {
                            int j = entity.mpUsed;
                            int damage = 0;
                            while (j > entity.getRunMP(false, false) + k) {
                                j--;
                                damage++;
                            }
                            vPhaseReport.addAll(doExtremeGravityDamage(entity, damage));
                        }
                    }
                }
                // jumping
                if (entity.moved == IEntityMovementType.MOVE_JUMP && entity instanceof Mech) {
                    // low g, 1 damage for each hex jumped further than
                    // possible normally
                    if (game.getPlanetaryConditions().getGravity() < 1) {
                        int j = entity.mpUsed;
                        int damage = 0;
                        while (j > entity.getJumpMP(false)) {
                            j--;
                            damage++;
                        }
                        // Wee, direct internal damage
                        vPhaseReport.addAll(doExtremeGravityDamage(entity, damage));
                    }
                    // high g, 1 damage for each MP we have less than normally
                    else if (game.getPlanetaryConditions().getGravity() > 1) {
                        int damage = entity.getWalkMP(false, false) - entity.getWalkMP();
                        // Wee, direct internal damage
                        vPhaseReport.addAll(doExtremeGravityDamage(entity, damage));
                    }
                }
            } else {
                r.choose(true);
                vPhaseReport.add(r);
            }
        }
        // non mechs and prone mechs can now return
        if (!(entity instanceof Mech) || entity.isProne() || (entity.isHullDown() && entity.canGoHullDown()) ) {
            return vPhaseReport;
        }

        // Mechs with UMU float and don't have to roll???
        if (entity instanceof Mech) {
            IHex hex = game.getBoard().getHex(dest);
            int water = hex.terrainLevel(Terrains.WATER);
            if (water > 0 && entity.getElevation() != -hex.depth() && (entity.getElevation() < 0 || (entity.getElevation() == 0 && hex.terrainLevel(Terrains.BRIDGE_ELEV) != 0 && !hex.containsTerrain(Terrains.ICE))) && !entity.isMakingDfa()) {
                // mech is floating in water....
                if (entity.hasUMU())
                    return vPhaseReport;
                game.addPSR(new PilotingRollData(entity.getId(), TargetRoll.AUTOMATIC_FAIL, "lost buoyancy"));
            }
        }

        // add all cumulative rolls, count all rolls
        Vector<PilotingRollData> rolls = new Vector<PilotingRollData>();
        StringBuffer reasons = new StringBuffer();
        PilotingRollData base = entity.getBasePilotingRoll();
        entity.addPilotingModifierForTerrain(base);
        for (Enumeration<PilotingRollData> i = game.getPSRs(); i.hasMoreElements();) {
            final PilotingRollData modifier = i.nextElement();
            if (modifier.getEntityId() != entity.getId()) {
                continue;
            }
            // found a roll, add it
            rolls.addElement(modifier);
            if (reasons.length() > 0) {
                reasons.append("; ");
            }
            reasons.append(modifier.getPlainDesc());
            // only cumulative rolls get added to the base roll
            if (modifier.isCumulative()) {
                base.append(modifier);
            }
        }
        // any rolls needed?
        if (rolls.size() == 0) {
            return vPhaseReport;
        }
        // is our base roll impossible?
        if (base.getValue() == TargetRoll.AUTOMATIC_FAIL || base.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(2275);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(rolls.size());
            r.add(reasons.toString()); // international issue
            r.add(base.getDesc()); // international issue
            vPhaseReport.add(r);
            if (moving) {
                vPhaseReport.addAll(doEntityFallsInto(entity, src, dest, base));
            } else {
                vPhaseReport.addAll(doEntityFall(entity, base));
            }
            return vPhaseReport;
        }
        // loop thru rolls we do have to make...
        r = new Report(2280);
        r.subject = entity.getId();
        r.addDesc(entity);
        r.add(rolls.size());
        r.add(reasons.toString()); // international issue
        vPhaseReport.add(r);
        r = new Report(2285);
        r.subject = entity.getId();
        r.add(base.getValueAsString());
        r.add(base.getDesc()); // international issue
        vPhaseReport.add(r);
        for (int i = 0; i < rolls.size(); i++) {
            PilotingRollData modifier = rolls.elementAt(i);
            PilotingRollData target = base;
            r = new Report(2290);
            r.subject = entity.getId();
            r.indent();
            r.newlines = 0;
            r.add(i + 1);
            r.add(modifier.getPlainDesc()); // international issue
            vPhaseReport.add(r);
            if (!modifier.isCumulative()) {
                // non-cumulative rolls only happen due to weight class adj.
                r = new Report(2295);
                r.subject = entity.getId();
                r.newlines = 0;
                r.add(modifier.getValueAsString()); // international issue
                target = new PilotingRollData(entity.getId());
                target.append(base);
                target.append(modifier);
            }
            int diceRoll = Compute.d6(2);
            r = new Report(2300);
            r.subject = entity.getId();
            r.add(target.getValueAsString());
            r.add(diceRoll);
            if (diceRoll < target.getValue()) {
                r.choose(false);
                vPhaseReport.add(r);
                if (moving) {
                    vPhaseReport.addAll(doEntityFallsInto(entity, src, dest, base));
                } else {
                    if ( entity instanceof Mech 
                            && game.getOptions().booleanOption("tacops_falling_expanded") 
                            && entity.getCrew().getPiloting() < 6
                            && !entity.isHullDown() 
                            && entity.canGoHullDown()){
                        if ( entity.getCrew().getPiloting() > 1 && target.getValue() - diceRoll < 2){
                            entity.setHullDown(true);
                        }else if ( entity.getCrew().getPiloting() <= 1 && target.getValue() - diceRoll < 3 ){
                            entity.setHullDown(true);
                        }
                        if ( entity.isHullDown() && entity.canGoHullDown() ){
                            r = new Report (2317);
                            r.subject = entity.getId();
                            r.add(entity.getDisplayName());
                            vPhaseReport.add(r);
                        }else{
                            vPhaseReport.addAll(doEntityFall(entity, base));
                        }

                    }else {
                        vPhaseReport.addAll(doEntityFall(entity, base));
                    }
                }
                return vPhaseReport;
            }
            r.choose(true);
            vPhaseReport.add(r);
        }
        return vPhaseReport;
    }

    /**
     * Resolves all built up control rolls. Used only during end phase
     */
    private Vector<Report> resolveControlRolls() {
        Vector<Report> vFullReport = new Vector<Report>();
        vFullReport.add(new Report(5001, Report.PUBLIC));
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            vFullReport.addAll(resolveControl(i.nextElement()));
        }
        game.resetControlRolls();
        return vFullReport;
    }

    /**
     * Resolves and reports all control skill rolls for a single aero.
     */
    private Vector<Report> resolveControl(Entity e) {
        Vector<Report> vReport = new Vector<Report>();
        if (e.isDoomed() || e.isDestroyed() || e.isOffBoard() || !e.isDeployed()) {
            return vReport;
        }
        Report r;

        /*
         * See forum answers on OOC
         * http://forums.classicbattletech.com/index.php/topic,20424.0.html
         */

        if (e instanceof Aero) {
            Aero a = (Aero) e;

            // they should get a shot at a recovery roll at the end of all this
            // if they are already out of control
            boolean canRecover = a.isOutControl();

            // if the unit already is moving randomly then it can't get any
            // worse
            if (!a.isRandomMove()) {

                // find control rolls and make them
                Vector<PilotingRollData> rolls = new Vector<PilotingRollData>();
                StringBuffer reasons = new StringBuffer();
                PilotingRollData base = e.getBasePilotingRoll();
                for (Enumeration<PilotingRollData> j = game.getControlRolls(); j.hasMoreElements();) {
                    final PilotingRollData modifier = j.nextElement();
                    if (modifier.getEntityId() != e.getId()) {
                        continue;
                    }
                    // found a roll, add it
                    rolls.addElement(modifier);
                    if (reasons.length() > 0) {
                        reasons.append("; ");
                    }
                    reasons.append(modifier.getPlainDesc());
                    // only cumulative rolls get added to the base roll
                    // FIXME: nuclear attacks are not resetting to
                    // non-cumulative for some reason
                    // when they are added. Until I resolve this, the following
                    // hack will have
                    // to suffice
                    if (modifier.getPlainDesc().contains("Nuclear")) {
                        modifier.setCumulative(false);
                    }
                    if (modifier.isCumulative()) {
                        base.append(modifier);
                    }
                }
                // any rolls needed?
                if (rolls.size() > 0) {
                    // loop thru rolls we do have to make...
                    r = new Report(9310);
                    r.subject = e.getId();
                    r.addDesc(e);
                    r.add(rolls.size());
                    r.add(reasons.toString()); // international issue
                    vReport.add(r);
                    r = new Report(2285);
                    r.subject = e.getId();
                    r.add(base.getValueAsString());
                    r.add(base.getDesc()); // international issue
                    vReport.add(r);
                    for (int j = 0; j < rolls.size(); j++) {
                        PilotingRollData modifier = rolls.elementAt(j);
                        PilotingRollData target = base;
                        r = new Report(2290);
                        r.subject = e.getId();
                        r.indent();
                        r.newlines = 0;
                        r.add(j + 1);
                        r.add(modifier.getPlainDesc()); // international issue
                        vReport.add(r);
                        if (!modifier.isCumulative()) {
                            // only for nuclear attacks
                            r = new Report(2296);
                            r.subject = e.getId();
                            r.newlines = 0;
                            r.add(modifier.getValueAsString()); // international
                                                                // issue
                            target = new PilotingRollData(e.getId());
                            target.append(base);
                            target.append(modifier);
                            vReport.add(r);
                        }
                        int diceRoll = Compute.d6(2);
                        // different reports depending on out-of-control status
                        if (a.isOutControl()) {
                            r = new Report(9360);
                            r.subject = e.getId();
                            r.add(target.getValueAsString());
                            r.add(diceRoll);
                            if (diceRoll < (target.getValue() - 5)) {
                                r.choose(false);
                                vReport.add(r);
                                a.setRandomMove(true);
                            } else {
                                r.choose(true);
                                vReport.add(r);
                            }
                        } else {
                            r = new Report(9315);
                            r.subject = e.getId();
                            r.add(target.getValueAsString());
                            r.add(diceRoll);
                            r.newlines = 1;
                            if (diceRoll < target.getValue()) {
                                r.choose(false);
                                vReport.add(r);
                                a.setOutControl(true);
                                // do we have random movement?
                                if ((target.getValue() - diceRoll) > 5) {
                                    r = new Report(9365);
                                    r.newlines = 0;
                                    r.subject = e.getId();
                                    vReport.add(r);
                                    a.setRandomMove(true);
                                }
                                // if on the atmospheric map, then lose altitude
                                // and check
                                // for crash
                                if (game.getBoard().inAtmosphere()) {
                                    int loss = Compute.d6(1);
                                    r = new Report(9366);
                                    r.newlines = 0;
                                    r.subject = e.getId();
                                    r.addDesc(e);
                                    r.add(loss);
                                    vReport.add(r);
                                    // check for crash
                                    if ((a.getElevation() - loss) <= game.getBoard().getHex(a.getPosition()).ceiling()) {
                                        a.setElevation(game.getBoard().getHex(a.getPosition()).surface());
                                        vReport.addAll(processCrash(e, a.getCurrentVelocity()));
                                    } else {
                                        a.setElevation(a.getElevation() - loss);
                                    }
                                }

                            } else {
                                r.choose(true);
                                vReport.add(r);
                            }
                        }
                    }
                }
            }

            // if they were out-of-control to start with, give them a chance to
            // regain control
            if (canRecover) {
                PilotingRollData base = e.getBasePilotingRoll();
                // is our base roll impossible?
                if (base.getValue() == PilotingRollData.AUTOMATIC_FAIL || base.getValue() == PilotingRollData.IMPOSSIBLE) {
                    // report something
                    r = new Report(9340);
                    r.subject = e.getId();
                    r.addDesc(e);
                    r.add(base.getDesc()); // international issue
                    vReport.add(r);
                    return vReport;
                }
                r = new Report(9345);
                r.subject = e.getId();
                r.addDesc(e);
                r.add(base.getDesc()); // international issue
                vReport.add(r);
                int diceRoll = Compute.d6(2);
                r = new Report(9350);
                r.subject = e.getId();
                r.add(base.getValueAsString());
                r.add(diceRoll);
                if (diceRoll < base.getValue()) {
                    r.choose(false);
                    vReport.add(r);
                } else {
                    r.choose(true);
                    vReport.add(r);
                    a.setOutControl(false);
                    a.setOutCtrlHeat(false);
                    a.setRandomMove(false);
                }
            }

        }
        return vReport;
    }

    /**
     * Inflict damage on a pilot
     * 
     * @param en
     *            The <code>Entity</code> who's pilot gets damaged.
     * @param damage
     *            The <code>int</code> amount of damage.
     */
    private Vector<Report> damageCrew(Entity en, int damage) {
        Vector<Report> vDesc = new Vector<Report>();
        Pilot crew = en.getCrew();
        if (!crew.isDead() && !crew.isEjected() && !crew.isDoomed()) {
            crew.setHits(crew.getHits() + damage);
            Report r = new Report(6025);
            r.subject = en.getId();
            r.indent(2);
            r.addDesc(en);
            r.add(crew.getName());
            r.add(damage);
            r.newlines = 0;
            vDesc.addElement(r);
            if (Pilot.DEATH > crew.getHits()) {
                vDesc.addAll(resolveCrewDamage(en, damage));
            } else if (!crew.isDoomed()) {
                crew.setDoomed(true);
                vDesc.addAll(destroyEntity(en, "pilot death", true));
            }
        }
        return vDesc;
    }

    /**
     * resolves consciousness rolls for one entity
     */
    private Vector<Report> resolveCrewDamage(Entity e, int damage) {
        Vector<Report> vDesc = new Vector<Report>();
        final int totalHits = e.getCrew().getHits();
        if (e instanceof MechWarrior || !e.isTargetable() || !e.getCrew().isActive() || damage == 0) {
            return vDesc;
        }

        // no consciousness roll for pain-shunted warriors
        if (e.crew.getOptions().booleanOption("pain_shunt")) {
            return vDesc;
        }

        for (int hit = totalHits - damage + 1; hit <= totalHits; hit++) {
            int rollTarget = Compute.getConsciousnessNumber(hit);
            boolean edgeUsed = false;
            do {
                if (edgeUsed)
                    e.crew.decreaseEdge();
                int roll = Compute.d6(2);
                if (e.getCrew().getOptions().booleanOption("pain_resistance"))
                    roll = Math.min(12, roll + 1);
                Report r = new Report(6030);
                r.indent(2);
                r.subject = e.getId();
                r.addDesc(e);
                r.add(e.getCrew().getName());
                r.add(rollTarget);
                r.add(roll);
                if (roll >= rollTarget) {
                    e.crew.setKoThisRound(false);
                    r.choose(true);
                } else {
                    e.crew.setKoThisRound(true);
                    r.choose(false);
                    if (e.crew.hasEdgeRemaining() && e.crew.getOptions().booleanOption("edge_when_ko")) {
                        edgeUsed = true;
                        vDesc.add(r);
                        r = new Report(6520);
                        r.subject = e.getId();
                        r.addDesc(e);
                        r.add(e.getCrew().getName());
                        r.add(e.crew.getOptions().intOption("edge"));
                    } // if
                    // return true;
                } // else
                vDesc.add(r);
            } while (e.crew.hasEdgeRemaining() && e.crew.isKoThisRound() && e.crew.getOptions().booleanOption("edge_when_ko"));
            // end of do-while
            if (e.crew.isKoThisRound()) {
                e.crew.setUnconscious(true);
                return vDesc;
            }
        }
        return vDesc;
    }

    /**
     * Make the rolls indicating whether any unconscious crews wake up
     */
    private void resolveCrewWakeUp() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity e = i.nextElement();

            // only unconscious pilots of mechs and protos and MechWarrirs
            // can roll to wake up
            if (!e.isTargetable() || !e.crew.isUnconscious() || e.crew.isKoThisRound() || !(e instanceof Mech || e instanceof Protomech || e instanceof MechWarrior)) {
                continue;
            }
            int roll = Compute.d6(2);

            if (e.getCrew().getOptions().booleanOption("pain_resistance"))
                roll = Math.min(12, roll + 1);

            int rollTarget = Compute.getConsciousnessNumber(e.crew.getHits());
            Report r = new Report(6029);
            r.subject = e.getId();
            r.addDesc(e);
            r.add(e.getCrew().getName());
            r.add(rollTarget);
            r.add(roll);
            if (roll >= rollTarget) {
                r.choose(true);
                e.crew.setUnconscious(false);
            } else {
                r.choose(false);
            }
            addReport(r);
        }
    }

    /**
     * Resolve any collected standard damage to a capital-scale entity This is
     * not where fighter squadron damage is resolved because that can only
     * happen after all firing is resolved
     */
    private Vector<Report> resolveStandardtoCapital(int cen) {

        Vector<Report> vDesc = new Vector<Report>();
        boolean needReport = false;
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity en = e.nextElement();
            if (!en.isCapitalScale() || !(en instanceof Aero) || en instanceof FighterSquadron || cen == Entity.NONE) {
                continue;
            }
            Aero ship = (Aero) en;
            Report r;
            boolean tookDamage = false;
            for (int i = 0; i < ship.locations(); i++) {
                int damage = ship.getStandardDamage(i);
                if (damage > 0) {
                    needReport = true;
                    if (!tookDamage) {
                        // put out a report
                        r = new Report(9055);
                        r.subject = en.getId();
                        r.indent(1);
                        r.newlines = 0;
                        r.addDesc(en);
                        vDesc.addElement(r);
                        tookDamage = true;
                    }
                    // I need to roll the hit location in order to determine the
                    // right critical, but I may have to reroll until the
                    // location matches
                    int sideHit = Compute.targetSideTable(game.getEntity(cen), en);
                    HitData hit = ship.rollHitLocation(ToHitData.HIT_NORMAL, sideHit);
                    // lets make sure we don't get any infinite loops
                    int j = 0;
                    while (hit.getLocation() != i && j < 200) {
                        j++;
                        hit = ship.rollHitLocation(ToHitData.HIT_NORMAL, sideHit);
                    }
                    if (hit.getLocation() != i) {
                        hit = new HitData(i);
                    }
                    damage = (int) Math.floor(damage / 10.0);
                    hit.setCapital(true);
                    vDesc.addAll(damageEntity(en, hit, damage));
                }
            }
            ship.resetStandardDamage();
        }
        // carriage return
        if (needReport) {
            Report finish;
            finish = new Report(1210, Report.PUBLIC);
            finish.newlines = 1;
            vDesc.addElement(finish);
        }
        return vDesc;
    }

    /**
     * loop through available entities find fighter squadrons and resolve any
     * remaining damage
     */
    private Vector<Report> resolveSquadronDamage() {

        Vector<Report> vDescFinal = new Vector<Report>();
        Vector<Report> vDesc = new Vector<Report>();

        Report header;
        Report r;

        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity en = e.nextElement();
            if (en instanceof FighterSquadron) {
                FighterSquadron fs = (FighterSquadron) en;
                header = new Report(9400);
                header.subject = en.getId();
                header.indent(0);
                header.newlines = 0;
                header.addDesc(en);
                int damage = fs.getStandardDamage();
                if (damage > 0) {
                    // put out a report
                    vDesc.addElement(header);
                    r = new Report(9055);
                    r.subject = en.getId();
                    r.indent(1);
                    r.newlines = 0;
                    r.addDesc(en);
                    vDesc.addElement(r);
                    // hmm--how I am I going to get the right side?
                    // this shouldn't cause a critical, so I just need to make
                    // sure that
                    // it doesn't somehow
                    HitData hit = fs.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    int newdamage = (int) Math.floor(damage / 10.0);
                    hit.setCapital(true);
                    vDesc.addAll(damageEntity(en, hit, newdamage));
                }
                fs.resetStandardDamage();
                // now check damage threshold
                int destroyed = fs.getDamageRound() / fs.getThresh();
                if (destroyed > 0) {
                    if (damage <= 0) {
                        vDesc.addElement(header);
                    }
                    fs.setNFighters(fs.getNFighters() - destroyed);
                    r = new Report(9405);
                    r.subject = en.getId();
                    r.indent(1);
                    r.newlines = 0;
                    r.addDesc(en);
                    r.add(fs.getDamageRound());
                    r.add(destroyed);
                    vDesc.addElement(r);
                    // check for destruction
                    if (fs.getNFighters() <= 0) {
                        vDesc.addAll(destroyEntity(en, "total annhiliation", true));
                    }
                }
                fs.resetDamageRound();
            }
        }
        if (vDesc.size() > 0) {
            vDescFinal.addElement(new Report(9410, Report.PUBLIC));
            vDescFinal.addAll(vDesc);
        }
        return vDescFinal;
    }

    /**
     * damage an Entity
     * 
     * @param te
     *            the <code>Entity</code> to be damaged
     * @param hit
     *            the corresponding <code>HitData</code>
     * @param damage
     *            the <code>int</code> amount of damage
     * @param ammoExplosion
     *            a <code>boolean</code> indicating if this is an
     *            ammoexplosion
     * @return a <code>Vector<Report></code> containg the phasereports
     */
    private Vector<Report> damageEntity(Entity te, HitData hit, int damage, boolean ammoExplosion) {
        return damageEntity(te, hit, damage, ammoExplosion, DamageType.NONE, false, false);
    }

    /**
     * Deals the listed damage to an entity. Returns a vector of Reports for the
     * phase report
     * 
     * @param te
     *            the target entity
     * @param hit
     *            the hit data for the location hit
     * @param damage
     *            the damage to apply
     * @return a <code>Vector</code> of <code>Report</code>s
     */
    public Vector<Report> damageEntity(Entity te, HitData hit, int damage) {
        return damageEntity(te, hit, damage, false, DamageType.NONE, false, false);
    }

    /**
     * Deals the listed damage to an entity. Returns a vector of Reports for the
     * phase report
     * 
     * @param te
     *            the target entity
     * @param hit
     *            the hit data for the location hit
     * @param damage
     *            the damage to apply
     * @param ammoExplosion
     *            ammo explosion type damage is applied directly to the IS,
     *            hurts the pilot, causes auto-ejects, and can blow the unit to
     *            smithereens
     * @param bFrag
     *            The DamageType of the attack.
     * @param damageIS
     *            Should the target location's internal structure be damaged
     *            directly?
     * @return a <code>Vector</code> of <code>Report</code>s
     */
    public Vector<Report> damageEntity(Entity te, HitData hit, int damage, boolean ammoExplosion, DamageType bFrag, boolean damageIS) {
        return damageEntity(te, hit, damage, ammoExplosion, bFrag, damageIS, false);
    }

    /**
     * Deals the listed damage to an entity. Returns a vector of Reports for the
     * phase report
     * 
     * @param te
     *            the target entity
     * @param hit
     *            the hit data for the location hit
     * @param damage
     *            the damage to apply
     * @param ammoExplosion
     *            ammo explosion type damage is applied directly to the IS,
     *            hurts the pilot, causes auto-ejects, and can blow the unit to
     *            smithereens
     * @param bFrag
     *            The DamageType of the attack.
     * @param damageIS
     *            Should the target location's internal structure be damaged
     *            directly?
     * @param areaSatArty
     *            Is the damage from an area saturating artillery attack?
     * @return a <code>Vector</code> of <code>Report</code>s
     */
    private Vector<Report> damageEntity(Entity te, HitData hit, int damage, boolean ammoExplosion, DamageType bFrag, boolean damageIS, boolean areaSatArty) {
        return damageEntity(te, hit, damage, ammoExplosion, bFrag, damageIS, areaSatArty, true);
    }

    /**
     * Deals the listed damage to an entity. Returns a vector of Reports for the
     * phase report
     * 
     * @param te
     *            the target entity
     * @param hit
     *            the hit data for the location hit
     * @param damage
     *            the damage to apply
     * @param ammoExplosion
     *            ammo explosion type damage is applied directly to the IS,
     *            hurts the pilot, causes auto-ejects, and can blow the unit to
     *            smithereens
     * @param bFrag
     *            The DamageType of the attack.
     * @param damageIS
     *            Should the target location's internal structure be damaged
     *            directly?
     * @param areaSatArty
     *            Is the damage from an area saturating artillery attack?
     * @param throughFront
     *            Is the damage coming through the hex the unit is facing?
     * @return a <code>Vector</code> of <code>Report</code>s
     */
    public Vector<Report> damageEntity(Entity te, HitData hit, int damage, boolean ammoExplosion, DamageType bFrag, boolean damageIS, boolean areaSatArty, boolean throughFront) {
        return damageEntity(te, hit, damage, ammoExplosion, bFrag, damageIS, areaSatArty, throughFront, false, false);
    }

    /**
     * Deals the listed damage to an entity. Returns a vector of Reports for the
     * phase report
     * 
     * @param te
     *            the target entity
     * @param hit
     *            the hit data for the location hit
     * @param damage
     *            the damage to apply
     * @param ammoExplosion
     *            ammo explosion type damage is applied directly to the IS,
     *            hurts the pilot, causes auto-ejects, and can blow the unit to
     *            smithereens
     * @param bFrag
     *            The DamageType of the attack.
     * @param damageIS
     *            Should the target location's internal structure be damaged
     *            directly?
     * @param areaSatArty
     *            Is the damage from an area saturating artillery attack?
     * @param throughFront
     *            Is the damage coming through the hex the unit is facing?
     * @param finalSquadDamage
     *            is this the final resolution of squadron damage
     * @param nukeS2s
     *            is this a ship-to-ship nuke?
     * @return a <code>Vector</code> of <code>Report</code>s
     */
    public Vector<Report> damageEntity(Entity te, HitData hit, int damage, boolean ammoExplosion, DamageType bFrag, boolean damageIS, boolean areaSatArty, boolean throughFront, boolean finalSquadDamage, boolean nukeS2S) {

        Vector<Report> vDesc = new Vector<Report>();
        Report r;
        int te_n = te.getId();
        // This is good for shields if a shield absorps the hit it shouldn't
        // effect the pilot.
        // TC SRM's that hit the head do external and internal damage but its
        // one hit and shouldn't cause
        // 2 hits to the pilot.
        boolean isHeadHit = te instanceof Mech && ((Mech) te).getCockpitType() != Mech.COCKPIT_TORSO_MOUNTED && hit.getLocation() == Mech.LOC_HEAD && ((hit.getEffect() & HitData.EFFECT_NO_CRITICALS) != HitData.EFFECT_NO_CRITICALS);

        // booleans to indicate criticals for AT2
        boolean critBoxCars = false;
        boolean critSI = false;
        boolean critThresh = false;

        // is this capital-scale damage
        boolean isCapital = hit.isCapital();
        // get any capital missile critical mods
        int CapitalMissile = hit.getCapMisCritMod();
        // check for box cars
        if (hit.rolledBoxCars()) {
            critBoxCars = true;
        }

        // check capital/standard damage
        if (isCapital && !te.isCapitalScale()) {
            damage = 10 * damage;
        }
        boolean standardToCapital = false;
        if (!isCapital && te.isCapitalScale()) {
            if (ammoExplosion) {
                // applies directly to SI no need to
                // collect it with the rest of the standard damage
                damage = damage / 10;
            } else {
                standardToCapital = true;
            }
        }

        int damage_orig = damage;

        // show Locations which have rerolled with Edge
        HitData undoneLocation = hit.getUndoneLocation();
        while (undoneLocation != null) {
            r = new Report(6500);
            r.subject = te_n;
            r.indent(2);
            r.newlines = 0;
            r.addDesc(te);
            r.add(te.getLocationAbbr(undoneLocation));
            vDesc.addElement(r);
            undoneLocation = undoneLocation.getUndoneLocation();
        } // while
        // if edge was uses, give at end overview of remainings
        if (hit.getUndoneLocation() != null) {
            r = new Report(6510);
            r.subject = te_n;
            r.indent(2);
            r.newlines = 0;
            r.addDesc(te);
            r.add(te.crew.getOptions().intOption("edge"));
            vDesc.addElement(r);
        } // if

        boolean autoEject = false;
        if (ammoExplosion) {
            if (te instanceof Mech) {
                Mech mech = (Mech) te;
                if (mech.isAutoEject()) {
                    autoEject = true;
                    vDesc.addAll(ejectEntity(te, true));
                }
            }
        }
        boolean isBattleArmor = te instanceof BattleArmor;
        boolean isPlatoon = !isBattleArmor && te instanceof Infantry;
        boolean isFerroFibrousTarget = false;
        boolean wasDamageIS = false;
        boolean tookInternalDamage = damageIS;
        IHex te_hex = null;

        boolean hardenedArmor = false;
        boolean reflectiveArmor = false;
        boolean reactiveArmor = false;

        if ( (te instanceof Mech || te instanceof Tank) && te.getArmorType() == EquipmentType.T_ARMOR_HARDENED)
            hardenedArmor = true;

        if ((te instanceof Mech || te instanceof Tank) && te.getArmorType() == EquipmentType.T_ARMOR_REFLECTIVE)
            reflectiveArmor = true;

        if ((te instanceof Mech || te instanceof Tank) && te.getArmorType() == EquipmentType.T_ARMOR_REACTIVE)
            reactiveArmor = true;

        int crits = ((hit.getEffect() & HitData.EFFECT_CRITICAL) == HitData.EFFECT_CRITICAL) && !hardenedArmor ? 1 : 0;
        int specCrits = ((hit.getEffect() & HitData.EFFECT_CRITICAL) == HitData.EFFECT_CRITICAL) && hardenedArmor ? 1 : 0;
        HitData nextHit = null;
        
        // Some "hits" on a Protomech are actually misses.
        if (te instanceof Protomech && hit.getLocation() == Protomech.LOC_NMISS) {
            r = new Report(6035);
            r.subject = te_n;
            r.indent(2);
            r.newlines = 0;
            vDesc.addElement(r);
            return vDesc;
        }

        // check for critical hit/miss vs. a BA
        if (crits > 0 && te instanceof BattleArmor) {
            // possible critical miss if the rerolled location isn't alive
            if (hit.getLocation() >= te.locations() || te.getInternal(hit.getLocation()) <= 0) {
                r = new Report(6037);
                r.add(hit.getLocation());
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
                return vDesc;
            }
            // otherwise critical hit
            r = new Report(6225);
            r.add(te.getLocationAbbr(hit));
            r.subject = te_n;
            r.indent(2);
            r.newlines = 0;
            vDesc.addElement(r);

            crits = 0;
            damage = Math.max(te.getInternal(hit.getLocation()) + te.getArmor(hit.getLocation()), damage);
        }

        if (te.getArmor(hit) > 0 && (te.getArmorType() == EquipmentType.T_ARMOR_FERRO_FIBROUS || te.getArmorType() == EquipmentType.T_ARMOR_LIGHT_FERRO || te.getArmorType() == EquipmentType.T_ARMOR_HEAVY_FERRO)) {
            isFerroFibrousTarget = true;
        }

        // Is the infantry in the open?
        if (isPlatoon && !te.isDestroyed() && !te.isDoomed() && ((Infantry) te).getDugIn() != Infantry.DUG_IN_COMPLETE) {
            te_hex = game.getBoard().getHex(te.getPosition());
            if (te_hex != null && !te_hex.containsTerrain(Terrains.WOODS) && !te_hex.containsTerrain(Terrains.JUNGLE) && !te_hex.containsTerrain(Terrains.ROUGH) && !te_hex.containsTerrain(Terrains.RUBBLE) && !te_hex.containsTerrain(Terrains.SWAMP) && !te_hex.containsTerrain(Terrains.BUILDING) && !te_hex.containsTerrain(Terrains.FUEL_TANK) && !te_hex.containsTerrain(Terrains.FORTIFIED) && !ammoExplosion) {
                // PBI. Damage is doubled.
                damage *= 2;
                r = new Report(6040);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
        }
        // Is the infantry in vacuum?
        if ((isPlatoon || isBattleArmor) && !te.isDestroyed() && !te.isDoomed() 
                && game.getPlanetaryConditions().isVacuum()) {
            // PBI. Double damage.
            damage *= 2;
            r = new Report(6041);
            r.subject = te_n;
            r.indent(2);
            r.newlines = 0;
            vDesc.addElement(r);
        }
        // If dealing with fragmentation missiles,
        // it does double damage to infantry...
        // We're actually going to abuse this for AX-head warheads, too, so as
        // to not add another parameter.
        switch (bFrag) {
        case FRAGMENTATION:
            if (!isPlatoon) {
                damage = 0;
                r = new Report(6050);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
            break;
        case FLECHETTE:
            if (!isPlatoon && !isBattleArmor) {
                damage /= 2;
                r = new Report(6060);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            } else if (isPlatoon && !isBattleArmor) {
                r = new Report(6055);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
            break;
        case ACID:
            if (isFerroFibrousTarget) {
                damage = te.getArmor(hit) >= 3 ? 3 : te.getArmor(hit);
                r = new Report(6061);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                r.add(damage);
                vDesc.addElement(r);
            } else {
                r = new Report(6062);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
            break;
        case INCENDIARY:
            // Incendiary AC ammo does +2 damage to unarmoured infantry
            if (isPlatoon) {
                damage += 2;
                r = new Report(6064);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
            break;
        case ANTI_TSM:
            te.hitThisRoundByAntiTSM = true;
        default:
            // We can ignore this.
            break;
        }

        // adjust VTOL rotor damage
        if (te instanceof VTOL && hit.getLocation() == VTOL.LOC_ROTOR) {
            damage = (damage + 9) / 10;
        }

        // save EI status, in case sensors crit destroys it
        final boolean eiStatus = te.hasActiveEiCockpit();
        // BA using EI implants receive +1 damage from attacks
        if (!(te instanceof Mech) && !(te instanceof Protomech) && eiStatus) {
            damage += 1;
        }

        // check for case on Aeros
        if (te instanceof Aero) {
            Aero a = (Aero) te;
            if (ammoExplosion && a.hasCase()) {
                // damage should be reduced by a factor of 2 for ammo explosions
                // according to p. 161, TW
                damage /= 2;
                r = new Report(9010);
                r.subject = te_n;
                r.add(damage);
                r.indent(3);
                r.newlines = 0;
                vDesc.addElement(r);
            }
        }

        // Allocate the damage
        while (damage > 0) {

            // first check for ammo explosions on aeros separately, because it
            // must be done before
            // standard to capital damage conversions
            if (te instanceof Aero && hit.getLocation() == Aero.LOC_AFT && !damageIS) {
                for (Mounted mAmmo : te.getAmmo()) {
                    if (mAmmo.isDumping() && !mAmmo.isDestroyed() && !mAmmo.isHit()) {
                        // doh. explode it
                        vDesc.addAll(explodeEquipment(te, mAmmo.getLocation(), mAmmo));
                        mAmmo.setHit(true);
                    }
                }
            }

            // check if this a standard scale attack to capital armor
            // if so, collect the damage on the entity and stop processing
            if (standardToCapital && te instanceof Aero) {
                r = new Report(9050);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                r.addDesc(te);
                r.add(damage);
                r.add(te.getLocationAbbr(hit));
                if (te instanceof FighterSquadron) {
                    r = new Report(9051);
                    r.subject = te_n;
                    r.indent(2);
                    r.newlines = 0;
                    r.addDesc(te);
                    r.add(damage);
                }
                vDesc.addElement(r);
                Aero ship = (Aero) te;
                ship.addStandardDamage(damage, hit);
                // if boxcars were rolled, then this might be a critical hit
                if (critBoxCars) {
                    Aero a = (Aero) te;
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "12 to hit", 8, damage_orig / 10, isCapital));
                }
                // if this did over 20 points of standard damage it might
                // critical a FighterSquadron
                if (damage >= 20 && te instanceof FighterSquadron) {
                    Aero a = (Aero) te;
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "Damage threshold exceeded", 8, damage_orig / 10, isCapital));
                }

                return vDesc;
            }

            if (te instanceof Aero) {
                // chance of a critical if damage greater than threshold
                Aero a = (Aero) te;
                int threshold = a.getThresh(hit.getLocation());
                if (damage > threshold && !(te instanceof FighterSquadron)) {
                    critThresh = true;
                }

                if (damage >= 2 && te instanceof FighterSquadron) {
                    critThresh = true;
                }
            }

            // fighter squadrons receive damage differently
            if (te instanceof FighterSquadron) {
                FighterSquadron fs = (FighterSquadron) te;
                fs.setArmor(fs.getArmor() - damage);
                fs.addDamageRound(damage);
                r = new Report(9065);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                r.addDesc(te);
                r.add(damage);
                vDesc.addElement(r);
                r = new Report(6085);
                r.subject = te_n;
                r.newlines = 0;
                r.add(Math.max(fs.getArmor(), 0));
                vDesc.addElement(r);
                // check to see if this detroyed the entity
                if (fs.getArmor() <= 0) {
                    vDesc.addAll(destroyEntity(te, "structural integrity collapse"));
                    fs.setArmor(0);
                }
                damage = 0;
            } else if (!(te instanceof Aero && ammoExplosion)) {
                // report something different for Aero ammo explosions
                r = new Report(6065);
                r.subject = te_n;
                r.indent(2);
                r.newlines = 0;
                r.addDesc(te);
                r.add(damage);
                if (damageIS)
                    r.messageId = 6070;
                r.add(te.getLocationAbbr(hit));
                vDesc.addElement(r);
            }

            // was the section destroyed earlier this phase?
            if (te.getInternal(hit) == IArmorState.ARMOR_DOOMED) {
                // cannot transfer a through armor crit if so
                crits = 0;
            }

            // here goes the fun :)
            // Shields take damage first then cowls then armor whee
            // Shield does not protect from ammo explosions or falls.
            if (!ammoExplosion && !hit.isFallDamage() && !damageIS && te.hasShield() && ((hit.getEffect() & HitData.EFFECT_NO_CRITICALS) != HitData.EFFECT_NO_CRITICALS)) {
                Mech me = (Mech) te;
                int damageNew = me.shieldAbsorptionDamage(damage, hit.getLocation(), hit.isRear());
                // if a shield absorbed the damage then lets tell the world
                // about it.
                if (damageNew != damage) {
                    int absorb = damage - damageNew;
                    te.damageThisPhase += absorb;
                    damage = damageNew;

                    r = new Report(3530);
                    r.subject = te_n;
                    r.indent(3);
                    r.newlines = 0;
                    r.add(absorb);
                    vDesc.addElement(r);

                    if (damage <= 0) {
                        crits = 0;
                        specCrits = 0;
                        isHeadHit = false;
                    }
                }
            }

            // Armored Cowl may absorb some damage from hit
            if (te instanceof Mech) {
                Mech me = (Mech) te;
                if (me.hasCowl() && hit.getLocation() == Mech.LOC_HEAD && !throughFront) {
                    int damageNew = me.damageCowl(damage);
                    int damageDiff = damage - damageNew;
                    me.damageThisPhase += damageDiff;
                    damage = damageNew;

                    r = new Report(3520);
                    r.subject = te_n;
                    r.indent(3);
                    r.newlines = 0;
                    r.add(damageDiff);
                    vDesc.addElement(r);
                }
            }

            if (!ammoExplosion && !damageIS && ((hit.getEffect() & HitData.EFFECT_NO_CRITICALS) != HitData.EFFECT_NO_CRITICALS)) {
                damage = te.getDamageReductionFromModularArmor(hit.getLocation(), damage, vDesc);
            }
             
            // Destroy searchlights on 7+ (torso hits on mechs)
            boolean spotlightHittable = false;
            if (te.hasSpotlight()) {
                spotlightHittable = true;
                int loc = hit.getLocation();
                if (te instanceof Mech) {
                    if (loc != Mech.LOC_CT && loc != Mech.LOC_LT && loc != Mech.LOC_RT) {
                        spotlightHittable = false;
                    }
                } else if (te instanceof Tank) {
                    if (loc != Tank.LOC_FRONT && loc != Tank.LOC_RIGHT && loc != Tank.LOC_LEFT) {
                        spotlightHittable = false;
                    }
                }
                if (spotlightHittable) {
                    int spotroll = Compute.d6(2);
                    r = new Report(6072);
                    r.indent(2);
                    r.subject = te_n;
                    r.add(spotroll);
                    vDesc.addElement(r);
                    if (spotroll >= 7) {
                        r = new Report(6071);
                        r.subject = te_n;
                        r.indent(2);
                        vDesc.addElement(r);
                        te.setSpotlightState(false);
                        te.setSpotlight(false);
                    }
                }
            }

            // Does an exterior passenger absorb some of the damage?
            if (!damageIS) {
                int nLoc = hit.getLocation();
                Entity passenger = te.getExteriorUnitAt(nLoc, hit.isRear());
                // Does an exterior passenger absorb some of the damage?
                if (!ammoExplosion && null != passenger && Compute.d6() >= 5 && !passenger.isDoomed() && bFrag != DamageType.IGNORE_PASSENGER) {
                    // Yup. Roll up some hit data for that passenger.
                    r = new Report(6075);
                    r.subject = passenger.getId();
                    r.indent(3);
                    r.addDesc(passenger);
                    vDesc.addElement(r);

                    HitData passHit = passenger.getTrooperAtLocation(hit, te);

                    // How much damage will the passenger absorb?
                    int absorb = 0;
                    HitData nextPassHit = passHit;
                    do {
                        if (0 < passenger.getArmor(nextPassHit)) {
                            absorb += passenger.getArmor(nextPassHit);
                        }
                        if (0 < passenger.getInternal(nextPassHit)) {
                            absorb += passenger.getInternal(nextPassHit);
                        }
                        nextPassHit = passenger.getTransferLocation(nextPassHit);
                    } while (damage > absorb && nextPassHit.getLocation() >= 0);

                    // Damage the passenger.
                    vDesc.addAll(damageEntity(passenger, passHit, damage));

                    // Did some damage pass on?
                    if (damage > absorb) {
                        // Yup. Remove the absorbed damage.
                        damage -= absorb;
                        r = new Report(6080);
                        r.subject = te_n;
                        r.indent(1);
                        r.add(damage);
                        r.addDesc(te);
                        vDesc.addElement(r);
                    } else {
                        // Nope. Return our description.
                        return vDesc;
                    }

                } // End nLoc-has-exterior-passenger

                boolean bTorso = nLoc == Mech.LOC_CT || nLoc == Mech.LOC_RT || nLoc == Mech.LOC_LT;

                // Does a swarming unit absorb damage?
                int swarmer = te.getSwarmAttackerId();
                if ((!(te instanceof Mech) || bTorso) && swarmer != Entity.NONE && (hit.getEffect() & HitData.EFFECT_CRITICAL) == 0 && Compute.d6() >= 5 && bFrag != DamageType.IGNORE_PASSENGER) {
                    Entity swarm = game.getEntity(swarmer);
                    // Yup. Roll up some hit data for that passenger.
                    r = new Report(6076);
                    r.subject = swarmer;
                    r.indent(3);
                    r.addDesc(swarm);
                    vDesc.addElement(r);

                    HitData passHit = swarm.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);

                    // How much damage will the swarm absorb?
                    int absorb = 0;
                    HitData nextPassHit = passHit;
                    do {
                        if (0 < swarm.getArmor(nextPassHit)) {
                            absorb += swarm.getArmor(nextPassHit);
                        }
                        if (0 < swarm.getInternal(nextPassHit)) {
                            absorb += swarm.getInternal(nextPassHit);
                        }
                        nextPassHit = swarm.getTransferLocation(nextPassHit);
                    } while (damage > absorb && nextPassHit.getLocation() >= 0);

                    // Damage the swarm.
                    vDesc.addAll(damageEntity(swarm, passHit, damage));

                    // Did some damage pass on?
                    if (damage > absorb) {
                        // Yup. Remove the absorbed damage.
                        damage -= absorb;
                        r = new Report(6080);
                        r.subject = te_n;
                        r.indent(1);
                        r.add(damage);
                        r.addDesc(te);
                        vDesc.addElement(r);
                    } else {
                        // Nope. Return our description.
                        return vDesc;
                    }
                }

                // is this a mech dumping ammo being hit in the rear torso?
                if ((te instanceof Mech && hit.isRear() && bTorso) || (te instanceof Tank && hit.getLocation() == Tank.LOC_REAR)) {
                    for (Mounted mAmmo : te.getAmmo()) {
                        if (mAmmo.isDumping() && !mAmmo.isDestroyed() && !mAmmo.isHit()) {
                            // doh. explode it
                            vDesc.addAll(explodeEquipment(te, mAmmo.getLocation(), mAmmo));
                            mAmmo.setHit(true);
                        }
                    }
                }
            }

            // is there armor in the location hit?
            if (!ammoExplosion && te.getArmor(hit) > 0 && !damageIS) {
                int tmpDamageHold = -1;
                int origDamage = damage;

                // If the target has hardened armor, we need to adjust damage.
                if (hardenedArmor && hit.getGeneralDamageType() != HitData.DAMAGE_ARMOR_PIERCING && hit.getGeneralDamageType() != HitData.DAMAGE_ARMOR_PIERCING_MISSILE) {
                    tmpDamageHold = damage;
                    damage = (int) Math.ceil(((double) damage) / 2);
                    r = new Report(6069);
                    r.subject = te_n;
                    r.indent(2);
                    r.newlines = 0;
                    r.add(damage);
                    vDesc.addElement(r);
                } else if (isPlatoon) {
                    // infantry armour works differently
                    int armor = te.getArmor(hit);
                    int men = te.getInternal(hit);
                    tmpDamageHold = damage % 2;
                    damage /= 2;
                    if (tmpDamageHold == 1 && armor >= men) {
                        // extra 1 point of damage to armor
                        tmpDamageHold = damage;
                        damage++;
                    } else {
                        // extra 0 or 1 point of damage to men
                        tmpDamageHold += damage;
                    }

                } else if (reflectiveArmor && hit.getGeneralDamageType() == HitData.DAMAGE_PHYSICAL) {
                    tmpDamageHold = damage;
                    damage *= 2;
                    r = new Report(6066);
                    r.subject = te_n;
                    r.indent(2);
                    r.newlines = 0;
                    r.add(damage);
                    vDesc.addElement(r);
                } else if (reflectiveArmor && hit.getGeneralDamageType() == HitData.DAMAGE_ENERGY) {
                    tmpDamageHold = damage;
                    damage = (int) Math.ceil(((double) damage) / 2);
                    r = new Report(6067);
                    r.subject = te_n;
                    r.indent(2);
                    r.newlines = 0;
                    r.add(damage);
                    vDesc.addElement(r);
                } else if (reactiveArmor && ( hit.getGeneralDamageType() == HitData.DAMAGE_MISSILE || hit.getGeneralDamageType() == HitData.DAMAGE_ARMOR_PIERCING_MISSILE)) {
                    tmpDamageHold = damage;
                    damage = (int) Math.ceil(((double) damage) / 2);
                    r = new Report(6068);
                    r.subject = te_n;
                    r.indent(2);
                    r.newlines = 0;
                    r.add(damage);
                    vDesc.addElement(r);
                }

                if (te.getArmor(hit) >= damage) {

                    // armor absorbs all damage
                    te.setArmor(te.getArmor(hit) - damage, hit);
                    if (tmpDamageHold >= 0)
                        te.damageThisPhase += tmpDamageHold;
                    else
                        te.damageThisPhase += damage;
                    damage = 0;
                    r = new Report(6085);
                    r.subject = te_n;
                    r.newlines = 0;
                    if (spotlightHittable)
                        r.indent(3);
                    r.add(te.getArmor(hit));
                    vDesc.addElement(r);

                    // tele-missiles are destroyed if they lose all armor
                    if (te instanceof TeleMissile && te.getArmor(hit) == damage) {
                        vDesc.addAll(destroyEntity(te, "damage", false));
                    }

                } else {
                    // damage goes on to internal
                    int absorbed = Math.max(te.getArmor(hit), 0);
                    if (reflectiveArmor && hit.getGeneralDamageType() == HitData.DAMAGE_PHYSICAL) {
                        absorbed = (int) Math.round(Math.ceil(absorbed / 2));
                        damage = tmpDamageHold;
                        tmpDamageHold = 0;
                    }
                    te.setArmor(IArmorState.ARMOR_DESTROYED, hit);
                    if (tmpDamageHold >= 0)
                        te.damageThisPhase += 2 * absorbed;
                    else
                        te.damageThisPhase += absorbed;
                    damage -= absorbed;
                    r = new Report(6090);
                    r.subject = te_n;
                    r.newlines = 0;
                    if (spotlightHittable)
                        r.indent(3);
                    vDesc.addElement(r);
                    if (te instanceof GunEmplacement) {
                        // gun emplacements have no internal,
                        // destroy the section
                        te.destroyLocation(hit.getLocation());
                        r = new Report(6115);
                        r.subject = te_n;
                        r.newlines = 0;
                        vDesc.addElement(r);

                        if (te.getTransferLocation(hit).getLocation() == Entity.LOC_DESTROYED) {
                            vDesc.addAll(destroyEntity(te, "damage", false));
                        }
                    }
                }

                // Support Vees gets crits, depending on damage and BAR rating
                if (te instanceof SupportTank || te instanceof SupportVTOL) {
                    if (te instanceof SupportTank) {
                        SupportTank ste = (SupportTank) te;
                        if (origDamage > ste.getBARRating()) {
                            if (hit.getSpecCritMod() < 0) {
                                // this is from AP rounds, auto crit
                                if (ste.getBARRating() < 10) {
                                    // crit roll with +2 mod
                                    Report.addNewline(vDesc);
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 2 + hit.getSpecCritMod()));
                                } else {
                                    Report.addNewline(vDesc);
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 0 + hit.getSpecCritMod()));
                                }
                            }
                            if (ste.hasArmoredChassis()) {
                                if (ste.getBARRating() < 10) {
                                    Report.addNewline(vDesc);
                                    // crit roll with -1 mod
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 1));
                                }
                                // else no crit
                            } else {
                                Report.addNewline(vDesc);
                                vDesc.addAll(criticalTank(ste, hit.getLocation(), 0));
                            }
                        }
                    } else {
                        SupportVTOL ste = (SupportVTOL) te;
                        if (origDamage > ste.getBARRating()) {
                            if (hit.getSpecCritMod() < 0) {
                                // this is from AP rounds, auto crit
                                if (ste.getBARRating() < 10) {
                                    // crit roll with +2 mod
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 2 + hit.getSpecCritMod()));
                                } else {
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 0 + hit.getSpecCritMod()));
                                }
                            }
                            if (ste.hasArmoredChassisAndControl()) {
                                if (ste.getBARRating() < 10) {
                                    // crit roll with -1 mod
                                    vDesc.addAll(criticalTank(ste, hit.getLocation(), 1));
                                }
                                // else no crit
                            } else {
                                vDesc.addAll(criticalTank(ste, hit.getLocation(), 0));
                            }
                        }
                    }
                }

                // If it has hardened armor, now we need to "correct" any
                // remaining damage.
                if (tmpDamageHold > 0) {
                    if (hardenedArmor) {
                        damage *= 2;
                        damage -= tmpDamageHold % 2;
                    } else if (isPlatoon) {
                        damage = tmpDamageHold;
                    }
                }
            }

            // is there damage remaining?
            if (damage > 0) {

                // if this is an Aero then I need to apply internal damage
                // to the SI after halving it. Return from here to prevent
                // further processing
                if (te instanceof Aero) {
                    Aero a = (Aero) te;

                    // divide damage in half
                    // do not divide by half if it is an ammo exposion
                    if (!ammoExplosion && !nukeS2S)
                        damage /= 2;

                    // this should result in a crit
                    // but only if it really did damage after rounding down
                    if (damage > 0) {
                        critSI = true;
                    }

                    // Now apply damage to the structural integrity
                    a.setSI(a.getSI() - damage);
                    te.damageThisPhase += damage;
                    // send the report
                    r = new Report(1210);
                    r.subject = te_n;
                    r.newlines = 0;
                    if (!ammoExplosion) {
                        r.messageId = 9005;
                    } else {
                        r.messageId = 9006;
                    }
                    r.add(damage);
                    r.add(Math.max(a.getSI(), 0));
                    vDesc.addElement(r);
                    damage = 0;
                    // check to see if this would destroy the ASF
                    if (a.getSI() <= 0) {
                        vDesc.addAll(destroyEntity(te, "structural integrity collapse"));
                        a.setSI(0);
                    }

                     //check for nuclear critical 
                    if(nukeS2S) { 
                        //add a control roll 
                        PilotingRollData nukePSR = new PilotingRollData( te.getId(), 4, "Nuclear attack" );
                        nukePSR.setCumulative(false);
                        game.addControlRoll(nukePSR);
                        
                        //need some kind of report 
                        int nukeroll = Compute.d6(2);
                        r = new Report(9145); 
                        r.subject = a.getId(); 
                        r.newlines = 0; 
                        r.indent(3); 
                        r.add(CapitalMissile); 
                        r.add(nukeroll);
                        vDesc.add(r); 
                        if(nukeroll >= CapitalMissile) {                      
                            int nukeDamage = damage_orig;
                            a.setSI(a.getSI()-nukeDamage); 
                            te.damageThisPhase += nukeDamage; 
                            r = new Report(9146); 
                            r.subject = a.getId();
                            r.newlines = 0; 
                            r.add(nukeDamage);
                            r.add(Math.max(a.getSI(),0)); 
                            vDesc.addElement(r);
                            if(a.getSI() <= 0) { 
                                vDesc.addAll( destroyEntity(te,"structural integrity collapse")); 
                                a.setSI(0); 
                            } else if(!critSI) { 
                                critSI = true; 
                            }     
                        } else { 
                            r = new Report(9147); 
                            r.subject = a.getId(); 
                            r.newlines = 0;
                            vDesc.addElement(r); 
                        }     
                    }
                     
                    // apply crits
                    if (critBoxCars) {
                        vDesc.elementAt(vDesc.size() - 1).newlines++;
                        vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "12 to hit", 8, damage_orig, isCapital));
                    }
                    // ammo explosions shouldn't affect threshold because they
                    // go right to SI
                    if (critThresh && !ammoExplosion && !finalSquadDamage) {
                        vDesc.elementAt(vDesc.size() - 1).newlines++;
                        vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "Damage threshold exceeded", 8, damage_orig, isCapital));
                    }
                    if (critSI && !ammoExplosion && !finalSquadDamage) {
                        vDesc.elementAt(vDesc.size() - 1).newlines++;
                        vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "SI damaged", 8, damage_orig, isCapital));
                    }
                    if (CapitalMissile > 0 && !nukeS2S && !finalSquadDamage) {
                        vDesc.elementAt(vDesc.size() - 1).newlines++;
                        vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "Capital Missile", CapitalMissile, damage_orig, isCapital));
                    }
                    return vDesc;
                }

                // Check for CASE II right away. if so reduce damage to 1
                // and let it hit the IS.
                // Also remove as much of the rear armor as allowed by the
                // damage. If arm/leg/head
                // Then they lose all their armor if its less then the
                // explosion damage.
                if (ammoExplosion && te.hasCASEII(hit.getLocation())) {
                    te.damageThisPhase += damage;
                    // 1 point of damage goes to IS
                    damage--;
                    // Remaining damage prevented by CASE II
                    r = new Report(6126);
                    r.subject = te_n;
                    r.add(damage);
                    r.indent(3);
                    r.newlines = 0;
                    vDesc.addElement(r);
                    boolean rearArmor = te.hasRearArmor(hit.getLocation());
                    if (damage > te.getArmor(hit.getLocation(), rearArmor))
                        te.setArmor(IArmorState.ARMOR_DESTROYED, hit.getLocation(), rearArmor);
                    else
                        te.setArmor(te.getArmor(hit.getLocation(), rearArmor) - damage, hit.getLocation(), rearArmor);

                    if (te.getInternal(hit) > 0) {
                        // Mek takes 1 point of IS damage
                        damage = 1;
                    } else {
                        damage = 0;
                    }

                    int roll = Compute.d6(2);
                    r = new Report(6127);
                    r.subject = te.getId();
                    r.add(roll);
                    r.newlines = 0;
                    vDesc.add(r);
                    if (roll >= 8)
                        hit.setEffect(HitData.EFFECT_NO_CRITICALS);
                }
                // check for tank CASE here: damage to rear armor, excess
                // dissipating, and a crew stunned crit
                if (ammoExplosion && te instanceof Tank && te.locationHasCase(Tank.LOC_BODY)) {
                    te.damageThisPhase += damage;
                    r = new Report(6124);
                    r.subject = te_n;
                    r.indent(2);
                    r.add(damage);
                    vDesc.add(r);
                    if (damage > te.getArmor(Tank.LOC_REAR)) {
                        te.setArmor(IArmorState.ARMOR_DESTROYED, Tank.LOC_REAR);
                        r = new Report(6090);
                    } else {
                        te.setArmor(te.getArmor(Tank.LOC_REAR) - damage, Tank.LOC_REAR);
                        r = new Report(6085);
                        r.add(te.getArmor(Tank.LOC_REAR));
                    }
                    r.subject = te_n;
                    r.newlines = 0;
                    r.indent(2);
                    vDesc.add(r);
                    damage = 0;
                    int critIndex;
                    if (((Tank) te).isCommanderHit() && ((Tank) te).isDriverHit()) {
                        critIndex = Tank.CRIT_CREW_KILLED;
                    } else
                        critIndex = Tank.CRIT_CREW_STUNNED;
                    vDesc.addAll(applyCriticalHit(te, Entity.NONE, new CriticalSlot(0, critIndex), true));
                }

                // is there internal structure in the location hit?
                if (te.getInternal(hit) > 0) {

                    // Now we need to consider alternate structure types!
                    int tmpDamageHold = -1;
                    if (te instanceof Mech && ((Mech) te).hasCompositeStructure()) {
                        tmpDamageHold = damage;
                        damage *= 2;
                    }
                    if (te instanceof Mech && ((Mech) te).hasReinforcedStructure()) {
                        tmpDamageHold = damage;
                        damage /= 2;
                        damage += tmpDamageHold % 2;
                    }
                    if (te.getInternal(hit) > damage && damage > 0) {
                        // internal structure absorbs all damage
                        te.setInternal(te.getInternal(hit) - damage, hit);
                        // Triggers a critical hit on Vehicles and Mechs.
                        if (!isPlatoon && !isBattleArmor) {
                            crits++;
                        }
                        tookInternalDamage = true;
                        te.damageThisPhase += damage;
                        damage = 0;
                        r = new Report(1210);
                        r.subject = te_n;
                        r.newlines = 0;
                        // Infantry platoons have men not "Internals".
                        if (isPlatoon) {
                            r.messageId = 6095;
                        } else {
                            r.messageId = 6100;
                        }
                        r.add(te.getInternal(hit));
                        vDesc.addElement(r);
                    } else if (damage > 0) {
                        // Triggers a critical hit on Vehicles and Mechs.
                        if (!isPlatoon && !isBattleArmor) {
                            crits++;
                        }
                        // damage transfers, maybe
                        int absorbed = Math.max(te.getInternal(hit), 0);

                        // Handle Protomech pilot damage
                        // due to location destruction
                        if (te instanceof Protomech) {
                            int hits = Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()] - ((Protomech) te).getPilotDamageTaken(hit.getLocation());
                            if (hits > 0) {
                                vDesc.addAll(damageCrew(te, hits));
                                ((Protomech) te).setPilotDamageTaken(hit.getLocation(), Protomech.POSSIBLE_PILOT_DAMAGE[hit.getLocation()]);
                            }
                        }

                        // Platoon, Trooper, or Section destroyed message
                        r = new Report(1210);
                        r.subject = te_n;
                        r.newlines = 0;
                        if (isPlatoon) {
                            // Infantry have only one section, and
                            // are therefore destroyed.
                            r.messageId = 6105;
                        } else if (isBattleArmor) {
                            r.messageId = 6110;
                        } else {
                            r.messageId = 6115;
                        }
                        vDesc.addElement(r);

                        // If a sidetorso got destroyed, and the
                        // corresponding arm is not yet destroyed, add
                        // it as a club to that hex (p.35 BMRr)
                        if (te instanceof Mech && (hit.getLocation() == Mech.LOC_RT && te.getInternal(Mech.LOC_RARM) > 0 || hit.getLocation() == Mech.LOC_LT && te.getInternal(Mech.LOC_LARM) > 0)) {
                            int blownOffLocation = -1; // good initial value?
                            if (hit.getLocation() == Mech.LOC_RT) {
                                blownOffLocation = Mech.LOC_RARM;
                            } else {
                                blownOffLocation = Mech.LOC_LARM;
                            }
                            r = new Report(6120);
                            r.subject = te_n;
                            r.add(te.getLocationName(blownOffLocation));
                            r.newlines = 0;
                            vDesc.addElement(r);
                            IHex h = game.getBoard().getHex(te.getPosition());
                            if (te instanceof BipedMech) {
                                if (!h.containsTerrain(Terrains.ARMS)) {
                                    h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ARMS, 1));
                                } else
                                    h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ARMS, h.terrainLevel(Terrains.ARMS) + 1));
                            } else if (!h.containsTerrain(Terrains.LEGS)) {
                                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.LEGS, 1));
                            } else
                                h.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.LEGS, h.terrainLevel(Terrains.LEGS) + 1));
                            sendChangedHex(te.getPosition());
                        }

                        // Troopers riding on a location
                        // all die when the location is destroyed.
                        if (te instanceof Mech || te instanceof Tank) {
                            Entity passenger = te.getExteriorUnitAt(hit.getLocation(), hit.isRear());
                            if (null != passenger && !passenger.isDoomed()) {
                                HitData passHit = passenger.getTrooperAtLocation(hit, te);
                                // ensures a kill
                                passHit.setEffect(HitData.EFFECT_CRITICAL);
                                if (passenger.getInternal(passHit) > 0) {
                                    vDesc.addAll(damageEntity(passenger, passHit, damage));
                                }
                                passHit = new HitData(hit.getLocation(), !hit.isRear());
                                passHit = passenger.getTrooperAtLocation(passHit, te);
                                // ensures a kill
                                passHit.setEffect(HitData.EFFECT_CRITICAL);
                                if (passenger.getInternal(passHit) > 0) {
                                    vDesc.addAll(damageEntity(passenger, passHit, damage));
                                }
                            }
                        }

                        // BA inferno explosions
                        if (te instanceof BattleArmor) {
                            int infernos = 0;
                            for (Mounted m : te.getEquipment()) {
                                if (m.getType() instanceof AmmoType) {
                                    AmmoType at = (AmmoType) m.getType();
                                    if ((at.getAmmoType() == AmmoType.T_SRM || at.getAmmoType() == AmmoType.T_MML) && at.getMunitionType() == AmmoType.M_INFERNO) {
                                        infernos += at.getRackSize() * m.getShotsLeft();
                                    }
                                } else if (m.getType().hasFlag(MiscType.F_FIRE_RESISTANT)) {
                                    // immune to inferno explosion
                                    infernos = 0;
                                    break;
                                }
                            }
                            if (infernos > 0) {
                                int roll = Compute.d6(2);
                                r = new Report(6680);
                                r.add(roll);
                                vDesc.add(r);
                                if (roll >= 8) {
                                    Coords c = te.getPosition();
                                    if (c == null) {
                                        Entity transport = game.getEntity(te.getTransportId());
                                        if (transport != null)
                                            c = transport.getPosition();
                                        vPhaseReport.addAll(deliverInfernoMissiles(te, te, infernos));
                                    }
                                    if (c != null) {
                                        vPhaseReport.addAll(deliverInfernoMissiles(te, new HexTarget(c, game.getBoard(), Targetable.TYPE_HEX_ARTILLERY), infernos));
                                    }
                                }
                            }
                        }

                        // Destroy the location.
                        te.destroyLocation(hit.getLocation());
                        te.damageThisPhase += absorbed;
                        damage -= absorbed;

                        // Now we need to consider alternate structure types!
                        if (tmpDamageHold > 0) {
                            if (((Mech) te).hasCompositeStructure()) {
                                // If there's a remainder, we can actually
                                // ignore it.
                                damage /= 2;
                            } else if (((Mech) te).hasReinforcedStructure()) {
                                damage *= 2;
                                damage -= tmpDamageHold % 2;
                            }
                        }

                        if (te instanceof Mech && (hit.getLocation() == Mech.LOC_RT || hit.getLocation() == Mech.LOC_LT)) {

                            boolean engineExploded = false;

                            int numEngineHits = 0;
                            numEngineHits += te.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_CT);
                            numEngineHits += te.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_RT);
                            numEngineHits += te.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_LT);

                            engineExploded = checkEngineExplosion(te, vDesc, numEngineHits);
                            if (!engineExploded && numEngineHits > 2) {
                                // third engine hit
                                vDesc.addAll(destroyEntity(te, "engine destruction"));
                                if (game.getOptions().booleanOption("auto_abandon_unit"))
                                    vDesc.addAll(abandonEntity(te));
                            }
                        }

                        if (te instanceof VTOL && hit.getLocation() == VTOL.LOC_ROTOR) {
                            // if rotor is destroyed, movement goes bleh.
                            // I think this will work?
                            te.setOriginalWalkMP(0);
                            vDesc.addAll(crashVTOLorWiGE((VTOL) te));

                        }
                    }
                }
                if (te.getInternal(hit) <= 0) {
                    // internal structure is gone, what are the transfer
                    // potentials?
                    nextHit = te.getTransferLocation(hit);
                    if (nextHit.getLocation() == Entity.LOC_DESTROYED) {
                        if (te instanceof Mech) {
                            // add all non-destroyed engine crits
                            te.engineHitsThisRound += te.getGoodCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, hit.getLocation());
                            // and substract those that where hit previously
                            // this round
                            // hackish, but works.
                            te.engineHitsThisRound -= te.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, hit.getLocation());
                        }

                        boolean engineExploded = false;

                        engineExploded = checkEngineExplosion(te, vDesc, te.engineHitsThisRound);

                        if (!engineExploded && !(te instanceof VTOL && hit.getLocation() == VTOL.LOC_ROTOR)) {
                            // Entity destroyed. Ammo explosions are
                            // neither survivable nor salvagable.
                            // Only ammo explosions in the CT are devastating.
                            vDesc.addAll(destroyEntity(te, "damage", !ammoExplosion, !((ammoExplosion || areaSatArty) && ((te instanceof Tank) || (te instanceof Mech && hit.getLocation() == Mech.LOC_CT)))));
                            // If the head is destroyed, kill the crew.
                            
                            if (te instanceof Mech && hit.getLocation() == Mech.LOC_HEAD
                                    && !te.getCrew().isDead()
                                    && !te.getCrew().isDoomed()
                                    && game.getOptions().booleanOption("tacops_skin_of_the_teeth_ejection") ) {
                                Mech mech = (Mech) te;
                                if (mech.isAutoEject()) {
                                    if ( mech.getCrew().getHits() < 5) {
                                        Report.addNewline(vDesc);
                                        mech.setDoomed(false);
                                        vDesc.addAll(damageCrew(te, 5-mech.getCrew().getHits()));
                                        mech.setDoomed(true);
                                    }
                                    autoEject = true;
                                    vDesc.addAll(ejectEntity(te, true));
                                }
                            }
                            
                            if (hit.getLocation() == Mech.LOC_HEAD || hit.getLocation() == Mech.LOC_CT && (ammoExplosion && !autoEject || areaSatArty)) {
                                te.getCrew().setDoomed(true);
                            }
                            if (game.getOptions().booleanOption("auto_abandon_unit")) {
                                vDesc.addAll(abandonEntity(te));
                            }
                        }

                        // nowhere for further damage to go
                        damage = 0;
                    } else if (nextHit.getLocation() == Entity.LOC_NONE) {
                        // Rest of the damage is wasted.
                        damage = 0;
                    } else if (ammoExplosion && te.locationHasCase(hit.getLocation())) {
                        // Remaining damage prevented by CASE
                        r = new Report(6125);
                        r.subject = te_n;
                        r.add(damage);
                        r.indent(3);
                        r.newlines = 0;
                        vDesc.addElement(r);

                        // ... but page 21 of the Ask The Precentor Martial FAQ
                        // www.classicbattletech.com/PDF/AskPMForumArchiveandFAQ.pdf
                        // says that the damage counts for making PSRs.
                        te.damageThisPhase += damage;

                        // The target takes no more damage from the explosion.
                        damage = 0;
                    } else if (damage > 0) {
                        // remaining damage transfers
                        r = new Report(6130);
                        r.subject = te_n;
                        r.indent(2);
                        r.newlines = 0;
                        r.add(damage);
                        r.add(te.getLocationAbbr(nextHit));
                        vDesc.addElement(r);

                        // If there are split weapons in this location, mark it
                        // as
                        // destroyed, even if it took no criticals.
                        for (Mounted m : te.getWeaponList()) {
                            if (m.isSplit()) {
                                if (m.getLocation() == hit.getLocation() || m.getLocation() == nextHit.getLocation()) {
                                    te.setWeaponDestroyed(m);
                                }
                            }
                        }
                    }
                }
            } else if (hit.getSpecCritMod() < 0) {
                // If there ISN'T any armor left but we did damage, then there's
                // a chance of a crit, using Armor Piercing.
                specCrits++;
            }
            // check for breaching
            vDesc.addAll(breachCheck(te, hit.getLocation(), null));

            // resolve special results
            if ((hit.getEffect() & HitData.EFFECT_VEHICLE_MOVE_DAMAGED) == HitData.EFFECT_VEHICLE_MOVE_DAMAGED) {
                vDesc.addAll(vehicleMotiveDamage((Tank) te, hit.getMotiveMod()));
            } else if ((hit.getEffect() & HitData.EFFECT_GUN_EMPLACEMENT_WEAPONS) == HitData.EFFECT_GUN_EMPLACEMENT_WEAPONS) {
                r = new Report(6146);
                r.subject = te_n;
                r.indent(3);
                vDesc.addElement(r);
                for (Mounted mounted : te.getWeaponList()) {
                    mounted.setDestroyed(true);
                }
            } else if ((hit.getEffect() & HitData.EFFECT_GUN_EMPLACEMENT_TURRET) == HitData.EFFECT_GUN_EMPLACEMENT_TURRET) {
                r = new Report(6145);
                r.subject = te_n;
                r.indent(3);
                vDesc.addElement(r);
                ((GunEmplacement) te).setTurretLocked(true);
            } else if ((hit.getEffect() & HitData.EFFECT_GUN_EMPLACEMENT_CREW) == HitData.EFFECT_GUN_EMPLACEMENT_CREW) {
                r = new Report(6148);
                r.subject = te_n;
                r.indent(3);
                vDesc.addElement(r);
                ((GunEmplacement) te).getCrew().setDoomed(true);
            }
            // roll all critical hits against this location
            // unless the section destroyed in a previous phase?
            // Cause a crit.
            if (te.getInternal(hit) != IArmorState.ARMOR_DESTROYED && ((hit.getEffect() & HitData.EFFECT_NO_CRITICALS) != HitData.EFFECT_NO_CRITICALS)) {
                for (int i = 0; i < crits; i++) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalEntity(te, hit.getLocation(), hit.glancingMod()));
                }
                crits = 0;

                for (int i = 0; i < specCrits; i++) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalEntity(te, hit.getLocation(), (hardenedArmor ? -2 : hit.getSpecCritMod()) + hit.glancingMod()));
                }
                specCrits = 0;
            }

            // resolve Aero crits
            if (te instanceof Aero) {
                // apply crits
                Aero a = (Aero) te;

                // check for nuclear critical
                if(nukeS2S) {
                    //add a control roll 
                    PilotingRollData nukePSR = new PilotingRollData( te.getId(), 4, "Nuclear attack" );
                    nukePSR.setCumulative(false);
                    game.addControlRoll(nukePSR);
                    
                    //need some kind of report 
                    int nukeroll = Compute.d6(2);
                    r = new Report(9145); 
                    r.subject = a.getId(); 
                    r.newlines = 0; 
                    r.indent(3); 
                    r.add(CapitalMissile); 
                    r.add(nukeroll);
                    vDesc.add(r); 
                    if(nukeroll >= CapitalMissile) {                      
                        int nukeDamage = damage_orig;
                        a.setSI(a.getSI()-nukeDamage); 
                        te.damageThisPhase += nukeDamage; 
                        r = new Report(9146); 
                        r.subject = a.getId();
                        r.newlines = 0; 
                        r.add(nukeDamage);
                        r.add(Math.max(a.getSI(),0)); 
                        vDesc.addElement(r);
                        if(a.getSI() <= 0) { 
                            vDesc.addAll( destroyEntity(te,"structural integrity collapse")); 
                            a.setSI(0); 
                        } else if(!critSI) { 
                            critSI = true; 
                        }     
                    } else { 
                        r = new Report(9147); 
                        r.subject = a.getId(); 
                        r.newlines = 0;
                        vDesc.addElement(r); 
                    }     
                }
                  
                if (critBoxCars) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "12 to hit", 8, damage_orig, isCapital));
                }
                if (critThresh && !ammoExplosion && !finalSquadDamage) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "Damage threshold exceeded", 8, damage_orig, isCapital));
                }
                if (critSI && !ammoExplosion && !finalSquadDamage) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "SI damaged", 8, damage_orig, isCapital));
                }
                if (CapitalMissile > 0 && !nukeS2S && !finalSquadDamage) {
                    vDesc.elementAt(vDesc.size() - 1).newlines++;
                    vDesc.addAll(criticalAero(a, hit.getLocation(), hit.glancingMod(), "Capital Missile", CapitalMissile, damage_orig, isCapital));
                }
            }

            if (isHeadHit) {
                Report.addNewline(vDesc);
                vDesc.addAll(damageCrew(te, 1));
            }

            // loop to next location
            hit = nextHit;
            if (damageIS) {
                wasDamageIS = true;
                damageIS = false;
            }
        }
        // Mechs using EI implants take pilot damage each time a hit
        // inflicts IS damage
        if (tookInternalDamage && (te instanceof Mech || te instanceof Protomech) && te.hasActiveEiCockpit()) {
            Report.addNewline(vDesc);
            int roll = Compute.d6(2);
            r = new Report(5075);
            r.subject = te.getId();
            r.addDesc(te);
            r.add(7);
            r.add(roll);
            r.choose(roll >= 7);
            r.indent(2);
            vDesc.add(r);
            if (roll < 7) {
                vDesc.addAll(damageCrew(te, 1));
            }
        }

        // if using VDNI (but not buffered), check for damage on an internal hit
        if (tookInternalDamage && te.crew.getOptions().booleanOption("vdni") && !te.crew.getOptions().booleanOption("bvdni") && !te.crew.getOptions().booleanOption("pain_shunt")) {
            Report.addNewline(vDesc);
            int roll = Compute.d6(2);
            r = new Report(3580);
            r.subject = te.getId();
            r.addDesc(te);
            r.add(7);
            r.add(roll);
            r.choose(roll >= 8);
            r.indent(2);
            vDesc.add(r);
            if (roll >= 8) {
                vDesc.addAll(damageCrew(te, 1));
            }
        }

        // damage field guns on infantry platoons if there arent enough men left
        // to man it
        if (isPlatoon) {
            float tons = 0.0f;
            for (Mounted weap : te.getWeaponList()) {
                WeaponType wtype = (WeaponType) weap.getType();
                if (!wtype.hasFlag(WeaponType.F_INFANTRY)) {
                    tons += wtype.getTonnage(te);
                    if (tons > te.getInternal(Infantry.LOC_INFANTRY)) {
                        weap.setDestroyed(true);
                    }
                }
            }
        }

        // TacOps p.78 Ammo booms can hurt other units in same and adjcent hexes
        if (ammoExplosion 
                && game.getOptions().booleanOption("tacops_ammunition") 
                && damage > 0
                && damage_orig/10 > 0) {

            Report.addNewline(vDesc);
            r = new Report(5068, Report.PUBLIC);
            r.subject = te.getId();
            r.addDesc(te);
            r.indent(2);
            vDesc.add(r);
            Report.addNewline(vDesc);
            r = new Report(5400, Report.PUBLIC);
            r.subject = te.getId();
            r.indent(2);
            vDesc.add(r);
            int[] damages = { (int) Math.floor(damage_orig / 10), (int) Math.floor(damage_orig / 20) };
            doExplosion(damages, false, te.getPosition(), true, vDesc, null,5);
            Report.addNewline(vDesc);
            r = new Report(5410, Report.PUBLIC);
            r.subject = te.getId();
            r.indent(2);
            vDesc.add(r);
        }

        // This flag indicates the hit was directly to IS
        if (wasDamageIS) {
            Report.addNewline(vDesc);
        }
        return vDesc;
    }

    /**
     * Check to see if the entity's engine explodes. Rules for ICE explosions
     * are different to fusion engines.
     * 
     * @param en -
     *            the <code>Entity</code> in question. This value must not be
     *            <code>null</code>.
     * @param vDesc -
     *            the <code>Vector</code> that this function should add its
     *            <code>Report<code>s to.  It may be empty, but not
     *          <code>null</code>.
     * @param   hits - the number of criticals on the engine
     * @return  <code>true</code> if the unit's engine exploded,
     *          <code>false</code> if not.
     */
    private boolean checkEngineExplosion(Entity en, Vector<Report> vDesc, int hits) {
        if (!(en instanceof Mech) 
                && !(en instanceof QuadMech) 
                && !(en instanceof BipedMech) 
                && !(en instanceof Aero) 
                && !(en instanceof Tank)) {
            return false;
        }
        if (en.isDestroyed())
            return false;
        int explosionBTH = 10;
        int hitsPerRound = 4;
        Engine engine = null;

        if (en instanceof Mech) {
            engine = ((Mech) en).getEngine();
        } else if (en instanceof Tank) {
            explosionBTH = 12;
            hitsPerRound = 1;
            engine = ((Tank) en).getEngine();
        } else {
            engine = ((Aero) en).getEngine();
            explosionBTH = 12;
            hitsPerRound = 1;
        }

        // Non mechs and mechs that already rolled are safe
        if (en.rolledForEngineExplosion || !(en instanceof Mech))
            return false;
        // ICE can always explode and roll every time hit
        if (engine.isFusion() 
                && (!game.getOptions().booleanOption("tacops_engine_explosions") 
                        || en.engineHitsThisRound < hitsPerRound))
            return false;
        if (!engine.isFusion()) {
            switch (hits) {
            case 0:
                return false;
            case 1:
                explosionBTH = 10;
                break;
            case 2:
                explosionBTH = 7;
                break;
            case 3:
            default:
                explosionBTH = 4;
                break;
            }
        }
        int explosionRoll = Compute.d6(2);
        boolean didExplode = explosionRoll >= explosionBTH;

        Report r;
        r = new Report(6150);
        r.subject = en.getId();
        r.indent(2);
        r.addDesc(en);
        r.add(en.engineHitsThisRound);
        vDesc.addElement(r);
        r = new Report(6155);
        r.subject = en.getId();
        r.indent(2);
        r.add(explosionBTH);
        r.add(explosionRoll);
        vDesc.addElement(r);

        if (!didExplode) {
            // whew!
            if (engine.isFusion())
                en.rolledForEngineExplosion = true;
            // fusion engines only roll 1/phase but ICE roll every time damaged
            r = new Report(6160);
            r.subject = en.getId();
            r.indent(2);
            vDesc.addElement(r);
        } else {
            en.rolledForEngineExplosion = true;
            r = new Report(6165, Report.PUBLIC);
            r.subject = en.getId();
            r.indent(2);
            vDesc.addElement(r);
            vDesc.addAll(destroyEntity(en, "engine explosion", false, false));
            // kill the crew
            en.getCrew().setDoomed(true);

            // This is a hack so MM.NET marks the mech as not salvageable
            if (en instanceof Mech)
                en.destroyLocation(Mech.LOC_CT);

            // Light our hex on fire
            final IHex curHex = game.getBoard().getHex(en.getPosition());

            if (null != curHex && !curHex.containsTerrain(Terrains.FIRE)) {
                ignite(en.getPosition(), false, vDesc);
            }           

            // ICE explosions don't hurt anyone else, but fusion do
            if (engine.isFusion()) {
                int engineRating = en.getEngine().getRating();
                Report.addNewline(vDesc);
                r = new Report(5400, Report.PUBLIC);
                r.subject = en.getId();
                r.indent(2);
                vDesc.add(r);

                if (en instanceof Mech) {
                    Mech mech = (Mech) en;
                    if (mech.isAutoEject()) {
                        vDesc.addAll(ejectEntity(en, true));
                    }
                }

                doFusionEngineExplosion(engineRating, en.getPosition(), vDesc, null);
                Report.addNewline(vDesc);
                r = new Report(5410, Report.PUBLIC);
                r.subject = en.getId();
                r.indent(2);
                vDesc.add(r);

            }
        }

        return didExplode;
    }

    /**
     * Extract explosion functionality for generalized explosions in areas.
     */
    public void doFusionEngineExplosion(int engineRating, Coords position, Vector<Report> vDesc, Vector<Integer> vUnits) {
        int[] myDamages = { engineRating, (engineRating / 10), (engineRating / 20), (engineRating / 40) };
        doExplosion(myDamages, true, position, false, vDesc, vUnits,5);
    }

    /**
     * General function to cause explosions in areas.
     */
    public void doExplosion(int damage, int degredation, boolean autoDestroyInSameHex, Coords position, boolean allowShelter, Vector<Report> vDesc, Vector<Integer> vUnits) {

        if (degredation < 1)
            return;

        int[] myDamages = new int[damage / degredation];

        if (myDamages.length < 1)
            return;

        myDamages[0] = damage;
        for (int x = 1; x < myDamages.length; x++)
            myDamages[x] = myDamages[x - 1] - degredation;
        doExplosion(myDamages, autoDestroyInSameHex, position, allowShelter, vDesc, vUnits,5);
    }

    /**
     * General function to cause explosions in areas.
     */
    public void doExplosion(int[] damages, boolean autoDestroyInSameHex, Coords position, boolean allowShelter, Vector<Report> vDesc, Vector<Integer> vUnits, int clusterAmt) {
        if (vDesc == null)
            vDesc = new Vector<Report>();

        if (vUnits == null)
            vUnits = new Vector<Integer>();

        Report r;
        HashSet<Entity> entitiesHit = new HashSet<Entity>();

        // We need to damage buildings.
        Enumeration<Building> bldgs = game.getBoard().getBuildings();
        while (bldgs.hasMoreElements()) {
            final Building bldg = bldgs.nextElement();

            // Lets find the closest hex from the building.
            Enumeration<Coords> hexes = bldg.getCoords();
            
            while (hexes.hasMoreElements()) {
                final Coords coords = hexes.nextElement();
                int dist = position.distance(coords);
                if (dist < damages.length) {
                    Vector<Report> buildingReport = damageBuilding(bldg, damages[dist], coords);
                    for (Report report : buildingReport) {
                        report.type = Report.PUBLIC;
                    }
                    vDesc.addAll(buildingReport);
                }
            }
        }
        applyBuildingDamage();

        // Now we damage people near the explosion.
        ArrayList<Entity> loaded = new ArrayList<Entity>();
        for (Enumeration<Entity> entities = game.getEntities(); entities.hasMoreElements();) {

            Entity entity = entities.nextElement();

            if (entitiesHit.contains(entity))
                continue;

            if (entity.isDestroyed() || !entity.isDeployed()) {
                // FIXME
                // IS this the behavior we want?
                // This means, incidentally, that salvage is never affected by
                // explosions
                // as long as it was destroyed before the explosion.
                continue;
            }

            if ( entity instanceof MechWarrior && !((MechWarrior)entity).hasLanded() ){
                //MechWarrior is still up in the air ejecting their for safe from this explosion.
                continue;
            }
            
            Coords entityPos = entity.getPosition();
            if (entityPos == null) {
                // maybe its loaded?
                Entity transport = game.getEntity(entity.getTransportId());
                if (transport != null) {
                    loaded.add(entity);
                }
                continue;
            }
            int range = position.distance(entityPos);

            if (range >= damages.length) {
                // Yeah, this is fine. It's outside the blast radius.
                continue;
            }

            // We might need to nuke everyone in the explosion hex. If so...
            if (range == 0 && autoDestroyInSameHex) {
                // Add the reports
                vDesc.addAll(destroyEntity(entity, "explosion proximity", false, false));

                // Add it to the "blasted units" list
                vUnits.add(entity.getId());

                // Kill the crew
                entity.getCrew().setDoomed(true);

                entitiesHit.add(entity);

                continue;
            }

            int damage = damages[range];

            if (allowShelter && canShelter(entityPos, position, entity.absHeight())) {
                if (isSheltered()) {
                    r = new Report(6545);
                    r.addDesc(entity);
                    r.subject = entity.getId();
                    vDesc.addElement(r);
                    continue;
                }
                // If shelter is allowed but didn't work, report that.
                r = new Report(6546);
                r.subject = entity.getId();
                r.addDesc(entity);
                vDesc.addElement(r);
            }

            // Since it's taking damage, add it to the list of units hit.
            vUnits.add(entity.getId());

            r = new Report(6175);
            r.subject = entity.getId();
            r.indent(2);
            r.addDesc(entity);
            r.add(damage);
            r.newlines = 0;
            vDesc.addElement(r);

            while (damage > 0) {
                int cluster = Math.min(clusterAmt, damage);
                HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, Compute.targetSideTable(position, entity));
                vDesc.addAll(damageEntity(entity, hit, cluster, false, DamageType.IGNORE_PASSENGER, false, true));
                damage -= cluster;
            }
            Report.addNewline(vDesc);
        }

        // now deal with loaded units...
        for (Entity e : loaded) {
            // This can be null, if the transport died from damage
            final Entity transporter = game.getEntity(e.getTransportId());
            if (transporter == null || transporter.getExternalUnits().contains(e)) {
                // Its external or transport was destroyed - hit it.
                final Coords entityPos = (transporter == null ? e.getPosition() : transporter.getPosition());
                final int range = position.distance(entityPos);

                if (range >= damages.length) {
                    // Yeah, this is fine. It's outside the blast radius.
                    continue;
                }

                int damage = damages[range];
                if (allowShelter) {
                    final int absHeight = (transporter == null ? e.absHeight() : transporter.absHeight());
                    if (entityPos != null && canShelter(entityPos, position, absHeight)) {
                        if (isSheltered()) {
                            r = new Report(6545);
                            r.addDesc(e);
                            r.subject = e.getId();
                            vDesc.addElement(r);
                            continue;
                        }
                        // If shelter is allowed but didn't work, report that.
                        r = new Report(6546);
                        r.subject = e.getId();
                        r.addDesc(e);
                        vDesc.addElement(r);
                    }
                }
                // No shelter
                // Since it's taking damage, add it to the list of units hit.
                vUnits.add(e.getId());

                r = new Report(6175);
                r.subject = e.getId();
                r.indent(2);
                r.addDesc(e);
                r.add(damage);
                r.newlines = 0;
                vDesc.addElement(r);

                while (damage > 0) {
                    int cluster = Math.min(5, damage);
                    HitData hit = e.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    vDesc.addAll(damageEntity(e, hit, cluster, false, DamageType.IGNORE_PASSENGER, false, true));
                    damage -= cluster;
                }
                Report.addNewline(vDesc);
            }
        }
    }

    /**
     * Check if an Entity of the passed height can find shelter from a nukeblast
     * 
     * @param entityPosition
     *            the <code>Coords</code> the Entity is at
     * @param position
     *            the <code>Coords</code> of the explosion
     * @param entityAbsHeight
     *            the <code>int</code> height of the entity
     * @return a <code>boolean</code> value indicating if the entity of the
     *         given height can find shelter
     */
    public boolean canShelter(Coords entityPosition, Coords position, int entityAbsHeight) {
        // What is the next hex in the direction of the blast?
        Coords shelteringCoords = Coords.nextHex(entityPosition, position);
        IHex shelteringHex = game.getBoard().getHex(shelteringCoords);

        // This is an error condition. It really shouldn't ever happen.
        if (shelteringHex == null)
            return false;

        // Now figure out the height to which that hex will provide shelter.
        // It's worth noting, this assumes that any building in the hex has
        // already survived the bomb blast. In the case where a building
        // won't survive the blast but hasn't actually taken the damage
        // yet, this will be wrong.
        int shelterLevel = shelteringHex.floor();
        if (shelteringHex.containsTerrain(Terrains.BUILDING))
            shelterLevel = shelteringHex.ceiling();

        // Get the absolute height of the unit relative to level 0.
        entityAbsHeight += game.getBoard().getHex(entityPosition).surface();

        // Now find the height that needs to be sheltered, and compare.
        if (entityAbsHeight < shelterLevel)
            return true;

        // Well, if the above isn't true... Give up.
        return false;
    }

    /**
     * @return true if the unit succeeds a shelter roll
     */
    private boolean isSheltered() {
        if (Compute.d6(2) >= 9)
            return true;
        return false;
    }

    /**
     * add a nuke to be exploded in the next weapons attack phase
     * 
     * @param nuke
     *            the nuke paramater either 3 or 6 ints 0 and 1 being X and Y
     *            when 3 ints: third is nuketype (from HS:3070) if 6 ints: third
     *            is damage fourth is degradation fifth is secondary radius
     *            sixth is crater depth
     */
    public void addScheduledNuke(int[] nuke) {
        scheduledNukes.add(nuke);
    }

    /**
     * explode any scheduled nukes
     */
    private void resolveScheduledNukes() {
        for (int[] nuke : scheduledNukes) {
            if (nuke.length == 3) {
                doNuclearExplosion(new Coords(nuke[0] - 1, nuke[1] - 1), nuke[2], vPhaseReport);
            }
            if (nuke.length == 6) {
                doNuclearExplosion(new Coords(nuke[0] - 1, nuke[1] - 1), nuke[2], nuke[3], nuke[4], nuke[5], vPhaseReport);
            }
        }
        scheduledNukes.clear();
    }

    /**
     * do a nuclear explosion
     * 
     * @param position
     * @param nukeType
     * @param vDesc
     */
    public void doNuclearExplosion(Coords position, int nukeType, Vector<Report> vDesc) {
        // Throws a nuke for one of the pre-defined types.
        switch (nukeType) {
        case 0:
        case 1:
            doNuclearExplosion(position, 100, 5, 40, 0, vDesc);
            break;
        case 2:
            doNuclearExplosion(position, 1000, 23, 86, 1, vDesc);
            break;
        case 3:
            doNuclearExplosion(position, 10000, 109, 184, 3, vDesc);
            break;
        case 4:
            doNuclearExplosion(position, 100000, 505, 396, 5, vDesc);
            break;
        default:
            // This isn't a valid nuke type by HS:3070 rules.
            // And since that's our only current source...
            return;
        }
    }

    /**
     * explode a nuke
     * 
     * @param position
     * @param baseDamage
     * @param degredation
     * @param secondaryRadius
     * @param craterDepth
     * @param vDesc
     */
    public void doNuclearExplosion(Coords position, int baseDamage, int degredation, int secondaryRadius, int craterDepth, Vector<Report> vDesc) {
        // Just in case.
        if (vDesc == null)
            vDesc = new Vector<Report>();

        // First, crater the terrain.
        // All terrain, units, buildings... EVERYTHING in here is just gone.
        // Gotta love nukes.
        Report r = new Report(1215, Report.PUBLIC);

        r.indent();
        r.add(position.getBoardNum(), true);
        vDesc.add(r);

        int curDepth = game.getBoard().getHex(position).floor() - craterDepth;
        int range = 0;
        while (range < 2 * craterDepth) {
            // Get the set of hexes at this range.
            Enumeration<Coords> hexSet = game.getBoard().getHexesAtDistance(position, range);

            // Iterate through the hexes.
            while (hexSet.hasMoreElements()) {
                Coords myHexCoords = hexSet.nextElement();
                IHex myHex = game.getBoard().getHex(myHexCoords);
                // In each hex, first, sink the terrain if necessary.
                myHex.setElevation(myHex.getElevation() - craterDepth + (range / 2));

                // Then, remove ANY terrains here.
                // I mean ALL of them; they're all just gone.
                // No ruins, no water, no rough, no nothing.
                if (myHex.containsTerrain(Terrains.WATER)) {
                    myHex.setElevation(myHex.floor());
                }
                myHex.removeAllTerrains();
                myHex.clearExits();

                sendChangedHex(myHexCoords);
            }

            // Now that the hexes are dealt with, increment the distance.
            range++;

            // Lastly, if the next distance is a multiple of 2...
            // The crater depth goes down one.
            if ((range > 0) && (range % 2 == 0))
                curDepth++;
        }

        // This is technically part of cratering, but...
        // Now we destroy all the units inside the cratering range.
        Enumeration<Entity> entitiesInCrater = game.getEntities();

        while (entitiesInCrater.hasMoreElements()) {
            Entity entity = entitiesInCrater.nextElement();

            // loaded units and off board units don't have a position,
            // so we don't count 'em here
            if (entity.getTransportId() != Entity.NONE || entity.getPosition() == null)
                continue;

            // If it's too far away for this...
            if (position.distance(entity.getPosition()) >= range)
                continue;

            // If it's already destroyed...
            if (entity.isDestroyed())
                continue;

            vDesc.addAll(destroyEntity(entity, "nuclear explosion proximity", false, false));
            // Kill the crew
            entity.getCrew().setDoomed(true);
        }
        entitiesInCrater = null;

        // Then, do actual blast damage.
        // Use the standard blast function for this.
        Vector<Report> tmpV = new Vector<Report>();
        Vector<Integer> blastedUnitsVec = new Vector<Integer>();
        doExplosion(baseDamage, degredation, true, position, true, tmpV, blastedUnitsVec);
        Report.indentAll(tmpV, 2);
        vDesc.addAll(tmpV);

        // Everything that was blasted by the explosion has to make a piloting
        // check at +6.
        for (Object i : blastedUnitsVec) {
            Entity o = game.getEntity((Integer) i);
            if (o instanceof Mech) {
                Mech bm = (Mech) o;
                // Needs a piloting check at +6 to avoid falling over.
                // Obviously not if it's already prone, though.
                if (!bm.isProne()) {
                    game.addPSR(new PilotingRollData(bm.getId(), 6, "hit by nuclear blast"));
                }
            } else if (o instanceof VTOL) {
                // Needs a piloting check at +6 to avoid crashing.
                // Wheeeeee!
                VTOL vt = (VTOL) o;

                // Check only applies if it's in the air.
                // FIXME: is this actually correct? What about
                // buildings/bridges?
                if (vt.getElevation() > 0) {
                    game.addPSR(new PilotingRollData(vt.getId(), 6, "hit by nuclear blast"));
                }
            } else if (o instanceof Tank) {
                // As per official answer on the rules questions board...
                // Needs a piloting check at +6 to avoid a 1-level fall...
                // But ONLY if a hover-tank.
                // FIXME
            }
        }

        // Just get rid of it for the balance of the function...
        tmpV = null;

        // This ISN'T part of the blast, but if there's ANYTHING in the ground
        // zero hex, destroy it.
        Building tmpB = game.getBoard().getBuildingAt(position);
        if (tmpB != null) {
            r = new Report(2415);
            r.add(tmpB.getName());
            addReport(r);
            collapseBuilding(tmpB, game.getPositionMap(), position);
        }
        IHex gzHex = game.getBoard().getHex(position);
        if (gzHex.containsTerrain(Terrains.WATER)) {
            gzHex.setElevation(gzHex.floor());
        }
        gzHex.removeAllTerrains();

        // Next, for whatever's left, do terrain effects
        // such as clearing, roughing, and boiling off water.
        boolean damageFlag = true;
        int damageAtRange = baseDamage - (degredation * range);
        if (damageAtRange > 0) {
            for (int x = range; damageFlag; x++) {
                // Damage terrain as necessary.
                // Get all the hexes, and then iterate through them.
                Enumeration<Coords> hexSet = game.getBoard().getHexesAtDistance(position, x);

                // Iterate through the hexes.
                while (hexSet.hasMoreElements()) {
                    Coords myHexCoords = hexSet.nextElement();
                    IHex myHex = game.getBoard().getHex(myHexCoords);

                    // For each 3000 damage, water level is reduced by 1.
                    if ((damageAtRange >= 3000) && (myHex.containsTerrain(Terrains.WATER))) {
                        int numCleared = damageAtRange / 3000;
                        int oldLevel = myHex.terrainLevel(Terrains.WATER);
                        myHex.removeTerrain(Terrains.WATER);
                        if (oldLevel > numCleared) {
                            myHex.addTerrain(new Terrain(Terrains.WATER, oldLevel - numCleared));
                        }
                    }

                    // ANY non-water hex that takes 200 becomes rough.
                    if ((damageAtRange >= 200) && (!myHex.containsTerrain(Terrains.WATER))) {
                        myHex.removeAllTerrains();
                        myHex.clearExits();
                        myHex.addTerrain(new Terrain(Terrains.ROUGH, 1));
                    } else if ((damageAtRange >= 20) && ((myHex.containsTerrain(Terrains.WOODS)) || (myHex.containsTerrain(Terrains.JUNGLE)))) {
                        // Each 20 clears woods by 1 level.
                        int numCleared = damageAtRange / 20;
                        int terrainType = (myHex.containsTerrain(Terrains.WOODS) ? Terrains.WOODS : Terrains.JUNGLE);
                        int oldLevel = myHex.terrainLevel(terrainType);
                        myHex.removeTerrain(terrainType);
                        if (oldLevel > numCleared) {
                            myHex.addTerrain(new Terrain(terrainType, oldLevel - numCleared));
                        }
                    }

                    sendChangedHex(myHexCoords);
                }

                // Initialize for the next iteration.
                damageAtRange = baseDamage - (degredation * x + 1);

                // If the damage is less than 20, it has no terrain effect.
                if (damageAtRange < 20) {
                    damageFlag = false;
                }
            }
        }

        // Lastly, do secondary effects.
        Enumeration<Entity> entitiesInSecondaryRange = game.getEntities();
        while (entitiesInSecondaryRange.hasMoreElements()) {
            Entity entity = entitiesInSecondaryRange.nextElement();

            // loaded units and off board units don't have a position,
            // so we don't count 'em here
            if (entity.getTransportId() != Entity.NONE || entity.getPosition() == null)
                continue;

            // If it's already destroyed...
            if ((entity.isDoomed()) || (entity.isDestroyed()))
                continue;

            // If it's too far away for this...
            if (position.distance(entity.getPosition()) > secondaryRadius)
                continue;

            // Actually do secondary effects against it.
            // Since the effects are unit-dependant, we'll just define it in the
            // entity.
            applySecondaryNuclearEffects(entity, position, vDesc);
        }

        // All right. We're done.
        r = new Report(1216, Report.PUBLIC);
        r.indent();
        r.newlines = 2;
        vDesc.add(r);
    }

    /**
     * Handles secondary effects from nuclear blasts against all units in range.
     * 
     * @param entity
     *            The entity to affect.
     * @param position
     *            The coordinates of the nuclear blast, for to-hit directions.
     * @param vDesc
     *            a description vector to use for reports.
     */
    public void applySecondaryNuclearEffects(Entity entity, Coords position, Vector<Report> vDesc) {
        // If it's already destroyed, give up. We really don't care.
        if (entity.isDestroyed())
            return;

        // Check to see if the infantry is in a protective structure.
        boolean inHardenedBuilding = (Compute.isInBuilding(game, entity) && (game.getBoard().getHex(entity.getPosition()).terrainLevel(Terrains.BUILDING) == 4));

        // Roll 2d6.
        int roll = Compute.d6(2);

        Report r = new Report(6555);
        r.subject = entity.getId();
        r.add(entity.getDisplayName());
        r.add(roll);

        // If they are in protective structure, add 2 to the roll.
        if (inHardenedBuilding) {
            roll += 2;
            r.add(" + 2 (unit is in hardened building)");
        } else {
            r.add("");
        }

        // Also, if the entity is "hardened" against EMI, it gets a +2.
        // For these purposes, I'm going to hand this off to the Entity itself
        // to tell us.
        // Right now, it IS based purely on class, but I won't rule out the idea
        // of
        // "nuclear hardening" as equipment for a support vehicle, for example.
        if (entity.isNuclearHardened()) {
            roll += 2;
            r.add(" + 2 (unit is hardened against EMI)");
        } else {
            r.add("");
        }

        r.indent(2);
        vDesc.add(r);

        // Now, compare it to the table, and apply the effects.
        if (roll <= 4) {
            // The unit is destroyed.
            // Sucks, doesn't it?
            // This applies to all units.
            // Yup, just sucks.
            vDesc.addAll(destroyEntity(entity, "nuclear explosion secondary effects", false, false));
            // Kill the crew
            entity.getCrew().setDoomed(true);
        } else if (roll <= 6) {
            if (entity instanceof BattleArmor) {
                // It takes 50% casualties, rounded up.
                BattleArmor myBA = (BattleArmor) entity;
                int numDeaths = (int) (Math.ceil((myBA.getNumberActiverTroopers())) / 2.0);
                for (int x = 0; x < numDeaths; x++)
                    vDesc.addAll(applyCriticalHit(entity, 0, null, false));
            } else if (entity instanceof Infantry) {
                // Standard infantry are auto-killed in this band, unless
                // they're in a building.
                if (game.getBoard().getHex(entity.getPosition()).containsTerrain(Terrains.BUILDING)) {
                    // 50% casualties, rounded up.
                    int damage = (int) (Math.ceil((((Infantry) entity).getInternal(Infantry.LOC_INFANTRY)) / 2.0));
                    vDesc.addAll(damageEntity(entity, new HitData(Infantry.LOC_INFANTRY), damage, true));
                } else {
                    vDesc.addAll(destroyEntity(entity, "nuclear explosion secondary effects", false, false));
                    entity.getCrew().setDoomed(true);
                }
            } else if (entity instanceof Tank) {
                // All vehicles suffer two critical hits...
                HitData hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));
                hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));

                // ...and a Crew Killed hit.
                vDesc.addAll(applyCriticalHit(entity, 0, new CriticalSlot(0, Tank.CRIT_CREW_KILLED), false));
            } else if ((entity instanceof Mech) || (entity instanceof Protomech)) {
                // 'Mechs suffer two critical hits...
                HitData hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));
                hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));

                // and four pilot hits.
                vDesc.addAll(damageCrew(entity, 4));
            } else {
                // Buildings and gun emplacements and such are only effected by
                // the EMI.
                // No auto-crits or anything.
            }
        } else if (roll <= 10) {
            if (entity instanceof BattleArmor) {
                // It takes 25% casualties, rounded up.
                BattleArmor myBA = (BattleArmor) entity;
                int numDeaths = (int) (Math.ceil(((myBA.getNumberActiverTroopers())) / 4.0));
                for (int x = 0; x < numDeaths; x++)
                    vDesc.addAll(applyCriticalHit(entity, 0, null, false));
            } else if (entity instanceof Infantry) {
                if (game.getBoard().getHex(entity.getPosition()).containsTerrain(Terrains.BUILDING)) {
                    // 25% casualties, rounded up.
                    int damage = (int) (Math.ceil((((Infantry) entity).getInternal(Infantry.LOC_INFANTRY)) / 4.0));
                    vDesc.addAll(damageEntity(entity, new HitData(Infantry.LOC_INFANTRY), damage, true));
                } else {
                    // 50% casualties, rounded up.
                    int damage = (int) (Math.ceil((((Infantry) entity).getInternal(Infantry.LOC_INFANTRY)) / 2.0));
                    vDesc.addAll(damageEntity(entity, new HitData(Infantry.LOC_INFANTRY), damage, true));
                }
            } else if (entity instanceof Tank) {
                // It takes one crit...
                HitData hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));

                // Plus a Crew Stunned critical.
                vDesc.addAll(applyCriticalHit(entity, 0, new CriticalSlot(0, Tank.CRIT_CREW_STUNNED), false));
            } else if ((entity instanceof Mech) || (entity instanceof Protomech)) {
                // 'Mechs suffer a critical hit...
                HitData hd = entity.rollHitLocation(ToHitData.HIT_NORMAL, entity.sideTable(position));
                vDesc.addAll(oneCriticalEntity(entity, hd.getLocation()));

                // and two pilot hits.
                vDesc.addAll(damageCrew(entity, 2));
            } else {
                // Buildings and gun emplacements and such are only effected by
                // the EMI.
                // No auto-crits or anything.
            }
        }
        // If it's 11+, there are no secondary effects beyond EMI.
        // Lucky bastards.

        // And lastly, the unit is now affected by electromagnetic interference.
        entity.setEMI(true);
    }

    /**
     * Apply a single critical hit. The following private member of Server are
     * accessed from this function, preventing it from being factored out of the
     * Server class: destroyEntity() destroyLocation() checkEngineExplosion()
     * damageCrew() explodeEquipment() game
     * 
     * @param en
     *            the <code>Entity</code> that is being damaged. This value
     *            may not be <code>null</code>.
     * @param loc
     *            the <code>int</code> location of critical hit. This value
     *            may be <code>Entity.NONE</code> for hits to
     *            <code>Tank</code>s and for hits to a <code>Protomech</code>
     *            torso weapon.
     * @param cs
     *            the <code>CriticalSlot</code> being damaged. This value may
     *            not be <code>null</code>. For critical hits on a
     *            <code>Tank</code>, the index of the slot should be the
     *            index of the critical hit table.
     * @param secondaryEffects
     *            the <code>boolean</code> flag that indicates whether to
     *            allow critical hits to cause secondary effects (such as
     *            triggering an ammo explosion, sending hovercraft to watery
     *            graves, or damaging Protomech torso weapons). This value is
     *            normally <code>true</code>, but it will be
     *            <code>false</code> when the hit is being applied from a
     *            saved game or scenario.
     */
    public Vector<Report> applyCriticalHit(Entity en, int loc, CriticalSlot cs, boolean secondaryEffects) {
        return applyCriticalHit(en, loc, cs, secondaryEffects, -1, false);
    }

    /**
     * Apply a single critical hit. The following private member of Server are
     * accessed from this function, preventing it from being factored out of the
     * Server class: destroyEntity() destroyLocation() checkEngineExplosion()
     * damageCrew() explodeEquipment() game
     * 
     * @param en
     *            the <code>Entity</code> that is being damaged. This value
     *            may not be <code>null</code>.
     * @param loc
     *            the <code>int</code> location of critical hit. This value
     *            may be <code>Entity.NONE</code> for hits to
     *            <code>Tank</code>s and for hits to a <code>Protomech</code>
     *            torso weapon.
     * @param cs
     *            the <code>CriticalSlot</code> being damaged. This value may
     *            not be <code>null</code>. For critical hits on a
     *            <code>Tank</code>, the index of the slot should be the
     *            index of the critical hit table.
     * @param secondaryEffects
     *            the <code>boolean</code> flag that indicates whether to
     *            allow critical hits to cause secondary effects (such as
     *            triggering an ammo explosion, sending hovercraft to watery
     *            graves, or damaging Protomech torso weapons). This value is
     *            normally <code>true</code>, but it will be
     *            <code>false</code> when the hit is being applied from a
     *            saved game or scenario.
     * @param damageCaused
     *            the amount of damage causing this critical. Necessary for
     *            Aeros, but may be -1 for other units.
     * @param isCapital
     *            whether it was capital scale damage that caused critical
     */
    public Vector<Report> applyCriticalHit(Entity en, int loc, CriticalSlot cs, boolean secondaryEffects, int damageCaused, boolean isCapital) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // Handle hits on "critical slots" of tanks.
        if (en instanceof Tank) {
            Tank t = (Tank) en;
            HitData hit;
            switch (cs.getIndex()) {
            case Tank.CRIT_NONE:
                // no effect
                r = new Report(6005);
                r.subject = t.getId();
                vDesc.add(r);
                break;
            case Tank.CRIT_AMMO:
                // ammo explosion
                r = new Report(6610);
                r.subject = t.getId();
                vDesc.add(r);
                int damage = 0;
                for (Mounted m : t.getAmmo()) {
                    m.setHit(true);
                    int tmp = m.getShotsLeft() * ((AmmoType) m.getType()).getDamagePerShot() * ((AmmoType) m.getType()).getRackSize();
                    m.setShotsLeft(0);
                    // non-explosive ammo can't explode
                    if (!m.getType().isExplosive()) {
                        continue;
                    }
                    damage += tmp;
                    r = new Report(6390);
                    r.subject = t.getId();
                    r.add(m.getName());
                    r.add(tmp);
                    r.newlines = 0;
                    vDesc.add(r);
                }
                hit = new HitData(loc);
                vDesc.addAll(damageEntity(t, hit, damage, true));
                break;
            case Tank.CRIT_CARGO:
                // Cargo/infantry damage
                r = new Report(6615);
                r.subject = t.getId();
                vDesc.add(r);
                Vector<Entity> passengers = t.getLoadedUnits();
                Entity target = passengers.get(Compute.randomInt(passengers.size()));
                hit = target.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                // FIXME should be original weapon damage
                vDesc.addAll(damageEntity(target, hit, 5));
                break;
            case Tank.CRIT_COMMANDER:
                if (en.crew.getOptions().booleanOption("vdni") || en.crew.getOptions().booleanOption("bvdni")) {
                    r = new Report(6191);
                    r.subject = t.getId();
                    vDesc.add(r);
                    vDesc.addAll(damageCrew(en, 1));
                } else {
                    if (en.crew.getOptions().booleanOption("pain_shunt") && !t.isCommanderHitPS()) {
                        r = new Report(6606);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.setCommanderHitPS(true);
                    } else {
                        r = new Report(6605);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.setCommanderHit(true);
                    }
                }
                // fall through here, because effects of crew stunned also apply
            case Tank.CRIT_CREW_STUNNED:
                if (en.crew.getOptions().booleanOption("vdni") || en.crew.getOptions().booleanOption("bvdni")) {
                    r = new Report(6191);
                    r.subject = t.getId();
                    vDesc.add(r);
                    vDesc.addAll(damageCrew(en, 1));
                } else {
                    if (en.crew.getOptions().booleanOption("pain_shunt")) {
                        r = new Report(6186);
                        r.subject = t.getId();
                        vDesc.add(r);
                    } else {
                        t.stunCrew();
                        r = new Report(6185);
                        r.add(t.getStunnedTurns() - 1);
                        r.subject = t.getId();
                        vDesc.add(r);
                    }
                }
                break;
            case Tank.CRIT_DRIVER:
                if (en.crew.getOptions().booleanOption("vdni") || en.crew.getOptions().booleanOption("bvdni")) {
                    r = new Report(6191);
                    r.subject = t.getId();
                    vDesc.add(r);
                    vDesc.addAll(damageCrew(en, 1));
                } else {
                    if (en.crew.getOptions().booleanOption("pain_shunt") && !t.isDriverHitPS()) {
                        r = new Report(6601);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.setDriverHitPS(true);
                    } else {
                        r = new Report(6600);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.setDriverHit(true);
                    }
                }
                break;
            case Tank.CRIT_CREW_KILLED:
                if (en.crew.getOptions().booleanOption("vdni") || en.crew.getOptions().booleanOption("bvdni")) {
                    r = new Report(6191);
                    r.subject = t.getId();
                    vDesc.add(r);
                    vDesc.addAll(damageCrew(en, 1));
                } else {
                    if (en.crew.getOptions().booleanOption("pain_shunt") && !t.isCrewHitPS()) {
                        r = new Report(6191);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.setCrewHitPS(true);
                    } else {
                        r = new Report(6190);
                        r.subject = t.getId();
                        vDesc.add(r);
                        t.getCrew().setDoomed(true);
                    }
                }
                break;
            case Tank.CRIT_ENGINE:
                r = new Report(6210);
                r.subject = t.getId();
                vDesc.add(r);
                t.engineHit();
                t.engineHitsThisRound++;
                boolean engineExploded = checkEngineExplosion(t, vDesc, 1);
                if (engineExploded) {
                    vDesc.addAll(destroyEntity(en, "engine destruction", true, true));
                }

                if (t instanceof VTOL) {
                    PilotingRollData psr = t.getBasePilotingRoll();
                    IHex hex = game.getBoard().getHex(t.getPosition());
                    psr.addModifier(4, "forced landing");
                    int elevation = Math.max(hex.terrainLevel(Terrains.BLDG_ELEV), hex.terrainLevel(Terrains.BRIDGE_ELEV));
                    elevation = Math.max(elevation, 0);
                    elevation = Math.min(elevation, t.getElevation());
                    if (t.getElevation() > elevation) {
                        if (!hex.containsTerrain(Terrains.FUEL_TANK) && !hex.containsTerrain(Terrains.JUNGLE) && !hex.containsTerrain(Terrains.MAGMA) && !hex.containsTerrain(Terrains.MUD) && !hex.containsTerrain(Terrains.RUBBLE) && !hex.containsTerrain(Terrains.WATER) && !hex.containsTerrain(Terrains.WOODS)) {
                            r = new Report(2180);
                            r.subject = t.getId();
                            r.addDesc(t);
                            r.add(psr.getLastPlainDesc(), true);
                            vDesc.add(r);

                            // roll
                            final int diceRoll = Compute.d6(2);
                            r = new Report(2185);
                            r.subject = t.getId();
                            r.add(psr.getValueAsString());
                            r.add(psr.getDesc());
                            r.add(diceRoll);
                            if (diceRoll < psr.getValue()) {
                                r.choose(false);
                                vDesc.add(r);
                                vDesc.addAll(crashVTOLorWiGE(t));
                            } else {
                                r.choose(true);
                                vDesc.add(r);
                                t.setElevation(elevation);
                            }
                        } else {
                            vDesc.addAll(crashVTOLorWiGE(t));
                        }
                    }
                }
                break;
            case Tank.CRIT_FUEL_TANK:
                r = new Report(6215);
                r.subject = t.getId();
                vDesc.add(r);
                vDesc.addAll(destroyEntity(t, "fuel explosion", false, false));
                break;
            case Tank.CRIT_SENSOR:
                r = new Report(6620);
                r.subject = t.getId();
                vDesc.add(r);
                t.setSensorHits(t.getSensorHits() + 1);
                break;
            case Tank.CRIT_STABILIZER:
                r = new Report(6625);
                r.subject = t.getId();
                vDesc.add(r);
                t.setStabiliserHit(loc);
                break;
            case Tank.CRIT_TURRET_DESTROYED:
                r = new Report(6630);
                r.subject = t.getId();
                vDesc.add(r);
                t.destroyLocation(Tank.LOC_TURRET);
                vDesc.addAll(destroyEntity(t, "turret blown off", true, true));
                break;
            case Tank.CRIT_TURRET_JAM:
                if (t.isTurretEverJammed()) {
                    r = new Report(6640);
                    r.subject = t.getId();
                    vDesc.add(r);
                    t.lockTurret();
                    break;
                }
                r = new Report(6635);
                r.subject = t.getId();
                vDesc.add(r);
                t.jamTurret();
                break;
            case Tank.CRIT_TURRET_LOCK:
                r = new Report(6640);
                r.subject = t.getId();
                vDesc.add(r);
                t.lockTurret();
                break;
            case Tank.CRIT_WEAPON_DESTROYED: {
                r = new Report(6305);
                r.subject = t.getId();
                ArrayList<Mounted> weapons = new ArrayList<Mounted>();
                for (Mounted weap : t.getWeaponList()) {
                    if (weap.getLocation() == loc && !weap.isHit() && !weap.isDestroyed()) {
                        weapons.add(weap);
                    }
                }
                // sort weapons by BV
                Collections.sort(weapons, new WeaponComparator());
                int roll = Compute.d6();
                Mounted weapon;
                if (roll < 4) {
                    // defender should choose, we'll just use the lowest BV
                    // weapon
                    weapon = weapons.get(weapons.size() - 1);
                } else {
                    // attacker chooses, we'll use the highest BV weapon
                    weapon = weapons.get(0);
                }
                weapon.setHit(true);
                r.add(weapon.getName());
                vDesc.add(r);
                // explosive weapons e.g. gauss now explode
                vDesc.addAll(explodeEquipment(t, loc, weapon));
                weapon.setDestroyed(true);
                break;
            }
            case Tank.CRIT_WEAPON_JAM: {
                r = new Report(6645);
                r.subject = t.getId();
                ArrayList<Mounted> weapons = new ArrayList<Mounted>();
                for (Mounted weap : t.getWeaponList()) {
                    if (weap.getLocation() == loc && !weap.isJammed() && !weap.isHit() && !weap.isDestroyed()) {
                        weapons.add(weap);
                    }
                }
                Mounted weapon = weapons.get(Compute.randomInt(weapons.size()));
                weapon.setJammed(true);
                t.addJammedWeapon(weapon);
                r.add(weapon.getName());
                vDesc.add(r);
                break;
            }
            case VTOL.CRIT_PILOT:
                r = new Report(6650);
                r.subject = t.getId();
                vDesc.add(r);
                t.setDriverHit(true);
                PilotingRollData psr = t.getBasePilotingRoll();
                psr.addModifier(0, "pilot injury");
                if (!doSkillCheckInPlace(t, psr)) {
                    r = new Report(6675);
                    r.subject = t.getId();
                    r.addDesc(t);
                    vDesc.add(r);
                    boolean crash = true;
                    if (t.canGoDown()) {
                        t.setElevation(t.getElevation() - 1);
                        crash = !t.canGoDown();
                    }
                    if (crash) {
                        vDesc.addAll(crashVTOLorWiGE(t));
                    }
                }
                break;
            case VTOL.CRIT_COPILOT:
                r = new Report(6655);
                r.subject = t.getId();
                vDesc.add(r);
                t.setCommanderHit(true);
                break;
            case VTOL.CRIT_ROTOR_DAMAGE: {
                r = new Report(6660);
                r.subject = t.getId();
                vDesc.add(r);
                int mp = t.getOriginalWalkMP();
                if (mp > 1)
                    t.setOriginalWalkMP(mp - 1);
                else if (mp == 1) {
                    t.setOriginalWalkMP(0);
                    vDesc.addAll(crashVTOLorWiGE(t));
                }
                break;
            }
            case VTOL.CRIT_ROTOR_DESTROYED:
                r = new Report(6670);
                r.subject = t.getId();
                vDesc.add(r);
                t.immobilize();
                t.destroyLocation(VTOL.LOC_ROTOR);
                vDesc.addAll(crashVTOLorWiGE(t));
                break;
            case VTOL.CRIT_FLIGHT_STABILIZER:
                r = new Report(6665);
                r.subject = t.getId();
                vDesc.add(r);
                t.setStabiliserHit(VTOL.LOC_ROTOR);
                break;
            }
        } else if (en instanceof Aero) {
            Aero a = (Aero) en;

            Jumpship js = new Jumpship();
            if (en instanceof Jumpship) {
                js = (Jumpship) en;
            } else {
                js = null;
            }

            switch (cs.getIndex()) {
            case Aero.CRIT_NONE:
                // no effect
                r = new Report(6005);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                break;
            case Aero.CRIT_FCS:
                // Fire control system
                r = new Report(9105);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setFCSHits(a.getFCSHits() + 1);
                break;
            case Aero.CRIT_SENSOR:
                // sensors
                r = new Report(6620);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setSensorHits(a.getSensorHits() + 1);
                break;
            case Aero.CRIT_AVIONICS:
                // avionics
                r = new Report(9110);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setAvionicsHits(a.getAvionicsHits() + 1);
                game.addControlRoll(new PilotingRollData(a.getId(), 0, "critical hit"));
                break;
            case Aero.CRIT_CONTROL:
                // force control roll
                r = new Report(9115);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                game.addControlRoll(new PilotingRollData(a.getId(), 0, "critical hit"));
                break;
            case Aero.CRIT_FUEL_TANK:
                // fuel tank
                r = new Report(9120);
                r.subject = a.getId();
                r.newlines = 0;
                // check for possible explosion
                int fuelroll = Compute.d6(2);
                if (fuelroll > 9) {
                    r.choose(true);
                    vDesc.add(r);
                    vDesc.addAll(destroyEntity(a, "fuel explosion", false, false));
                } else {
                    r.choose(false);
                    vDesc.add(r);
                }
                break;
            case Aero.CRIT_CREW:
                // pilot hit
                r = new Report(6650);
                if (a instanceof SmallCraft || a instanceof Jumpship)
                    r = new Report(9197);
                r.subject = a.getId();
                r.newlines = 1;
                vDesc.add(r);
                vDesc.addAll(damageCrew(a, 1));
                // The pilot may have just expired.
                if ((a.crew.isDead() || a.crew.isDoomed()) && !a.crew.isEjected()) {
                    vDesc.addAll(destroyEntity(a, "pilot death", true, true));
                }
                break;
            case Aero.CRIT_GEAR:
                // landing gear
                r = new Report(9125);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setGearHit(true);
                break;
            case Aero.CRIT_BOMB:
                // bomb destroyed
                // go through bomb list and choose one
                ArrayList<Integer> bombList = new ArrayList<Integer>();
                int[] bombs = a.getBombChoices();
                for (int i = 0; i < Aero.BOMB_NUM; i++) {
                    for (int j = 0; j < bombs[i]; j++) {
                        bombList.add(i);
                    }
                }
                if (bombList.size() > 0) {
                    int bombtype = bombList.get(Compute.randomInt(bombList.size()));
                    a.critBombs(bombtype);
                    r = new Report(9130);
                    r.subject = a.getId();
                    r.newlines = 0;
                    r.add(Aero.bombNames[bombtype]);
                    vDesc.add(r);
                } else {
                    r = new Report(9131);
                    r.subject = a.getId();
                    r.newlines = 0;
                    vDesc.add(r);
                }
                break;
            case Aero.CRIT_HEATSINK:
                // heat sink hit
                int sinksLost = 1;
                if (isCapital) {
                    sinksLost = 10;
                }
                r = new Report(9135);
                r.subject = a.getId();
                r.newlines = 0;
                r.add(sinksLost);
                vDesc.add(r);
                a.setHeatSinks(Math.max(0, a.getHeatSinks() - sinksLost));
                break;
            case Aero.CRIT_WEAPON:
                r = new Report(9150);
                r.subject = a.getId();
                r.newlines = 0;
                ArrayList<Mounted> weapons = new ArrayList<Mounted>();
                for (Mounted weap : a.getWeaponList()) {
                    if (weap.getLocation() == loc && !weap.isDestroyed() && !weap.isBombMounted() && !weap.getType().getInternalName().equals(Aero.SPACE_BOMB_ATTACK)) {
                        weapons.add(weap);
                    }
                }
                // this is kind of hack but I don't know why null isn't working
                if (weapons.size() > 0) {
                    Mounted weapon = weapons.get(Compute.randomInt(weapons.size()));
                    // possibly check for an ammo explosion
                    // don't allow ammo explosions on fighter squadrons
                    if (game.getOptions().booleanOption("ammo_explosions") && !(a instanceof FighterSquadron)) {
                        // does it use Ammo?
                        WeaponType wtype = (WeaponType) weapon.getType();
                        if (wtype.getAmmoType() != AmmoType.T_NA) {
                            Mounted m = weapon.getLinked();
                            int ammoroll = Compute.d6(2);
                            if (ammoroll >= 10) {
                                r = new Report(9151);
                                r.subject = a.getId();
                                r.add(m.getName());
                                r.newlines = 0;
                                vDesc.add(r);
                                vDesc.addAll(explodeEquipment(a, loc, m));
                                break;
                            }
                        }
                    }
                    weapon.setHit(true);
                    r.add(weapon.getName());
                    vDesc.add(r);
                    // explosive weapons e.g. gauss now explode
                    vDesc.addAll(explodeEquipment(a, loc, weapon));
                    weapon.setDestroyed(true);
                } else {
                    r = new Report(9155);
                    r.subject = a.getId();
                    r.newlines = 0;
                    vDesc.add(r);
                }
                break;
            case Aero.CRIT_ENGINE:
                // engine hit
                r = new Report(9140);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.engineHitsThisRound++;
                boolean engineExploded = checkEngineExplosion(a, vDesc, 1);
                if ((a.getEngineHits() + 1) < a.getMaxEngineHits() && !engineExploded) {
                    a.setEngineHits(a.getEngineHits() + 1);
                    if (a instanceof SmallCraft || a instanceof Jumpship) {
                        a.setOriginalWalkMP(Math.max(0, a.getOriginalWalkMP() - 1));
                    } else {
                        a.setOriginalWalkMP(Math.max(0, a.getOriginalWalkMP() - 2));
                    }
                } else {
                    // this engine hit puts the ASF out of commission
                    vDesc.addAll(destroyEntity(a, "engine destruction", true, true));
                }
                break;
            case Aero.CRIT_LEFT_THRUSTER:
                // thruster hit
                r = new Report(9160);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setLeftThrustHits(a.getLeftThrustHits() + 1);
                break;
            case Aero.CRIT_RIGHT_THRUSTER:
                // thruster hit
                r = new Report(9160);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                a.setRightThrustHits(a.getRightThrustHits() + 1);
                break;
            case Aero.CRIT_CARGO:
                // cargo hit
                // First what percentage of the cargo did the hit destroy?
                double percentDestroyed = 0.0;
                if (damageCaused > 0)
                    percentDestroyed = Math.min(damageCaused / (2.0 * a.getSI()), 1.0);
                // did it hit cargo or units
                int roll = Compute.d6(1);
                if (roll < 4) {
                    // cargo was hit
                    // just report; no game effect
                    r = new Report(9165);
                    r.subject = a.getId();
                    r.newlines = 0;
                    r.add((int) (percentDestroyed * 100));
                    vDesc.add(r);
                } else {
                    // units were hit
                    // get a list of units
                    Vector<Entity> passengers = en.getBayLoadedUnits();
                    int unitsDestroyed = (int) Math.ceil(percentDestroyed * passengers.size());
                    r = new Report(9166);
                    r.subject = a.getId();
                    r.newlines = 0;
                    r.add(unitsDestroyed);
                    vDesc.add(r);
                    while (unitsDestroyed > 0) {
                        // redraw loaded units to make sure I don't get ones
                        // already destroyed
                        Vector<Entity> units = en.getLoadedUnits();
                        if (units.size() > 0) {
                            Entity target = units.get(Compute.randomInt(units.size()));
                            vDesc.addAll(destroyEntity(target, "cargo damage", false, false));
                        }
                        unitsDestroyed--;
                    }
                }
                break;
            case Aero.CRIT_DOOR:
                // door hit
                // choose a random bay
                String bayType = en.damageBayDoor();
                if (!bayType.equals("none")) {
                    r = new Report(9170);
                    r.subject = a.getId();
                    r.add(bayType);
                    r.newlines = 0;
                    vDesc.add(r);
                } else {
                    r = new Report(9171);
                    r.subject = a.getId();
                    r.newlines = 0;
                    vDesc.add(r);
                }
                break;
            case Aero.CRIT_DOCK_COLLAR:
                // docking collar hit
                // different effect for dropships and jumpships
                if (en instanceof Dropship) {
                    Dropship ds = (Dropship) en;
                    ds.setDamageDockCollar(true);
                    r = new Report(9175);
                    r.subject = a.getId();
                    r.newlines = 0;
                    vDesc.add(r);
                }
                if (en instanceof Jumpship) {
                    // damage the docking collar
                    if (en.damageDockCollar()) {
                        r = new Report(9176);
                        r.subject = a.getId();
                        r.newlines = 0;
                        vDesc.add(r);
                    } else {
                        r = new Report(9177);
                        r.subject = a.getId();
                        r.newlines = 0;
                        vDesc.add(r);
                    }
                }
                break;
            case Aero.CRIT_KF_BOOM:
                // KF boom hit
                // no real effect yet
                r = new Report(9180);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                break;
            case Aero.CRIT_CIC:
                if (js == null) {
                    break;
                }
                // CIC hit
                r = new Report(9185);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                js.setCICHits(js.getCICHits() + 1);
                break;
            case Aero.CRIT_KF_DRIVE:
                if (js == null) {
                    break;
                }
                // KF Drive hit
                r = new Report(9190);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                js.setKFIntegrity(js.getKFIntegrity() - 1);
                break;
            case Aero.CRIT_GRAV_DECK:
                if (js == null) {
                    break;
                }
                // Grave Deck hit
                r = new Report(9195);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                break;
            case Aero.CRIT_LIFE_SUPPORT:
                // Life Support hit
                a.setLifeSupport(false);
                r = new Report(9196);
                r.subject = a.getId();
                r.newlines = 0;
                vDesc.add(r);
                break;
            }
        } else if (en instanceof BattleArmor) {
            // We might as well handle this here.
            // However, we're considering a crit against BA as a "crew kill".
            BattleArmor ba = (BattleArmor) en;
            r = new Report(6111);
            int randomTrooper = ba.getRandomTrooper();
            ba.destroyLocation(randomTrooper);
            r.add(randomTrooper);
            r.newlines = 1;
            vDesc.add(r);
        } else if (CriticalSlot.TYPE_SYSTEM == cs.getType()) {
            // Handle critical hits on system slots.
            cs.setHit(true);
            if (en instanceof Protomech) {
                int numHit = ((Protomech) en).getCritsHit(loc);
                if (cs.getIndex() != Protomech.SYSTEM_TORSO_WEAPON_A && cs.getIndex() != Protomech.SYSTEM_TORSO_WEAPON_B) {
                    r = new Report(6225);
                    r.subject = en.getId();
                    r.indent(3);
                    r.newlines = 0;
                    r.add(Protomech.systemNames[cs.getIndex()]);
                    vDesc.addElement(r);
                }
                switch (cs.getIndex()) {
                case Protomech.SYSTEM_HEADCRIT:
                    if (2 == numHit) {
                        r = new Report(6230);
                        r.subject = en.getId();
                        r.newlines = 0;
                        vDesc.addElement(r);
                        en.destroyLocation(loc);
                    }
                    break;
                case Protomech.SYSTEM_ARMCRIT:
                    if (2 == numHit) {
                        r = new Report(6235);
                        r.subject = en.getId();
                        r.newlines = 0;
                        vDesc.addElement(r);
                        en.destroyLocation(loc);
                    }
                    break;
                case Protomech.SYSTEM_LEGCRIT:
                    if (3 == numHit) {
                        r = new Report(6240);
                        r.subject = en.getId();
                        r.newlines = 0;
                        vDesc.addElement(r);
                        en.destroyLocation(loc);
                    }
                    break;
                case Protomech.SYSTEM_TORSOCRIT:
                    if (3 == numHit) {
                        vDesc.addAll(destroyEntity(en, "torso destruction"));
                    }
                    // Torso weapon hits are secondary effects and
                    // do not occur when loading from a scenario.
                    else if (secondaryEffects) {
                        int tweapRoll = Compute.d6(1);
                        CriticalSlot newSlot = null;
                        switch (tweapRoll) {
                        case 1:
                        case 2:
                            newSlot = new CriticalSlot(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSO_WEAPON_A);
                            vDesc.addAll(applyCriticalHit(en, Entity.NONE, newSlot, secondaryEffects));
                            break;
                        case 3:
                        case 4:
                            newSlot = new CriticalSlot(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSO_WEAPON_B);
                            vDesc.addAll(applyCriticalHit(en, Entity.NONE, newSlot, secondaryEffects));
                            break;
                        case 5:
                        case 6:
                            // no effect
                        }
                    }
                    break;
                case Protomech.SYSTEM_TORSO_WEAPON_A:
                    Mounted weaponA = ((Protomech) en).getTorsoWeapon(true);
                    if (null != weaponA) {
                        weaponA.setHit(true);
                        r = new Report(6245);
                        r.subject = en.getId();
                        r.newlines = 0;
                        vDesc.addElement(r);
                    }
                    break;
                case Protomech.SYSTEM_TORSO_WEAPON_B:
                    Mounted weaponB = ((Protomech) en).getTorsoWeapon(false);
                    if (null != weaponB) {
                        weaponB.setHit(true);
                        r = new Report(6250);
                        r.subject = en.getId();
                        r.newlines = 0;
                        vDesc.addElement(r);
                    }
                    break;

                } // End switch( cs.getType() )

                // Shaded hits cause pilot damage.
                if (((Protomech) en).shaded(loc, numHit)) {
                    // Destroyed Protomech sections have
                    // already damaged the pilot.
                    int pHits = Protomech.POSSIBLE_PILOT_DAMAGE[loc] - ((Protomech) en).getPilotDamageTaken(loc);
                    if (Math.min(1, pHits) > 0) {
                        Report.addNewline(vDesc);
                        vDesc.addAll(damageCrew(en, 1));
                        pHits = 1 + ((Protomech) en).getPilotDamageTaken(loc);
                        ((Protomech) en).setPilotDamageTaken(loc, pHits);
                    }
                } // End have-shaded-hit

            } // End entity-is-protomech
            else {
                r = new Report(6225);
                r.subject = en.getId();
                r.indent(3);
                r.add(((Mech) en).getSystemName(cs.getIndex()));
                r.newlines = 0;
                vDesc.addElement(r);
                switch (cs.getIndex()) {
                case Mech.SYSTEM_COCKPIT:
                    // Don't kill a pilot multiple times.
                    if (Pilot.DEATH > en.getCrew().getHits()) {
                        // boink!
                        en.getCrew().setDoomed(true);
                        Report.addNewline(vDesc);
                        vDesc.addAll(destroyEntity(en, "pilot death", true));
                    }
                    break;
                case Mech.SYSTEM_ENGINE:
                    // if the slot is missing, the location was previously
                    // destroyedd and the enginehit was then counted already
                    if (!cs.isMissing()) {
                        en.engineHitsThisRound++;
                    }
                    boolean engineExploded = false;

                    int numEngineHits = 0;
                    numEngineHits += en.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_CT);
                    numEngineHits += en.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_RT);
                    numEngineHits += en.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_LT);

                    engineExploded = checkEngineExplosion(en, vDesc, numEngineHits);
                    if (!engineExploded && numEngineHits > 2) {
                        // third engine hit
                        vDesc.addAll(destroyEntity(en, "engine destruction"));
                        if (game.getOptions().booleanOption("auto_abandon_unit")) {
                            vDesc.addAll(abandonEntity(en));
                        }
                    }
                    break;
                case Mech.SYSTEM_GYRO:
                    if (en.getGyroType() != Mech.GYRO_HEAVY_DUTY) {
                        int gyroHits = en.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_GYRO, loc);
                        // check for < 3 to make sure only one PSR gets
                        // scheduled,
                        // otherwise the avoid pilot damage to-hit is too high
                        // pilot value at 3 so we end up at a total of +6
                        if (gyroHits > 1 && gyroHits < 3) {
                            // gyro destroyed
                            game.addPSR(new PilotingRollData(en.getId(), TargetRoll.AUTOMATIC_FAIL, 3, "gyro destroyed"));
                        } else {
                            // first gyro hit
                            game.addPSR(new PilotingRollData(en.getId(), 3, "gyro hit"));
                        }// No check against HD Gyros.
                    } else {
                        int gyroHits = en.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_GYRO, loc);
                        // check for < 4 to make sure only one PSR gets
                        // scheduled,
                        // otherwise the avoid pilot damage to-hit is too high
                        // pilot value at 3 so we end up at a total of +6
                        if (gyroHits > 2 && gyroHits < 4) {
                            // gyro destroyed
                            game.addPSR(new PilotingRollData(en.getId(), TargetRoll.AUTOMATIC_FAIL, 1, "gyro destroyed"));
                        } else if (gyroHits == 1) {
                            // first gyro hit
                            game.addPSR(new PilotingRollData(en.getId(), 2, "gyro hit"));
                        } else {
                            // second gyro hit
                            game.addPSR(new PilotingRollData(en.getId(), 3, "gyro hit"));
                        }
                    }
                    break;
                case Mech.ACTUATOR_UPPER_LEG:
                case Mech.ACTUATOR_LOWER_LEG:
                case Mech.ACTUATOR_FOOT:
                    // leg/foot actuator piloting roll
                    game.addPSR(new PilotingRollData(en.getId(), 1, "leg/foot actuator hit"));
                    break;
                case Mech.ACTUATOR_HIP:
                    // hip piloting roll
                    game.addPSR(new PilotingRollData(en.getId(), 2, "hip actuator hit"));
                    break;
                }

            } // End entity-is-mek

        } // End crit-on-system-slot

        // Handle critical hits on equipment slots.
        else if (CriticalSlot.TYPE_EQUIPMENT == cs.getType()) {
            cs.setHit(true);
            Mounted mounted = en.getEquipment(cs.getIndex());
            EquipmentType eqType = mounted.getType();
            boolean hitBefore = mounted.isHit();

            r = new Report(6225);
            r.subject = en.getId();
            r.indent(3);
            r.add(mounted.getDesc());
            r.newlines = 0;
            vDesc.addElement(r);

            // Shield objects are not useless when they take one crit.
            // Shields can be critted and still be usable.
            if (eqType instanceof MiscType && ((MiscType) eqType).isShield())
                mounted.setHit(false);
            else
                mounted.setHit(true);

            if (eqType instanceof MiscType && eqType.hasFlag(MiscType.F_HARJEL)) {
                r = new Report(6254);
                r.subject = en.getId();
                r.indent(2);
                vDesc.add(r);
                vDesc.addAll(breachLocation(en, loc, null, true));
            }

            // If the item is the ECM suite of a Mek Stealth system
            // then it's destruction turns off the stealth.
            if (!hitBefore && eqType instanceof MiscType && eqType.hasFlag(MiscType.F_ECM) && mounted.getLinkedBy() != null) {
                Mounted stealth = mounted.getLinkedBy();
                r = new Report(6255);
                r.subject = en.getId();
                r.indent(2);
                r.add(stealth.getType().getName());
                r.newlines = 0;
                vDesc.addElement(r);
                stealth.setMode("Off");
            }

            // Handle equipment explosions.
            // Equipment explosions are secondary effects and
            // do not occur when loading from a scenario.
            if ((secondaryEffects && eqType.isExplosive() || mounted.isHotLoaded() || mounted.hasChargedCapacitor()) && !hitBefore) {
                vDesc.addAll(explodeEquipment(en, loc, mounted));
            }

            // Make sure that ammo in this slot is exhaused.
            if (mounted.getShotsLeft() > 0) {
                mounted.setShotsLeft(0);
            }

        } // End crit-on-equipment-slot
        // mechs with TSM hit by anti-tsm missiles this round get another crit
        if (en instanceof Mech && en.hitThisRoundByAntiTSM) {
            Mech mech = (Mech) en;
            if (mech.hasTSM()) {
                r = new Report(6430);
                r.subject = en.getId();
                r.indent(2);
                r.addDesc(en);
                r.newlines = 0;
                vDesc.addElement(r);
                vDesc.addAll(oneCriticalEntity(en, Compute.d6(2)));
            }
            en.hitThisRoundByAntiTSM = false;
        }

        // if using buffered VDNI then a possible pilot hit
        if (en.crew.getOptions().booleanOption("bvdni") && !en.crew.getOptions().booleanOption("pain_shunt")) {
            Report.addNewline(vDesc);
            int roll = Compute.d6(2);
            r = new Report(3580);
            r.subject = en.getId();
            r.addDesc(en);
            r.add(7);
            r.add(roll);
            r.choose(roll >= 8);
            r.indent(2);
            vDesc.add(r);
            if (roll >= 7) {
                vDesc.addAll(damageCrew(en, 1));
            }
        }

        // Return the results of the damage.
        return vDesc;
    }

    /**
     * Rolls and resolves critical hits with a die roll modifier.
     */

    public Vector<Report> criticalEntity(Entity en, int loc, int critMod) {
        return criticalEntity(en, loc, critMod, true, false);
    }

    /**
     * Rolls one critical hit
     */
    private Vector<Report> oneCriticalEntity(Entity en, int loc) {
        return criticalEntity(en, loc, 0, false, false);
    }

    /**
     * Crash a VTOL
     * 
     * @param en
     *            the <code>VTOL</code> to be crashed
     * @return the <code>Vector<Report></code> containg phasereports
     */
    private Vector<Report> crashVTOLorWiGE(Tank en) {
        return crashVTOLorWiGE(en, false, 0, en.getPosition(), en.getElevation(), 0);
    }

    /**
     * Crash a VTOL
     * 
     * @param en
     *            The <code>VTOL</code> to crash
     * @param sideSlipCrash
     *            A <code>boolean</code> value indicating wether this is a
     *            sideslip crash or not.
     * @param hexesMoved
     *            The <code>int</code> number of hexes moved.
     * @param crashPos
     *            The <code>Coords</code> of the crash
     * @param crashElevation
     *            The <code>int</code> elevation of the VTOL
     * @param impactSide
     *            The <code>int</code> describing the side on which the VTOL
     *            falls
     * @return a <code>Vector<Report></code> of Reports.
     */
    private Vector<Report> crashVTOLorWiGE(Tank en, boolean sideSlipCrash, int hexesMoved, Coords crashPos, int crashElevation, int impactSide) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        if (!sideSlipCrash) {
            // report lost movement and crashing
            r = new Report(6260);
            r.subject = en.getId();
            r.newlines = 0;
            r.addDesc(en);
            vDesc.addElement(r);
            int newElevation = 0;
            IHex fallHex = game.getBoard().getHex(crashPos);

            // May land on roof of building or bridge
            if (fallHex.containsTerrain(Terrains.BLDG_ELEV))
                newElevation = fallHex.terrainLevel(Terrains.BLDG_ELEV);
            else if (fallHex.containsTerrain(Terrains.BRIDGE_ELEV)) {
                newElevation = fallHex.terrainLevel(Terrains.BRIDGE_ELEV);
                if (newElevation > crashElevation)
                    newElevation = 0; // vtol was under bridge already
            }

            int fall = crashElevation - newElevation;
            if (fall == 0) {
                // already on ground, no harm done
                r = new Report(6265);
                r.subject = en.getId();
                vDesc.addElement(r);
                return vDesc;
            }
            // set elevation 1st to avoid multiple crashes
            en.setElevation(newElevation);

            // plummets to ground
            r = new Report(6270);
            r.subject = en.getId();
            r.add(fall);
            vDesc.addElement(r);

            // facing after fall
            String side;
            int table;
            int facing = Compute.d6();
            switch (facing) {
            case 1:
            case 2:
                side = "right side";
                table = ToHitData.SIDE_RIGHT;
                break;
            case 3:
                side = "rear";
                table = ToHitData.SIDE_REAR;
                break;
            case 4:
            case 5:
                side = "left side";
                table = ToHitData.SIDE_LEFT;
                break;
            case 0:
            default:
                side = "front";
                table = ToHitData.SIDE_FRONT;
            }

            if (newElevation <= 0) {
                boolean waterFall = fallHex.containsTerrain(Terrains.WATER);
                if (waterFall && fallHex.containsTerrain(Terrains.ICE)) {
                    int roll = Compute.d6(1);
                    r = new Report(2119);
                    r.subject = en.getId();
                    r.addDesc(en);
                    r.add(roll);
                    r.subject = en.getId();
                    vDesc.add(r);
                    if (roll > 3) {
                        vDesc.addAll(resolveIceBroken(crashPos));
                    } else {
                        waterFall = false; // saved by ice
                    }
                }
                if (waterFall) {
                    // falls into water and is destroyed
                    r = new Report(6275);
                    r.subject = en.getId();
                    vDesc.addElement(r);
                    vDesc.addAll(destroyEntity(en, "Fell into water", false, false));
                    // not sure, is this salvagable?
                }
            }

            // calculate damage for hitting the surface
            int damage = (int) Math.round(en.getWeight() / 10.0) * (fall + 1);

            // adjust damage for gravity
            damage = Math.round(damage * game.getPlanetaryConditions().getGravity());
            // report falling
            r = new Report(6280);
            r.subject = en.getId();
            r.indent();
            r.addDesc(en);
            r.add(side);
            r.add(damage);
            r.newlines = 0;
            vDesc.addElement(r);

            en.setFacing((en.getFacing() + (facing - 1)) % 6);

            boolean exploded = false;

            // standard damage loop
            while (damage > 0) {
                int cluster = Math.min(5, damage);
                HitData hit = en.rollHitLocation(ToHitData.HIT_NORMAL, table);
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                int ISBefore[] = { en.getInternal(Tank.LOC_FRONT), en.getInternal(Tank.LOC_RIGHT), en.getInternal(Tank.LOC_LEFT), en.getInternal(Tank.LOC_REAR) };// hack?
                vDesc.addAll(damageEntity(en, hit, cluster));
                int ISAfter[] = { en.getInternal(Tank.LOC_FRONT), en.getInternal(Tank.LOC_RIGHT), en.getInternal(Tank.LOC_LEFT), en.getInternal(Tank.LOC_REAR) };
                for (int x = 0; x <= 3; x++) {
                    if (ISBefore[x] != ISAfter[x]) {
                        exploded = true;
                    }
                }
                damage -= cluster;
            }
            if (exploded) {
                r = new Report(6285);
                r.subject = en.getId();
                r.addDesc(en);
                vDesc.addElement(r);
                vDesc.addAll(explodeVTOLorWiGE(en));
            }

            // check for location exposure
            vDesc.addAll(doSetLocationsExposure(en, fallHex, false, newElevation));

        } else {
            en.setElevation(0);// considered landed in the hex.
            // crashes into ground thanks to sideslip
            r = new Report(6290);
            r.subject = en.getId();
            r.addDesc(en);
            vDesc.addElement(r);
            int damage = (int) Math.round(en.getWeight() / 10.0) * (hexesMoved + 1);
            boolean exploded = false;

            // standard damage loop
            while (damage > 0) {
                int cluster = Math.min(5, damage);
                HitData hit = en.rollHitLocation(ToHitData.HIT_NORMAL, impactSide);
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                int ISBefore[] = { en.getInternal(Tank.LOC_FRONT), en.getInternal(Tank.LOC_RIGHT), en.getInternal(Tank.LOC_LEFT), en.getInternal(Tank.LOC_REAR) };// hack?
                vDesc.addAll(damageEntity(en, hit, cluster));
                int ISAfter[] = { en.getInternal(Tank.LOC_FRONT), en.getInternal(Tank.LOC_RIGHT), en.getInternal(Tank.LOC_LEFT), en.getInternal(Tank.LOC_REAR) };
                for (int x = 0; x <= 3; x++) {
                    if (ISBefore[x] != ISAfter[x]) {
                        exploded = true;
                    }
                }
                damage -= cluster;
            }
            if (exploded) {
                r = new Report(6295);
                r.subject = en.getId();
                r.addDesc(en);
                vDesc.addElement(r);
                vDesc.addAll(explodeVTOLorWiGE(en));
            }

        }
        
        if(game.containsMinefield(crashPos)) {
            //may set off any minefields in the hex
            enterMinefield(en, crashPos, 0, true, vDesc, 7);
            //it may also clear any minefields that it detonated
            clearDetonatedMines(crashPos, 5); 
            resetMines();
        }
        
        return vDesc;

    }

    /**
     * Explode a VTOL
     * 
     * @param en
     *            The <code>VTOL</code> to explode.
     * @return a <code>Vector</code> of reports
     */
    private Vector<Report> explodeVTOLorWiGE(Tank en) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        if (en.getEngine().isFusion()) {
            // fusion engine, no effect
            r = new Report(6300);
            r.subject = en.getId();
            vDesc.addElement(r);
        } else {
            Coords pos = en.getPosition();
            IHex hex = game.getBoard().getHex(pos);
            if (hex.containsTerrain(Terrains.WOODS) || hex.containsTerrain(Terrains.JUNGLE)) {
                ignite(pos, false, vDesc);
            } else {
                ignite(pos, true, vDesc);
            }
            vDesc.addAll(destroyEntity(en, "crashed and burned", false, false));
        }

        return vDesc;
    }

    /**
     * rolls and resolves one tank critical hit
     * 
     * @param t
     *            the <code>Tank</code> to be critted
     * @param loc
     *            the <code>int</code> location of the Tank to be critted
     * @param critMod
     *            the <code>int</code> modifier to the critroll
     * @return a <code>Vector<Report></code> containing the phasereports
     */
    private Vector<Report> criticalTank(Tank t, int loc, int critMod) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // roll the critical
        r = new Report(6305);
        r.subject = t.getId();
        r.indent(2);
        r.add(t.getLocationAbbr(loc));
        r.newlines = 0;
        vDesc.add(r);
        int roll = Compute.d6(2);
        r = new Report(6310);
        r.subject = t.getId();
        String rollString = "";
        if (critMod != 0) {
            rollString = "(" + roll;
            if (critMod > 0) {
                rollString += "+";
            }
            rollString += critMod + ") = ";
            roll += critMod;
        }
        rollString += roll;
        r.add(rollString);
        r.newlines = 0;
        vDesc.add(r);

        // now look up on vehicle crits table
        int critType = t.getCriticalEffect(roll, loc);
        vDesc.addAll(applyCriticalHit(t, loc, new CriticalSlot(0, critType), true));
        return vDesc;
    }

    private Vector<Report> criticalAero(Aero a, int loc, int critMod, String reason, int target, int damage, boolean isCapital) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // roll the critical
        r = new Report(9100);
        r.subject = a.getId();
        r.add(reason);
        r.indent(3);
        r.newlines = 0;
        vDesc.add(r);
        int roll = Compute.d6(2);
        r = new Report(9101);
        r.subject = a.getId();
        r.add(target);
        String rollString = "";
        if (critMod != 0) {
            rollString = "(" + roll;
            if (critMod > 0) {
                rollString += "+";
            }
            rollString += critMod + ") = ";
            roll += critMod;
        }
        rollString += roll;
        r.add(rollString);
        r.newlines = 0;
        vDesc.add(r);

        // now look up on vehicle crits table
        int critType = a.getCriticalEffect(roll, target);
        vDesc.addAll(applyCriticalHit(a, loc, new CriticalSlot(0, critType), true, damage, isCapital));
        return vDesc;
    }

    /**
     * Rolls and resolves critical hits on mechs or vehicles. if rollNumber is
     * false, a single hit is applied - needed for MaxTech Heat Scale rule.
     */
    public Vector<Report> criticalEntity(Entity en, int loc, int critMod, boolean rollNumber, boolean isCapital) {
        
        if (en instanceof Tank)
            return criticalTank((Tank) en, loc, critMod);
        if (en instanceof Aero)
            return criticalAero((Aero) en, loc, critMod, "unknown", 8, -1, isCapital);
        CriticalSlot slot = null;
        Vector<Report> vDesc = new Vector<Report>();
        Report r;
        Coords coords = en.getPosition();
        IHex hex = null;
        int hits;
        if (rollNumber) {
            if (null != coords)
                hex = game.getBoard().getHex(coords);
            r = new Report(6305);
            r.subject = en.getId();
            r.indent(2);
            r.add(en.getLocationAbbr(loc));
            r.newlines = 0;
            vDesc.addElement(r);
            hits = 0;
            int roll = Compute.d6(2);
            r = new Report(6310);
            r.subject = en.getId();
            String rollString = "";
            if (critMod != 0) {
                rollString = "(" + roll;
                if (critMod > 0) {
                    rollString += "+";
                }
                rollString += critMod + ") = ";
                roll += critMod;
            }
            rollString += roll;
            r.add(rollString);
            r.newlines = 0;
            vDesc.addElement(r);
            if (roll <= 7) {
                // no effect
                r = new Report(6005);
                r.subject = en.getId();
                r.newlines = 0;
                vDesc.addElement(r);
                return vDesc;
            } else if (roll >= 8 && roll <= 9) {
                hits = 1;
                r = new Report(6315);
                r.subject = en.getId();
                r.newlines = 0;
                vDesc.addElement(r);
            } else if (roll >= 10 && roll <= 11) {
                hits = 2;
                r = new Report(6320);
                r.subject = en.getId();
                r.newlines = 0;
                vDesc.addElement(r);
            } else if (roll >= 12) {
                if (en instanceof Protomech) {
                    hits = 3;
                    r = new Report(6325);
                    r.subject = en.getId();
                    r.newlines = 0;
                    vDesc.addElement(r);
                } else if (en.locationIsLeg(loc)) {
                    // limb blown off
                    r = new Report(6120);
                    r.subject = en.getId();
                    r.add(en.getLocationName(loc));
                    r.newlines = 0;
                    vDesc.addElement(r);
                    if (en.getInternal(loc) > 0) {
                        en.destroyLocation(loc);
                    }
                    if (null != hex) {
                        if (!hex.containsTerrain(Terrains.LEGS)) {
                            hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.LEGS, 1));
                        } else {
                            hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.LEGS, hex.terrainLevel(Terrains.LEGS) + 1));
                        }
                    }
                    sendChangedHex(en.getPosition());
                    return vDesc;
                } else if (loc == Mech.LOC_RARM || loc == Mech.LOC_LARM) {
                    // limb blown off
                    r = new Report(6120);
                    r.subject = en.getId();
                    r.add(en.getLocationName(loc));
                    r.newlines = 0;
                    vDesc.addElement(r);
                    en.destroyLocation(loc);
                    if (null != hex) {
                        if (!hex.containsTerrain(Terrains.ARMS)) {
                            hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ARMS, 1));
                        } else {
                            hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ARMS, hex.terrainLevel(Terrains.ARMS) + 1));
                        }
                    }
                    sendChangedHex(en.getPosition());
                    return vDesc;
                } else if (loc == Mech.LOC_HEAD) {
                    // head blown off
                    r = new Report(6330);
                    r.subject = en.getId();
                    r.add(en.getLocationName(loc));
                    r.newlines = 0;
                    vDesc.addElement(r);
                    en.destroyLocation(loc);
                    if (((Mech) en).getCockpitType() != Mech.COCKPIT_TORSO_MOUNTED) {
                        // Don't kill a pilot multiple times.
                        if (Pilot.DEATH > en.getCrew().getHits()) {
                            en.crew.setDoomed(true);
                            Report.addNewline(vDesc);
                            vDesc.addAll(destroyEntity(en, "pilot death", true));
                        }
                    }
                    return vDesc;
                } else {
                    // torso hit
                    hits = 3;
                    r = new Report(6325);
                    r.subject = en.getId();
                    r.newlines = 0;
                    vDesc.addElement(r);
                }
            }
        } else {
            hits = 1;
        }

        // transfer criticals, if needed
        while (hits > 0 && en.canTransferCriticals(loc) && en.getTransferLocation(loc) != Entity.LOC_DESTROYED && en.getTransferLocation(loc) != Entity.LOC_NONE) {
            loc = en.getTransferLocation(loc);
            r = new Report(6335);
            r.subject = en.getId();
            r.indent(3);
            r.add(en.getLocationAbbr(loc));
            r.newlines = 0;
            vDesc.addElement(r);
        }

        // Roll critical hits in this location.
        while (hits > 0) {

            // Have we hit all available slots in this location?
            if (en.getHittableCriticals(loc) <= 0) {
                r = new Report(6340);
                r.subject = en.getId();
                r.indent(3);
                r.newlines = 0;
                vDesc.addElement(r);
                break;
            }

            // Randomly pick a slot to be hit.
            int slotIndex = Compute.randomInt(en.getNumberOfCriticals(loc));
            slot = en.getCritical(loc, slotIndex);

            // Ignore empty or unhitable slots (this
            // includes all previously hit slots).

            if (slot != null && slot.isHittable()) {
                // if explosive use edge
                if (en instanceof Mech && (en.crew.hasEdgeRemaining() && en.crew.getOptions().booleanOption("edge_when_explosion")) && slot.getType() == CriticalSlot.TYPE_EQUIPMENT && en.getEquipment(slot.getIndex()).getType().isExplosive()) {
                    en.crew.decreaseEdge();
                    r = new Report(6530);
                    r.subject = en.getId();
                    r.indent(3);
                    r.newlines = 0;
                    r.add(en.crew.getOptions().intOption("edge"));
                    vDesc.addElement(r);
                    continue;
                }

                // check for reactive armor
                if (en.getArmorType() == EquipmentType.T_ARMOR_REACTIVE) {
                    Mounted mount = en.getEquipment(slot.getIndex());
                    if (mount != null && mount.getType() instanceof MiscType && ((MiscType) mount.getType()).hasFlag(MiscType.F_REACTIVE)) {
                        int roll = Compute.d6(2);
                        r = new Report(6082);
                        r.subject = en.getId();
                        r.indent(3);
                        r.newlines = 0;
                        r.add(roll);
                        vDesc.addElement(r);
                        // big budda boom
                        if (roll == 2) {
                            r = new Report(6083);
                            r.subject = en.getId();
                            r.indent(3);
                            r.newlines = 0;
                            vDesc.addElement(r);
                            vDesc.addElement(r);
                            vDesc.addAll(damageEntity(en, new HitData(loc), en.getArmor(loc)));
                            if (en.hasRearArmor(loc))
                                vDesc.addAll(damageEntity(en, new HitData(loc, true), en.getArmor(loc, true)));
                            vDesc.addAll(damageEntity(en, new HitData(loc), 1));
                        } else
                            continue;
                    }
                }
                vDesc.addAll(applyCriticalHit(en, loc, slot, true));
                hits--;
            }

        } // Hit another slot in this location.

        return vDesc;
    }

    /**
     * Checks for location breach and returns phase logging. <p/> Please note
     * that dependent locations ARE NOT considered breached!
     * 
     * @param entity
     *            the <code>Entity</code> that needs to be checked.
     * @param loc
     *            the <code>int</code> location on the entity that needs to be
     *            checked for a breach.
     * @param target 
     *            the <code>int</code> target number for the breach
     * @param hex
     *            the <code>IHex</code> the enitity occupies when checking
     *            This value will be <code>null</code> if the check is the
     *            result of an attack, and non-null if it occurs during
     *            movement.
     */
    private Vector<Report> breachCheck(Entity entity, int loc, IHex hex) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // BattleArmor does not breach
        if (entity instanceof Infantry) {
            return vDesc;
        }

        if (entity instanceof VTOL) {
            return vDesc;
        }

        boolean dumping = false;
        for (Mounted m : entity.getAmmo()) {
            if (m.isDumping()) {
                // dumping ammo underwater is very stupid thing to do
                dumping = true;
                break;
            }
        }

        // This handles both water and vacuum breaches.
        if (entity.getLocationStatus(loc) > ILocationExposureStatus.NORMAL) {
            // Does the location have armor (check rear armor on Mek)
            // and is the check due to damage?
            int breachroll = 0;
            //set the target roll for the breach
            int target = 10;
            //if this is a vacuum check and we are in trace atmosphere then adjust target
            if(entity.getLocationStatus(loc) == ILocationExposureStatus.VACUUM && 
                    game.getPlanetaryConditions().getAtmosphere() == PlanetaryConditions.ATMO_TRACE) {
                target = 12;
            }
            if (entity.getArmor(loc) > 0 && (entity instanceof Mech ? entity.getArmor(loc, true) > 0 : true) && null == hex) {
                // functional HarJel prevents breach
                if (entity instanceof Mech && ((Mech) entity).hasHarJelIn(loc)) {
                    r = new Report(6342);
                    r.subject = entity.getId();
                    r.indent(3);
                    vDesc.addElement(r);
                    return vDesc;
                }
                breachroll = Compute.d6(2);
                r = new Report(6345);
                r.subject = entity.getId();
                r.indent(3);
                r.add(entity.getLocationAbbr(loc));
                r.add(breachroll);
                r.newlines = 0;
                if (breachroll >= target)
                    r.choose(false);
                else
                    r.choose(true);
                vDesc.addElement(r);
            }
            // Breach by damage or lack of armor.
            if (breachroll >= target || !(entity.getArmor(loc) > 0) || (dumping && (!(entity instanceof Mech) || loc == Mech.LOC_CT || loc == Mech.LOC_RT || loc == Mech.LOC_LT)) || !(entity instanceof Mech ? entity.getArmor(loc, true) > 0 : true)) {
                // functional HarJel prevents breach
                if (entity instanceof Mech && ((Mech) entity).hasHarJelIn(loc)) {
                    r = new Report(6342);
                    r.subject = entity.getId();
                    r.indent(3);
                    vDesc.addElement(r);
                    return vDesc;
                }
                vDesc.addAll(breachLocation(entity, loc, hex, false));
            }
        }
        return vDesc;
    }

    /**
     * Marks all equipment in a location on an entity as useless.
     * 
     * @param entity
     *            the <code>Entity</code> that needs to be checked.
     * @param loc
     *            the <code>int</code> location on the entity that needs to be
     *            checked for a breach.
     * @param hex
     *            the <code>IHex</code> the enitity occupies when checking
     *            This value will be <code>null</code> if the check is the
     *            result of an attack, and non-null if it occurs during
     *            movement.
     * @param harJel
     *            a <code>boolean</code> value indicating if the uselessness
     *            is the cause of a critically hit HarJel system
     */
    private Vector<Report> breachLocation(Entity entity, int loc, IHex hex, boolean harJel) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        if (entity.getInternal(loc) < 0 || entity.getLocationStatus(loc) < ILocationExposureStatus.NORMAL) {
            // already destroyed or breached? don't bother
            return vDesc;
        }

        r = new Report(6350);
        if (harJel)
            r.messageId = 6351;
        r.subject = entity.getId();
        r.add(entity.getShortName());
        r.add(entity.getLocationAbbr(loc));
        r.newlines = 0;
        vDesc.addElement(r);

        if (entity instanceof Tank) {
            vDesc.addAll(destroyEntity(entity, "hull breach", true, true));
            return vDesc;
        }
        // equipment and crits will be marked in applyDamage?

        // equipment marked missing
        for (Mounted mounted : entity.getEquipment()) {
            if (mounted.getLocation() == loc) {
                mounted.setBreached(true);
            }
        }
        // all critical slots set as useless
        for (int i = 0; i < entity.getNumberOfCriticals(loc); i++) {
            final CriticalSlot cs = entity.getCritical(loc, i);
            if (cs != null) {
                // for every undamaged actuator destroyed by breaching,
                // we make a PSR (see bug 1040858)
                if (entity.locationIsLeg(loc)) {
                    if (cs.isHittable()) {
                        switch (cs.getIndex()) {
                        case Mech.ACTUATOR_UPPER_LEG:
                        case Mech.ACTUATOR_LOWER_LEG:
                        case Mech.ACTUATOR_FOOT:
                            // leg/foot actuator piloting roll
                            game.addPSR(new PilotingRollData(entity.getId(), 1, "leg/foot actuator hit"));
                            break;
                        case Mech.ACTUATOR_HIP:
                            // hip piloting roll (at +0, because we get the
                            // +2
                            // anyway because the location is breached
                            // phase report will look a bit weird, but the
                            // roll
                            // is correct
                            game.addPSR(new PilotingRollData(entity.getId(), 0, "hip actuator hit"));
                            break;
                        }
                    }
                }
                cs.setBreached(true);
            }
        }

        // Check location for engine/cockpit breach and report accordingly
        if (loc == Mech.LOC_CT) {
            vDesc.addAll(destroyEntity(entity, "hull breach"));
            if (game.getOptions().booleanOption("auto_abandon_unit")) {
                vDesc.addAll(abandonEntity(entity));
            }
        }
        if (loc == Mech.LOC_HEAD) {
            entity.crew.setDoomed(true);
            vDesc.addAll(destroyEntity(entity, "hull breach"));
            if (entity.getLocationStatus(loc) == ILocationExposureStatus.WET) {
                r = new Report(6355);
                r.subject = entity.getId();
                r.addDesc(entity);
                vDesc.addElement(r);
            } else {
                r = new Report(6360);
                r.subject = entity.getId();
                r.addDesc(entity);
                vDesc.addElement(r);
            }
        }

        // Set the status of the location.
        // N.B. if we set the status before rolling water PSRs, we get a
        // "LEG DESTROYED" modifier; setting the status after gives a hip
        // actuator modifier.
        entity.setLocationStatus(loc, ILocationExposureStatus.BREACHED);

        // Did the hull breach destroy the engine?
        if (entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_LT) + entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_CT) + entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_ENGINE, Mech.LOC_RT) >= 3) {
            vDesc.addAll(destroyEntity(entity, "engine destruction"));
            if (game.getOptions().booleanOption("auto_abandon_unit")) {
                vDesc.addAll(abandonEntity(entity));
            }
        }

        return vDesc;
    }

    /**
     * Mark the unit as destroyed! Units transported in the destroyed unit will
     * get a chance to escape.
     * 
     * @param entity -
     *            the <code>Entity</code> that has been destroyed.
     * @param reason -
     *            a <code>String</code> detailing why the entity was
     *            destroyed.
     * @return a <code>Vector</code> of <code>Report</code> objects that can
     *         be sent to the output log.
     */
    private Vector<Report> destroyEntity(Entity entity, String reason) {
        return destroyEntity(entity, reason, true);
    }

    /**
     * Marks a unit as destroyed! Units transported inside the destroyed unit
     * will get a chance to escape unless the destruction was not survivable.
     * 
     * @param entity -
     *            the <code>Entity</code> that has been destroyed.
     * @param reason -
     *            a <code>String</code> detailing why the entity was
     *            destroyed.
     * @param survivable -
     *            a <code>boolean</code> that identifies the desctruction as
     *            unsurvivable for transported units.
     * @return a <code>Vector</code> of <code>Report</code> objects that can
     *         be sent to the output log.
     */
    private Vector<Report> destroyEntity(Entity entity, String reason, boolean survivable) {
        // Generally, the entity can still be salvaged.
        return destroyEntity(entity, reason, survivable, true);
    }

    /**
     * Marks a unit as destroyed! Units transported inside the destroyed unit
     * will get a chance to escape unless the destruction was not survivable.
     * 
     * @param entity -
     *            the <code>Entity</code> that has been destroyed.
     * @param reason -
     *            a <code>String</code> detailing why the entity was
     *            destroyed.
     * @param survivable -
     *            a <code>boolean</code> that identifies the desctruction as
     *            unsurvivable for transported units.
     * @param canSalvage -
     *            a <code>boolean</code> that indicates if the unit can be
     *            salvaged (or cannibalized for spare parts). If
     *            <code>true</code>, salvage operations are possible, if
     *            <code>false</code>, the unit is too badly damaged.
     * @return a <code>Vector</code> of <code>Report</code> objects that can
     *         be sent to the output log.
     */
    private Vector<Report> destroyEntity(Entity entity, String reason, boolean survivable, boolean canSalvage) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // The unit can suffer an ammo explosion after it has been destroyed.
        int condition = IEntityRemovalConditions.REMOVE_SALVAGEABLE;
        if (!canSalvage) {
            entity.setSalvage(canSalvage);
            condition = IEntityRemovalConditions.REMOVE_DEVASTATED;
        }

        // Destroy the entity, unless it's already destroyed.
        if (!entity.isDoomed() && !entity.isDestroyed()) {
            r = new Report(6365);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(reason);
            r.newlines = 0;
            vDesc.addElement(r);

            entity.setDoomed(true);

            // Kill any picked up MechWarriors
            Enumeration<Integer> iter = entity.getPickedUpMechWarriors().elements();
            while (iter.hasMoreElements()) {
                int mechWarriorId = iter.nextElement();
                Entity mw = game.getEntity(mechWarriorId);
                mw.setDestroyed(true);
                game.removeEntity(mw.getId(), condition);
                entityUpdate(mw.getId());
                send(createRemoveEntityPacket(mw.getId(), condition));
                r = new Report(6370);
                r.subject = mw.getId();
                r.addDesc(mw);
                vDesc.addElement(r);
            }

            // make any remaining tele-missiles operated by this entity
            // out of contact
            for (int missileId : entity.getTMTracker().getMissiles()) {
                Entity tm = game.getEntity(missileId);
                if (null != tm && !tm.isDestroyed() && tm instanceof TeleMissile) {
                    ((TeleMissile) tm).setOutContact(true);
                    entityUpdate(tm.getId());
                }
            }

            // Mechanized BA that could die on a 3+
            ArrayList<Entity> externalUnits = entity.getExternalUnits();

            // Handle escape of transported units.
            Enumeration<Entity> transporter = entity.getLoadedUnits().elements();
            if (transporter.hasMoreElements()) {
                Entity other = null;
                Coords curPos = entity.getPosition();
                IHex entityHex = game.getBoard().getHex(curPos);
                int curFacing = entity.getFacing();
                while (transporter.hasMoreElements()) {
                    other = transporter.nextElement();

                    // Can the other unit survive?
                    if (!survivable || (externalUnits.contains(other) && Compute.d6() >= 3)) {

                        // Nope.
                        other.setDestroyed(true);
                        game.moveToGraveyard(other.getId());
                        entityUpdate(other.getId());
                        send(createRemoveEntityPacket(other.getId(), condition));
                        r = new Report(6370);
                        r.subject = other.getId();
                        r.addDesc(other);
                        vDesc.addElement(r);
                    }
                    // Can we unload the unit to the current hex?
                    // TODO : unloading into stacking violation is not
                    // explicitly prohibited in the BMRr.
                    else if (null != Compute.stackingViolation(game, other.getId(), curPos) || other.isHexProhibited(entityHex)) {
                        // Nope.
                        other.setDestroyed(true);
                        game.moveToGraveyard(other.getId());
                        entityUpdate(other.getId());
                        send(createRemoveEntityPacket(other.getId(), condition));
                        r = new Report(6375);
                        r.subject = other.getId();
                        r.addDesc(other);
                        vDesc.addElement(r);
                    } // End can-not-unload
                    else {
                        // The other unit survives.
                        unloadUnit(entity, other, curPos, curFacing, entity.getElevation());
                    }

                } // Handle the next transported unit.

            } // End has-transported-unit

            // Handle transporting unit.
            if (Entity.NONE != entity.getTransportId()) {
                final Entity transport = game.getEntity(entity.getTransportId());
                Coords curPos = transport.getPosition();
                int curFacing = transport.getFacing();
                unloadUnit(transport, entity, curPos, curFacing, transport.getElevation());
                entityUpdate(transport.getId());
            } // End unit-is-transported

            // Is this unit being swarmed?
            final int swarmerId = entity.getSwarmAttackerId();
            if (Entity.NONE != swarmerId) {
                final Entity swarmer = game.getEntity(swarmerId);

                // remove the swarmer from the move queue
                game.removeTurnFor(swarmer);
                send(createTurnVectorPacket());

                swarmer.setSwarmTargetId(Entity.NONE);
                entity.setSwarmAttackerId(Entity.NONE);
                r = new Report(6380);
                r.subject = swarmerId;
                r.addDesc(swarmer);
                vDesc.addElement(r);
                entityUpdate(swarmerId);
            }

            // Is this unit swarming somebody?
            final int swarmedId = entity.getSwarmTargetId();
            if (Entity.NONE != swarmedId) {
                final Entity swarmed = game.getEntity(swarmedId);
                swarmed.setSwarmAttackerId(Entity.NONE);
                entity.setSwarmTargetId(Entity.NONE);
                r = new Report(6385);
                r.subject = swarmed.getId();
                r.addDesc(swarmed);
                vDesc.addElement(r);
                entityUpdate(swarmedId);
            }

            // If in a grapple, release both mechs
            if (entity.getGrappled() != Entity.NONE) {
                int grappler = entity.getGrappled();
                entity.setGrappled(Entity.NONE, false);
                Entity e = game.getEntity(grappler);
                if (e != null) {
                    e.setGrappled(Entity.NONE, false);
                }
                entityUpdate(grappler);
            }

        } // End entity-not-already-destroyed.

        //if using battlefield wreckage rules, then the destruction of this unit
        //might convert the hex to rough
        Coords curPos = entity.getPosition();
        IHex entityHex = game.getBoard().getHex(curPos);
        if(game.getOptions().booleanOption("tacops_battle_wreck")  
                && entityHex != null && game.getBoard().onGround()
                && !(entity instanceof Infantry || entity instanceof Protomech)) {
            //large support vees will create ultra rough, otherwise rough 
            if(entity instanceof LargeSupportTank) {
                if(entityHex.terrainLevel(Terrains.ROUGH) < 2) {
                    entityHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ROUGH, 2));
                    sendChangedHex(curPos);
                } 
            } else if ( entity.getWeight() >= 40 && !entityHex.containsTerrain(Terrains.ROUGH)) {
                    entityHex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.ROUGH, 1));
                       sendChangedHex(curPos);
            }
            
        }
        
        
        // update our entity, so clients have correct data
        // needed for MekWars stuff
        entityUpdate(entity.getId());

        return vDesc;
    }

    /**
     * Makes a piece of equipment on a mech explode! POW! This expects either
     * ammo, or an explosive weapon. Returns a vector of Report objects.
     */
    private Vector<Report> explodeEquipment(Entity en, int loc, int slot) {
        return explodeEquipment(en, loc, en.getEquipment(en.getCritical(loc, slot).getIndex()));
    }

    /**
     * Makes a piece of equipment on a mech explode! POW! This expects either
     * ammo, or an explosive weapon. Returns a vector of Report objects.
     */
    private Vector<Report> explodeEquipment(Entity en, int loc, Mounted mounted) {
        Vector<Report> vDesc = new Vector<Report>();
        // is this already destroyed?
        if (mounted.isDestroyed()) {
            System.err.println("server: explodeEquipment called on destroyed" + " equipment (" + mounted.getName() + ')');
            return vDesc;
        }

        // special-case. RACs only explode when jammed
        if (mounted.getType() instanceof WeaponType && ((WeaponType) mounted.getType()).getAmmoType() == AmmoType.T_AC_ROTARY) {
            if (!mounted.isJammed()) {
                return vDesc;
            }
        }

        // special case. ACs only explode when firing incendiary ammo
        if (mounted.getType() instanceof WeaponType && (((WeaponType) mounted.getType()).getAmmoType() == AmmoType.T_AC || ((WeaponType) mounted.getType()).getAmmoType() == AmmoType.T_LAC)) {
            if (!mounted.isUsedThisRound()) {
                return vDesc;
            }
            Mounted ammo = mounted.getLinked();
            if (ammo == null || !(ammo.getType() instanceof AmmoType) || ((AmmoType) ammo.getType()).getMunitionType() != AmmoType.M_INCENDIARY_AC) {
                return vDesc;
            }

            WeaponType wtype = (WeaponType) mounted.getType();
            if (wtype.getAmmoType() == AmmoType.T_LRM || wtype.getAmmoType() == AmmoType.T_LRM_STREAK || wtype.getAmmoType() == AmmoType.T_LRM_TORPEDO || wtype.getAmmoType() == AmmoType.T_LRM_TORPEDO_COMBO) {
                return vDesc;
            }

        }

        // Inferno ammo causes heat buildup as well as the damage
        if (mounted.getType() instanceof AmmoType && (((AmmoType) mounted.getType()).getAmmoType() == AmmoType.T_SRM || ((AmmoType) mounted.getType()).getAmmoType() == AmmoType.T_MML) && ((AmmoType) mounted.getType()).getMunitionType() == AmmoType.M_INFERNO && mounted.getShotsLeft() > 0) {
            en.heatBuildup += Math.min(mounted.getExplosionDamage(), 30);
        }

        // determine and deal damage
        int damage = mounted.getExplosionDamage();

        // divide damage by 10 for aeros, per TW rules on pg. 161
        if (en instanceof Aero) {
            int newdamage = (int) Math.floor(damage / 10.0);
            if (newdamage == 0 && damage > 0)
                damage = 1;
            else
                damage = newdamage;
        }

        if (damage <= 0) {
            return vDesc;
        }

        Report r = new Report(6390);
        r.subject = en.getId();
        r.add(mounted.getName());
        r.add(damage);
        r.indent(3);
        r.newlines = 0;
        vDesc.addElement(r);
        // Mounted is a weapon and has Hot-Loaded ammo in it and it exploded now
        // we need to roll for chain reaction
        if (mounted.getType() instanceof WeaponType && mounted.isHotLoaded()) {
            int roll = Compute.d6(2);
            int ammoExploded = 0;
            r = new Report(6077);
            r.subject = en.getId();
            r.add(roll);
            r.indent(2);
            vDesc.addElement(r);

            // roll of 2-5 means a chain reaction happened
            if (roll < 6) {
                for (Mounted ammo : en.getAmmo()) {
                    if (ammo.getLocation() == loc && ammo.getExplosionDamage() > 0
                    // Dead-Fire ammo bins are designed not to explode
                            // from the chain reaction
                            // Of Critted Launchers with DFM or HotLoaded ammo.
                            && ((AmmoType) ammo.getType()).getMunitionType() != AmmoType.M_DEAD_FIRE) {
                        ammoExploded++;
                        vDesc.addAll(this.explodeEquipment(en, loc, ammo));
                        break;
                    }
                }
                if (ammoExploded == 0) {
                    r = new Report(6078);
                    r.subject = en.getId();
                    r.indent(2);
                    vDesc.addElement(r);
                }
            } else {
                r = new Report(6079);
                r.subject = en.getId();
                r.indent(2);
                vDesc.addElement(r);
            }
        }

        HitData hit = new HitData(loc);
        // check to determine whether this is capital scale if we have a capital
        // scale entity
        if (mounted.getType() instanceof AmmoType) {
            if (((AmmoType) mounted.getType()).isCapital()) {
                hit.setCapital(true);
            }

        }

        mounted.setShotsLeft(0);
        vDesc.addAll(damageEntity(en, hit, damage, true));
        Report.addNewline(vDesc);

        int pilotDamage = 2;
        if (en instanceof Aero)
            pilotDamage = 1;
        if (en.getCrew().getOptions().booleanOption("pain_resistance"))
            pilotDamage = 1;
        if (en.getCrew().getOptions().booleanOption("iron_man"))
            pilotDamage = 1;
        if (!en.crew.getOptions().booleanOption("pain_shunt"))
            vDesc.addAll(damageCrew(en, pilotDamage));
        if (en.crew.isDoomed() || en.crew.isDead()) {
            vDesc.addAll(destroyEntity(en, "crew death", true));
        } else {
            Report.addNewline(vDesc);
        }

        return vDesc;
    }

    /**
     * Makes one slot of ammo, determined by certain rules, explode on a mech.
     */
    private Vector<Report> explodeAmmoFromHeat(Entity entity) {
        int damage = 0;
        int rack = 0;
        int boomloc = -1;
        int boomslot = -1;
        Vector<Report> vDesc = new Vector<Report>();

        for (int j = 0; j < entity.locations(); j++) {
            for (int k = 0; k < entity.getNumberOfCriticals(j); k++) {
                CriticalSlot cs = entity.getCritical(j, k);
                if (cs == null || cs.isDestroyed() || cs.isHit() || cs.getType() != CriticalSlot.TYPE_EQUIPMENT) {
                    continue;
                }
                Mounted mounted = entity.getEquipment(entity.getCritical(j, k).getIndex());
                if (!(mounted.getType() instanceof AmmoType)) {
                    continue;
                }
                AmmoType atype = (AmmoType) mounted.getType();
                if (!atype.isExplosive()) {
                    continue;
                }
                // ignore empty bins
                if (mounted.getShotsLeft() == 0) {
                    continue;
                }
                // BMRr, pg. 48, compare one rack's
                // damage. Ties go to most rounds.
                int newRack = atype.getDamagePerShot() * atype.getRackSize();
                int newDamage = mounted.getExplosionDamage();
                if (!mounted.isHit() && (rack < newRack || rack == newRack && damage < newDamage)) {
                    rack = newRack;
                    damage = newDamage;
                    boomloc = j;
                    boomslot = k;
                }
            }
        }
        if (boomloc != -1 && boomslot != -1) {
            CriticalSlot slot = entity.getCritical(boomloc, boomslot);
            slot.setHit(true);
            entity.getEquipment(slot.getIndex()).setHit(true);
            vDesc.addAll(explodeEquipment(entity, boomloc, boomslot));
        } else {
            // Luckily, there is no ammo to explode.
            Report r = new Report(5105);
            r.subject = entity.getId();
            r.indent();
            vDesc.addElement(r);
        }
        return vDesc;
    }

    /**
     * Makes a mech fall.
     */
    private Vector<Report> doEntityFall(Entity entity, Coords fallPos, int height, int facing, PilotingRollData roll) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        Report r;

        IHex fallHex = game.getBoard().getHex(fallPos);

        // we don't need to deal damage yet, if the entity is doing DFA
        if (entity.isMakingDfa()) {
            r = new Report(2305);
            r.subject = entity.getId();
            vPhaseReport.add(r);
            entity.setProne(true);
            return vPhaseReport;
        }

        // facing after fall
        String side;
        int table;
        switch (facing) {
        case 1:
        case 2:
            side = "right side";
            table = ToHitData.SIDE_RIGHT;
            break;
        case 3:
            side = "rear";
            table = ToHitData.SIDE_REAR;
            break;
        case 4:
        case 5:
            side = "left side";
            table = ToHitData.SIDE_LEFT;
            break;
        case 0:
        default:
            side = "front";
            table = ToHitData.SIDE_FRONT;
        }

        int waterDepth = fallHex.terrainLevel(Terrains.WATER);
        int bridgeHeight = fallHex.terrainLevel(Terrains.BRIDGE_ELEV) + fallHex.depth();
        int buildingHeight = fallHex.terrainLevel(Terrains.BLDG_ELEV);
        int damageHeight = height;
        int newElevation;

        if (height >= buildingHeight && buildingHeight >= 0) {
            damageHeight -= buildingHeight;
            newElevation = buildingHeight;
        } else if (height >= bridgeHeight && bridgeHeight >= 0) {
            damageHeight -= bridgeHeight;
            waterDepth = 0;
            newElevation = fallHex.terrainLevel(Terrains.BRIDGE_ELEV);
        } else if (fallHex.containsTerrain(Terrains.ICE) && entity.getElevation() == 0) {
            waterDepth = 0;
            newElevation = 0;
        }
        // HACK: if the dest hex is water, assume that the fall height given is
        // to the floor of the hex, and modifiy it so that it's to the surface
        else if (waterDepth > 0) {
            damageHeight = height - waterDepth;
            newElevation = -waterDepth;
        } else if (fallHex.containsTerrain(Terrains.BLDG_ELEV)) {
            Building bldg = game.getBoard().getBuildingAt(fallPos);
            if (bldg.getType() == Building.WALL)
                newElevation = Math.max(fallHex.getElevation(), fallHex.terrainLevel(Terrains.BLDG_ELEV));
            else
                newElevation = 0;
            waterDepth = 0;
        } else {
            waterDepth = 0; // because it will be used to set elevation
            newElevation = 0;
        }

        // Falling into water instantly destroys most non-mechs
        if (waterDepth > 0 && !(entity instanceof Mech) && !(entity instanceof Protomech) && !(entity.getRunMP() > 0 && entity.getMovementMode() == IEntityMovementMode.HOVER) && entity.getMovementMode() != IEntityMovementMode.HYDROFOIL && entity.getMovementMode() != IEntityMovementMode.NAVAL && entity.getMovementMode() != IEntityMovementMode.SUBMARINE && entity.getMovementMode() != IEntityMovementMode.INF_UMU) {
            vPhaseReport.addAll(destroyEntity(entity, "a watery grave", false));
            return vPhaseReport;
        }

        // calculate damage for hitting the surface
        int damage = (int) Math.round(entity.getWeight() / 10.0) * (damageHeight + 1);
        // calculate damage for hitting the ground, but only if we actually fell
        // into water
        // if we fell onto the water surface, that damage is halved.
        int waterDamage = 0;
        if (waterDepth > 0) {
            damage /= 2;
            waterDamage = (int) Math.round(entity.getWeight() / 10.0) * (waterDepth + 1) / 2;
        }

        // If the waterDepth is larger than the fall height,
        // we fell underwater
        if (waterDepth > height) {
            damage = 0;
            waterDamage = (int) Math.round(entity.getWeight() / 10.0) * (height + 1) / 2;
        }
        // adjust damage for gravity
        damage = Math.round(damage * game.getPlanetaryConditions().getGravity());
        waterDamage = Math.round(waterDamage * game.getPlanetaryConditions().getGravity());

        // report falling
        if (waterDamage == 0) {
            r = new Report(2310);
            r.subject = entity.getId();
            r.indent();
            r.newlines = 0;
            r.addDesc(entity);
            r.add(side); // international issue
            r.add(damage);
        } else if (damage > 0) {
            r = new Report(2315);
            r.subject = entity.getId();
            r.indent();
            r.newlines = 0;
            r.addDesc(entity);
            r.add(side); // international issue
            r.add(damage);
            r.add(waterDamage);
        } else {
            r = new Report(2310);
            r.subject = entity.getId();
            r.indent();
            r.newlines = 0;
            r.addDesc(entity);
            r.add(side); // international issue
            r.add(waterDamage);
        }
        vPhaseReport.add(r);
        damage += waterDamage;

        // Any swarming infantry will be dislodged, but we don't want to
        // interrupt the fall's report. We have to get the ID now because
        // the fall may kill the entity which will reset the attacker ID.
        final int swarmerId = entity.getSwarmAttackerId();

        // Positioning must be prior to damage for proper handling of breaches
        // Only Mechs can fall prone.
        if (entity instanceof Mech) {
            entity.setProne(true);
        }
        entity.setPosition(fallPos);
        entity.setFacing((entity.getFacing() + (facing - 1)) % 6);
        entity.setSecondaryFacing(entity.getFacing());
        entity.setElevation(newElevation);
        if (waterDepth > 0) {
            for (int loop = 0; loop < entity.locations(); loop++) {
                entity.setLocationStatus(loop, ILocationExposureStatus.WET);
            }
        }

        //if falling into a bog-down hex, the entity automatically gets stuck
        if(fallHex.getBogDownModifier(entity.getMovementMode(), entity instanceof LargeSupportTank) != TargetRoll.AUTOMATIC_SUCCESS) {
            entity.setStuck(true);
            r = new Report(2081);
            r.subject = entity.getId();
            r.add(entity.getDisplayName(), true);
            vPhaseReport.add(r);
            //check for quicksand
            vPhaseReport.addAll(checkQuickSand(fallPos));
        }
        
        // standard damage loop
        while (damage > 0) {
            int cluster = Math.min(5, damage);
            HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, table);
            hit.makeFallDamage(true);
            vPhaseReport.addAll(damageEntity(entity, hit, cluster));
            damage -= cluster;
        }

        // check for location exposure
        vPhaseReport.addAll(doSetLocationsExposure(entity, fallHex, false, -waterDepth));

        // we want to be able to avoid pilot damage even when it was
        // an automatic fall, only unconsciousness should cause auto-damage
        roll.removeAutos();

        if (height > 1) {
            roll.addModifier(height - 1, "height of fall");
        }

        entity.addPilotingModifierForTerrain(roll, fallPos);

        if (roll.getValue() == TargetRoll.IMPOSSIBLE) {
            r = new Report(2320);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(entity.crew.getName());
            r.indent();
            vPhaseReport.add(r);
            vPhaseReport.addAll(damageCrew(entity, 1));
            Report.addNewline(vPhaseReport);
        } else {
            int diceRoll = Compute.d6(2);
            r = new Report(2325);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(entity.crew.getName());
            r.add(roll.getValueAsString());
            r.add(diceRoll);
            if (diceRoll >= roll.getValue()) {
                r.choose(true);
                vPhaseReport.add(r);
            } else {
                r.choose(false);
                vPhaseReport.add(r);
                vPhaseReport.addAll(damageCrew(entity, 1));
                Report.addNewline(vPhaseReport);
            }
        }

        // Now dislodge any swarming infantry.
        if (Entity.NONE != swarmerId) {
            final Entity swarmer = game.getEntity(swarmerId);
            entity.setSwarmAttackerId(Entity.NONE);
            swarmer.setSwarmTargetId(Entity.NONE);
            // Did the infantry fall into water?
            if (waterDepth > 0 && swarmer.getMovementMode() != IEntityMovementMode.INF_UMU) {
                // Swarming infantry die.
                swarmer.setPosition(fallPos);
                r = new Report(2330);
                r.newlines = 0;
                r.subject = swarmer.getId();
                r.addDesc(swarmer);
                vPhaseReport.add(r);
                vPhaseReport.addAll(destroyEntity(swarmer, "a watery grave", false));
            } else {
                // Swarming infantry take a 2d6 point hit.
                // ASSUMPTION : damage should not be doubled.
                r = new Report(2335);
                r.newlines = 0;
                r.subject = swarmer.getId();
                r.addDesc(swarmer);
                vPhaseReport.add(r);
                vPhaseReport.addAll(damageEntity(swarmer, swarmer.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT), Compute.d6(2)));
                Report.addNewline(vPhaseReport);
            }
            swarmer.setPosition(fallPos);
            entityUpdate(swarmerId);
            if (!swarmer.isDone()) {
                swarmer.setDone(true);
                game.removeTurnFor(swarmer);
                send(createTurnVectorPacket());
            }
        } // End dislodge-infantry

        // clear all PSRs after a fall -- the Mek has already failed ONE and
        // fallen, it'd be cruel to make it fail some more!
        game.resetPSRs(entity);
        
        //if there is a minefield in this hex, then the mech may set it off
        if (game.containsMinefield(fallPos) && enterMinefield(entity, fallPos, newElevation, true, vPhaseReport, 12)) {
            resetMines();
        }
        
        return vPhaseReport;
    }

    /**
     * The mech falls into an unoccupied hex from the given height above
     */
    private Vector<Report> doEntityFall(Entity entity, Coords fallPos, int height, PilotingRollData roll) {
        return doEntityFall(entity, fallPos, height, Compute.d6(1), roll);
    }

    /**
     * The mech falls down in place
     */
    private Vector<Report> doEntityFall(Entity entity, PilotingRollData roll) {
        return doEntityFall(entity, entity.getPosition(), entity.getElevation() + game.getBoard().getHex(entity.getPosition()).depth(), roll);
    }

    /**
     * Report: - Any ammo dumps beginning the following round. - Any ammo dumps
     * that have ended with the end of this round.
     */
    private void resolveAmmoDumps() {
        Report r;
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity entity = i.nextElement();
            for (Mounted m : entity.getAmmo()) {
                if (m.isPendingDump()) {
                    // report dumping next round
                    r = new Report(5110);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(m.getName());
                    addReport(r);
                    // update status
                    m.setPendingDump(false);
                    m.setDumping(true);
                } else if (m.isDumping()) {
                    // report finished dumping
                    r = new Report(5115);
                    r.subject = entity.getId();
                    r.addDesc(entity);
                    r.add(m.getName());
                    addReport(r);
                    // update status
                    m.setDumping(false);
                    m.setShotsLeft(0);
                }
            }
            entity.reloadEmptyWeapons();
        }
    }

    /**
     * Checks for fire ignition based on a given target roll.
     * If successful, lights a fire
     * also checks to see that fire is possible in the specified hex.
     * 
     * @param hex - the <code>IHex</code> to be lit.
     * @param roll - the <code>TargetRoll</code> for the ignition roll
     * @param bInferno - <code>true</code> if the fire is an inferno fire. If this value is <code>false</code> the hex will be
     *            lit only if it contains Woods,jungle or a Building.
     * @param entityId - the entityId responsible for the ignite attempt. If the
     *            value is Entity.NONE, then the roll attempt will not be
     *            included in the report.
     */
    public boolean checkIgnition(Coords c, TargetRoll roll, boolean bInferno, int entityId, Vector<Report> vPhaseReport) {

        IHex hex = game.getBoard().getHex(c);
        
        // The hex might be null due to spreadFire translation
        // goes outside of the board limit.
        if (null == hex) {
            return false;
        }

        // The hex may already be on fire.
        if (hex.containsTerrain(Terrains.FIRE)) {
            return false;
        }

        if (!bInferno && !hex.isIgnitable()) {
            return false;
        }

        int fireRoll = Compute.d6(2);
        Report r = null;
        if (entityId != Entity.NONE) {
            r = new Report(3430);
            r.indent(2);
            r.subject = entityId;
            r.add(roll.getValueAsString());
            r.add(roll.getDesc());
            r.add(fireRoll);
            vPhaseReport.add(r);
        }
        if (fireRoll >= roll.getValue()) {
            ignite(c, bInferno, vPhaseReport);
            return true;
        }
        return false;
    }

    /**
     * Returns true if the hex is set on fire with the specified roll. Of
     * course, also checks to see that fire is possible in the specified hex.
     * This version of the method will not report the attempt roll.
     * 
     * @param hex -
     *            the <code>IHex</code> to be lit.
     * @param roll -
     *            the <code>int</code> target number for the ignition roll
     * @param bAnyTerrain -
     *            <code>true</code> if the fire can be lit in any terrain. If
     *            this value is <code>false</code> the hex will be lit only if
     *            it contains Woods, jungle or a Building.
     */
    public boolean checkIgnition(Coords c, TargetRoll roll, boolean bInferno) {
        return checkIgnition(c, roll, bInferno, Entity.NONE, null);
    }

    /**
     * Returns true if the hex is set on fire with the specified roll. Of
     * course, also checks to see that fire is possible in the specified hex.
     * This version of the method will not report the attempt roll.
     * 
     * @param hex -
     *            the <code>IHex</code> to be lit.
     * @param roll -
     *            the <code>int</code> target number for the ignition roll
     */
    public boolean checkIgnition(Coords c, TargetRoll roll) {
        // default signature, assuming only woods can burn
        return checkIgnition(c, roll, false, Entity.NONE, null);
    }

    /**
     * add fire to a hex
     * 
     * @param c - the <code>Coords</code> of the hex to be set on fire
     * @param bInferno - <code>true</code> if the fire to be set is an inferno
     */
    public void ignite(Coords c, boolean bInferno, Vector<Report> vReport) {
        //you can't start fires in some planetary conditions!
        if(null != game.getPlanetaryConditions().cannotStartFire()) {
            Report r = new Report(3007);
            r.indent(2);
            r.add(game.getPlanetaryConditions().cannotStartFire());
            vReport.add(r);
            return;
        }
               
        if(!game.getOptions().booleanOption("tacops_start_fire")) {
            Report r = new Report(3008);
            r.indent(2);
            vReport.add(r);
            return;
        }
        
        IHex hex = game.getBoard().getHex(c);
        if(null == hex) {
            return;
        }
        
        Report r = new Report(3005);
        r.indent(2);
        r.add(c.getBoardNum());
        
        //type of fire. We use level 2 for infernos
        int type = 1;
        if(bInferno) {
            type = type + 1;
            r.messageId = 3006;
            
        }
        
        //report it
        if(null != vReport)
            vReport.add(r);
        hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.FIRE, type));
        sendChangedHex(c);    
    }
    
    /**
     * remove fire from a hex
     * 
     * @param x
     * @param y
     * @param hex
     */
    public void removeFire(Coords fireCoords, String reason) {
        IHex hex = game.getBoard().getHex(fireCoords);
        if(null == hex)
            return;
        hex.removeTerrain(Terrains.FIRE);
        hex.resetFireTurn();
        sendChangedHex(fireCoords);
        // fire goes out
        Report r = new Report(5170, Report.PUBLIC);
        r.add(fireCoords.getBoardNum());
        r.add(reason);
        addReport(r);
    }

    /**
     * Called when a fire is burning. Called 3 times per fire hex.
     * 
     * @param x
     *            The <code>int</code> x-coordinate of the hex
     * @param y
     *            The <code>int</code> y-coordinate of the hex
     */
    public void addSmoke(ArrayList<Coords> coords, int windDir, boolean bInferno) {

        // if a tornado, then no smoke!
        if (game.getPlanetaryConditions().getWindStrength() > PlanetaryConditions.WI_STORM) {
            return;
        }

        int smokeLevel = 0;
        for (Coords smokeCoords : coords) {
            IHex smokeHex = game.getBoard().getHex(smokeCoords);
            Report r;
            if (smokeHex == null) {
                continue;
            }
            // Have to check if it's inferno smoke or from a heavy/hardened
            // building
            // - heavy smoke from those
            if (bInferno 
                    || Building.MEDIUM < smokeHex.terrainLevel(Terrains.FUEL_TANK) 
                    || Building.MEDIUM < smokeHex.terrainLevel(Terrains.BUILDING)) {
                if (smokeHex.terrainLevel(Terrains.SMOKE) == 2) {
                    // heavy smoke fills hex
                    r = new Report(5180, Report.PUBLIC);
                    r.add(smokeCoords.getBoardNum());
                    addReport(r);
                    smokeLevel = 2;
                } else {
                    smokeLevel = Math.max(smokeLevel, 2);;
                    r = new Report(5185, Report.PUBLIC);
                    r.add(smokeCoords.getBoardNum());
                    addReport(r);
                    smokeLevel = 2;
                }
            } else {
                if (smokeHex.terrainLevel(Terrains.SMOKE) == 2) {
                    // heavy smoke overpowers light
                    r = new Report(5190, Report.PUBLIC);
                    r.add(smokeCoords.getBoardNum());
                    smokeLevel = Math.max(smokeLevel, 1);
                    addReport(r);
                } else if (smokeHex.terrainLevel(Terrains.SMOKE) == 1) {
                    // light smoke continue to fill hex
                    r = new Report(5195, Report.PUBLIC);
                    r.add(smokeCoords.getBoardNum());
                    addReport(r);
                    smokeLevel = Math.max(smokeLevel, 1);
                } else {
                    smokeLevel = Math.max(smokeLevel, 1);;
                    // light smoke fills hex
                    r = new Report(5200, Report.PUBLIC);
                    r.add(smokeCoords.getBoardNum());
                    addReport(r);
                }
            }
        }
        createSmoke(coords, smokeLevel, 0);
    }

    /**
     * Scans the boards directory for map boards of the appropriate size and
     * returns them.
     */
    private ArrayList<String> scanForBoardsInDir(String addPath, String basePath, int w, int h) {
        File dir = new File(addPath);
        String fileList[] = dir.list();
        ArrayList<String> tempList = new ArrayList<String>();
        for (int i = 0; i < fileList.length; i++) {
            File x = new File(addPath.concat("/").concat(fileList[i]));
            if (x.isDirectory()) {
                tempList.addAll(scanForBoardsInDir(addPath.concat("/").concat(fileList[i]), basePath.concat("/").concat(fileList[i]), w, h));
                continue;
            }
            if (fileList[i].indexOf(".svn") != -1) {
                continue; // Ignore Subversion directories
            }
            if (fileList[i].indexOf(".board") == -1) {
                continue;
            }
            if (Board.boardIsSize(basePath.concat("/").concat(fileList[i]), w, h)) {
                tempList.add(basePath.concat("/").concat(fileList[i].substring(0, fileList[i].lastIndexOf(".board"))));
            }
        }
        return tempList;
    }

    private ArrayList<String> scanForBoards(int boardWidth, int boardHeight) {
        ArrayList<String> boards = new ArrayList<String>();

        File boardDir = new File("data/boards");
        boards.add(MapSettings.BOARD_GENERATED);
        // just a check...
        if (!boardDir.isDirectory()) {
            return boards;
        }

        // scan files
        ArrayList<String> tempList = new ArrayList<String>();
        Comparator<String> sortComp = StringUtil.stringComparator();
        tempList = scanForBoardsInDir("data/boards", "", boardWidth, boardHeight);
        // if there are any boards, add these:
        if (tempList.size() > 0) {
            boards.add(MapSettings.BOARD_RANDOM);
            boards.add(MapSettings.BOARD_SURPRISE);
            Collections.sort(tempList, sortComp);
            for (int loop = 0; loop < tempList.size(); loop++) {
                boards.add(tempList.get(loop));
            }
        }

        // TODO: alphabetize files?

        return boards;
    }

    /**
     * @return wether this game is double blind or not and we should be blind in
     *         the current phase
     */
    private boolean doBlind() {
        return game.getOptions().booleanOption("double_blind") && game.getPhase().isDuringOrAfter(IGame.Phase.PHASE_DEPLOYMENT);
    }

    /**
     * In a double-blind game, update only visible entities. Otherwise, update
     * everyone
     */
    public void entityUpdate(int nEntityID) {
        entityUpdate(nEntityID, new Vector<UnitLocation>());
    }

    /**
     * In a double-blind game, update only visible entities. Otherwise, update
     * everyone
     */
    public void entityUpdate(int nEntityID, Vector<UnitLocation> movePath) {
        Entity eTarget = game.getEntity(nEntityID);
        if (eTarget == null) {
            if (game.getOutOfGameEntity(nEntityID) != null) {
                System.err.print("S: attempted to send entity update for out of game entity, id was ");
                System.err.println(nEntityID);
            } else {
                System.err.print("S: attempted to send entity update for null entity, id was ");
                System.err.println(nEntityID);
            }

            return; // do not send the update it will crash the client
        }

        // If we're doing double blind, be careful who can see it...
        if (doBlind()) {
            Vector<Player> vPlayers = game.getPlayersVector();
            Vector<Player> vCanSee = whoCanSee(eTarget);

            // send an entity update to everyone who can see
            Packet pack = createEntityPacket(nEntityID, movePath);
            for (int x = 0; x < vCanSee.size(); x++) {
                Player p = vCanSee.elementAt(x);
                send(p.getId(), pack);
            }
            // send an entity delete to everyone else
            pack = createRemoveEntityPacket(nEntityID, eTarget.getRemovalCondition());
            for (int x = 0; x < vPlayers.size(); x++) {
                if (!vCanSee.contains(vPlayers.elementAt(x))) {
                    Player p = vPlayers.elementAt(x);
                    send(p.getId(), pack);
                }
            }
        } else {
            // But if we're not, then everyone can see.
            send(createEntityPacket(nEntityID, movePath));
        }
    }

    /**
     * Returns a vector of which players can see this entity.
     */
    private Vector<Player> whoCanSee(Entity entity) {

        // Some times Null entities are sent to this
        if (entity == null)
            return new Vector<Player>();

        boolean bTeamVision = game.getOptions().booleanOption("team_vision");
        Vector<Entity> vEntities = game.getEntitiesVector();

        Vector<Player> vCanSee = new Vector<Player>();
        vCanSee.addElement(entity.getOwner());
        if (bTeamVision) {
            addTeammates(vCanSee, entity.getOwner());
        }

        // Deal with players who can see all.
        for (Enumeration<Player> p = game.getPlayers(); p.hasMoreElements();) {
            Player player = p.nextElement();

            if (player.canSeeAll() && !vCanSee.contains(p))
                vCanSee.addElement(player);
        }

        // If the entity is hidden, skip this; noone else will be able to see
        // it.
        if (!entity.isHidden()) {
            for (int i = 0; i < vEntities.size(); i++) {
                Entity e = vEntities.elementAt(i);
                if (vCanSee.contains(e.getOwner()) || !e.isActive()) {
                    continue;
                }

                // Off board units should not spot on board units
                if (e.isOffBoard()) {
                    continue;
                }
                if (Compute.canSee(game, e, entity)) {
                    vCanSee.addElement(e.getOwner());
                    if (bTeamVision) {
                        addTeammates(vCanSee, e.getOwner());
                    }
                    addObservers(vCanSee);
                }
            }
        }

        return vCanSee;
    }

    /**
     * can the passed <code>Player</code> see the passed <code>Entity</code>?
     * 
     * @param p
     *            <code>Player</code>
     * @param e
     *            <code>Entity</code>
     * @return if the player can see the entity
     */
    private boolean canSee(Player p, Entity e) {
        if (e.getOwner().getId() == p.getId()) {
            // The owner of an entity should be able to see it, of course.
            return true;
        }
        Vector<Player> playersWhoCanSee = whoCanSee(e);
        for (int i = 0; i < playersWhoCanSee.size(); i++) {
            Player currentPlayer = playersWhoCanSee.elementAt(i);
            if (currentPlayer.equals(p)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds teammates of a player to the Vector. Utility function for whoCanSee.
     */
    private void addTeammates(Vector<Player> vector, Player player) {
        Vector<Player> vPlayers = game.getPlayersVector();
        for (int j = 0; j < vPlayers.size(); j++) {
            Player p = vPlayers.elementAt(j);
            if (!player.isEnemyOf(p) && !vector.contains(p)) {
                vector.addElement(p);
            }
        }
    }

    /**
     * Adds observers to the Vector. Utility function for whoCanSee.
     */
    private void addObservers(Vector<Player> vector) {
        Vector<Player> vPlayers = game.getPlayersVector();
        for (int j = 0; j < vPlayers.size(); j++) {
            Player p = vPlayers.elementAt(j);
            if (p.isObserver() && !vector.contains(p)) {
                vector.addElement(p);
            }
        }
    }

    /**
     * Send the complete list of entities to the players. If double_blind is in
     * effect, enforce it by filtering the entities
     */
    private void entityAllUpdate() {
        // If double-blind is in effect, filter each players' list individually,
        // and then quit out...
        if (doBlind()) {
            Vector<Player> vPlayers = game.getPlayersVector();
            for (int x = 0; x < vPlayers.size(); x++) {
                Player p = vPlayers.elementAt(x);
                send(p.getId(), createFilteredEntitiesPacket(p));
            }
            return;
        }

        // Otherwise, send the full list.
        send(createEntitiesPacket());
    }

    /**
     * Filters an entity vector according to LOS
     */
    private Vector<Entity> filterEntities(Player pViewer, Vector<Entity> vEntities) {
        Vector<Entity> vCanSee = new Vector<Entity>();
        Vector<Entity> vMyEntities = new Vector<Entity>();
        Vector<Entity> vAllEntities = game.getEntitiesVector();
        boolean bTeamVision = game.getOptions().booleanOption("team_vision");

        // If they can see all, return the input list
        if (pViewer.canSeeAll()) {
            return vEntities;
        }

        // If they're an observer, they can see anything seen by any enemy.
        if (pViewer.isObserver()) {
            vMyEntities.addAll(vAllEntities);
            for (Entity a : vMyEntities) {
                for (Entity b : vMyEntities) {
                    if (a.isEnemyOf(b) && Compute.canSee(game, b, a)) {
                        vCanSee.add(a);
                        break;
                    }
                }
            }
            return vCanSee;
        }

        // If they aren't an observer and can't see all, create the list of
        // "friendly" units.
        for (int x = 0; x < vAllEntities.size(); x++) {
            Entity e = vAllEntities.elementAt(x);
            if (e.getOwner() == pViewer || bTeamVision && !e.getOwner().isEnemyOf(pViewer)) {
                vMyEntities.addElement(e);
            }
        }

        // Then, break down the list by whether they're friendly,
        // or whether or not any friendly unit can see them.
        for (int x = 0; x < vEntities.size(); x++) {
            Entity e = vEntities.elementAt(x);

            // If it's their own unit, obviously, they can see it.
            if (vMyEntities.contains(e)) {
                vCanSee.addElement(e);
                continue;
            } else if (e.isHidden()) {
                // If it's NOT friendly and is hidden, they can't see it,
                // period.
                // LOS doesn't matter.
                continue;
            }

            for (int y = 0; y < vMyEntities.size(); y++) {
                Entity e2 = vMyEntities.elementAt(y);

                // If they're off-board, skip it; they can't see anything.
                if (e2.isOffBoard()) {
                    continue;
                }

                // Otherwise, if they can see the entity in question...
                if (Compute.canSee(game, e2, e)) {
                    vCanSee.addElement(e);
                    break;
                }
            }
        }

        return vCanSee;
    }

    /**
     * filter a reportvector for double blind
     * 
     * @param originalReportVector
     *            the original <code>Vector<Report></code>
     * @param p
     *            the <code>Player</code> who should see stuff only visible to
     *            him
     * @return the <code>Vector<Report></code> with stuff only Player p can
     *         see
     */
    private Vector<Report> filterReportVector(Vector<Report> originalReportVector, Player p) {
        // FIXME
        // Please optimize and document me.
        // Only bother actually doing all this crap if double-blind is in
        // effect.
        if (!doBlind()) {
            return new Vector<Report>(originalReportVector);
        }

        // But if it is, then filter everything properly.
        Vector<Report> filteredReportVector = new Vector<Report>();
        Report r;
        for (int i = 0; i < originalReportVector.size(); i++) {
            r = originalReportVector.elementAt(i);
            filteredReportVector.addElement(filterReport(r, p, false));
        }

        return filteredReportVector;
    }

    /**
     * Filter a single report so that the correct double-blind obscuration takes
     * place. to mark a message as "thsi should be visible toanyone seeing this
     * entity" set r.subject to the entity id to mark a message as "only visble
     * to the player" set r.player to that player's id and set
     * r.type=Report.PLAYER to mark a message as visible to all , set r.type to
     * Report.PUBLIC
     * 
     * @param r
     *            the Report to filter
     * @param p
     *            the Player that we are going to send the filtered report to
     * @param omitCheck
     *            boolean indicating that this report hapened in the past, so we
     *            no longer have access to the Player
     * @return a new Report, which has possibly been obscured
     */
    private Report filterReport(Report r, Player p, boolean omitCheck) {

        if (r.subject == Entity.NONE && r.type != Report.PLAYER && r.type != Report.PUBLIC) {
            // Reports that don't have a subject should be public.
            System.err.println("Error: Attempting to filter a Report object that is not public yet has no subject.\n\t\tmessageId: " + r.messageId);
            return r;
        }
        if (r.type == Report.PUBLIC || p == null && !omitCheck) {
            return r;
        }
        Entity entity = game.getEntity(r.subject);
        Player owner = null;
        if (entity != null) {
            owner = entity.getOwner();
            // off board (Artillery) units get treated as public messages
            if (entity.isOffBoard())
                return r;
        }

        if (r.type != Report.PLAYER && !omitCheck && (entity == null || owner == null)) {
            System.err.println("Error: Attempting to filter a Report object that is not public but has a subject (" + entity + ") with owner (" + owner + ").\n\tmessageId: " + r.messageId);
            return r;
        }

        Report copy = new Report(r);
        for (int j = 0; j < copy.dataCount(); j++) {
            if ((r.type == Report.PLAYER && p.getId() != r.player) || omitCheck || (entity != null && !canSee(p, entity))) {
                if (r.isValueObscured(j)) {
                    copy.hideData(j);
                    // Mark the original report to indicate which players
                    // received an obscured version of it.
                    if (p != null) {
                        r.addObscuredRecipient(p.getName());
                    }
                }
                // simulate hiding the report for *true* double-blind play
                // ***DEBUG*** TESTING ONLY
                // copy.markForTesting();
            }
        }
        return copy;
    }

    /**
     * Returns a vector which has as it's keys the round number and as it's
     * elements vectors that contain all the reports for the specified player
     * that round. The reports returned this way are properly filtered for
     * double blind.
     * 
     * @param pastReports
     * @param p
     * @return
     */
    private Vector<Vector<Report>> filterPastReports(Vector<Vector<Report>> pastReports, Player p) {
        // This stuff really only needs to be printed for debug reasons. other
        // wise the logs get
        // filled to the brim when ever someone connects. --Torren.
        // System.err.println("filterPastReports() begin");
        // System.err.println(" player is " + p.getName());

        // Only actually bother with the filtering if double-blind is in effect.
        if (doBlind()) {
            // System.err.println(" pastReports vector is\n" + pastReports);
            Vector<Vector<Report>> filteredReports = new Vector<Vector<Report>>();
            Vector<Report> filteredRoundReports;
            Vector<Report> roundReports = new Vector<Report>();
            Report r;
            for (int i = 0; i < pastReports.size(); i++) {
                filteredRoundReports = new Vector<Report>();
                roundReports = pastReports.elementAt(i);
                // System.err.println(" roundReports vector is\n" +
                // roundReports);
                for (int j = 0; j < roundReports.size(); j++) {
                    r = roundReports.elementAt(j);
                    if (r.isObscuredRecipient(p.getName())) {
                        // System.err.println(" report is " + r + "
                        // -obscuring-");
                        filteredRoundReports.addElement(filterReport(r, null, true));
                    } else {
                        // System.err.println(" report is " + r);
                        filteredRoundReports.addElement(r);
                    }
                }
                // System.err.println(" filteredRoundReport is\n" +
                // filteredRoundReports);
                filteredReports.addElement(filteredRoundReports);
            }
            // System.err.println("filterPastReports() end");
            return filteredReports;
        }

        // System.err.println("filterPastReports() end");
        return pastReports;
    }

    /**
     * Updates entities graphical "visibility indications" which are used in
     * double-blind games.
     */
    private void updateVisibilityIndicator() {
        Vector<Entity> vAllEntities = game.getEntitiesVector();
        for (int x = 0; x < vAllEntities.size(); x++) {
            Entity e = vAllEntities.elementAt(x);
            boolean previousVisibleValue = e.isVisibleToEnemy();
            boolean previousSeenValue = e.isSeenByEnemy();
            e.setVisibleToEnemy(false);
            Vector<Player> vCanSee = whoCanSee(e);
            for (int y = 0; y < vCanSee.size(); y++) {
                Player p = vCanSee.elementAt(y);
                if (e.getOwner().isEnemyOf(p) && !p.isObserver()) {
                    e.setVisibleToEnemy(true);
                    e.setSeenByEnemy(true);
                }
            }
            if (previousVisibleValue != e.isVisibleToEnemy() || previousSeenValue != e.isSeenByEnemy()) {
                sendVisibilityIndicator(e);
            }
        }
    }

    /**
     * Checks if an entity added by the client is valid and if so, adds it to
     * the list
     */
    private void receiveEntityAdd(Packet c, int connIndex) {
        final Entity entity = (Entity) c.getObject(0);

        // Verify the entity's design
        if (Server.entityVerifier == null)
            Server.entityVerifier = new EntityVerifier(new File(VERIFIER_CONFIG_FILENAME));
        // we can only test meks and vehicles right now
        if (entity instanceof Mech || entity instanceof Tank) {
            TestEntity testEntity = null;
            entity.restore();
            if (entity instanceof Mech)
                testEntity = new TestMech((Mech) entity, Server.entityVerifier.mechOption, null);
            if (entity instanceof VTOL)
                testEntity = new TestTank((Tank) entity, Server.entityVerifier.tankOption, null);// not
            // implemented
            // yet.
            if (entity instanceof Tank)
                testEntity = new TestTank((Tank) entity, Server.entityVerifier.tankOption, null);
            StringBuffer sb = new StringBuffer();
            if (testEntity.correctEntity(sb, !game.getOptions().booleanOption("is_eq_limits"))) {
                entity.setDesignValid(true);
            } else {
                System.err.println(sb);
                if (game.getOptions().booleanOption("allow_illegal_units")) {
                    entity.setDesignValid(false);
                } else {
                    Player cheater = game.getPlayer(connIndex);
                    sendServerChat("Player " + cheater.getName() + " attempted to add an illegal unit design (" + entity.getShortNameRaw() + "), the unit was rejected.");
                    return;
                }
            }
        }

        // If we're adding a Protomech, calculate it's unit number.
        if (entity instanceof Protomech) {

            // How many Protomechs does the player already have?
            int numPlayerProtos = game.getSelectedEntityCount(new EntitySelector() {
                private final int ownerId = entity.getOwnerId();

                public boolean accept(Entity entity) {
                    if (entity instanceof Protomech && ownerId == entity.getOwnerId())
                        return true;
                    return false;
                }
            });

            // According to page 54 of the BMRr, Protomechs must be
            // deployed in full Points of five, unless circumstances have
            // reduced the number to less that that.
            entity.setUnitNumber((char) (numPlayerProtos / 5));

        } // End added-Protomech

        // Only assign an entity ID when the client hasn't.
        if (Entity.NONE == entity.getId()) {
            entity.setId(getFreeEntityId());
        }

        game.addEntity(entity.getId(), entity);
        send(createAddEntityPacket(entity.getId()));
    }

    /**
     * Updates an entity with the info from the client. Only valid to do this
     * during the lounge phase, except for heat sink changing.
     */
    private void receiveEntityUpdate(Packet c, int connIndex) {
        Entity entity = (Entity) c.getObject(0);
        Entity oldEntity = game.getEntity(entity.getId());
        if (oldEntity != null && oldEntity.getOwner() == getPlayer(connIndex)) {
            game.setEntity(entity.getId(), entity);
            entityUpdate(entity.getId());
            // In the chat lounge, notify players of customizing of unit
            if (game.getPhase() == IGame.Phase.PHASE_LOUNGE) {
                StringBuffer message = new StringBuffer();
                message.append("Unit ");
                if (game.getOptions().booleanOption("blind_drop") || game.getOptions().booleanOption("real_blind_drop")) {
                    if (Entity.NONE != entity.getExternalId()) {
                        message.append('[').append(entity.getExternalId()).append("] ");
                    }
                    message.append(entity.getId()).append('(').append(entity.getOwner().getName()).append(')');
                } else {
                    message.append(entity.getDisplayName());
                }
                message.append(" has been customized.");
                sendServerChat(message.toString());
            }
        } else {
            // hey!
        }
    }

    private void receiveCustomInit(Packet c, int connIndex) {
        // In the chat lounge, notify players of customizing of unit
        if (game.getPhase() == IGame.Phase.PHASE_LOUNGE) {
            Player p = (Player) c.getObject(0);
            sendServerChat("" + p.getName() + " has customized initiative.");
        }
    }

    /**
     * receive and process an entity mode change packet
     * 
     * @param c
     * @param connIndex
     */
    private void receiveEntityModeChange(Packet c, int connIndex) {
        int entityId = c.getIntValue(0);
        int equipId = c.getIntValue(1);
        int mode = c.getIntValue(2);
        Entity e = game.getEntity(entityId);
        if (e.getOwner() != getPlayer(connIndex)) {
            return;
        }
        Mounted m = e.getEquipment(equipId);

        if ( m == null ) {
            return;
        }
        
        try {
            // a mode change for ammo means dumping or hotloading
            if (m.getType() instanceof AmmoType && !m.getType().hasInstantModeSwitch() && mode <= 0) {
                m.setPendingDump(mode == -1);
            } else {
                if ( !m.setMode(mode) ){
                    System.err.println(e.getShortName()+ ": " + m.getName() + ": " + e.getLocationName(m.getLocation()) + " trying to compensate");
                    sendServerChat(e.getShortName()+ ": " + m.getName() + ": " + e.getLocationName(m.getLocation()) + " trying to compensate");
                    e.setGameOptions();
                    
                    if ( !m.setMode(mode) ){
                        System.err.println(e.getShortName()+ ": " + m.getName() + ": " + e.getLocationName(m.getLocation()) + " unable to compensate");
                        sendServerChat(e.getShortName()+ ": " + m.getName() + ": " + e.getLocationName(m.getLocation()) + " unable to compensate");
                    }
                    
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * receive and process an entity sytem mode change packet
     * 
     * @param c
     * @param connIndex
     */
    private void receiveEntitySystemModeChange(Packet c, int connIndex) {
        int entityId = c.getIntValue(0);
        int equipId = c.getIntValue(1);
        int mode = c.getIntValue(2);
        Entity e = game.getEntity(entityId);
        if (e.getOwner() != getPlayer(connIndex)) {
            return;
        }
        if (e instanceof Mech && equipId == Mech.SYSTEM_COCKPIT) {
            ((Mech) e).setCockpitStatus(mode);
        }
    }

    /**
     * Receive a packet that contains an Entity ammo change
     * 
     * @param c
     * @param connIndex
     */
    private void receiveEntityAmmoChange(Packet c, int connIndex) {
        int entityId = c.getIntValue(0);
        int weaponId = c.getIntValue(1);
        int ammoId = c.getIntValue(2);
        Entity e = game.getEntity(entityId);

        // Did we receive a request for a valid Entity?
        if (null == e) {
            System.err.print("Server.receiveEntityAmmoChange: could not find entity #");
            System.err.println(entityId);
            return;
        }
        Player player = getPlayer(connIndex);
        if (null != player && e.getOwner() != player) {
            System.err.print("Server.receiveEntityAmmoChange: player ");
            System.err.print(player.getName());
            System.err.print(" does not own the entity ");
            System.err.println(e.getDisplayName());
            return;
        }

        // Make sure that the entity has the given equipment.
        Mounted mWeap = e.getEquipment(weaponId);
        Mounted mAmmo = e.getEquipment(ammoId);
        if (null == mAmmo) {
            System.err.print("Server.receiveEntityAmmoChange: entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" does not have ammo #");
            System.err.println(ammoId);
            return;
        }
        if (!(mAmmo.getType() instanceof AmmoType)) {
            System.err.print("Server.receiveEntityAmmoChange: item # ");
            System.err.print(ammoId);
            System.err.print(" of entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" is a ");
            System.err.print(mAmmo.getName());
            System.err.println(" and not ammo.");
            return;
        }
        if (null == mWeap) {
            System.err.print("Server.receiveEntityAmmoChange: entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" does not have weapon #");
            System.err.println(weaponId);
            return;
        }
        if (!(mWeap.getType() instanceof WeaponType)) {
            System.err.print("Server.receiveEntityAmmoChange: item # ");
            System.err.print(weaponId);
            System.err.print(" of entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" is a ");
            System.err.print(mWeap.getName());
            System.err.println(" and not a weapon.");
            return;
        }
        if (((WeaponType) mWeap.getType()).getAmmoType() == AmmoType.T_NA) {
            System.err.print("Server.receiveEntityAmmoChange: item # ");
            System.err.print(weaponId);
            System.err.print(" of entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" is a ");
            System.err.print(mWeap.getName());
            System.err.println(" and does not use ammo.");
            return;
        }
        if (((WeaponType) mWeap.getType()).hasFlag(WeaponType.F_ONESHOT)) {
            System.err.print("Server.receiveEntityAmmoChange: item # ");
            System.err.print(weaponId);
            System.err.print(" of entity ");
            System.err.print(e.getDisplayName());
            System.err.print(" is a ");
            System.err.print(mWeap.getName());
            System.err.println(" and cannot use external ammo.");
            return;
        }

        // Load the weapon.
        e.loadWeapon(mWeap, mAmmo);
    }

    /**
     * Deletes an entity owned by a certain player from the list
     */
    private void receiveEntityDelete(Packet c, int connIndex) {
        int entityId = c.getIntValue(0);
        final Entity entity = game.getEntity(entityId);

        // Only allow players to delete their *own* entities.
        if (entity != null && entity.getOwner() == getPlayer(connIndex)) {

            // If we're deleting a Protomech, recalculate unit numbers.
            if (entity instanceof Protomech) {

                // How many Protomechs does the player have (include this one)?
                int numPlayerProtos = game.getSelectedEntityCount(new EntitySelector() {
                    private final int ownerId = entity.getOwnerId();

                    public boolean accept(Entity entity) {
                        if (entity instanceof Protomech && ownerId == entity.getOwnerId())
                            return true;
                        return false;
                    }
                });

                // According to page 54 of the BMRr, Protomechs must be
                // deployed in full Points of five, unless "losses" have
                // reduced the number to less that that.
                final char oldMax = (char) (Math.ceil(numPlayerProtos / 5.0) - 1);
                char newMax = (char) (Math.ceil((numPlayerProtos - 1) / 5.0) - 1);
                char deletedUnitNum = entity.getUnitNumber();

                // Do we have to update a Protomech from the last unit?
                if (oldMax != deletedUnitNum && oldMax != newMax) {

                    // Yup. Find a Protomech from the last unit, and
                    // set it's unit number to the deleted entity.
                    Enumeration<Entity> lastUnit = game.getSelectedEntities(new EntitySelector() {
                        private final int ownerId = entity.getOwnerId();

                        private final char lastUnitNum = oldMax;

                        public boolean accept(Entity entity) {
                            if (entity instanceof Protomech && ownerId == entity.getOwnerId() && lastUnitNum == entity.getUnitNumber())
                                return true;
                            return false;
                        }
                    });
                    Entity lastUnitMember = lastUnit.nextElement();
                    lastUnitMember.setUnitNumber(deletedUnitNum);
                    entityUpdate(lastUnitMember.getId());

                } // End update-unit-numbetr

            } // End added-Protomech

            game.removeEntity(entityId, IEntityRemovalConditions.REMOVE_NEVER_JOINED);
            send(createRemoveEntityPacket(entityId, IEntityRemovalConditions.REMOVE_NEVER_JOINED));
        }
    }

    /**
     * Sets a player's ready status
     */
    private void receivePlayerDone(Packet pkt, int connIndex) {
        boolean ready = pkt.getBooleanValue(0);
        Player player = getPlayer(connIndex);
        if (null != player) {
            player.setDone(ready);
        }
    }

    private void receiveInitiativeRerollRequest(Packet pkt, int connIndex) {
        Player player = getPlayer(connIndex);
        if (IGame.Phase.PHASE_INITIATIVE_REPORT != game.getPhase()) {
            StringBuffer message = new StringBuffer();
            if (null == player) {
                message.append("Player #").append(connIndex);
            } else {
                message.append(player.getName());
            }
            message.append(" is not allowed to ask for a reroll at this time.");
            System.err.println(message.toString());
            sendServerChat(message.toString());
            return;
        }
        if (game.hasTacticalGenius(player)) {
            game.addInitiativeRerollRequest(game.getTeamForPlayer(player));
        }
        if (null != player) {
            player.setDone(true);
        }
        checkReady();
    }

    /**
     * Sets game options, providing that the player has specified the password
     * correctly.
     * 
     * @return true if any options have been successfully changed.
     */
    private boolean receiveGameOptions(Packet packet, int connId) {
        Player player = game.getPlayer(connId);
        // Check player
        if (null == player) {
            System.err.print("Server does not recognize player at connection ");
            System.err.println(connId);
            return false;
        }

        // check password
        if (password != null && password.length() > 0 && !password.equals(packet.getObject(0))) {
            sendServerChat(connId, "The password you specified to change game options is incorrect.");
            return false;
        }

        if ( game.getPhase().isDuringOrAfter(Phase.PHASE_DEPLOYMENT) ){
            return false;
        }
        
        int changed = 0;

        for (Enumeration<?> i = ((Vector<?>) packet.getObject(1)).elements(); i.hasMoreElements();) {
            IBasicOption option = (IBasicOption) i.nextElement();
            IOption originalOption = game.getOptions().getOption(option.getName());

            if (originalOption == null) {
                continue;
            }

            StringBuffer message = new StringBuffer();
            message.append("Player ").append(player.getName()).append(" changed option \"").append(originalOption.getDisplayableName()).append("\" to ").append(option.getValue().toString()).append('.');
            sendServerChat(message.toString());
            originalOption.setValue(option.getValue());
            changed++;
        }

        // Set proper RNG
        Compute.setRNG(game.getOptions().intOption("rng_type"));
        
        if (changed > 0) {
            for (Enumeration<Entity> entities = game.getEntities(); entities.hasMoreElements(); ){
                Entity en = entities.nextElement();
                en.setGameOptions();
            }
            entityAllUpdate();
            return true;
        }
        return false;
    }

    /**
     * Performs the additional processing of the received options after the the
     * <code>receiveGameOptions<code> done its job; should be called after
     * <code>receiveGameOptions<code> only if the <code>receiveGameOptions<code>
     * returned <code>true</code>
     * @param packet
     * @param connId
     */
    private void receiveGameOptionsAux(Packet packet, int connId) {

        for (Enumeration<?> i = ((Vector<?>) packet.getObject(1)).elements(); i.hasMoreElements();) {
            IBasicOption option = (IBasicOption) i.nextElement();
            IOption originalOption = game.getOptions().getOption(option.getName());
            if (originalOption != null) {
                if ("maps_include_subdir".equals(originalOption.getName())) {
                    mapSettings.setBoardsAvailableVector(scanForBoards(mapSettings.getBoardWidth(), mapSettings.getBoardHeight()));
                    mapSettings.removeUnavailable();
                    mapSettings.setNullBoards(DEFAULT_BOARD);
                    send(createMapSettingsPacket());
                }
            }
        }

    }

    /**
     * Sends out all player info to the specified connection
     */
    private void transmitAllPlayerConnects(int connId) {
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();

            send(connId, createPlayerConnectPacket(player.getId()));
        }
    }

    /**
     * Creates a packet informing that the player has connected
     */
    private Packet createPlayerConnectPacket(int playerId) {
        final Object[] data = new Object[2];
        data[0] = new Integer(playerId);
        data[1] = getPlayer(playerId);
        return new Packet(Packet.COMMAND_PLAYER_ADD, data);
    }

    /**
     * Creates a packet containing the player info, for update
     */
    private Packet createPlayerUpdatePacket(int playerId) {
        final Object[] data = new Object[2];
        data[0] = new Integer(playerId);
        data[1] = getPlayer(playerId);
        return new Packet(Packet.COMMAND_PLAYER_UPDATE, data);
    }

    /**
     * Sends out the player info updates for all players to all connections
     */
    private void transmitAllPlayerUpdates() {
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();
            if (null != player) {
                send(createPlayerUpdatePacket(player.getId()));
            }
        }
    }

    /**
     * Sends out the player ready stats for all players to all connections
     */
    private void transmitAllPlayerDones() {
        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements();) {
            final Player player = i.nextElement();

            send(createPlayerDonePacket(player.getId()));
        }
    }

    /**
     * Creates a packet containing the player ready status
     */
    private Packet createPlayerDonePacket(int playerId) {
        Object[] data = new Object[2];
        data[0] = new Integer(playerId);
        data[1] = Boolean.valueOf(getPlayer(playerId).isDone());
        return new Packet(Packet.COMMAND_PLAYER_READY, data);
    }

    /** Creates a packet containing the current turn vector */
    private Packet createTurnVectorPacket() {
        return new Packet(Packet.COMMAND_SENDING_TURNS, game.getTurnVector());
    }

    /** Creates a packet containing the current turn index */
    private Packet createTurnIndexPacket() {
        return new Packet(Packet.COMMAND_TURN, new Integer(game.getTurnIndex()));
    }

    /**
     * Creates a packet containing the map settings
     */
    private Packet createMapSettingsPacket() {
        return new Packet(Packet.COMMAND_SENDING_MAP_SETTINGS, mapSettings);
    }

    /**
     * Creates a packet containing the planetary conditions
     */
    private Packet createPlanetaryConditionsPacket() {
        return new Packet(Packet.COMMAND_SENDING_PLANETARY_CONDITIONS, game.getPlanetaryConditions());
    }

    /**
     * Creates a packet containing temporary map settings as a response to a
     * client query
     */
    private Packet createMapQueryPacket(MapSettings temp) {
        return new Packet(Packet.COMMAND_QUERY_MAP_SETTINGS, temp);
    }

    /**
     * Creates a packet containing the game settingss
     */
    private Packet createGameSettingsPacket() {
        return new Packet(Packet.COMMAND_SENDING_GAME_SETTINGS, game.getOptions());
    }

    /**
     * Creates a packet containing the game board
     */
    private Packet createBoardPacket() {
        return new Packet(Packet.COMMAND_SENDING_BOARD, game.getBoard());
    }

    /**
     * Creates a packet containing a single entity, for update
     */
    private Packet createEntityPacket(int entityId, Vector<UnitLocation> movePath) {
        final Entity entity = game.getEntity(entityId);
        final Object[] data = new Object[3];
        data[0] = new Integer(entityId);
        data[1] = entity;
        data[2] = movePath;
        return new Packet(Packet.COMMAND_ENTITY_UPDATE, data);
    }

    /**
     * Creates a packet containing a Vector of Reports
     */
    private Packet createReportPacket(Player p) {
        // When the final report is created, MM sends a null player to create
        // the
        // report. This will handle that issue.
        if (p == null || !doBlind())
            return new Packet(Packet.COMMAND_SENDING_REPORTS, vPhaseReport);
        return new Packet(Packet.COMMAND_SENDING_REPORTS, p.getTurnReport());
    }

    /**
     * Creates a packet containing a Vector of special Reports which needs to be
     * sent during a phase that is not a report phase.
     */
    private Packet createSpecialReportPacket() {
        return new Packet(Packet.COMMAND_SENDING_REPORTS_SPECIAL, vPhaseReport.clone());
    }

    /**
     * Creates a packet containing a Vector of Reports that represent a Tactical
     * Genius re-roll request which needs to update a current phase's report.
     */
    private Packet createTacticalGeniusReportPacket() {
        return new Packet(Packet.COMMAND_SENDING_REPORTS_TACTICAL_GENIUS, vPhaseReport.clone());
    }

    /**
     * Creates a packet containing all the round reports
     */
    private Packet createAllReportsPacket(Player p) {
        return new Packet(Packet.COMMAND_SENDING_REPORTS_ALL, filterPastReports(game.getAllReports(), p));
    }

    /**
     * Creates a packet containing all current entities
     */
    private Packet createEntitiesPacket() {
        return new Packet(Packet.COMMAND_SENDING_ENTITIES, game.getEntitiesVector());
    }

    /**
     * Creates a packet containing all current and out-of-game entities
     */
    private Packet createFullEntitiesPacket() {
        final Object[] data = new Object[2];
        data[0] = game.getEntitiesVector();
        data[1] = game.getOutOfGameEntitiesVector();
        return new Packet(Packet.COMMAND_SENDING_ENTITIES, data);
    }

    /**
     * Creates a packet containing all entities visible to the player in a blind
     * game
     */
    private Packet createFilteredEntitiesPacket(Player p) {
        return new Packet(Packet.COMMAND_SENDING_ENTITIES, filterEntities(p, game.getEntitiesVector()));
    }

    /**
     * Creates a packet containing all entities, including wrecks, visible to
     * the player in a blind game
     */
    private Packet createFilteredFullEntitiesPacket(Player p) {
        final Object[] data = new Object[2];
        data[0] = filterEntities(p, game.getEntitiesVector());
        data[1] = game.getOutOfGameEntitiesVector();
        return new Packet(Packet.COMMAND_SENDING_ENTITIES, data);
    }

    /**
     * Creates a packet detailing the addition of an entity
     */
    private Packet createAddEntityPacket(int entityId) {
        final Entity entity = game.getEntity(entityId);
        final Object[] data = new Object[2];
        data[0] = new Integer(entityId);
        data[1] = entity;
        return new Packet(Packet.COMMAND_ENTITY_ADD, data);
    }

    /**
     * Creates a packet detailing the removal of an entity. Maintained for
     * backwards compatability.
     * 
     * @param entityId -
     *            the <code>int</code> ID of the entity being removed.
     * @return A <code>Packet</code> to be sent to clients.
     */
    private Packet createRemoveEntityPacket(int entityId) {
        return createRemoveEntityPacket(entityId, IEntityRemovalConditions.REMOVE_SALVAGEABLE);
    }

    /**
     * Creates a packet detailing the removal of an entity.
     * 
     * @param entityId -
     *            the <code>int</code> ID of the entity being removed.
     * @param condition -
     *            the <code>int</code> condition the unit was in. This value
     *            must be one of constants in
     *            <code>IEntityRemovalConditions</code>, or an
     *            <code>IllegalArgumentException</code> will be thrown.
     * @return A <code>Packet</code> to be sent to clients.
     */
    private Packet createRemoveEntityPacket(int entityId, int condition) {
        if (condition != IEntityRemovalConditions.REMOVE_UNKNOWN && condition != IEntityRemovalConditions.REMOVE_IN_RETREAT && condition != IEntityRemovalConditions.REMOVE_PUSHED && condition != IEntityRemovalConditions.REMOVE_SALVAGEABLE && condition != IEntityRemovalConditions.REMOVE_EJECTED && condition != IEntityRemovalConditions.REMOVE_CAPTURED && condition != IEntityRemovalConditions.REMOVE_DEVASTATED && condition != IEntityRemovalConditions.REMOVE_NEVER_JOINED) {
            throw new IllegalArgumentException("Unknown unit condition: " + condition);
        }
        Object[] array = new Object[2];
        array[0] = new Integer(entityId);
        array[1] = new Integer(condition);
        return new Packet(Packet.COMMAND_ENTITY_REMOVE, array);
    }

    /**
     * Creates a packet indicating end of game, including detailed unit status
     */
    private Packet createEndOfGamePacket() {
        Object[] array = new Object[3];
        array[0] = getDetailedVictoryReport();
        array[1] = new Integer(game.getVictoryPlayerId());
        array[2] = new Integer(game.getVictoryTeam());
        return new Packet(Packet.COMMAND_END_OF_GAME, array);
    }

    /**
     * Transmits a chat message to all players
     */
    public void sendChat(int connId, String origin, String message) {
        send(connId, new Packet(Packet.COMMAND_CHAT, origin + ": " + message));
    }

    /**
     * Transmits a chat message to all players
     */
    private void sendChat(String origin, String message) {
        String chat = origin + ": " + message;
        send(new Packet(Packet.COMMAND_CHAT, chat));
    }

    public void sendServerChat(int connId, String message) {
        sendChat(connId, "***Server", message);
    }

    public void sendServerChat(String message) {
        sendChat("***Server", message);
    }

    /**
     * Creates a packet containing a hex, and the coordinates it goes at.
     */
    private Packet createHexChangePacket(Coords coords, IHex hex) {
        final Object[] data = new Object[2];
        data[0] = coords;
        data[1] = hex;
        return new Packet(Packet.COMMAND_CHANGE_HEX, data);
    }

    /**
     * Sends notification to clients that the specified hex has changed.
     */
    public void sendChangedHex(Coords coords) {
        send(createHexChangePacket(coords, game.getBoard().getHex(coords)));
    }
    
    /**
     * Creates a packet containing a vector of mines.
     */
    private Packet createMineChangePacket(Coords coords) {
        return new Packet(Packet.COMMAND_UPDATE_MINEFIELDS, game.getMinefields(coords));
    }
    
    /**
     * Sends notification to clients that the specified hex has changed.
     */
    public void sendChangedMines(Coords coords) {
        send(createMineChangePacket(coords));
    }

    public void sendVisibilityIndicator(Entity e) {
        final Object[] data = new Object[3];
        data[0] = new Integer(e.getId());
        data[1] = Boolean.valueOf(e.isSeenByEnemy());
        data[2] = Boolean.valueOf(e.isVisibleToEnemy());
        send(new Packet(Packet.COMMAND_ENTITY_VISIBILITY_INDICATOR, data));
    }

    /**
     * Creates a packet for an attack
     */
    private Packet createAttackPacket(Vector<?> vector, int charges) {
        final Object[] data = new Object[2];
        data[0] = vector;
        data[1] = new Integer(charges);
        return new Packet(Packet.COMMAND_ENTITY_ATTACK, data);
    }

    /**
     * Creates a packet for an attack
     */
    private Packet createAttackPacket(EntityAction ea, int charge) {
        Vector<EntityAction> vector = new Vector<EntityAction>(1);
        vector.addElement(ea);
        Object[] data = new Object[2];
        data[0] = vector;
        data[1] = new Integer(charge);
        return new Packet(Packet.COMMAND_ENTITY_ATTACK, data);
    }

    /**
     * 
     */
    private Packet createSpecialHexDisplayPacket(int toPlayer) {
        Hashtable<Coords, Collection<SpecialHexDisplay>> shdTable = game.getBoard().getSpecialHexDisplayTable();
        Hashtable<Coords, Collection<SpecialHexDisplay>> shdTable2 = new Hashtable<Coords, Collection<SpecialHexDisplay>>();
        LinkedList<SpecialHexDisplay> tempList = null;
        Player player = getPlayer(toPlayer);
        if (player != null) {
            final String playerName = getPlayer(toPlayer).getName();

            for (Coords coord : shdTable.keySet()) {
                tempList = new LinkedList<SpecialHexDisplay>();
                for (SpecialHexDisplay shd : shdTable.get(coord)) {
                    if (!shd.isObscured() || shd.isOwner(playerName)) {
                        tempList.add(0, shd);
                    }
                }
                if (!tempList.isEmpty()) {
                    shdTable2.put(coord, tempList);
                }
            }
        }
        return new Packet(Packet.COMMAND_SENDING_SPECIAL_HEX_DISPLAY, shdTable2);
    }

    /**
     * Creates a packet containing offboard artillery attacks
     */
    private Packet createArtilleryPacket(Player p) {
        Vector<ArtilleryAttackAction> v = new Vector<ArtilleryAttackAction>();
        int team = p.getTeam();
        for (Enumeration<AttackHandler> i = game.getAttacks(); i.hasMoreElements();) {
            WeaponHandler wh = (WeaponHandler) i.nextElement();
            if (wh.waa instanceof ArtilleryAttackAction) {
                ArtilleryAttackAction aaa = (ArtilleryAttackAction) wh.waa;
                if (aaa.getPlayerId() == p.getId() || (team != Player.TEAM_NONE && team == game.getPlayer(aaa.getPlayerId()).getTeam()) || p.getSeeAll()) {
                    v.addElement(aaa);
                }
            }
        }
        return new Packet(Packet.COMMAND_SENDING_ARTILLERYATTACKS, v);
    }

    /**
     * Creates a packet containing flares
     */
    private Packet createFlarePacket() {

        return new Packet(Packet.COMMAND_SENDING_FLARES, game.getFlares());
    }

    /**
     * Send a packet to all connected clients.
     */
    private void send(Packet packet) {
        if (connections == null) {
            return;
        }
        for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            conn.send(packet);
        }
    }

    private void sendReport() {
        sendReport(false);
    }

    /**
     * Send the round report to all connected clients.
     */
    private void sendReport(boolean tacticalGeniusReport) {
        if (connections == null) {
            return;
        }

        for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
            final IConnection conn = i.nextElement();
            Player p = game.getPlayer(conn.getId());
            Packet packet;
            if (tacticalGeniusReport)
                packet = createTacticalGeniusReportPacket();
            else
                packet = createReportPacket(p);
            conn.send(packet);
        }
    }

    /**
     * Send a packet to a specific connection.
     */
    private void send(int connId, Packet packet) {
        if (getClient(connId) != null) {
            getClient(connId).send(packet);
        } else {
            // What should we do if we've lost this client?
            // For now, nothing.
        }
    }

    /**
     * Send a packet to a pending connection
     */
    private void sendToPending(int connId, Packet packet) {
        if (getPendingConnection(connId) != null) {
            getPendingConnection(connId).send(packet);
        } else {
            // What should we do if we've lost this client?
            // For now, nothing.
        }
    }

    /**
     * Process an in-game command
     */
    private void processCommand(int connId, String commandString) {
        String[] args;
        String commandName;
        // all tokens are read as strings; if they're numbers, string-ize 'em.
        // replaced the tokenizer with the split function.
        args = commandString.split("\\s+");

        // figure out which command this is
        commandName = args[0].substring(1);

        // process it
        ServerCommand command = getCommand(commandName);
        if (command != null) {
            command.run(connId, args);
        } else {
            sendServerChat(connId, "Command not recognized.  Type /help for a list of commands.");
        }
    }

    // Easter eggs. Happy April Fool's Day!!
    private static final String DUNE_CALL = "They tried and failed?";

    private static final String DUNE_RESPONSE = "They tried and died!";

    private static final String STAR_WARS_CALL = "I'd just as soon kiss a Wookiee.";

    private static final String STAR_WARS_RESPONSE = "I can arrange that!";

    private static final String INVADER_ZIM_CALL = "What does the G stand for?";

    private static final String INVADER_ZIM_RESPONSE = "I dont know.";
    
    /**
     * Process a packet from a connection.
     * 
     * @param connId -
     *            the <code>int</code> ID the connection that received the
     *            packet.
     * @param packet -
     *            the <code>Packet</code> to be processed.
     */
    protected synchronized void handle(int connId, Packet packet) {
        Player player = game.getPlayer(connId);
        // Check player. Please note, the connection may be pending.
        if (null == player && null == getPendingConnection(connId)) {
            System.err.print("Server does not recognize player at connection ");
            System.err.println(connId);
            return;
        }

        // System.out.println("s(" + cn + "): received command");
        if (packet == null) {
            System.out.println("server.connection.handle: got null packet");
            return;
        }
        // act on it
        switch (packet.getCommand()) {
        case Packet.COMMAND_CLOSE_CONNECTION:
            // We have a client going down!
            IConnection c = getConnection(connId);
            if (c != null) {
                c.close();
            }
            break;
        case Packet.COMMAND_CLIENT_NAME:
            receivePlayerName(packet, connId);
            break;
        case Packet.COMMAND_PLAYER_UPDATE:
            receivePlayerInfo(packet, connId);
            validatePlayerInfo(connId);
            send(createPlayerUpdatePacket(connId));
            break;
        case Packet.COMMAND_PLAYER_READY:
            receivePlayerDone(packet, connId);
            send(createPlayerDonePacket(connId));
            checkReady();
            break;
        case Packet.COMMAND_REROLL_INITIATIVE:
            receiveInitiativeRerollRequest(packet, connId);
            send(createPlayerDonePacket(connId));
            break;
        case Packet.COMMAND_CHAT:
            String chat = (String) packet.getObject(0);
            if (chat.startsWith("/")) {
                processCommand(connId, chat);
            } else {
                sendChat(player.getName(), chat);
            }
            // Easter eggs. Happy April Fool's Day!!
            if (DUNE_CALL.equalsIgnoreCase(chat)) {
                sendServerChat(DUNE_RESPONSE);
            } else if (STAR_WARS_CALL.equalsIgnoreCase(chat)) {
                sendServerChat(STAR_WARS_RESPONSE);
            } else if ( INVADER_ZIM_CALL.equalsIgnoreCase(chat) ){
                sendServerChat(INVADER_ZIM_RESPONSE);
            }
            break;
        case Packet.COMMAND_ENTITY_MOVE:
            receiveMovement(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_DEPLOY:
            receiveDeployment(packet, connId);
            break;
        case Packet.COMMAND_DEPLOY_MINEFIELDS:
            receiveDeployMinefields(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_ATTACK:
            receiveAttack(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_ADD:
            receiveEntityAdd(packet, connId);
            resetPlayersDone();
            transmitAllPlayerDones();
            break;
        case Packet.COMMAND_ENTITY_UPDATE:
            receiveEntityUpdate(packet, connId);
            resetPlayersDone();
            transmitAllPlayerDones();
            break;
        case Packet.COMMAND_ENTITY_MODECHANGE:
            receiveEntityModeChange(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_SYSTEMMODECHANGE:
            receiveEntitySystemModeChange(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_AMMOCHANGE:
            receiveEntityAmmoChange(packet, connId);
            break;
        case Packet.COMMAND_ENTITY_REMOVE:
            receiveEntityDelete(packet, connId);
            resetPlayersDone();
            transmitAllPlayerDones();
            break;
        case Packet.COMMAND_SENDING_GAME_SETTINGS:
            if (receiveGameOptions(packet, connId)) {
                resetPlayersDone();
                transmitAllPlayerDones();
                send(createGameSettingsPacket());
                receiveGameOptionsAux(packet, connId);
            }
            break;
        case Packet.COMMAND_SENDING_MAP_SETTINGS:
            if ( game.getPhase().isBefore(Phase.PHASE_DEPLOYMENT)){
                MapSettings newSettings = (MapSettings) packet.getObject(0);
                if (!mapSettings.equalMapGenParameters(newSettings)) {
                    sendServerChat("Player " + player.getName() + " changed mapsettings");
                }
                mapSettings = newSettings;
                newSettings = null;
                mapSettings.replaceBoardWithRandom(MapSettings.BOARD_RANDOM);
                resetPlayersDone();
                transmitAllPlayerDones();
                send(createMapSettingsPacket());
            }
            break;
        case Packet.COMMAND_SENDING_PLANETARY_CONDITIONS:
            // MapSettings newSettings = (MapSettings) packet.getObject(0);
            if ( game.getPhase().isBefore(Phase.PHASE_DEPLOYMENT) ){
                PlanetaryConditions conditions = (PlanetaryConditions) packet.getObject(0);
                sendServerChat("Player " + player.getName() + " changed planetary conditions");
                game.setPlanetaryConditions(conditions);
                resetPlayersDone();
                transmitAllPlayerDones();
                send(createPlanetaryConditionsPacket());
            }
            break;
        case Packet.COMMAND_QUERY_MAP_SETTINGS:
            MapSettings temp = (MapSettings) packet.getObject(0);
            temp.setBoardsAvailableVector(scanForBoards(temp.getBoardWidth(), temp.getBoardHeight()));
            temp.removeUnavailable();
            temp.setNullBoards(DEFAULT_BOARD);
            temp.replaceBoardWithRandom(MapSettings.BOARD_RANDOM);
            temp.removeUnavailable();
            // if still only nulls left, use BOARD_GENERATED
            if (temp.getBoardsSelected().next() == null)
                temp.setNullBoards((MapSettings.BOARD_GENERATED));
            send(connId, createMapQueryPacket(temp));
            break;
        case Packet.COMMAND_UNLOAD_STRANDED:
            receiveUnloadStranded(packet, connId);
            break;
        case Packet.COMMAND_SET_ARTYAUTOHITHEXES:
            receiveArtyAutoHitHexes(packet, connId);
        case Packet.COMMAND_CUSTOM_INITIATIVE:
            receiveCustomInit(packet, connId);
            resetPlayersDone();
            transmitAllPlayerDones();
            break;
        case Packet.COMMAND_LOAD_GAME:
            if(loadGame((File) packet.getObject(0))) {
                resetConnections();
            }
        }
    }

    /**
     * Listen for incoming clients.
     */
    public void run() {
        Thread currentThread = Thread.currentThread();
        System.out.println("s: listening for clients...");
        HashSet<IConnection> toUpdate = new HashSet<IConnection>();
        while (connector == currentThread) {
            try {
                Socket s = serverSocket.accept();

                int id = getFreeConnectionId();
                System.out.println("s: accepting player connection #" + id + " ...");

                IConnection c = ConnectionFactory.getInstance().createServerConnection(s, id);
                c.addConnectionListener(connectionListener);
                c.open();
                connectionsPending.addElement(c);

                greeting(id);
                ConnectionWatchdog w = new ConnectionWatchdog(this, id);
                timer.schedule(w, 1000, 500);
            } catch (InterruptedIOException iioe) {
                // ignore , just SOTimeout blowing..
            } catch (IOException ex) {

            }
            /* update all connections */
            toUpdate.clear();
            toUpdate.addAll(connections);
            toUpdate.addAll(connectionsPending);
            /* process stuff */
            Iterator<IConnection> it = toUpdate.iterator();
            while (it.hasNext())
                it.next().update();
            /* then make sure stuff is sent away */
            it = toUpdate.iterator();
            while (it.hasNext())
                it.next().flush();
        }
    }

    /**
     * Makes one slot of inferno ammo, determined by certain rules, explode on a
     * mech.
     * 
     * @param entity
     *            The <code>Entity</code> that should suffer an inferno ammo
     *            explosion.
     */
    private Vector<Report> explodeInfernoAmmoFromHeat(Entity entity) {
        int damage = 0;
        int rack = 0;
        int boomloc = -1;
        int boomslot = -1;
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // Find the most destructive Inferno ammo.
        for (int j = 0; j < entity.locations(); j++) {
            for (int k = 0; k < entity.getNumberOfCriticals(j); k++) {
                CriticalSlot cs = entity.getCritical(j, k);
                // Ignore empty, destroyed, hit, and structure slots.
                if (cs == null || cs.isDestroyed() || cs.isHit() || cs.getType() != CriticalSlot.TYPE_EQUIPMENT) {
                    continue;
                }
                // Ignore everything but weapons slots.
                Mounted mounted = entity.getEquipment(entity.getCritical(j, k).getIndex());
                if (!(mounted.getType() instanceof AmmoType)) {
                    continue;
                }
                // Ignore everything but Inferno ammo.
                AmmoType atype = (AmmoType) mounted.getType();
                if (!atype.isExplosive() || atype.getMunitionType() != AmmoType.M_INFERNO) {
                    continue;
                }
                // Find the most destructive undamaged ammo.
                // BMRr, pg. 48, compare one rack's
                // damage. Ties go to most rounds.
                int newRack = atype.getDamagePerShot() * atype.getRackSize();
                int newDamage = mounted.getExplosionDamage();
                if (!mounted.isHit() && (rack < newRack || rack == newRack && damage < newDamage)) {
                    rack = newRack;
                    damage = newDamage;
                    boomloc = j;
                    boomslot = k;
                }
            }
        }
        // Did we find anything to explode?
        if (boomloc != -1 && boomslot != -1) {
            CriticalSlot slot = entity.getCritical(boomloc, boomslot);
            slot.setHit(true);
            Mounted equip = entity.getEquipment(slot.getIndex());
            equip.setHit(true);
            // We've allocated heatBuildup to heat in resolveHeat(),
            // so need to add to the entity's heat instead.
            entity.heat += Math.min(equip.getExplosionDamage(), 30);
            vDesc.addAll(explodeEquipment(entity, boomloc, boomslot));
            r = new Report(5155);
            r.indent();
            r.subject = entity.getId();
            r.add(entity.heat);
            vDesc.addElement(r);
            entity.heatBuildup = 0;
        } else { // no ammo to explode
            r = new Report(5160);
            r.indent();
            r.subject = entity.getId();
            vDesc.addElement(r);
        }
        return vDesc;
    }

    /**
     * checks for unintended exposion of heavy industrial zone hex and applies damage
     * to entities occupying the hex
     */
    public void checkExplodeIndustrialZone(Coords c, Vector<Report> vDesc) {
        Report r;
        IHex hex = game.getBoard().getHex(c);
        if(null == hex)
            return;
        
        if(!hex.containsTerrain(Terrains.INDUSTRIAL))
            return;
        
        
        r = new Report(3590, Report.PUBLIC);
        r.add(c.getBoardNum());
        r.indent(2);
        int effect = Compute.d6(2);
        r.add(8);
        r.add(effect);
        if(effect > 7) {
            r.choose(true);
            r.newlines = 0;
            vDesc.add(r);
            boolean onFire = false;
            boolean powerLine = false;
            boolean minorExp = false;
            boolean elecExp = false;
            boolean majorExp = false;
            if(effect == 8) {
                onFire = true;
                r = new Report(3600, Report.PUBLIC);
                r.newlines = 0;
                vDesc.add(r);
            } else if(effect == 9) {
                powerLine = true;
                r = new Report(3605, Report.PUBLIC);
                r.newlines = 0;
                vDesc.add(r);
            } else if (effect == 10) {
                minorExp = true;
                onFire = true;
                r = new Report(3610, Report.PUBLIC);
                r.newlines  = 0;
                vDesc.add(r);
            } else if (effect == 11) {
                elecExp = true;
                r = new Report(3615, Report.PUBLIC);
                r.newlines = 0;
                vDesc.add(r);
            } else {
                onFire = true;
                majorExp = true;
                r = new Report(3620, Report.PUBLIC);
                r.newlines = 0;
                vDesc.add(r);
            }
            //apply damage here
            if(powerLine || minorExp || elecExp || majorExp) {
                //cycle through the entities in the hex and apply damage
                for (Enumeration<Entity> e = game.getEntities(c); e.hasMoreElements();) {
                    Entity en = e.nextElement();
                    int damage = 3;
                    if(minorExp)
                        damage = 5;
                    if(elecExp)
                        damage = Compute.d6(1) + 3;
                    if(majorExp)
                        damage = Compute.d6(2);
                    HitData hit = en.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    if(en instanceof BattleArmor) {
                        //ugly - I have to apply damage to each trooper separately
                        for(int loc = 0; loc < en.locations(); loc++) {
                            if(IArmorState.ARMOR_NA != en.getInternal(loc)
                                    && IArmorState.ARMOR_DESTROYED != en.getInternal(loc)
                                    && (IArmorState.ARMOR_DOOMED != en.getInternal(loc))) {
                                vDesc.addAll( damageEntity(en, new HitData(loc), damage) );
                            }
                        } 
                    } else {
                        vDesc.addAll( damageEntity(en, hit, damage) );
                    }
                    if(majorExp)
                        //lets pretend that the infernos came from the entity itself (should give us side_front)
                        vDesc.addAll( deliverInfernoMissiles(en, en, Compute.d6(2)) );
                }
            }
            Report.addNewline(vDesc);
            if(onFire && !hex.containsTerrain(Terrains.FIRE)) {               
                this.ignite(c, false, vDesc);
            }
        } else {
            //report no explosion
            r.choose(false);
            vDesc.add(r);
        }
    }
    
    /**
     * Determine the results of an entity moving through a wall of a building
     * after having moved a certain distance. This gets called when a Mech or a
     * Tank enters a building, leaves a building, or travels from one hex to
     * another inside a multi-hex building.
     * 
     * @param entity -
     *            the <code>Entity</code> that passed through a wall. Don't
     *            pass <code>Infantry</code> units to this method.
     * @param bldg -
     *            the <code>Building</code> the entity is passing through.
     * @param lastPos -
     *            the <code>Coords</code> of the hex the entity is exiting.
     * @param curPos -
     *            the <code>Coords</code> of the hex the entity is entering
     * @param distance -
     *            the <code>int</code> number of hexes the entity has moved
     *            already this phase.
     * @param why -
     *            the <code>String</code> explanation for this action.
     * @param backwards -
     *            the <code>boolean</code> indicating if the entity is
     *            entering the hex backwards
     * @param entering -
     *            a <code>boolean</code> if the entity is entering or exiting a building
     * @return <code>true</code> if the building collapses due to overloading.
     */
    private boolean passBuildingWall(Entity entity, Building bldg, Coords lastPos, Coords curPos, int distance, String why, boolean backwards, int overallMoveType, boolean entering) {

        Report r;

        // Need to roll based on building type.
        PilotingRollData psr = entity.rollMovementInBuilding(bldg, distance, why, overallMoveType);

        // Did the entity make the roll?
        if (0 < doSkillCheckWhileMoving(entity, lastPos, curPos, psr, false)) {

            // Divide the building's current CF by 10, round up.
            int damage = (int) Math.ceil(bldg.getCurrentCF(entering?curPos:lastPos) / 10.0);

            // It is possible that the unit takes no damage.
            if (damage == 0) {
                r = new Report(6440);
                r.add(entity.getDisplayName());
                r.subject = entity.getId();
                r.indent(2);
                addReport(r);
            } else {
                // TW, pg. 268: if unit moves forward, damage from front,
                // if backwards, damage from rear.
                int side = ToHitData.SIDE_FRONT;
                if (backwards)
                    side = ToHitData.SIDE_REAR;
                HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, side);
                hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                addReport(damageEntity(entity, hit, damage));
            }
        }

        // Damage the building. The CF can never drop below 0.
        int toBldg = (int) Math.ceil(entity.getWeight() / 10.0);
        int curCF = bldg.getCurrentCF(entering?curPos:lastPos);
        curCF -= Math.min(curCF, toBldg);
        bldg.setCurrentCF(curCF, entering?curPos:lastPos);

        // Apply the correct amount of damage to infantry in the building.
        // ASSUMPTION: We inflict toBldg damage to infantry and
        // not the amount to bring building to 0 CF.
        damageInfantryIn(bldg, toBldg, entering?curPos:lastPos);

        return checkBuildingCollapseWhileMoving(bldg, entity, entering?curPos:lastPos);
    }

    /**
     * check if a building collapes because of a moving entity
     * 
     * @param bldg
     *            the <code>Building</code>
     * @param entity
     *            the <code>Entity</code>
     * @param curPos
     *            the <coode>Coords</code> of the position of the entity
     * @return a <code>boolean/code> value indicating if the building collapses
     */
    private boolean checkBuildingCollapseWhileMoving(Building bldg, Entity entity, Coords curPos) {
        Coords oldPos = entity.getPosition();
        // Count the moving entity in its current position, not
        // its pre-move postition. Be sure to handle nulls.
        entity.setPosition(curPos);

        // Get the position map of all entities in the game.
        Hashtable<Coords, Vector<Entity>> positionMap = game.getPositionMap();

        // Check for collapse of this building due to overloading, and return.
        boolean rv = checkForCollapse(bldg, positionMap, curPos);

        // If the entity was not displaced and didnt fall, move it back where it
        // was
        if (curPos.equals(entity.getPosition()) && !entity.isProne()) {
            entity.setPosition(oldPos);
        }

        return rv;
    }
    
    /**
     * Apply the correct amount of damage that passes on to any infantry unit in
     * the given building, based upon the amount of damage the building just
     * sustained. This amount is a percentage dictated by pg. 52 of BMRr.
     * 
     * @param bldg -
     *            the <code>Building</code> that sustained the damage.
     * @param damage -
     *            the <code>int</code> amount of damage.
     */
    public void damageInfantryIn(Building bldg, int damage, Coords hexCoords) {
        
        if ( bldg == null ) {
            return;
        }
        // Calculate the amount of damage the infantry will sustain.
        float percent = 0.0f;
        Report r;
        switch (bldg.getType()) {
        case Building.LIGHT:
            percent = 0.75f;
            break;
        case Building.MEDIUM:
            percent = 0.5f;
            break;
        case Building.HEAVY:
            percent = 0.25f;
            break;
        }

        // Round up at .5 points of damage.
        int toInf = Math.round(damage * percent);

        // Record if we find any infantry.
        boolean foundInfantry = false;

        // Walk through the entities in the game.
        Enumeration<Entity> entities = game.getEntities();
        while (entities.hasMoreElements()) {
            Entity entity = entities.nextElement();
            final Coords coords = entity.getPosition();

            // If the entity is infantry in the affected hex?
            if (entity instanceof Infantry && bldg.isIn(coords)
                    && coords.equals(hexCoords)) {

                // Is the entity is inside of the building
                // (instead of just on top of it)?
                if (Compute.isInBuilding(game, entity, coords)) {

                    // Report if the infantry receive no points of damage.
                    if (toInf == 0) {
                        r = new Report(6445);
                        r.subject = entity.getId();
                        addReport(r);
                    } else {
                        // Yup. Damage the entity.
                        // Battle Armor units use 5 point clusters.
                        r = new Report(6450);
                        r.indent(2);
                        r.subject = entity.getId();
                        r.add(entity.getDisplayName());
                        r.add(toInf);
                        addReport(r);
                        int remaining = toInf;
                        int cluster = toInf;
                        if (entity instanceof BattleArmor) {
                            cluster = 5;
                        }
                        while (remaining > 0) {
                            int next = Math.min(cluster, remaining);
                            HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                            addReport(damageEntity(entity, hit, next));
                            remaining -= next;
                        }
                        addReport(new Report(1210, Report.PUBLIC));
                    }

                } // End infantry-inside-building

            } // End entity-is-infantry-in-building-hex

        } // Handle the next entity

        // If we found any infantry, add a line to the phase report.
        if (foundInfantry) {
            addReport(new Report(1210, Report.PUBLIC));
        }

    } // End private void damageInfantryIn( Building, int )

    /**
     * Determine if the given building should collapse. If so, inflict the
     * appropriate amount of damage on each entity in the building and update
     * the clients. If the building does not collapse, determine if any entities
     * crash through its floor into its basement. Again, apply appropriate
     * damage.
     * 
     * @param bldg -
     *            the <code>Building</code> being checked. This value should
     *            not be <code>null</code>.
     * @param positionMap -
     *            a <code>Hashtable</code> that maps the <code>Coords</code>
     *            positions or each unit in the game to a <code>Vector</code>
     *            of <code>Entity</code>s at that position. This value should
     *            not be <code>null</code>.
     * @param coords -
     *            the <code>Coords</code> of the building hex to be checked
     * @return <code>true</code> if the building collapsed.
     */
    public boolean checkForCollapse(Building bldg, Hashtable<Coords, Vector<Entity>> positionMap, Coords coords) {

        // If the input is meaningless, do nothing and throw no exception.
        if (bldg == null || positionMap == null || positionMap.isEmpty() || coords == null || !bldg.isIn(coords) || !bldg.hasCFIn(coords)) {
            return false;
        }

        // Get the building's current CF.
        final int currentCF = bldg.getCurrentCF(coords);

        // Track all units that fall into the building's basement by Coords.
        Hashtable<Coords, Vector<Entity>> basementMap = new Hashtable<Coords, Vector<Entity>>();

        // look for a collapse.
        boolean collapse = false;

        // Get the Vector of Entities at these coordinates.
        final Vector<Entity> vector = positionMap.get(coords);

        // Are there any Entities at these coords?
        if (vector != null) {

            // How many levels does this building have in this hex?
            final IHex curHex = game.getBoard().getHex(coords);
            final int numFloors = Math.max(0, curHex.terrainLevel(Terrains.BLDG_ELEV));
            final int bridgeEl = curHex.terrainLevel(Terrains.BRIDGE_ELEV);
            int numLoads = numFloors;
            if (bridgeEl != ITerrain.LEVEL_NONE) {
                numLoads++;
            }
            if (numLoads < 1) {
                System.err.println("Check for collapse: hex " + coords.toString() + " has no bridge or building");
                return false;
            }

            // Track the load of each floor (and of the roof) separately.
            // Track all units that fall into the basement in this hex.
            // N.B. don't track the ground floor, the first floor is at
            // index 0, the second is at index 1, etc., and the roof is
            // at index (numFloors-1).
            // if bridge is present, bridge will be numFloors
            int[] loads = new int[numLoads];
            Vector<Entity> basement = new Vector<Entity>();
            for (int loop = 0; loop < numLoads; loop++) {
                loads[loop] = 0;
            }

            // Walk through the entities in this position.
            Enumeration<Entity> entities = vector.elements();
            while (!collapse && entities.hasMoreElements()) {
                final Entity entity = entities.nextElement();
                final int entityElev = entity.getElevation();

                if (entityElev != bridgeEl) {
                    // Ignore entities not *inside* the building
                    if (entityElev > numFloors) {
                        continue;
                    }
                }

                if (entity.getMovementMode() == IEntityMovementMode.HYDROFOIL || entity.getMovementMode() == IEntityMovementMode.NAVAL || entity.getMovementMode() == IEntityMovementMode.SUBMARINE || entity.getMovementMode() == IEntityMovementMode.INF_UMU) {
                    continue; // under the bridge even at same level
                }

                // Add the weight of a Mek or tank to the correct floor.
                if (entity instanceof Mech || entity instanceof Tank) {
                    int load = (int) entity.getWeight();
                    int floor = entityElev;
                    if (floor == bridgeEl) {
                        floor = numLoads;
                    }

                    // Entities on the ground floor may fall into the
                    // basement, but they won't collapse the building.
                    if (numFloors > 0 && floor == 0 && load > currentCF) {
                        basement.addElement(entity);
                    } else if (floor > 0) {

                        // If the load on any floor but the ground floor
                        // exceeds the building's current CF it collapses.
                        floor--;
                        loads[floor] += load;
                        if (loads[floor] > currentCF) {
                            collapse = true;
                        }

                    } // End not-ground-floor

                } // End increase-load

            } // Handle the next entity.

            // Track all entities that fell into the basement.
            if (!basement.isEmpty()) {
                basementMap.put(coords, basement);
            }

        } // End have-entities-here

        // Collapse the building if the flag is set.
        if (collapse) {
            Report r = new Report(2375, Report.PUBLIC);
            r.add(bldg.getName());
            addReport(r);
            collapseBuilding(bldg, positionMap, coords);
        }

        // Otherwise, did any entities fall into the basement?
        else if (!basementMap.isEmpty()) {
            // TODO: implement basements
        }

        // Return true if the building collapsed.
        return collapse;

    } // End private boolean checkForCollapse( Building, Hashtable )

    /**
     * Collapse a building hex. Inflict the appropriate amount of damage on all
     * entities in the building. Update all clients.
     * 
     * @param bldg -
     *            the <code>Building</code> that has collapsed.
     * @param positionMap -
     *            a <code>Hashtable</code> that maps the <code>Coords</code>
     *            positions or each unit in the game to a <code>Vector</code>
     *            of <code>Entity</code>s at that position. This value should
     *            not be <code>null</code>.
     * @param coords - 
     *            The <code>Coords></code> of the building hex that has collapsed
     */
    public void collapseBuilding(Building bldg, Hashtable<Coords, Vector<Entity>> positionMap, Coords coords) {
        // Loop through the hexes in the building, and apply
        // damage to all entities inside or on top of the building.
        Report r;
        final int phaseCF = bldg.getPhaseCF(coords);
        // Get the Vector of Entities at these coordinates.
        final Vector<Entity> vector = positionMap.get(coords);

        // Are there any Entities at these coords?
        if (vector != null) {

            // How many levels does this building have in this hex?
            final IHex curHex = game.getBoard().getHex(coords);
            final int bridgeEl = curHex.terrainLevel(Terrains.BRIDGE_ELEV);
            final int numFloors = Math.max(bridgeEl, curHex.terrainLevel(Terrains.BLDG_ELEV));

            // Now collapse the building in this hex, so entities fall to
            // the ground
            bldg.setCurrentCF(0, coords);
            bldg.setPhaseCF(0, coords);
            send(createCollapseBuildingPacket(coords));
            game.getBoard().collapseBuilding(coords);

            // Sort in elevation order
            Collections.sort(vector, new Comparator<Entity>() {
                public int compare(Entity a, Entity b) {
                    if (a.getElevation() > b.getElevation())
                        return -1;
                    else if (a.getElevation() > b.getElevation())
                        return 1;
                    return 0;
                }
            });
            // Walk through the entities in this position.
            Enumeration<Entity> entities = vector.elements();
            while (entities.hasMoreElements()) {
                final Entity entity = entities.nextElement();
                // final int entityElev = entity.elevationOccupied( curHex
                // );
                int floor = entity.getElevation();

                // Ignore units above the building / bridge.
                if (floor > numFloors) {
                    continue;
                }

                // Treat units on the roof like
                // they were in the top floor.
                if (floor == numFloors) {
                    floor--;
                }

                // Calculate collapse damage for this entity.
                int damage = (int) Math.ceil(phaseCF * (numFloors - floor) / 10.0);

                // Infantry suffer triple damage.
                if (entity instanceof Infantry) {
                    damage *= 3;
                }

                // Apply collapse damage the entity.
                // ASSUMPTION: use 5 point clusters.
                r = new Report(6455);
                r.indent();
                r.subject = entity.getId();
                r.add(entity.getDisplayName());
                r.add(damage);
                addReport(r);
                int remaining = damage;
                int cluster = damage;
                if (entity instanceof BattleArmor || entity instanceof Mech || entity instanceof Tank) {
                    cluster = 5;
                }
                while (remaining > 0) {
                    int next = Math.min(cluster, remaining);
                    // In
                    // www.classicbattletech.com/PDF/AskPMForumArchiveandFAQ.pdf,
                    // pg. 18, Randall Bills says that all damage from a
                    // collapsing building is applied to the front.

                    HitData hit = entity.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_FRONT);
                    hit.setGeneralDamageType(HitData.DAMAGE_PHYSICAL);
                    addReport(damageEntity(entity, hit, next));
                    remaining -= next;
                }
                addReport(new Report(1210, Report.PUBLIC));
                // TODO: Why are dead entities showing up on firing phase?

                // Do we need to handle falling Meks?
                // BMRr, pg. 53 only mentions falling BattleMechs;
                // Tanks can't be above the floor and I guess that
                // infantry don't suffer falling damage.
                // TODO: implement basements, then fall into it.
                // ASSUMPTION: we'll let the Mech fall twice: once
                // during damageEntity() above and once here.
                if (entity instanceof Mech) {
                    floor = entity.getElevation();
                    if (floor > 0 || floor == bridgeEl) {
                        // ASSUMPTION: PSR to avoid pilot damage
                        // should use mods for entity damage and
                        // 20+ points of collapse damage (if any).
                        PilotingRollData psr = entity.getBasePilotingRoll();
                        entity.addPilotingModifierForTerrain(psr, coords);
                        if (damage >= 20) {
                            psr.addModifier(1, "20+ damage");
                        }
                        addReport(doEntityFallsInto(entity, coords, coords, psr));
                    }
                    
                }
                // Update this entity.
                // ASSUMPTION: this is the correct thing to do.
                entityUpdate(entity.getId());

            } // Handle the next entity.

        } // End have-entities-here.

        else {
            // Update the building.
            bldg.setCurrentCF(0, coords);
            bldg.setPhaseCF(0, coords);
            send(createCollapseBuildingPacket(coords));
            game.getBoard().collapseBuilding(coords);
        }

    } // End private void collapseBuilding( Building )

    /**
     * Tell the clients to replace the given building with rubble hexes.
     * 
     * @param bldg -
     *            the <code>Building</code> that has collapsed.
     * @return a <code>Packet</code> for the command.
     */
    private Packet createCollapseBuildingPacket(Coords coords) {
        Vector<Coords> coordsV = new Vector<Coords>();
        coordsV.addElement(coords);
        return createCollapseBuildingPacket(coordsV);
    }

    /**
     * Tell the clients to replace the given building hexes with rubble hexes.
     * 
     * @param buildings -
     *            a <code>Vector</code> of <code>Building</code>s that has
     *            collapsed.
     * @return a <code>Packet</code> for the command.
     */
    private Packet createCollapseBuildingPacket(Vector<Coords> coords) {
        return new Packet(Packet.COMMAND_BLDG_COLLAPSE, coords);
    }

    /**
     * Tell the clients to update the CFs of the given buildings.
     * 
     * @param buildings -
     *            a <code>Vector</code> of <code>Building</code>s that need
     *            to be updated.
     * @return a <code>Packet</code> for the command.
     */
    private Packet createUpdateBuildingCFPacket(Vector<Building> buildings) {
        return new Packet(Packet.COMMAND_BLDG_UPDATE_CF, buildings);
    }

    /**
     * Apply this phase's damage to all buildings. Buildings may collapse due to
     * damage.
     */
    private void applyBuildingDamage() {

        // Walk through the buildings in the game.
        // Build the collapse and update vectors as you go.
        // N.B. never, NEVER, collapse buildings while you are walking through
        // the Enumeration from megamek.common.Board#getBuildings.
        Map<Building, Vector<Coords>> collapse = new HashMap<Building, Vector<Coords>>();
        Map<Building, Vector<Coords>> update = new HashMap<Building, Vector<Coords>>();
        Enumeration<Building> buildings = game.getBoard().getBuildings();
        while (buildings.hasMoreElements()) {
            Building bldg = buildings.nextElement();
            Vector<Coords> collapseCoords = new Vector<Coords>();
            Vector<Coords> updateCoords = new Vector<Coords>();
            Enumeration<Coords> buildingCoords = bldg.getCoords();
            while (buildingCoords.hasMoreElements()) {
                Coords coords = buildingCoords.nextElement();
                // If the CF is zero, the building should fall.
                if (bldg.getCurrentCF(coords) == 0) {
                    collapseCoords.addElement(coords);
                }
                // If the building took damage this round, update it.
                else if (bldg.getPhaseCF(coords) != bldg.getCurrentCF(coords)) {
                    bldg.setPhaseCF(bldg.getCurrentCF(coords), coords);
                    updateCoords.addElement(coords);
                }
            }
            collapse.put(bldg, collapseCoords);
            update.put(bldg, updateCoords);
        } // Handle the next building

        // If we have any buildings to collapse, collapse them now.
        if (!collapse.isEmpty()) {

            // Get the position map of all entities in the game.
            Hashtable<Coords, Vector<Entity>> positionMap = game.getPositionMap();

            // Walk through the hexes that have collapsed.
            for (Building bldg : collapse.keySet()) {
                Vector <Coords> coordsVector = collapse.get(bldg);
                for (Coords coords : coordsVector) {
                    Report r = new Report(6460, Report.PUBLIC);
                    r.add(bldg.getName());
                    addReport(r);
                    collapseBuilding(bldg, positionMap, coords);
                }
            }
        }

        // check for buildings which should collapse due to being overloaded now
        // CF is reduced
        if (!update.isEmpty()) {
            Hashtable<Coords, Vector<Entity>> positionMap = game.getPositionMap();
            for (Iterator<Building> i = update.keySet().iterator(); i.hasNext();) {
                Building bldg = i.next();
                Vector<Coords> updateCoords = update.get(bldg);
                for (Enumeration<Coords> coordsEnum = updateCoords.elements();coordsEnum.hasMoreElements();) {
                    Coords coords = coordsEnum.nextElement();
                    if (checkForCollapse(bldg, positionMap, coords)) {
                        updateCoords.remove(coords);
                    }
                }
                update.put(bldg, updateCoords);
            }
        }

        // If we have any buildings to update, send the message.
        if (!update.isEmpty()) {
            sendChangedCFBuildings(new Vector<Building>(update.keySet()));
        }
    }

    /**
     * Apply the given amount of damage to the building. Please note, this
     * method does <b>not</b> apply any damage to units inside the building,
     * update the clients, or check for the building's collapse. <p/> A default
     * message will be used to describe why the building took the damage.
     * 
     * @param bldg -
     *            the <code>Building</code> that has been damaged. This value
     *            should not be <code>null</code>, but no exception will
     *            occur.
     * @param damage -
     *            the <code>int</code> amount of damage.
     * @param coords - the <code>Coords</code> of the building hex to be damaged
     * @return a <code>Report</code> to be shown to the players.
     */
    public Vector<Report> damageBuilding(Building bldg, int damage, Coords coords) {
        final String defaultWhy = " absorbs ";
        return damageBuilding(bldg, damage, defaultWhy, coords);
    }

    /**
     * Apply the given amount of damage to the building. Please note, this
     * method does <b>not</b> apply any damage to units inside the building,
     * update the clients, or check for the building's collapse.
     * 
     * @param bldg -
     *            the <code>Building</code> that has been damaged. This value
     *            should not be <code>null</code>, but no exception will
     *            occur.
     * @param damage -
     *            the <code>int</code> amount of damage.
     * @param why -
     *            the <code>String</code> message that describes why the
     *            building took the damage.
     * @param coords - the <code>Coords</code> of the building hex to be damaged
     * @return a <code>Report</code> to be shown to the players.
     */
    public Vector<Report> damageBuilding(Building bldg, int damage, String why,
            Coords coords) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        Report r = new Report(1210, Report.PUBLIC);
        r.newlines = 0;

        // Do nothing if no building or no damage was passed.
        if (bldg != null && damage > 0) {
            int curCF = bldg.getCurrentCF(coords);
            final int startingCF = curCF;
            curCF -= Math.min(curCF, damage);
            bldg.setCurrentCF(curCF, coords);
            r.messageId = 3435;
            r.add(bldg.getName());
            r.add(why);
            r.add(damage);
            r.newlines = 1;

            // If the CF is zero, the building should fall.
            if (curCF == 0 && startingCF != 0) {
                if (bldg instanceof FuelTank) {
                    // If this is a fuel tank, we'll give it its own message.
                    r.messageId = 3441;
                    r.type = Report.PUBLIC;
                    vPhaseReport.add(r);
                    // ...But we ALSO need to blow up everything nearby.
                    // Bwahahahahaha...
                    r = new Report(3560);
                    r.type = Report.PUBLIC;
                    r.newlines = 1;
                    vPhaseReport.add(r);
                    Vector<Report> vRep = new Vector<Report>();
                    doExplosion(((FuelTank) bldg).getMagnitude(), 10, false, bldg.getCoords().nextElement(), true, vRep, null);
                    Report.indentAll(vRep, 2);
                    vPhaseReport.addAll(vRep);
                    return vPhaseReport;
                }
                if (bldg.getType() == Building.WALL)
                    r.messageId = 3442;
                else
                    r.messageId = 3440;
            }
        }
        Report.indentAll(vPhaseReport, 2);
        return vPhaseReport;
    }

    public void sendChangedCFBuildings(Vector<Building> buildings) {
        send(createUpdateBuildingCFPacket(buildings));
    }

    /**
     * Receives an packet to unload entityis stranded on immobile transports,
     * and queue all valid requests for execution. If all players that have
     * stranded entities have answered, executes the pending requests and end
     * the current turn.
     */
    private void receiveUnloadStranded(Packet packet, int connId) {
        GameTurn.UnloadStrandedTurn turn = null;
        final Player player = game.getPlayer(connId);
        int[] entityIds = (int[]) packet.getObject(0);
        Vector<Player> declared = null;
        Player other = null;
        Enumeration<EntityAction> pending = null;
        UnloadStrandedAction action = null;
        Entity entity = null;

        // Is this the right phase?
        if (game.getPhase() != IGame.Phase.PHASE_MOVEMENT) {
            System.err.println("error: server got unload stranded packet in wrong phase");
            return;
        }

        // Are we in an "unload stranded entities" turn?
        if (game.getTurn() instanceof GameTurn.UnloadStrandedTurn) {
            turn = (GameTurn.UnloadStrandedTurn) game.getTurn();
        } else {
            System.err.println("error: server got unload stranded packet out of sequence");
            StringBuffer message = new StringBuffer();
            message.append(player.getName()).append(" should not be sending 'unload stranded entity' packets at this time.");
            sendServerChat(message.toString());
            return;
        }

        // Can this player act right now?
        if (!turn.isValid(connId, game)) {
            System.err.println("error: server got unload stranded packet from invalid player");
            StringBuffer message = new StringBuffer();
            message.append(player.getName()).append(" should not be sending 'unload stranded entity' packets.");
            sendServerChat(message.toString());
            return;
        }

        // Did the player already send an 'unload' request?
        // N.B. we're also building the list of players who
        // have declared their "unload stranded" actions.
        declared = new Vector<Player>();
        pending = game.getActions();
        while (pending.hasMoreElements()) {
            action = (UnloadStrandedAction) pending.nextElement();
            if (action.getPlayerId() == connId) {
                System.err.println("error: server got multiple unload stranded packets from player");
                StringBuffer message = new StringBuffer();
                message.append(player.getName()).append(" should not send multiple 'unload stranded entity' packets.");
                sendServerChat(message.toString());
                return;
            }
            // This player is not from the current connection.
            // Record this player to determine if this turn is done.
            other = game.getPlayer(action.getPlayerId());
            if (!declared.contains(other)) {
                declared.addElement(other);
            }
        } // Handle the next "unload stranded" action.

        // Make sure the player selected at least *one* valid entity ID.
        boolean foundValid = false;
        for (int index = 0; null != entityIds && index < entityIds.length; index++) {
            entity = game.getEntity(entityIds[index]);
            if (!game.getTurn().isValid(connId, entity, game)) {
                System.err.println("error: server got unload stranded packet for invalid entity");
                StringBuffer message = new StringBuffer();
                message.append(player.getName()).append(" can not unload stranded entity ");
                if (null == entity) {
                    message.append('#').append(entityIds[index]);
                } else {
                    message.append(entity.getDisplayName());
                }
                message.append(" at this time.");
                sendServerChat(message.toString());
            } else {
                foundValid = true;
                game.addAction(new UnloadStrandedAction(connId, entityIds[index]));
            }
        }

        // Did the player choose not to unload any valid stranded entity?
        if (!foundValid) {
            game.addAction(new UnloadStrandedAction(connId, Entity.NONE));
        }

        // Either way, the connection's player has now declared.
        declared.addElement(player);

        // Are all players who are unloading entities done? Walk
        // through the turn's stranded entities, and look to see
        // if their player has finished their turn.
        entityIds = turn.getEntityIds();
        for (int index = 0; index < entityIds.length; index++) {
            entity = game.getEntity(entityIds[index]);
            other = entity.getOwner();
            if (!declared.contains(other)) {
                // At least one player still needs to declare.
                return;
            }
        }

        // All players have declared whether they're unloading stranded units.
        // Walk the list of pending actions and unload the entities.
        pending = game.getActions();
        while (pending.hasMoreElements()) {
            action = (UnloadStrandedAction) pending.nextElement();

            // Some players don't want to unload any stranded units.
            if (Entity.NONE != action.getEntityId()) {
                entity = game.getEntity(action.getEntityId());
                if (null == entity) {
                    // After all this, we couldn't find the entity!!!
                    System.err.print("error: server could not find stranded entity #");
                    System.err.print(action.getEntityId());
                    System.err.println(" to unload!!!");
                } else {
                    // Unload the entity. Get the unit's transporter.
                    Entity transporter = game.getEntity(entity.getTransportId());
                    unloadUnit(transporter, entity, transporter.getPosition(), transporter.getFacing(), transporter.getElevation());
                }
            }

        } // Handle the next pending unload action

        // Clear the list of pending units and move to the next turn.
        game.resetActions();
        changeToNextTurn();
    }

    /**
     * For all current artillery attacks in the air from this entity with this
     * weapon, clear the list of spotters. Needed because firing another round
     * before first lands voids spotting.
     * 
     * @param entityID
     *            the <code>int</code> id of the entity
     * @param weaponID
     *            the <code>int</code> id of the weapon
     */
    private void clearArtillerySpotters(int entityID, int weaponID) {
        for (Enumeration<AttackHandler> i = game.getAttacks(); i.hasMoreElements();) {
            WeaponHandler wh = (WeaponHandler) i.nextElement();
            if (wh.waa instanceof ArtilleryAttackAction && wh.waa.getEntityId() == entityID && wh.waa.getWeaponId() == weaponID) {
                ArtilleryAttackAction aaa = (ArtilleryAttackAction) wh.waa;
                aaa.setSpotterIds(null);
            }
        }
    }

    /**
     * Credits a Kill for an entity, if the target got killed.
     * 
     * @param target
     *            The <code>Entity</code> that got killed.
     * @param attacker
     *            The <code>Entity</code> that did the killing.
     */
    public void creditKill(Entity target, Entity attacker) {
        if ((target.isDoomed() || target.getCrew().isDoomed()) && !target.getGaveKillCredit()) {
            attacker.addKill(target);
        }
    }

    /**
     * pre-treats a physical attack
     * 
     * @param aaa
     *            The <code>AbstractAttackAction</code> of the physical attack
     *            to pre-treat
     * @return The <code>PhysicalResult</code> of that action, including
     *         possible damage.
     */
    private PhysicalResult preTreatPhysicalAttack(AbstractAttackAction aaa) {
        final Entity ae = game.getEntity(aaa.getEntityId());
        int damage = 0;
        PhysicalResult pr = new PhysicalResult();
        ToHitData toHit = new ToHitData();
        pr.roll = Compute.d6(2);
        pr.aaa = aaa;
        if (aaa instanceof BrushOffAttackAction) {
            BrushOffAttackAction baa = (BrushOffAttackAction) aaa;
            int arm = baa.getArm();
            baa.setArm(BrushOffAttackAction.LEFT);
            toHit = BrushOffAttackAction.toHit(game, aaa.getEntityId(), aaa.getTarget(game), BrushOffAttackAction.LEFT);
            baa.setArm(BrushOffAttackAction.RIGHT);
            pr.toHitRight = BrushOffAttackAction.toHit(game, aaa.getEntityId(), aaa.getTarget(game), BrushOffAttackAction.RIGHT);
            damage = BrushOffAttackAction.getDamageFor(ae, BrushOffAttackAction.LEFT);
            pr.damageRight = BrushOffAttackAction.getDamageFor(ae, BrushOffAttackAction.RIGHT);
            baa.setArm(arm);
            pr.rollRight = Compute.d6(2);
        } else if (aaa instanceof ChargeAttackAction) {
            ChargeAttackAction caa = (ChargeAttackAction) aaa;
            toHit = caa.toHit(game);
            if (caa.getTarget(game) instanceof Entity) {
                Entity target = (Entity) caa.getTarget(game);
                damage = ChargeAttackAction.getDamageFor(ae, target, game.getOptions().booleanOption("tacops_charge_damage"), toHit.getMoS());
            } else {
                damage = ChargeAttackAction.getDamageFor(ae);
            }
        } else if (aaa instanceof ClubAttackAction) {
            ClubAttackAction caa = (ClubAttackAction) aaa;
            toHit = caa.toHit(game);
            damage = ClubAttackAction.getDamageFor(ae, caa.getClub(), caa.getTarget(game) instanceof Infantry && !(caa.getTarget(game) instanceof BattleArmor));
        } else if (aaa instanceof DfaAttackAction) {
            DfaAttackAction daa = (DfaAttackAction) aaa;
            toHit = daa.toHit(game);
            damage = DfaAttackAction.getDamageFor(ae, daa.getTarget(game) instanceof Infantry && !(daa.getTarget(game) instanceof BattleArmor));
        } else if (aaa instanceof KickAttackAction) {
            KickAttackAction kaa = (KickAttackAction) aaa;
            toHit = kaa.toHit(game);
            damage = KickAttackAction.getDamageFor(ae, kaa.getLeg(), kaa.getTarget(game) instanceof Infantry && !(kaa.getTarget(game) instanceof BattleArmor));
        } else if (aaa instanceof ProtomechPhysicalAttackAction) {
            ProtomechPhysicalAttackAction paa = (ProtomechPhysicalAttackAction) aaa;
            toHit = paa.toHit(game);
            damage = ProtomechPhysicalAttackAction.getDamageFor(ae);
        } else if (aaa instanceof PunchAttackAction) {
            PunchAttackAction paa = (PunchAttackAction) aaa;
            int arm = paa.getArm();
            int damageRight = 0;
            paa.setArm(PunchAttackAction.LEFT);
            toHit = paa.toHit(game);
            paa.setArm(PunchAttackAction.RIGHT);
            ToHitData toHitRight = paa.toHit(game);
            damage = PunchAttackAction.getDamageFor(ae, PunchAttackAction.LEFT, paa.getTarget(game) instanceof Infantry && !(paa.getTarget(game) instanceof BattleArmor));
            damageRight = PunchAttackAction.getDamageFor(ae, PunchAttackAction.RIGHT, paa.getTarget(game) instanceof Infantry && !(paa.getTarget(game) instanceof BattleArmor));
            paa.setArm(arm);
            // If we're punching while prone (at a Tank,
            // duh), then we can only use one arm.
            if (ae.isProne()) {
                double oddsLeft = Compute.oddsAbove(toHit.getValue());
                double oddsRight = Compute.oddsAbove(toHitRight.getValue());
                // Use the best attack.
                if (oddsLeft * damage > oddsRight * damageRight) {
                    paa.setArm(PunchAttackAction.LEFT);
                } else
                    paa.setArm(PunchAttackAction.RIGHT);
            }
            pr.damageRight = damageRight;
            pr.toHitRight = toHitRight;
            pr.rollRight = Compute.d6(2);
        } else if (aaa instanceof PushAttackAction) {
            PushAttackAction paa = (PushAttackAction) aaa;
            toHit = paa.toHit(game);
        } else if (aaa instanceof TripAttackAction) {
            TripAttackAction paa = (TripAttackAction) aaa;
            toHit = paa.toHit(game);
        } else if (aaa instanceof LayExplosivesAttackAction) {
            LayExplosivesAttackAction leaa = (LayExplosivesAttackAction) aaa;
            toHit = leaa.toHit(game);
            damage = LayExplosivesAttackAction.getDamageFor(ae);
        } else if (aaa instanceof ThrashAttackAction) {
            ThrashAttackAction taa = (ThrashAttackAction) aaa;
            toHit = taa.toHit(game);
            damage = ThrashAttackAction.getDamageFor(ae);
        } else if (aaa instanceof JumpJetAttackAction) {
            JumpJetAttackAction jaa = (JumpJetAttackAction) aaa;
            toHit = jaa.toHit(game);
            if (jaa.getLeg() == JumpJetAttackAction.BOTH) {
                damage = JumpJetAttackAction.getDamageFor(ae, JumpJetAttackAction.LEFT);
                pr.damageRight = JumpJetAttackAction.getDamageFor(ae, JumpJetAttackAction.LEFT);
            } else {
                damage = JumpJetAttackAction.getDamageFor(ae, jaa.getLeg());
                pr.damageRight = 0;
            }
            ae.heatBuildup += (damage + pr.damageRight) / 3;
        } else if (aaa instanceof GrappleAttackAction) {
            GrappleAttackAction taa = (GrappleAttackAction) aaa;
            toHit = taa.toHit(game);
        } else if (aaa instanceof BreakGrappleAttackAction) {
            BreakGrappleAttackAction taa = (BreakGrappleAttackAction) aaa;
            toHit = taa.toHit(game);
        } else if (aaa instanceof RamAttackAction) {
            RamAttackAction raa = (RamAttackAction) aaa;
            toHit = raa.toHit(game);
            damage = RamAttackAction.getDamageFor((Aero) ae, (Aero) aaa.getTarget(game));
        } else if (aaa instanceof TeleMissileAttackAction) {
            TeleMissileAttackAction taa = (TeleMissileAttackAction) aaa;
            toHit = taa.toHit(game);
            damage = TeleMissileAttackAction.getDamageFor(ae);
        } else if (aaa instanceof BAVibroClawAttackAction) {
            BAVibroClawAttackAction bvca = (BAVibroClawAttackAction) aaa;
            toHit = bvca.toHit(game);
            damage = BAVibroClawAttackAction.getDamageFor(ae);
        }
        pr.toHit = toHit;
        pr.damage = damage;
        return pr;
    }

    /**
     * Resolve a Physical Attack
     * 
     * @param pr
     *            The <code>PhysicalResult</code> of the physical attack
     * @param cen
     *            The <code>int</code> Entity Id of the entit's whose physical
     *            attack was last resolved
     */
    private void resolvePhysicalAttack(PhysicalResult pr, int cen) {
        AbstractAttackAction aaa = pr.aaa;
        if (aaa instanceof PunchAttackAction) {
            PunchAttackAction paa = (PunchAttackAction) aaa;
            if (paa.getArm() == PunchAttackAction.BOTH) {
                paa.setArm(PunchAttackAction.LEFT);
                pr.aaa = paa;
                resolvePunchAttack(pr, cen);
                cen = paa.getEntityId();
                paa.setArm(PunchAttackAction.RIGHT);
                pr.aaa = paa;
                resolvePunchAttack(pr, cen);
            } else {
                resolvePunchAttack(pr, cen);
                cen = paa.getEntityId();
            }
        } else if (aaa instanceof KickAttackAction) {
            resolveKickAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof BrushOffAttackAction) {
            BrushOffAttackAction baa = (BrushOffAttackAction) aaa;
            if (baa.getArm() == BrushOffAttackAction.BOTH) {
                baa.setArm(BrushOffAttackAction.LEFT);
                pr.aaa = baa;
                resolveBrushOffAttack(pr, cen);
                cen = baa.getEntityId();
                baa.setArm(BrushOffAttackAction.RIGHT);
                pr.aaa = baa;
                resolveBrushOffAttack(pr, cen);
            } else {
                resolveBrushOffAttack(pr, cen);
                cen = baa.getEntityId();
            }
        } else if (aaa instanceof ThrashAttackAction) {
            resolveThrashAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof ProtomechPhysicalAttackAction) {
            resolveProtoAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof ClubAttackAction) {
            resolveClubAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof PushAttackAction) {
            resolvePushAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof ChargeAttackAction) {
            resolveChargeAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof DfaAttackAction) {
            resolveDfaAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof LayExplosivesAttackAction) {
            resolveLayExplosivesAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof TripAttackAction) {
            resolveTripAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof JumpJetAttackAction) {
            resolveJumpJetAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof GrappleAttackAction) {
            resolveGrappleAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof BreakGrappleAttackAction) {
            resolveBreakGrappleAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof RamAttackAction) {
            resolveRamAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof TeleMissileAttackAction) {
            resolveTeleMissileAttack(pr, cen);
            cen = aaa.getEntityId();
        } else if (aaa instanceof BAVibroClawAttackAction){
            resolveBAVibroClawAttack(pr, cen);
            cen = aaa.getEntityId();
        } else {
            // hmm, error.
        }
        // Not all targets are Entities.
        Targetable target = game.getTarget(aaa.getTargetType(), aaa.getTargetId());
        if (target instanceof Entity) {
            creditKill((Entity) target, game.getEntity(cen));
        }
    }

    /**
     * Add any extreme gravity PSRs the entity gets due to its movement
     * 
     * @param entity
     *            The <code>Entity</code> to check.
     * @param step
     *            The last <code>MoveStep</code> of this entity
     * @param curPos
     *            The current <code>Coords</code> of this entity
     * @param cachedMaxMPExpenditure
     *            Server checks run/jump MP at start of move, as appropriate,
     *            caches to avoid mid-move change in MP causing erroneous grav
     *            check
     */
    private void checkExtremeGravityMovement(Entity entity, MoveStep step, Coords curPos, int cachedMaxMPExpenditure) {
        PilotingRollData rollTarget;
        if (game.getPlanetaryConditions().getGravity() != 1) {
            if (entity instanceof Mech) {
                if (step.getMovementType() == IEntityMovementType.MOVE_WALK || step.getMovementType() == IEntityMovementType.MOVE_VTOL_WALK || step.getMovementType() == IEntityMovementType.MOVE_RUN || step.getMovementType() == IEntityMovementType.MOVE_VTOL_RUN) {
                    if (step.getMpUsed() > cachedMaxMPExpenditure) {
                        // We moved too fast, let's make PSR to see if we get
                        // damage
                        game.addExtremeGravityPSR(entity.checkMovedTooFast(step));
                    }
                } else if (step.getMovementType() == IEntityMovementType.MOVE_JUMP) {
                    System.err.println("gravity move check jump: " + step.getMpUsed() + "/" + cachedMaxMPExpenditure);
                    System.err.flush();
                    if (step.getMpUsed() > cachedMaxMPExpenditure) {
                        // We jumped too far, let's make PSR to see if we get
                        // damage
                        game.addExtremeGravityPSR(entity.checkMovedTooFast(step));
                    } else if (game.getPlanetaryConditions().getGravity() > 1) {
                        // jumping in high g is bad for your legs
                        rollTarget = entity.getBasePilotingRoll(step.getParent().getLastStepMovementType());
                        entity.addPilotingModifierForTerrain(rollTarget, step);
                        rollTarget.append(new PilotingRollData(entity.getId(), 0, "jumped in high gravity"));
                        game.addExtremeGravityPSR(rollTarget);
                    }
                }
            } else if (entity instanceof Tank) {
                if (step.getMovementType() == IEntityMovementType.MOVE_WALK || step.getMovementType() == IEntityMovementType.MOVE_VTOL_WALK || step.getMovementType() == IEntityMovementType.MOVE_RUN || step.getMovementType() == IEntityMovementType.MOVE_VTOL_RUN) {
                    // For Tanks, we need to check if the tank had
                    // more MPs because it was moving along a road.
                    if (step.getMpUsed() > cachedMaxMPExpenditure && !step.isOnlyPavement()) {
                        game.addExtremeGravityPSR(entity.checkMovedTooFast(step));
                    } else if (step.getMpUsed() > cachedMaxMPExpenditure + 1) {
                        // If the tank was moving on a road, he got a +1 bonus.
                        // N.B. The Ask Precentor Martial forum said that a 4/6
                        // tank on a road can move 5/7, **not** 5/8.
                        game.addExtremeGravityPSR(entity.checkMovedTooFast(step));
                    } // End tank-has-road-bonus
                }
            }
        }
    }

    /**
     * Damage the inner structure of a mech's leg / a tank's front. This only
     * happens when the Entity fails an extreme Gravity PSR.
     * 
     * @param entity
     *            The <code>Entity</code> to damage.
     * @param damage
     *            The <code>int</code> amount of damage.
     */
    private Vector<Report> doExtremeGravityDamage(Entity entity, int damage) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        HitData hit;
        if (entity instanceof BipedMech) {
            for (int i = 6; i <= 7; i++) {
                hit = new HitData(i);
                vPhaseReport.addAll(damageEntity(entity, hit, damage, false, DamageType.NONE, true));
            }
        }
        if (entity instanceof QuadMech) {
            for (int i = 4; i <= 7; i++) {
                hit = new HitData(i);
                vPhaseReport.addAll(damageEntity(entity, hit, damage, false, DamageType.NONE, true));
            }
        } else if (entity instanceof Tank) {
            hit = new HitData(Tank.LOC_FRONT);
            vPhaseReport.addAll(damageEntity(entity, hit, damage, false, DamageType.NONE, true));
            vPhaseReport.addAll(vehicleMotiveDamage((Tank) entity, 0));
        }
        return vPhaseReport;
    }

    /**
     * Eject an Entity.
     * 
     * @param entity
     *            The <code>Entity</code> to eject.
     * @param autoEject
     *            The <code>boolean</code> state of the entity's auto-
     *            ejection system
     * @return a <code>Vector</code> of report objects for the gamelog.
     */
    public Vector<Report> ejectEntity(Entity entity, boolean autoEject) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // An entity can only eject it's crew once.
        if (entity.getCrew().isEjected())
            return vDesc;

        // If the crew are already dead, don't bother
        if (entity.isCarcass())
            return vDesc;

        // Mek pilots may get hurt during ejection,
        // and run around the board afterwards.
        if (entity instanceof Mech) {
            PilotingRollData rollTarget = new PilotingRollData(entity.getId(), entity.getCrew().getPiloting(), "ejecting");
            if (entity.isProne()) {
                rollTarget.addModifier(5, "Mech is prone");
            }
            if (entity.getCrew().isUnconscious()) {
                rollTarget.addModifier(3, "pilot unconscious");
            }
            if (autoEject) {
                rollTarget.addModifier(1, "automatic ejection");
            }
            if (entity.getInternal(Mech.LOC_HEAD) < 3) {
                rollTarget.addModifier(Math.min(3 - entity.getInternal(Mech.LOC_HEAD), 2), "Head Internal Structure Damage");
            }
            int facing = entity.getFacing();
            Coords targetCoords = entity.getPosition().translated((facing + 3) % 6);
            IHex targetHex = game.getBoard().getHex(targetCoords);
            if (targetHex != null) {
                if (targetHex.terrainLevel(Terrains.WATER) > 0 && !targetHex.containsTerrain(Terrains.ICE)) {
                    rollTarget.addModifier(-1, "landing in water");
                } else if (targetHex.containsTerrain(Terrains.ROUGH)) {
                    rollTarget.addModifier(0, "landing in rough");
                } else if (targetHex.containsTerrain(Terrains.RUBBLE)) {
                    rollTarget.addModifier(0, "landing in rubble");
                } else if (targetHex.terrainLevel(Terrains.WOODS) == 1) {
                    rollTarget.addModifier(2, "landing in light woods");
                } else if (targetHex.terrainLevel(Terrains.WOODS) == 2) {
                    rollTarget.addModifier(3, "landing in heavy woods");
                } else if (targetHex.terrainLevel(Terrains.WOODS) == 3) {
                    rollTarget.addModifier(4, "landing in ultra heavy woods");
                } else if (targetHex.terrainLevel(Terrains.JUNGLE) == 1) {
                    rollTarget.addModifier(3, "landing in light jungle");
                } else if (targetHex.terrainLevel(Terrains.JUNGLE) == 2) {
                    rollTarget.addModifier(5, "landing in heavy jungle");
                } else if (targetHex.terrainLevel(Terrains.JUNGLE) == 3) {
                    rollTarget.addModifier(7, "landing in ultra heavy jungle");
                } else if (targetHex.terrainLevel(Terrains.BLDG_ELEV) > 0) {
                    rollTarget.addModifier(targetHex.terrainLevel(Terrains.BLDG_ELEV), "landing in a building");
                } else
                    rollTarget.addModifier(-2, "landing in clear terrain");
            } else {
                rollTarget.addModifier(-2, "landing off the board");
            }
            
            if ( game.getPlanetaryConditions().getGravity() == 0 ){
                rollTarget.addModifier(3,"Zero-G");
            }else if ( game.getPlanetaryConditions().getGravity() < .8){
                rollTarget.addModifier(2,"Low-G");
            }else if (  game.getPlanetaryConditions().getGravity() > 1.2 ){
                rollTarget.addModifier(2,"High-G");
            }
            
            if ( game.getPlanetaryConditions().getAtmosphere() == PlanetaryConditions.ATMO_VACUUM ){
                rollTarget.addModifier(3,"Vacuum");
            }else if ( game.getPlanetaryConditions().getAtmosphere() == PlanetaryConditions.ATMO_VHIGH){
                rollTarget.addModifier(2,"Very High Atmosphere Pressure");
            }else if ( game.getPlanetaryConditions().getAtmosphere() == PlanetaryConditions.ATMO_TRACE){
                rollTarget.addModifier(2,"Trace atmosphere");
            }
            
            if ( game.getPlanetaryConditions().getWeather() == PlanetaryConditions.WE_HEAVY_SNOW
                    || game.getPlanetaryConditions().getWeather() == PlanetaryConditions.WE_ICE_STORM
                    || game.getPlanetaryConditions().getWeather() == PlanetaryConditions.WE_DOWNPOUR
                    || game.getPlanetaryConditions().getWindStrength() == PlanetaryConditions.WI_STRONG_GALE){
                rollTarget.addModifier(2,"Bad Weather");
            }
            
            if ( game.getPlanetaryConditions().getWindStrength() >= PlanetaryConditions.WI_STORM
                    || ( game.getPlanetaryConditions().getWeather() == PlanetaryConditions.WE_HEAVY_SNOW 
                            && game.getPlanetaryConditions().getWindStrength() == PlanetaryConditions.WI_STRONG_GALE)){
                rollTarget.addModifier(3,"Really Bad Weather");
            }
            
            if (autoEject) {
                r = new Report(6395);
                r.subject = entity.getId();
                r.addDesc(entity);
                r.indent(2);
                r.newlines = 0;
                vDesc.addElement(r);
            }
            // okay, print the info
            r = new Report(2180);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(rollTarget.getLastPlainDesc(), true);
            r.indent(3);
            vDesc.addElement(r);
            // roll
            final int diceRoll = Compute.d6(2);
            r = new Report(2190);
            r.subject = entity.getId();
            r.add(rollTarget.getValueAsString());
            r.add(rollTarget.getDesc());
            r.add(diceRoll);
            r.indent(4);
            r.newlines = 0;
            // create the MechWarrior in any case, for campaign tracking
            MechWarrior pilot = new MechWarrior(entity);
            pilot.setDeployed(true);
            pilot.setId(getFreeEntityId());
            pilot.setLanded(false);
            game.addEntity(pilot.getId(), pilot);
            send(createAddEntityPacket(pilot.getId()));
            // make him not get a move this turn
            pilot.setDone(true);
            if (diceRoll < rollTarget.getValue()) {
                r.choose(false);
                vDesc.addElement(r);
                Report.addNewline(vDesc);
                if ( (rollTarget.getValue()-diceRoll) > 1){
                    vDesc.addAll(damageCrew(pilot, (rollTarget.getValue()-diceRoll)/2));
                }
            } else {
                r.choose(true);
                vDesc.addElement(r);
            }
            if (entity.getCrew().isDoomed()) {
                vDesc.addAll(destroyEntity(pilot, "deadly ejection", false, false));
            } else {
                // Add the pilot as an infantry unit on the battlefield.
                if (game.getBoard().contains(targetCoords)) {
                    pilot.setPosition(targetCoords);
                    /*
                     * Can pilots eject into water??? ASSUMPTION : They can
                     * (because they get a -1 mod to the PSR. // Did the pilot
                     * land in water? if ( game.getBoard().getHex(
                     * targetCoords).levelOf ( Terrain.WATER ) > 0 ) { //report
                     * missing desc.append("and the pilot ejects, but lands in
                     * water!!!\n"); //report missing desc.append(destroyEntity(
                     * pilot, "a watery grave", false )); } else { //report
                     * missing desc.append("and the pilot ejects safely!\n"); }
                     */
                    // report safe ejection
                    r = new Report(6400);
                    r.subject = entity.getId();
                    r.indent(5);
                    vDesc.addElement(r);
                    //infantry have auto XTC gear so do pilots
                    /*if (game.getPlanetaryConditions().isVacuum()) {
                        // ended up in a vacuum
                        r = new Report(6405);
                        r.subject = entity.getId();
                        r.indent(3);
                        vDesc.addElement(r);
                        vDesc.addAll(destroyEntity(pilot, "explosive decompression", false, false));
                    }*/
                    // Update the entity
                    entityUpdate(pilot.getId());
                    // check if the pilot lands in a minefield
                    vDesc.addAll(doEntityDisplacementMinefieldCheck(pilot, entity.getPosition(), targetCoords, entity.getElevation()));
                } else {
                    // ejects safely
                    r = new Report(6410);
                    r.subject = entity.getId();
                    r.indent(3);
                    vDesc.addElement(r);
                    /*
                    if (game.getPlanetaryConditions().isVacuum()) {
                        // landed in vacuum
                        r = new Report(6405);
                        r.subject = entity.getId();
                        r.indent(3);
                        vDesc.addElement(r);
                        vDesc.addAll(destroyEntity(pilot, "explosive decompression", false, false));
                    } else {*/
                    game.removeEntity(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT);
                    send(createRemoveEntityPacket(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT));
                    //}
                }
                if (game.getOptions().booleanOption("ejected_pilots_flee")) {
                    game.removeEntity(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT);
                    send(createRemoveEntityPacket(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT));
                }
            } // Pilot safely ejects.

        } // End entity-is-Mek
        else if (game.getBoard().contains(entity.getPosition()) && !game.getOptions().booleanOption("ejected_pilots_flee") && entity instanceof Tank) {
            int crewSize = Math.max(1, (int) (14 + entity.getWeight()) / 15);
            MechWarrior pilot = new MechWarrior(entity);
            pilot.setChassis("Vehicle Crew");
            pilot.setDeployed(true);
            pilot.setId(getFreeEntityId());
            pilot.initializeInternal(crewSize, Infantry.LOC_INFANTRY);
            game.addEntity(pilot.getId(), pilot);
            send(createAddEntityPacket(pilot.getId()));
            // make him not get a move this turn
            pilot.setDone(true);
            // place on board

            pilot.setPosition(entity.getPosition());
            // Update the entity
            entityUpdate(pilot.getId());
            // check if the pilot lands in a minefield
            vDesc.addAll(doEntityDisplacementMinefieldCheck(pilot, entity.getPosition(), entity.getPosition(), entity.getElevation()));
        }

        // Mark the entity's crew as "ejected".
        entity.getCrew().setEjected(true);
        if (entity instanceof VTOL) {
            vDesc.addAll(crashVTOLorWiGE((VTOL) entity));
        }
        vDesc.addAll(destroyEntity(entity, "ejection", true, true));

        // only remove the unit that ejected manually
        if (!autoEject) {
            game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_EJECTED);
            send(createRemoveEntityPacket(entity.getId(), IEntityRemovalConditions.REMOVE_EJECTED));
        }
        return vDesc;
    }

    /**
     * Abandon an Entity.
     * 
     * @param entity
     *            The <code>Entity</code> to abandon.
     * @return a <code>Vector</code> of report objects for the gamelog.
     */
    public Vector<Report> abandonEntity(Entity entity) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;

        // An entity can only eject it's crew once.
        if (entity.getCrew().isEjected())
            return vDesc;

        if (entity.getCrew().isDoomed())
            return vDesc;

        // Don't make them abandon into vacuum
        if (game.getPlanetaryConditions().isVacuum())
            return vDesc;

        Coords targetCoords = entity.getPosition();

        if (entity instanceof Mech) {
            // okay, print the info
            r = new Report(2027);
            r.subject = entity.getId();
            r.add(entity.getCrew().getName());
            r.addDesc(entity);
            r.indent(3);
            vDesc.addElement(r);

            // create the MechWarrior in any case, for campaign tracking
            MechWarrior pilot = new MechWarrior(entity);
            pilot.getCrew().setUnconscious(entity.getCrew().isUnconscious());
            pilot.setDeployed(true);
            pilot.setId(getFreeEntityId());
            game.addEntity(pilot.getId(), pilot);
            send(createAddEntityPacket(pilot.getId()));
            // make him not get a move this turn
            pilot.setDone(true);
            // Add the pilot as an infantry unit on the battlefield.
            if (game.getBoard().contains(targetCoords))
                pilot.setPosition(targetCoords);
            // Update the entity
            entityUpdate(pilot.getId());
            // check if the pilot lands in a minefield
            vDesc.addAll(doEntityDisplacementMinefieldCheck(pilot, entity.getPosition(), targetCoords, entity.getElevation()));
            if (game.getOptions().booleanOption("ejected_pilots_flee")) {
                game.removeEntity(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT);
                send(createRemoveEntityPacket(pilot.getId(), IEntityRemovalConditions.REMOVE_IN_RETREAT));
            }
        } // End entity-is-Mek
        else if (game.getBoard().contains(entity.getPosition()) && !game.getOptions().booleanOption("ejected_pilots_flee") && game.getOptions().booleanOption("vehicles_can_eject") && entity instanceof Tank) {
            int crewSize = Math.max(1, (int) (14 + entity.getWeight()) / 15);
            MechWarrior pilot = new MechWarrior(entity);
            pilot.setChassis("Vehicle Crew");
            pilot.setDeployed(true);
            pilot.setId(getFreeEntityId());
            pilot.initializeInternal(crewSize, Infantry.LOC_INFANTRY);
            game.addEntity(pilot.getId(), pilot);
            send(createAddEntityPacket(pilot.getId()));
            // make him not get a move this turn
            pilot.setDone(true);
            // place on board

            pilot.setPosition(entity.getPosition());
            // Update the entity
            entityUpdate(pilot.getId());
            // check if the pilot lands in a minefield
            vDesc.addAll(doEntityDisplacementMinefieldCheck(pilot, entity.getPosition(), entity.getPosition(), entity.getElevation()));
        }

        // Mark the entity's crew as "ejected".
        entity.getCrew().setEjected(true);

        return vDesc;
    }

    /**
     * Checks if ejected Mechwarriors are eligible to be picked up, and if so,
     * captures them or picks them up
     */
    private void resolveMechWarriorPickUp() {
        Report r;

        // fetch all mechWarriors that are not picked up
        Enumeration<Entity> mechWarriors = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (entity instanceof MechWarrior) {
                    MechWarrior mw = (MechWarrior) entity;
                    if (mw.getPickedUpById() == Entity.NONE && !mw.isDoomed() && mw.getTransportId() == Entity.NONE) {
                        return true;
                    }
                }
                return false;
            }
        });
        // loop through them, check if they are in a hex occupied by another
        // unit
        while (mechWarriors.hasMoreElements()) {
            boolean pickedUp = false;
            MechWarrior e = (MechWarrior) mechWarriors.nextElement();
            Enumeration<Entity> pickupEntities = game.getEntities(e.getPosition());
            while (pickupEntities.hasMoreElements()) {
                Entity pe = pickupEntities.nextElement();
                if (pe.isDoomed() || pe.isShutDown() || pe.getCrew().isUnconscious()) {
                    continue;
                }
                if (!pickedUp && pe.getOwnerId() == e.getOwnerId() && pe.getId() != e.getId()) {
                    if (pe instanceof MechWarrior) {
                        // MWs have a beer together
                        r = new Report(6415, Report.PUBLIC);
                        r.add(pe.getDisplayName());
                        addReport(r);
                        continue;
                    }
                    // Pick up the unit.
                    pe.pickUp(e);
                    // The picked unit is being carried by the loader.
                    e.setPickedUpById(pe.getId());
                    e.setPickedUpByExternalId(pe.getExternalId());
                    pickedUp = true;
                    r = new Report(6420, Report.PUBLIC);
                    r.add(e.getDisplayName());
                    r.addDesc(pe);
                    addReport(r);
                    break;
                }
            }
            if (!pickedUp) {
                Enumeration<Entity> pickupEnemyEntities = game.getEnemyEntities(e.getPosition(), e);
                while (pickupEnemyEntities.hasMoreElements()) {
                    Entity pe = pickupEnemyEntities.nextElement();
                    if (pe.isDoomed() || pe.isShutDown() || pe.getCrew().isUnconscious()) {
                        continue;
                    }
                    if (pe instanceof MechWarrior) {
                        // MWs have a beer together
                        r = new Report(6415, Report.PUBLIC);
                        r.add(pe.getDisplayName());
                        addReport(r);
                        continue;
                    }
                    // Capture the unit.
                    pe.pickUp(e);
                    // The captured unit is being carried by the loader.
                    e.setCaptured(true);
                    e.setPickedUpById(pe.getId());
                    e.setPickedUpByExternalId(pe.getExternalId());
                    pickedUp = true;
                    r = new Report(6420, Report.PUBLIC);
                    r.add(e.getDisplayName());
                    r.addDesc(pe);
                    addReport(r);
                    break;
                }
            }
            if (pickedUp) {
                // Remove the picked-up unit from the screen.
                e.setPosition(null);
                // Update the loaded unit.
                entityUpdate(e.getId());
            }
        }
    }

    /**
     * destroy all wheeled and tracked Tanks that got displaced into water
     */
    private void resolveSinkVees() {
        Enumeration<Entity> sinkableTanks = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (entity.isOffBoard()) {
                    return false;
                }

                if (entity instanceof Tank && entity.getPosition() != null && (entity.getMovementMode() == IEntityMovementMode.TRACKED || entity.getMovementMode() == IEntityMovementMode.WHEELED) && game.getBoard().getHex(entity.getPosition()).terrainLevel(Terrains.WATER) > 0 && entity.getElevation() < 0) {
                    return true;
                }
                return false;
            }
        });
        while (sinkableTanks.hasMoreElements()) {
            Entity e = sinkableTanks.nextElement();
            addReport(destroyEntity(e, "a watery grave", false));
        }
    }

    /**
     * let all Entities make their "break-free-of-swamp-stickyness" PSR
     */
    private void doTryUnstuck() {
        if (game.getPhase() != IGame.Phase.PHASE_MOVEMENT)
            return;

        Report r;

        Enumeration<Entity> stuckEntities = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (entity.isStuck()) {
                    return true;
                }
                return false;
            }
        });
        PilotingRollData rollTarget;
        while (stuckEntities.hasMoreElements()) {
            Entity entity = stuckEntities.nextElement();
            rollTarget = entity.getBasePilotingRoll();          
            entity.addPilotingModifierForTerrain(rollTarget);
            // apart from swamp & liquid magma, -1 modifier
            IHex hex = game.getBoard().getHex(entity.getPosition());
            rollTarget.addModifier(hex.getUnstuckModifier(entity.getElevation()), "terrain");
            // okay, print the info
            r = new Report(2340);
            r.addDesc(entity);
            addReport(r);

            // roll
            final int diceRoll = Compute.d6(2);
            r = new Report(2190);
            r.subject = entity.getId();
            r.add(rollTarget.getValueAsString());
            r.add(rollTarget.getDesc());
            r.add(diceRoll);
            if (diceRoll < rollTarget.getValue()) {
                r.choose(false);
            } else {
                r.choose(true);
                entity.setStuck(false);
                entity.setCanUnstickByJumping(false);
                entity.setElevation(0);
                entityUpdate(entity.getId());
            }
            addReport(r);
        }
    }

    /**
     * Remove all iNarc pods from all vehicles that did not move and shoot this
     * round NOTE: this is not quite what the rules say, the player should be
     * able to choose whether or not to remove all iNarc Pods that are attached.
     */
    private void resolveVeeINarcPodRemoval() {
        Enumeration<Entity> vees = game.getSelectedEntities(new EntitySelector() {
            public boolean accept(Entity entity) {
                if (entity instanceof Tank && entity.mpUsed == 0) {
                    return true;
                }
                return false;
            }
        });
        boolean canSwipePods;
        while (vees.hasMoreElements()) {
            canSwipePods = true;
            Entity entity = vees.nextElement();
            for (int i = 0; i <= 5; i++) {
                if (entity.weaponFiredFrom(i)) {
                    canSwipePods = false;
                }
            }
            if (((Tank) entity).getStunnedTurns() > 0)
                canSwipePods = false;
            if (canSwipePods && entity.hasINarcPodsAttached() && entity.getCrew().isActive()) {
                entity.removeAllINarcPods();
                Report r = new Report(2345);
                r.addDesc(entity);
                addReport(r);
            }
        }
    }

    /*
     * //See note above where knownDeadEntities variable is declared private
     * void deadEntitiesCleanup() { Entity en = null; for(Enumeration k =
     * game.getGraveyardEntities(); k.hasMoreElements(); en = (Entity)
     * k.nextElement()) { if (en != null) { if (!knownDeadEntities.contains(en)) {
     * knownDeadEntities.add(en); } } } }
     */

    /**
     * remove Ice in the hex that's at the passed coords, and let entities fall
     * into water below it, if there is water
     * 
     * @param c
     *            the <code>Coords</code> of the hex where ice should be
     *            removed
     * @return a <code>Vector<Report></code> for the phase report
     */
    private Vector<Report> resolveIceBroken(Coords c) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        IHex hex = game.getBoard().getHex(c);
        hex.removeTerrain(Terrains.ICE);
        sendChangedHex(c);
        // if there is water below the ice
        if (hex.terrainLevel(Terrains.WATER) > 0) {
            // drop entities on the surface into the water
            for (Enumeration<Entity> entities = game.getEntities(c); entities.hasMoreElements();) {
                Entity e = entities.nextElement();
                // If the unit is on the surface, and is no longer allowed in
                // the hex
                if (e.getElevation() == 0 && !(e.getRunMP() > 0 && (e.getMovementMode() == IEntityMovementMode.HOVER || e.getMovementMode() == IEntityMovementMode.WIGE)) && e.getMovementMode() != IEntityMovementMode.INF_UMU && !e.hasUMU()) {
                    vPhaseReport.addAll(doEntityFallsInto(e, c, c, new PilotingRollData(TargetRoll.AUTOMATIC_FAIL)));
                }
            }
        }
        return vPhaseReport;
    }

    /**
     * melt any snow or ice in a hex, including checking for the effects of breaking through ice
     */
    private Vector<Report> meltIceAndSnow(Coords c, int entityId) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;
        IHex hex = game.getBoard().getHex(c);
        r = new Report(3069);
        r.indent(2);
        r.subject = entityId;
        vDesc.add(r);
        if (hex.containsTerrain(Terrains.SNOW)) {
            hex.removeTerrain(Terrains.SNOW);
            sendChangedHex(c);
        }
        if (hex.containsTerrain(Terrains.ICE)) {
            vDesc.addAll(resolveIceBroken(c));
        }
        //if we were not in water, then add mud
        if (!hex.containsTerrain(Terrains.MUD) && !hex.containsTerrain(Terrains.WATER)) {
            hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.MUD, 1));
            sendChangedHex(c);
        }
        return vDesc;
    }
    
    
    /**
     * check to see if a swamp hex becomes quicksand
     */
    private Vector<Report> checkQuickSand(Coords c) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;
        IHex hex = game.getBoard().getHex(c);
        if(hex.terrainLevel(Terrains.SWAMP)  == 1) {
            if(Compute.d6(2) == 12) {
                //better find a rope
                hex.removeTerrain(Terrains.SWAMP);
                hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.SWAMP,2));
                sendChangedHex(c);
                r = new Report(2440);
                r.indent(1);
                vDesc.add(r);
            }
        }
        return vDesc;
    }
     
  
    /**
     * check for vehicle fire, according to the MaxTech rules
     * 
     * @param tank
     *            the <code>Tank</code> to be checked
     * @param inferno
     *            a <code>boolean</code> parameter wether or not this check is
     *            because of inferno fire
     */
    public Vector<Report> checkForVehicleFire(Tank tank, boolean inferno) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        int boomroll = Compute.d6(2);
        int penalty = 0;
        switch (tank.getMovementMode()) {
        case IEntityMovementMode.HOVER:
            penalty = 4;
            break;
        case IEntityMovementMode.VTOL:
        case IEntityMovementMode.WHEELED:
            penalty = 2;
            break;
        }
        if (inferno) {
            boomroll = 12;
        }
        Report r = new Report(5250);
        r.subject = tank.getId();
        r.addDesc(tank);
        r.add(8 - penalty);
        r.add(boomroll);
        if (boomroll + penalty < 8) {
            // phew!
            r.choose(true);
            vPhaseReport.add(r);
        } else {
            // eek
            if (!inferno) {
                r.choose(false);
                vPhaseReport.add(r);
            }
            if (boomroll + penalty < 10) {
                addReport(vehicleMotiveDamage(tank, penalty - 1));
            } else {
                vPhaseReport.addAll(resolveVehicleFire(tank, false));
                if (boomroll + penalty >= 12) {
                    r = new Report(5255);
                    r.subject = tank.getId();
                    r.indent(3);
                    vPhaseReport.add(r);
                    tank.setOnFire(inferno);
                }
            }
        }
        return vPhaseReport;
    }

    private Vector<Report> resolveVehicleFire(Tank tank, boolean existingStatus) {
        Vector<Report> vPhaseReport = new Vector<Report>();
        if (existingStatus && !tank.isOnFire())
            return vPhaseReport;
        for (int i = 0; i < tank.locations(); i++) {
            if (i == Tank.LOC_BODY || tank instanceof VTOL && i == VTOL.LOC_ROTOR)
                continue;
            if (existingStatus && !tank.isLocationBurning(i))
                continue;
            HitData hit = new HitData(i);
            int damage = Compute.d6(1);
            vPhaseReport.addAll(damageEntity(tank, hit, damage));
            if (damage == 1 && existingStatus) {
                tank.extinguishLocation(i);
            }
        }
        return vPhaseReport;
    }

    private Vector<Report> vehicleMotiveDamage(Tank te, int modifier) {
        Vector<Report> vDesc = new Vector<Report>();
        Report r;
        r = new Report(1210, Report.PUBLIC);
        vDesc.add(r);
        switch (te.getMovementMode()) {
        case IEntityMovementMode.HOVER:
        case IEntityMovementMode.HYDROFOIL:
            modifier += 3;
            break;
        case IEntityMovementMode.WHEELED:
            modifier += 2;
            break;
        case IEntityMovementMode.WIGE:
            modifier += 4;
            break;
        case IEntityMovementMode.VTOL:
            // VTOL don't roll, auto -1 MP
            int nMP = te.getOriginalWalkMP();
            if (nMP > 0) {
                te.setOriginalWalkMP(nMP - 1);
            }
            if (nMP > 1) {
                r = new Report(6660);
                r.subject = te.getId();
                vDesc.add(r);
            } else {
                r = new Report(6670);
                r.subject = te.getId();
                vDesc.add(r);
                vDesc.addAll(crashVTOLorWiGE(te));
            }
            return vDesc;
        }
        if (game.getOptions().booleanOption("tacops_vehicle_effective")) {
            modifier = Math.max(modifier - 1, 0);
        }
        int roll = Compute.d6(2) + modifier;
        r = new Report(6305);
        r.subject = te.getId();
        r.add("movement system");
        r.newlines = 0;
        r.indent(3);
        vDesc.add(r);
        r = new Report(6310);
        r.subject = te.getId();
        r.add(roll);
        r.newlines = 0;
        vDesc.add(r);
        r = new Report(3340);
        r.add(modifier);
        r.subject = te.getId();
        vDesc.add(r);

        if (roll <= 5) {
            // no effect
            r = new Report(6005);
            r.subject = te.getId();
            r.indent(3);
            vDesc.add(r);
        } else if (roll <= 7) {
            // minor damage
            r = new Report(6470);
            r.subject = te.getId();
            r.indent(3);
            vDesc.add(r);
            te.addMovementDamage(1);
        } else if (roll <= 9) {
            // moderate damage
            r = new Report(6471);
            r.subject = te.getId();
            r.indent(3);
            vDesc.add(r);
            te.addMovementDamage(2);
            int nMP = te.getOriginalWalkMP();
            if (nMP > 0) {
                te.setOriginalWalkMP(nMP - 1);
            }
        } else if (roll <= 11) {
            // heavy damage
            r = new Report(6472);
            r.subject = te.getId();
            r.indent(3);
            vDesc.add(r);
            te.addMovementDamage(3);
            int nMP = te.getOriginalWalkMP();
            if (nMP > 0) {
                te.setOriginalWalkMP(nMP / 2);
            }
        } else {
            r = new Report(6473);
            r.subject = te.getId();
            r.indent(3);
            vDesc.add(r);
            te.immobilize();
        }
        if (te.getOriginalWalkMP() == 0 || te.isImmobile()) {
            // Hovercraft reduced to 0MP over water sink
            if ((te.getMovementMode() == IEntityMovementMode.HOVER || (te.getMovementMode() == IEntityMovementMode.WIGE && te.getElevation() == 0)) && game.getBoard().getHex(te.getPosition()).terrainLevel(Terrains.WATER) > 0 && !game.getBoard().getHex(te.getPosition()).containsTerrain(Terrains.ICE)) {
                vDesc.addAll(destroyEntity(te, "a watery grave", false));
            }
            if (te instanceof VTOL || (te.getMovementMode() == IEntityMovementMode.WIGE && te.getElevation() > 0)) {
                // report problem: add tab
                vDesc.addAll(crashVTOLorWiGE(te));
            }
        }
        return vDesc;
    }

    /**
     * Add a whole lotta Reports to the players report queues as well as the
     * Master report queue vPhaseReport.
     */
    private void addReport(Vector<Report> reports) {
        // Only bother with player reports if doing double blind.
        if (doBlind()) {
            for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
                final IConnection conn = i.nextElement();
                Player p = game.getPlayer(conn.getId());

                p.getTurnReport().addAll(filterReportVector(reports, p));
            }
        }
        vPhaseReport.addAll(reports);
    }

    /**
     * Add a single report to the report queue of all players and the master
     * vPhaseReport queue
     */
    private void addReport(Report report) {
        // Only bother with player reports if doing double blind.
        if (doBlind()) {
            for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
                final IConnection conn = i.nextElement();
                Player p = game.getPlayer(conn.getId());

                p.getTurnReport().addElement(filterReport(report, p, false));
            }
        }
        vPhaseReport.addElement(report);
    }

    /**
     * New Round has started clear everyone's report queue
     */
    private void clearReports() {
        // Only bother with player reports if doing double blind.
        if (doBlind()) {
            for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
                final IConnection conn = i.nextElement();
                Player p = game.getPlayer(conn.getId());

                p.getTurnReport().removeAllElements();
            }
        }
        vPhaseReport.removeAllElements();
    }

    /**
     * make sure all the new lines that were added to the old vPhaseReport get
     * added to all of the players filters
     */
    private void addNewLines() {
        // Only bother with player reports if doing double blind.
        if (doBlind()) {
            for (Enumeration<IConnection> i = connections.elements(); i.hasMoreElements();) {
                final IConnection conn = i.nextElement();
                Player p = game.getPlayer(conn.getId());

                Report.addNewline(p.getTurnReport());
            }
        }
        Report.addNewline(vPhaseReport);
    }

    /**
     * resolve an assault drop
     * 
     * @param entity
     *            the <code>Entity</code> for which to resolve it
     */
    public void doAssaultDrop(Entity entity) {
        PilotingRollData psr;
        if (entity instanceof Mech) {
            psr = entity.getBasePilotingRoll();
        } else {
            psr = new PilotingRollData(entity.getId(), 4, "landing assault drop");
        }
        int roll = Compute.d6(2);
        // check for a safe landing
        Report r = new Report(2380);
        r.subject = entity.getId();
        r.add(entity.getDisplayName(), true);
        r.add(psr.getValueAsString());
        r.add(roll);
        r.choose(roll >= psr.getValue());
        addReport(r);
        if (roll < psr.getValue()) {
            int fallHeight = psr.getValue() - roll;
            // determine where we really land
            int distance = Compute.d6(fallHeight);
            Coords c = Compute.scatter(entity.getPosition(), fallHeight);
            r = new Report(2385);
            r.subject = entity.getId();
            r.add(distance);
            r.indent(3);
            r.newlines = 0;
            addReport(r);
            if (fallHeight >= 5 || !game.getBoard().contains(c)) {
                r = new Report(2386);
                addReport(r);
                game.removeEntity(entity.getId(), IEntityRemovalConditions.REMOVE_NEVER_JOINED);
                return;
            }
            entity.setPosition(c);

            // do fall damage
            if (entity instanceof Mech || entity instanceof Protomech) {
                entity.setElevation(fallHeight);
                addReport(doEntityFallsInto(entity, c, c, psr, true));
            } else if (entity instanceof BattleArmor) {
                for (int i = 1; i < entity.locations(); i++) {
                    HitData h = new HitData(i);
                    addReport(damageEntity(entity, h, Compute.d6(fallHeight)));
                    addNewLines();
                }
            } else if (entity instanceof Infantry) {
                HitData h = new HitData(Infantry.LOC_INFANTRY);
                addReport(damageEntity(entity, h, 1));
                addNewLines();
            }
        }
        // set entity to expected elevation
        IHex hex = game.getBoard().getHex(entity.getPosition());
        entity.setElevation(entity.elevationOccupied(hex) - hex.floor());
        // finally, check for any stacking violations
        Entity violated = Compute.stackingViolation(game, entity, entity.getPosition(), null);
        if (violated != null) {
            // handle this as accidental fall from above
            entity.setElevation(violated.getElevation() + 2);
            r = new Report(2390);
            r.subject = entity.getId();
            r.add(entity.getDisplayName(), true);
            r.add(violated.getDisplayName(), true);
            addReport(r);
            addReport(doEntityFallsInto(entity, entity.getPosition(), entity.getPosition(), psr));
        }
    }

    /**
     * resolve assault drops for all entities
     */
    void doAllAssaultDrops() {
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity e = i.nextElement();
            if (e.isAssaultDropInProgress()) {
                doAssaultDrop(e);
                e.setLandedAssaultDrop();
            }
        }
    }

    /**
     * do damage from magma
     * 
     * @param en
     *            the affected <code>Entity</code>
     * @param eruption
     *            <code>boolean</code> indicating wether or not this is
     *            because of an eruption
     */
    void doMagmaDamage(Entity en, boolean eruption) {
        if ((en.getMovementMode() == IEntityMovementMode.VTOL || en.getMovementMode() == IEntityMovementMode.HOVER || en.getMovementMode() == IEntityMovementMode.WIGE && en.getOriginalWalkMP() > 0 && !eruption) && !en.isImmobile()) {
            return;
        }
        Report r;
        boolean isMech = en instanceof Mech;
        if (isMech)
            r = new Report(2405);
        else
            r = new Report(2400);
        r.addDesc(en);
        r.subject = en.getId();
        addReport(r);
        if (isMech) {
            HitData h;
            for (int i = 0; i < en.locations(); i++) {
                if (eruption || en.locationIsLeg(i) || en.isProne()) {
                    h = new HitData(i);
                    addReport(damageEntity(en, h, Compute.d6(2)));
                }
            }
        } else {
            addReport(destroyEntity(en, "fell into magma", false, false));
        }
        addNewLines();
    }
    
    /**
     * sink any entities in quicksand in the current hex
     */
    public void doSinkEntity(Entity en) {
        Report r;
        r = new Report(2445);
        r.addDesc(en);
        r.subject = en.getId();
        addReport(r);
        en.setElevation(en.getElevation() - 1);
        //if this means the entity is below the ground, then bye-bye!
        if(Math.abs(en.getElevation()) > en.getHeight()) {
            addReport(destroyEntity(en, "quicksand"));
        }
    }

    /**
     * deal area saturation damage to an individual hex
     * 
     * @param coords
     *            The hex being hit
     * @param attackSource
     *            The location the attack came from. For hit table resolution
     * @param damage
     *            Amount of damage to deal to each entity
     * @param ammo
     *            The ammo type being used
     * @param subjectId
     *            Subject for reports
     * @param killer
     *            Who should be credited with kills
     * @param exclude
     *            Entity that should take no damage (used for homing splash)
     * @param flak
     *            Flak, hits flying units only, instead of flyers being immune
     * @param altitude
     *            Absolute altitude for flak attack
     */
    void artilleryDamageHex(Coords coords, Coords attackSource, int damage, AmmoType ammo, int subjectId, Entity killer, Entity exclude, boolean flak, int altitude, Vector<Report> vPhaseReport) {

        IHex hex = game.getBoard().getHex(coords);
        if (hex == null)
            return; // not on board.

        int flakElevation = altitude - hex.surface();

        Report r;

        if (!flak) {
            vPhaseReport.addAll(tryClearHex(coords, damage * 2, subjectId));
        }
        Building bldg = game.getBoard().getBuildingAt(coords);
        int bldgAbsorbs = 0;
        if (bldg != null && !(flak && flakElevation > hex.terrainLevel(Terrains.BLDG_ELEV))) {
            bldgAbsorbs = bldg.getPhaseCF(coords) / 10;
            if (!(ammo != null && ammo.getMunitionType() == AmmoType.M_FLECHETTE)) {
                // damage the building
                Vector<Report> buildingReport = damageBuilding(bldg, damage, coords);
                for (Report report : buildingReport) {
                    report.subject = subjectId;
                }
                vPhaseReport.addAll(buildingReport);
                addNewLines();
            }
        }

        if (flak && (flakElevation <= 0 || flakElevation <= hex.terrainLevel(Terrains.BLDG_ELEV) || flakElevation == hex.terrainLevel(Terrains.BRIDGE_ELEV))) {
            // Flak in this hex would only hit landed units
            return;
        }

        // get units in hex
        for (Enumeration<Entity> impactHexHits = game.getEntities(coords); impactHexHits.hasMoreElements();) {
            Entity entity = impactHexHits.nextElement();
            int hits = damage;
            ToHitData toHit = new ToHitData();
            int cluster = 5;

            // Check: is entity excluded?
            if (entity == exclude)
                continue;

            // Check: is entity inside building?
            if (bldg != null && bldgAbsorbs > 0 && entity.getElevation() < hex.terrainLevel(Terrains.BLDG_ELEV)) {
                cluster -= bldgAbsorbs;
                if (entity instanceof Infantry) {
                    continue; // took its damage already from building damage
                } else if (cluster <= 0) {
                    // entity takes no damage
                    r = new Report(6426);
                    r.subject = subjectId;
                    r.addDesc(entity);
                    vPhaseReport.add(r);
                    continue;
                } else {
                    r = new Report(6425);
                    r.subject = subjectId;
                    r.add(bldgAbsorbs);
                    vPhaseReport.add(r);
                }
            }

            if (flak) {
                // Check: is entity not a VTOL in flight
                if (!(entity instanceof VTOL || entity.getMovementMode() == IEntityMovementMode.VTOL)) {
                    continue;
                }
                // Check: is entity at correct elevation?
                if (entity.getElevation() != flakElevation)
                    continue;
            } else {
                // Check: is entity a VTOL in flight?
                if (entity instanceof VTOL || entity.getMovementMode() == IEntityMovementMode.VTOL) {
                    // VTOLs take no damage from normal artillery unless landed
                    if (entity.getElevation() != 0 && entity.getElevation() != hex.terrainLevel(Terrains.BLDG_ELEV) && entity.getElevation() != hex.terrainLevel(Terrains.BRIDGE_ELEV)) {
                        continue;
                    }
                }
            }

            // Work out hit table to use
            if (attackSource != null) {
                toHit.setSideTable(entity.sideTable(attackSource));
                if (ammo != null && ammo.getMunitionType() == AmmoType.M_CLUSTER && attackSource.equals(coords)) {
                    if (entity instanceof Mech) {
                        toHit.setHitTable(ToHitData.HIT_ABOVE);
                    } else if (entity instanceof Tank) {
                        toHit.setSideTable(ToHitData.SIDE_FRONT);
                        toHit.addModifier(2, "cluster artillery hitting a Tank");                        
                    }                    
                }
            }

            // convention infantry take x2 damage from AE weapons
            if (entity instanceof Infantry && !(entity instanceof BattleArmor))
                hits *= 2;

            // Entity/ammo specific damage modifiers
            if (ammo != null) {
                if (ammo.getMunitionType() == AmmoType.M_CLUSTER) {
                    if (hex.containsTerrain(Terrains.FORTIFIED) && entity instanceof Infantry && !(entity instanceof BattleArmor)) {
                        hits *= 2;
                    }
                } else if (ammo.getMunitionType() == AmmoType.M_FLECHETTE) {
                    // wheeled and hover tanks take movement critical
                    if (entity instanceof Tank && (entity.getMovementMode() == IEntityMovementMode.WHEELED || entity.getMovementMode() == IEntityMovementMode.HOVER)) {
                        r = new Report(6480);
                        r.subject = entity.getId();
                        r.addDesc(entity);
                        r.add(toHit.getTableDesc());
                        r.add(0);
                        vPhaseReport.add(r);
                        vPhaseReport.addAll(vehicleMotiveDamage((Tank) entity, 0));
                        continue;
                    }
                    // only infantry and support vees with bar < 5 are affected
                    if (entity instanceof BattleArmor || !(entity instanceof Infantry) && !((entity instanceof SupportTank && ((SupportTank)entity).getBARRating() < 5)
                            || (entity instanceof SupportVTOL && ((SupportVTOL)entity).getBARRating() < 5))) {
                        continue;
                    }
                    if (entity instanceof Infantry) {
                        hits = Compute.d6(damage);
                        hits *= 2;
                    }
                    if ((entity instanceof SupportTank && ((SupportTank)entity).getBARRating() < 5)) {
                        switch (ammo.getAmmoType()) {
                        case AmmoType.T_LONG_TOM:
                            // hack: check if damage is still at 4, so we're in the
                            // center hex. otherwise, do no damage
                            if (damage == 4) {
                                damage = (5 - ((SupportTank)entity).getBARRating()) * 5;
                            }
                            else
                                continue;
                            break;
                        case AmmoType.T_SNIPER:
                            // hack: check if damage is still at 2, so we're in the
                            // center hex. otherwise, do no damage
                            if (damage == 2) {
                                damage = (5 - ((SupportTank)entity).getBARRating()) * 3;
                            }
                            else
                                continue;
                            break;
                        case AmmoType.T_THUMPER:
                            // no need to check for damage, because falloff = damage for the thumper
                            damage = 5 - ((SupportTank)entity).getBARRating();
                            break;
                        }                        
                    }
                    if (entity instanceof SupportVTOL && ((SupportVTOL)entity).getBARRating() < 5) {
                        switch (ammo.getAmmoType()) {
                        case AmmoType.T_LONG_TOM:
                            // hack: check if damage is still at 4, so we're in the
                            // center hex. otherwise, do no damage
                            if (damage == 4) {
                                damage = (5 - ((SupportTank)entity).getBARRating()) * 5;
                            }
                            else
                                continue;
                            break;
                        case AmmoType.T_SNIPER:
                            // hack: check if damage is still at 2, so we're in the
                            // center hex. otherwise, do no damage
                            if (damage == 2) {
                                damage = (5 - ((SupportTank)entity).getBARRating()) * 3;
                            }
                            else
                                continue;
                            break;
                        case AmmoType.T_THUMPER:
                            // no need to check for damage, because falloff = damage for the thumper
                            damage = (5 - ((SupportTank)entity).getBARRating());
                            break;
                        }           
                    }
                }
            }

            // Do the damage
            r = new Report(6480);
            r.subject = entity.getId();
            r.addDesc(entity);
            r.add(toHit.getTableDesc());
            r.add(hits);
            vPhaseReport.add(r);
            if (entity instanceof BattleArmor) {
                // BA take full damage to each trooper, ouch!
                for (int loc = 0; loc < entity.locations(); loc++) {
                    if (entity.getInternal(loc) > 0) {
                        HitData hit = new HitData(loc);
                        vPhaseReport.addAll(damageEntity(entity, hit, hits, false, DamageType.NONE, false, true, false));
                    }
                }
            } else {
                while (hits > 0) {
                    HitData hit = entity.rollHitLocation(toHit.getHitTable(), toHit.getSideTable());

                    vPhaseReport.addAll(damageEntity(entity, hit, Math.min(cluster, hits), false, DamageType.NONE, false, true, false));
                    hits -= Math.min(5, hits);
                }
            }
            if (killer != null) {
                creditKill(entity, killer);
            }
        }
    }

    /**
     * deal area saturation damage to the map, used for artillery
     * 
     * @param centre
     *            The hex on which damage is centred
     * @param attackSource
     *            The position the attack came from
     * @param ammo
     *            The ammo type doing the damage
     * @param subjectId
     *            Subject for reports
     * @param killer
     *            Who should be credited with kills
     * @param flak
     *            Flak, hits flying units only, instead of flyers being immune
     * @param altitude
     *            Absolute altitude for flak attack
     */
    public void artilleryDamageArea(Coords centre, Coords attackSource, AmmoType ammo, int subjectId, Entity killer, boolean flak, int altitude, boolean mineClear, Vector<Report> vPhaseReport) {
        int damage = ammo.getRackSize();
        int falloff = 10;
        if (ammo.getAmmoType() == AmmoType.T_CRUISE_MISSILE) {
            falloff = 25;
        }
        if (ammo.getMunitionType() == AmmoType.M_CLUSTER) {
            // non-arrow-iv cluster does 5 less than standard
            if (ammo.getAmmoType() != AmmoType.T_ARROW_IV) 
                damage -= 5;
            // thumper gets falloff 9 for 1 damage at 1 hex range
            if (ammo.getAmmoType() == AmmoType.T_THUMPER)
                falloff = 9;
            attackSource = centre;
        } else if (ammo.getMunitionType() == AmmoType.M_FLECHETTE) {
            switch (ammo.getAmmoType()) {
            // for flechette, damage and fallof is number of d6, not absolut damage
            case AmmoType.T_LONG_TOM:
                damage = 4;
                falloff = 2;
                break;
            case AmmoType.T_SNIPER:
                damage = 2;
                falloff = 1;
                break;
            case AmmoType.T_THUMPER:
                damage = 1;
                falloff = 1;
            }
        } else 
        //if this was a mine clearance, then it only affects the hex hit
        if (mineClear) {
            falloff = damage;
        }
        artilleryDamageArea(centre, attackSource, ammo, subjectId, killer, damage, falloff, flak, altitude, vPhaseReport);
    }

    /**
     * Deals area-saturation damage to an area of the board. Used for artillery,
     * bombs, or anything else with linear decreas in damage
     * 
     * @param centre
     *            The hex on which damage is centred
     * @param attackSource
     *            The position the attack came from
     * @param ammo
     *            The ammo type doing the damage
     * @param subjectId
     *            Subject for reports
     * @param killer
     *            Who should be credited with kills
     * @param damage
     *            Damage at ground zero
     * @param falloff
     *            Reduction in damage for each hex of distance
     * @param flak
     *            Flak, hits flying units only, instead of flyers being immune
     * @param altitude
     *            Absolute altitude for flak attack
     */
    public void artilleryDamageArea(Coords centre, Coords attackSource, AmmoType ammo, int subjectId, Entity killer, int damage, int falloff, boolean flak, int altitude, Vector<Report> vPhaseReport) {
        for (int ring = 0; damage > 0; ring++, damage -= falloff) {
            ArrayList<Coords> hexes = Compute.coordsAtRange(centre, ring);
            for (Coords c : hexes) {
                artilleryDamageHex(c, attackSource, damage, ammo, subjectId, killer, null, flak, altitude, vPhaseReport);
            }
            attackSource = centre; // all splash comes from ground zero
        }
    }

    /**
     * Resolve any Infantry units which are fortifying hexes
     */
    void resolveFortify() {
        Enumeration<Entity> entities = game.getEntities();
        Report r;
        while (entities.hasMoreElements()) {
            Entity ent = entities.nextElement();
            if (ent instanceof Infantry) {
                Infantry inf = (Infantry) ent;
                int dig = inf.getDugIn();
                if (dig == Infantry.DUG_IN_WORKING) {
                    r = new Report(5300);
                    r.addDesc(inf);
                    r.subject = inf.getId();
                    addReport(r);
                } else if (dig == Infantry.DUG_IN_FORTIFYING2) {
                    Coords c = inf.getPosition();
                    r = new Report(5305);
                    r.addDesc(inf);
                    r.add(c.getBoardNum());
                    r.subject = inf.getId();
                    addReport(r);
                    // fortification complete - add to map
                    IHex hex = game.getBoard().getHex(c);
                    hex.addTerrain(Terrains.getTerrainFactory().createTerrain(Terrains.FORTIFIED, 1));
                    sendChangedHex(c);
                    // Clear the dig in for any units in same hex, since they
                    // get it for free by fort
                    for (Enumeration<Entity> e = game.getEntities(c); e.hasMoreElements();) {
                        Entity ent2 = e.nextElement();
                        if (ent2 instanceof Infantry) {
                            Infantry inf2 = (Infantry) ent;
                            inf2.setDugIn(Infantry.DUG_IN_NONE);
                        }
                    }
                }
            }
        }
    }

    /**
     * check if spikes get breaken in the given location
     * 
     * @param e
     *            the <code>Entity</code> to check
     * @param loc
     *            the <code>int</code location
     */
    private void checkBreakSpikes(Entity e, int loc) {
        int roll = Compute.d6(2);
        Report r;
        if (roll < 9) {
            r = new Report(4445);
            r.newlines = 0;
            r.add(roll);
            r.subject = e.getId();
            addReport(r);
            return;
        }
        r = new Report(4440);
        r.newlines = 0;
        r.add(roll);
        r.subject = e.getId();
        addReport(r);
        for (Mounted m : e.getMisc()) {
            if (m.getType().hasFlag(MiscType.F_SPIKES) && m.getLocation() == loc) {
                m.setHit(true);
            }
        }
    }

    class ConnectionWatchdog extends TimerTask {

        private Server server;

        private int id;

        private int failCount;

        public ConnectionWatchdog(Server server, int id) {
            this.server = server;
            this.id = id;
            failCount = 0;
        }

        @Override
        public void run() {
            if (server.getPlayer(id) != null) {
                // fully connected
                cancel();
                return;
            }
            if (server.getPendingConnection(id) == null) {
                // dropped
                cancel();
                return;
            }
            System.err.println("Bark Bark");
            if (failCount > 10) {
                server.getPendingConnection(id).close();
                cancel();
                System.err.println("Growl");
                System.err.println("\n\n\n\n\n");
                return;
            }
            server.greeting(id);
            failCount++;
        }

    }

    /**
     * @return a <code>String</code> representing the hostname
     */
    public String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @return the <code>int</code> this server is listening on
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Loops through all the attacks the game has. Checks if they care about
     * current phase, if so, runs them, and removes them if they don't want to
     * stay. TODO: Refactor the new entity annoucement out of here.
     */
    private void handleAttacks() {
        Report r;
        int lastAttackerId = -1;
        Vector<AttackHandler> currentAttacks, keptAttacks;
        currentAttacks = game.getAttacksVector();
        keptAttacks = new Vector<AttackHandler>();
        Vector<Report> handleAttackReports = new Vector<Report>();
        // first, do any TAGs, so homing arty will have TAG
        for (AttackHandler ah : currentAttacks) {
            if (!(ah instanceof TAGHandler)) {
                continue;
            }
            if (ah.cares(game.getPhase())) {
                int aId = ah.getAttackerId();
                if (aId != lastAttackerId && !ah.announcedEntityFiring()) {
                    // report who is firing
                    r = new Report(3100);
                    r.subject = aId;
                    Entity ae = game.getEntity(aId);
                    if (ae == null)
                        System.err.println("ae null in handleattacks, entityId " + aId);
                    r.addDesc(game.getEntity(aId));
                    handleAttackReports.addElement(r);
                    ah.setAnnouncedEntityFiring(true);
                }
                lastAttackerId = aId;
                boolean keep = ah.handle(game.getPhase(), handleAttackReports);
                if (keep) {
                    keptAttacks.add(ah);
                }
            }
        }
        // now resolve everything but TAG
        for (AttackHandler ah : currentAttacks) {
            if (ah instanceof TAGHandler) {
                continue;
            }
            if (ah.cares(game.getPhase())) {
                int aId = ah.getAttackerId();
                if (aId != lastAttackerId && !ah.announcedEntityFiring()) {
                    // if this is a new attacker then resolve any
                    // standard-to-cap damage
                    // from previous
                    handleAttackReports.addAll(resolveStandardtoCapital(aId));
                    // report who is firing
                    r = new Report(3100);
                    r.subject = aId;
                    Entity ae = game.getEntity(aId);
                    // for arty, attacker may be dead, or fled, so check out-of-
                    // game entities
                    if (ae == null)
                        ae = game.getOutOfGameEntity(aId);
                    if (ae == null)
                        System.err.println("ae null in handleattacks, entityId " + aId);
                    r.addDesc(game.getEntity(aId));
                    handleAttackReports.addElement(r);
                    ah.setAnnouncedEntityFiring(true);
                }
                lastAttackerId = aId;
                boolean keep = ah.handle(game.getPhase(), handleAttackReports);
                if (keep) {
                    keptAttacks.add(ah);
                }
            } else
                keptAttacks.add(ah);
        }
        // resolve standard to capital one more time
        handleAttackReports.addAll(resolveStandardtoCapital(lastAttackerId));
        handleAttackReports.addAll(resolveSquadronDamage());
        addReport(handleAttackReports);
        // HACK, but anything else seems to run into weird problems.
        game.setAttacksVector(keptAttacks);
    }

    /**
     * @return
     */
    public static Server getServerInstance() {
        return serverInstance;
    }
    
    /**
     * create a <code>SmokeCloud object and add it to the server list</code>
     * @param coords
     */
    public void createSmoke(Coords coords){
        createSmoke(coords,1,0);
    }
    
    /**
     * create a <code>SmokeCloud object and add it to the server list</code>
     * @param coords
     * @param level 1=Light 2=Heavy Smoke
     */
    public void createSmoke(Coords coords, int level){
        createSmoke(coords,level,0);
    }
    
    /**
     * create a <code>SmokeCloud object and add it to the server list</code>
     * @param coords
     * @param level 1=Light 2=Heavy Smoke
     * @param duration How long the smoke will last.
     */
    public void createSmoke(Coords coords, int level, int duration){
        SmokeCloud cloud = new SmokeCloud(coords,level,duration);
        game.addSmokeCloud(cloud);
    }
    
    /**
     * create a <code>SmokeCloud object and add it to the server list</code>
     * @param coords
     * @param level 1=Light 2=Heavy Smoke
     * @param duration  duration How long the smoke will last.
     */
    public void createSmoke(ArrayList<Coords> coords, int level, int duration){
        SmokeCloud cloud = new SmokeCloud(coords,level,duration);
        game.addSmokeCloud(cloud);
    }
    
    /**
     * Update the map with a new set of coords.
     * @param newCoords
     */
    public void updateSmoke(SmokeCloud cloud, ArrayList<Coords> newCoords){
        removeSmokeTerrain(cloud);
        cloud.getCoordsList().clear();
        cloud.getCoordsList().addAll(newCoords);
    }

    /**
     * remove a cloud from the map
     * @param cloud
     */
    public void removeSmokeTerrain(SmokeCloud cloud){
        for ( Coords coords : cloud.getCoordsList() ){
            IHex nextHex = game.getBoard().getHex(coords);
            if (nextHex != null && nextHex.containsTerrain(Terrains.SMOKE)) {
                nextHex.removeTerrain(Terrains.SMOKE);
                sendChangedHex(coords);
            }
        }
    }
    
    public List<SmokeCloud> getSmokeCloudList(){
        return game.getSmokeCloudList();
    }
    
    /**
     * Check to see if blowing sand caused damage to airborne VTOL/WIGEs
     */
    private Vector<Report> resolveBlowingSandDamage() {
        Vector<Report> vFullReport = new Vector<Report>();
        vFullReport.add(new Report(5002, Report.PUBLIC));
        int damage_bonus = Math.max(0, game.getPlanetaryConditions().getWindStrength() - PlanetaryConditions.WI_MOD_GALE);
        //cycle through each team and damage 1d6 airborne VTOL/WIGE
        for (Enumeration<Team> loop = game.getTeams(); loop.hasMoreElements();) {
            Team team = loop.nextElement();
            Vector<Integer> airborne = team.getAirborneVTOL();
            if(airborne.size() > 0) {
                //how many units are affected
                int unitsAffected = Math.min(Compute.d6(), airborne.size());
                while(unitsAffected > 0 && airborne.size() > 0) {
                    int loc = Compute.randomInt(airborne.size());
                    Entity en = game.getEntity(airborne.get(loc));                    
                    int damage = Math.max(1, Compute.d6()/2) + damage_bonus;
                    while(damage > 0) {
                        HitData hit = en.rollHitLocation(ToHitData.HIT_NORMAL, ToHitData.SIDE_RANDOM);
                        vFullReport.addAll(damageEntity(en, hit, 1));
                        damage--;
                    }
                    unitsAffected--;
                    airborne.remove(loc);
                }
            }
        }
        Report.addNewline(vPhaseReport);
        return vFullReport;
    }
    
}
