import com.alandevise.nettyTool.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Filename: ClientHandler.java
 * @Package: PACKAGE_NAME
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 19:32
 */

@Slf4j
public class ClientHandler  extends ChannelHandlerAdapter {

    // 连接成功监听
    // @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            NettyMessage message = (NettyMessage)msg;
            System.err.println("Client receive message from server: " + message.getBody());

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // 客户端断开连接监听
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.info("----------客户端断开连接-----------");
        ctx.close();
    }

}