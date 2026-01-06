package forge.gamemodes.net.event;

import java.io.Serializable;

import forge.game.card.CardView;
import forge.game.combat.CombatView;
import forge.game.player.PlayerView;
import forge.game.spellability.StackItemView;
import forge.trackable.TrackableObject;
import forge.trackable.TrackableTypes;
import forge.trackable.Tracker;

public final class ViewRef implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_PLAYER = 1;
    public static final byte TYPE_CARD = 2;
    public static final byte TYPE_COMBAT = 3;
    public static final byte TYPE_STACK_ITEM = 4;
    
    private final int id;
    private final byte type;
    
    public ViewRef(TrackableObject obj) {
        if (obj == null) {
            this.id = -1;
            this.type = 0;
        } else {
            this.id = obj.getId();
            this.type = getTypeCode(obj);
        }
    }
    
    private static byte getTypeCode(TrackableObject obj) {
        if (obj instanceof PlayerView) return TYPE_PLAYER;
        if (obj instanceof CardView) return TYPE_CARD;
        if (obj instanceof CombatView) return TYPE_COMBAT;
        if (obj instanceof StackItemView) return TYPE_STACK_ITEM;
        return 0;
    }
    
    public int getId() {
        return id;
    }
    
    public byte getType() {
        return type;
    }
    
    public boolean isNull() {
        return id == -1;
    }
    
    public Object resolve(Tracker tracker) {
        if (id == -1 || tracker == null) {
            return null;
        }
        switch (type) {
            case TYPE_PLAYER:
                return tracker.getObj(TrackableTypes.PlayerViewType, id);
            case TYPE_CARD:
                return tracker.getObj(TrackableTypes.CardViewType, id);
            case TYPE_COMBAT:
                return tracker.getObj(TrackableTypes.CombatViewType, id);
            case TYPE_STACK_ITEM:
                return tracker.getObj(TrackableTypes.StackItemViewType, id);
            default:
                return null;
        }
    }
    
    public static boolean isTrackableView(Object obj) {
        return obj instanceof PlayerView 
            || obj instanceof CardView 
            || obj instanceof CombatView 
            || obj instanceof StackItemView;
    }
}

