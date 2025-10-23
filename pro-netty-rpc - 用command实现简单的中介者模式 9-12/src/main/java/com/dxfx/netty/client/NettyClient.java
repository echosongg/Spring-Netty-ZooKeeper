package com.dxfx.netty.client;

import com.dxfx.netty.handler.SimpleClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import io.netty.channel.EventLoopGroup;
public class NettyClient {
	public static void main(String[] args) throws InterruptedException {
        String host = "localhost";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,Delimiters.lineDelimiter()[0]));
                	ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new SimpleClientHandler());
                    ch.pipeline().addLast(new StringEncoder());
                }
            });
            
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            System.out.println("Get Connected");
            f.channel().writeAndFlush("giving request to server");
            f.channel().writeAndFlush("\r\n");

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            //Object result = f.channel().attr(AttributeKey.valueOf("sssss")).get();
            //System.out.println("获取到服务器的数据"+result.toString());
        }
        catch (Exception e) {
            // 捕获其他所有异常（如连接失败、I/O 错误等）
            System.err.println("客户端发生错误：" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
            System.out.println("client close");
        }
	}
}
