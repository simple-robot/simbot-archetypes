//!!REPLACE package ${groupId}

import love.forte.simbot.ID
import love.forte.simbot.application.Application
import love.forte.simbot.core.application.createSimpleApplication
import love.forte.simbot.core.application.listeners
import love.forte.simbot.core.event.EventListenersGenerator
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.installAll

/**
 * 当前程序入口
 */
suspend fun main() {
    val application = createSimpleApplication {
        listeners {
            // 注册一个事件监听
            friendHello()
        }
        
        // 尝试加载当前环境中所有支持的组件等信息。
        // 你也可以通过 install(...) 或者 useXxx { ... } 来定制化环境。
        installAll()
        
        
        bots {
            // 注册你的bot信息..
            /*
            
             // 例如注册一个mirai bot:
             mirai {
                 register(123456L, "PASSWORD") {
                     // ...
                 }
             }
             // 注意！
             // 如果你需要使用mirai组件，那么你需要自行添加依赖：
             // groupId:    love.forte.simbot.component
             // artifactId: simbot-component-mirai-core
             // version:    3.0.0.0-beta-M3
             // 对于其他组件来讲也是大同小异的。
             
             */
        }
        
    }
    
    // 输出信息
    application.showAllBotsAndContacts()
    
    // 挂起application直到被关闭
    application.join()
}


/**
 * 收到好友的‘你好’，回复一句‘你也好’
 */
private fun EventListenersGenerator.friendHello() {
    // 监听事件类型 'FriendMessageEvent' (好友消息事件)
    FriendMessageEvent { event ->
        // 回复‘你也好’
        event.reply("你也好")
        
        // 你也可以使用 'friend().send(...)' 来实现比较“干净”的回复行为
        // event.friend().send("你也好")
    } onMatch {
        // 条件: 好友的消息是‘你好’
        textContent?.trim() == "你好"
    }
    
    // 你也可以使用下述风格的代码注册监听事件
    /*
    listen(FriendMessageEvent) {
        match { textContent?.trim() == "你好" }
        process { event ->
            event.reply("你也好")
        }
    }
    */
    
    
    
}

/**
 * 获取当前环境中所有的bot（任何组件），以及它们的所有联系人，并在控制台中输出这些信息。
 *
 * 尝试寻找有没有id为 '114514' 的好友，如果有，发送一句 “哼哼”
 *
 */
private suspend fun Application.showAllBotsAndContacts() {
    botManagers.flatMap { it.all() }.forEach { bot ->
        println("BOT(id=${bot.id}, username=${bot.username}, component=${bot.component})")
        bot.contacts.collect { contact ->
            println("\tContact(id=${contact.id}, username=${contact.username})")
            if (contact.id == 114514.ID) {
                contact.send("哼哼")
            }
        }
    
        // Contact 并不一定代表 Friend（好友），如果希望得到Friend信息：
        /*
        if (bot is FriendsContainer) {
            bot.friends // ...
        }
        */
        
    }
}