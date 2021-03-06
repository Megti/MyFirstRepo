package me.megti.myfirstrepo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World");
        URL url = new URL("https://lfblizzcon.com/available-tickets/");
        final SslContext ctx = SslContextBuilder.forClient().build();
        Bootstrap b = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
                                .addLast(ctx.newHandler(ch.alloc(), url.getHost(), 443))
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(50 * 1024 * 1024))
                                .addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                                        System.out.println(msg);
                                        System.out.println(msg.content().toString(StandardCharsets.UTF_8));
                                    }
                                });
                    }
                });

                 /* b.connect(url.getHost(), 443).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("Connected");
                DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.getPath());
                req.headers().set(HttpHeaderNames.HOST, url.getHost());
                future.channel().writeAndFlush(req);
            } else {
                future.cause().printStackTrace();
            }
        });*/

        Set<String> s = new HashSet<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(() -> {
            try {
                runTest(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        },5,5,TimeUnit.SECONDS);





    }
    public static void runTest(Set<String> s) throws IOException {
        System.out.println("Running Test");
        Document doc = Jsoup.connect("https://lfblizzcon.com/available-tickets/").get();
        Elements list = doc.select("[href*=tickets-forsale]");
        for (Element aList : list) {
            String link = aList.attr("href");
            if (s.add(link)) {
                System.out.println("Bruce is bad");
            }
        }
        System.out.println(s);
    }
}
