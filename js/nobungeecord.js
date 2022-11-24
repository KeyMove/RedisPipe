// Tools.Event("net.minecraftforge.fml.common.network.FMLNetworkEvent$CustomPacketRegistrationEvent",function(e){
//     print(e);
// });

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
    var uuid=e.player.func_110124_au();
    RedisApi.SavePlayer(uuid);
    for(var i=0;i<target.size();i++)
        RedisApi.sendPlayer(uuid,target.get(i));
});