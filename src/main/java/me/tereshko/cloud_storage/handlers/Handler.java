package me.tereshko.cloud_storage.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class Handler extends ChannelInboundHandlerAdapter {

    //public static final ConcurrentLinkedDeque<ChannelHandlerContext> channels = new ConcurrentLinkedDeque<>();
    private int commandLength = 0;
    private StringBuilder sb;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected");
//        channels.add(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = ((ByteBuf) msg);
        byte readBuffer = 0;

        sb = new StringBuilder();
        while (buffer.readableBytes() > 0) {
            commandLength--;
            sb.append((char) buffer.readByte());
        }

        System.out.println(sb.toString());



//        System.out.println("Message from client: " + s);
//
//        String lineFromServer = s;
//        int x = lineFromServer.indexOf("/");
//        int y = lineFromServer.lastIndexOf("/username");
//        String actionType = lineFromServer.substring(x + 1, y);
//        System.out.println("Action: " + actionType);
//
//        lineFromServer = s;
//
//        x = lineFromServer.indexOf("/username:");
//        y = lineFromServer.lastIndexOf("/password:");
//        String userName = lineFromServer.substring(x + 10, y);
//        System.out.println("Username: " + userName);
//
//        lineFromServer = s;
//
//        x = lineFromServer.indexOf("/password:");
//        y = lineFromServer.lastIndexOf("");
//        String password = lineFromServer.substring(x + 10, y);
//        System.out.println("Password: " + password);
//
//        ConnectionDB connectionDB = new ConnectionDB();
//
//        int userID = -1;
//        String folderName = null;
//
//        String answerToClient = String.valueOf(userID);
//
//        switch (actionType) {
//            case "authorization":
//                int ID = connectionDB.getIDFromUsername(userName);
//                boolean isPasswordEquals = connectionDB.comparePass(ID, password);
//                if (isPasswordEquals) {
//                    answerToClient = "authOK";
//                }
//                break;
//            case "registration":
//                int ifUserExist = connectionDB.getIDFromUsername(userName);
//                if (ifUserExist > -1) {
//                    answerToClient = "regNotOK";
//                } else {
//                    userID = connectionDB.addUser(userName, password);
//                    folderName = connectionDB.createNewFolder(userID);
//                    if (userID > -1) {
//                        folderName = connectionDB.createNewFolder(userID);
//                        answerToClient = folderName;
//                    } else {
//                        answerToClient = "regNotOK";
//                    }
//                }
//                break;
//            default:
//                //TODO do something
//        }
//
//
//        //send to server
//        String finalAnswerToClient = answerToClient;
//        channels.forEach(c -> c.writeAndFlush(finalAnswerToClient));
    }
}
