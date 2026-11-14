package org.geysermc.geyser.network.java;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

public final class RemoteSession extends ClientNetworkSession {
    private final EventLoop channelEventLoop;

    public RemoteSession(SocketAddress targetAddress, MinecraftProtocol protocol, EventLoop tickEventLoop, boolean shareTickEventLoop) {
        super(targetAddress, protocol, shareTickEventLoop ? inline(tickEventLoop) : tickEventLoop, null, null);
        this.channelEventLoop = shareTickEventLoop ? tickEventLoop : null;
    }

    private static Executor inline(EventLoop eventLoop) {
        return task -> {
            if (!eventLoop.inEventLoop()) {
                eventLoop.execute(task);
                return;
            }
            try {
                task.run();
            } catch (Throwable throwable) {
                GeyserImpl.getInstance().getLogger().error("Error while handling a Java packet", throwable);
            }
        };
    }

    @Override
    protected EventLoopGroup getEventLoopGroup() {
        return channelEventLoop != null ? channelEventLoop : super.getEventLoopGroup();
    }

    @Override
    protected ChannelHandler getChannelHandler() {
        ChannelHandler initializer = super.getChannelHandler();
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(initializer);
                channel.pipeline().addBefore("codec", IgnoredPacketFilter.NAME, new IgnoredPacketFilter(getPacketProtocol()));
            }
        };
    }
}
