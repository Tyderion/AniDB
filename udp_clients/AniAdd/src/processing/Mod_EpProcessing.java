package processing;

import aniAdd.Communication.ComListener;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import processing.FileInfo.eAction;
import udpApi.Cmd;
import udpApi.Query;

import aniAdd.IAniAdd;
import aniAdd.Module;
import aniAdd.misc.ICallBack;
import aniAdd.misc.Mod_Memory;
import aniAdd.misc.Misc;
import aniAdd.misc.MultiKeyDict;
import aniAdd.misc.MultiKeyDict.IKeyMapper;
import java.util.TreeMap;
import udpApi.Mod_UdpApi;

public class Mod_EpProcessing implements Module {

    public static String[] supportedFiles = {"avi", "mpg", "mpeg", "rm", "rmvb", "asf", "wmv", "mov", "ogm", "mp4", "mkv", "rar", "zip", "ace", "srt", "sub", "ssa", "smi", "idx", "ass", "txt", "swf", "flv"};
    private IAniAdd aniAdd;
    private Mod_UdpApi api;
    private Mod_Memory mem;
    private MultiKeyDict<String, Object, FileInfo> files;
    private ArrayList<Integer> index2Id;
    private FileParser fileParser;
    private boolean isProcessing;
    private boolean isPaused;
    private int lastFileId;

    public Mod_EpProcessing() {
        lastFileId = 0;
        files = new MultiKeyDict<String, Object, FileInfo>(new IKeyMapper<String, Object, FileInfo>() {

            public int count() {
                return 2;
            }

            public int getCatIndex(String category) {
                return category.equals("Id") ? 0 : (category.equals("Path") ? 1 : -1);
            }

            public Object getKey(int index, FileInfo fileInfo) {
                return index == 0 ? fileInfo.Id() : (index == 1 ? fileInfo.FileObj().getAbsolutePath() : null);
            }
        });
        index2Id = new ArrayList<Integer>();
    }

