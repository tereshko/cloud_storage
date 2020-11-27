package Network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import utils.Condition;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Handler extends ChannelInboundHandlerAdapter {
    Condition condition = Condition.WAIT;
    StringBuilder commandRead;
    private int commandLength = 0;
    private Callback callback;
    private Path currentPath = Paths.get("user/");
    private final String LOCAL_PATH = "user";

    private int fileNameLength = 0;
    private long fileSize = 0L;
    BufferedOutputStream out;
    private long receivedFileSize = 0L;

    public void getCallback(Callback callbackMessage) {
        this.callback = callbackMessage;
    }


    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath ;
    }

    public Path getCurrentPath() {
        return this.currentPath;
    }


    @Override
    public void channelRead(ChannelHandlerContext currentChannel, Object msg) {
        ByteBuf buffer = ((ByteBuf) msg);
        System.out.println("CHANNEL READ");
        while (buffer.readableBytes() > 0) {
            if (condition == Condition.WAIT) {
                byte read = buffer.readByte();
                if (read == NetworkSignalsForAction.COMMAND_BYTE) {
                    condition = Condition.COMMAND;
                    System.out.println("Command COMMAND_BYTE");
                } else if (read == NetworkSignalsForAction.FILE_BYTE) {
                    condition = Condition.FILE_NAME_LENGTH;
                    System.out.println("Command NAME_LENGTH");
                } else {
                    condition = Condition.WAIT;
                    throw new RuntimeException("Unknown command");
                }
            }

            if (condition == Condition.COMMAND) {
                if (buffer.readableBytes() >= 4) {
                    commandLength = buffer.readInt();
                    condition = Condition.COMMAND_READED;
                }
                System.out.println("commandLength: " + commandLength);
            }

            if (condition == Condition.COMMAND_READED) {
                commandRead = new StringBuilder();
                while (buffer.readableBytes() > 0 && commandLength != 0) {
                    commandLength--;
                    commandRead.append((char) buffer.readByte());
                }

                System.out.println("Condition.COMMAND_READED: " + commandRead.toString());
                condition = Condition.NEED_ACTION;
            }

            if (condition == Condition.NEED_ACTION) {
                String[] split = commandRead.toString().split("\n");
                switch (split[0]) {

                    case "auth/reg":
                        String answer = split[1];
                        if (answer.equals("NotOK")) {
                            callback.callback("notOk");
                            //TODO can not authorize
                        } else {
                            this.setCurrentPath(Paths.get(answer));
                            callback.callback("serverPath\n" +answer);
                            System.out.println("Folder: " + answer);
                        }
                        condition = Condition.WAIT;
                        break;
                    case "SERVER_FILE_LIST":
                        callback.callback(commandRead.toString());
                        condition = Condition.WAIT;
                        break;
                    case "serverPath":
                        //String userPath = split[1];
                        callback.callback(commandRead.toString());
                        condition = Condition.WAIT;
                        break;
                    default:
                        condition = Condition.WAIT;
                }
            }

            if (condition == Condition.FILE_NAME_LENGTH) {
                if (buffer.readableBytes() >= 4) {
                    fileNameLength = buffer.readInt();
                    System.out.println("fileNameLength: " + fileNameLength);
                    condition = Condition.FILE_NAME;
                }
            }

            if (condition == Condition.FILE_NAME) {
                if (buffer.readableBytes() >= fileNameLength) {
                    byte[] filenameBytes = new byte[fileNameLength];
                    buffer.readBytes(filenameBytes);
                    String fileName = new String(filenameBytes, StandardCharsets.UTF_8);
                    //path
                    File file = new File(currentPath.toString() + File.separator + fileName);
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
                if (buffer.readableBytes() >= 8) {
                    fileSize = buffer.readLong();
                    System.out.println("fileSize: " + fileSize);
                    condition = Condition.FILE;
                }
            }

            if (condition == Condition.FILE) {
                while (buffer.readableBytes() > 0) {
                    try {
                        out.write(buffer.readByte());
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

        if (buffer.readableBytes() == 0) {
            buffer.release();
        }
    }
}
