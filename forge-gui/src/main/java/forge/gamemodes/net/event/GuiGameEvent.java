package forge.gamemodes.net.event;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import forge.gamemodes.net.ProtocolMethod;
import forge.gamemodes.net.server.RemoteClient;
import forge.trackable.TrackableCollection;
import forge.trackable.TrackableObject;

public final class GuiGameEvent implements IdentifiableNetEvent {
    private static final long serialVersionUID = 6223690008522514575L;
    private static int staticId = 0;

    private final int id;
    private final ProtocolMethod method;
    private final Object[] objects;

    public GuiGameEvent(final ProtocolMethod method, final Object ... objects) {
        this.id = staticId++;
        this.method = method;
        this.objects = objects == null ? new Object[0] : objects;
    }

    @Override
    public String toString() {
        return String.format("GuiGameEvent %d: %s (%d args)", id, method, objects.length);
    }

    @Override
    public void updateForClient(final RemoteClient client) {
    }

    @Override
    public int getId() {
        return id;
    }

    public ProtocolMethod getMethod() {
        return method;
    }

    public Object[] getObjects() {
        return objects;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private Object writeReplace() {
        if (shouldOptimize()) {
            return new OptimizedGuiGameEvent(id, method, optimizeObjects(objects));
        }
        return this;
    }

    private boolean shouldOptimize() {
        switch (method) {
            case setGameView:
            case openView:
            case useMana:
            case undoLastAction:
            case selectPlayer:
            case selectCard:
            case selectButtonOk:
            case selectButtonCancel:
            case selectAbility:
            case passPriorityUntilEndOfTurn:
            case passPriority:
            case nextGameDecision:
            case getActivateDescription:
            case concede:
            case alphaStrike:
            case reorderHand:
                return false;
            default:
                return true;
        }
    }

    private static Object[] optimizeObjects(Object[] objects) {
        if (objects == null || objects.length == 0) {
            return objects;
        }
        Object[] optimized = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            optimized[i] = optimizeObject(objects[i]);
        }
        return optimized;
    }

    @SuppressWarnings("unchecked")
    private static Object optimizeObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (ViewRef.isTrackableView(obj)) {
            return new ViewRef((TrackableObject) obj);
        }
        if (obj instanceof TrackableCollection) {
            TrackableCollection<?> tc = (TrackableCollection<?>) obj;
            List<Object> list = new ArrayList<>();
            for (Object item : tc) {
                list.add(optimizeObject(item));
            }
            return new ViewRefCollection(list);
        }
        if (obj instanceof Iterable && !(obj instanceof String)) {
            List<Object> list = new ArrayList<>();
            boolean hasViewRef = false;
            for (Object item : (Iterable<?>) obj) {
                Object optimized = optimizeObject(item);
                list.add(optimized);
                if (optimized instanceof ViewRef) {
                    hasViewRef = true;
                }
            }
            if (hasViewRef) {
                return new ViewRefCollection(list);
            }
        }
        return obj;
    }
}
