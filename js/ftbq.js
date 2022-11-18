// Tools.Event("ServerChatEvent",function(e){
//     eval(e.getMessage());
// });
Tools.Event("com.feed_the_beast.ftbquests.events.ObjectCompletedEvent",function(e){
    //print(e);
    //print(e.getData().getTeamID());
    //print(e.getData().getTeamUID());
    //print(e.getObject().getParentID());
    //lastdata=e.getData();
    var lastquest=e.getObject();
    if(String(lastquest.getClass()).lastIndexOf("Task")==50){
        var n=e.getData().getTeamID();
        var c=handle.chapters.indexOf(lastquest.quest.chapter);
        var q=lastquest.quest.chapter.quests.indexOf(lastquest.quest);
        var t=lastquest.quest.tasks.indexOf(lastquest);
        var data=n+","+c+","+q+","+t;
        if(data==lastdata)return;
        Tools.PublishMessage("ftbquest",sername+","+data);
    }
});
sername=Packages.com.github.KeyMove.RedisPipeAPI.ServerName;
comp=Packages.com.feed_the_beast.ftbquests.quest.ChangeProgress.COMPLETE;
reset=Packages.com.feed_the_beast.ftbquests.quest.ChangeProgress.RESET;
handle=Packages.com.feed_the_beast.ftbquests.quest.ServerQuestFile.INSTANCE;
task=Packages.com.feed_the_beast.ftbquests.quest.task.Task;
lastdata="";
Tools.OnMessage("ftbquest",function(e){
    var v=e.split(',');
    lastdata=e.substring(v[0].length+1);
    if(v[0]==sername)return;
    handle.chapters.get(Number(v[2])).quests.get(Number(v[3])).tasks.get(Number(v[4])).changeProgress(handle.getData(v[1]),comp);
    print(e);
});

