package org.geysermc.geyser.network.bedrock.nethernet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannel;
import org.geysermc.geyser.GeyserImpl;
import tel.schich.libdatachannel.DataChannel;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

@ChannelHandler.Sharable
public class NetherNetBufferGuard extends ChannelOutboundHandlerAdapter {
    public static final String NAME = "nethernet-buffer-guard";
    public static final NetherNetBufferGuard INSTANCE = new NetherNetBufferGuard();

    static final int MAX_BUFFERED_BYTES = Integer.getInteger("geyserNetherNetMaxBufferedBytes", 8 * 1024 * 1024);
    static final VarHandle RELIABLE_CHANNEL;

    static {
        try {
            RELIABLE_CHANNEL = MethodHandles.privateLookupIn(NetherNetChannel.class, MethodHandles.lookup())
                    .findVarHandle(NetherNetChannel.class, "reliableChannel", DataChannel.class);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        DataChannel reliable = (DataChannel) RELIABLE_CHANNEL.get((NetherNetChannel) ctx.channel());
        int buffered = reliable == null ? 0 : reliable.bufferedAmount();
        if (buffered > MAX_BUFFERED_BYTES) {
            GeyserImpl.getInstance().getLogger().warning("Closing NetherNet connection " + ctx.channel().remoteAddress()
                    + ": " + buffered + " bytes stuck in the send buffer");
            ctx.close();
        }
        ctx.write(msg, promise);
    }
}
