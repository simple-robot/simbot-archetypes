//!!REPLACE package ${groupId};

import love.forte.simbot.Components;
import love.forte.simbot.application.Application;
import love.forte.simbot.application.Applications;
import love.forte.simbot.application.BotManagers;
import love.forte.simbot.application.EventProviders;
import love.forte.simbot.bot.BotVerifyInfo;
import love.forte.simbot.bot.BotVerifyInfos;
import love.forte.simbot.bot.JsonBotVerifyInfoDecoder;
import love.forte.simbot.core.application.Simple;
import love.forte.simbot.core.application.SimpleApplication;
import love.forte.simbot.core.application.SimpleApplicationBuilder;
import love.forte.simbot.core.application.SimpleApplicationConfiguration;
import love.forte.simbot.core.event.SimpleListenerBuilder;
import love.forte.simbot.event.EventListenerManager;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.message.Image;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.Messages;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 程序入口
 */
public class Main {
    /*
        首先你需要明白，直接通过Java使用 simbot-core 是会比较“麻烦”的。
        如果你希望能够整合 spring boot，请参考与 java-spring-boot 相关的 archetype 而不是当前的。

        此示例中不默认加载任何组件，因此你需要自行添加所需组件的依赖。
        你可以在 https://github.com/simple-robot 找到它们，或者参考：

            QQ:    https://github.com/simple-robot/simbot-component-mirai
            QQ频道: https://github.com/simple-robot/simbot-component-tencent-guild
            Kook:  https://github.com/simple-robot/simbot-component-kook
     */


    /**
     * The main.
     */
    public static void main(String[] args) {
        SimpleApplication application = Applications.createSimbotApplication(
                // 使用 Simple Application Factory，也就是最基础的Application。
                Simple.INSTANCE, (appConfiguration) -> {
                    // 可以在build开始之前配置一些属性。

                },

                // 适当地拆分了一下配置逻辑
                Main::buildApp);

        // 配置事件处理(注册监听函数)
        configEvents(application);

        // 配置bot(注册bot信息)
        configBots(application);

        /*
            建议先配置监听函数，再注册bot。
            如果先注册bot，则可能在监听函数未注册完成前便开始接收处理事件，进而导致事件丢失。
         */

        // 阻塞当前线程直到 application 被关闭。
        application.joinBlocking();
    }


    /**
     * Application的build阶段配置。
     *
     * @param builder       builder
     * @param configuration 普通配置属性
     */
    private static void buildApp(SimpleApplicationBuilder builder, SimpleApplicationConfiguration configuration) {
        // 启用指定组件
        // builder.install(MiraiComponent.Factory, (config, completionPerceivable) -> Unit.INSTANCE);
        // builder.install(MiraiBotManager.Factory, (config, completionPerceivable) -> Unit.INSTANCE);

        // 或者尝试加载当前环境中所有支持的组件（以及botManager）
        // 尝试加载所有组件信息
        Components.installAllComponents(builder, Main.class.getClassLoader());
        // 尝试加载所有EventProvider
        EventProviders.installAllEventProviders(builder, Main.class.getClassLoader());
    }


    /**
     * 主要是监听函数的注册
     */
    private static void configEvents(Application application) {
        // 得到监听函数管理器
        EventListenerManager eventListenerManager = application.getEventListenerManager();
        // 注册监听函数。
        // 这个监听函数我们实现：
        // 好友发送一句: 你好
        // 就回复一句: 你也好

        // 此处通过 SimpleListenerBuilder 来构建监听函数并注册

        eventListenerManager.register(new SimpleListenerBuilder<>(FriendMessageEvent.Key)
                // 事件匹配
                .match((context, event) -> "你好".equals(context.getTextContent()))
                // 事件处理
                .handle((context, event) -> {
                    // 回复一句 '你也好'
                    // 通过 EventResult.of 配合 replyAsync得到的 CompletableFuture，可以避免出现阻塞调用，会有更好的性能。
                    return EventResult.of(event.replyAsync("你也好"));
                }).build());

        // 如果不需要保证 event.replyAsync 得到结果之后才进入下一个监听函数，
        // 那么可以在 replyAsync 之后便结束它，即忽略掉异步返回值。
        /*
            process((context, event) -> {
                event.replyAsync("你也好");
            });
        */
        // 当然，如果你真的很懒，也可以考虑使用
        /*
            process((context, event) -> {
                event.replyBlocking("你也好");
            });
        */

        // 这里我们再注册一个监听函数，当他收到好友包含图片的消息时，回复一句“哇，是图片耶”
        eventListenerManager.register(new SimpleListenerBuilder<>(FriendMessageEvent.Key)
                // 匹配逻辑
                .match((context, event) -> {
                    // 得到消息列表，看看其中是否存在图片消息
                    final Messages messages = event.getMessageContent().getMessages();
                    for (Message.Element<?> message : messages) {
                        if (message instanceof Image) {
                            return true;
                        }
                    }

                    // 没找到图片类型的消息
                    return false;
                })
                // 处理逻辑
                .process((context, event) -> {
                    // 处理逻辑
                    // 当然，如果你想把 match 匹配逻辑直接放在这里而不单独使用match也是可以的。

                    event.replyAsync("哇，是图片耶！");
                })
                .build());

    }

    /**
     * bot配置。
     */
    private static void configBots(Application application) {
        // 得到所有的bot管理器。它们是在 Application 构建阶段通过 install 注册的。
        BotManagers botManagers = application.getBotManagers();

        // 👇 由于示例没有真正的引用组件，因此下述逻辑暂时置于注释中。
        // for (BotManager<?> botManager : botManagers) {
        //     // 寻找一个你所需要的 provider（也就是BotManager）并使用它
        //     // 比如:
        //     if (botManager instanceof MiraiBotManager miraiManager) {
        //         Bot bot = miraiManager.register(1234L, "PASSWORD");
        //         bot.startAsync();
        //         // ...
        //     }

        // }

        // 或者直接加载自定义配置文件
        try (
                // 此处使用了 JSON 解析器，因此你需要保证你的项目依赖环境中存在 org.jetbrains.kotlinx:kotlinx-serialization-json 依赖。
                final BotVerifyInfo botVerifyInfo = BotVerifyInfos.toBotVerifyInfo(Path.of("my-bot.bot"), JsonBotVerifyInfoDecoder.Factory)) {
            botManagers.register(botVerifyInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

