// Tools.Event("ServerChatEvent",function(e){
//     lastplayer=e.getPlayer();
//     eval(e.getMessage());
// });

universe=Packages.com.feed_the_beast.ftblib.lib.data.Universe.get();
TEAMPLAYER=Packages.com.feed_the_beast.ftblib.lib.data.TeamType.PLAYER;
universeplayers=Packages.com.feed_the_beast.ftblib.lib.data.Universe.class.getDeclaredFields()[4];
universeplayers.setAccessible(true);
teamlogin=new Packages.java.util.ArrayList();
teamname=Packages.com.feed_the_beast.ftblib.lib.util.FinalIDObject.class.getDeclaredFields()[0];
teamname.setAccessible(true);
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
blocksync=false;
blockteamsync=false;

loadteam=function(){
    var db = Packages.com.github.KeyMove.RedisPipeAPI.database;
    if (db == null) return;
    var qs = db.lrange("ftbteam", 0, -1);
    for(var i=0;i<qs.size();i++){
        teamsync(qs.get(i));
    }
}

teamsync=function(t){
    var db = Packages.com.github.KeyMove.RedisPipeAPI.database;
    if (db == null) return;
    blockteamsync = true;
    try {
        var plist = null;
        var team = universe.getTeam(t+"");
        var qs = db.lrange("ftbteam." + t, 0, -1);
        //print(qs);
        for (var i = 0; i < qs.size(); i++) {
            var v = qs.get(i).split(",");
            var uuid = Packages.java.util.UUID.fromString(v[1]);
            var fp = universe.getPlayer(uuid);
            if (fp == null) {
                fp = new Packages.com.feed_the_beast.ftblib.lib.data.ForgePlayer(universe, uuid, v[0]);
                universeplayers.get(universe).put(uuid, fp);
            }
            //print(fp.getName()+"=="+v[0]);
            //print(team.getUID()+":"+team);
            if (team.getUID() == 0) {
                if (fp.getName() == v[0]) {
                    i = -1;
                    team = new Packages.com.feed_the_beast.ftblib.lib.data.ForgeTeam(universe, universe.generateTeamUID(0), fp.getName(), TEAMPLAYER);
                    fp.team = team;
                    team.owner = fp;
                    team.universe.addTeam(team);
                    team.markDirty();
                    fp.markDirty();
                    new Packages.com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent(team).post();
                }
            }
            if (team.getUID() != 0) {
                if (plist == null)
                    plist = team.getMembers();
                plist.remove(fp);
                //print(plist);
                if (fp.team != team) {
                    fp.team = team;
                    print("add " + fp);
                    team.markDirty();
                    fp.markDirty();
                }
            }
        }
        if (plist != null)
            for (var i = 0; i < plist.size(); i++) {
                team.removeMember(plist.get(i));
                print("remove " + plist.get(i));
            }
    }
    catch (e) {
        print(e);
    }
    blockteamsync = false;
}

pullquest = function (team) {
    init();
    var db = Packages.com.github.KeyMove.RedisPipeAPI.database;
    if (db == null) return;
    var qs = db.lrange("ftbquest." + team, 0, -1);
    var d = handle.getData(team.getId());
    blocksync = true;
    for (var i = 0; i < qs.size(); i++) {
        var v = qs.get(i).split(",");
        var q = handle.chapters.get(Number(v[0])).quests.get(Number(v[1]));
        if (!q.isComplete(d)) {
            q.changeProgress(d, comp);
            for (var k = 0; k < q.rewards.size(); k++) {
                var rw = q.rewards.get(k);
                d.setRewardClaimed(team.getOwner().getId(), rw);
                var ps = universe.players.iterator();
                while(ps.hasNext())
                {
                    var fp=ps.next();
                    if(fp.team!=team)continue;
                    d.setRewardClaimed(fp.getId(), rw);
                }
            }
            d.markDirty();
        }
    }
    blocksync = false;
}
Tools.Event("com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent",function(e){
    if(blockteamsync)return;
    var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
    if(db==null)return;
    db.rpush("ftbteam."+e.getTeam(), e.getPlayer().getName()+","+e.getPlayer().getId());
    Tools.PublishMessage("ftbquest",Packages.com.github.KeyMove.RedisPipeAPI.ServerName+","+e.getTeam());
});
Tools.Event("com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent",function(e){
    if(blockteamsync)return;
    var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
    if(db==null)return;
    db.lrem("ftbteam."+e.getTeam(),1, e.getPlayer().getName()+","+e.getPlayer().getId());
    Tools.PublishMessage("ftbquest",Packages.com.github.KeyMove.RedisPipeAPI.ServerName+","+e.getTeam());
});
Tools.Event("com.feed_the_beast.ftblib.events.team.ForgeTeamOwnerChangedEvent",function(e){
    if(e.getOldOwner().getName().toLowerCase()==e.getTeam().getId()){
        e.getTeam().setStatus(e.getOldOwner(),Packages.com.feed_the_beast.ftblib.lib.EnumTeamStatus.OWNER);
    }
});
Tools.Event("net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent",function(e){
    var fp=universe.getPlayer(e.player);
    if(!fp.hasTeam())return;
    if(!teamlogin.contains(fp.team.getId())){
        teamlogin.add(fp.team.getId());
        pullquest(fp.team);
        print(fp.team+" 队伍同步完成!");
    }
});
Tools.Event("com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent",function(e){
    var team=e.getTeam();
    var fp=team.getOwner();
    teamname.set(team,fp.getName().toLowerCase());
    print(fp.getName()+" 创建队伍!");
    if(!blockteamsync){
        pullquest(team);
        print(team+" 队伍同步完成!");
    }
    var db=Packages.com.github.KeyMove.RedisPipeAPI.database;
    if(db==null)return;
    db.lrem("ftbteam",1, team.getId());
    db.rpush("ftbteam", team.getId());
    //Tools.PublishMessage("ftbquest",Packages.com.github.KeyMove.RedisPipeAPI.ServerName+","+fp.getName()+","+fp.getId());
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
        if(db!=null)db.rpush("ftbquest."+n, c+","+q);
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
        case 2:
            print(e);
            
            teamsync(v[1]);
            return;
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
    comp=Packages.com.feed_the_beast.ftbquests.quest.ChangeProgress.COMPLETE;
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

loadteam();
