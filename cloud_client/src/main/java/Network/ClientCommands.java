package Network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.concurrent.FutureListener;
import utils.FileInfo;
import utils.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientCommands {
    private ByteBuf buffer;

    public void sendCommand(Channel channel, String command) {
        System.out.println("send command: " + command);
        buffer = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + command.length());
        buffer.writeByte(NetworkSignalsForAction.COMMAND_BYTE);
        buffer.writeInt(command.length());
        buffer.writeBytes(command.getBytes());
        channel.writeAndFlush(buffer);
    }

    public List<FileInfo> createFileList(String s) {
        List<FileInfo> temp = new ArrayList<>();
        String[] files = s.split("\n");
        for (String file : files) {
            String[] tmp = file.split(",");
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(tmp[0]);
            fileInfo.setSize(Long.parseLong(tmp[1]));
            if (tmp[2].equals("FILE")) {
                fileInfo.setType(FileType.FILE);
            } else {
                fileInfo.setType(FileType.DIRECTORY);
            }
            fileInfo.setLastModified(LocalDateTime.parse(tmp[3]));
            temp.add(fileInfo);
        }
        return temp;
    }

    public void uploadFile(Channel channel, FutureListener listener, Path path) {
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
    }

    public void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDirectory(Path path, String folderName) {
        File file = new File(path + File.separator + folderName);
        if (file.exists()) {
            try {
                throw new Exception("The directory is already exists");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            file.mkdir();
        }
    }
}
