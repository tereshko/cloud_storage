package me.tereshko.cloud_storage.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import me.tereshko.cloud_storage.db.ConnectionDB;
import me.tereshko.cloud_storage.utils.Condition;
import me.tereshko.cloud_storage.utils.NetworkSignalsForAction;
import me.tereshko.cloud_storage.utils.ServerCommands;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Handler extends ChannelInboundHandlerAdapter {

    private Condition condition = Condition.WAIT;
    private int commandLength = 0;
    StringBuilder commandRead;
    String answerToClient = "NotOK";
    int userID = -1;
    ConnectionDB connectionDB = new ConnectionDB();
    ServerCommands serverCommands = new ServerCommands();


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
                    condition = Condition.NAME_LENGTH;
                    System.out.println("Command NAME_LENGTH");
                } else {
                    condition = Condition.WAIT;
                    throw new RuntimeException("Unknown command");
                }
            }

            if (condition == Condition.COMMAND) {
                System.out.println("commandLength: " + commandLength);
                if (buf.readableBytes() >= 4) {
                    System.out.println("commandLength: " + commandLength);
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
                String commandForDo = commandRead.toString();
                String[] split = commandForDo.split("\n");
                System.out.println("split 0: " + split[0]);
                System.out.println("split 1: " + split[1]);
                System.out.println("split 2: " + split[2]);
                switch (split[0]) {
                    case "authorization":
                        System.out.println("authorization");
                        int ID = connectionDB.getIDFromUsername(split[1]);
                        boolean isPasswordEquals = connectionDB.comparePass(ID, split[2]);
                        if (isPasswordEquals) {
                            answerToClient = connectionDB.getFolderName(ID);
                        } else {
                            answerToClient = "NotOK";
                        }
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
                            } else {
                                answerToClient = "NotOK";
                            }
                            condition = Condition.WAIT;
                            break;
                        }
                    default:
                        condition = Condition.WAIT;
                        //TODO do something
                }
                answerToClient = "auth/reg\n" + answerToClient;
                serverCommands.sendCommand(channelHandlerContext.channel(), answerToClient);
                System.out.println("ANSWER: " + answerToClient);
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
