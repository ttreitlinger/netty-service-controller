package controller;

import io.netty.channel.*;
import service.SomeService;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * ControllerHandler - a simple telnet style service console
 */
@Sharable
public class ControllerHandler extends SimpleChannelInboundHandler<String> {

    private static final String PROMPT = "> ";
    private static final char NEWLINE = '\n';
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private static String serviceName = SomeService.class.getSimpleName();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        String now = "[" + dateFormat.format(new Date()) + "] ";
        ctx.write(now + "Welcome to the " + serviceName + " console at " + InetAddress.getLocalHost().getHostName() + NEWLINE);
        ctx.write(now + "Current status: ");
        status(ctx);
        ctx.write(now + "Type 'help', 'start', 'stop', 'restart', 'status', or 'exit'" + NEWLINE);
        ctx.write(now + PROMPT);
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        // Generate and write a response.
        String response = "";
        boolean close = false;

        if (msg.isEmpty()) {
            response = "Please type something.";

        } else if ("help".equals(msg.toLowerCase())) {
            printHelp(ctx);

        } else if ("start".equals(msg.toLowerCase())) {
            start(ctx);

        } else if ("stop".equals(msg.toLowerCase())) {
            stop(ctx);

        } else if ("restart".equals(msg.toLowerCase())) {
            restart(ctx);

        } else if ("status".equals(msg.toLowerCase())) {
            status(ctx);

        } else if ("exit".equals(msg.toLowerCase())) {
            response = "Have a nice day!";
            close = true;

        } else {
            response = "I do not understand '" + msg + "'. Type 'help' for available commands.";
        }

        /* format response */
        String now = "[" + dateFormat.format(new Date()) + "] ";
        String respText;
        if (response.equals("")) {
            respText = now + PROMPT;
        } else {
            respText = now + response + NEWLINE + now + PROMPT;
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(respText);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    public static boolean start(ChannelHandlerContext ctx) throws Exception {
        if (SomeService.start()) {
            ctx.write(serviceName + " started OK" + NEWLINE);
            return true;
        } else {
            ctx.write(serviceName + " start FAILED" + NEWLINE);
            return false;
        }
    }

    public static boolean stop(ChannelHandlerContext ctx) {
        if (SomeService.stop()) {
            ctx.write(serviceName + " stopped OK" + NEWLINE);
            return true;
        } else {
            ctx.write(serviceName + " stop FAILED" + NEWLINE);
            return false;
        }
    }

    public static boolean restart(ChannelHandlerContext ctx) throws Exception {
        return stop(ctx) && start(ctx);
    }

    public static void status(ChannelHandlerContext ctx) {
        if (SomeService.isRunning()) {
            String startTime = dateFormat.format(SomeService.getStartTime());
            ctx.write(serviceName + " running since " + startTime + NEWLINE);
        } else {
            ctx.write(serviceName + " not running" + NEWLINE);
        }
    }


    private void printHelp(ChannelHandlerContext ctx) {

        int len = serviceName.length() + " console".length();

        StringBuilder sb = new StringBuilder();

        sb.append(NEWLINE);
        sb.append("   ").append(serviceName).append(" console").append(NEWLINE);
        sb.append("   ");
        for (int i = 0; i < len; i++) {
            sb.append('-');
        }
        sb.append(NEWLINE);
        sb.append("   start    - start service").append(NEWLINE);
        sb.append("   stop     - stop service").append(NEWLINE);
        sb.append("   restart  - stop and start (reloads configuration)").append(NEWLINE);
        sb.append("   status   - display service status").append(NEWLINE);
        sb.append("   exit     - exit console").append(NEWLINE);
        sb.append(NEWLINE);
        ctx.write(sb.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
