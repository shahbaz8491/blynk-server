package cc.blynk.server.servers.hardware;

import cc.blynk.server.Holder;
import cc.blynk.server.handlers.common.AlreadyLoggedHandler;
import cc.blynk.server.hardware.handlers.hardware.HardwareChannelStateHandler;
import cc.blynk.server.hardware.handlers.hardware.mqtt.auth.MqttHardwareLoginHandler;
import cc.blynk.server.servers.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 */
public class MQTTHardwareServer extends BaseServer {

    private final ChannelInitializer<SocketChannel> channelInitializer;

    public MQTTHardwareServer(Holder holder) {
        super(holder.props.getProperty("listen.address"),
                holder.props.getIntProperty("hardware.mqtt.port"), holder.transportTypeHolder);

        int hardTimeoutSecs = holder.limits.hardwareIdleTimeout;
        MqttHardwareLoginHandler mqttHardwareLoginHandler = new MqttHardwareLoginHandler(holder);
        AlreadyLoggedHandler alreadyLoggedHandler = new AlreadyLoggedHandler();
        HardwareChannelStateHandler hardwareChannelStateHandler =
                new HardwareChannelStateHandler(holder);

        channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast("MqttIdleStateHandler", new IdleStateHandler(hardTimeoutSecs, hardTimeoutSecs, 0))
                    .addLast(hardwareChannelStateHandler)
                    .addLast(new MqttDecoder())
                    .addLast(MqttEncoder.INSTANCE)
                    .addLast(mqttHardwareLoginHandler)
                    .addLast(alreadyLoggedHandler);
            }
        };

        log.debug("hard.socket.idle.timeout = {}", hardTimeoutSecs);
    }

    @Override
    public ChannelInitializer<SocketChannel> getChannelInitializer() {
        return channelInitializer;
    }

    @Override
    protected String getServerName() {
        return "Mqtt hardware";
    }

    @Override
    public void close() {
        System.out.println("Shutting down Mqtt hardware server...");
        super.close();
    }

}