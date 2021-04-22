package cn.org.hentai.jtt1078.app;

import java.net.InetAddress;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import cn.org.hentai.jtt1078.http.GeneralResponseWriter;
import cn.org.hentai.jtt1078.http.NettyHttpServerHandler;
import cn.org.hentai.jtt1078.publisher.PublishManager;
import cn.org.hentai.jtt1078.server.Jtt1078Handler;
import cn.org.hentai.jtt1078.server.Jtt1078MessageDecoder;
import cn.org.hentai.jtt1078.util.Configs;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * RTP流媒体服务器
 * 
 * @author eason
 * @date 2021/04/22
 */
@SpringBootApplication
@Slf4j
@ComponentScan("cn.org.hentai.jtt1078.*")
public class VideoServerApp {

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(VideoServerApp.class);
        app.setBannerMode(Mode.LOG);
        app.run(args);
        Configs.init("/app.properties");
        PublishManager.init();

        VideoServer videoServer = new VideoServer();
        HttpServer httpServer = new HttpServer();

        Signal.handle(new Signal("TERM"), new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                videoServer.shutdown();
                httpServer.shutdown();
            }
        });

        videoServer.start();
        httpServer.start();
    }

    static class VideoServer {
        private static ServerBootstrap serverBootstrap;

        private static EventLoopGroup bossGroup;
        private static EventLoopGroup workerGroup;

        private void start() throws Exception {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, Configs.getInt("server.backlog", 102400));
            bossGroup = new NioEventLoopGroup(
                Configs.getInt("server.worker-count", Runtime.getRuntime().availableProcessors()));
            workerGroup = new NioEventLoopGroup();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel channel) throws Exception {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new Jtt1078MessageDecoder());
                        // p.addLast(new Jtt808MessageEncoder());
                        // p.addLast(new JTT808Handler());
                        p.addLast(new Jtt1078Handler());
                    }
                });

            int port = Configs.getInt("server.port", 1078);
            Channel ch = serverBootstrap.bind(InetAddress.getByName("0.0.0.0"), port).sync().channel();
            log.info("Video Server started at: {}", port);
            ch.closeFuture();
        }

        private void shutdown() {
            try {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class HttpServer {
        private static EventLoopGroup bossGroup;
        private static EventLoopGroup workerGroup;

        private void start() throws Exception {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new GeneralResponseWriter(), new HttpResponseEncoder(),
                            new HttpRequestDecoder(), new HttpObjectAggregator(1024 * 64),
                            new NettyHttpServerHandler());
                    }
                }).option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true);
            try {
                int port = Configs.getInt("server.http.port", 3333);
                ChannelFuture f = bootstrap.bind(InetAddress.getByName("0.0.0.0"), port).sync();
                log.info("HTTP Server started at: {}", port);
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("http server error", e);
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }

        private void shutdown() {
            try {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
