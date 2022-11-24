universe=Packages.com.feed_the_beast.ftblib.lib.data.Universe.get();
TEAMPLAYER=Packages.com.feed_the_beast.ftblib.lib.data.TeamType.PLAYER;
universeplayers=Packages.com.feed_the_beast.ftblib.lib.data.Universe.class.getDeclaredFields()[4];
universeplayers.setAccessible(true);
blocksync=false;
teamlogin=new Packages.java.util.ArrayList();
pullquest=function(team){
    init();
    var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
    if(db==null)return;
    var qs=db.lrange("ftbquest."+team, 0, -1);
    var d=handle.getData(team.getId());
    blocksync=true;
    for(var i=0;i<qs.size();i++){
        var v=qs.get(i).split(",");
        var q=handle.chapters.get(Number(v[0])).quests.get(Number(v[1]));
        if(!q.isComplete(d))
        {
            q.changeProgress(d,comp);
            for(var k=0;k<q.rewards.size();k++){
                var rw=q.rewards.get(k);
                var ps=team.getMembers();
                for(var j=0;j<ps.size();j++)
                    d.setRewardClaimed(ps.get(j).getId(),rw);
            }
        }
    }
    blocksync=false;
}
Tools.Event("com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent",function(e){
    var fp=e.getPlayer();
    if(!fp.hasTeam())return;
    if(teamlogin.contains(fp.team)){
        teamlogin.add(fp.team);
        pullquest(fp.team);
        print(team+" 队伍同步完成!");
    }
});
Tools.Event("com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent",function(e){
    var team=e.getTeam();
    var fp=team.getOwner();
    print(fp.getName()+" 创建队伍!");
    pullquest(team);
    print(team+" 队伍同步完成!");
    Tools.PublishMessage("ftbquest",Packages.com.github.KeyMove.RedisPipeAPI.ServerName+","+fp.getName()+","+fp.getId());
});
Tools.Event("com.feed_the_beast.ftbquests.events.ObjectCompletedEvent",function(e){
    if(blocksync)return;
    init();
    lastquest=e.getObject();
    //print("tq:"+lastquest.id);
    if(task.class.isInstance(lastquest)){
        nextquest=lastquest.quest.id;
        var n=e.getData().getTeamID();
        var c=handle.chapters.indexOf(lastquest.quest.chapter);
        var q=lastquest.quest.chapter.quests.indexOf(lastquest.quest);
        var t=lastquest.quest.tasks.indexOf(lastquest);
        var data=n+","+c+","+q+","+t;
        if(data==lastdata||data==predata)return;
        Tools.PublishMessage("ftbquest",sername+","+data);
        print("task: "+data);
    }
    if(quest.class.isInstance(lastquest)){
        if(nextquest==recvquest&&nextquest==lastquest.id)return;
        var n=e.getData().getTeamID();
        var c=handle.chapters.indexOf(lastquest.chapter);
        var q=lastquest.chapter.quests.indexOf(lastquest);
        var data=n+","+c+","+q;
        if(data==lastdata||data==predata)return;
        Tools.PublishMessage("ftbquest",sername+","+data);
        var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
        if(db!=null)db.lpush("ftbquest."+n, c+","+q);
        print("quest: "+data);
    }
});
Tools.OnMessage("ftbquest",function(e){
    var v=e.split(',');
    init();
    if(v[0]==sername)return;
    var t;
    switch(v.length){
        case 6:
            var d=handle.getData(v[1]);
            var p=Packages.java.util.UUID.fromString(v[4]);
            if(Number(v[2])!=2){
                var rw=handle.getReward(Number(v[3]));
                d.setRewardClaimed(p,rw);
            }
            else{
                for(var i=0;i<handle.chapters.size();i++){
                    var ct=handle.chapters.get(i);
                    for(var j=0;j<ct.quests.size();j++){
                        var qs=ct.quests.get(j);
                        for(var k=0;k<qs.rewards.size();k++){
                            var rw=qs.rewards.get(k);
                            if(!rw.getExcludeFromClaimAll()){
                                d.setRewardClaimed(p,rw);
                            }
                        }
                    }
                }
            }
            print(e);
            return;
            break;
        case 5:
            t=handle.chapters.get(Number(v[2])).quests.get(Number(v[3])).tasks.get(Number(v[4]));
            recvquest=t.quest.id;
            break;
        case 4:
            t=handle.chapters.get(Number(v[2])).quests.get(Number(v[3]));
            break;
        case 3:
            var uuid=Packages.java.util.UUID.fromString(v[2]);
            var fp=universe.getPlayer(uuid);
            if(fp==null)
            {
                fp = new Packages.com.feed_the_beast.ftblib.lib.data.ForgePlayer(universe, uuid, v[1]);
                universeplayers.get(universe).put(uuid, fp);
            }
            if(fp.team.getUID()==0){
                var team=new Packages.com.feed_the_beast.ftblib.lib.data.ForgeTeam(universe, universe.generateTeamUID(0), fp.getName(), TEAMPLAYER);
                fp.team=team;
                team.owner = fp;
                team.universe.addTeam(team);
                team.markDirty();
                fp.markDirty();
                print(fp.getName()+" 创建队伍! ["+team+"]");
                new Packages.com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent(team).post();
            }
            return;
            break;
        default:
            return;
    }
    //print("rq:"+t.id);
    predata=lastdata;
    lastdata=e.substring(v[0].length+1);
    var d=handle.getData(v[1]);
    if(!t.isComplete(d)){
        blocksync=true;
        t.changeProgress(d,comp);
        blocksync=false;
    }
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
    predata="";
    nextquest=-1;
    recvquest=-1;
    print("init ftbq sync");
    init=function(){};
}
// Tools.Event("ServerChatEvent",function(e){
//     lastplayer=e.getPlayer();
//     eval(e.getMessage());
// });
universe=Packages.com.feed_the_beast.ftblib.lib.data.Universe.get();
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
    var mode=0;
    //print(p);
    var data;
    switch(buf.readByte()){
        case 4:
            mode=1;
            id=buf.readInt();
            //print("玩家领取奖励ID:"+id);
            break;
        case 11:
            mode=2;
            //print("玩家领取全部奖励");
            break;
        case 12:
            mode=3;
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
            //print("玩家领取选择奖励ID:"+id+" 选项:"+index);
            break;
    }
    if(mode!=0){
        var fp=universe.getPlayer(p);
        data=fp.team.getId()+","+mode+","+id+","+fp.getId()+",0";
        Tools.PublishMessage("ftbquest",sername+","+data);
    }
});
Tools.Command("ftbqreset","ftbqreset <team>",function(e){
    if(e.args.length==0){
        e.send("ftbqreset <team>重置团队任务数据库");
        return;
    }
    else{
        init();
        var team=universe.getTeam(e.args[0]);
        if(team==null){
            e.send("未找到队伍"+e.args[0]);
            return;
        }
        var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
        if(db==null)return;
        db.del("ftbquest."+team);
        e.send("已重置["+team+"]任务数据库");
    }
})

Tools.Command("ftbqsync","ftbqsync <team>",function(e){
    if(e.args.length==0){
        e.send("ftbqsync <team>同步团队任务数据库");
        return;
    }
    else{
        init();
        var team=universe.getTeam(e.args[0]);
        if(team==null){
            e.send("未找到队伍"+e.args[0]);
            return;
        }
        var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
        if(db==null)return;
        pullquest(team);
        e.send(team+"任务数据库同步完成");
    }
})
