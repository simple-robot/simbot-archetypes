//!!REPLACE package ${groupId};

import kotlin.Unit;
import love.forte.simbot.Components;
import love.forte.simbot.application.Application;
import love.forte.simbot.application.Applications;
import love.forte.simbot.application.BotRegistrar;
import love.forte.simbot.application.EventProviders;
import love.forte.simbot.bot.BotVerifyInfo;
import love.forte.simbot.bot.BotVerifyInfoDecoderFactory;
import love.forte.simbot.bot.BotVerifyInfos;
import love.forte.simbot.bot.JsonBotVerifyInfoDecoder;
import love.forte.simbot.core.application.Simple;
import love.forte.simbot.core.application.SimpleApplicationBuilder;
import love.forte.simbot.core.application.SimpleApplicationConfiguration;
import love.forte.simbot.core.event.SimpleListenerManagerConfiguration;
import love.forte.simbot.core.event.SimpleListeners;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.message.Image;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.Messages;
import love.forte.simbot.utils.Lambdas;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 程序入口
 */
public class Main {
    /*
        首先你需要明白，直接通过Java使用 simbot-core 是会比较“麻烦”的。
        如果你希望能够整合 spring boot，请参考与 java-spring-boot 相关的 archetype 而不是当前的。
     */

    public static void main(String[] args) {
        final BotVerifyInfo botVerifyInfo = BotVerifyInfos.toBotVerifyInfo(Path.of("my-bot.bot"), (BotVerifyInfoDecoderFactory) JsonBotVerifyInfoDecoder.Factory);
        System.out.println(botVerifyInfo);
    }

    /**
     * The main.
     */
    public static void main1(String[] args) {
        Applications.simbotApplication(
                // 使用 Simple Application，也就是最基础的Application。
                Simple.INSTANCE,
                (appConfiguration) -> {
                    // 在build开始之前配置一些属性。

                    // Kotlin的lambda表达式兼容问题，你需要返回一个 Unit。
                    return Unit.INSTANCE;
                },

                // 适当地拆分了一下配置逻辑
                Lambdas.suspendConsumer(Main::buildApp)
        );
    }


    /**
     * Application的build阶段配置。
     *
     * @param builder       builder
     * @param configuration 普通配置属性
     */
    private static void buildApp(SimpleApplicationBuilder builder, SimpleApplicationConfiguration configuration) {
        // 启用指定组件
        // builder.install(MiraiComponent.Facroty, (config, completionPerceivable) -> Unit.INSTANCE);
        // builder.install(MiraiBotManager.Facroty, (config, completionPerceivable) -> Unit.INSTANCE);

        // 或者尝试加载当前环境中所有支持的组件（以及botManager）
        // 加载所有组件信息
        Components.installAllComponents(builder, Main.class.getClassLoader());
        // 加载所有EventProvider
        EventProviders.installAllEventProviders(builder, Main.class.getClassLoader());

        // 事件处理器配置（主要是监听函数的配置）
        builder.eventProcessor(Main::eventProcessorConfig);

        // bot 加载配置
        builder.bots(Lambdas.suspendConsumer(Main::botConfig));

    }

    /**
     * 事件处理器配置（主要是监听函数的配置）
     */
    private static Unit eventProcessorConfig(SimpleListenerManagerConfiguration listenerManagerConfiguration, Application.Environment environment) {
        // 配置监听函数
        listenerManagerConfiguration.listeners(generator -> {
            // 这个监听函数我们实现：
            // 好友发送一句: 你好
            // 就回复一句: 你也好
            generator.listen(FriendMessageEvent.Key, (builder) -> {
                // 事件匹配
                builder.match((context, event) -> "你好".equals(context.getTextContent()));

                // 事件处理
                builder.handle((context, event) -> {
                    // 回复一句 '你也好'
                    // 通过 EventResult.of 配合 replyAsync得到的 CompletableFuture，可以避免出现阻塞调用。
                    return EventResult.of(event.replyAsync("你也好"));

                    // 当然，如果你真的很懒，也可以考虑使用
                    /*
                        builder.handle((context, event) -> {
                            event.replyBlocking("你也好");
                        });
                     */

                });
            });

            // 配置其他...

            return Unit.INSTANCE;
        });

        // 当然，你也可以用 addListener(...) 添加。
        // 这里我们在配置一个监听函数，当他收到好友包含图片的消息时，回复一句“哇，是图片耶”

        SimpleListeners.listener(FriendMessageEvent.Key,
                (context, event) -> {
                    // 匹配逻辑
                    // 得到消息列表，看看其中是否存在图片消息
                    final Messages messages = event.getMessageContent().getMessages();
                    for (Message.Element<?> message : messages) {
                        if (message instanceof Image) {
                            return true;
                        }
                    }

                    // 没找到图片类型的消息
                    return false;
                }, (context, event) -> {
                    // 处理逻辑
                    // 当然，如果你想把匹配逻辑直接放在这里也是可以的。

                    event.replyBlocking("哇，是图片耶！");
                });


        return Unit.INSTANCE;
    }

    /**
     * bot配置。
     *
     * @param botRegistrar bot注册器
     */
    private static void botConfig(BotRegistrar botRegistrar) {
        // for (EventProvider provider : botRegistrar.getProviders()) {
        // 寻找一个你所需要的 provider（也就是BotManager）并使用它
        // 比如:
        // if (provider instanceof MiraiBotManager) {
        //     provider.register(1234L, "");
        //
        // }

        // }

        // 或者直接加载自定义配置文件
        try (
                // 此处使用了 JSON 解析器，因此你需要保证你的项目依赖环境中存在 org.jetbrains.kotlinx:kotlinx-serialization-json 依赖。
                final BotVerifyInfo botVerifyInfo = BotVerifyInfos.toBotVerifyInfo(Path.of("my-bot.bot"), JsonBotVerifyInfoDecoder.Factory);
        ) {
            botRegistrar.register(botVerifyInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

