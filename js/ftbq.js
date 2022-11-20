Tools.Event("com.feed_the_beast.ftbquests.events.ObjectCompletedEvent",function(e){
    if(init!=null){
        init();
        init=null;
        print("init ftbq sync")
    }
    lastquest=e.getObject();
    if(task.class.isInstance(lastquest)){
        var n=e.getData().getTeamID();
        var c=handle.chapters.indexOf(lastquest.quest.chapter);
        var q=lastquest.quest.chapter.quests.indexOf(lastquest.quest);
        var t=lastquest.quest.tasks.indexOf(lastquest);
        var data=n+","+c+","+q+","+t;
        if(data==lastdata)return;
        Tools.PublishMessage("ftbquest",sername+","+data);
        print("task: "+data);
    }
    if(quest.class.isInstance(lastquest)){
        if(lastquest.tasks.size()==0)return;
        var n=e.getData().getTeamID();
        var c=handle.chapters.indexOf(lastquest.chapter);
        var q=lastquest.chapter.quests.indexOf(lastquest);
        var data=n+","+c+","+q;
        if(data==lastdata)return;
        Tools.PublishMessage("ftbquest",sername+","+data);
        print("quest: "+data);
    }
});
Tools.OnMessage("ftbquest",function(e){
    var v=e.split(',');
    lastdata=e.substring(v[0].length+1);
    if(v[0]==sername)return;
    var t;
    switch(v.length){
        case 5:
            t=handle.chapters.get(Number(v[2])).quests.get(Number(v[3])).tasks.get(Number(v[4]));
            break;
        case 4:
            t=handle.chapters.get(Number(v[2])).quests.get(Number(v[3]));
            break;
        default:
            return;
    }
    var d=handle.getData(v[1]);
    if(t.isComplete(d))
        t.changeProgress(d,comp);
    print(e);
});
init=function(){
    task=Packages.com.feed_the_beast.ftbquests.events.ObjectCompletedEvent$TaskEvent;
    quest=Packages.com.feed_the_beast.ftbquests.quest.Quest;
    sername=Packages.com.github.KeyMove.RedisPipeAPI.ServerName;
    comp=Packages.com.feed_the_beast.ftbquests.quest.ChangeProgress.COMPLETE_DEPS;
    reset=Packages.com.feed_the_beast.ftbquests.quest.ChangeProgress.RESET;
    handle=Packages.com.feed_the_beast.ftbquests.quest.ServerQuestFile.INSTANCE;
    task=Packages.com.feed_the_beast.ftbquests.quest.task.Task;
    lastdata="";
}
Tools.Event("ServerChatEvent",function(e){
    eval(e.getMessage());
});
questwarp=Packages.com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
qf=questwarp.class.getDeclaredFields()[0];
qf.setAccessible(true);
questwarp=qf.get(questwarp.class);
print(questwarp);
ss=Packages.net.minecraftforge.fml.relauncher.Side.SERVER;
pipe=questwarp.getChannel(ss);
sc=Packages.net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
type=pipe.findChannelHandlerNameForType(sc.class);
print(cr=Packages.com.feed_the_beast.ftbquests.net.MessageClaimReward);
ndplayer=Packages.net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.class.getDeclaredFields()[6];
ndplayer.setAccessible(true);
Tools.ChannelBefor(pipe.pipeline(),type,"qb",function(e){
    var buf=e.payload();
    var p=ndplayer.get(e.getDispatcher());
    var l=buf.readableBytes();
    buf=buf.copy(0,l>10?10:l);
    var id=0;
    var index=0;
    print(p);
    switch(buf.readByte()){
        case 4:
            id=buf.readInt();
            print("玩家领取奖励ID:"+id);
            break;
        case 11:
            print("玩家领取全部奖励");
            break;
        case 12:
            id=buf.readInt();
            switch (index=buf.readByte()) {
            case 121:
                index=buf.readByte();
                break;
            case 122:
                index=buf.readShort();
                break;
            case 123:
                index=buf.readInt();
                break;
            }
            print("玩家领取选择奖励ID:"+id+" 选项:"+index);
            break;
    }
});