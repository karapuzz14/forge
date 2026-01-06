package forge.gamemodes.net.event;

import forge.gamemodes.net.ProtocolMethod;
import forge.gamemodes.net.server.RemoteClient;

public final class OptimizedGuiGameEvent implements IdentifiableNetEvent {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final ProtocolMethod method;
    private final Object[] objects;

    public OptimizedGuiGameEvent(int id, ProtocolMethod method, Object[] objects) {
        this.id = id;
        this.method = method;
        this.objects = objects;
    }

    @Override
    public String toString() {
        return String.format("OptimizedGuiGameEvent %d: %s (%d args)", id, method, objects.length);
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
}

