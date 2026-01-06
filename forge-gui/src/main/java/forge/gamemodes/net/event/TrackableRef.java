package forge.gamemodes.net.event;

import java.io.Serializable;

import forge.trackable.TrackableObject;
import forge.trackable.TrackableTypes;
import forge.trackable.Tracker;

public final class TrackableRef<T extends TrackableObject> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int id;
    private final String typeName;
    
    public TrackableRef(T obj) {
        this.id = obj != null ? obj.getId() : -1;
        this.typeName = obj != null ? obj.getClass().getSimpleName() : null;
    }
    
    public int getId() {
        return id;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public boolean isNull() {
        return id == -1;
    }
    
    @SuppressWarnings("unchecked")
    public T resolve(Tracker tracker) {
        if (id == -1 || tracker == null || typeName == null) {
            return null;
        }
        switch (typeName) {
            case "PlayerView":
                return (T) tracker.getObj(TrackableTypes.PlayerViewType, id);
            case "CardView":
                return (T) tracker.getObj(TrackableTypes.CardViewType, id);
            case "CombatView":
                return (T) tracker.getObj(TrackableTypes.CombatViewType, id);
            case "StackItemView":
                return (T) tracker.getObj(TrackableTypes.StackItemViewType, id);
            default:
                return null;
        }
    }
}

