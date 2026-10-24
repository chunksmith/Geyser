package org.geysermc.geyser.network.bedrock.nethernet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannel;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChildChannel;
import org.geysermc.geyser.GeyserImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import tel.schich.libdatachannel.DataChannel;

import static org.mockito.Mockito.*;

class NetherNetBufferGuardTest {

    @Test
    void passesWritesWhileTheBufferDrains() {
        ByteBuf message = Unpooled.buffer(1);
        ChannelPromise promise = mock(ChannelPromise.class);
        ChannelHandlerContext ctx = write(NetherNetBufferGuard.MAX_BUFFERED_BYTES, message, promise);

        verify(ctx).write(message, promise);
        verify(ctx, never()).close();
    }

    @Test
    void closesBeforeWritingWhenTheBufferIsStuck() {
        ByteBuf message = Unpooled.buffer(1);
        ChannelPromise promise = mock(ChannelPromise.class);
        ChannelHandlerContext ctx;
        try (var geyser = mockStatic(GeyserImpl.class)) {
            geyser.when(GeyserImpl::getInstance).thenReturn(mock(GeyserImpl.class, RETURNS_DEEP_STUBS));
            ctx = write(NetherNetBufferGuard.MAX_BUFFERED_BYTES + 1, message, promise);
        }

        InOrder order = inOrder(ctx);
        order.verify(ctx).close();
        order.verify(ctx).write(message, promise);
    }

    private static ChannelHandlerContext write(int buffered, ByteBuf message, ChannelPromise promise) {
        DataChannel reliable = mock(DataChannel.class);
        when(reliable.isOpen()).thenReturn(true);
        when(reliable.bufferedAmount()).thenReturn(buffered);
        NetherNetChannel channel = mock(NetherNetChildChannel.class);
        NetherNetBufferGuard.RELIABLE_CHANNEL.set(channel, reliable);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);
        NetherNetBufferGuard.INSTANCE.write(ctx, message, promise);
        return ctx;
    }
}
