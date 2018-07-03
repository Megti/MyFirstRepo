package me.megti.myfirstrepo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws SSLException, MalformedURLException {
        System.out.println("Hello World");
        URL url = new URL("https://lfblizzcon.com/available-tickets/");
        final SslContext ctx = SslContextBuilder.forClient().build();
        Bootstrap b = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(ctx.newHandler(ch.alloc(), url.getHost(), 443))
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(50 * 1024 * 1024))
                                .addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                                        System.out.println(msg);
                                        System.out.println(msg.content().toString(StandardCharsets.UTF_8));
                                    }
                                });
                    }
                });

        b.connect(url.getHost(), 443).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("Connected");
                    DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.getPath());
                    req.headers().set(HttpHeaderNames.HOST, url.getHost());
                    future.channel().writeAndFlush(req);
                } else {
                    future.cause().printStackTrace();
                }
            }
        });
    }
}
