package forge.player;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.trackable.TrackableTypes;
import forge.trackable.Tracker;

public class PlayerZoneUpdate implements Serializable {
    private static final long serialVersionUID = -7666875897455073970L;

    private final int playerId;
    private transient PlayerView playerCache;
    private final Set<ZoneType> zones;

    public PlayerZoneUpdate(final PlayerView player, final ZoneType zone) {
        if (player == null) {
            throw new NullPointerException();
        }
        this.playerId = player.getId();
        this.playerCache = player;
        if (zone != null) {
            this.zones = EnumSet.of(zone);
        } else {
            this.zones = EnumSet.noneOf(ZoneType.class);
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public PlayerView getPlayer() {
        return playerCache;
    }

    public PlayerView getPlayer(Tracker tracker) {
        if (playerCache == null && tracker != null) {
            playerCache = tracker.getObj(TrackableTypes.PlayerViewType, playerId);
        }
        return playerCache;
    }
    public Set<ZoneType> getZones() {
        return zones;
    }

    void addZone(final ZoneType zone) {
        if (zone == null) {
            return;
        }
        zones.add(zone);
    }
    void add(final PlayerZoneUpdate other) {
        if (other == null) {
            return;
        }
        zones.addAll(other.getZones());
    }
}
