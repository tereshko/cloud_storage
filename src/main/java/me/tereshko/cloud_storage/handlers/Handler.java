package me.tereshko.cloud_storage.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.tereshko.cloud_storage.db.ConnectionDB;
import me.tereshko.cloud_storage.utils.Condition;
import me.tereshko.cloud_storage.utils.FileInfo;
import me.tereshko.cloud_storage.utils.NetworkSignalsForAction;
import me.tereshko.cloud_storage.utils.ServerCommands;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class Handler extends ChannelInboundHandlerAdapter {

    private Condition condition = Condition.WAIT;
    private int commandLength = 0;
    StringBuilder commandRead;
    String answerToClient = "NotOK";
    int userID = -1;
    ConnectionDB connectionDB = new ConnectionDB();
    ServerCommands serverCommands = new ServerCommands();
    private Path userPath;
    private final String LOCAL_PATH = "user";
    private int fileNameLength = 0;
    private long fileSize = 0L;
    BufferedOutputStream out;
    private long receivedFileSize = 0L;
    String userFolder;


    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        System.out.println("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        System.out.println("Client disconnected");
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        ByteBuf buf = ((ByteBuf) object);
        System.out.println("CHANNEL READ");

        while (buf.readableBytes() > 0) {
            if (condition == Condition.WAIT) {
                byte read = buf.readByte();
                if (read == NetworkSignalsForAction.COMMAND_BYTE) {
                    condition = Condition.COMMAND;
                    System.out.println("Command COMMAND_BYTE");
                } else if (read == NetworkSignalsForAction.FILE_BYTE) {
                    condition = Condition.FILE_NAME_LENGTH;
                    System.out.println("Command FILE_NAME_LENGTH");
                } else {
                    condition = Condition.WAIT;
                    throw new RuntimeException("Unknown command");
                }
            }

            if (condition == Condition.COMMAND) {
                if (buf.readableBytes() >= 4) {
                    commandLength = buf.readInt();
                    condition = Condition.COMMAND_READED;
                }
                System.out.println("commandLength: " + commandLength);
            }

            if (condition == Condition.COMMAND_READED) {
                commandRead = new StringBuilder();
                while (buf.readableBytes() > 0 && commandLength != 0) {
                    commandLength--;
                    commandRead.append((char) buf.readByte());
                }

                System.out.println("Condition.COMMAND_READED: " + commandRead.toString());
                condition = Condition.NEED_ACTION;
            }

            if (condition == Condition.NEED_ACTION) {
                String[] split = commandRead.toString().split("\n");
                switch (split[0]) {

                    case "authorization":
                        System.out.println("authorization");
                        int ID = connectionDB.getIDFromUsername(split[1]);
                        boolean isPasswordEquals = connectionDB.comparePass(ID, split[2]);
                        if (isPasswordEquals) {
                            answerToClient = connectionDB.getFolderName(ID);
                            Path newPath = Paths.get(LOCAL_PATH, answerToClient);
                            userFolder = answerToClient;
                            userPath = newPath;

                            if (!Files.exists(newPath)) {
                                try {
                                    Files.createDirectory(newPath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            answerToClient = "NotOK";
                        }
                        answerToClient = "auth/reg\n" + answerToClient;
                        serverCommands.sendCommand(channelHandlerContext.channel(), answerToClient);
                        condition = Condition.WAIT;
                        break;

                    case "registration":
                        System.out.println("registration");
                        int ifUserExist = connectionDB.getIDFromUsername(split[1]);
                        if (ifUserExist > -1) {
                            answerToClient = "NotOK";
                        } else {
                            userID = connectionDB.addUser(split[1], split[2]);
                            if (userID > -1) {
                                answerToClient = connectionDB.createNewFolder(userID);
                                Path newPath = Paths.get(LOCAL_PATH, answerToClient);
                                if (!Files.exists(newPath)) {
                                    try {
                                        Files.createDirectory(newPath);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                answerToClient = "NotOK";
                            }
                            answerToClient = "auth/reg\n" + answerToClient;
                            serverCommands.sendCommand(channelHandlerContext.channel(), answerToClient);
                            condition = Condition.WAIT;
                            break;
                        }

                    case "SERVER_FILE_LIST":
                        condition = Condition.SERVER_FILE_LIST;
                        break;

                    case "download":
                        System.out.println("case download");
                        System.out.println("file name: " + userPath.resolve(split[1]));
                        serverCommands.uploadFile(channelHandlerContext.channel(), null, userPath.resolve(split[1]));
                        condition = Condition.WAIT;
                        break;

                    case "updateServerFilesList":
                        condition = Condition.SERVER_FILE_LIST;
                        break;

                    case "deleteFile":
                        serverCommands.deleteFile(userPath.resolve(split[1]));
                        condition = Condition.SERVER_FILE_LIST;
                        break;

                    case "mkdir":
                        serverCommands.createDirectory(userPath, split[1]);
                        condition = Condition.SERVER_FILE_LIST;
                        break;

                    case "openDirectory":
                        System.out.println("DIRECTORY NAME:  " + split[1]);
                        userPath = userPath.resolve(split[1]);
                        serverCommands.sendCommand(channelHandlerContext.channel(), "serverPath\n" + userPath.toString());
                        condition = Condition.SERVER_FILE_LIST;
                        break;

                    case "upDirectory":
                        if (userPath.getParent().toString().equals("user")) {
                            condition = Condition.WAIT;
                        } else {
                            System.out.println("USER FOLDER NOW IS: " + userPath.toString());
                            userPath = userPath.getParent();
                            serverCommands.sendCommand(channelHandlerContext.channel(), "serverPath\n" + userPath.toString());
                            condition = Condition.SERVER_FILE_LIST;
                        }

                        break;
                    default:
                        condition = Condition.WAIT;
                        //TODO do something
                }
            }


            if (condition == Condition.SERVER_FILE_LIST) {
                try {
                    List<FileInfo> fileListOnTheServer = Files.list(userPath).map(FileInfo::new).collect(Collectors.toList());
                    StringBuilder builder = new StringBuilder();
                    for (FileInfo fileInfo : fileListOnTheServer) {
                        builder.append(String.format("%s,%d,%s,%s\n",
                                fileInfo.getFileName(),
                                fileInfo.getSize(),
                                fileInfo.getType(),
                                fileInfo.getLastModified()));
                    }
                    System.out.println("Sended to client: " + "SERVER_FILE_LIST" + builder.toString());

                    serverCommands.sendCommand(channelHandlerContext.channel(), "SERVER_FILE_LIST\n" + builder.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                condition = Condition.WAIT;
            }

            if (condition == Condition.FILE_NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    System.out.println("fileNameLength: " + fileNameLength);
                    condition = Condition.FILE_NAME;
                }
            }

            if (condition == Condition.FILE_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] filenameBytes = new byte[fileNameLength];
                    buf.readBytes(filenameBytes);
                    String fileName = new String(filenameBytes, StandardCharsets.UTF_8);
                    File file = new File(userPath.toString() + File.separator + fileName);
                    try {
                        out = new BufferedOutputStream(new FileOutputStream((file)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println("fileName: " + fileName);
                    condition = Condition.FILE_LENGTH;
                }
            }

            if (condition == Condition.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    System.out.println("fileSize: " + fileSize);
                    condition = Condition.FILE;
                }
            }

            if (condition == Condition.FILE) {
                while (buf.readableBytes() > 0) {
                    try {
                        out.write(buf.readByte());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    receivedFileSize++;
                    if (fileSize == receivedFileSize) {
                        condition = Condition.SERVER_FILE_LIST;
                        System.out.println("File received");
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