    // <editor-fold defaultstate="collapsed" desc="Processing"> 
    private void processEps() {
        while(isPaused) try {Thread.sleep(100);} catch (InterruptedException ex) {}

        for (FileInfo procFile : files.values()) {
            if (!procFile.Served()) {
                procFile.Served(true);
                //System.out.println("Processing: " + procFile.FileObj().getAbsolutePath());
                Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.Processing, procFile.Id());

                fileParser = new FileParser(procFile.FileObj(), new ICallBack<FileParser>() {
                    public void invoke(FileParser fileParser) {
                        continueProcessing(fileParser);
                    }
                }, procFile.Id());
                fileParser.start();
                return;
            }
        }
        isProcessing = false;
        Log(ComEvent.eType.Information, eComType.Status, eComSubType.Done);
        //System.out.println("Processing done");
    }
    private void continueProcessing(FileParser fileParser) {
        this.fileParser = null;

        FileInfo procFile = files.get("Id", fileParser.Tag());


        //System.out.println("Cont Processing: " + id);

        procFile.Data().put("Ed2k", fileParser.Hash());
        procFile.ActionsDone().add(eAction.Process);
        procFile.ActionsTodo().remove(eAction.Process);

        Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.ParsingDone, procFile.Id(), fileParser);

        boolean sendML = procFile.ActionsTodo().contains(eAction.MyListCmd);
        boolean sendFile = procFile.ActionsTodo().contains(eAction.FileCmd);


        if(sendFile) requestDBFileInfo(procFile);
        if(sendML) requestDBMyList(procFile);
        
        Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.GetDBInfo, procFile.Id(), sendFile, sendML);

        if (isProcessing) processEps();
    }

    private void requestDBFileInfo(FileInfo procFile) {
        Cmd cmd = new Cmd("FILE", "file", procFile.Id().toString(), true);

        BitSet binCode = new BitSet(32);
        binCode.set(6); //aid
        binCode.set(5); //eid
        binCode.set(4); //gid
        binCode.set(3); //'lid
        binCode.set(1); //'Depr
        binCode.set(0); //'state
        binCode.set(23); //'Quality
        binCode.set(22); //'Source
        binCode.set(24); //'anidb filename scheme
        cmd.setArgs("fmask", Misc.toMask(binCode, 32));

        binCode = new BitSet(32);
        binCode.set(31); //'group name
        binCode.set(30); //'group short name
        binCode.set(23); //'epno
        binCode.set(22); //'ep name
        binCode.set(21); //'ep romaji
        binCode.set(20); //'ep kanji
        binCode.set(7); //'epCount
        binCode.set(6); //'highest EpCount
        binCode.set(4); //'type
        binCode.set(15); //'romaji name
        binCode.set(14); //'kanji name
        binCode.set(13); //'english name
        binCode.set(12); //'other name
        binCode.set(11); //'short name
        binCode.set(10); //'synonym
        cmd.setArgs("amask", Misc.toMask(binCode, 32));
        cmd.setArgs("size", Long.toString(procFile.FileObj().length()));
        cmd.setArgs("ed2k", procFile.Data().get("Ed2k"));

        api.queryCmd(cmd);
        //System.out.println("Sending File Cmd");
    }
    private void requestDBMyList(FileInfo procFile) {
        Cmd cmd = new Cmd("MYLISTADD", "mladd", procFile.Id().toString(), true);
        cmd.setArgs("size", Long.toString(procFile.FileObj().length()));
        cmd.setArgs("ed2k", (String)procFile.Data().get("Ed2k"));
        cmd.setArgs("viewed", procFile.ActionsTodo().contains(eAction.Watched) ? "1" : "0");
        cmd.setArgs("state", Integer.toString(procFile.MLStorage().ordinal()));
        
        if(procFile.Data().containsKey(("Other"))) cmd.setArgs("other", (String)procFile.Data().get("Other"));
        if(procFile.Data().containsKey(("Source"))) cmd.setArgs("source", (String)procFile.Data().get("Source"));
        if(procFile.Data().containsKey(("Storage"))) cmd.setArgs("storage", (String)procFile.Data().get("Storage"));

        api.queryCmd(cmd);
        //System.out.println("Sending ML Cmd");
    }
    private void requestDBVote(FileInfo procFile) {
        Cmd cmd = new Cmd("VOTE", "vote", procFile.Id().toString(), true);
        cmd.setArgs("id", (String) procFile.Data().get("AId"));
        cmd.setArgs("type", true ? "1" : "2"); //decision missing (Perm/Temp Vote)
        cmd.setArgs("value", (String) procFile.Data().get("Vote"));
        cmd.setArgs("epno", (String) procFile.Data().get("EpNo"));

        api.queryCmd(cmd);
    }

    private void aniDBInfoReply(int queryId) {
        //System.out.println("Got Fileinfo reply");

        Query query = api.Queries().get(queryId);
        int replyId = query.getReply().ReplyId();

        int fileId = Integer.parseInt(query.getReply().Tag());
        if (!files.contains("Id", fileId)) {
            return; //File not found (Todo: throw error)
        }
        FileInfo procFile = files.get("Id", fileId);
        procFile.ActionsTodo().remove(eAction.FileCmd);

        if (replyId == 320 || replyId == 505 || replyId == 322) {
            procFile.ActionsError().add(eAction.FileCmd);
            Log(ComEvent.eType.Information, eComType.FileEvent,replyId==320?eComSubType.FileCmd_NotFound:eComSubType.FileCmd_Error, procFile.Id());
        } else {
            procFile.ActionsDone().add(eAction.FileCmd);
            ArrayDeque<String> df = new ArrayDeque<String>(query.getReply().DataField());
            procFile.Data().put("DB_FId", df.poll());
            procFile.Data().put("DB_AId", df.poll());
            procFile.Data().put("DB_EId", df.poll());
            procFile.Data().put("DB_GId", df.poll());
            procFile.Data().put("DB_LId", df.poll());
            procFile.Data().put("DB_Deprecated", df.poll());
            procFile.Data().put("DB_State", df.poll());
            procFile.Data().put("DB_Quality", df.poll());
            procFile.Data().put("DB_Source", df.poll());
            procFile.Data().put("DB_FileName", df.poll());

            procFile.Data().put("DB_EpCount", df.poll());
            procFile.Data().put("DB_EpHiCount", df.poll());
            procFile.Data().put("DB_Type", df.poll());
            procFile.Data().put("DB_SN_Romaji", df.poll());
            procFile.Data().put("DB_SN_Kanji", df.poll());
            procFile.Data().put("DB_SN_English", df.poll());
            procFile.Data().put("DB_SN_Other", df.poll());
            procFile.Data().put("DB_SN_Short", df.poll());
            procFile.Data().put("DB_SN_Synonym", df.poll());
            procFile.Data().put("DB_EpNo", df.poll());
            procFile.Data().put("DB_EpN_English", df.poll());
            procFile.Data().put("DB_EpN_Romaji", df.poll());
            procFile.Data().put("DB_EpN_Kanji", df.poll());
            procFile.Data().put("DB_Group_Long", df.poll());
            procFile.Data().put("DB_Group_Short", df.poll());
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.FileCmd_GotInfo, procFile.Id());
        }

        if (!procFile.IsFinal() && !(procFile.ActionsTodo().contains(eAction.FileCmd) || (procFile.ActionsTodo().contains(eAction.MyListCmd)))) {
            finalProcessing(procFile);
        }
    }
    private void aniDBMyListReply(int queryId) {
        //System.out.println("Got ML Reply");

        Query query = api.Queries().get(queryId);
        int replyId = query.getReply().ReplyId();

        int fileId = Integer.parseInt(query.getReply().Tag());
        if (!files.contains("Id", fileId)) {
            //System.out.println("MLCmd: Id not found");
            return; //File not found (Todo: throw error)
        }
        FileInfo procFile = files.get("Id", fileId);
        procFile.ActionsTodo().remove(eAction.MyListCmd);

        if (replyId == 210 || replyId == 311) {
            //File Added/Edited
            procFile.ActionsDone().add(eAction.MyListCmd);
            if(procFile.ActionsTodo().remove(eAction.Watched)){
                procFile.ActionsDone().add(eAction.Watched);
            }
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.MLCmd_FileAdded, procFile.Id());
            
        } else if (replyId == 310) {
            //File Already Added
            procFile.ActionsTodo().add(eAction.MyListCmd);
            Cmd cmd = new Cmd(query.getCmd(), true);
            cmd.setArgs("edit", "1");
            api.queryCmd(cmd);
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.MLCmd_AlreadyAdded, procFile.Id());

        } else {
            procFile.ActionsError().add(eAction.MyListCmd);
            if(procFile.ActionsTodo().remove(eAction.Watched)){
                procFile.ActionsError().add(eAction.Watched);
            }

            if (replyId == 320 || replyId == 330 || replyId == 350) {
                Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.MLCmd_NotFound, procFile.Id());
            } else {
                Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.MLCmd_Error, procFile.Id());
            }
        }

        if (!procFile.IsFinal() && !(procFile.ActionsTodo().contains(eAction.FileCmd) || (procFile.ActionsTodo().contains(eAction.MyListCmd)))) {
            finalProcessing(procFile);
        }
    }
    private void aniDBVoteReply(int queryId) {
        Query query = api.Queries().get(queryId);
        int replyId = query.getReply().ReplyId();

        int fileId = Integer.parseInt(query.getReply().Tag());
        if (!files.contains("Id", fileId)) {
            return; //File not found (Todo: throw error)
        }
        FileInfo procFile = files.get("Id", fileId);
        procFile.ActionsTodo().remove(eAction.MyListCmd);

        if (replyId == 260 && replyId == 262) {
            procFile.Data().put("Voted", "true"); //Voted
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.VoteCmd_EpVoted, procFile.Id());
        } else if (replyId == 263) {
            procFile.Data().put("Voted", "false");//Revoked
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.VoteCmd_EpVoteRevoked, procFile.Id());
        } else if (replyId == 363) {
            //PermVote Not Allowed
            Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.VoteCmd_Error, procFile.Id());
        }
    }

    private void finalProcessing(FileInfo procFile) {
        //System.out.println("Final processing");
        procFile.IsFinal(true);
        if (procFile.Data().get("Vote") != null) {
            requestDBVote(procFile);
        }

        if (procFile.ActionsTodo().contains(eAction.Rename) && procFile.ActionsDone().contains(eAction.FileCmd)) {
            procFile.ActionsTodo().remove(eAction.Rename);

            if(renameFile(procFile)) {
                procFile.ActionsDone().add(eAction.Rename);
            } else {
                procFile.ActionsError().add(eAction.Rename);
                Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.RenamingFailed, procFile.Id());
            }
        }
        
        Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.Done, procFile.Id());
    }

    private boolean renameFile(FileInfo procFile){
        try {
            String path="";
            TreeMap<String,String> ts = getPathFromTagSystem(procFile);

            if((Boolean)mem.get("EnableFileMove")){
                if((Boolean)mem.get("MoveTypeUseFolder")){
                    path = (String)mem.get("MoveToFolder");
                    if((Boolean) mem.get("AppendAnimeTitle")){
                        int titleType = (Integer)mem.get("AppendAnimeTitleType");
                        path += titleType==0?procFile.Data().get("DB_SN_English"):(titleType==1?procFile.Data().get("DB_SN_Romaji"):procFile.Data().get("DB_SN_Kanji")) + java.io.File.separatorChar;
                    }

                } else {
                    path = ts.get("PathName");
                }
            }

            path = path.replaceAll("[:\"/*|<>?]", "");
            if(path.isEmpty()){
                path = procFile.FileObj().getParent() + java.io.File.separatorChar;
            }

            if((Boolean)mem.get("RenameTypeAniDBFileName")){
                path += procFile.Data().get("DB_FileName");
            } else {
                String ext = procFile.FileObj().getName().substring(procFile.FileObj().getName().lastIndexOf("."));
                path += ts.get("FileName").replaceAll("[\\\\:\"/*|<>?]", "") + ext;
            }
            
            //System.out.println(path);

            File renFile = new File(path);
            if(procFile.FileObj().renameTo(renFile)){
                Log(ComEvent.eType.Information, eComType.FileEvent, eComSubType.FileRenamed, procFile.Id(), procFile.FileObj(), renFile);
                procFile.FileObj(renFile);
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    private TreeMap<String,String> getPathFromTagSystem(FileInfo procFile) {
        TagSystem ts = new TagSystem();
        TreeMap<String, String> tags = new TreeMap<String, String>();
        tags.put("ATr", procFile.Data().get("DB_SN_Romaji"));
        tags.put("ATe", procFile.Data().get("DB_SN_English"));
        tags.put("ATk", procFile.Data().get("DB_SN_Kanji"));
        tags.put("ATs", procFile.Data().get("DB_Synonym"));
        tags.put("ATo", procFile.Data().get("DB_SN_Other"));

        tags.put("ETr", procFile.Data().get("DB_EpN_Romaji"));
        tags.put("ETe", procFile.Data().get("DB_EpN_English"));
        tags.put("ETk", procFile.Data().get("DB_EpN_Kanji"));

        tags.put("GTs", procFile.Data().get("DB_Group_Short"));
        tags.put("GTl", procFile.Data().get("DB_Group_Long"));

        tags.put("EpNo", procFile.Data().get("DB_EpNo"));
        tags.put("EpHiNo", procFile.Data().get("DB_EpHiCount"));
        tags.put("EpCount", procFile.Data().get("DB_EpCount"));

        tags.put("Quality", procFile.Data().get("DB_Quality"));
        tags.put("Source", procFile.Data().get("DB_Source"));
        tags.put("Type", procFile.Data().get("DB_Type"));

        tags.put("Watched", procFile.ActionsDone().contains(eAction.Watched) ? "1" : "");
        tags.put("Depr", procFile.Data().get("DB_Deprecated").equals("1") ? "1" : "");
        tags.put("Cen", ((Integer.valueOf(procFile.Data().get("DB_State")) & 8) != 0 ? "1" : ""));

        //String path = "";
        try {
            ts.parseAndTransform((String) mem.get("TagSystemCode"), tags);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return tags;
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Public Methods"> 
    public FileInfo id2FileInfo(int id) {
        return files.get("Id", id);
    }
    public FileInfo index2FileInfo(int index) {
        return files.get("Id", index2Id.get(index));
    }
    public int Index2Id(int index){ return index2Id.get(index); }
    public int Id2Index(int id){ return Misc.binarySearch(index2Id, id); }

    public int FileCount() {
        return files.size();
    }

    public void addFiles(Collection<File> newFiles) {
        Integer storage = (Integer) mem.get("SetStorageType");
        boolean watched = (Boolean) mem.get("SetWatched");
        boolean rename = (Boolean) mem.get("RenameFiles");
        boolean addToMyList = (Boolean) mem.get("AddToMyList");

        for (File cf : newFiles) {
            if (files.contains("Path", cf.getAbsolutePath())) {
                //continue;
            }

            FileInfo fileInfo = new FileInfo(cf, lastFileId);
            fileInfo.MLStorage(FileInfo.eMLStorageState.values()[storage]);
            fileInfo.ActionsTodo().add(eAction.Process);
            fileInfo.ActionsTodo().add(eAction.FileCmd);
            if (addToMyList) {
                fileInfo.ActionsTodo().add(eAction.MyListCmd);
            }
            if (watched) {
                fileInfo.ActionsTodo().add(eAction.Watched);
            }
            if (rename) {
                fileInfo.ActionsTodo().add(eAction.Rename);
            }

            index2Id.add(lastFileId++);
            files.put(fileInfo);
        }
        Log(ComEvent.eType.Information, eComType.FileCountChanged);
    }
    public void addFile(File cf) {
        ArrayList<File> lst = new ArrayList<File>();
        lst.add(cf);
        addFiles(lst);
    }
    public void delFile(int index) {
        files.remove("Id", index2Id.get(index));
        index2Id.remove(index);
        Log(ComEvent.eType.Information, eComType.FileCountChanged);
    }

    public void processing(eProcess proc) {
        switch (proc) {
            case Start:
                //System.out.println("Processing started");
                Log(ComEvent.eType.Information, eComType.Status, eProcess.Start);
                isProcessing = true;
                isPaused = false;
                processEps();
                break;
            case Pause:
                //System.out.println("Processing paused");
                Log(ComEvent.eType.Information, eComType.Status, eProcess.Pause);
                isPaused = true;
                if(fileParser!=null) fileParser.pause();
                break;
            case Resume:
                //System.out.println("Processing resumed");
                Log(ComEvent.eType.Information, eComType.Status, eProcess.Resume);
                if(fileParser!=null) fileParser.resume();
                isPaused = false;
                break;
            case Stop:
                //System.out.println("Processing stopped");
                Log(ComEvent.eType.Information, eComType.Status, eProcess.Stop);
                //Not yet supported
                isProcessing = false;
                isPaused = false;
                break;
        }
    }

    public boolean isPaused() {return isPaused;}
    public boolean isProcessing(){return isProcessing;}
    
    public int processedFileCount(){
        int count=0;
        for (FileInfo fi : files.values()) {
            if(fi.ActionsDone().contains(eAction.Process)) count++;
        }
        return count;
    }
    public long processedBytes(){
        long count=0;
        for (FileInfo fi : files.values()) {
            if(fi.ActionsDone().contains(eAction.Process)) count += fi.FileObj().length();
        }
        //if(fileParser!=null) count += fileParser.getBytesRead();

        return count;
    }
    public long processedBytesCurrentFile(){
        long count=0;
        if(fileParser!=null) count += fileParser.getBytesRead();
        return count;
    }
    public long totalBytesCurrentFile() { return fileParser!=null?fileParser.getByteCount():0; }
    public long totalBytes(){
        long count=0;
        for (FileInfo fi : files.values()) {
            count += fi.FileObj().length();
        }
        return count;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="IModule"> 
    protected String modName = "EpProcessing";
    protected eModState modState = eModState.New;
    
    public eModState ModState() { return modState; }
    public String ModuleName() {return modName;}
    public void Initialize(IAniAdd aniAdd) {
        modState = eModState.Initializing;
        
        this.aniAdd = aniAdd;
        aniAdd.AddComListener(new AniAddEventHandler());
        mem = (Mod_Memory)aniAdd.GetModule("Memory");
        api = (Mod_UdpApi)aniAdd.GetModule("UdpApi");
        
        api.registerEvent(new ICallBack<Integer>() {
            public void invoke(Integer queryIndex) {
                aniDBInfoReply(queryIndex);
            }
        }, "file");
        api.registerEvent(new ICallBack<Integer>() {
            public void invoke(Integer queryIndex) {
                aniDBMyListReply(queryIndex);
            }
        }, "mladd", "mldel");
        api.registerEvent(new ICallBack<Integer>() {
            public void invoke(Integer queryIndex) {
            }
        }, "vote");
        
        modState = eModState.Initialized;
   }
    public void Terminate() {
        modState = eModState.Terminating;

        isProcessing = false;
        if(fileParser != null) fileParser.terminate();

        modState = eModState.Terminated;
    }    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Com System"> 
	private ArrayList<ComListener> listeners = new ArrayList<ComListener>();
    protected void ComFire(ComEvent comEvent){
        for (ComListener listener : listeners) {
            listener.EventHandler(comEvent);
        }
    }
	public void AddComListener(ComListener comListener){ listeners.add(comListener); }
	public void RemoveComListener(ComListener comListener){ listeners.remove(comListener); }
    class AniAddEventHandler implements ComListener{
        public void EventHandler(ComEvent comEvent) {

        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Misc"> 
    protected void Log(ComEvent.eType type, Object... params){
        ComFire(new ComEvent(this, type, params));
    }
    
    public enum eProcess {
        Start, Pause, Resume, Stop
    }
    
	public enum eComType {
		FileSettings,
		FileCountChanged,
		FileEvent,
		Status
    }
    public enum eComSubType{
		Processing,
		NoWriteAccess,
		GotFromHistory,
		ParsingDone,
		ParsingError,
		GetDBInfo,
		FileCmd_NotFound,
		FileCmd_GotInfo,
		FileCmd_Error,
		MLCmd_FileAdded,
		MLCmd_AlreadyAdded,
		MLCmd_FileRemoved,
		MLCmd_NotFound,
		MLCmd_Error,
		VoteCmd_EpVoted,
		VoteCmd_EpVoteRevoked,
		VoteCmd_Error,
		RenamingFailed,
		FileRenamed,
		RenamingNotNeeded,
		RelFilesRenamed,
		RelFilesRenamingFailed,
		Done
    }
    // </editor-fold>
}









































