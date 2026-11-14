package org.geysermc.geyser.network.java;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.geysermc.geyser.registry.PacketTranslatorRegistry;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.mcprotocollib.network.codec.PacketDefinition;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;

final class IgnoredPacketFilter extends ChannelInboundHandlerAdapter {
    static final String NAME = "geyser-ignored-packet-filter";

    private final MinecraftProtocol protocol;

    IgnoredPacketFilter(MinecraftProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf buf && isIgnored(buf)) {
            buf.release();
            return;
        }
        ctx.fireChannelRead(msg);
    }

    private boolean isIgnored(ByteBuf buf) {
        if (protocol.getInboundState() != ProtocolState.GAME || !buf.isReadable()) return false;

        int id = protocol.getPacketHeader().readPacketId(buf.duplicate());
        if (id < 0) return false;

        PacketDefinition<?> definition = protocol.getInboundPacketRegistry().getClientboundDefinition(id);
        return PacketTranslatorRegistry.isIgnoredPacket(definition.getPacketClass())
            && Registries.JAVA_PACKET_TRANSLATORS.get(definition.getPacketClass()) == null;
    }
}
