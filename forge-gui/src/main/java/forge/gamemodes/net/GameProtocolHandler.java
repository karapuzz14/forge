package forge.gamemodes.net;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import forge.gamemodes.net.event.GuiGameEvent;
import forge.gamemodes.net.event.OptimizedGuiGameEvent;
import forge.gamemodes.net.event.ReplyEvent;
import forge.gamemodes.net.event.ViewRef;
import forge.gamemodes.net.event.ViewRefCollection;
import forge.gui.FThreads;
import forge.gui.util.SOptionPane;
import forge.localinstance.skin.FSkinProp;
import forge.trackable.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class GameProtocolHandler<T> extends ChannelInboundHandlerAdapter {

    private final boolean runInEdt;
    protected GameProtocolHandler(final boolean runInEdt) {
        this.runInEdt = runInEdt;
    }

    protected abstract ReplyPool getReplyPool(ChannelHandlerContext ctx);
    protected abstract IRemote getRemote(ChannelHandlerContext ctx);

    protected abstract T getToInvoke(ChannelHandlerContext ctx);
    protected abstract void beforeCall(ProtocolMethod protocolMethod, Object[] args);
    protected Tracker getTracker() { return null; }

    @Override
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        final String[] catchedError = {""};
        System.out.println("Received: " + msg);
        if (msg instanceof ReplyEvent) {
            final ReplyEvent event = (ReplyEvent) msg;
            getReplyPool(ctx).complete(event.getIndex(), event.getReply());
        } else if (msg instanceof OptimizedGuiGameEvent) {
            final OptimizedGuiGameEvent event = (OptimizedGuiGameEvent) msg;
            handleGameEvent(ctx, event.getMethod(), resolveObjects(event.getObjects()), event.getId(), catchedError);
        } else if (msg instanceof GuiGameEvent) {
            final GuiGameEvent event = (GuiGameEvent) msg;
            handleGameEvent(ctx, event.getMethod(), event.getObjects(), event.getId(), catchedError);
        }
    }

    private Object[] resolveObjects(Object[] objects) {
        if (objects == null) return null;
        Tracker tracker = getTracker();
        if (tracker == null) return objects;
        
        Object[] resolved = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            resolved[i] = resolveObject(objects[i], tracker);
        }
        return resolved;
    }

    private Object resolveObject(Object obj, Tracker tracker) {
        if (obj instanceof ViewRef) {
            return ((ViewRef) obj).resolve(tracker);
        }
        if (obj instanceof ViewRefCollection) {
            return ((ViewRefCollection) obj).resolve(tracker);
        }
        return obj;
    }

    private void handleGameEvent(ChannelHandlerContext ctx, ProtocolMethod protocolMethod, Object[] args, int eventId, String[] catchedError) {
        final String methodName = protocolMethod.name();

        final Method method = protocolMethod.getMethod();
        if (method == null) {
            catchedError[0] += String.format("IllegalStateException: Method %s not found (GameProtocolHandler.java)\n", protocolMethod.name());
            System.err.printf("Method %s not found%n", protocolMethod.name());
            return;
        }

        protocolMethod.checkArgs(args);

        final Object toInvoke = getToInvoke(ctx);

        beforeCall(protocolMethod, args);

        final Class<?> returnType = protocolMethod.getReturnType();
        final Runnable toRun = () -> {
            if (returnType.equals(Void.TYPE)) {
                try {
                    method.invoke(toInvoke, args);
                } catch (final IllegalAccessException | IllegalArgumentException e) {
                    System.err.printf("Unknown protocol method %s with %d args%n", methodName, args == null ? 0 : args.length);
                } catch (final InvocationTargetException e) {
                    catchedError[0] += (String.format("RuntimeException: %s (GameProtocolHandler.java)\n", e.getTargetException().toString()));
                    System.err.println(e.getTargetException().toString());
                }
            } else {
                Serializable reply = null;
                try {
                    final Object theReply = method.invoke(toInvoke, args);
                    if (theReply instanceof Serializable) {
                        protocolMethod.checkReturnValue(theReply);
                        reply = (Serializable) theReply;
                    } else if (theReply != null) {
                        System.err.printf("Non-serializable return type %s for method %s, returning null%n", returnType.getName(), methodName);
                    }
                } catch (final IllegalAccessException | IllegalArgumentException e) {
                    System.err.printf("Unknown protocol method %s with %d args, replying with null%n", methodName, args == null ? 0 : args.length);
                } catch (final NullPointerException | InvocationTargetException e) {
                    catchedError[0] += e.toString();
                    SOptionPane.showMessageDialog(catchedError[0], "Error", FSkinProp.ICO_WARNING);
                    System.err.println(e.toString());
                }
                getRemote(ctx).send(new ReplyEvent(eventId, reply));
            }
        };

        if (runInEdt) {
            FThreads.invokeInEdtNowOrLater(toRun);
        } else {
            FThreads.invokeInBackgroundThread(toRun);
        }
    }

    @Override
    public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
