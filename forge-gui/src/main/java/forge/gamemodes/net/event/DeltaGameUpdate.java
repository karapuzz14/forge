package forge.gamemodes.net.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.gamemodes.net.server.RemoteClient;
import forge.trackable.TrackableObject;
import forge.trackable.TrackableProperty;

public final class DeltaGameUpdate implements NetEvent {
    private static final long serialVersionUID = 3L;

    private final List<ObjectDelta> deltas = new ArrayList<>();

    public void addDelta(int objectId, String objectType, Map<TrackableProperty, Object> props) {
        deltas.add(new ObjectDelta(objectId, objectType, optimizeProps(props)));
    }

    private static Map<Integer, Object> optimizeProps(Map<TrackableProperty, Object> props) {
        Map<Integer, Object> optimized = new HashMap<>();
        for (Map.Entry<TrackableProperty, Object> e : props.entrySet()) {
            optimized.put(e.getKey().ordinal(), optimizeValue(e.getValue()));
        }
        return optimized;
    }

    private static Object optimizeValue(Object value) {
        if (value == null) return null;
        if (ViewRef.isTrackableView(value)) {
            return new ViewRef((TrackableObject) value);
        }
        // TrackableCollection may contain NEW objects not yet on client - don't optimize
        // ViewRef only works for objects client already received
        return value;
    }

    public List<ObjectDelta> getDeltas() {
        return deltas;
    }

    public boolean isEmpty() {
        return deltas.isEmpty();
    }

    @Override
    public void updateForClient(RemoteClient client) {
    }

    public static class ObjectDelta implements java.io.Serializable {
        private static final long serialVersionUID = 2L;

        private final int objectId;
        private final String objectType;
        private final Map<Integer, Object> props;

        public ObjectDelta(int objectId, String objectType, Map<Integer, Object> props) {
            this.objectId = objectId;
            this.objectType = objectType;
            this.props = props;
        }

        public int getObjectId() {
            return objectId;
        }

        public String getObjectType() {
            return objectType;
        }

        public Map<Integer, Object> getProps() {
            return props;
        }
    }
}

