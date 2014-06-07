/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Service skeleton
 *
 * TODO: initialise from Spring config files
 * TODO: on service stop, destroy Spring context
 *
 */
public final class SomeService {

    private static Logger logger = LoggerFactory.getLogger(SomeService.class);

    static final int PORT = 9001;

    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static boolean running;
    private static Date startTime;

    public static synchronized boolean start() throws Exception {

        if (running) {
            return false;
        }

        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new SomeServiceInitializer());

        // Start the server.
        bootstrap.bind(PORT).sync();
        running = true;
        startTime = new Date();
        logger.info("Started " + SomeService.class.getName() + " on port " + PORT);
        return true;
    }

    public static synchronized boolean stop() {

        if (!running) {
            return false;
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        running = false;
        logger.info("Stopped  " + SomeService.class.getName());
        return true;
    }

    public static boolean isRunning() {
        return running;
    }

    public static Date getStartTime() {
        return startTime;
    }
}
