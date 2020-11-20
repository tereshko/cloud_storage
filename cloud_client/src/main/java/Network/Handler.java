package Network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import utils.Condition;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Handler extends ChannelInboundHandlerAdapter {
    Condition condition = Condition.WAIT;
    StringBuilder commandRead;
    private int commandLength = 0;
    private Callback callback;
    private Path currentPath = Paths.get("new");

    public void getCallback(Callback callbackMessage) {
        this.callback = callbackMessage;
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
                    condition = Condition.NAME_LENGTH;
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
                            callback.callback(answer);
                            System.out.println("Folder: " + answer);
                        }
                        condition = Condition.WAIT;
                        break;
                    case "SERVER_FILE_LIST":
                        callback.callback(commandRead.toString());
                        condition = Condition.WAIT;
                        break;

                    default:
                        condition = Condition.WAIT;
                }
            }
        }

        if (buffer.readableBytes() == 0) {
            buffer.release();
        }
    }
}
