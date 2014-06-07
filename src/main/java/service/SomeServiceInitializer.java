package service;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Initialize the service pipeline
 */
public class SomeServiceInitializer extends ChannelInitializer<SocketChannel> {

    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(LOGGING_HANDLER);
        pipeline.addLast(new SomeServiceHandler());
    }
}
