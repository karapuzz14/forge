package forge.gamemodes.net.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import forge.trackable.Tracker;

public final class ViewRefCollection implements Serializable, Iterable<Object> {
    private static final long serialVersionUID = 1L;
    
    private final List<Object> items;
    
    public ViewRefCollection(List<Object> items) {
        this.items = items;
    }
    
    public List<Object> getItems() {
        return items;
    }
    
    @Override
    public Iterator<Object> iterator() {
        return items.iterator();
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> resolve(Tracker tracker) {
        List<T> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof ViewRef) {
                result.add((T) ((ViewRef) item).resolve(tracker));
            } else {
                result.add((T) item);
            }
        }
        return result;
    }
}

