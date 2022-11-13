# RedisPipe
[Minecraft Server Plugin]基于Redis的跨服同步插件
# 开始
RedisPipe.jar分别放入BungeeCord的plugins文件夹与Minecraft 1.12.2服务器plugins文件夹<BR>
Redis服务器默认使用6379端口<BR>
服务器启动完成后输入 rp server [服务器名称] 指令修改当前服务器名称<BR>
再输入 rp save 保存当前配置<BR>
完成之后使用 rptp [玩家] [服务器名称] 传送玩家到指定服务器 <BR>
玩家也能使用rptp [服务器名称] 传送到指定服务器<BR>

# 注意事项
必须由rptp指令传送玩家才会同步数据<BR>
如果使用了server指令切换服务器，玩家数据还是子服务器的旧数据。<BR>
上下线并不会触发同步操作，所以bungeecord的force_default_server选项必须为false<BR>


# 指令
/rp reload 重新加载配置文件<BR>
/rp link <ip> <port> 连接数据库<BR>
/rp server [Name] 设置服务器名称<BR>
/rp save [Name] 保存配置<BR>
/rp setspawn <PlayerName> 以指定玩家位置设置出生点<BR>
  
/rptp [服务器] 传送到目标服务器<BR>
/rptp [玩家] [服务器] 传送玩家到目标服务器<BR>
/rptp [玩家] [服务器IP] [服务器端口] 传送玩家到目标服务器(!!!)<BR>
  
# 配置文件
#数据库地址<BR>
Host: localhost<BR>
#端口号<BR>
Port: 6379<BR>
#服务器名称<BR>
ServerName: lobby<BR>
#出现同名服务器也强制连接到数据库<BR>
CoverServer: true<BR>
#服务器信息中显示真实IP<BR>
RealIP: false<BR>
#是否启用跨服聊天<BR>
ServerChat: true<BR>
#是否启用跨服背包同步<BR>
ServerBackpack: true<BR>
#跨服聊天格式  服务器名称采用 %server% 玩家名称采用%player% 聊天信息采用%message%<BR>
ServerChatFormat: '[%server%] <%player%> %message%'<BR>
#世界出生点<BR>
SpwanPoint: ''<BR>
