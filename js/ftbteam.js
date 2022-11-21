// universe=Packages.com.feed_the_beast.ftblib.lib.data.Universe.get();
// TEAMPLAYER=Packages.com.feed_the_beast.ftblib.lib.data.TeamType.PLAYER;
// print(universe);
// Tools.Event("net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent",function(e){
//     print(e);
//     print(e.player);
//     var fp=universe.getPlayer(e.player);
//     print(fp.team.getUID());
//     // if(fp.team.getUID()==0){
//     //     var team=new Packages.com.feed_the_beast.ftblib.lib.data.ForgeTeam(universe, universe.generateTeamUID(0), fp.getName(), TEAMPLAYER);
//     //     fp.team=team;
//     //     team.owner = fp;
//     //     team.universe.addTeam(team);
//     //     team.markDirty();
//     //     fp.markDirty();
//     // }
//     //print(Universe.class.get().getPlayer(e.player).team);
// });
// ndplayer=Packages.net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.class.getDeclaredFields()[6];
// ndplayer.setAccessible(true);
// Tools.Event("net.minecraftforge.fml.common.network.FMLNetworkEvent$CustomPacketRegistrationEvent",function(e){
//     //print(e);
//     //print(e.getHandler().getClass());
//     var p=ndplayer.get(Packages.net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.get(e.getManager()));
//     var fp=universe.getPlayer(p);
//     if(fp.team.getUID()==0){
//         var team=new Packages.com.feed_the_beast.ftblib.lib.data.ForgeTeam(universe, universe.generateTeamUID(0), fp.getName(), TEAMPLAYER);
//         fp.team=team;
//         team.owner = fp;
//         team.universe.addTeam(team);
//         team.markDirty();
//         fp.markDirty();
//     }
//     //print(p);
//     //print(e.getHandlerType());
// });
// Tools.Event("net.minecraftforge.fml.common.network.FMLNetworkEvent$ServerConnectionFromClientEvent",function(e){
//     print("link start");
//     var p=ndplayer.get(Packages.net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.get(e.getManager()));
//     print(p.func_110124_au());
//     RedisApi.LoadPlayer(p.func_110124_au());
// });
// Tools.Event("ServerChatEvent",function(e){
//     lastplayer=e.getPlayer();
//     eval(e.getMessage());
// });
// pls=Packages.net.minecraft.server.management.PlayerList.class.getDeclaredFields()[15];
// pls.setAccessible(true);
// oldif=pls.get(MinecraftServer.func_184103_al());
// print(oldif);
// newif=Tools.PlayerFileProxy(oldif,
// function(e){
//     var p=e.get(0);
//     RedisApi.LoadPlayer(p.func_110124_au());
//     e.clear();
//     e.add(oldif.func_75752_b(p));
// },
// function(e){
//     oldif.func_75753_a(e);
// });
// print(newif);
// pls.set(MinecraftServer.func_184103_al(),newif);
Tools.Event("net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedOutEvent",function(e){
    print(e);
    var objs=RedisApi.Servers();
    var target=new Packages.java.util.ArrayList();
    print(objs.length);
    for(var i=0;i<objs.size();i++){
        var v=objs.get(i);
        if(!target.contains(v)&&Packages.com.github.KeyMove.RedisPipeAPI.ServerName!=v)
            target.add(v);
    }
    for(var i=0;i<target.size();i++)
        RedisApi.sendPlayer(e.player.func_110124_au(),target.get(i));
});
RedisApi.Servers("");
print(RedisApi.Servers());
//print(Packages.com.github.KeyMove.RedisPipeAPI.database.ltrim("server",1,1));
//print(RedisApi.Servers());