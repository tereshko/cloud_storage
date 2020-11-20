package Network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
    private String HOST;
    private int PORT;

    private static Client instance;

    private Handler handler = new Handler();
    private SocketChannel currentChannel;

    public Client(String host, int port) {
        this.HOST = host;
        this.PORT = port;
        instance = this;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        try {
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    currentChannel = socketChannel;
                    socketChannel.pipeline().addLast(handler);
                }
            });
            ChannelFuture channelFuture = null;
            try {
                channelFuture = b.connect(host, port).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public SocketChannel getCurrentChannel() {
        return currentChannel;
    }

    public Handler getHandler() {
        return handler;
    }

    public static Client getInstance() {
        return instance;
    }
}
