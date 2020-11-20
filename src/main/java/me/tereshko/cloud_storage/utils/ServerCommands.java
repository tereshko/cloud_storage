package me.tereshko.cloud_storage.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;


public class ServerCommands {
    private ByteBuf buffer;

    public void sendCommand(Channel channel, String command) {
        System.out.println("send command server: " + command);
        buffer = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + command.length());
        buffer.writeByte(NetworkSignalsForAction.COMMAND_BYTE);
        buffer.writeInt(command.length());
        buffer.writeBytes(command.getBytes());
        channel.writeAndFlush(buffer);
    }

}
