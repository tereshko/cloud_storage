package me.tereshko.cloud_storage.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.concurrent.FutureListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void uploadFile(Channel channel, FutureListener listener, Path path) {
        System.out.println("server uploadFile");
        FileRegion fileRegion = null;
        try {
            fileRegion = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path.normalize()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buffer = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length + 8);
        buffer.writeByte(NetworkSignalsForAction.FILE_BYTE);
        buffer.writeInt(path.getFileName().toString().length());
        buffer.writeBytes(filenameBytes);
        try {
            buffer.writeLong(Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel.writeAndFlush(buffer);
        ChannelFuture future = channel.writeAndFlush(fileRegion);
        if (listener != null) {
            future.addListener(listener);
        }
        System.out.println("server uploadFile end");
    }

    public void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDirectory(Path path, String folderName) {
        System.out.println("Folder Name: " + folderName);
        File file = new File(path + File.separator + folderName);
        if (file.exists()) {
            try {
                throw new Exception("Folder is already exists");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            file.mkdir();
        }
    }

}
